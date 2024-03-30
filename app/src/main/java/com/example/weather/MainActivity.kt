package com.example.weather

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.weather.ui.theme.WeatherTheme
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var database: WeatherDatabase
    private val viewModel: WeatherViewModel by viewModels() // Use viewModels() for correct factory

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = WeatherDatabase.getInstance(this) // Initialize database for storage

        setContent {
            WeatherApp(viewModel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherApp(viewModel: WeatherViewModel) {
    WeatherTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    Header()
                    WeatherScreen(viewModel)
                }
                // Positioning the footer at the bottom-left corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                ) {
                    Footer()
                }
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { /* Handle settings button click */ },
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(36.dp)
                )
            },
            modifier = Modifier.size(25.dp)
        )
        Text(
            "Weather App",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf("") }

    val state = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
    val openDialog = remember { mutableStateOf(false) }
    val showResopnse = remember { mutableStateOf(false) }
    val isFuture = remember { mutableStateOf("false") }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val Date = LocalDate.ofEpochDay(state.selectedDateMillis?.div((1000 * 60 * 60 * 24)) ?: 1).format(formatter)

    val data = viewModel.weatherData
    val maxTemp = viewModel.maxTemperature.value
    val minTemp = viewModel.minTemperature.value
    val lat = viewModel.latitudeS.value
    val long = viewModel.longitudeS.value
    isFuture.value = viewModel.isFuture.value;

    println(Date + " DATE YE HAI"+ " "+ data.value)


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,

    ) {
        TextButton(
            onClick = {
                openDialog.value = true
            }, colors = ButtonDefaults.textButtonColors(
                Color.Magenta
            )
        ) {
            Text("Select Date", color = Color.White)
        }

        if (openDialog.value) {
            showResopnse.value = false;
            DatePickerDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDialog.value = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDialog.value = false
                        }
                    ) {
                        Text("CANCEL")
                    }
                }
            ) {
                DatePicker(
                    state = state
                )
            }
        }
        Text("Weather App", color = Color.White, fontSize = 24.sp)
        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude", color = Color.White, fontSize = 16.sp) }
        )
        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude", color = Color.White, fontSize = 16.sp) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // Handling the date and year input here
            if (true) {
                isFuture.value = "false"
                showResopnse.value = true
                viewModel.fetchWeather(Date, 28.5485254, 77.2749852)
            }
        }) {
            Text("Proceed")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if(showResopnse.value){
            if(isFuture.value == "true"){
                Box(modifier = Modifier.background(Color.White)){
                    Text(text = "Note: This is mean data from previous years", color = Color.Black, fontSize = 16.sp)
                }
            }
            Text(text = "Date is: $Date", color = Color.White, fontSize = 16.sp)
            Text(text = "Minimum Temp: $maxTemp", color = Color.White, fontSize = 16.sp)
            Text(text = "Maximum Temp: $minTemp", color = Color.White, fontSize = 16.sp)
            Text(text = "Latitude: $lat", color = Color.White, fontSize = 16.sp)
            Text(text = "Longitude: $long", color = Color.White, fontSize = 16.sp)
        }
        // Display the day of the week
    }
}

@Composable
fun Footer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { /* Handle home button click */ },
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.home_button),
                    contentDescription = "Home",
                    modifier = Modifier.size(36.dp)
                )
            },
            modifier = Modifier.size(36.dp)
        )
        IconButton(
            onClick = { /* Handle location button click */ },
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.location_button),
                    contentDescription = "Location",
                    modifier = Modifier.size(36.dp)
                )
            },
            modifier = Modifier.size(36.dp)
        )
        IconButton(
            onClick = { /* Handle profile button click */ },
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.profile_button),
                    contentDescription = "Profile",
                    modifier = Modifier.size(36.dp)
                )
            },
            modifier = Modifier.size(36.dp)
        )
    }
}