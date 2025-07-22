package com.frigontech.networkdrive

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.frigontech.networkdrive.ui.theme.ColorManager
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0green
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0warningred
import kotlinx.coroutines.delay
import com.frigontech.lftuc_1.lftuc_main_lib.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


//Font Family
val bahnschriftFamily = FontFamily(Font(R.font.bahnschrift, FontWeight.Normal))

// Keep these at file level
private val deviceListState = mutableStateOf<List<LFTUCServers>>(emptyList())
private val isScanningState = mutableStateOf(false)

// Server state object to track server status
object ServerState {
    val updatingMonitoringStatus = mutableStateOf(false)
    val ServerOnline = mutableStateOf(false)
}

suspend fun ep_StartServer(context: Context) {
    startServer(context, displayName)
    ServerState.updatingMonitoringStatus.value = true
    while(true){
        val currentStatus = lftuc_getServerRunning()
        println("server running = $currentStatus")
        ServerState.ServerOnline.value = currentStatus
        if(currentStatus){
            ServerState.updatingMonitoringStatus.value = false
            break
        }
        delay(1000)
    }
}

suspend fun ep_StopServer() {
    stopServer()
    ServerState.updatingMonitoringStatus.value = true
    delay(500)
    while(true){
        val currentStatus = lftuc_getServerRunning()
        println("server running = $currentStatus")
        ServerState.ServerOnline.value = currentStatus
        if(!currentStatus){
            ServerState.updatingMonitoringStatus.value = false
            break
        }
        delay(1000)
    }
}

//get android shared preferences to know if the app has been started first time since install
fun isFirstTimeLaunch(context: Context): Boolean {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val isFirstTime = sharedPreferences.getBoolean("FirstTimeLaunch", true)

    if (isFirstTime) {
        // Update the flag so the dialog won't show next time
        sharedPreferences.edit() { putBoolean("FirstTimeLaunch", false) }
    }

    return isFirstTime
}

//fetch wifi name when the composable if first created
fun getCurrentWifiName(context: Context): String {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        return "Permission Not Granted"
    }

    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    @Suppress("DEPRECATION") val wifiInfo: WifiInfo? = wifiManager.connectionInfo
    return wifiInfo?.ssid?.removePrefix("\"")?.removeSuffix("\"") ?: "Unknown SSID"
}

