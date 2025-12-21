package net.axiomainteractive.tranquility.screen

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot

class PhilosophersStoneScreenHandler(syncId: Int, playerInventory: PlayerInventory, private val context: ScreenHandlerContext) :
    ScreenHandler(ModScreenHandlers.PHILOSOPHERS_STONE_SCREEN_HANDLER, syncId) {

    constructor(syncId: Int, playerInventory: PlayerInventory) : this(syncId, playerInventory, ScreenHandlerContext.EMPTY)

    private val input: SimpleInventory = object : SimpleInventory(2) {
        override fun markDirty() {
            super.markDirty()
            this@PhilosophersStoneScreenHandler.onContentChanged(this)
        }
    }
    private val output: CraftingResultInventory = CraftingResultInventory()

    init {
        // Input Slots (0, 1)
        this.addSlot(Slot(input, 0,34, 35))
        this.addSlot(Slot(input, 1, 126, 35))

        // Output Slot (2)
        this.addSlot(object : Slot(output, 0, 80, 35) {
            override fun canInsert(stack: ItemStack): Boolean {
                return false
            }

            override fun onTakeItem(player: PlayerEntity, stack: ItemStack) {
                input.removeStack(0, 1)
                input.removeStack(1, 1)
                super.onTakeItem(player, stack)
                
                // Re-evaluate recipe after taking
                this@PhilosophersStoneScreenHandler.onContentChanged(input)
            }
        })

        addPlayerInventory(playerInventory)
        addPlayerHotbar(playerInventory)
        
        // Initial recipe check
        onContentChanged(input)
    }

    override fun onContentChanged(inventory: Inventory) {
        context.run { world, _ ->
            if (!world.isClient) {
                val slot0 = input.getStack(0)
                val slot1 = input.getStack(1)

                val valid = (slot0.isOf(Items.REDSTONE) && slot1.isOf(Items.FLINT)) ||
                            (slot0.isOf(Items.FLINT) && slot1.isOf(Items.REDSTONE))

                if (valid) {
                    output.setStack(0, ItemStack(Items.GUNPOWDER, 1))
                } else {
                    output.setStack(0, ItemStack.EMPTY)
                }
            }
        }
    }

    override fun onClosed(player: PlayerEntity) {
        super.onClosed(player)
        context.run { _, _ ->
            this.dropInventory(player, input)
        }
    }

    private fun addPlayerInventory(playerInventory: PlayerInventory) {
        for (i in 0..2) {
            for (l in 0..8) {
                this.addSlot(Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18))
            }
        }
    }

    private fun addPlayerHotbar(playerInventory: PlayerInventory) {
        for (i in 0..8) {
            this.addSlot(Slot(playerInventory, i, 8 + i * 18, 142))
        }
    }

    override fun quickMove(player: PlayerEntity, invSlot: Int): ItemStack {
        var newStack = ItemStack.EMPTY
        val slot = slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val originalStack = slot.stack
            newStack = originalStack.copy()
            if (invSlot < 3) { // Input(2) + Output(1) = 3 slots total from top
                // Moving from Block to Player
                if (!this.insertItem(originalStack, 3, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
                
                slot.onQuickTransfer(originalStack, newStack)
            } else {
                // Moving from Player to Inout
                if (!this.insertItem(originalStack, 0, 2, false)) {
                    return ItemStack.EMPTY
                }
            }

            if (originalStack.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
            
            if (originalStack.count == newStack.count) {
                return ItemStack.EMPTY
            }
            
            slot.onTakeItem(player, originalStack)
        }
        return newStack
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return canUse(context, player, net.axiomainteractive.tranquility.block.ModBlocks.PHILOSOPHERS_STONE)
    }
}
