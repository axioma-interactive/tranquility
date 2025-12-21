package net.axiomainteractive.tranquility.mixin;

import net.axiomainteractive.tranquility.util.RedstoneChargerManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneView.class)
public interface RedstoneViewMixin {

    @Inject(method = "isReceivingRedstonePower", at = @At("HEAD"), cancellable = true)
    default void onIsReceivingRedstonePower(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof World world && RedstoneChargerManager.INSTANCE.isCharged(world, pos)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getReceivedRedstonePower", at = @At("HEAD"), cancellable = true)
    default void onGetReceivedRedstonePower(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (this instanceof World world && RedstoneChargerManager.INSTANCE.isCharged(world, pos)) {
            cir.setReturnValue(15);
        }
    }
}
