package com.frigontech.networkdrive

import android.R
import android.os.Looper
import android.util.Log
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.frigontech.networkdrive.ui.theme.ColorManager
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0warningred
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.utils.SmbFiles
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
import java.net.InetAddress
import java.net.URL
import jcifs.netbios.NbtAddress
import java.net.UnknownHostException
import com.frigontech.lftuc_1.lftuc_main_lib.*


@Composable
fun SearchHostPage (navSystem: NavController, focusManager: FocusManager){
    val context = LocalContext.current
    var loadPort = retrieveTextData(context, "port").toIntOrNull()?: 8080
    var hostSearchResult: MutableState<String> = remember{ mutableStateOf("No host(s) found on port: ${loadPort}")}
    var searchProgress = remember{ mutableFloatStateOf(0f) }
    var isScanRunning = remember {mutableStateOf(false)}
    val followSMBProtocol = retrieveTextData(context, "SMB").let { text ->
        if (text.isNullOrBlank()) false else (text == "true")
        //------------------------------------//^^^^^^^^^^^ this is a way to directly pass bool value
    }
    val LFTUCServers = remember{mutableStateListOf<LFTUCServers>()}

    LaunchedEffect(Unit) {
        while (true) {
            if(isScanRunning.value){
                // Get new servers from the module
                val currentServers = lftuc_getCurrentServers() // Replace with actual call

                // Only add servers that aren't already in the list
                val updatedServers = currentServers.filterNot { it in LFTUCServers }

                if (updatedServers.isNotEmpty()) {
                    LFTUCServers.addAll(updatedServers)
                }

                //get servers that went offline
                val outdatedServers = LFTUCServers.filterNot { it in currentServers }

                if(outdatedServers.isNotEmpty()){
                    LFTUCServers.removeAll(outdatedServers)
                }
            }
            delay(1000) // Poll every 1s
        }
    }

    Column(modifier=Modifier
        .fillMaxSize()
        .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }) {
        var scanMessage = "Scan for LFTUC Servers"


        TitleBar(title="Search Hosts", navSystem=navSystem, {})
        Box(
            modifier = Modifier
                .weight(1f) // This makes the Box take all remaining space
                .fillMaxWidth()
                .padding(bottom=5.dp, start=5.dp, end=5.dp,top=0.dp)
        ){
            Column {
//                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center,
//                    modifier=Modifier.padding(0.dp)) {
//                    LinearProgressIndicator(
//                        progress = { searchProgress.floatValue },
//                        modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)),
//                        color = MaterialTheme.colorScheme.tertiary,
//                    )
//                } commenting it for reference to add it in file transfer operations
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    FrigonTechStateButton(
                        onClick = {
                            if(!isScanRunning.value){
                                isScanRunning.value=true
                                startScanningForServers(context, port=loadPort)
                            }else{
                                stopScanningForServers()
                                isScanRunning.value=false
                                showToast(context, "Scan Cancelled by user")
                                searchProgress.floatValue=0f
                            }
                        },
                        canCancel = isScanRunning.value,
                        content = {
                            Icon(
                                imageVector = if(isScanRunning.value)Icons.Rounded.Cancel else Icons.Rounded.WifiFind,
                                contentDescription = null,
                                tint = White
                            )
                            Spacer(modifier=Modifier.width(5.dp))
                            Text(
                                text = if(!isScanRunning.value)"Auto Scan" else "Stop Scan",
                                fontFamily = bahnschriftFamily,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                        .fillMaxWidth()
                    ) {
                        items(LFTUCServers) { server->
                            NetworkHostCard(
                                server.ServerName,
                                server.ServerAddress,
                                server.ServerPort.toString()
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
        Log.d("ContextMenu", "Menu state: isOpen=$isOpen")
        // Cache the actions based on the caller context
        val actions = remember(showMenu.caller.value) { GetContextActions(context) }

        // Animation value from 0 to 1
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f, // Start fully expanded if not initialized
            animationSpec = tween(durationMillis = 370),
            label = "sidebar scale anim"
        )
        val animatedOpacity = animatedProgress * 0.9f

        // Background overlay
        if (animatedOpacity > 0f || isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClose() }
                    .alpha(animatedOpacity)
                    .background(Color.Black.copy(alpha = animatedOpacity))
            )
        }

        // Sidebar content properly aligned at the bottom
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                    .fillMaxWidth()
                    .height((animatedProgress * 400).dp) // Animate the height
                    .align(Alignment.BottomCenter) // Anchor to bottom of the screen
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
                    .clickable(enabled = false) { /* Prevent click-through */ }
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                FrigonTechRow(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontal = Arrangement.Center
                ) {
                    Text(
                        text = "-Actions-", // Limit folder name to ensure identification
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
                    items(count = actions.size) { index -> /* List of options based on context */
                        val action = actions[index]
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
    //construct-SMB Auth
    @Composable
    fun AnimatedSMBAuth(isOpen: Boolean, onClose: () -> Unit) {
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 370),
            label = "sidebar scale anim"
        )
        LaunchedEffect(Unit) {
            delay(800)
        }
        val username = remember{mutableStateOf("")}
        val password = remember{mutableStateOf("")}
        val authSuccessful = remember{mutableStateOf(false)}

        if (isOpen || animatedProgress > 0f) { // Ensures animation completes before disappearing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = animatedProgress * 0.5f)) // Fade effect
                    .clickable {focusManager.clearFocus()} // do nothing
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                        .fillMaxWidth()
                        .height((animatedProgress * 400).dp) // Animate the height
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                        .clickable(enabled = false) {} // Prevent click-through
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Username", fontSize = 16.sp)
                    TextField(value = username.value, onValueChange = {newValue->username.value=newValue}, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Password", fontSize = 16.sp)
                    TextField(
                        value = password.value, onValueChange = {newValue->password.value=newValue},
                        //visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = false, onCheckedChange = {})
                        Text("Remember Creds")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onClose, // Call onClose instead of modifying state directly
                            colors = ButtonDefaults.buttonColors(containerColor = ColorManager(frigontech0warningred))
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {},//handle authorization
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Go", color = Color.White)
                        }
                    }
                }
            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedSMBAuth(
        isOpen = showMenu.showInputDialogue.value,
        onClose = { showMenu.showInputDialogue.value = false }
    )
}