package com.habitseed.app.ui.components

import androidx.annotation.DrawableRes
import com.habitseed.app.R

@DrawableRes
fun plantAssetFor(plantType: String): Int {
    return PlantAssetMapper.imageFor(plantTypeId = plantType, growthStage = 5)
}

object PlantAssetMapper {
    @DrawableRes
    fun imageFor(
        plantTypeId: String?,
        growthStage: Int
    ): Int {
        val stage = growthStage.coerceIn(0, 5)
        return when (plantTypeId?.lowercase()) {
            "sunflower", "succulent", "plant_succulent" -> sunflowerForStage(stage)
            "cactus", "desert_cactus", "plant_desert_cactus" -> cactusForStage(stage)
            "lotus" -> lotusForStage(stage)
            "water_lily", "plant_water_lily" -> waterLilyForStage(stage)
            "bonsai", "small_bonsai", "golden_bonsai", "plant_golden_bonsai", "plant_bonsai" -> bonsaiForStage(stage)
            "lavender" -> lavenderForStage(stage)
            "mushroom_garden" -> mushroomGardenForStage(stage)
            "venus_flytrap" -> venusFlytrapForStage(stage)
            "sakura_tree", "sakura_bonsai", "plant_sakura_bonsai" -> sakuraTreeForStage(stage)
            else -> sunflowerForStage(stage)
        }
    }

    @DrawableRes
    private fun sunflowerForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.sunflower_seed
            1 -> R.drawable.sunflower_sprout
            2 -> R.drawable.sunflower_young_plant
            3 -> R.drawable.sunflower_healthy_plant
            4 -> R.drawable.sunflower_blooming_plant
            else -> R.drawable.sunflower_fully_grown
        }
    }

    @DrawableRes
    private fun cactusForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.cactus_seed
            1 -> R.drawable.cactus_sprout
            2 -> R.drawable.cactus_young_plant
            3 -> R.drawable.cactus_healthy_plant
            4 -> R.drawable.cactus_blooming_plant
            else -> R.drawable.cactus_fully_grown
        }
    }

    @DrawableRes
    private fun lotusForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.lotus_seed
            1 -> R.drawable.lotus_sprout
            2 -> R.drawable.lotus_young_plant
            3 -> R.drawable.lotus_healthy_plant
            4 -> R.drawable.lotus_blooming_plant
            else -> R.drawable.lotus_fully_grown
        }
    }

    @DrawableRes
    private fun waterLilyForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.water_lily_seed
            1 -> R.drawable.water_lily_sprout
            2 -> R.drawable.water_lily_young_plant
            3 -> R.drawable.water_lily_healthy_plant
            4 -> R.drawable.water_lily_blooming_plant
            else -> R.drawable.water_lily_fully_grown
        }
    }

    @DrawableRes
    private fun bonsaiForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.bonsai_seed
            1 -> R.drawable.bonsai_sprout
            2 -> R.drawable.bonsai_young_plant
            3 -> R.drawable.bonsai_healthy_plant
            4 -> R.drawable.bonsai_blooming_plant
            else -> R.drawable.bonsai_fully_grown
        }
    }

    @DrawableRes
    private fun lavenderForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.lavender_seed
            1 -> R.drawable.lavender_sprout
            2 -> R.drawable.lavender_young_plant
            3 -> R.drawable.lavender_healthy_plant
            4 -> R.drawable.lavender_blooming_plant
            else -> R.drawable.lavender_fully_grown
        }
    }

    @DrawableRes
    private fun mushroomGardenForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.mushroom_garden_seed
            1 -> R.drawable.mushroom_garden_sprout
            2 -> R.drawable.mushroom_garden_young_plant
            3 -> R.drawable.mushroom_garden_healthy_plant
            4 -> R.drawable.mushroom_garden_blooming_plant
            else -> R.drawable.mushroom_garden_fully_grown
        }
    }

    @DrawableRes
    private fun venusFlytrapForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.venus_flytrap_seed
            1 -> R.drawable.venus_flytrap_sprout
            2 -> R.drawable.venus_flytrap_young_plant
            3 -> R.drawable.venus_flytrap_healthy_plant
            4 -> R.drawable.venus_flytrap_blooming_plant
            else -> R.drawable.venus_flytrap_fully_grown
        }
    }

    @DrawableRes
    private fun sakuraTreeForStage(stage: Int): Int {
        return when (stage) {
            0 -> R.drawable.sakura_tree_seed
            1 -> R.drawable.sakura_tree_sprout
            2 -> R.drawable.sakura_tree_young_plant
            3 -> R.drawable.sakura_tree_healthy_plant
            4 -> R.drawable.sakura_tree_blooming_plant
            else -> R.drawable.sakura_tree_fully_grown
        }
    }
}
