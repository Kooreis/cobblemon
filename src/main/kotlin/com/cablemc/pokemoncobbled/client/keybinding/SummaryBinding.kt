package com.cablemc.pokemoncobbled.client.keybinding

import com.cablemc.pokemoncobbled.client.gui.summary.Summary
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object SummaryBinding : KeyMapping(
    "key.pokemoncobbled.summary",
    InputConstants.Type.KEYSYM,
    InputConstants.KEY_X,
    CATEGORY_GAMEPLAY
) {
    var wasDown = false
    @SubscribeEvent
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (this.isDown && !wasDown) {
            Minecraft.getInstance().setScreen(Summary())
            wasDown = true
        } else if (!this.isDown) {
            wasDown = false
        }
    }
}