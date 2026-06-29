package com.habitseed.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitseed.app.data.local.entity.PlantTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantTypeDao {
    @Query("SELECT * FROM plant_types ORDER BY priceDrops ASC, name ASC")
    fun getPlantTypes(): Flow<List<PlantTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlantTypes(items: List<PlantTypeEntity>)
}
