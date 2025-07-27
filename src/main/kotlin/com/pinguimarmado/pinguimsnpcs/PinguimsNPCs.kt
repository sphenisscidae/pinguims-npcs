package com.pinguimarmado.pinguimsnpcs

import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import com.pinguimarmado.pinguimsnpcs.registry.ModEntities
import com.pinguimarmado.pinguimsnpcs.registry.ModPackets
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class PinguimsNPCs : ModInitializer {
	override fun onInitialize() {
		ModEntities.registerAttributes()
		registerPacketHandlers()
	}

	private fun registerPacketHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(ModPackets.UPDATE_SKIN_URL_ID) { server, player, handler, buf, responseSender ->
			val npcId = buf.readInt()
			val newSkinUrl = buf.readString()

			server.execute {
				val world = player.serverWorld
				val entity = world.getEntityById(npcId)
				if (entity is CustomEntityNPC) {
					entity.skinUrl = newSkinUrl
				}
			}
		}
	}
}