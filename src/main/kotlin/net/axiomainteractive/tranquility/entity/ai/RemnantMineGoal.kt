package net.axiomainteractive.tranquility.entity.ai

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext
import java.util.EnumSet

class RemnantMineGoal(private val mob: RemnantEntity) : Goal() {

    private var targetPos: BlockPos? = null
    private var miningTicks: Int = 0
    private var lastProgress: Int = -1
    private var cooldown: Int = 0

    init {
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK))
    }

    override fun canStart(): Boolean {
        val target = this.mob.target ?: return false

        // 1. Check if we have a direct path?
        // If navigation path is complete/partial but reaches target, we don't need to mine.
        if (this.mob.navigation.isFollowingPath) return false
        
        // 2. Are we hugging the target?
        if (this.mob.squaredDistanceTo(target) < 4.0) return false

        // 3. Cooldown
        if (this.cooldown > 0) {
             this.cooldown--
             return false
        }
        
        // 4. Raycast check
        return scanForBlock()
    }

    private fun scanForBlock(): Boolean {
         val target = this.mob.target ?: return false
         val start = this.mob.eyePos
         val targetEye = target.eyePos
         val vecToTarget = targetEye.subtract(start).normalize()
         
         // Candidates: Pos -> Score (Higher is better)
         var bestPos: BlockPos? = null
         var bestScore = -1
         
         // Scan angles: 0 (Center), -30 (Left), +30 (Right)
         val angles = listOf(0.0, -0.5, 0.5) // Radians roughly approx
         
         for (angle in angles) {
             // Rotate vecToTarget around Y axis
             val x = vecToTarget.x * kotlin.math.cos(angle) - vecToTarget.z * kotlin.math.sin(angle)
             val z = vecToTarget.x * kotlin.math.sin(angle) + vecToTarget.z * kotlin.math.cos(angle)
             val rayDir = net.minecraft.util.math.Vec3d(x, vecToTarget.y, z).normalize().multiply(4.5) // Range 4.5
             val end = start.add(rayDir)
             
             val hit = this.mob.world.raycast(RaycastContext(
                 start, end,
                 RaycastContext.ShapeType.COLLIDER,
                 RaycastContext.FluidHandling.NONE,
                 this.mob
             ))
             
             if (hit.type == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                 val pos = hit.blockPos
                 // Evaluate this spot
                 val state = this.mob.world.getBlockState(pos)
                 if (state.getHardness(this.mob.world, pos) < 0) continue // Skip unbreakable
                 
                 // Check block BEHIND it (estimated)
                 // Or simple heuristic: Distance to target from this block?
                 // Or depth check: Is the block AFTER this one air?
                 val vecBehind = hit.pos.add(rayDir.normalize().multiply(1.1))
                 val posBehind = BlockPos.ofFloored(vecBehind)
                 val stateBehind = this.mob.world.getBlockState(posBehind)
                 
                 var score = 1
                 if (stateBehind.isAir) {
                     score = 10 // Thinnest wall!
                 }
                 
                 if (score > bestScore) {
                     bestScore = score
                     bestPos = pos
                 }
             }
         }
         
         if (bestPos != null) {
             this.targetPos = bestPos
             return true
         }
         
         return false
    }

    override fun start() {
        this.miningTicks = 0
        this.lastProgress = -1
        // Scan was done in canStart
        if (this.targetPos != null) {
            this.mob.isMining = true
        }
    }

    override fun tick() {
        val pos = this.targetPos ?: return
        
        // Move close to block
        if (this.mob.squaredDistanceTo(pos.toCenterPos()) > 4.5) {
            this.mob.navigation.startMovingTo(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), 1.0)
        } else {
             this.mob.navigation.stop()
        }

        // Look and break
        this.mob.lookControl.lookAt(pos.toCenterPos())
        
        if (this.mob.mainHandStack.item != Items.STONE_PICKAXE) {
            this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_PICKAXE))
        }
        
        // Swing
        this.mob.swingHand(Hand.MAIN_HAND)
        
        this.miningTicks++
        
        // Break logic (Simplified)
        val state = this.mob.world.getBlockState(pos)
        val hardness = state.getHardness(this.mob.world, pos)
        
        if (hardness < 0) { // Unbreakable
             this.stop()
             return
        }
        
        val speed = 0.05f // Slower
        var breakingProgress = (this.miningTicks * speed) / (hardness + 0.1f)
        
        if (breakingProgress > 1.0f) breakingProgress = 1.0f
        
        val progressInt = (breakingProgress * 10).toInt()
        if (progressInt != this.lastProgress) {
            this.mob.world.setBlockBreakingInfo(this.mob.id, pos, progressInt)
            this.lastProgress = progressInt
        }
        
        if (breakingProgress >= 1.0f) {
            this.mob.world.breakBlock(pos, true)
            this.stop()
        }
    }

    override fun shouldContinue(): Boolean {
        return this.mob.isMining && this.targetPos != null && this.mob.target != null
    }

    override fun stop() {
        this.mob.isMining = false
        if (this.targetPos != null) {
            this.mob.world.setBlockBreakingInfo(this.mob.id, this.targetPos!!, -1)
        }
        this.targetPos = null
        this.cooldown = 20
    }
}
