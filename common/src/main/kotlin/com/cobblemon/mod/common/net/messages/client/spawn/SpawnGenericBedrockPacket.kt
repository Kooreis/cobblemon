package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.generic.GenericBedrockEntity
import com.cobblemon.mod.common.net.IntSize
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.writeSizedInt
import net.minecraft.entity.Entity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.Identifier

/**
 * Spawn packet for [GenericBedrockEntity]. Wraps around vanilla spawn packet behaviour.
 *
 * @author Hiroku
 * @since May 22nd, 2023
 */
class SpawnGenericBedrockPacket(
    val category: Identifier,
    val aspects: Set<String>,
    val poseType: PoseType,
    val scale: Float,
    val width: Float,
    val height: Float,
    vanillaSpawnPacket: EntitySpawnS2CPacket
) : SpawnExtraDataEntityPacket<SpawnGenericBedrockPacket, GenericBedrockEntity>(vanillaSpawnPacket) {
    override val id: Identifier = ID

    override fun encodeEntityData(buffer: PacketByteBuf) {
        buffer.writeIdentifier(this.category)
        buffer.writeCollection(aspects) { _, aspect -> buffer.writeString(aspect) }
        buffer.writeSizedInt(size = IntSize.U_BYTE, poseType.ordinal)
        buffer.writeFloat(scale)
        buffer.writeFloat(width)
        buffer.writeFloat(height)
    }

    override fun applyData(entity: GenericBedrockEntity) {
        entity.category = this.category
        entity.aspects = this.aspects
        entity.poseType.set(this.poseType)
        entity.scale = this.scale
        entity.colliderWidth = this.width
        entity.colliderHeight = this.height
    }

    override fun checkType(entity: Entity): Boolean = entity is GenericBedrockEntity

    companion object {
        val ID = cobblemonResource("spawn_generic_bedrock_entity")
        fun decode(buffer: PacketByteBuf): SpawnGenericBedrockPacket {
            val category = buffer.readIdentifier()
            val aspects = buffer.readList { it.readString() }.toSet()
            val poseType = buffer.readEnumConstant(PoseType::class.java)
            val scale = buffer.readFloat()
            val width = buffer.readFloat()
            val height = buffer.readFloat()
            val vanillaPacket = decodeVanillaPacket(buffer)
            return SpawnGenericBedrockPacket(category, aspects, poseType, scale, width, height, vanillaPacket)
        }
    }
}