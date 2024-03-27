package com.example.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
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
                    WeatherScreen()
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
            label = { Text("Date (Format: DD-MM)", color = Color.White, fontSize = 16.sp) }
        )
        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year", color = Color.White, fontSize = 16.sp) }
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
        Text(text = "Day of the week: $dayOfWeek", color = Color.White, fontSize = 16.sp)
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
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WeatherApp()
}