package com.example.gpsservice.dao

import androidx.room.*
import com.example.gpsservice.entity.Location

@Dao
interface LocationDAO {

    @Insert
    fun insert (location: Location)

    @Query ("select * from location_table")
    fun query(): List<Location>
}