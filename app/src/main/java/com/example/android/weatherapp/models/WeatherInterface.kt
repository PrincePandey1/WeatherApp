package com.example.android.weatherapp.models

import com.example.android.weatherapp.Constants.API_KEY
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherInterface {

    @GET("2.5/weather?apikey=$API_KEY")
       fun getWeather(
           @Query("lat") lat: Double,
           @Query("lon") lon: Double,
           @Query("units") units: String?,
           @Query("apikey") apikey: String?,
       ): Call<WeatherResponse>

}