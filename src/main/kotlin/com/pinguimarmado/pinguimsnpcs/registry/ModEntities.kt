package com.pinguimarmado.pinguimsnpcs.registry

import com.pinguimarmado.pinguimsnpcs.entity.CustomEntityNPC
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.entity.attribute.EntityAttributes

object ModEntities {
    val NPC: EntityType<CustomEntityNPC> = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier("pinguimsnpcs", "npc"),
        FabricEntityTypeBuilder.create(SpawnGroup.MISC, ::CustomEntityNPC)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .build()
    )
    fun registerAttributes() {
        FabricDefaultAttributeRegistry.register(
            NPC,
            CustomEntityNPC.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
        )
    }

}