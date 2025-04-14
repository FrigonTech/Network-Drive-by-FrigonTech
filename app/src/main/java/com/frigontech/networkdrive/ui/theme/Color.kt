package com.frigontech.networkdrive.ui.theme


import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.frigontech.networkdrive.R

@Composable
fun ColorManager(colors: List<Color>): Color {
    return remember {
        derivedStateOf { if (IsDarkMode.isDarkMode.value) colors[0] else colors[1] }
    }.value
}

@Composable
fun getBanner(): Int {
    val banners = listOf(R.drawable.background_darkmode, R.drawable.background_lightmode)
    return remember {
        derivedStateOf { if (IsDarkMode.isDarkMode.value) banners[0] else banners[1] }
    }.value
}

// Color Definitions
object Colors {
    //dark mode values in 0 index and light mode values in 1 index
    var frigontech0green: List<Color> = listOf(Color(0xFF00ab5b), Color(0xFF01a358))
    var frigontech0warningred: List<Color> = listOf(Color(0xFFcf6262), Color(0xFFc96060))
    var frigontech0terminal: List<Color> = listOf(Color(0xff080808), Color(0xffbfbfbf))
}