package com.example.android.weatherapp.models

import java.io.Serializable

data class Main(
        val temp: Double,
        val temp_min: Double,
        val temp_max: Double,
        val pressure: Double,
        val humidity: Int,
        val sea_level: Double,
        val grd_level: Double,
        ):Serializable
