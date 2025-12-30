package net.axiomainteractive.tranquility.entity.ai

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.item.Items
import net.minecraft.util.Hand
import java.util.EnumSet
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RemnantAttackGoal(private val mob: RemnantEntity) : Goal() {
    private var cooldown = 0
    private var ticks = 0
    private var squadRole = 0 // 0=Aggressor, 1=Left, 2=Right
    
    // Config
    private val attackRangeSq = 4.0
    private val flankRange = 8.0 // Distance to maintain while flanking
    private val engageRange = 6.0 // Distance at which we stop flanking and COMMIT

    init {
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK))
    }

    override fun canStart(): Boolean {
        return this.mob.target != null
    }

    override fun stop() {
        this.mob.isSprinting = false
        this.mob.navigation.stop()
    }

    override fun tick() {
        val target = this.mob.target ?: return
        this.ticks++
        
        // --- 1. SQUAD LOGIC (Update every 20 ticks) ---
        if (this.ticks % 20 == 0) {
            val neighbors = this.mob.world.getEntitiesByClass(RemnantEntity::class.java, this.mob.boundingBox.expand(20.0)) {
                it != this.mob && it.target == target
            }
            val squad = (neighbors + this.mob).sortedBy { it.id }
            val myIndex = squad.indexOf(this.mob)
            this.squadRole = myIndex % 3
        }

        val distSq = this.mob.squaredDistanceTo(target)
        val dist = sqrt(distSq)

        // --- 2. MOVEMENT LOGIC ---
        
        // If we are FLANKING (Role 1/2) and we are NOT yet close enough to commit
        if (this.squadRole != 0 && dist > engageRange) {
            this.mob.isSprinting = false
            
            // Flank Movement
            val flankAngle = if (this.squadRole == 1) -0.8 else 0.8 // Radians (~45 deg)
            
            // Calculate Flank Pos
            val vecFromTarget = target.pos.subtract(this.mob.pos).multiply(-1.0).normalize() 
            val x = vecFromTarget.x * cos(flankAngle) - vecFromTarget.z * sin(flankAngle)
            val z = vecFromTarget.x * sin(flankAngle) + vecFromTarget.z * cos(flankAngle)
            
            val flankPos = target.pos.add(x * flankRange, 0.0, z * flankRange)
            
            // Move to flank
            this.mob.navigation.startMovingTo(flankPos.x, flankPos.y, flankPos.z, 1.1)
        
        } else {
            // AGGRESSOR or COMMITTED FLANKER
            // SPRINT AT TARGET only if far
            if (dist > 3.0) {
                this.mob.isSprinting = true
            } else {
                this.mob.isSprinting = false
            }
            this.mob.lookControl.lookAt(target, 30.0f, 30.0f)
            this.mob.navigation.startMovingTo(target, 1.2) // Fast!
        }

        // --- 3. ATTACK LOGIC ---
        if (this.cooldown > 0) {
            this.cooldown--
        }
        
        // If within range
        if (distSq <= attackRangeSq) {
             if (this.cooldown <= 0) {
                 // CRITICAL HIT LOGIC: Jump if on ground before attacking
                 if (this.mob.isOnGround && this.mob.random.nextFloat() < 0.7f) { // 70% chance to crit
                     this.mob.jump()
                 }
                 
                 this.mob.swingHand(Hand.MAIN_HAND)
                 val world = this.mob.world
                 if (world is net.minecraft.server.world.ServerWorld) {
                     this.mob.tryAttack(world, target)
                 }
                 
                 // Reset Cooldown (Standard ~1s + Random)
                 this.cooldown = 20
             }
        }
    }
}
