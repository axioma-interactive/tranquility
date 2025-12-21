package net.axiomainteractive.tranquility.screen

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class PhilosophersStoneScreen(handler: PhilosophersStoneScreenHandler, inventory: PlayerInventory, title: Text) : HandledScreen<PhilosophersStoneScreenHandler>(handler, inventory, title) {
    
    private val TEXTURE = Identifier.of("minecraft", "textures/gui/container/dispenser.png")

    override fun init() {
        super.init()
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2
    }



    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2
        // Fallback to solid color as RenderPipeline API is unstable/unresolved
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, -0x39393a) // Dark grey
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
        super.drawForeground(context, mouseX, mouseY)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context, mouseX, mouseY, delta)
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }
}
