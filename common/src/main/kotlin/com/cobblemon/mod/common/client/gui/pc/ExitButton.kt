/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.pc

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.util.math.MatrixStack
class ExitButton(
    pX: Int, pY: Int,
    val pWidth: Int, val pHeight: Int,
    pXTexStart: Int, pYTexStart: Int, pYDiffText: Int,
    onPress: PressAction
): TexturedButtonWidget(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffText, exitButtonResource, onPress) {

    companion object {
        private val exitButtonResource = cobblemonResource("ui/pc/pc_exit.png")
    }

    override fun renderButton(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        hovered = pMouseX >= x && pMouseY >= y && pMouseX < x + width && pMouseY < y + height
        if (isHovered) {
            blitk(
                matrixStack = pMatrixStack,
                x = x + 0.1, y = y - 1F,
                texture = exitButtonResource,
                width = pWidth, height = pHeight
            )
        }
    }

}