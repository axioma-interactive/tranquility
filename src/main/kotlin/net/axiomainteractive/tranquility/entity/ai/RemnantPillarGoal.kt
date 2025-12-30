package net.axiomainteractive.tranquility.entity.ai

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.block.Blocks
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.BlockPos
import kotlin.math.abs

class RemnantPillarGoal(private val mob: RemnantEntity) : Goal() {

    private var targetY: Double = 0.0

    init {
        this.setControls(java.util.EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP, Goal.Control.LOOK))
    }

    override fun canStart(): Boolean {
        val target = this.mob.target ?: return false

        // Check horizontal distance
        // Must be very close (within 4 blocks)
        // Check horizontal distance (Ignore Y)
        // Must be very close (within 4 blocks horizontally)
        val dx = target.x - this.mob.x
        val dz = target.z - this.mob.z
        val distSqHorizontal = dx * dx + dz * dz
        
        if (distSqHorizontal > 16.0) return false 

        // Check vertical distance
        // Target must be slightly higher (1.2 blocks)
        if (target.y < this.mob.y + 1.2) return false
        
        // Mob must be on ground OR in water to start pillaring
        return this.mob.isOnGround || this.mob.isTouchingWater
    }

    override fun start() {
        this.mob.isBuilding = true
        this.targetY = this.mob.target?.y ?: (this.mob.y + 3.0)
        // Stop navigation to focus on building vertical
        this.mob.navigation.stop()
    }

    override fun tick() {
        // Equip Dirt
        if (this.mob.mainHandStack.item != Items.DIRT) {
            this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.DIRT))
        }

        // Look down
        // Use lookControl to avoid "snapping" conflict if goal exits briefly
        // But for strict 90 degree down, manual set is often needed.
        this.mob.lookControl.lookAt(this.mob.x, this.mob.y - 1.0, this.mob.z)
        this.mob.pitch = 90.0f
        this.mob.headYaw = this.mob.bodyYaw
        
        // Jump !
        if (this.mob.isOnGround) {
            this.mob.jump()
        }
        
        // Place block logic
        // If we are in the air, and there is air below us, place dirt.
        // We need to be slightly above the block position to place it comfortably without suffocating.
        // In vanilla, players jump and place at the peak.
        
        val posUnder = BlockPos.ofFloored(this.mob.x, this.mob.y - 0.5, this.mob.z) // Current block we might be inside or above
        val posToPlace = BlockPos.ofFloored(this.mob.x, this.mob.y - 1.0, this.mob.z) // Block below feet
        
        // Only place if we are high enough (dy > 0)
        // Or simplified: if current block below feet is AIR, place it.
        // But we must be jumping.
        
        if (this.mob.velocity.y > 0.1) { // Rising
             // Check if we can place
             if (this.mob.world.getBlockState(posToPlace).isAir || this.mob.world.getBlockState(posToPlace).isLiquid) {
                 // Place!
                 this.placeBlock(posToPlace)
             } else {
                 // Maybe we are higher up?
                 // Let's try to place exactly under us if we are high enough
                 val posExactUnder = BlockPos.ofFloored(this.mob.x, this.mob.y - 0.1, this.mob.z)
                 if (this.mob.world.getBlockState(posExactUnder).isAir) {
                     this.placeBlock(posExactUnder)
                 }
             }
        }
    }
    
    private fun placeBlock(pos: BlockPos) {
        // Place Dirt
        this.mob.world.setBlockState(pos, Blocks.DIRT.defaultState)
        this.mob.world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0f, 0.8f)
        
        // Reset velocity slightly to "hop" on it? No, vanilla physics handle landing.
    }

    override fun shouldContinue(): Boolean {
        val target = this.mob.target ?: return false
        // Continue if target is still higher
        // And we are not obstructed?
        return this.mob.isBuilding && target.y > this.mob.y - 0.5 && this.mob.y < this.targetY + 2.0 && target.y >= this.mob.y - 1.0
    }

    override fun stop() {
        this.mob.isBuilding = false
        // Reset pitch?
        this.mob.pitch = 0.0f
    }
}
