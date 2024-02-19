/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.fishing

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokeball.catching.CaptureEffect
import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.MultiplierModifier
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.fishing.PokeRod
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlin.math.roundToInt
import net.minecraft.resource.ResourceType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

/**
 * The data registry for [PokeRod]s.
 * All the pokerod fields are guaranteed to exist
 */
object PokeRods : JsonDataRegistry<PokeRod> {

    override val id = cobblemonResource("pokerods")
    override val type = ResourceType.SERVER_DATA
    override val observable = SimpleObservable<PokeRods>()

    // ToDo once datapack pokerod is implemented add required adapters here
    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()
    override val typeToken: TypeToken<PokeRod> = TypeToken.get(PokeRod::class.java)
    override val resourcePath = "pokerods"

    private val defaults = hashMapOf<Identifier, PokeRod>()
    // ToDo datapack pokerod type here instead
    private val custom = hashMapOf<Identifier, PokeRod>()

    val POKE_ROD
        get() = this.byName("poke_rod")
    val SLATE_ROD
        get() = this.byName("slate_rod")
    val AZURE_ROD
        get() = this.byName("azure_rod")
    val VERDANT_ROD
        get() = this.byName("verdant_rod")
    val ROSEATE_ROD
        get() = this.byName("roseate_rod")
    val CITRINE_ROD
        get() = this.byName("citrine_rod")
    val GREAT_ROD
        get() = this.byName("great_rod")
    val ULTRA_ROD
        get() = this.byName("ultra_rod")
    val MASTER_ROD
        get() = this.byName("master_rod")
    val SAFARI_ROD
        get() = this.byName("safari_rod")
    val FAST_ROD
        get() = this.byName("fast_rod")
    val LEVEL_ROD
        get() = this.byName("level_rod")
    val LURE_ROD
        get() = this.byName("lure_rod")
    val HEAVY_ROD
        get() = this.byName("heavy_rod")
    val LOVE_ROD
        get() = this.byName("love_rod")
    val FRIEND_ROD
        get() = this.byName("friend_rod")
    val MOON_ROD
        get() = this.byName("moon_rod")
    val SPORT_ROD
        get() = this.byName("sport_rod")
    val NET_ROD
        get() = this.byName("net_rod")
    val DIVE_ROD
        get() = this.byName("dive_rod")
    val NEST_ROD
        get() = this.byName("nest_rod")
    val REPEAT_ROD
        get() = this.byName("repeat_rod")
    val TIMER_ROD
        get() = this.byName("timer_rod")
    val LUXURY_ROD
        get() = this.byName("luxury_rod")
    val PREMIER_ROD
        get() = this.byName("premier_rod")
    val DUSK_ROD
        get() = this.byName("dusk_rod")
    val HEAL_ROD
        get() = this.byName("heal_rod")
    val QUICK_ROD
        get() = this.byName("quick_rod")
    val CHERISH_ROD
        get() = this.byName("cherish_rod")
    val PARK_ROD
        get() = this.byName("park_rod")
    val DREAM_ROD
        get() = this.byName("dream_rod")
    val BEAST_ROD
        get() = this.byName("beast_rod")
    /*val ANCIENT_POKE_ROD
        get() = this.byName("ancient_poke_rod")
    val ANCIENT_CITRINE_ROD
        get() = this.byName("ancient_citrine_rod")
    val ANCIENT_VERDANT_ROD
        get() = this.byName("ancient_verdant_rod")
    val ANCIENT_AZURE_ROD
        get() = this.byName("ancient_azure_rod")
    val ANCIENT_ROSEATE_ROD
        get() = this.byName("ancient_roseate_rod")
    val ANCIENT_SLATE_ROD
        get() = this.byName("ancient_slate_rod")
    val ANCIENT_IVORY_ROD
        get() = this.byName("ancient_ivory_rod")
    val ANCIENT_GREAT_ROD
        get() = this.byName("ancient_great_rod")
    val ANCIENT_ULTRA_ROD
        get() = this.byName("ancient_ultra_rod")
    val ANCIENT_HEAVY_ROD
        get() = this.byName("ancient_heavy_rod")
    val ANCIENT_LEADEN_ROD
        get() = this.byName("ancient_leaden_rod")
    val ANCIENT_GIGATON_ROD
        get() = this.byName("ancient_gigaton_rod")
    val ANCIENT_FEATHER_ROD
        get() = this.byName("ancient_feather_rod")
    val ANCIENT_WING_ROD
        get() = this.byName("ancient_wing_rod")
    val ANCIENT_JET_ROD
        get() = this.byName("ancient_jet_rod")
    val ANCIENT_ORIGIN_ROD
        get() = this.byName("ancient_origin_rod")*/

