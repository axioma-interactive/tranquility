package net.axiomainteractive.tranquility.entity.ai

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.entity.EquipmentSlot
import net.minecraft.util.math.Vec3d
import java.util.EnumSet

class RemnantJumpAttackGoal(private val mob: MobEntity, private val speed: Double, private val jumpRange: Double) : Goal() {
    private var target: LivingEntity? = null
    private var cooldown: Int = 0
    private var isJumping: Boolean = false

    init {
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK))
    }

    override fun canStart(): Boolean {
        val target = this.mob.target ?: return false
        this.target = target
        
        if (cooldown > 0) {
            cooldown--
            return false
        }
        
        if (target.y > this.mob.y + 1.2) return false // Too high, let Pillar goal handle it

        val distanceSq = this.mob.squaredDistanceTo(target)
        
        // Drop Attack / Death From Above (If we are significantly higher)
        if (this.mob.y > target.y + 2.0) {
             // Ignoring minimum distance check to allow jumping OFF a pillar onto a player
             return distanceSq <= jumpRange * jumpRange && this.mob.isOnGround
        }

        // Standard Jump Attack
        // Can jump if within range (e.g. 5-10 blocks) and on ground and cooldown ready
        return distanceSq <= jumpRange * jumpRange && distanceSq >= 4.0 && this.mob.isOnGround
    }

    override fun start() {
        super.start()
        this.isJumping = true
        // Switch to Axe
        this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_AXE))
        
        // Leap
        val vec3d: Vec3d = this.mob.velocity
        var vec3d2: Vec3d = Vec3d(this.target!!.x - this.mob.x, 0.0, this.target!!.z - this.mob.z)
        if (vec3d2.lengthSquared() > 1.0E-7) {
            vec3d2 = vec3d2.normalize().multiply(0.4).add(vec3d.multiply(0.2))
        }
        this.mob.velocity = Vec3d(vec3d2.x, 0.4, vec3d2.z)
    }

    override fun tick() {
        super.tick()
        // While in air / attacking
        val target = this.target ?: return
        this.mob.lookAtEntity(target, 30.0f, 30.0f)
    }
    
    override fun shouldContinue(): Boolean {
        return !this.mob.isOnGround && this.isJumping
    }

    override fun stop() {
        this.isJumping = false
        // Switch back to sword when done
        this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_SWORD))
        // Reset cooldown
        this.cooldown = 40 // ~2 seconds cooldown (Aggressive)
    }
}
