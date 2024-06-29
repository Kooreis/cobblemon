/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.block

import com.cobblemon.mod.common.block.entity.GildedChestBlockEntity
import com.cobblemon.mod.common.client.render.models.blockbench.repository.BlockEntityModelRepository
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.state.property.Properties
import com.mojang.math.Axis

class GildedChestBlockRenderer(context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<GildedChestBlockEntity> {
    override fun render(
        entity: GildedChestBlockEntity,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val aspects = emptySet<String>()
        val state = entity.posableState
        state.updatePartialTicks(tickDelta)

        val poserId = entity.type.poserId

        val model = BlockEntityModelRepository.getPoser(poserId, aspects)
        val texture = BlockEntityModelRepository.getTexture(poserId, aspects, state.animationSeconds)
        val vertexConsumer = vertexConsumers.getBuffer(RenderType.entityCutout(texture))
        model.bufferProvider = vertexConsumers
        state.currentModel = model
        state.currentAspects = aspects

        val context = RenderContext()
        context.put(RenderContext.RENDER_STATE, RenderContext.RenderState.BLOCK)
        context.put(RenderContext.ASPECTS, aspects)
        context.put(RenderContext.TEXTURE, texture)
        context.put(RenderContext.SPECIES, poserId)
        context.put(RenderContext.POSABLE_STATE, state)
        matrices.pushPose()
        matrices.mulPose(Axis.ZP.rotationDegrees(180f))
        matrices.translate(-0.5, 0.0, 0.5)
        matrices.mulPose(Axis.YP.rotationDegrees(entity.cachedState.get(Properties.HORIZONTAL_FACING).asRotation()))
        matrices.mulPose(Axis.YP.rotationDegrees(180f))

        model.applyAnimations(
            entity = null,
            state = state,
            headYaw = 0F,
            headPitch = 0F,
            limbSwing = 0F,
            limbSwingAmount = 0F,
            ageInTicks = state.animationSeconds * 20
        )
        model.render(context, matrices, vertexConsumer, light, overlay, -0x1)
        model.withLayerContext(vertexConsumers, state, BlockEntityModelRepository.getLayers(poserId, aspects)) {
            model.render(context, matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -0x1)
        }
        model.setDefault()
        matrices.popPose()

    }
}