package net.axiomainteractive.tranquility.mixin;

import net.axiomainteractive.tranquility.util.RedstoneChargerManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Inject(method = "getWeakRedstonePower", at = @At("HEAD"), cancellable = true)
    private void injectGetWeakRedstonePower(BlockView world, BlockPos pos, Direction direction,
            CallbackInfoReturnable<Integer> cir) {
        if (world instanceof World && RedstoneChargerManager.INSTANCE.isCharged((World) world, pos)) {
            cir.setReturnValue(15);
        }
    }

    @Inject(method = "getStrongRedstonePower", at = @At("HEAD"), cancellable = true)
    private void injectGetStrongRedstonePower(BlockView world, BlockPos pos, Direction direction,
            CallbackInfoReturnable<Integer> cir) {
        if (world instanceof World && RedstoneChargerManager.INSTANCE.isCharged((World) world, pos)) {
            cir.setReturnValue(15);
        }
    }
}
