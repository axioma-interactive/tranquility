package net.axiomainteractive.tranquility.entity.ai

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.NoPenaltyTargeting
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand
import java.util.EnumSet

class RemnantHealGoal(private val mob: RemnantEntity) : Goal() {

    private var healCooldown: Int = 0
    private var isHealing: Boolean = false

    init {
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK))
    }

    override fun canStart(): Boolean {
        if (this.healCooldown > 0) {
            this.healCooldown--
            return false
        }
        // Passive: Heal if any damage taken (Top Up)
        if (this.mob.target == null) {
            return this.mob.health < this.mob.maxHealth
        }
        // Combat: Only heal if critical (< 50%)
        return this.mob.health < this.mob.maxHealth / 2
    }

    private var startHealth: Float = 0f
    private var wasPassive: Boolean = false
    private var startupTicks: Int = 0

    override fun start() {
        this.isHealing = true
        this.startHealth = this.mob.health
        this.wasPassive = (this.mob.target == null)
        this.startupTicks = 5
        
        // Equip Porkchop
        this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.PORKCHOP))
        
        // Flee logic: Run away from target IF we have one
        val target = this.mob.target
        if (target != null) {
            val fleePos = NoPenaltyTargeting.find(this.mob, 16, 7)
            if (fleePos != null) {
                this.mob.navigation.startMovingTo(fleePos.x, fleePos.y, fleePos.z, 1.2) 
            }
        } else {
            // Passive Heal: Stop moving
            this.mob.navigation.stop()
        }
    }

    override fun tick() {
        super.tick()
        if (this.startupTicks > 0) this.startupTicks--
        
        val target = this.mob.target
        
        // Flee if close AND TARGET EXISTS
        if (target != null && this.mob.squaredDistanceTo(target) < 64.0) { 
             if (this.mob.navigation.isIdle) {
                 val fleeVec = NoPenaltyTargeting.find(this.mob, 16, 7)
                 if (fleeVec != null) {
                     this.mob.navigation.startMovingTo(fleeVec.x, fleeVec.y, fleeVec.z, 1.3)
                 }
             }
        } else {
             // Safe to stand still
             if (this.mob.navigation.isFollowingPath) {
                 this.mob.navigation.stop()
             }
        }
        
        // Eat
        if (!this.mob.isUsingItem) {
             this.mob.setCurrentHand(Hand.MAIN_HAND)
        }
    }

    override fun stop() {
        this.isHealing = false
        
        // Check if we successfully ate (Health increased)
        if (this.mob.health > this.startHealth) {
            // If we were passive, heal to full
            if (this.wasPassive) {
                this.mob.health = this.mob.maxHealth
            }
        }
        
        this.healCooldown = 600 // 30 seconds cooldown
        this.mob.clearActiveItem()
        this.mob.equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_SWORD))
    }
    
    override fun shouldContinue(): Boolean {
        // Startup Grace Period: Allow 5 ticks to start eating
        if (this.startupTicks > 0) return true
        // Continue if using item
        return this.isHealing && this.mob.isUsingItem
    }
}
