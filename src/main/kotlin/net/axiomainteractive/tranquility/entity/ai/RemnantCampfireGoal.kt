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
            
            // Maintenance: Check Altar Integrity
            if (this.ticks % 100 == 0) {
                 val base = this.mob.homePos!!
                 val world = this.mob.world
                 
                 // 1. Base (Gold)
                 if (world.getBlockState(base).block != Blocks.GOLD_BLOCK) {
                      world.setBlockState(base, Blocks.GOLD_BLOCK.defaultState)
                 }
                 
                 // 2. Mid (Netherrack)
                 if (world.getBlockState(base.up()).block != Blocks.NETHERRACK) {
                      world.setBlockState(base.up(), Blocks.NETHERRACK.defaultState)
                 }
                 
                 // 3. Top (Fire)
                 val firePos = base.up(2)
                 if (world.getBlockState(firePos).block != Blocks.FIRE && world.getBlockState(firePos).block != Blocks.SOUL_FIRE) {
                      // Equip Flint & Steel behaviorally
                      this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.FLINT_AND_STEEL))
                      this.mob.swingHand(net.minecraft.util.Hand.MAIN_HAND)
                      world.setBlockState(firePos, Blocks.FIRE.defaultState)
                 }
            }
        } else {
             // FIND HOME
            this.mob.campTicks = 0
            this.mob.invasionPhase = "SCOUTING"
            
            val nearbyAltar = BlockPos.findClosest(this.mob.blockPos, 20, 10) { 
                this.mob.world.getBlockState(it).block == Blocks.GOLD_BLOCK 
            }
            
            if (nearbyAltar.isPresent) {
                this.mob.homePos = nearbyAltar.get()
            } else {
                if (!tryBuildAltar()) {
                    this.mob.homePos = this.mob.blockPos // Temporary
                }
            }
        }
    }

    private fun tryBuildAltar(): Boolean {
         // Find flat spot
         val pos = this.mob.blockPos.offset(this.mob.horizontalFacing)
         val world = this.mob.world
         
         // Basic check: Solid ground below, Air above
         if (world.getBlockState(pos.down()).isSolid && world.getBlockState(pos).isReplaceable) {
             
             // Build Altar
             world.setBlockState(pos, Blocks.GOLD_BLOCK.defaultState)
             world.setBlockState(pos.up(), Blocks.NETHERRACK.defaultState)
             world.setBlockState(pos.up(2), Blocks.FIRE.defaultState)
             
             this.mob.homePos = pos
             
             // Back up to admire (and not burn)
             this.mob.navigation.startMovingTo(this.mob.x - this.mob.rotationVector.x * 2, this.mob.y, this.mob.z - this.mob.rotationVector.z * 2, 1.0)
             
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
