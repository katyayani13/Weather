package com.example.weather

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object RetrofitInstance {
    val BASE_URL = "https://archive-api.open-meteo.com/"

    val api: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoApi::class.java)
    }
}
interface OpenMeteoApi {
    @GET("v1/era5")
    fun getWeatherData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("daily") dailyData: String
    ): Call<APIresponse>

}

data class APIresponse(
    val latitude: Double,
    val longitude: Double,
    val daily: DailyData
)

data class DailyData(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)
@Entity(tableName = "weather_data")
data class WeatherRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val minTemperature: Double,
    val maxTemperature: Double
)

@Database(entities = [WeatherRecord::class], version = 1, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        private const val DATABASE_NAME = "weather_database.db"

        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, WeatherDatabase::class.java, DATABASE_NAME)
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}
@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: WeatherRecord)

    @androidx.room.Query("SELECT * FROM weather_data")
    suspend fun getAllWeatherRecords(): List<WeatherRecord>
    @androidx.room.Query("SELECT * FROM weather_data WHERE date = :date AND latitude = :latitude AND longitude= :longitude")
    suspend fun getWeatherDataByDateAndLocation(date: String, latitude: Double, longitude: Double): List<WeatherRecord>
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<WeatherRecord?>(null)
    val weatherData: LiveData<WeatherRecord?> = _weatherData
    private val api = RetrofitInstance.api
    val minTemperature: MutableState<String> = mutableStateOf(String())
    val isFuture: MutableState<String> = mutableStateOf(String())
    val maxTemperature: MutableState<String> = mutableStateOf(String())
    val latitudeS: MutableState<String> = mutableStateOf(String())
    val longitudeS: MutableState<String> = mutableStateOf(String())
    val noDataFromAPI: MutableState<String> = mutableStateOf(String())
    val noDataFromDB: MutableState<String> = mutableStateOf(String())

    fun calculateAverageMinMax(data: List<Pair<Double, Double>>): Pair<Double, Double> {
        val minSum = data.sumOf { it.second }
        val maxSum = data.sumOf { it.first }
        val minAverage = minSum / data.size
        val maxAverage = maxSum / data.size
        return Pair(minAverage, maxAverage)
    }
    fun filterTemperaturesByDate(data: DailyData, targetDate: String): List<Pair<Double, Double>> {

        val matchingIndices = data.time.withIndex().filter { (_, value) ->
            val monthDay = value.substring(5) // Extract month and day (ignore year)
            monthDay == targetDate.substring(5) // Compare month and day
        }

        if (matchingIndices.isEmpty()) {
            println("No data available for $targetDate.")
            return emptyList()
        }

        return matchingIndices.map { (index, _) ->
            Pair(data.temperature_2m_max[index], data.temperature_2m_min[index])
        }
    }
    fun getAllRecords() {
        viewModelScope.launch {
            val allRecords = WeatherDatabase.getInstance(getApplication()).weatherDao().getAllWeatherRecords()

            allRecords.forEach { record ->
                Log.d("WeatherViewModel", "Record: Date: ${record.date}, Latitude: ${record.latitude}, Longitude: ${record.longitude}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchHistoricalWeather(date: String, latitude: Double, longitude: Double) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val currentDate = LocalDate.now().minusDays(6) // Implement this function to get the current date
        val startDate = currentDate.minusYears(10) // Format start date for 10 years ago

        if(LocalDate.now().minusDays(5).isBefore(LocalDate.parse(date, formatter))){
            println("Date is future "+  currentDate + " " + startDate)
        }
        viewModelScope.launch {
            try {
                val response = api.getWeatherData(
                    latitude = latitude,
                    longitude = longitude,
                    startDate = startDate.toString(),
                    endDate = currentDate.toString(),
                    "temperature_2m_max,temperature_2m_min"
                ).await()

                println("Future response " + response.daily)
//
            if (response != null) {
                val historicalData = response.daily // Assuming the API response contains daily data
                val relevantData = filterTemperaturesByDate(historicalData, date)
                println("filtered history "  + relevantData)
                val average = calculateAverageMinMax(relevantData)
                println("filtered history average "  + average)
                minTemperature.value = average.first.toString()
                maxTemperature.value = average.second.toString()
                noDataFromAPI.value = "false"
            } else {
                Log.e("Average", "Could not get past 10 years data")
            }
            } catch (e: Exception) {
                noDataFromAPI.value = "true"
                // Handle exceptions, potentially retry or inform the user
                Log.e("WeatherViewModel", "Error fetching historical weather: $e")
            }
        }
    }

    fun fetchWeatherData(date: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val record = WeatherDatabase.getInstance(getApplication()).weatherDao().getWeatherDataByDateAndLocation(date, latitude, longitude)
            if (record.isNotEmpty()){
                minTemperature.value = record[0].minTemperature.toString()
                maxTemperature.value = record[0].maxTemperature.toString()
                latitudeS.value = record[0].latitude.toString()
                longitudeS.value = record[0].longitude.toString()
                noDataFromDB.value = "false"
                println("from DB " + record)
            }
            else{
                noDataFromDB.value = "true"
                println("from DB no record found for this location and date")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchWeather(date: String, latitude: Double, longitude: Double) {
        latitudeS.value = latitude.toString()
        longitudeS.value = longitude.toString()
        println(" LOCATION " + latitude + " "+ longitude + " " + date)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        if(LocalDate.now().minusDays(5).isBefore(LocalDate.parse(date, formatter))){
            isFuture.value = "true"
            fetchHistoricalWeather(date,latitude,longitude)
        }
        else{

            viewModelScope.launch {
                try {
                    val response = api.getWeatherData(
                        latitude = latitude.toDouble(), // Replace with desired latitude
                        longitude = longitude.toDouble(), // Replace with desired longitude
                        startDate = date, // Replace with desired start date (inclusive)
                        endDate = date,  // Replace with desired end date (inclusive)
                        "temperature_2m_max,temperature_2m_min"
                    ).await()

    //                println("Exsisting record" + response)
                    if (response!=null) {
                        val weatherData = response
                        println("Weather data "+weatherData)
                        val existingRecord = WeatherDatabase.getInstance(getApplication()).weatherDao().getWeatherDataByDateAndLocation(date, latitude, longitude)
                        println("Existing" + existingRecord)
                        isFuture.value = "false"
    //                    _weatherData.postValue(weatherData.toWeatherRecord())  // Update LiveData
                        minTemperature.value = response.daily.temperature_2m_min.toString()
                        maxTemperature.value = response.daily.temperature_2m_max.toString()
                        noDataFromAPI.value = "false"
//                        getAllRecords()
                        if (existingRecord.isEmpty()) {
                            // No existing entry, insert new data
                            Log.d("WeatherViewModel", "Weather data inserted for $date ($latitude, $longitude)")
                            WeatherDatabase.getInstance(getApplication()).weatherDao().insert(weatherData.toWeatherRecord(latitude,longitude))
                        } else {
                            Log.d("WeatherViewModel", "Duplicate entry detected for $date ($latitude, $longitude)")
                        }

                    } else {
                        Log.e("WeatherViewModel", "API call failed: ${response}")
                    }

                } catch (e: Exception) {
                    noDataFromAPI.value = "true"
                    // Handle exceptions, potentially retry or inform the user
                    Log.e("WeatherViewModel", "Error fetching weather: $e")
                }
            }
        }
    }
    fun APIresponse.toWeatherRecord(latitude: Double, longitude: Double): WeatherRecord {
        val dailyData = this.daily
        return WeatherRecord(
            date = dailyData.time[0],
            latitude = latitude,
            longitude = longitude,
            minTemperature = dailyData.temperature_2m_min[0],
            maxTemperature = dailyData.temperature_2m_max[0]
        )
    }
}