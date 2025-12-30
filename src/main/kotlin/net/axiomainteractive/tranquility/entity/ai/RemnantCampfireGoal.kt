package net.axiomainteractive.tranquility.entity.ai

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.block.Blocks
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import java.util.EnumSet
import kotlin.random.Random

class RemnantCampfireGoal(private val mob: RemnantEntity) : Goal() {

    private enum class Activity { IDLE, PATROL }
    
    // Internal Goal State
    private var currentActivity = Activity.IDLE
    private var targetPos: BlockPos? = null
    private var actionTimer = 0
    private var actionCooldown = 0
    private var ticks = 0
    
    // Building State
    private var buildHeight = 0
    private var targetHeight = 0

    init {
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK))
    }

    override fun canStart(): Boolean {
        // Run if no combat target.
        // Also, we need to have a squad leader or be one to really function well,
        // but even loners should try to survive.
        return this.mob.target == null
    }

    override fun start() {
        this.resetState()
    }

    private fun resetState() {
        this.currentActivity = Activity.IDLE
        this.targetPos = null
        this.mob.isBuilding = false
        this.mob.isChopping = false
        this.mob.isCooking = false
        this.actionTimer = 0
    }

    override fun tick() {
        this.ticks++
        if (this.actionCooldown > 0) this.actionCooldown--

        // 1. Leader Logic (The Brain)
        if (this.mob.isSquadLeader) {
            tickLeaderLogic()
        }

        // 2. Activity Logic (The Body)
        // Only act if we have a home (Campfire) established by the leader (or us).
        if (this.mob.homePos != null) {
             // Debug Particles
             val world = this.mob.world
             if (world is net.minecraft.server.world.ServerWorld) {
             if (world is net.minecraft.server.world.ServerWorld) {
                 if (!this.mob.isSquadLeader && this.mob.homePos != null) {
                     // Followers = Smoke (Guards)
                     world.spawnParticles(net.minecraft.particle.ParticleTypes.SMOKE, this.mob.x, this.mob.eyeY + 0.5, this.mob.z, 1, 0.0, 0.0, 0.0, 0.0)
                 }
             }
             }
             
             when (this.currentActivity) {
                Activity.IDLE -> decideNextActivity()
                Activity.PATROL -> tickPatrol()
            }
        }
    }
    
    private fun tickLeaderLogic() {
        // Increment global clock
        if (this.mob.homePos != null) {
            this.mob.campTicks++
            // Immediate Urbanization upon base establishment
            this.mob.invasionPhase = "URBANIZING"
            
            val state = this.mob.world.getBlockState(this.mob.homePos!!)
            if (state.block != Blocks.CAMPFIRE && this.ticks % 100 == 0) {
                 tryPlaceCampfire()
            }
        } else {
             // ... (Keep existing home finding logic)
            this.mob.campTicks = 0
            this.mob.invasionPhase = "SCOUTING"
            
            val nearbyCampfire = BlockPos.findClosest(this.mob.blockPos, 20, 10) { 
                this.mob.world.getBlockState(it).block == Blocks.CAMPFIRE 
            }
            
            if (nearbyCampfire.isPresent) {
                this.mob.homePos = nearbyCampfire.get()
            } else {
                if (!tryPlaceCampfire()) {
                    this.mob.homePos = this.mob.blockPos
                }
            }
        }
    }

    private fun tryPlaceCampfire(): Boolean {
         val pos = this.mob.blockPos.offset(this.mob.horizontalFacing)
         val stateBelow = this.mob.world.getBlockState(pos.down())
         val state = this.mob.world.getBlockState(pos)
         
         // Standard check (Solid Ground + Air)
         if (stateBelow.isSolid && (state.isAir || state.isReplaceable)) {
             this.mob.world.setBlockState(pos, Blocks.CAMPFIRE.defaultState)
             this.mob.homePos = pos
             // Back up slightly so we don't stand in fire
             this.mob.navigation.startMovingTo(this.mob.x - this.mob.rotationVector.x, this.mob.y, this.mob.z - this.mob.rotationVector.z, 1.0)
             return true
         }
         
         // Fallback: Try AT feet if offset failed
         val posFeet = this.mob.blockPos
         val stateBelowFeet = this.mob.world.getBlockState(posFeet.down())
         if (stateBelowFeet.isSolid && (this.mob.world.getBlockState(posFeet).isAir || this.mob.world.getBlockState(posFeet).isReplaceable)) {
             this.mob.world.setBlockState(posFeet, Blocks.CAMPFIRE.defaultState)
             this.mob.homePos = posFeet
             return true
         }
         
         return false
    }


    
    // --- ACTIONS ---
    
    // PATROL
    private fun startPatrol() {
        val home = this.mob.homePos ?: return
        val dx = Random.nextInt(-15, 16)
        val dz = Random.nextInt(-15, 16)
        val p = home.add(dx, 0, dz)
        this.mob.navigation.startMovingTo(p.x.toDouble(), p.y.toDouble(), p.z.toDouble(), 1.0)
        this.currentActivity = Activity.PATROL
        this.actionTimer = 140 
    }
    
    private fun tickPatrol() {
        this.actionTimer--
        if (this.actionTimer <= 0 || this.mob.navigation.isIdle) {
            this.resetState()
            this.actionCooldown = 15
        }
    }

    private fun decideNextActivity() {
        if (this.actionCooldown > 0) return
        
        // LEADER: If base established, he relies on RemnantBuildGoal (Priority 2).
        // Here (Priority 6), he should just idle or patrol lightly if BuildGoal fails?
        // Actually, if we want him to ONLY Build, we should restrict him here.
        if (this.mob.isSquadLeader) {
             // Do nothing here. Let BuildGoal handle it.
             return
        }
        
        // FOLLOWER LOGIC: Torch or Patrol
        val rng = this.mob.random.nextFloat()
        if (this.mob.homePos != null && rng < 0.4f) {
             tryPlacePerimeterTorch()
        }
        
        this.startPatrol()
    }
    
    // UTILS
    private fun clearObstructions() {
         val box = this.mob.boundingBox
         val start = BlockPos.ofFloored(box.minX, box.minY, box.minZ)
         val end = BlockPos.ofFloored(box.maxX, box.maxY + 1.0, box.maxZ)
         BlockPos.stream(start, end).forEach { pos ->
             if (this.mob.world.getBlockState(pos).isIn(net.minecraft.registry.tag.BlockTags.LEAVES) || 
                 this.mob.world.getBlockState(pos).isIn(net.minecraft.registry.tag.BlockTags.LOGS)) {
                 this.mob.world.breakBlock(pos, true)
             }
         }
    }

    private fun tryPlacePerimeterTorch() {
          val home = this.mob.homePos ?: return
          val pos = this.mob.blockPos
          val dist = kotlin.math.sqrt(pos.getSquaredDistance(home))
          // Logic: Only place if solid ground
          if (dist > 10.0 && this.mob.world.getBlockState(pos).isAir && this.mob.world.getBlockState(pos.down()).isSolid) {
               val nearby = BlockPos.findClosest(pos, 5, 5) { this.mob.world.getBlockState(it).block == Blocks.REDSTONE_TORCH }
               if (nearby.isEmpty) {
                    this.mob.world.setBlockState(pos, Blocks.REDSTONE_TORCH.defaultState)
                    this.mob.swingHand(net.minecraft.util.Hand.MAIN_HAND)
               }
          }
    }
}
