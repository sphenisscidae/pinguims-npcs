package com.pinguimarmado.pinguimsnpcs.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.World
import net.minecraft.text.Text

class CustomEntityNPC(entityType: EntityType<out PathAwareEntity>, world: World) : PathAwareEntity(entityType, world) {

    var skinUrl: String
        get() = this.dataTracker.get(SKIN_URL)
        set(value) = this.dataTracker.set(SKIN_URL, value)

    override fun initDataTracker() {
        super.initDataTracker()
        this.dataTracker.startTracking(SKIN_URL, "")
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putString("SkinUrl", this.dataTracker.get(SKIN_URL))
        this.customName?.let { nbt.putString("CustomName", Text.Serializer.toJson(it)) }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        this.dataTracker.set(SKIN_URL, nbt.getString("SkinUrl"))
        if (nbt.contains("CustomName")) {
            this.customName = Text.Serializer.fromJson(nbt.getString("CustomName"))
        }
    }

    override fun initGoals() {
        super.initGoals()
    }

    companion object {
        private val SKIN_URL: TrackedData<String> =
            DataTracker.registerData(CustomEntityNPC::class.java, TrackedDataHandlerRegistry.STRING)

        fun createMobAttributes(): DefaultAttributeContainer.Builder {
            return PathAwareEntity.createMobAttributes()
        }
    }
}