package net.axiomainteractive.tranquility.entity.client.feature

import net.axiomainteractive.tranquility.Tranquility // Assumed existence based on previous steps
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.LoadedEntityModels
import net.minecraft.client.render.entity.model.SlimeEntityModel
import net.minecraft.client.render.entity.state.SlimeEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class CrimsonSlimeOverlayFeatureRenderer(
    context: FeatureRendererContext<SlimeEntityRenderState, SlimeEntityModel>,
    loader: LoadedEntityModels
) : FeatureRenderer<SlimeEntityRenderState, SlimeEntityModel>(context) {

    // Define your custom texture identifier once
    private val CUSTOM_TEXTURE: Identifier = Identifier.of(Tranquility.MOD_ID, "textures/entity/crimson_slime.png")

    private val model: SlimeEntityModel = SlimeEntityModel(loader.getModelPart(EntityModelLayers.SLIME_OUTER))

    override fun render(
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int,
        slimeEntityRenderState: SlimeEntityRenderState,
        f: Float,
        g: Float
    ) {
        val bl = slimeEntityRenderState.hasOutline && slimeEntityRenderState.invisible

        if (!slimeEntityRenderState.invisible || bl) {
            val vertexConsumer: VertexConsumer

            if (bl) {
                // 🚨 FIX 1: Use the custom texture for the outline
                vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getOutline(CUSTOM_TEXTURE))
            } else {
                // 🚨 FIX 2: Use the custom texture for the translucent inner core
                // Note: We use getEntityTranslucent as requested, but see suggestion below!
                vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(CUSTOM_TEXTURE))
            }

            this.model.setAngles(slimeEntityRenderState)
            this.model.render(matrixStack, vertexConsumer, i, LivingEntityRenderer.getOverlay(slimeEntityRenderState, 0.0f))
        }
    }
}