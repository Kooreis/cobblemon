package com.cobblemon.mod.common.item.interactive

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.item.CobblemonItem
import com.cobblemon.mod.common.pokemon.EVs
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.level.Level

class NegativeVitaminItem(val stat: Stat) : CobblemonItem(Properties()), PokemonSelectingItem {

    companion object {
        const val EV_YIELD = 50 // Amount of EVs to subtract
    }

    override val bagItem = null

    // Check if the Pokémon has EVs to subtract in the specified stat
    override fun canUseOnPokemon(pokemon: Pokemon) = pokemon.evs.getOrDefault(stat) > 0

    override fun applyToPokemon(
        player: ServerPlayer,
        stack: ItemStack,
        pokemon: Pokemon
    ): InteractionResultHolder<ItemStack> {
        val currentEVs = pokemon.evs.getOrDefault(stat)

        // Calculate the amount to subtract, ensuring it doesn't go below 0
        val evsToSubtract = if (currentEVs >= EV_YIELD) {
            EV_YIELD
        } else {
            currentEVs // Subtract only the remaining EVs if less than EV_YIELD
        }

        // Subtract the calculated amount
        val evsSubtracted = pokemon.evs.add(stat, -evsToSubtract)

        return if (evsSubtracted < currentEVs) { // If EVs were successfully subtracted
            pokemon.entity?.playSound(CobblemonSounds.MEDICINE_PILLS_USE, 1F, 1F)
            if (!player.isCreative) {
                stack.shrink(1) // Consume the item unless in creative mode
            }
            InteractionResultHolder.success(stack)
        } else {
            InteractionResultHolder.fail(stack) // If no EVs were subtracted
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user is ServerPlayer) {
            return use(user, user.getItemInHand(hand))
        }
        return InteractionResultHolder.success(user.getItemInHand(hand))
    }
}