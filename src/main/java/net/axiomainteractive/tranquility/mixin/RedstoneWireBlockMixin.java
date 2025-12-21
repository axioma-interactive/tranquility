package net.axiomainteractive.tranquility.mixin;

import net.axiomainteractive.tranquility.util.RedstoneChargerManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {

    @Inject(method = "getWeakRedstonePower", at = @At("HEAD"), cancellable = true)
    private void injectGetWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction,
            CallbackInfoReturnable<Integer> cir) {
        if (world instanceof World && RedstoneChargerManager.INSTANCE.isCharged((World) world, pos)) {
            cir.setReturnValue(15);
        }
    }

    @Inject(method = "getStrongRedstonePower", at = @At("HEAD"), cancellable = true)
    private void injectGetStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction,
            CallbackInfoReturnable<Integer> cir) {
        if (world instanceof World && RedstoneChargerManager.INSTANCE.isCharged((World) world, pos)) {
            cir.setReturnValue(15);
        }
    }

    // This makes the wire itself "feel" powered even if it's disconnected or
    // isolated
    @Inject(method = "emitsRedstonePower", at = @At("HEAD"), cancellable = true)
    private void injectEmitsRedstonePower(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        // We can't easily check for charge here without world context,
        // but AbstractBlockStateMixin already handles this at a higher level.
    }
}
