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
                ModelPartBuilder.create(),
                ModelTransform.rotation(0.0f, 0.0f, 0.0f)
            )

            // inner_layer (Inner features)
            mainBody.addChild(
                "inner_layer",
                ModelPartBuilder.create(),
                ModelTransform.rotation(0.0f, 0.0f, 0.0f)
            )

            // bb_main (Base component)
            modelPartData.addChild(
                "bb_main",
                ModelPartBuilder.create(),
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
