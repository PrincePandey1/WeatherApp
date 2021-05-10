package com.example.android.weatherapp.models

import java.io.Serializable

data class Sys(
        val type: Int,
        val id: Int,
        val country: Int,
        val sunrise: Int,
        val sunset: Int, ): Serializable
