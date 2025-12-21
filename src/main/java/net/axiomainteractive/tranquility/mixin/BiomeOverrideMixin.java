package net.axiomainteractive.tranquility.mixin;

import net.axiomainteractive.tranquility.Tranquility;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public class BiomeOverrideMixin {
    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void tranquility$mushroomFieldsOutsideBorder(int x, int y, int z,
            MultiNoiseUtil.MultiNoiseSampler noiseSampler, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Biome coordinates are block coordinates / 4.
        // Border is 5000 wide, centered at 0,0. So +/- 2500 blocks.
        // 2500 / 4 = 625.
        if (Math.abs(x) > 625 || Math.abs(z) > 625) {
            RegistryEntry<Biome> mushroom = Tranquility.INSTANCE.getMushroomFields();
            if (mushroom != null) {
                cir.setReturnValue(mushroom);
            }
        }
    }
}
