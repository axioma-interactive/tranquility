package net.axiomainteractive.tranquility.entity.client

import net.axiomainteractive.tranquility.entity.RemnantEntity
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.util.Identifier
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer

class RemnantRenderer(ctx: EntityRendererFactory.Context) : MobEntityRenderer<RemnantEntity, net.minecraft.client.render.entity.state.PlayerEntityRenderState, net.minecraft.client.render.entity.model.PlayerEntityModel>(ctx, net.minecraft.client.render.entity.model.PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f) {

    private val itemModelManager = ctx.itemModelManager

    companion object {
        val TEXTURE = Identifier.of(net.axiomainteractive.tranquility.Tranquility.MOD_ID, "textures/entity/remnant.png")
    }

    init {
        this.addFeature(net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer(this))
        this.addFeature(net.minecraft.client.render.entity.feature.ArmorFeatureRenderer(this, 
            net.minecraft.client.render.entity.model.BipedEntityModel(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)), 
            net.minecraft.client.render.entity.model.BipedEntityModel(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)), 
            ctx.equipmentRenderer
        ))
    }

    override fun createRenderState(): net.minecraft.client.render.entity.state.PlayerEntityRenderState {
        return net.minecraft.client.render.entity.state.PlayerEntityRenderState()
    }

    override fun getTexture(state: net.minecraft.client.render.entity.state.PlayerEntityRenderState): Identifier {
        return TEXTURE
    }

    override fun updateRenderState(entity: RemnantEntity, state: net.minecraft.client.render.entity.state.PlayerEntityRenderState, f: Float) {
        super.updateRenderState(entity, state, f)
        
        // Ensure all layers are visible (Standard Player Skin)
        state.hatVisible = true
        state.jacketVisible = true
        state.leftPantsLegVisible = true
        state.rightPantsLegVisible = true
        state.leftSleeveVisible = true
        state.rightSleeveVisible = true
        
        // Populate Armor Stacks
        state.equippedHeadStack = entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD)
        state.equippedChestStack = entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST)
        state.equippedLegsStack = entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS)
        state.equippedFeetStack = entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
        
        // Populate Hand Items using standard helper
        net.minecraft.client.render.entity.state.ArmedEntityRenderState.updateRenderState(entity, state, this.itemModelManager)

        val mainHand = entity.mainHandStack
        val offHand = entity.offHandStack

        state.rightArmPose = if (mainHand.isEmpty) net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose.EMPTY else net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose.ITEM
        state.leftArmPose = if (offHand.isEmpty) net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose.EMPTY else net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose.ITEM
    }
}
