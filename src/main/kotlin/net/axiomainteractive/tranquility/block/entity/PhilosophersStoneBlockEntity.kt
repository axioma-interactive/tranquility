package net.axiomainteractive.tranquility.block.entity

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.axiomainteractive.tranquility.screen.PhilosophersStoneScreenHandler

class PhilosophersStoneBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.PHILOSOPHERS_STONE_BLOCK_ENTITY, pos, state), NamedScreenHandlerFactory {
    
    override fun getDisplayName(): Text {
        return Text.translatable("block.tranquility.philosophers_stone")
    }

    override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return PhilosophersStoneScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos))
    }
}
