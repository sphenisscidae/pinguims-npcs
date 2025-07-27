package com.pinguimarmado.pinguimsnpcs.client.renderer

import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.BipedEntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import java.net.URL
import java.util.*

class CustomEntityNPCRenderer(ctx: EntityRendererFactory.Context) :
    BipedEntityRenderer<CustomEntityNPC, BipedEntityModel<CustomEntityNPC>>(
        ctx,
        BipedEntityModel(ctx.getPart(EntityModelLayers.PLAYER)),
        0.5f
    ) {

    private val skinCache = mutableMapOf<String, Identifier?>()
    private val defaultSkin = Identifier("textures/entity/player/wide/steve.png")

    override fun getTexture(entity: CustomEntityNPC): Identifier {
        val skinUrl = entity.skinUrl

        if (skinUrl.isBlank()) {
            return defaultSkin
        }

        if (skinCache.containsKey(skinUrl)) {
            return skinCache[skinUrl] ?: defaultSkin
        }

        skinCache[skinUrl] = null
        loadTextureFromUrl(skinUrl)

        return defaultSkin
    }

    private fun loadTextureFromUrl(url: String) {
        Util.getIoWorkerExecutor().execute {
            try {
                URL(url).openStream().use { inputStream ->
                    val nativeImage = NativeImage.read(inputStream)
                    val texture = NativeImageBackedTexture(nativeImage)
                    val textureIdentifier = Identifier("pinguimsnpcs", "skins/${UUID.randomUUID()}")

                    MinecraftClient.getInstance().execute {
                        MinecraftClient.getInstance().textureManager.registerTexture(textureIdentifier, texture)
                        skinCache[url] = textureIdentifier
                    }
                }
            } catch (e: Exception) {
                println("Failed to download skin from URL: $url. Error: ${e.message}")
            }
        }
    }
}