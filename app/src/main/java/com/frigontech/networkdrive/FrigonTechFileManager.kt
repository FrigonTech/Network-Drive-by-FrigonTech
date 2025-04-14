@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.frigontech.networkdrive

import android.os.Environment
import android.util.Log
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DevicesOther
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.io.File

fun folderNavigator(folderPath: String): List<Pair<String, String>> {
    val currentFolder = File(folderPath) // Base folder
    val folderList = mutableListOf<Pair<String, String>>() // pair of name and abs path

    currentFolder.listFiles()?.let { files ->
        for (file in files) {
            if (file.isDirectory) {
                folderList.add(file.name to file.absolutePath) // Name-Path pair
            }
        }
    }
    return folderList
}

object FileManagerData{
    val accessedServers = mutableStateListOf<Triple<String, String, String>>(Triple("My Device", "", "")) //Device Name, IP Address, FolderPath
    val refreshExtFileManager = mutableStateOf(false)
}

@Composable
fun FileManagerPage(navSystem: NavController, focusManager: FocusManager){
    val context = LocalContext.current
    val isNavigatingServer = remember{mutableStateOf(false)}
    var hasReadPermission = remember { mutableStateOf(checkSpecificPermission(context, 8)) }
    LaunchedEffect(hasReadPermission.value) {
        if (!hasReadPermission.value) {
            requestSpecificPermission(context, 8)
        }
    }
    // State to hold the current folder's path
    val currentFolder = remember { mutableStateOf("") }
    //navigate to parent folder on server coroutine scope
    val navigateToParentFolderOnServerScope = rememberCoroutineScope()
    //current server name
    val currentServerName = remember{mutableStateOf("")}
    //current server IP Address
    val currentServerAddress = remember{mutableStateOf("")}
    // State to Hold Server Path
    val currentServerFolder = remember{mutableStateOf("")}
    // Folder contents based on the current folder
    val folderContents = remember(currentFolder.value, FileManagerData.refreshExtFileManager.value) {
        // Fetch folder contents only if the condition is true
        if (currentFolder.value.isNotEmpty() || FileManagerData.refreshExtFileManager.value) {
            folderNavigator(currentFolder.value) // Fetch folder names and paths
        } else {
            emptyList() // Return an empty list if the condition is false
        }
    }
    //server Folder contents based on current server folder
    val serverFolderContents = remember{mutableStateListOf<String>()}
    // Initial setup with the root directory
    LaunchedEffect(Unit) {
        currentFolder.value = Environment.getExternalStorageDirectory().absolutePath
    }
    LaunchedEffect(FileManagerData.refreshExtFileManager.value) {
        if(FileManagerData.refreshExtFileManager.value){
            FileManagerData.refreshExtFileManager.value=false
        }
    }

    fun navigateToParentFolder() {
        val currentFile = File(currentFolder.value)
        if(currentFile.parent?.let { it.isNotBlank() && it != "/storage/emulated" } == true){
            currentFolder.value =  currentFile.parent
        }
         //else Environment.getExternalStorageDirectory().absolutePath // Default to root Directory if null
    }

    fun navigateToParentFolderOnServer() {
        val parentDir = currentServerFolder.value.split("/").dropLast(1).takeIf { it.size > 1 }?.joinToString("/") ?: ""

        requestFilesInServerDirectory(currentServerAddress.value, parentDir,
            onSuccess = { files ->
                navigateToParentFolderOnServerScope.launch {
                    serverFolderContents.clear()
                    serverFolderContents.addAll(files)
                    currentServerFolder.value = parentDir
                    showToast(context, "Success in requesting server DIR...")
                }
            },
            onError = { error ->
                navigateToParentFolderOnServerScope.launch {
                    println("Failed to fetch files: $error")
                    showToast(context, "Failure in requesting server DIR...")
                }
            }
        )
    }

    fun navigateToRootFolder() {
        currentFolder.value = Environment.getExternalStorageDirectory().absolutePath
    }

    Column(modifier=Modifier
        .fillMaxSize()
        .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }) {
        TitleBar(title = "Device Storage", navSystem=navSystem, {
            Icon(
                imageVector = Icons.Rounded.Storage,
                contentDescription = "Storage",
                tint = MaterialTheme.colorScheme.primary,
                modifier=Modifier
                    .padding(0.dp)
                    .size(25.dp)
                    .clickable(onClick = { showMenu.storageMenu.value = true })
            )
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.primary,
                modifier=Modifier
                    .padding(0.dp)
                    .size(25.dp)
                    .clickable(onClick = {  })
            )
        }) //title bar
        FrigonTechRow(modifier=Modifier
            .height(60.dp)
            .padding(0.dp)) {  //navigation bar
            FrigonTechBox(modifier=Modifier.padding(0.dp)) {
                Row(modifier=Modifier
                    .fillMaxWidth()
                    .padding(0.dp), verticalAlignment = Alignment.CenterVertically){
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Go Back",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier=Modifier
                            .padding(0.dp)
                            .size(45.dp)
                            .clickable(onClick = { if(!isNavigatingServer.value) navigateToParentFolder() else navigateToParentFolderOnServer() })
                    )
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier=Modifier
                            .padding(0.dp)
                            .size(37.dp)
                            .clickable(onClick = { if(!isNavigatingServer.value){
                                FileManagerData.refreshExtFileManager.value=true} })
                    )
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Root Dir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier=Modifier
                            .padding(0.dp)
                            .size(37.dp)
                            .clickable(onClick = {navigateToRootFolder()})
                    )
                    var displayServerFolderName = ""
                    if(isNavigatingServer.value){
                        displayServerFolderName = currentServerFolder.value.takeIf { !it.startsWith("/") } ?: currentServerFolder.value.substring(1)
                    }
                    Text(
                        text = if(!isNavigatingServer.value) currentFolder.value else ("lftuc://"+currentServerName.value +"/"+ displayServerFolderName),
                        fontFamily = bahnschriftFamily,
                        fontSize = 14.sp,
                        color=MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.padding(start=5.dp)
                    )
                }
            }
        }

        if (hasReadPermission.value ) {
            val scope = rememberCoroutineScope()
            LazyColumn(modifier = Modifier
                .padding(start = 5.dp, end = 5.dp, top = 0.dp, bottom = 50.dp)
                .clip(RoundedCornerShape(11.dp))) {
                if(!isNavigatingServer.value){
                    items(folderContents.size) { index ->
                        val currentFile = folderContents[index] // Get the Pair
                        val file = File(currentFile.second)
                        if(file.isDirectory){
                            FolderCard_ListView(
                                folderName = currentFile.first,    // Access the name
                                folderPath = currentFile.second,   // Access the absolute path
                                onClick = {
                                    // Navigate to the clicked folder
                                    currentFolder.value = currentFile.second
                                }
                            )
                        }else{
                            FileCard_ListView(
                                fileName = currentFile.first,    // Access the name
                                filePath = currentFile.second,   // Access the absolute path
                                onClick = {
                                    // handle open file logic
                                }
                            )
                        }

                    }
                }else{
                    items(serverFolderContents) { file ->
                        if(!file.contains(".")){
                            FolderCard_ListView(
                                folderName = file,    // Access the name
                                folderPath = file,   // Access the absolute path
                                onClick = {
                                    showToast(context, "Requesting server DIR...")
                                    val nextDir = (currentServerFolder.value.takeIf { !it.startsWith("/") } ?: currentServerFolder.value.substring(1)) +"/"+ file
                                    requestFilesInServerDirectory(currentServerAddress.value, nextDir,
                                        onSuccess = { files ->
                                            scope.launch {
                                                serverFolderContents.clear()
                                                serverFolderContents.addAll(files)
                                                currentServerFolder.value = nextDir
                                                showToast(context, "Success in requesting server DIR...")
                                                //isNavigatingServer.value = true
                                            }
                                        },
                                        onError = { error ->
                                            scope.launch {
                                                println("Failed to fetch files: $error")
                                                showToast(context, "Failure in requesting server DIR...")
                                                //isNavigatingServer.value = false
                                            }
                                        }
                                    )
                                }
                            )
                        }else{
                            FileCard_ListView(
                                fileName = file,    // Access the name
                                filePath = file,   // Access the absolute path
                                onClick = {
                                    //Handle server file opening
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    //construct-context menu
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
                        text = ("-") + (showMenu.caller.value.split('-')[0].takeLast(15)) + (" Actions-"), // Limit folder name to ensure identification
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

    //construct-Storage menu
    @Composable
    fun AnimatedStorageMenuContent(isOpen: Boolean, onClose: () -> Unit) {
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
                        text = "-Storage/Servers-", // Limit folder name to ensure identification
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
                val scope = rememberCoroutineScope()
                LazyColumn {
                    items(FileManagerData.accessedServers) {device->
                        SidebarMenuItem(Icons.Rounded.DevicesOther, device.first,({
                            if(device.second != "My Device"){
                                showToast(context, "Requesting server DIR...")

                                requestFilesInServerDirectory(device.second, currentServerFolder.value,
                                    onSuccess = { files ->
                                        scope.launch {
                                            serverFolderContents.clear()
                                            serverFolderContents.addAll(files)
                                            currentServerName.value = device.first
                                            showToast(context, "Success in requesting server DIR...")
                                            isNavigatingServer.value = true
                                            currentServerAddress.value = device.second
                                            onClose()
                                        }
                                    },
                                    onError = { error ->
                                        scope.launch {
                                            println("Failed to fetch files: $error")
                                            showToast(context, "Server went offline unexpectedly...")
                                            isNavigatingServer.value = false
                                            FileManagerData.accessedServers.remove(device)
                                            onClose()
                                        }
                                    }
                                )
                            }
                        })) //make an arrangement
                    }
                }
            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedStorageMenuContent(
        isOpen = showMenu.storageMenu.value,
        onClose = { showMenu.storageMenu.value = false }
    )

}