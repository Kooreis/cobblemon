/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.animation.WingFlapIdleAnimation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.BiWingedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.wavefunction.sineFunction
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.MOVING_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.client.model.ModelPart
import net.minecraft.util.math.Vec3d

class VenomothModel(root: ModelPart) : PokemonPoseableModel(), HeadedFrame, BiWingedFrame {
    override val rootPart = root.registerChildWithAllChildren("venomoth")
    override val head = getPart("head")

    override val leftWing = getPart("left_wings")
    override val rightWing = getPart("right_wings")

    override val portraitScale = 1.8F
    override val portraitTranslation = Vec3d(-0.3, 0.1, 0.0)

    override val profileScale = 0.8F
    override val profileTranslation = Vec3d(0.0, 0.6, 0.0)

    lateinit var sleep: PokemonPose
    lateinit var standing: PokemonPose
    lateinit var walk: PokemonPose
    lateinit var battleidle: PokemonPose
    lateinit var hover: PokemonPose
    lateinit var flying: PokemonPose

    override fun registerPoses() {
        val blink = quirk("blink") { bedrockStateful("venomoth", "blink").setPreventsIdle(false) }
        val quirk1 = quirk("quirk1") { bedrockStateful("venomoth", "quirk1").setPreventsIdle(false) }
        val quirk2 = quirk("quirk2") { bedrockStateful("venomoth", "quirk2").setPreventsIdle(false) }
        val quirkSleep = quirk("quirksleep") { bedrockStateful("venomoth", "quirk_sleep").setPreventsIdle(false) }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(quirk1, quirkSleep),
            idleAnimations = arrayOf(bedrock("venomoth", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = STATIONARY_POSES + UI_POSES - PoseType.HOVER,
            condition = { !it.isBattling },
            quirks = arrayOf(blink, quirk1, quirk2),
            idleAnimations = arrayOf(
                bedrock("venomoth", "ground_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = MOVING_POSES - PoseType.FLY,
            quirks = arrayOf(blink, quirk1, quirk2),
            idleAnimations = arrayOf(
                bedrock("venomoth", "ground_walk")
            )
        )

        hover = registerPose(
            poseName = "hover",
            poseType = PoseType.HOVER,
            quirks = arrayOf(blink, quirk1, quirk2),
            idleAnimations = arrayOf(
                bedrock("venomoth", "air_idle")
            )
        )

        flying = registerPose(
            poseName = "fly",
            poseType = PoseType.FLY,
            quirks = arrayOf(blink, quirk1, quirk2),
            idleAnimations = arrayOf(
                bedrock("venomoth", "air_fly")
            )
        )

        battleidle = registerPose(
            poseName = "battle_idle",
            poseTypes = PoseType.STATIONARY_POSES,
            transformTicks = 10,
            quirks = arrayOf(blink, quirk1, quirk2),
            condition = { it.isBattling },
            idleAnimations = arrayOf(
                bedrock("venomoth", "battle_idle")
            )
        )
    }

//    override fun getFaintAnimation(
//        pokemonEntity: PokemonEntity,
//        state: PoseableEntityState<PokemonEntity>
//    ) = if (state.isPosedIn(standing, walk)) bedrockStateful("venomoth", "faint") else null
}