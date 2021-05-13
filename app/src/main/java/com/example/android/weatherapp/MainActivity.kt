package com.example.android.weatherapp

import android.Manifest.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat.*
import com.example.android.weatherapp.models.WeatherService
import com.example.android.weatherapp.models.WeatherResponse
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToast.Companion.darkToast
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(){

             private var mcustom_diaLog: Dialog? = null
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
            Log.e("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.e("Current Longitude", "$longitude")

            getLocationWeatherDetails(latitude,longitude)
        }
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double){
        if(Constants.isNetworkAvailable(this)){

            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val service: WeatherService = retrofit
                    .create(WeatherService::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(latitude,longitude,Constants.METRIC_UNIT,Constants.APP_ID)

           CustomDialogProgress()

            listCall.enqueue(object: Callback<WeatherResponse>{

                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {

                  if(response!!.isSuccessful){
                      hideProgressDialog()

                      val weatherList: WeatherResponse? = response.body()
                      if (weatherList != null) {
                          setupUI(weatherList )
                      }
                      Log.i("Response" , "$weatherList" )
                  }else{
                      val rc = response.code()
                              when(rc){
                                  400 ->{
                                      Log.e("Error 400" , "Bad Connection")
                                  }
                                  404 ->{
                                      Log.e("Error 404" , " Not Found")
                                  }else ->{
                                      Log.e("Error", " Generic Error")
                                  }
                              }

                  }
                }


                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                      Log.i("Error" ,t!!.message.toString())
                    hideProgressDialog()
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
    }

    private fun CustomDialogProgress() {

        mcustom_diaLog = Dialog(this)

        mcustom_diaLog!!.setContentView(R.layout.customdialog)

        mcustom_diaLog!!.show()

    }

    private fun hideProgressDialog(){
        if(mcustom_diaLog != null){
            mcustom_diaLog!!.dismiss()
        }
    }
             private fun setupUI(weatherList: WeatherResponse){
                for(i in weatherList.weather.indices)
            {
            Log.i("Weather",weatherList.weather.toString())
            tv_main.text = weatherList.weather[i].main
            tv_main_description.text = weatherList.weather[i].description
            tv_temp.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.toString()) //have to add locales
            tv_sunrise_time.text = unixTime(weatherList.sys.sunrise)
            tv_sunset_time.text =  unixTime(weatherList.sys.sunset)
            tv_country.text = weatherList.sys.country
            tv_min.text = weatherList.main.temp_min.toString() + getMinTemp(application.resources.configuration.toString())
            tv_max.text = weatherList.main.temp_max.toString() + getMaxTemp(application.resources.configuration.toString())
            tv_humidity.text = weatherList.main.humidity.toString()
                tv_speed.text = weatherList.wind.speed.toString()
                tv_speed_unit.text = weatherList.wind.deg.toString() + getDegree(application.resources.configuration.toString())

       }
   }
    private fun getUnit(value: String): String{
        var value = "°C"

        if("US" == value  || "LR" == value || "MM" == value){
            value = "°F"
        }
        return value
    }

    private fun getMinTemp(value: String): String{
        var value = "°C"

        if("US" == value  || "LR" == value || "MM" == value){
            value = "°F"
        }
        return value
    }
    private fun getMaxTemp(value: String): String{
        var value = "°C"

        if("US" == value  || "LR" == value || "MM" == value){
            value = "°F"
        }
        return value
    }

    private fun getDegree(value: String): String{
        var value = "°"

        return value
    }

    private fun unixTime(timex: Long): String?{
                                                                                     // we multiplied 1000l such that we get date in milliseconds
        val date = Date(timex *1000L)                                          // we use Locale.UK such that we get time in 24 hr format val date = Date(timex *1000L)
        val sdf = SimpleDateFormat("hh:mm" , Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return  sdf.format(date)

    }
}