    init {
        createDefault("poke_rod")
        createDefault("slate_rod")
        createDefault("azure_rod")
        createDefault("verdant_rod")
        createDefault("roseate_rod")
        createDefault("citrine_rod")
        createDefault("great_rod")
        createDefault("ultra_rod")
        createDefault("master_rod")
        createDefault("safari_rod")
        createDefault("fast_rod")
        createDefault("level_rod")
        // ToDo we will need fishing context here once fishing is implemented for a multiplier
        createDefault("lure_rod")
        createDefault("heavy_rod")
        createDefault("love_rod")
        createDefault("friend_rod")
        createDefault("moon_rod")
        createDefault("sport_rod")
        createDefault("net_rod")
        createDefault("dive_rod")
        createDefault("nest_rod")
        // ToDo implement effect once pokedex is implemented, we have a custom multiplier of 2.5 instead of the official pokerod
        createDefault("repeat_rod")
        createDefault("timer_rod")
        createDefault("luxury_rod")
        createDefault("premier_rod")
        createDefault("dusk_rod")
        createDefault("heal_rod")
        createDefault("quick_rod")
        createDefault("cherish_rod")
        createDefault("park_rod")
        createDefault("dream_rod")
        createDefault("beast_rod")
        /*createDefault("ancient_poke_rod")
        createDefault("ancient_citrine_rod")
        createDefault("ancient_verdant_rod")
        createDefault("ancient_azure_rod")
        createDefault("ancient_roseate_rod")
        createDefault("ancient_slate_rod")
        createDefault("ancient_ivory_rod")
        createDefault("ancient_great_rod")
        createDefault("ancient_ultra_rod")
        createDefault("ancient_heavy_rod")
        createDefault("ancient_leaden_rod")
        createDefault("ancient_gigaton_rod")
        createDefault("ancient_feather_rod")
        createDefault("ancient_wing_rod")
        createDefault("ancient_jet_rod")
        createDefault("ancient_origin_rod")*/
    }

    override fun reload(data: Map<Identifier, PokeRod>) {
        this.custom.clear()
        // ToDo once datapack pokerod is implemented load them here, we will want datapacks to be able to override our default pokerods too, however they will never be able to disable them
    }

    override fun sync(player: ServerPlayerEntity) {
        // ToDo once datapack pokerod is implemented sync them here
    }

    /**
     * Gets a Pokerod from registry name.
     * @return the pokerod object if found otherwise null.
     */
    fun getPokeRod(name : Identifier): PokeRod? = this.custom[name] ?: this.defaults[name]

    fun all() = this.defaults.filterKeys { !this.custom.containsKey(it) }.values + this.custom.values

    private fun createDefault(
            name: String
    ): PokeRod {
        val identifier = cobblemonResource(name)
        val pokerod = PokeRod(identifier)
        this.defaults[identifier] = pokerod
        return pokerod
    }

    private fun byName(name: String): PokeRod {
        val identifier = cobblemonResource(name)
        return this.custom[identifier] ?: this.defaults[identifier]!!
    }

}