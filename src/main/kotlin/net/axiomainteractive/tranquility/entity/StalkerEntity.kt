package net.axiomainteractive.tranquility.entity

import net.minecraft.block.AbstractTorchBlock
import net.minecraft.block.Blocks
import net.minecraft.block.DoorBlock
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
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
                .add(EntityAttributes.FOLLOW_RANGE, 128.0) // Greatly increased to prevent losing target
        }
    }

    private var aggressionCheckTimer: Int = 0
    var observationTimer: Int = 0

    override fun initDataTracker(builder: DataTracker.Builder) {
        super.initDataTracker(builder)
        // builder.add(IS_FROZEN, false)
        builder.add(IS_AGGRESSIVE, false)
    }

    override fun initGoals() {
        this.goalSelector.add(1, SwimGoal(this))
        
        // Hide Goal (Priority 2) - Attempts to hide if observed.
        this.goalSelector.add(2, HideFromPlayerGoal(this, 1.2))

        // Attack Goal (Priority 3)
        this.goalSelector.add(3, object : MeleeAttackGoal(this, 1.2, false) {
            override fun canStart(): Boolean {
                return super.canStart() && this@StalkerEntity.dataTracker.get(IS_AGGRESSIVE)
            }
        })
        
        // Sabotage Goal (Priority 4) - Breaks torches/doors if NOT observed
        this.goalSelector.add(4, SabotageGoal(this, 1.0))
        
        // Keep Distance Goal (Priority 5) - Maintains distance instead of stalking closer
        this.goalSelector.add(5, KeepDistanceGoal(this, 1.0))
        
        this.goalSelector.add(6, WanderAroundFarGoal(this, 1.0))
        this.goalSelector.add(7, LookAroundGoal(this))

        this.targetSelector.add(1, ActiveTargetGoal(this, PlayerEntity::class.java, false)) // false = ignore visibility (track through walls)
        this.targetSelector.add(2, RevengeGoal(this))
    }

    override fun tick() {
        super.tick()
        
        // Debug: Always glow
        this.isGlowing = true
        
        if (!world.isClient) {
            val target = this.target

            // 1. Proximity Check: If player is too close (< 5 blocks)
            if (target != null && this.squaredDistanceTo(target) < 25.0) {
                this.dataTracker.set(IS_AGGRESSIVE, true)
            }

            // 2. Retaliation: If attacked, become aggressive
            if (this.attacker != null) {
                this.dataTracker.set(IS_AGGRESSIVE, true)
            }

            // 3. Random Aggression Check (15% chance every 2 minutes)
            if (!this.dataTracker.get(IS_AGGRESSIVE)) {
                aggressionCheckTimer++
                // 2 minutes = 20 ticks * 60 * 2 = 2400 ticks
                if (aggressionCheckTimer >= 2400) {
                    aggressionCheckTimer = 0
                    if (this.random.nextFloat() < 0.10f) { // Lowered to 10%
                        this.dataTracker.set(IS_AGGRESSIVE, true)
                    }
                }
            }

            // Reset Aggression if target lost (Optional, but good for cleanup)
            if (this.dataTracker.get(IS_AGGRESSIVE)) {
                if (target == null || !target.isAlive) {
                    this.dataTracker.set(IS_AGGRESSIVE, false)
                    aggressionCheckTimer = 0
                }
            }
            
            // Track consistent observation
            if (checkObserved()) {
                observationTimer++
            } else {
                observationTimer = 0
            }
        }
    }
    
    class KeepDistanceGoal(private val stalker: StalkerEntity, private val speed: Double) : Goal() {
        init {
            this.setControls(java.util.EnumSet.of(Control.MOVE))
        }

        override fun canStart(): Boolean {
            return !stalker.dataTracker.get(IS_AGGRESSIVE) && stalker.target != null
        }

        override fun tick() {
            val target = stalker.target ?: return
            val distSq = stalker.squaredDistanceTo(target)
            
            // Maintain ~15 blocks distance (sq 225)
            // Range: Keep between 15 (225) and 25 (625)
            
            if (distSq < 225) {
                // Too close (<15), move away
                val awayDir = stalker.pos.subtract(target.pos).normalize()
                val targetPos = stalker.pos.add(awayDir.multiply(5.0))
                stalker.navigation.startMovingTo(targetPos.x, targetPos.y, targetPos.z, speed)
            } else if (distSq > 625) {
                // Too far (>25), move closer
                stalker.navigation.startMovingTo(target, speed)
            } else {
                // Good distance (15-25), stop
                stalker.navigation.stop()
            }
        }
    }

    class SabotageGoal(private val stalker: StalkerEntity, private val speed: Double) : Goal() {
        private var targetPos: BlockPos? = null

        init {
            this.setControls(java.util.EnumSet.of(Control.MOVE))
        }

        override fun canStart(): Boolean {
            if (stalker.dataTracker.get(IS_AGGRESSIVE)) return false
            if (stalker.checkObserved()) return false // Don't start if seen
            
            targetPos = findSabotageTarget()
            return targetPos != null
        }
        
        override fun shouldContinue(): Boolean {
             return !stalker.dataTracker.get(IS_AGGRESSIVE) && !stalker.checkObserved() && targetPos != null && !stalker.navigation.isIdle
        }
        
        override fun start() {
            if (targetPos != null) {
                stalker.navigation.startMovingTo(targetPos!!.x.toDouble(), targetPos!!.y.toDouble(), targetPos!!.z.toDouble(), speed)
            }
        }
        
        override fun tick() {
            if (stalker.checkObserved()) {
                stalker.navigation.stop()
                return 
            }
            val tPos = targetPos ?: return
            
            if (stalker.blockPos.isWithinDistance(tPos, 2.0)) {
                // Break block
                stalker.world.breakBlock(tPos, true)
                targetPos = null
            }
        }
        
        private fun findSabotageTarget(): BlockPos? {
            val start = stalker.blockPos
            // Scan nearby for torches/doors
            for (x in -10..10) {
                for (y in -3..3) {
                    for (z in -10..10) {
                        val pos = start.add(x, y, z)
                        val state = stalker.world.getBlockState(pos)
                        if (state.block is AbstractTorchBlock || state.block is DoorBlock) {
                            return pos
                        }
                    }
                }
            }
            return null
        }
    }

    class HideFromPlayerGoal(private val stalker: StalkerEntity, private val speed: Double) : Goal() {
        private var targetPos: Vec3d? = null
        private var isPanicMode: Boolean = false

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
            // Reset panic mode
            isPanicMode = false
            startHiding(30)
        }
        
        private fun startHiding(range: Int) {
            val found = findHidingSpot(range)
            if (found != null) {
                targetPos = found
                val currentSpeed = if (isPanicMode) speed * 1.5 else speed
                stalker.navigation.startMovingTo(found.x, found.y, found.z, currentSpeed)
            } else {
                teleportRandomly()
            }
        }
        
        private fun teleportRandomly() {
            for (i in 0..16) {
                val x = stalker.x + (stalker.random.nextDouble() - 0.5) * 32.0
                val y = stalker.y + (stalker.random.nextInt(16) - 8)
                val z = stalker.z + (stalker.random.nextDouble() - 0.5) * 32.0
                
                if (attemptTeleport(x, y, z)) {
                    return
                }
            }
        }
        
        private fun attemptTeleport(x: Double, y: Double, z: Double): Boolean {
             val pos = BlockPos(x.toInt(), y.toInt(), z.toInt())
             // Check if target is air and head is air
             if (stalker.world.getBlockState(pos).isAir && stalker.world.getBlockState(pos.up()).isAir) {
                 // Check headroom
                 if (stalker.world.getBlockState(pos.up()).isAir) {
                     val prevX = stalker.x
                     val prevY = stalker.y
                     val prevZ = stalker.z
                     
                     stalker.refreshPositionAndAngles(x, y, z, stalker.yaw, stalker.pitch)
                     
                     // Spawn particles at OLD and NEW location
                     // Server side particles
                     // Using serverWorld.spawnParticles if possible, but we are in Entity class so `world` check is needed.
                     if (stalker.world is net.minecraft.server.world.ServerWorld) {
                         val serverWorld = stalker.world as net.minecraft.server.world.ServerWorld
                         serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.POOF, prevX, prevY + 1.0, prevZ, 10, 0.5, 1.0, 0.5, 0.0)
                         serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.POOF, x, y + 1.0, z, 10, 0.5, 1.0, 0.5, 0.0)
                     }
                     return true
                 }
             }
             return false
        }
        
        override fun shouldContinue(): Boolean {
            return !stalker.dataTracker.get(IS_AGGRESSIVE) && !stalker.navigation.isIdle && stalker.checkObserved()
        }
        
        override fun tick() {
            // Check for panic escalation
            if (!isPanicMode && stalker.observationTimer > 60) {
                 isPanicMode = true
                 // Re-calculate with wider range and higher speed
                 startHiding(64)
            }
        }

        private fun findHidingSpot(range: Int): Vec3d? {
            val player = stalker.target ?: return null
            val start = player.blockPos // Anchor to PLAYER for normal hide? 
            // If Panic, maybe hide far from player (anchor to self or random?)
            // "Go really far away"
            
            // If range is large (Panic), we want to be FAR from player.
            // Using standard random logic anchored to player might just pick a spot 60 blocks "behind" player which is "far" but...
            // Let's widen the search.
            
            val searchRadius = range
            val attempts = if ( range > 30 ) 60 else 40
            
            for (i in 0..attempts) {
                // Pick a spot within range of PLAYER
                val x = start.x + stalker.random.nextInt(searchRadius * 2) - searchRadius
                val z = start.z + stalker.random.nextInt(searchRadius * 2) - searchRadius
                // Y should be close
                val y = start.y + stalker.random.nextInt(10) - 5
                
                val candidate = net.minecraft.util.math.BlockPos(x, y, z)
                // Check if passable
                if (stalker.world.getBlockState(candidate).isAir && stalker.world.getBlockState(candidate.up()).isAir) {
                    // Check LOC from Player
                    val obscureCheck = stalker.world.raycast(net.minecraft.world.RaycastContext(
                        player.eyePos, 
                        Vec3d.ofCenter(candidate).add(0.0, 1.62, 0.0), 
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
