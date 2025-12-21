package net.axiomainteractive.tranquility.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World
import net.minecraft.util.math.Vec3d

class StalkerEntity(entityType: EntityType<out HostileEntity>, world: World) : HostileEntity(entityType, world) {

    companion object {
        // Removed IS_FROZEN
        val IS_AGGRESSIVE: TrackedData<Boolean> = DataTracker.registerData(StalkerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

        fun createStalkerAttributes(): DefaultAttributeContainer.Builder {
            return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35) 
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.FOLLOW_RANGE, 64.0) // Increased range
        }
    }

    private var stalkingTimer: Int = 0
    private var observedTimer: Int = 0
    var currentStalkDistance: Float = 25.0f // Default start distance

    override fun initDataTracker(builder: DataTracker.Builder) {
        super.initDataTracker(builder)
        // builder.add(IS_FROZEN, false)
        builder.add(IS_AGGRESSIVE, false)
    }

    override fun initGoals() {
        this.goalSelector.add(1, SwimGoal(this))
        
        // Hide Goal (Priority 1) - Only if observed and NOT aggressive
        this.goalSelector.add(2, HideFromPlayerGoal(this, 1.2))

        // Attack Goal
        this.goalSelector.add(3, object : MeleeAttackGoal(this, 1.2, false) {
            override fun canStart(): Boolean {
                return super.canStart() && this@StalkerEntity.dataTracker.get(IS_AGGRESSIVE)
            }
        })
        
        // Stalk Goal - Runs if NOT Aggressive
        this.goalSelector.add(4, StalkGoal(this, 1.0))
        
        this.goalSelector.add(5, WanderAroundFarGoal(this, 1.0))
        this.goalSelector.add(6, LookAroundGoal(this))

        this.targetSelector.add(1, ActiveTargetGoal(this, PlayerEntity::class.java, true))
        this.targetSelector.add(2, RevengeGoal(this)) // Retaliation
    }

    override fun tick() {
        super.tick()
        
        if (!world.isClient) {
            // Retaliation: If attacked, become aggressive
            if (this.attacker != null) {
                this.dataTracker.set(IS_AGGRESSIVE, true)
            }

            // Reset Aggression if target is dead or lost
            if (this.dataTracker.get(IS_AGGRESSIVE)) {
                val currentTarget = this.target
                if (currentTarget == null || !currentTarget.isAlive) {
                    this.dataTracker.set(IS_AGGRESSIVE, false)
                    this.stalkingTimer = 0
                    this.observedTimer = 0
                    this.currentStalkDistance = 25.0f // Reset distance
                }
            }

            val isObserved = checkObserved()
            
            if (!this.dataTracker.get(IS_AGGRESSIVE)) {
                if (isObserved) {
                    // Being watched
                    stalkingTimer = 0
                    currentStalkDistance = 25.0f // Reset stalk distance if caught? Or keep pressure? 
                    // User says: "If it cannot lose the player's sight for 30 seconds, it should attack"
                    observedTimer++
                    if (observedTimer > 600) { // 30 seconds
                        this.dataTracker.set(IS_AGGRESSIVE, true)
                    }
                } else {
                    // Not observed
                    observedTimer = 0
                    if (this.target != null) {
                        stalkingTimer++
                        
                        // "If won't be seen for 60 seconds it should closer the hover distance to 10 blocks"
                        if (stalkingTimer > 1200) { // 60s
                            currentStalkDistance = 10.0f
                        } else {
                            currentStalkDistance = 25.0f
                        }

                        // "if then the player fails to notice it for 30 seconds it should attack"
                        // Total 90s (60s + 30s)
                        if (stalkingTimer > 1800) { // 90s
                             this.dataTracker.set(IS_AGGRESSIVE, true)
                        }
                    }
                }
            }
        }
    }
    
    class StalkGoal(private val stalker: StalkerEntity, private val speed: Double) : Goal() {
        init {
            this.setControls(java.util.EnumSet.of(Control.MOVE))
        }

        override fun canStart(): Boolean {
            return !stalker.dataTracker.get(IS_AGGRESSIVE) && stalker.target != null
        }

        override fun tick() {
            val target = stalker.target ?: return
            val distSq = stalker.squaredDistanceTo(target)
            val dist = kotlin.math.sqrt(distSq)
            val desiredDist = stalker.currentStalkDistance
            
            // If too far (> distance + 5), move closer
            if (dist > desiredDist + 5) {
                stalker.navigation.startMovingTo(target, speed)
            } 
            // If too close (< distance - 5), move away
            else if (dist < desiredDist - 5) {
                val awayDir = stalker.pos.subtract(target.pos).normalize()
                val targetPos = stalker.pos.add(awayDir.multiply(5.0))
                stalker.navigation.startMovingTo(targetPos.x, targetPos.y, targetPos.z, speed)
            }
            // If within acceptable range, stop/hover
            else {
                stalker.navigation.stop()
            }
        }
    }

    class HideFromPlayerGoal(private val stalker: StalkerEntity, private val speed: Double) : Goal() {
        private var targetPos: Vec3d? = null

        init {
            this.setControls(java.util.EnumSet.of(Control.MOVE))
        }

        override fun canStart(): Boolean {
            // Start if NOT aggressive AND observed
            if (stalker.dataTracker.get(IS_AGGRESSIVE)) return false
            if (stalker.target == null) return false
            return stalker.checkObserved()
        }

        override fun start() {
            // Find a hiding spot
            val found = findHidingSpot()
            if (found != null) {
                targetPos = found
                stalker.navigation.startMovingTo(found.x, found.y, found.z, speed)
            } else {
                // If no hiding spot found, Attack HEAD ON
                stalker.dataTracker.set(IS_AGGRESSIVE, true)
            }
        }
        
        override fun shouldContinue(): Boolean {
            return !stalker.dataTracker.get(IS_AGGRESSIVE) && !stalker.navigation.isIdle && stalker.checkObserved()
        }

        private fun findHidingSpot(): Vec3d? {
            val player = stalker.target ?: return null
            val start = player.blockPos // Anchor to PLAYER
            // Search wider range (30 radius) and more attempts (40)
            for (i in 0..40) {
                // Pick a spot within 30 blocks
                val x = start.x + stalker.random.nextInt(60) - 30
                val z = start.z + stalker.random.nextInt(60) - 30
                // Y should be close
                val y = start.y + stalker.random.nextInt(10) - 5
                
                val candidate = net.minecraft.util.math.BlockPos(x, y, z)
                // Check if passable
                if (stalker.world.getBlockState(candidate).isAir && stalker.world.getBlockState(candidate.up()).isAir) {
                    // Check LOC from Player
                    // We need a spot where player CANNOT see.
                    // Using player.canSee(Vec3d) or raycast.
                    // Simple distance check first?
                    
                    // Raycast from player eyes to candidate centerup (eye level)
                    // If blocked, good.
                    // But we can't easily perform a raycast here without helper tools or world.raycast.
                    // Actually `world.raycast` exists.
                    
                    val obscureCheck = stalker.world.raycast(net.minecraft.world.RaycastContext(
                        player.eyePos, 
                        Vec3d.ofCenter(candidate).add(0.0, 1.62, 0.0), // Eye level of "hiding stalker"
                        net.minecraft.world.RaycastContext.ShapeType.VISUAL, 
                        net.minecraft.world.RaycastContext.FluidHandling.NONE, 
                        stalker
                    ))
                    
                    if (obscureCheck.type == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                        return Vec3d.ofBottomCenter(candidate)
                    }
                }
            }
            return null
        }
    }

    private fun checkObserved(): Boolean {
        // Iterate through all players to see if any are looking at this entity
        for (player in world.players) {
            if (isPlayerLookingAtMe(player)) {
                return true
            }
        }
        return false
    }

    private fun isPlayerLookingAtMe(player: PlayerEntity): Boolean {
        val vecToEntity = this.eyePos.subtract(player.eyePos).normalize()
        val playerLookVec = player.rotationVector
        val dotProduct = vecToEntity.dotProduct(playerLookVec)

        // Dot product > 0 means looking generally in direction
        // For clearer "looking at", we want a tighter cone, e.g., > 0.5 (60 degrees) or higher
        // Standard "can see" checks often use FOV. 
        // Let's use a threshold.
        // Also check Line of Sight
        
        if (dotProduct > 0.4 && player.canSee(this)) {
            return true
        }
        return false
    }
}
