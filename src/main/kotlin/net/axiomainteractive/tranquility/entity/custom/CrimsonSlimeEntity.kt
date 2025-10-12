package net.axiomainteractive.tranquility.entity.custom

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.RangedAttackMob
import net.minecraft.entity.ai.goal.ProjectileAttackGoal
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.SlimeEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.thrown.SnowballEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.world.World
import java.util.function.Consumer
import kotlin.math.sqrt


open class CrimsonSlimeEntity(entityType: EntityType<out CrimsonSlimeEntity>, world: World) : SlimeEntity(entityType, world), RangedAttackMob {

    override fun initGoals() {
        super.initGoals()
        this.goalSelector.add(1, ProjectileAttackGoal(this, 1.25, 20, 20.0f))
    }

    init {
        this.setSize(1, true)
    }

    override fun shootAt(target: LivingEntity, pullProgress: Float) {
        val dx = target.getX() - this.getX()
        val dy = target.getEyeY() - 1.1
        val dz = target.getZ() - this.getZ()
        val horizontalDist = sqrt(dx * dx + dz * dz) * 0.2

        val world = this.world
        if (world is ServerWorld) {
            val serverWorld = this.world as ServerWorld
            val itemStack = ItemStack(Items.PISTON)
            ProjectileEntity.spawn<SnowballEntity?>(
                SnowballEntity(serverWorld, this, itemStack),
                serverWorld,
                itemStack,
                Consumer { entity: SnowballEntity? -> entity!!.setVelocity(dx, dy + horizontalDist - entity.getY(), dz, 1.6f, 12.0f) }
            )
        }

        this.playSound(
            SoundEvents.ENTITY_SNOW_GOLEM_SHOOT,
            1.0f,
            0.4f / (this.getRandom().nextFloat() * 0.4f + 0.8f)
        )
    }

    companion object {
        fun createAttributes(): DefaultAttributeContainer.Builder {
            return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 12.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 1.0)
        }
    }
}