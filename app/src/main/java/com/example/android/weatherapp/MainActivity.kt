package com.example.android.weatherapp

import android.Manifest.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat.*
import com.example.android.weatherapp.models.WeatherInterface
import com.example.android.weatherapp.models.WeatherResponse
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import okhttp3.internal.Internal.instance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToast.Companion.TOAST_SUCCESS
import www.sanju.motiontoast.MotionToast.Companion.darkToast

class MainActivity : AppCompatActivity(){


    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

           if(!isLocationEnabled()){
               darkToast(this,"ERROR","Your location is turned Off. Please turned it ON.",
                       MotionToast.TOAST_ERROR,
                       MotionToast.GRAVITY_BOTTOM,
                       MotionToast.LONG_DURATION,
                       getFont(this,R.font.helvetica_regular))

// Directly takes the user to the setting for the respective request
               val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
               startActivity(intent)

           }else{
               Dexter.withActivity(this).withPermissions(permission.ACCESS_COARSE_LOCATION,
                                                          permission.ACCESS_FINE_LOCATION
                             ).withListener(object : MultiplePermissionsListener{
                   override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                       if(report!!.areAllPermissionsGranted()){
                              requestLocationData()
                       }
                       if (report.isAnyPermissionPermanentlyDenied){
                           darkToast(this@MainActivity,"INFO", "You have denied the Permission.",
                                   MotionToast.TOAST_INFO,
                                   MotionToast.GRAVITY_BOTTOM,
                                   MotionToast.LONG_DURATION,
                                   getFont(this@MainActivity,R.font.helvetica_regular))
                       }
                   }

                   override fun onPermissionRationaleShouldBeShown(permission: MutableList<PermissionRequest>?, token: PermissionToken?) {
                       showDialogForPermission()
                   }

               }).onSameThread().check()

           }

        }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(){

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest , mLocationCallback,
                Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude", "$longitude")
            getLocationWeatherDetails(latitude, longitude)
        }
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double){
        if(Constants.isNetworkAvailable(this)){

            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val instanceService: WeatherInterface = retrofit.create(WeatherInterface::class.java)

            val listCall: Call<WeatherResponse> = instanceService.getWeather(latitude,longitude,Constants.METRIC_UNIT,Constants.API_KEY)

            listCall.enqueue(object: Callback<WeatherResponse>{

                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                  if(response.isSuccessful){
                      val weatherList: WeatherResponse? = response.body()
                      Log.i("Response Result", "$weatherList")
                  }else{
                      val rc = response.code()
                              when(rc){
                                  400 ->{
                                      Log.e("Error 400" , "Bad Connection")
                                  }
                                  404 ->{
                                      Log.e("Error 404" , " Not Found")
                                  }else->{
                                      Log.e("Error", " Error")
                                  }
                              }

                  }
                }


                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                      Log.e("Error" ,"Network Failure"  )
                }

            })

        }else{
            darkToast(this@MainActivity,"Error", "No  INTERNET CONNECTION available",
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                getFont(this@MainActivity,R.font.helvetica_regular))
        }
    }



    fun showDialogForPermission(){
        AlertDialog.Builder(this)
                .setMessage("It looks likes you have turned off the permissions. Please turned it On by the Setting")
                .setPositiveButton("GO TO SETTINGS"){ _,_  ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package",packageName,null)
                        intent.data = uri
                        startActivity(intent)
                    }catch (e: ActivityNotFoundException){
                        e.printStackTrace()
                    }

                }.setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                    darkToast(this@MainActivity,"INFO", "You have denied the Permission.",
                            MotionToast.TOAST_INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            getFont(this@MainActivity,R.font.helvetica_regular))
                }.show()
    }


  private fun isLocationEnabled(): Boolean{

        // This provides access to the system location services.
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
         return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                 || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }}














