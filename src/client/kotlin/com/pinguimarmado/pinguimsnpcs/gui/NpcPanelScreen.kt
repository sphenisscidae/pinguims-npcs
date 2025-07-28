package com.pinguimarmado.pinguimsnpcs.gui

import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import com.pinguimarmado.pinguimsnpcs.registry.ModPackets
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.io.File

class NpcPanelScreen(private val npc: CustomEntityNPC) : Screen(Text.literal("Painel de Configuração do NPC")) {

    companion object {
        private val BACKGROUND_TEXTURE = Identifier.of("pinguimsnpcs", "textures/gui/npc_panel_background.png")
    }

    private enum class SkinInputMode(val displayName: String) {
        URL("URL"),
        NICKNAME("Nickname"),
        FILE("Arquivo")
    }

    private var currentSkinMode = SkinInputMode.URL

    private lateinit var skinInputField: TextFieldWidget
    private lateinit var skinModeButton: ButtonWidget
    private lateinit var fileBrowseButton: ButtonWidget

    private val panelWidth = 256
    private val panelHeight = 256
    private var panelX = 0
    private var panelY = 0

    private lateinit var nameField: TextFieldWidget

    override fun init() {
        super.init()

        this.panelX = (this.width - this.panelWidth) / 2
        this.panelY = (this.height - this.panelHeight) / 2

        val padding = 8
        val buttonHeight = 20
        val buttonSpacing = 4

        val previewX = panelX + 10
        val previewWidth = 80
        val nameFieldY = panelY + 20
        this.nameField = TextFieldWidget(this.textRenderer, previewX, nameFieldY, previewWidth, 20, Text.literal("Nome"))
        this.nameField.text = npc.customName?.string ?: ""
        addDrawableChild(nameField)

        val previewY = panelY + 45
        val previewHeight = 160
        this.addDrawableChild(DraggableEntityWidget(previewX, previewY, previewWidth, previewHeight, npc))

        val rightColumnX = previewX + previewWidth + padding
        val rightColumnWidth = panelWidth - rightColumnX + panelX - padding
        val buttonWidth = (rightColumnWidth - buttonSpacing) / 2
        var currentY = panelY + 20

        addDrawableChild(ButtonWidget.builder(Text.literal("Diálogo")) {}.dimensions(rightColumnX, currentY, buttonWidth, buttonHeight).build())
        addDrawableChild(ButtonWidget.builder(Text.literal("Ações")) {}.dimensions(rightColumnX + buttonWidth + buttonSpacing, currentY, buttonWidth, buttonHeight).build())

        currentY += buttonHeight + padding

        skinModeButton = ButtonWidget.builder(Text.literal("Modo: ${currentSkinMode.displayName}")) {
            cycleSkinInputMode()
        }.dimensions(rightColumnX, currentY, rightColumnWidth, buttonHeight).build()
        addDrawableChild(skinModeButton)

        currentY += buttonHeight + 2

        skinInputField = TextFieldWidget(this.textRenderer, rightColumnX, currentY, rightColumnWidth, buttonHeight, Text.empty())
        skinInputField.setMaxLength(256)

        if (npc.skinUrl.isNotBlank()) {
            skinInputField.text = npc.skinUrl
            if (npc.skinUrl.startsWith("file://")) {
                currentSkinMode = SkinInputMode.FILE
            } else {
                currentSkinMode = SkinInputMode.URL
            }
            skinModeButton.message = Text.literal("Modo: ${currentSkinMode.displayName}")
        }
        addDrawableChild(skinInputField)

        fileBrowseButton = ButtonWidget.builder(Text.literal("Procurar...")) {
            openFileSelectionDialog()
        }.dimensions(rightColumnX, currentY, rightColumnWidth, buttonHeight).build()
        addDrawableChild(fileBrowseButton)

        val closeButtonWidth = 90
        val closeButtonX = panelX + panelWidth - closeButtonWidth - padding
        val closeButtonY = panelY + panelHeight - buttonHeight - padding
        addDrawableChild(ButtonWidget.builder(Text.literal("Salvar e Fechar")) {
            saveAndClose()
        }.dimensions(closeButtonX, closeButtonY, closeButtonWidth, buttonHeight).build())

        updateSkinWidgets()
    }

    private fun cycleSkinInputMode() {
        currentSkinMode = when (currentSkinMode) {
            SkinInputMode.URL -> SkinInputMode.NICKNAME
            SkinInputMode.NICKNAME -> SkinInputMode.FILE
            SkinInputMode.FILE -> SkinInputMode.URL
        }
        skinModeButton.message = Text.literal("Modo: ${currentSkinMode.displayName}")
        updateSkinWidgets()
    }

    private fun updateSkinWidgets() {
        when (currentSkinMode) {
            SkinInputMode.URL, SkinInputMode.NICKNAME -> {
                skinInputField.visible = true
                fileBrowseButton.visible = false
                skinInputField.text = ""
                skinInputField.setPlaceholder(Text.literal("Cole a ${currentSkinMode.displayName} aqui"))
            }
            SkinInputMode.FILE -> {
                skinInputField.visible = false
                fileBrowseButton.visible = true
            }
        }
    }

    private fun saveAndClose() {
        val buf = PacketByteBufs.create()
        buf.writeInt(npc.id)
        buf.writeString(nameField.text)

        var skinUrl = ""
        var skinOwner = ""

        when (currentSkinMode) {
            SkinInputMode.URL -> skinUrl = skinInputField.text
            SkinInputMode.NICKNAME -> skinOwner = skinInputField.text
            SkinInputMode.FILE -> {
                skinUrl = if (skinInputField.text.startsWith("file://")) skinInputField.text else npc.skinUrl
            }
        }

        if (skinUrl.isBlank() && skinOwner.isBlank()) {
            skinUrl = npc.skinUrl
        }

        buf.writeString(skinUrl)
        buf.writeString(skinOwner)

        ClientPlayNetworking.send(ModPackets.UPDATE_NPC_DATA_ID, buf)
        this.close()
    }

    private fun openFileSelectionDialog() {
        Thread {
            try {
                MemoryStack.stackPush().use { stack ->
                    val patterns = stack.mallocPointer(1)
                    patterns.put(stack.UTF8("*.png")).flip()
                    val filePath = TinyFileDialogs.tinyfd_openFileDialog("Selecione a skin do NPC", null, patterns, "Arquivos de Imagem (*.png)", false)

                    filePath?.let { safeFilePath ->
                        val file = File(safeFilePath)
                        if (file.isFile) {
                            client?.execute {
                                skinInputField.text = "file://$safeFilePath"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        this.renderBackground(context)
        context.drawTexture(BACKGROUND_TEXTURE, panelX, panelY, 0f, 0f, panelWidth, panelHeight, panelWidth, panelHeight)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        skinInputField.mouseClicked(mouseX, mouseY, button)
        nameField.mouseClicked(mouseX, mouseY, button)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        if (nameField.isFocused) return nameField.charTyped(chr, modifiers)
        if (skinInputField.isFocused) return skinInputField.charTyped(chr, modifiers)
        return super.charTyped(chr, modifiers)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (nameField.keyPressed(keyCode, scanCode, modifiers)) return true
        if (skinInputField.keyPressed(keyCode, scanCode, modifiers)) return true

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun shouldPause(): Boolean {
        return false
    }
}