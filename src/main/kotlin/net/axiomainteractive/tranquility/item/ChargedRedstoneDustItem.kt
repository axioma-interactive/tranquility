package net.axiomainteractive.tranquility.item

import net.axiomainteractive.tranquility.util.RedstoneChargerManager
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ChargedRedstoneDustItem(settings: Item.Settings) : BlockItem(Blocks.REDSTONE_WIRE, settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val player = context.player
        if (player != null && player.itemCooldownManager.isCoolingDown(context.stack)) {
            return ActionResult.FAIL
        }
        return super.useOnBlock(context)
    }

    override fun postPlacement(pos: BlockPos, world: World, player: PlayerEntity?, stack: ItemStack, state: BlockState): Boolean {
        val result = super.postPlacement(pos, world, player, stack, state)
        if (!world.isClient) {
            RedstoneChargerManager.charge(world, pos, 20)
            
            // Notify neighbors and the block itself
            world.updateNeighbors(pos, state.block)
            state.neighborUpdate(world, pos, state.block, null, false)
            
            // Set cooldown
            player?.itemCooldownManager?.set(stack, 20)
        }
        return result
    }
}
