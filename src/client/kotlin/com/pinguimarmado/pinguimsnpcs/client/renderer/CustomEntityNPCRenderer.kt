package com.pinguimarmado.pinguimsnpcs.client.renderer

import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.BipedEntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.PlayerModelPart
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import java.io.FileInputStream
import java.util.*
import java.net.URI

class CustomEntityNPCRenderer(ctx: EntityRendererFactory.Context) :
    BipedEntityRenderer<CustomEntityNPC, PlayerEntityModel<CustomEntityNPC>>(
        ctx,
        PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER), false),
        0.5f
    ) {

    private val classicModel: PlayerEntityModel<CustomEntityNPC> = this.model
    private val slimModel: PlayerEntityModel<CustomEntityNPC> = PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER_SLIM), true)

    private val skinCache = mutableMapOf<String, Pair<Identifier, Boolean>?>()
    private val defaultSkin = Identifier("textures/entity/player/wide/steve.png")

    init {
        arrayOf(classicModel, slimModel).forEach { modelInstance ->
            PlayerModelPart.entries.forEach { part ->
                when (part) {
                    PlayerModelPart.HAT -> modelInstance.hat.visible = true
                    PlayerModelPart.JACKET -> modelInstance.jacket.visible = true
                    PlayerModelPart.LEFT_SLEEVE -> modelInstance.leftSleeve.visible = true
                    PlayerModelPart.RIGHT_SLEEVE -> modelInstance.rightSleeve.visible = true
                    PlayerModelPart.LEFT_PANTS_LEG -> modelInstance.leftPants.visible = true
                    PlayerModelPart.RIGHT_PANTS_LEG -> modelInstance.rightPants.visible = true
                    else -> {}
                }
            }
        }
    }

    override fun setupTransforms(
        entity: CustomEntityNPC,
        matrices: MatrixStack,
        animationProgress: Float,
        bodyYaw: Float,
        tickDelta: Float
    ) {
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta)
        val scale = 0.9375f
        matrices.scale(scale, scale, scale)
    }

    override fun getTexture(entity: CustomEntityNPC): Identifier {
        val skinPath = entity.skinUrl
        if (skinPath.isBlank()) {
            this.model = classicModel
            return defaultSkin
        }

        val cachedData = skinCache[skinPath]
        if (cachedData != null) {
            this.model = if (cachedData.second) slimModel else classicModel
            return cachedData.first
        }

        if (!skinCache.containsKey(skinPath)) {
            skinCache[skinPath] = null
            when {
                skinPath.startsWith("file://") -> loadTextureFromFile(skinPath)
                skinPath.startsWith("http") -> loadTextureFromUrl(skinPath)
            }
        }

        this.model = classicModel
        return defaultSkin
    }

    private fun processSkinImage(skinPath: String, image: NativeImage) {
        val isSlim = image.getColor(54, 20) and -0x1000000 == 0
        val texture = NativeImageBackedTexture(image)
        val textureIdentifier = Identifier("pinguimsnpcs", "skins/${UUID.randomUUID()}")

        MinecraftClient.getInstance().execute {
            MinecraftClient.getInstance().textureManager.registerTexture(textureIdentifier, texture)
            skinCache[skinPath] = Pair(textureIdentifier, isSlim)
        }
    }

    private fun loadTextureFromUrl(url: String) {
        Util.getIoWorkerExecutor().execute {
            try {
                URI(url).toURL().openStream().use { processSkinImage(url, NativeImage.read(it)) }
            } catch (e: Exception) {
                println("Falha ao carregar skin da URL: $url. Erro: ${e.message}")
            }
        }
    }

    private fun loadTextureFromFile(filePathWithPrefix: String) {
        val actualPath = filePathWithPrefix.removePrefix("file://")
        Util.getIoWorkerExecutor().execute {
            try {
                FileInputStream(actualPath).use { processSkinImage(filePathWithPrefix, NativeImage.read(it)) }
            } catch (e: Exception) {
                println("Falha ao carregar skin do arquivo: $actualPath. Erro: ${e.message}")
            }
        }
    }
}