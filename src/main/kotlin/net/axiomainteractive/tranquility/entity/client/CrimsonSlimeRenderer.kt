package net.axiomainteractive.tranquility.entity.client

import net.axiomainteractive.tranquility.Tranquility
import net.axiomainteractive.tranquility.entity.custom.CrimsonSlimeEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier


class CrimsonSlimeRenderer(context: EntityRendererFactory.Context) : MobEntityRenderer<CrimsonSlimeEntity, CrimsonSlimeRenderState, CrimsonSlimeModel>(context, CrimsonSlimeModel(context.getPart(CrimsonSlimeModel.CRIMSON_SLIME)), 0.75f) {
    override fun getTexture(state: CrimsonSlimeRenderState): Identifier {
        return Identifier.of(Tranquility.MOD_ID, "textures/entity/crimson_slime.png")
    }

    override fun render(
        state: CrimsonSlimeRenderState, matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider?, i: Int
    ) {
        if (state.baby) {
            matrixStack.scale(0.5f, 0.5f, 0.5f)
        } else {
            matrixStack.scale(1f, 1f, 1f)
        }

        super.render(state, matrixStack, vertexConsumerProvider, i)
    }

    override fun createRenderState(): CrimsonSlimeRenderState {
        return CrimsonSlimeRenderState
    }

    override fun updateRenderState(
        livingEntity: CrimsonSlimeEntity?,
        livingEntityRenderState: CrimsonSlimeRenderState?,
        f: Float
    ) {
        super.updateRenderState(livingEntity, livingEntityRenderState, f)
        livingEntityRenderState?.idleAnimationState?.copyFrom(livingEntity?.idleAnimationState)
    }

}