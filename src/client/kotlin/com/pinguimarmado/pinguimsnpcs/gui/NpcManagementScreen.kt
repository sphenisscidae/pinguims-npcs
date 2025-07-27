package com.pinguimarmado.pinguimsnpcs.gui

import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import com.pinguimarmado.pinguimsnpcs.registry.ModPackets

class NpcManagementScreen(private val npc: CustomEntityNPC) : Screen(Text.literal("Gerenciador de NPC")) {

    private lateinit var skinUrlField: TextFieldWidget
    private lateinit var saveButton: ButtonWidget
    private lateinit var cancelButton: ButtonWidget

    override fun init() {
        super.init()
        skinUrlField = TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 100,
            60,
            200,
            20,
            Text.literal("URL da Skin")
        )
        skinUrlField.text = npc.skinUrl
        skinUrlField.setMaxLength(256)
        addDrawableChild(skinUrlField)

        saveButton = ButtonWidget.builder(Text.literal("Salvar")) {
            val buf = PacketByteBufs.create()
            buf.writeInt(npc.id)
            buf.writeString(skinUrlField.text)
            ClientPlayNetworking.send(ModPackets.UPDATE_SKIN_URL_ID, buf)
            this.close()
        }
            .dimensions(this.width / 2 - 100, this.height - 50, 98, 20)
            .build()
        addDrawableChild(saveButton)

        cancelButton = ButtonWidget.builder(Text.literal("Cancelar")) {
            this.close()
        }
            .dimensions(this.width / 2 + 2, this.height - 50, 98, 20)
            .build()
        addDrawableChild(cancelButton)
        this.setInitialFocus(skinUrlField)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF)
        context.drawTextWithShadow(this.textRenderer, Text.literal("URL da Skin:"), this.width / 2 - 100, 45, 0xA0A0A0)
    }

    override fun shouldPause(): Boolean {
        return false
    }
}