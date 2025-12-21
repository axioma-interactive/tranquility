package net.axiomainteractive.tranquility.mixin;

import net.axiomainteractive.tranquility.block.ModBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow
    public abstract ItemStack getStack();

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void injectTick(CallbackInfo ci) {
        if (this.getWorld().isClient) {
            return;
        }

        if (this.getStack().getItem() == Items.REDSTONE && this.isOnGround()) {
            BlockPos posWithOffset = this.getBlockPos().down();
            if (this.getWorld().getBlockState(posWithOffset).getBlock() == ModBlocks.INSTANCE.getCRIMSON_OBSIDIAN()) {
                LightningEntity lightningBolt = EntityType.LIGHTNING_BOLT.create(this.getWorld(),
                        net.minecraft.entity.SpawnReason.TRIGGERED);
                if (lightningBolt != null) {
                    lightningBolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(this.getBlockPos()));
                    this.getWorld().spawnEntity(lightningBolt);
                    this.discard();
                }
            }
        }
    }
}
