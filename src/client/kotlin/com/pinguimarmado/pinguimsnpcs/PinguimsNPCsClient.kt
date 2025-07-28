package com.pinguimarmado.pinguimsnpcs

import com.pinguimarmado.pinguimsnpcs.client.renderer.CustomEntityNPCRenderer
import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import com.pinguimarmado.pinguimsnpcs.gui.NpcPanelScreen
import com.pinguimarmado.pinguimsnpcs.registry.ModEntities
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand

class PinguimsNPCsClient : ClientModInitializer {
	override fun onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.NPC, ::CustomEntityNPCRenderer)

		UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
			if (world.isClient && entity is CustomEntityNPC && hand == Hand.MAIN_HAND) {
				MinecraftClient.getInstance().setScreen(NpcPanelScreen(entity))

				return@register ActionResult.SUCCESS
			}

			return@register ActionResult.PASS
		}
	}
}