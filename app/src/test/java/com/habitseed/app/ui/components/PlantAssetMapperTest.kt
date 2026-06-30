package com.habitseed.app.ui.components

import com.habitseed.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class PlantAssetMapperTest {
    @Test
    fun imageFor_returnsCorrectStageAsset() {
        assertEquals(
            R.drawable.venus_flytrap_blooming_plant,
            PlantAssetMapper.imageFor(plantTypeId = "venus_flytrap", growthStage = 4)
        )
    }

    @Test
    fun imageFor_missingPlant_returnsFallback() {
        assertEquals(
            R.drawable.sunflower_sprout,
            PlantAssetMapper.imageFor(plantTypeId = "unknown_plant", growthStage = 1)
        )
    }

    @Test
    fun imageFor_invalidStage_clampsToValidRange() {
        assertEquals(
            R.drawable.cactus_seed,
            PlantAssetMapper.imageFor(plantTypeId = "cactus", growthStage = -4)
        )
        assertEquals(
            R.drawable.cactus_fully_grown,
            PlantAssetMapper.imageFor(plantTypeId = "cactus", growthStage = 42)
        )
    }
}
