package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weather.ui.theme.WeatherTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherApp()
        }
    }
}

@Composable
fun WeatherApp() {
    WeatherTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WeatherScreen()
        }
    }
}

@Composable
fun WeatherScreen() {
    var date by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") }
        )
        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            // Handling the date and year input here
            if (date.isNotEmpty() && year.isNotEmpty()) {
                val inputDateString = "$date-$year"
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val currentDate = Calendar.getInstance().time
                val selectedDate = dateFormat.parse(inputDateString)

                // Check if selected date is not in the future
                if (selectedDate.before(currentDate)) {
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.time = selectedDate

                    // Get the day of the week for the selected date
                    val dayOfWeekValue = selectedCalendar.get(Calendar.DAY_OF_WEEK)
                    dayOfWeek = when (dayOfWeekValue) {
                        Calendar.SUNDAY -> "Sunday"
                        Calendar.MONDAY -> "Monday"
                        Calendar.TUESDAY -> "Tuesday"
                        Calendar.WEDNESDAY -> "Wednesday"
                        Calendar.THURSDAY -> "Thursday"
                        Calendar.FRIDAY -> "Friday"
                        Calendar.SATURDAY -> "Saturday"
                        else -> ""
                    }
                } else {
                    // TODO: Handle future dates
                    dayOfWeek = "Date is in the future"
                }
            }
        }) {
            Text("Proceed")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Display the day of the week
        Text(text = "Day of the week: $dayOfWeek")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WeatherApp()
}