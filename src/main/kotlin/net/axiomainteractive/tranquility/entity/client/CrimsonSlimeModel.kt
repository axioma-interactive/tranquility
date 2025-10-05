package net.axiomainteractive.tranquility.entity.client

import net.axiomainteractive.tranquility.Tranquility
import net.minecraft.client.model.*
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.util.Identifier

class CrimsonSlimeModel(root: ModelPart) : EntityModel<CrimsonSlimeRenderState>(root) {

    // Convert Java snake_case (main_body, inner_layer, bb_main) to Kotlin camelCase
    private val mainBody: ModelPart
    private val innerLayer: ModelPart
    private val bbMain: ModelPart

    // --- Static Model Definitions (Companion Object) ---
    companion object {
        val CRIMSON_SLIME: EntityModelLayer = EntityModelLayer(Identifier.of(Tranquility.MOD_ID, "crimson_slime"), "main")

        fun getTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            val modelPartData = modelData.root

            // main_body (Outer Shell)
            val mainBody = modelPartData.addChild(
                "main_body",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, 0.0f, -4.0f, 8.0f, 8.0f, 8.0f, Dilation(0.0f)),
                ModelTransform.rotation(0.0f, 0.0f, 0.0f)
            )

            // inner_layer (Inner features)
            mainBody.addChild(
                "inner_layer",
                ModelPartBuilder.create()
                    // Feature 1
                    .uv(24, 16).cuboid(-3.5f, 2.7f, -3.5f, 2.0f, 2.0f, 2.0f, Dilation(0.0f))
                    // Feature 2
                    .uv(24, 20).cuboid(-3.5f, 2.7f, 1.5f, 2.0f, 2.0f, 2.0f, Dilation(0.0f))
                    // Inner Slime Body
                    .uv(0, 16).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 6.0f, 6.0f, Dilation(0.0f))
                    // Small feature
                    .uv(24, 24).cuboid(-3.5f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, Dilation(0.0f)),
                ModelTransform.rotation(0.0f, 0.0f, 0.0f)
            )

            // bb_main (Base component)
            modelPartData.addChild(
                "bb_main",
                ModelPartBuilder.create().uv(0, 0).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 2.0f, 2.0f, Dilation(0.0f)),
                ModelTransform.rotation(0.0f, 0.0f, 0.0f)
            )

            return TexturedModelData.of(modelData, 32, 32)
        }
    }

    // --- Initialization Block ---
    init {
        // Model Part assignment: uses Kotlin property names (camelCase)
        this.mainBody = root.getChild("main_body")
        this.innerLayer = this.mainBody.getChild("inner_layer")
        this.bbMain = root.getChild("bb_main")
    }
}
