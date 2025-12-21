package net.axiomainteractive.tranquility.mixin

import net.axiomainteractive.tranquility.block.ModBlocks
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
                    this.discard()
                }
            }
        }
    }
}
