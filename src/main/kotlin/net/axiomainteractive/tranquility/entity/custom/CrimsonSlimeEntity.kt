package net.axiomainteractive.tranquility.entity.custom

import net.minecraft.entity.AnimationState
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.SlimeEntity
import net.minecraft.world.World


open class CrimsonSlimeEntity(entityType: EntityType<out CrimsonSlimeEntity>, world: World) : SlimeEntity(entityType, world) {
    val idleAnimationState: AnimationState = AnimationState()
    private var idleAnimationTimeout = 0
    companion object {
        fun createAttributes(): DefaultAttributeContainer.Builder {
            return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 12.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.45)
                .add(EntityAttributes.ATTACK_DAMAGE, 1.0)
        }
    }

    private fun setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 40
            this.idleAnimationState.start(this.age)
        } else {
            --this.idleAnimationTimeout
        }
    }

    override fun tick() {
        super.tick()

        if (this.getWorld().isClient()) {
            this.setupAnimationStates()
        }
    }
}