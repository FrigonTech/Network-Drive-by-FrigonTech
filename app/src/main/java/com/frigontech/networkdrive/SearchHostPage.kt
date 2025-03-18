package com.frigontech.networkdrive

import android.R
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private var scanJob: Job? = null

@Composable
fun SearchHostPage (navSystem: NavController, focusManager: FocusManager){
    val context = LocalContext.current
    var loadPort = retrieveTextData(context, "port").toIntOrNull()?: 8080
    var hostSearchResult: MutableState<String> = remember{ mutableStateOf("No host(s) found on port: ${loadPort}")}
    var searchProgress = remember{ mutableFloatStateOf(0f) }
    var isAutoScanRunning = remember {mutableStateOf(false)}

    Column(modifier=Modifier
        .fillMaxSize()
        .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }) {
        var hostIdentifierList: MutableList<Int> = remember {mutableStateListOf(0,0)}
        var checked = remember{mutableStateOf(false)}
        var scanMessage = remember{mutableStateOf("")}
        scanMessage.value = if(checked.value) "Scan Range" else "Check Avail."
        var hostsToCheck = remember{mutableStateListOf<String>()}
        var foundDeviceHostDetails = remember{mutableStateListOf<deviceData>()}
        fun subnetString(): String{
            val subnet = localIPv4AD.split('.').take(3).joinToString(".") + "."
            return subnet
        }
        fun subnetHostIdentifierString(): Int{
            val identifier = localIPv4AD.split('.')[3].toInt()
            return identifier
        }
        var displayIPBeingSearched = remember{mutableStateOf("")}

        fun checkUrl(url: String): Boolean {
            return try {
                println("Checking URL: $url")  // Debug log
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                println("Response Code for $url: $responseCode")

                responseCode == 200
            } catch (e: Exception) {
                println("Error checking URL $url: ${e.message}")
                false
            }
        }

        fun makeSubnetsFromRangeAndCheckAvailability(autoScan: Boolean=false){
            showToast(context, "Scan Started!")
            if(scanJob?.isActive == true){println("cancelling job..."); scanJob?.cancel(); foundDeviceHostDetails.clear()}
            foundDeviceHostDetails.clear()
            hostSearchResult.value = "Searching!"

            if(checked.value){
                val hostIdentifier_a: Int = hostIdentifierList[0]
                val hostIdentifier_b: Int = hostIdentifierList[1]
                displayIPBeingSearched.value = "${subnetString()}$hostIdentifier_a-$hostIdentifier_b"
                hostsToCheck.clear()
                for (i in hostIdentifier_a..hostIdentifier_b){
                    hostsToCheck.add(subnetString() + i.toString())
                }
            }else if(autoScan){
                hostIdentifierList[0]=1
                hostIdentifierList[1]=254
                val hostIdentifier_a: Int = hostIdentifierList[0]
                val hostIdentifier_b: Int = hostIdentifierList[1]
                displayIPBeingSearched.value = "${subnetString()}$hostIdentifier_a-$hostIdentifier_b"
                hostsToCheck.clear()
                for (i in hostIdentifier_a..hostIdentifier_b){
                    hostsToCheck.add(subnetString() + i.toString())
                }
            }else{
                // Single host case
                hostsToCheck.clear()
                val hostId: Int = hostIdentifierList[0]
                displayIPBeingSearched.value = "${subnetString()}$hostId"
                val singleHostIP = subnetString() + hostId.toString()
                hostsToCheck.add(singleHostIP)
                println("Checking single host: $singleHostIP on port $loadPort")
            }

            scanJob = CoroutineScope(Dispatchers.IO).launch {
                val totalHosts = hostsToCheck.size.toFloat()
                var checkedHosts = 0.0f

                // Internet connectivity test remains the same...

                // Process hosts in batches of 20-30 instead of all at once
                var batchSize = 25

                /*try {
                    val url = URL("http://example.com")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"
                    val responseCode = connection.responseCode
                    println("Internet Test Response Code: $responseCode")
                } catch (e: Exception) {
                    println("Internet Test Error: ${e.message}")
                }*/

                hostsToCheck.chunked(batchSize).forEach {hostBatch->
                    // Run all checks in parallel using async
                    val jobs: List<Deferred<Unit>> = hostBatch.map { host ->
                        async {
                            val hostAddress = "http://$host:$loadPort/data.json"
                            if(!isAutoScanRunning.value){
                                this@async.cancel("Auto Scan Stopped")//cancel the async
                                return@async
                                searchProgress.floatValue=0f
                            }
                            else{
                                if (checkUrl(hostAddress) && isAutoScanRunning.value) {
                                    println("$hostAddress is available")

                                    // Fetch & sync devices
                                    fetchAndSyncDeviceList(host, loadPort, isHosting = false) { success ->
                                        if (success && isAutoScanRunning.value) {
                                            val currentList = getDevicesList(isHosting = false)

                                            // UI update only if no hosts found
                                            if (currentList.isEmpty()) {
                                                android.os.Handler(Looper.getMainLooper()).post {
                                                    Toast.makeText(
                                                        context,
                                                        "No such hosts found on LAN",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                currentList.filter { it.deviceName.contains("Admin-") }
                                                    .forEach { foundDeviceHostDetails.add(it) }
                                            }
                                        }
                                    }
                                    hostSearchResult.value="Host(s) detected"
                                } else {
                                    println("$hostAddress is unreachable")
                                    hostSearchResult.value="No host(s) detected"
                                }
                                // **Update Progress**
                                checkedHosts++
                                searchProgress.floatValue = if(isAutoScanRunning.value) (checkedHosts / (totalHosts)) else 0f
                            }

                        }
                    }
                    // Wait for all network checks to complete
                    jobs.awaitAll()

                }
                searchProgress.floatValue = 0f
                showToast(context, "Scan Complete!")
                if(isAutoScanRunning.value){
                    hostIdentifierList[0]=1
                    hostIdentifierList[1]=2
                }
                isAutoScanRunning.value=false
            }
        }

        TitleBar(title="Search Hosts", navSystem=navSystem)
        Box(
            modifier = Modifier
                .weight(1f) // This makes the Box take all remaining space
                .fillMaxWidth()
                .padding(bottom=5.dp, start=5.dp, end=5.dp,top=0.dp)
        ){
            Column {
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center,
                    modifier=Modifier.padding(0.dp)) {
                    LinearProgressIndicator(
                        progress = { searchProgress.floatValue },
                        modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    Text(
                        text = "Use Host Identifier",
                        fontSize = 15.sp,
                        fontFamily = bahnschriftFamily,
                        color = Color.Gray
                    )
                    Spacer(modifier=Modifier.width(5.dp))
                    Switch(
                        checked = checked.value,
                        onCheckedChange = {
                            checked.value = it
                        },
                        enabled = !isAutoScanRunning.value,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                        )
                    )
                    Spacer(modifier=Modifier.width(5.dp))
                    Text(
                        text = "Use Host Range",
                        fontSize = 15.sp,
                        fontFamily = bahnschriftFamily,
                        color = Color.Gray
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    if(checked.value){
                        FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                modifier=Modifier.width(100.dp),
                                value = if (hostIdentifierList[0] == 0) "" else hostIdentifierList[0].toString(),
                                onValueChange = {newValue->
                                    if(newValue.isEmpty()){
                                        hostIdentifierList[0]=0
                                    }else{
                                        newValue.toIntOrNull()?.let { parsedValue->
                                            hostIdentifierList[0] = parsedValue
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Start") },
                                // This is the key part - control cursor behavior
                                visualTransformation = VisualTransformation.None,
                                // Set selection to end of input after changes
                                singleLine = true
                            )
                            Spacer(modifier=Modifier.width(10.dp))
                            OutlinedTextField(
                                modifier=Modifier.width(100.dp),
                                value = if (hostIdentifierList[1] == 0) "" else hostIdentifierList[1].toString(),
                                onValueChange = {newValue->
                                    if(newValue.isEmpty()){
                                        hostIdentifierList[1]=0
                                    }else{
                                        newValue.toIntOrNull()?.let { parsedValue->
                                            hostIdentifierList[1] = parsedValue
                                        }
                                    }

                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("End") },
                                // This is the key part - control cursor behavior
                                visualTransformation = VisualTransformation.None,
                                // Set selection to end of input after changes
                                singleLine = true
                            )
                        }
                    }else{
                        OutlinedTextField(
                            modifier=Modifier.width(120.dp),
                            value = if (hostIdentifierList[0] == 0) "" else hostIdentifierList[0].toString(),
                            onValueChange = {newValue->
                                if(newValue.isEmpty()){
                                    hostIdentifierList[0]=0
                                }else{
                                    newValue.toIntOrNull()?.let { parsedValue->
                                        hostIdentifierList[0] = parsedValue
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = { Text("Identifier") }
                        )
                    }
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    FrigonTechGenButton(text=scanMessage.value,
                        enabled = !isAutoScanRunning.value,
                        onClick = {
                        if(checked.value && hostIdentifierList[1]<=hostIdentifierList[0]){
                            showToast(context, "Range End cannot be smaller than Range Start")
                        }else{makeSubnetsFromRangeAndCheckAvailability()}})
                    Spacer(modifier=Modifier.width(5.dp))
                    FrigonTechStateButton(
                        onClick = {
                            if(!isAutoScanRunning.value){
                                isAutoScanRunning.value=true
                                makeSubnetsFromRangeAndCheckAvailability(autoScan = true)
                            }else{
                                scanJob?.cancel()
                                isAutoScanRunning.value=false
                                showToast(context, "Scan Cancelled by user" +
                                        "")
                                hostIdentifierList[0]=1
                                hostIdentifierList[1]=2
                                searchProgress.floatValue=0f
                            }
                        },
                        cancelState = isAutoScanRunning.value,
                        content = {
                            Icon(
                                imageVector = if(isAutoScanRunning.value)Icons.Rounded.Cancel else Icons.Rounded.WifiFind,
                                contentDescription = null,
                                tint = White
                            )
                            Spacer(modifier=Modifier.width(5.dp))
                            Text(
                                text = if(!isAutoScanRunning.value)"Auto Scan" else "Stop Scan",
                                fontFamily = bahnschriftFamily,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    Text(
                        text ="Total hosts to scan: ${if(checked.value) {
                            hostIdentifierList[1] - hostIdentifierList[0]
                        } else {
                            (if (hostIdentifierList[0] > 0) 1 else 0)
                        }} at ${localIPv4AD.split('.').take(3).joinToString(".")+"."}${if(!displayIPBeingSearched.value.isEmpty()) (displayIPBeingSearched.value).split('.').last() else "x"}:$loadPort;",
                        fontSize = 12.sp,
                        fontFamily = bahnschriftFamily,
                        color = Color.Gray
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    Text(
                        text = hostSearchResult.value + ";",
                        fontSize = 12.sp,
                        fontFamily = bahnschriftFamily,
                        color = Color.Gray
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center, modifier=Modifier
                    .weight(1f)
                    .fillMaxWidth()){
                    LazyColumn(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .fillMaxWidth()) {
                        items(
                            count = foundDeviceHostDetails.size
                        ){ index->
                            NetworkHostCard(
                                foundDeviceHostDetails[index].deviceName,
                                foundDeviceHostDetails[index].deviceIPv4,
                                loadPort.toString()
                            )
                        }
                    }
                }
            }
            //og
        }

    }
    //contruct-contextmenu
    @Composable
    fun AnimatedContextMenuContent(isOpen: Boolean, onClose: () -> Unit) {
        var firstOpen = remember { mutableStateOf(true) }
        // Animation value from 0 to 1
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 370),
            label = "sidebar scale anim"
        )
        // Smooth opacity animation
        val animatedOpacity by animateFloatAsState(
            targetValue = if (isOpen) 0.7f else 0f,
            animationSpec = tween(durationMillis = 370),
            label = "sidebar opacity anim"
        )

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

        Box(modifier = Modifier.fillMaxSize().graphicsLayer {
            // Set the transformOrigin to ensure scaling happens from the left edge
            transformOrigin = TransformOrigin(0.5f, 1f) // 0.5f = horizontal center, 1f = bottom
            scaleY = (animatedProgress)
        }) {
            // Sidebar content
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart=15.dp, topEnd=15.dp))
                    .fillMaxWidth()
                    .height(400.dp)
                    .align(alignment = Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
                    .clickable(enabled = if(firstOpen.value)true else false) { /* Prevent click-through */ }
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                FrigonTechRow(modifier = Modifier.height(40.dp).fillMaxWidth()
                    .height(60.dp), horizontal = Arrangement.Center) {
                    Text(
                        text = "-Actions-",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bahnschriftFamily,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable(onClick = { onClose() })
                            .padding(start = 15.dp)
                    )
                }
                HorizontalDivider()
                LazyColumn {
                    items(count=GetContextActions().size) { index-> /*list of options would be decided based on context(caller in this case)*/
                        val action = GetContextActions()[index]
                        SidebarMenuItem(action.icon, action.actionName, action.actionClick)
                    }
                }
            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedContextMenuContent(
        isOpen = showMenu.menuVisible.value,
        onClose = { showMenu.menuVisible.value = false }
    )

}