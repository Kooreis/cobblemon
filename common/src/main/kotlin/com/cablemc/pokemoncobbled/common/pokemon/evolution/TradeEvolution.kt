package com.cablemc.pokemoncobbled.common.pokemon.evolution

import com.cablemc.pokemoncobbled.common.api.moves.MoveTemplate
import com.cablemc.pokemoncobbled.common.api.pokemon.PokemonProperties
import com.cablemc.pokemoncobbled.common.api.pokemon.evolution.ContextEvolution
import com.cablemc.pokemoncobbled.common.api.pokemon.evolution.requirement.EvolutionRequirement
import com.cablemc.pokemoncobbled.common.pokemon.Pokemon
import com.cablemc.pokemoncobbled.common.pokemon.Species
import net.minecraft.world.item.ItemStack

/**
 * Represents a [ContextEvolution] with [Pokemon] context.
 * This is triggered by trading.
 * The context is the received [Pokemon] from the trade.
 *
 * @property requiredContext The [PokemonProperties] representation of the expected received [Pokemon] from the trade.
 * @author Licious
 * @since March 20th, 2022
 */
open class TradeEvolution(
    override val id: String,
    override val result: PokemonProperties,
    override val requiredContext: PokemonProperties,
    override val optional: Boolean,
    override val consumeHeldItem: Boolean,
    override val requirements: List<EvolutionRequirement>,
    override val learnableMoves: List<MoveTemplate>
) : ContextEvolution<Pokemon, PokemonProperties> {

    override fun testContext(pokemon: Pokemon, context: Pokemon) = this.requiredContext.matches(context)

    companion object {

        internal const val ADAPTER_VARIANT = "trade"

    }

}