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
import net.minecraft.util.math.Vec3d
import kotlin.math.abs

class RemnantBridgeGoal(private val mob: RemnantEntity) : Goal() {

    private var targetBlock: BlockPos? = null
    private var cooldown: Int = 0

    init {
        // We don't want to take over MOVE, we want to bridge WHILE moving (or briefly pause)
        // actually, to place block, we might need to pause or just place while running
        // Let's take MOVE control to ensure we don't fall off before placing
        this.setControls(java.util.EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK))
    }

    override fun canStart(): Boolean {
        if (this.cooldown > 0) {
            this.cooldown--
            return false
        }
        
        val target = this.mob.target ?: return false

        // Check if chasing on similar level
        if (abs(target.y - this.mob.y) > 3.0) return false // Too vertical, let Pillar/JumpAttack handle
        
        // Scan for gap logic
        // Look ahead in direction of movement
        var velocity = this.mob.velocity
        if (velocity.lengthSquared() < 0.01) {
            // Use look vector if stationary
            velocity = this.mob.rotationVector
        }
        
        val lookDir = velocity.normalize()
        
        // Check block 1-2 blocks ahead and 1 block down
        // Position: Mob pos + (LookDir * 1.5) - (0, 1, 0)
        val aheadPos = this.mob.pos.add(lookDir.multiply(1.5))
        val blockPosAhead = BlockPos.ofFloored(aheadPos.x, this.mob.y - 1.0, aheadPos.z)
        
        val state = this.mob.world.getBlockState(blockPosAhead)
        
        // If it is AIR (and not liquid, let's say), we need to bridge
        if (state.isAir) {
            // Strict Check: Only bridge if there is a deep drop (Void/Chasm)
            // If the block BELOW that is solid, it's just a 1-block step down. We can walk that.
            val blockBelow = blockPosAhead.down()
            val stateBelow = this.mob.world.getBlockState(blockBelow)
            
            if (stateBelow.isAir || stateBelow.isLiquid) {
                // It is a drop of at least 2 blocks. Bridge it.
                this.targetBlock = blockPosAhead
                return true
            }
        }
        
        return false
    }

    override fun start() {
        this.mob.isBuilding = true
        this.mob.navigation.stop()
    }

    override fun tick() {
         val pos = this.targetBlock ?: return
         
         // Equip Dirt
        if (this.mob.mainHandStack.item != Items.DIRT) {
            this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.DIRT))
        }
        
        // Look at placement
        this.mob.lookControl.lookAt(pos.toCenterPos())
        
        // Place
        this.mob.world.setBlockState(pos, Blocks.DIRT.defaultState)
        this.mob.world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0f, 0.8f)
        
        // Done
        this.targetBlock = null
    }

    override fun shouldContinue(): Boolean {
        return this.targetBlock != null && this.mob.isBuilding
    }

    override fun stop() {
        this.mob.isBuilding = false
        this.targetBlock = null
        this.cooldown = 10 // Short cooldown
    }
}
