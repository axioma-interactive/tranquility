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
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void tranquility$creatorsGardenOutsideBorder(int x, int y, int z,
            MultiNoiseUtil.MultiNoiseSampler noiseSampler, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Biome coordinates are block coordinates / 4.
        // x or z > 561 (block coord > 2244)
        if (Math.abs(x) > 561 || Math.abs(z) > 561) {
            RegistryEntry<Biome> garden = Tranquility.INSTANCE.getCreatorsGarden();
            if (garden != null) {
                cir.setReturnValue(garden);
            }
        }
    }
}
