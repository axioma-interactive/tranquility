package net.axiomainteractive.tranquility.entity.ai

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.entity.ai.goal.Goal
import java.util.EnumSet

class RemnantSquadGoal(private val mob: RemnantEntity) : Goal() {
    
    private var updateTimer = 0

    init {
        // This goal runs alongside others, doesn't block Movement/Look usually
        // But if we need to force formation, we might want to override Move?
        // For now, it's a background logic goal mostly.
        this.setControls(EnumSet.noneOf(Control::class.java))
    }

    override fun canStart(): Boolean {
        return true // Always run to maintain squad state
    }

    override fun tick() {
        if (--updateTimer > 0) return
        updateTimer = 20 // Run every second

        // 0. Combat Check: Freeze squad roles if fighting
        if (this.mob.target != null) return

        val neighbors = this.mob.world.getEntitiesByClass(RemnantEntity::class.java, this.mob.boundingBox.expand(32.0)) { it != this.mob }
        
        // 1. Leader Election / Determination
        updateLeadership(neighbors)
        
        // 2. State Sync
        syncState()
        
        // 3. Formation (Soft Leash)
        maintainFormation()
    }

    private fun updateLeadership(neighbors:  List<RemnantEntity>) {
        if (this.mob.squadLeader != null) {
            val leader = this.mob.squadLeader!!
            if (!leader.isAlive || leader.isRemoved || leader.world != this.mob.world || leader.squaredDistanceTo(this.mob) > 64 * 64) {
                this.mob.squadLeader = null
                this.mob.isSquadLeader = false 
            } else {
                 return 
            }
        }
        
        val existingLeader = neighbors.firstOrNull { it.isSquadLeader }
        
        if (existingLeader != null) {
            this.mob.squadLeader = existingLeader
            this.mob.isSquadLeader = false
            
            // Assign Role if generic
            if (this.mob.squadRole == RemnantEntity.SquadRole.LEADER) {
                // Demoted
                 this.mob.squadRole = if (this.mob.random.nextFloat() < 0.6f) RemnantEntity.SquadRole.BUILDER else RemnantEntity.SquadRole.GUARD
            } else if (this.mob.squadRole == RemnantEntity.SquadRole.GUARD && this.mob.random.nextFloat() < 0.1f) {
                // Chance to re-roll to builder occasionally (load balance)
                 this.mob.squadRole = if (this.mob.random.nextFloat() < 0.6f) RemnantEntity.SquadRole.BUILDER else RemnantEntity.SquadRole.GUARD
            }
            return
        }
        
        val squad = (neighbors + this.mob).sortedBy { it.id }
        val candidate = squad.first()
        
        if (candidate == this.mob) {
            this.mob.isSquadLeader = true
            this.mob.squadLeader = null 
            this.mob.squadRole = RemnantEntity.SquadRole.LEADER
        } else {
            this.mob.squadLeader = candidate 
            this.mob.isSquadLeader = false
            // Assign Role
            this.mob.squadRole = if (this.mob.random.nextFloat() < 0.6f) RemnantEntity.SquadRole.BUILDER else RemnantEntity.SquadRole.GUARD
        }
    }
    
    private fun syncState() {
        val leader = this.mob.squadLeader
        
        if (leader != null) {
            this.mob.homePos = leader.homePos
            this.mob.invasionPhase = leader.invasionPhase
            this.mob.campTicks = leader.campTicks
        }
    }
    
    private fun maintainFormation() {
        val leader = this.mob.squadLeader ?: return
        
        // Safety: If we are actively building (rare for followers now), do not interrupt
        if (this.mob.isBuilding) return
        
        // Leash Range: 15 blocks (Keep them close to protect the Leader)
        val leashRange = 15.0
        
        if (this.mob.squaredDistanceTo(leader) > leashRange * leashRange) {
            this.mob.navigation.startMovingTo(leader, 1.3)
        }
    }
}
