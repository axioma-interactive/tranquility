package net.axiomainteractive.tranquility.entity.ai

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.block.Blocks
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.EnumSet
import kotlin.random.Random

class RemnantBuildGoal(private val mob: RemnantEntity) : Goal() {

    private enum class State { SEARCHING, MOVING, ALIGNING, PILLARING, CAPPING, RESETTING }
    private var state = State.SEARCHING
    private var targetPos: BlockPos? = null
    private var buildHeight = 0
    private var targetHeight = 0
    private var failSafeTimer = 0

    init {
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK))
    }

    override fun canStart(): Boolean {
        // Priority 1: Combat Override
        if (this.mob.target != null) return false
        
        return this.mob.isSquadLeader && this.mob.homePos != null
    }

    override fun shouldContinue(): Boolean {
        // Yield if we get a target
        return this.mob.target == null && super.shouldContinue()
    }

    override fun start() {
        this.reset()
    }

    private fun reset() {
        this.state = State.SEARCHING
        this.targetPos = null
        this.mob.isBuilding = false
        this.failSafeTimer = 0
    }

    override fun tick() {
        // Global Fail-safe
        this.failSafeTimer++
        if (this.failSafeTimer > 1200) { // 60s stuck check
             this.reset()
        }
        
        // Debug
        val world = this.mob.world
        if (world is net.minecraft.server.world.ServerWorld) {
             val pType = when (state) {
                 State.SEARCHING -> net.minecraft.particle.ParticleTypes.WITCH
                 State.MOVING -> net.minecraft.particle.ParticleTypes.CLOUD
                 State.PILLARING -> net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER
                 State.CAPPING -> net.minecraft.particle.ParticleTypes.FLAME
                 else -> net.minecraft.particle.ParticleTypes.SMOKE
             }
             world.spawnParticles(pType, this.mob.x, this.mob.eyeY + 0.8, this.mob.z, 1, 0.0, 0.0, 0.0, 0.0)
        }

        when (state) {
            State.SEARCHING -> {
                this.mob.isBuilding = false 
                if (findTowerSpot()) {
                    this.state = State.MOVING
                    this.failSafeTimer = 0 // Found one, reset timer
                }
            }
            State.MOVING -> {
                val pos = this.targetPos ?: run { reset(); return }
                this.mob.isBuilding = true
                
                // Navigation
                if (pos.getSquaredDistance(this.mob.blockPos) < 2.5) {
                    this.state = State.ALIGNING
                    this.mob.navigation.stop()
                } else {
                    if (this.mob.navigation.isIdle) {
                        this.mob.navigation.startMovingTo(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, 1.3)
                    }
                    if (this.failSafeTimer % 10 == 0) clearObstructions()
                }
            }
            State.ALIGNING -> {
                val pos = this.targetPos ?: run { reset(); return }
                // Physics Override: Snap to center
                this.mob.setPosition(pos.x + 0.5, this.mob.y, pos.z + 0.5)
                this.mob.velocity = Vec3d.ZERO
                this.mob.velocityDirty = true
                
                this.buildHeight = 0
                this.targetHeight = Random.nextInt(5, 12)
                this.state = State.PILLARING
            }
            State.PILLARING -> {
                val base = this.targetPos ?: run { reset(); return }
                
                // Equip
                if (this.mob.mainHandStack.item != Items.OAK_PLANKS) {
                    this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.OAK_PLANKS))
                }
                
                // Look Down
                this.mob.lookControl.lookAt(this.mob.x, this.mob.y - 2.0, this.mob.z)
                this.mob.pitch = 90.0f
                
                // Check Height
                if (this.mob.blockPos.y - base.y >= this.targetHeight) {
                    this.state = State.CAPPING
                    return
                }
                
                // Turbo Jump
                if (this.mob.isOnGround) this.mob.jump()
                
                // Place Check
                val posUnder = this.mob.blockPos.down()
                val state = world.getBlockState(posUnder)
                
                if (this.mob.y > posUnder.y + 0.9 && (state.isAir || state.isReplaceable)) {
                     world.setBlockState(posUnder, Blocks.OAK_PLANKS.defaultState)
                     this.mob.swingHand(net.minecraft.util.Hand.MAIN_HAND)
                }
                
                // Anti-Stuck: Force move up if stuck in block
                if (this.failSafeTimer % 10 == 0 && world.getBlockState(this.mob.blockPos).isSolid) {
                    this.mob.setPosition(this.mob.x, this.mob.y + 1.0, this.mob.z)
                }
            }
            State.CAPPING -> {
                 this.mob.navigation.stop()
                 val capPos = this.mob.blockPos
                 
                 // Physics Snap: Ensure on center
                 this.mob.setPosition(capPos.x + 0.5, this.mob.y, capPos.z + 0.5)
                 
                 // Ensure Solid Block Below
                 if (!world.getBlockState(capPos.down()).isSolid) {
                      world.setBlockState(capPos.down(), Blocks.OAK_PLANKS.defaultState)
                 }
                 
                 // Force Torch at feet
                 world.setBlockState(capPos, Blocks.REDSTONE_TORCH.defaultState)
                 this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.REDSTONE_TORCH))
                 this.mob.swingHand(net.minecraft.util.Hand.MAIN_HAND)
                 
                 this.state = State.RESETTING
            }
            State.RESETTING -> {
                 // Teleport Down
                 val base = this.targetPos ?: this.mob.blockPos
                 val ground = base.north()
                 // Ensure safe landing (not in wall)
                 var landingY = base.y
                 while (landingY < base.y + 10 && world.getBlockState(BlockPos(ground.x, landingY, ground.z)).isSolid) {
                     landingY++
                 }
                 
                 this.mob.setPosition(ground.x + 0.5, landingY.toDouble(), ground.z + 0.5)
                 this.reset()
            }
        }
    }
    
    private fun findTowerSpot(): Boolean {
         val home = this.mob.homePos ?: return false
         
         // Aggressive Search
         for (i in 0..10) {
             val dx = Random.nextInt(-35, 36)
             val dz = Random.nextInt(-35, 36)
             val spot = home.add(dx, 0, dz)
             
             // 1. Spacing Check (Don't build too close to other towers)
             val nearby = BlockPos.findClosest(spot, 6, 20) { 
                 this.mob.world.getBlockState(it).block == Blocks.REDSTONE_TORCH && it.y > spot.y - 5
             }
             if (nearby.isPresent) continue
             
             // 2. Ground Check
             // Find the surface at this X, Z
             val world = this.mob.world
             var y = spot.y + 10
             val minY = world.bottomY + 5
             
             while (y > minY) {
                 val p = BlockPos(spot.x, y, spot.z)
                 if (world.getBlockState(p).isSolid && world.getBlockState(p.up()).isAir) {
                     this.targetPos = p.up()
                     return true
                 }
                 y--
             }
         }
         return false
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
}
