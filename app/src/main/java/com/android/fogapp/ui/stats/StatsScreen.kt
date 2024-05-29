package com.android.fogapp.ui.stats

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(ScrollState(0))
    ) {
        ListItem(headlineContent = {
            Text(
                text = "Stats",
                color = MaterialTheme.colorScheme.primary
            )
        })

        Divider()

        ListItem(headlineContent = {
            Text(
                text = "World Stats"
            )
        }
        )

        Row (
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            /*CityPieChart(cityPercentages)*/
            FogPieChart(100.0-viewModel.discoveredPercentagePiechart, viewModel.discoveredPercentagePiechart)
        }

        Text(
            text = "Uncovered Percentage World: ${viewModel.discoveredPercentageRounded} %",
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        Text(
            text = "Total surface uncovered: ${viewModel.discoveredSurfaceAreaRounded} km²",
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        Text(
            text = "Total distance traveled: ${viewModel.totalDistanceTravelledRounded} km",
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )

        Divider(
            modifier = Modifier.padding(top = 16.dp)
        )

        ListItem(headlineContent = {
            Text(
                text = "Countries"
            )
        }
        )

        Row (
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            /*CityPieChart(cityPercentages)*/
            LocPieChart(viewModel.countryMap)
        }

        LocPercentageList(viewModel.countryMap)

        Divider(
            modifier = Modifier.padding(top = 16.dp)
        )

        ListItem(headlineContent = {
            Text(
                text = "Cities"
            )
        }
        )

        Row (
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            /*CityPieChart(cityPercentages)*/
            LocPieChart(viewModel.cityMap)
        }

        LocPercentageList(viewModel.cityMap)

        Divider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
fun LocPieChart(locPercentages: Map<String, Double>) {
    val totalPercentage = locPercentages.values.sum()
    val darkTheme = isSystemInDarkTheme()
    val defaultColor = MaterialTheme.colorScheme.primary
    val colors = generateSegmentColors(defaultColor, locPercentages, darkTheme)
    val radius = 300f
    val labelSize = 75f
    val sortedCityPercentages = locPercentages.entries.sortedByDescending { it.value }

    Log.d("gaming", colors.toString())

    if (locPercentages.isEmpty()) {
        Canvas(
            modifier = Modifier
                .size((radius).dp)
                .padding(vertical = 0.dp)
        ) {

            // Draw text "NO DATA YET" in the center
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    "No data yet",
                    center.x,
                    center.y,
                    Paint().apply {
                        color = if (darkTheme) Color.White.toArgb() else Color.Black.toArgb()
                        textSize = labelSize
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    } else {
        Canvas(
            modifier = Modifier
                .size((radius).dp)
                .padding(vertical = 0.dp)
        ) {
            var startAngle = 0f
            var index = 0
            sortedCityPercentages.forEach { (city, percentage) ->
                val sweepAngle = (percentage / totalPercentage * 360).toFloat()
                val angle = startAngle + sweepAngle / 2
                val radians = Math.toRadians(angle.toDouble())
                val offsetX = radius * cos(radians).toFloat()
                val offsetY = radius * sin(radians).toFloat()

                // Draw the colored segment
                drawArc(
                    color = colors.getOrElse(index) { Color.Gray }, // Use theme colors
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = androidx.compose.ui.graphics.drawscope.Fill
                )

                // Draw the text in the center of the segment
                val textX = center.x + offsetX
                val textY = center.y + offsetY
                drawIntoCanvas {
                    it.nativeCanvas.drawText(
                        city,
                        textX,
                        textY,
                        Paint().apply {
                            color = if (darkTheme) Color.White.toArgb() else Color.Black.toArgb()
                            textSize = labelSize
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }

                startAngle += sweepAngle
                index++
            }
        }
    }
}

@Composable
fun FogPieChart(fogPercentage: Double, uncoveredPercentage: Double) {
    val totalPercentage = fogPercentage + uncoveredPercentage

    val colors = mutableListOf(Color.Gray, MaterialTheme.colorScheme.primary)
    val radius = 300f
    val labelSize = 75f
    val darkTheme = isSystemInDarkTheme()

    val percentages = mapOf("Fog" to fogPercentage, "Uncovered" to uncoveredPercentage)

    Canvas(
        modifier = Modifier
            .size((radius).dp)
            .padding(vertical = 0.dp)
    ) {
        var startAngle = 0f
        var index = 0
        percentages.forEach { (label, percentage) ->
            val sweepAngle = (percentage / totalPercentage * 360).toFloat()
            val angle = startAngle + sweepAngle / 2
            val radians = Math.toRadians(angle.toDouble())
            val offsetX = radius * cos(radians).toFloat()
            val offsetY = radius * sin(radians).toFloat()

            // Draw the colored segment
            drawArc(
                color = colors.getOrElse(index) { Color.Gray }, // Use theme colors
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            // Draw the text in the center of the segment
            val textX = center.x + offsetX
            val textY = center.y + offsetY
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    label,
                    textX,
                    textY,
                    Paint().apply {
                        color = if(darkTheme) Color.White.toArgb() else Color.Black.toArgb()
                        textSize = labelSize
                        textAlign = Paint.Align.CENTER
                    }
                )
            }

            startAngle += sweepAngle
            index++
        }
    }
}

fun generateColorVariant(baseColor: Color, factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (baseColor.red * 255).toInt(),
        (baseColor.green * 255).toInt(),
        (baseColor.blue * 255).toInt(),
        hsv
    )
    Log.d("gaming", hsv[2].toString())
    hsv[2] *= factor // Adjust brightness
    Log.d("gaming", hsv[2].toString())
    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun generateSegmentColors(primaryColor: Color, cityPercentages: Map<String, Double>, darkMode: Boolean): List<Color> {
    if (cityPercentages.isEmpty()) {
        return emptyList()
    }
    val lightModeFactor = 0.2f
    val darkModeFactor = 0.08f

    val sortedCities = cityPercentages.entries.sortedByDescending { it.value }

    val primarySegment = sortedCities.firstOrNull()

    val colors = mutableListOf<Color>()

    sortedCities.forEachIndexed { index, (city, _) ->
        when {
            primarySegment != null && city == primarySegment.key -> colors.add(primaryColor)
            index == 1 -> generateColorVariant(primaryColor, 0.8f)
            else -> colors.add(generateColorVariant(primaryColor, if(darkMode) 0.8f - (darkModeFactor * index) else 0.8f + (lightModeFactor * index)))
        }
    }

    colors.add(generateColorVariant(primaryColor, if(darkMode) 0.8f - (darkModeFactor * (sortedCities.size)) else 0.8f + (lightModeFactor * (sortedCities.size))))

    return colors
}

@Composable
fun LocPercentageList(locPercentageMap: Map<String, Double>) {
    Column {
        locPercentageMap.forEach { (loc, percentage) ->
            Text(text = "- $loc: ${percentage.format(2)} %", modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

