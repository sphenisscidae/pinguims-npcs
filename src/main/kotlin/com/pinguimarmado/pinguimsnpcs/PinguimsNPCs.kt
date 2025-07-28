package com.pinguimarmado.pinguimsnpcs

import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import com.pinguimarmado.pinguimsnpcs.registry.ModEntities
import com.pinguimarmado.pinguimsnpcs.registry.ModPackets
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.text.Text

class PinguimsNPCs : ModInitializer {
	override fun onInitialize() {
		ModEntities.registerAttributes()
		registerPacketHandlers()
	}

	private fun registerPacketHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(ModPackets.UPDATE_NPC_DATA_ID) { server, player, handler, buf, responseSender ->
			val npcId = buf.readInt()
			val newCustomName = buf.readString()
			val skinUrl = buf.readString()
			val skinOwner = buf.readString()

			server.execute {
				val world = player.serverWorld
				val entity = world.getEntityById(npcId)

				if (entity is CustomEntityNPC) {
					if (newCustomName.isNotBlank()) {
						entity.customName = Text.literal(newCustomName)
					} else {
						entity.customName = null
					}

					// Lógica para a skin
					if (skinOwner.isNotBlank()) {
						val profileCache = server.userCache
						profileCache?.findByName(skinOwner)?.ifPresent { gameProfile ->
							val properties = server.sessionService.fillProfileProperties(gameProfile, true)
							val skinProperty = properties.properties.get("textures").firstOrNull()
							if (skinProperty != null) {
								val decoded = java.util.Base64.getDecoder().decode(skinProperty.value)
								val json = com.google.gson.JsonParser.parseString(String(decoded))
								val skinUrlFromProfile = json.asJsonObject
									.getAsJsonObject("textures")
									.getAsJsonObject("SKIN")
									.get("url").asString

								entity.skinUrl = skinUrlFromProfile
							}
						}
					} else {
						entity.skinUrl = skinUrl
					}
				}
			}
		}
	}
}