@Composable
fun ExplorePage(navSystem: NavController) {
    val navigatoryName = "LFTUC Server"
    val context = LocalContext.current
    val isOnline = ServerState.ServerOnline.value
    val buttonHaultScope = rememberCoroutineScope()

    // Change to a mutable state for WiFi name
    var wifiName by remember { mutableStateOf("Fetching...") }

    // Add sidebar state here
    var isSidebarOpen by remember { mutableStateOf(false) }

    // Function to update Wi-Fi name
    fun refreshWifiName() {//request code 4 if for the permission ACCESS_FINE_LOCATION
        if (checkSpecificPermission(context, 4)) {
            wifiName = getCurrentWifiName(context)
        } else {
            requestSpecificPermission(context, 4)
            wifiName = "Permission Required"
        }
    }

    if(showSnackBarMenu.show.value){
        showSnackBar()
    }

    // Call refreshWifiName when the composable is first displayed
    LaunchedEffect(Unit) {
        if(isFirstTimeLaunch(context)){
            showCustomPrompt(
                context = context,
                message = "WARNING: This app accesses sensitive network data. You are **strictly " +
                        "advised** to use the 'Start Service' feature **only** on a trusted private " +
                        "network. If you expose your data to malicious parties—whether intentionally" +
                        " or by mistake—that is **entirely your responsibility**. The developer " +
                        "**will not be held liable** for any data loss, security breaches, or " +
                        "unintended exposure of your information. For more details, please review " +
                        "the disclaimer in the Settings page. Do you want to continue?",
                positiveText = "Yes, continue",
                negativeText = "No, quit",
                onPositive = { dialog ->
                    dialog.dismiss() // Dismiss the dialog
                },
                onNegative = { dialog ->
                    (context as? Activity)?.finishAffinity() // Quit the app! s#ckers
                }
            )
        }

        requestPermissions(context)
        refreshWifiName()
        if(!areAllPermissionsGranted(context)){
            requestPermissions(context)
        }
        if(!checkSpecificPermission(context, 9)){
            requestSpecificPermission(context, 9)
        }
        specifiedPort = retrieveTextData(context, "port").toIntOrNull()?: 8080
        sMBJ_ID = if(!retrieveTextData(context, "SMBJ1").isNullOrBlank()) retrieveTextData(context, "SMBJ1") else displayName
        sMBJ_PASS = if(!retrieveTextData(context, "SMBJ2").isNullOrBlank()) retrieveTextData(context, "SMBJ2") else (localIPv4AD + "45ctuiy1b39f3")
        displayName = if(!retrieveTextData(context, "device-name").isNullOrBlank()) retrieveTextData(context, "device-name") else lftuc_getLocalIpv4Address().toString()

    }

    // Monitor permission changes
    LaunchedEffect(checkSpecificPermission(context, 4)) {
        if (checkSpecificPermission(context, 4)) {
            refreshWifiName()
        }
    }

    @Composable
    fun AnimatedSidebarContent(
        isOpen: Boolean,
        onClose: () -> Unit,
        navSystem: NavController
    ) {
        var firstOpen = remember { mutableStateOf(true) }
        // Animation value from 0 to 1
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 370),
            label = "sidebar scale anim"
        )
        // Smooth opacity animation
        val animatedOpacity by remember {
            derivedStateOf { animatedProgress * 0.9f }
        }

        LaunchedEffect(Unit) {
            delay(370)
            firstOpen.value=false
        }

        // Background overlay
        if (animatedOpacity > 0f || isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClose() }
                    .drawBehind {
                        drawRect(
                            color = Color.Black.copy(alpha = animatedOpacity),
                            size = this.size
                        )
                    }
            )
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Set the transformOrigin to ensure scaling happens from the left edge
                transformOrigin = TransformOrigin(0f, 0.5f) // 0f = left, 0.5f = vertical center
                scaleX = (animatedProgress)
            }
            .alpha(animatedProgress)
        ) {


            // Sidebar content
            Column(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
                    .clickable(enabled = if (firstOpen.value) true else false) { /* Prevent click-through */ }
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                FrigonTechRow(modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .height(60.dp)) {
                    val heading = remember {"Navigation Menu"}
                    Text(
                        text = heading,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bahnschriftFamily,
                        overflow = TextOverflow.Ellipsis
                    )
                    val closeIcon = remember{Icons.Rounded.Close}
                    Icon(
                        imageVector = closeIcon,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable(onClick = { onClose() })
                            .padding(start = 15.dp)
                    )
                }
                HorizontalDivider()
                SidebarMenuItem(icon = Icons.Rounded.Settings, title = "Settings") {
                    navSystem.navigate("settings")
                }
                val uriHandler = LocalUriHandler.current
                SidebarMenuItem(icon = Icons.Rounded.Star, title = "Rate The App / Feedback") {
                    //Rate the app in google form
                    uriHandler.openUri("https://forms.gle/hZem5edZRuvMyPiL8")
                }
                SidebarMenuItem(icon = Icons.Rounded.Terminal, title = "Logs") {
                    navSystem.navigate("network-interface")
                }
                SidebarMenuItem(icon = Icons.Rounded.Devices, title = "Configure Details") {
                    navSystem.navigate("configure-device-details")
                }
                SidebarMenuItem(icon = Icons.Rounded.Search, title = "Search Hosts") {
                    navSystem.navigate("search-host-page")
                }
                SidebarMenuItem(icon = Icons.Rounded.Folder, title="Device Storage") {
                    navSystem.navigate("file-manager")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title bar with fixed height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp) // Fixed height for title bar
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                // Add menu button to open sidebar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 38.dp, bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menu Button
                    Icon(
                        painter = painterResource(id = R.drawable.ellipsis), // Make sure you have a menu icon
                        contentDescription = "Menu",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isSidebarOpen = true },
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = navigatoryName,
                        fontSize = 20.sp,
                        fontFamily = bahnschriftFamily,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            //GetBanner()

            Spacer(Modifier.height(40.dp))

            // bottom area of the screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Add Start Service button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    var enableButton by remember {mutableStateOf(isAgreementAcknowledged(context))}
                    //Start Service button
                    Button(
                        modifier = Modifier.clip(RoundedCornerShape(7.dp)),
                        colors = ButtonDefaults.buttonColors(
                            // Make the difference more noticeable
                            containerColor = if(enableButton && !ServerState.updatingMonitoringStatus.value)MaterialTheme.colorScheme.tertiary else Color.Gray.copy(alpha = 0.5f),
                            contentColor = if(enableButton && !ServerState.updatingMonitoringStatus.value)MaterialTheme.colorScheme.primary else Color.Gray
                        ),
                        onClick = {
                            when {
                                !isOnline -> {
                                    if (enableButton || !ServerState.updatingMonitoringStatus.value) {
                                        if (displayName.isEmpty()) {
                                            snackbarController("Please Configure Host", "Go", 2500, navSystem)
                                        } else {
                                            buttonHaultScope.launch{
                                                ep_StartServer(context)
                                            }
                                        }
                                    }
                                }

                                isOnline && enableButton && !ServerState.updatingMonitoringStatus.value -> {
                                    buttonHaultScope.launch {
                                        ep_StopServer()
                                    }
                                }

                                else -> {
                                    showToast(context, "Please accept the 'Disclaimer/Warning' statements on the Settings page.", toastLength = Toast.LENGTH_LONG)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if(isOnline) {
                                Icons.Filled.StopCircle
                            }else{
                                Icons.Rounded.Build
                            },
                            contentDescription = null,
                            // Don't set tint here as it overrides the button's content color system
                        )
                        Text(
                            text = if(isOnline) "Stop Service" else "Start Service",
                            fontSize = 16.sp,
                            fontFamily = bahnschriftFamily,
                            // Don't set color here as it overrides the button's content color system
                        )
                    }
                }
                // Add Service Status indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if(isOnline) Icons.Rounded.Info else Icons.Rounded.PlayArrow,
                        contentDescription = "Status Indicator",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column {
                        Text(
                            text = if(isOnline) {
                                "Localhost is online!"
                            } else {
                                "Localhost is offline!"
                            },
                            fontSize = 16.sp,
                            fontFamily = bahnschriftFamily,
                            color = if(ServerState.ServerOnline.value) ColorManager(frigontech0green) else ColorManager(frigontech0warningred)
                        )

                        if (isOnline) {
                            Text(
                                text = "Access at: "+lftuc_getLinkLocalIPv6Address()+":"+ (retrieveTextData(context, "port").toIntOrNull()?: 8080),
                                fontSize = 14.sp,
                                fontFamily = bahnschriftFamily,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedSidebarContent(
        isOpen = isSidebarOpen,
        onClose = { isSidebarOpen = false },
        navSystem = navSystem
    )
}