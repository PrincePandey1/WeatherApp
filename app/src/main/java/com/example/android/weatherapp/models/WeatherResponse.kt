package com.example.android.weatherapp.models

import com.example.android.weatherapp.Coord
import java.io.Serializable

data class WeatherResponse (val coord: Coord,
                            val weather: List<Weather>,
                            val main: Main,
                            val base: String,
                            val  wind: Wind,
                            val clouds: Clouds,
                            val visibility: Int,
                            val dt: Int,
                            val sys: Sys,
                            val id: Int,
                            val name: String,
                            val cod: Int): Serializable
