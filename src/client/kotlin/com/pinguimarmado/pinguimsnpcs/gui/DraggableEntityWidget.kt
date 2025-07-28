package com.pinguimarmado.pinguimsnpcs.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import org.joml.Quaternionf
import kotlin.math.PI

class DraggableEntityWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val entity: LivingEntity
) : ClickableWidget(x, y, width, height, Text.empty()) {

    private var rotationY: Float = 15f
    private var isDragging = false

    override fun renderButton(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val centerX = this.x + this.width / 2
        val centerY = this.y + this.height - 15
        val entitySize = 50f
        val rotationQuaternion = Quaternionf()
            .rotateZ(PI.toFloat())
            .rotateY(rotationY)

        InventoryScreen.drawEntity(
            context,
            centerX, centerY, entitySize.toInt(),
            rotationQuaternion,
            null,
            entity
        )
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        this.isDragging = true
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        this.isDragging = false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (this.isDragging) {
            val sensitivity = 2.0f
            this.rotationY += (deltaX / sensitivity * (PI / 180.0)).toFloat()
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
    }
}