package com.example.android.weatherapp.models

import java.io.Serializable

data class Weather(
      val id: Int,
      val main: String,
      val description: String,
      val icon: String,
      val base: String, ): Serializable