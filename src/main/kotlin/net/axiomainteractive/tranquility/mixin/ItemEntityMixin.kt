package net.axiomainteractive.tranquility.mixin

import net.axiomainteractive.tranquility.block.ModBlocks
import net.axiomainteractive.tranquility.item.ModItems
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ItemEntity::class)
abstract class ItemEntityMixin(type: EntityType<*>, world: World) : Entity(type, world) {

    @Shadow
    abstract fun getStack(): ItemStack

    @Inject(method = ["tick"], at = [At("HEAD")])
    private fun injectTick(ci: CallbackInfo) {
        if (this.world.isClient) {
            return
        }

        if (this.getStack().item == Items.REDSTONE && this.isOnGround) {
            val posWithOffset = this.blockPos.down()
            if (this.world.getBlockState(posWithOffset).block == ModBlocks.CRIMSON_OBSIDIAN) {
                val lightningBolt = EntityType.LIGHTNING_BOLT.create(this.world, SpawnReason.TRIGGERED)
                if (lightningBolt != null) {
                    lightningBolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(this.blockPos))
                    this.world.spawnEntity(lightningBolt)
                    
                    val newStack = ItemStack(ModItems.CHARGED_REDSTONE_DUST, this.getStack().count)
                    // Spawn 0.5 blocks higher to avoid immediate ground collision
                    val newEntity = ItemEntity(this.world, this.x, this.y + 0.5, this.z, newStack)
                    newEntity.setToDefaultPickupDelay()
                    newEntity.isInvulnerable = true
                    
                    // Fling the item away to prevent it from landing on the same block immediately
                    val random = this.world.random
                    newEntity.velocity = Vec3d(
                        random.nextDouble() * 0.4 - 0.2, // Random X velocity
                        0.4,                             // Upward toss
                        random.nextDouble() * 0.4 - 0.2  // Random Z velocity
                    )
                    
                    this.world.spawnEntity(newEntity)
                    
                    this.discard()
                }
            }
        } else if (this.getStack().item == ModItems.CHARGED_REDSTONE_DUST && this.isOnGround) {
            val posWithOffset = this.blockPos.down()
            if (this.world.getBlockState(posWithOffset).block == ModBlocks.CRIMSON_OBSIDIAN) {
                // Spawn 5 Lightning bolts
                for (i in 1..5) {
                    val lightningBolt = EntityType.LIGHTNING_BOLT.create(this.world, SpawnReason.TRIGGERED)
                    if (lightningBolt != null) {
                        lightningBolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(this.blockPos))
                        this.world.spawnEntity(lightningBolt)
                    }
                }

                // Destroy block first to prevent explosion deflection
                this.world.removeBlock(posWithOffset, false)

                // Create massive explosion
                // Charged Creeper explosion power is 6.0f
                this.world.createExplosion(this, posWithOffset.x + 0.5, posWithOffset.y + 0.5, posWithOffset.z + 0.5, 6.0f, World.ExplosionSourceType.TNT)

                // Destroy item
                this.discard()
            }
        }
    }
}
