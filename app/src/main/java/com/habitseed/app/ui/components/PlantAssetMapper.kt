package com.habitseed.app.ui.components

import androidx.annotation.DrawableRes
import com.habitseed.app.R

@DrawableRes
fun plantAssetFor(plantType: String): Int {
    return when (plantType.lowercase()) {
        "succulent", "plant_succulent" -> R.drawable.plant_succulent
        "bonsai", "sakura_bonsai", "plant_bonsai" -> R.drawable.plant_golden_bonsai
        "golden_bonsai", "plant_golden_bonsai" -> R.drawable.plant_golden_bonsai
        "desert_cactus", "plant_desert_cactus" -> R.drawable.plant_desert_cactus
        "venus_flytrap", "monstera", "monstera_deliciosa", "plant_monstera" -> R.drawable.plant_monstera
        "starter_fern", "fern", "plant_starter_fern" -> R.drawable.plant_starter_fern
        "water_lily", "plant_water_lily" -> R.drawable.plant_water_lily
        else -> R.drawable.plant_succulent
    }
}
