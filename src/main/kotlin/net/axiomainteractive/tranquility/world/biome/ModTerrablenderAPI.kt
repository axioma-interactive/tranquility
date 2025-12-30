package net.axiomainteractive.tranquility.world.biome

import net.axiomainteractive.tranquility.world.biome.surface.ModMaterialRules
import net.axiomainteractive.tranquility.Tranquility
import net.minecraft.util.Identifier
import terrablender.api.Regions
import terrablender.api.SurfaceRuleManager
import terrablender.api.TerraBlenderApi

class ModTerrablenderAPI : TerraBlenderApi {
    override fun onTerraBlenderInitialized() {
        // Regions.register(ModOverworldRegion(Identifier.of(Tranquility.MOD_ID, "overworld"), 4))

        // SurfaceRuleManager.addSurfaceRules(
        //     SurfaceRuleManager.RuleCategory.OVERWORLD,
        //     Tranquility.MOD_ID,
        //     ModMaterialRules.makeRules()
        // )
    }
}