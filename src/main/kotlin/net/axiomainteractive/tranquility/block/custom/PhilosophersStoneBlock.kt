package net.axiomainteractive.tranquility.block.custom

import net.axiomainteractive.tranquility.block.entity.ModBlockEntities
import net.axiomainteractive.tranquility.block.entity.PhilosophersStoneBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult

import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class PhilosophersStoneBlock(settings: Settings) : Block(settings), BlockEntityProvider {

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
        return PhilosophersStoneBlockEntity(pos, state)
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hit: BlockHitResult
    ): ActionResult {
        if (!world.isClient) {
            val blockEntity = world.getBlockEntity(pos)
            if (blockEntity is NamedScreenHandlerFactory) {
                player.openHandledScreen(blockEntity)
            }
        }
        return ActionResult.SUCCESS
    }


}
