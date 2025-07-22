@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.frigontech.networkdrive

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.frigontech.networkdrive.ui.theme.ColorManager
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0green
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0warningred
import kotlinx.coroutines.launch
import java.io.File



fun folderNavigator(folderPath: String): List<Pair<String, String>> {
    val currentFolder = File(folderPath) // Base folder
    val folderList = mutableListOf<Pair<String, String>>() // pair of name and abs path

    currentFolder.listFiles()?.let { files ->
        for (file in files) {
            folderList.add(file.name to file.absolutePath) // Name-Path pair
        }
    }

//    currentFolder.listFiles()?.let { files ->
//        for (file in files) {
//            if (file.isDirectory) {
//                folderList.add(file.name to file.absolutePath) // Name-Path pair
//            }else if (file.is)
//        }
//    }
    return folderList
}

//fun openFileObject(){
//    //not folders, only files (folders opening have been taken care of already)
//    //WIP
//}

fun pasteFileObject(overwriteAll: Boolean=false) { //is overwrite all is false then we'll automatically perform overwrite for first file only
    if(!FileManagerData.isPasteOperationGoingOn.value){
        FileManagerData.isPasteOperationGoingOn.value = true
        if(FileManagerData.batchFilesToReplace.isNotEmpty()){
            if(FileManagerData.isBatchCopy.value){
                //copy
                if(!overwriteAll){
                    val currentFile = FileManagerData.batchFilesToReplace[0].first
                    val destinationFile = FileManagerData.batchFilesToReplace[0].second
                    if(currentFile.exists() && !destinationFile.exists()){
                        if(FileManagerData.isBatchCopy.value){
                            if(currentFile.copyTo(target = destinationFile, overwrite = true).exists()){
                                println("file copy success")
                                FileManagerData.batchFilesToReplace.remove(FileManagerData.batchFilesToReplace[0])
                            }else{
                                println("file copy failure")
                            }
                        }else{
                            if(destinationFile.delete()){
                                if(currentFile.renameTo(destinationFile)){
                                    println("file copy success")
                                    FileManagerData.batchFilesToReplace.remove(FileManagerData.batchFilesToReplace[0])
                                }else{
                                    println("file copy failure")
                                }
                            }else{
                                println("Failure in copying")
                            }
                        }
                    }
                }
                for (filePair in FileManagerData.batchFilesToReplace){
                    val currentFile = filePair.first
                    val destinationFolder = filePair.second
                    val destinationFile = File(destinationFolder, currentFile.name)
                    if(!currentFile.exists()){
                        println("source file doesn't exist")
                        continue
                    }
                    if(destinationFile.exists()){
                        FileManagerData.batchFilesToReplace.add(Pair(currentFile, destinationFile))
                        FileManagerData.isBatchCopy.value = true
                        showMenu.replaceMenu.value = true
                        break
                    }else{
                        if(currentFile.copyTo(target = destinationFile, overwrite = overwriteAll).exists()){
                            println("file copy success")
                        }else{
                            println("file copy failure")
                        }
                    }
                }

            }else{
                //cut
                for (filePair in FileManagerData.batchFilesToReplace){
                    val currentFile = filePair.first
                    val destinationFolder = filePair.second
                    val destinationFile = File(destinationFolder, currentFile.name)
                    if(!currentFile.exists()){
                        println("source file doesn't exist")
                        continue
                    }
                    if(destinationFile.exists()){
                        if(overwriteAll){
                            if(destinationFile.delete()){
                                if(currentFile.renameTo(destinationFile)){
                                    println("file copy success")
                                }else{
                                    println("file copy failure")
                                }
                            }else{
                                println("Failure in copying")
                            }
                        }
                    }else{
                        if(currentFile.renameTo(destinationFile)){
                            println("file copy success")
                        }else{
                            println("file copy failure")
                        }
                    }
                }
            }
            FileManagerData.batchFilesToReplace.clear()
        }else{
            if(FileManagerData.copiedFiles.isNotEmpty()){
                for (filePath in FileManagerData.copiedFiles){
                    val currentFile = File(filePath)
                    val destinationFolder = File(FileManagerData.currentFolder.value)
                    val destinationFile = File(destinationFolder, currentFile.name)
                    if(!currentFile.exists()){
                        println("source file doesn't exist")
                        continue
                    }
                    if(destinationFile.exists()){
                        FileManagerData.batchFilesToReplace.add(Pair(currentFile, destinationFile))
                        FileManagerData.isBatchCopy.value = true
                        showMenu.replaceMenu.value = true
                        break
                    }else{
                        if(currentFile.copyTo(destinationFile).exists()){
                            currentFile.copyTo(destinationFile).exists()
                            println("file copy success")
                        }else{
                            println("file copy failure")
                        }
                    }
                }
                FileManagerData.copiedFiles.clear()
            }else if(FileManagerData.cutFiles.isNotEmpty()){
                for (filePath in FileManagerData.cutFiles){
                    val currentFile = File(filePath)
                    val destinationFolder = File(FileManagerData.currentFolder.value)
                    val destinationFile = File(destinationFolder, currentFile.name)
                    if(!currentFile.exists()){
                        println("source file doesn't exist")
                        continue
                    }
                    if(destinationFile.exists()){
                        FileManagerData.batchFilesToReplace.add(Pair(currentFile, destinationFile))
                        FileManagerData.isBatchCopy.value = true
                        showMenu.replaceMenu.value = true
                        break
                    }else{
                        if(currentFile.renameTo(destinationFile)){
                            println("file copy success")
                        }else{
                            println("file copy failure")
                        }
                    }
                }
                FileManagerData.cutFiles.clear()
            }
        }
        FileManagerData.isPasteOperationGoingOn.value = false
    }
}

fun createFolder(folderName:String){
    val cwd = File(FileManagerData.currentFolder.value)
    val folderToCreate = File(cwd, folderName)
    folderToCreate.mkdirs()

}

//fun multiSelectFileObjects(){
//    //WIP
//
//}

object FileManagerData{
    val accessedServers = mutableStateListOf<Triple<String, String, String>>(Triple("My Device", "", "")) //Device Name, IP Address, FolderPath
    val refreshExtFileManager = mutableStateOf(false)
    val currentFolder = mutableStateOf("")
    val isMultiSelectOn = mutableStateOf(false)
    val multiSelectFiles = mutableStateListOf<String>()//absolute path
    var copiedFiles = mutableStateListOf<String>()
    var cutFiles = mutableStateListOf<String>()
    var batchFilesToReplace = mutableStateListOf<Pair<File, File>>() // [0] is original file ref, [1] is destination file ref
    val isBatchCopy = mutableStateOf(false) //use this to determine if replace menu has to replace in batch operations or mapping to server opr.
    val filesLeftWhileMappingToLFTUCServer = mutableStateListOf<String>()
    val isPasteOperationGoingOn = mutableStateOf(false)
    val isNavigatingServer = mutableStateOf(false)
    val currentServerFolder = mutableStateOf("")//server path
    val currentServerAddress = mutableStateOf("")
    val serverFolderContents = mutableStateListOf<String>()
    val lftuc_DownloadProgress = mutableFloatStateOf(0f)
    val lftuc_DownloadCompleteMessage = mutableStateOf("")
    val lftuc_FileToRequest = mutableStateOf("")
    val lftuc_RequestedFileSize = mutableStateOf("")
    val lftuc_isDownloadingFromServer = mutableStateOf(false)
    val lftuc_downloadStartTime = mutableStateOf("")
    val lftuc_downloadFinishTime = mutableStateOf("")
    val lftuc_totalDownloadDuration = mutableStateOf("")
}

@Composable
fun FileManagerPage(navSystem: NavController, focusManager: FocusManager){
    val context = LocalContext.current
    var hasReadPermission = remember { mutableStateOf(checkSpecificPermission(context, 8)) }
    var hasManagePermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mutableStateOf(Environment.isExternalStorageManager())
        } else {
            mutableStateOf(false)
        }
    }

    LaunchedEffect(hasReadPermission.value) {
        if (!hasReadPermission.value) {
            requestSpecificPermission(context, 8)
        }
    }
    //navigate to parent folder on server coroutine scope
    val navigateToParentFolderOnServerScope = rememberCoroutineScope()
    //current server name
    val currentServerName = remember{mutableStateOf("")}
    // Folder contents based on the current folder
    var folderContents by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LaunchedEffect(FileManagerData.currentFolder.value, FileManagerData.refreshExtFileManager.value) {
        folderContents = if (FileManagerData.currentFolder.value.isNotEmpty() || FileManagerData.refreshExtFileManager.value) {
            folderNavigator(FileManagerData.currentFolder.value)
        } else {
            emptyList()
        }
    }

    // Initial setup with the root directory
    LaunchedEffect(Unit) {
        FileManagerData.currentFolder.value = Environment.getExternalStorageDirectory().absolutePath
    }
    LaunchedEffect(FileManagerData.refreshExtFileManager.value) {
        if(FileManagerData.refreshExtFileManager.value){
            FileManagerData.refreshExtFileManager.value=false
        }
    }

    fun navigateToParentFolder() {
        val currentFile = File(FileManagerData.currentFolder.value)
        if(currentFile.parent?.let { it.isNotBlank() && it != "/storage/emulated" } == true){
            FileManagerData.currentFolder.value =  currentFile.parent
        }
    }

    fun navigateToParentFolderOnServer() {
        println("Moving From ${FileManagerData.currentServerFolder.value}")
        val parentDir = FileManagerData.currentServerFolder.value.split("/").dropLast(1).takeIf { it.isNotEmpty() }?.joinToString("/") ?: ""
        println("To $parentDir")

        requestFilesInServerDirectory(FileManagerData.currentServerAddress.value, parentDir,
            onSuccess = { files ->
                navigateToParentFolderOnServerScope.launch {
                    FileManagerData.serverFolderContents.clear()
                    FileManagerData.serverFolderContents.addAll(files)
                    FileManagerData.currentServerFolder.value = parentDir
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

    fun navigateToRootFolderOnServer() {
        requestFilesInServerDirectory(FileManagerData.currentServerAddress.value, "",
            onSuccess = { files ->
                navigateToParentFolderOnServerScope.launch {
                    FileManagerData.serverFolderContents.clear()
                    FileManagerData.serverFolderContents.addAll(files)
                    FileManagerData.currentServerFolder.value = ""
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
        FileManagerData.currentFolder.value = Environment.getExternalStorageDirectory().absolutePath
    }

    Column(modifier=Modifier
        .fillMaxSize()
        .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
        .clickable(onClick = {
            showMenu.menuVisible.value = true
            showMenu.caller.value =
                if (!FileManagerData.isNavigatingServer.value) "FileManagerPage" else "NetworkFileManagerPage"
        })) {
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
                    .clickable(onClick = { })
            )
        }, onGoToHome = {FileManagerData.multiSelectFiles.clear()}) //title bar
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
                            .clickable(onClick = { if (!FileManagerData.isNavigatingServer.value) navigateToParentFolder() else navigateToParentFolderOnServer() })
                    )
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier=Modifier
                            .padding(0.dp)
                            .size(37.dp)
                            .clickable(onClick = {
                                if (!FileManagerData.isNavigatingServer.value) {
                                    FileManagerData.refreshExtFileManager.value = true
                                }
                            })
                    )
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Root Dir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier=Modifier
                            .padding(0.dp)
                            .size(37.dp)
                            .clickable(onClick = { if (!FileManagerData.isNavigatingServer.value) navigateToRootFolder() else navigateToRootFolderOnServer() })
                    )
                    var displayServerFolderName = ""
                    var truncatedPath = ""

                    if (FileManagerData.isNavigatingServer.value) {
                        val serverInitials = "lftuc://${currentServerName.value}/"
                        val fullURI = serverInitials + FileManagerData.currentServerFolder.value
                        // When navigating on a server, create the folder path with possible truncation
                        displayServerFolderName =
                                if(fullURI.length > 23){
                                    "..." + fullURI.takeLast(23)
                                }else{fullURI}
                    } else {
                        // When not navigating on a server, simply truncate `currentFolder.value` if necessary
                        truncatedPath = if (FileManagerData.currentFolder.value.length > 23) {
                            "..."+FileManagerData.currentFolder.value.takeLast(23)
                        } else {
                            FileManagerData.currentFolder.value
                        }
                    }

                    Text(
                        text = if (!FileManagerData.isNavigatingServer.value) truncatedPath else displayServerFolderName,
                        fontFamily = bahnschriftFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .weight(1f)
                            .clickable(onClick = {
                                if (!FileManagerData.isNavigatingServer.value) showMenu.findPathMenu.value =
                                    true
                            })
                    )

                }
            }
        }

        val scope = rememberCoroutineScope()
        LazyColumn(modifier = Modifier
            .padding(start = 5.dp, end = 5.dp, top = 0.dp, bottom = 50.dp)
            .clip(RoundedCornerShape(11.dp))) {

            if (!FileManagerData.isNavigatingServer.value) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                    if(hasReadPermission.value)
                    {
                        println("has file read necessary permission")
                        items(count = folderContents.size, key = { index->folderContents[index].second }) { index ->
                            val currentFile = folderContents[index] // Get the Pair
                            val file = File(currentFile.second)
                            if(file.isDirectory){
                                FolderCard_ListView(
                                    folderName = currentFile.first,    // Access the name
                                    folderPath = currentFile.second,   // Access the absolute path
                                    onClick = {
                                        // Navigate to the clicked folder
                                        FileManagerData.currentFolder.value = currentFile.second
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
                        println("doesnt have necessary file read permission")
                    }
                }else{
                    if(hasManagePermission.value)
                    {
                        println("has file manage necessary permission")
                        items(count = folderContents.size, key = { index->folderContents[index].second }) { index ->
                            val currentFile = folderContents[index] // Get the Pair
                            val file = File(currentFile.second)
                            if(file.isDirectory){
                                FolderCard_ListView(
                                    folderName = currentFile.first,    // Access the name
                                    folderPath = currentFile.second,   // Access the absolute path
                                    onClick = {
                                        // Navigate to the clicked folder
                                        FileManagerData.currentFolder.value = currentFile.second
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
                        println("doesnt have necessary file manage permission")
                    }
                }

            }else{
                items(items = FileManagerData.serverFolderContents, key = { it }) { file ->
                    if(!file.contains(".")){
                        FolderCard_ListView(
                            folderName = file,    // Access the name
                            folderPath = file,   // Access the absolute path
                            onClick = {
                                showToast(context, "Requesting server DIR...")
                                val nextDir = if(FileManagerData.currentServerFolder.value.isNotEmpty()) (FileManagerData.currentServerFolder.value +"/"+ file) else (FileManagerData.currentServerFolder.value + file)
                                requestFilesInServerDirectory(FileManagerData.currentServerAddress.value, nextDir,
                                    onSuccess = { files ->
                                        scope.launch {
                                            FileManagerData.serverFolderContents.clear()
                                            FileManagerData.serverFolderContents.addAll(files)
                                            FileManagerData.currentServerFolder.value = nextDir
                                            showToast(context, "Success in requesting server DIR...")
                                            //isNavigatingServer.value = true
                                        }
                                    },
                                    onError = { error ->
                                        scope.launch {
                                            println("Failed to fetch files: $error")
                                            showToast(context, "Server might've went offline...")
                                            FileManagerData.serverFolderContents.clear()
                                            navigateToParentFolder()
                                            FileManagerData.isNavigatingServer.value = false
                                        }
                                    }
                                )
                            },
                            isNetworkFolder = true
                        )
                    }else{
                        FileCard_ListView(
                            fileName = file,    // Access the name
                            filePath = file,   // Access the absolute path
                            onClick = {
                                //Handle server file opening
                            },
                            isNetworkFile = true
                        )
                    }
                }
            }
        }
    }

    // Cache the actions based on the caller context
    val actions = remember(showMenu.caller.value) { GetContextActions(context) }
    //construct-context menu
    @Composable
    fun AnimatedContextMenuContent(isOpen: Boolean, onClose: () -> Unit) {
        Log.d("ContextMenu", "Menu state: isOpen=$isOpen")

        val transition = updateTransition(targetState = isOpen, label = "ContextMenuTransition")
        // Animation value from 0 to 1
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f, // Start fully expanded if not initialized
            animationSpec = tween(durationMillis = 370),
            label = "sidebar scale anim"
        )

        val scale by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 370) },
            label = "scale"
        ) { state ->
            if (state) 1f else 0f
        }

        val alpha by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 370) },
            label = "alpha"
        ) { state ->
            if (state) 1f else 0f
        }

        val animatedOpacity by remember {
            derivedStateOf { animatedProgress * 0.9f }
        }

        if (animatedOpacity > 0f || isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onClose() }
                    .alpha(animatedOpacity)
                    .background(Color.Black.copy(alpha = alpha))
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
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 1f) // From bottom
                        scaleY = scale
                    }
                    //.height((animatedProgress * 400).dp) // Animate the height
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
                        text = ("-") + (showMenu.caller.value.split('-')[0].takeLast(15)) + (" Actions -"), // Limit folder name to ensure identification
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
                LazyColumn {
                    itemsIndexed(actions) { index, action ->
                        key(action.actionName) {
                            SidebarMenuItem(action.icon, action.actionName, action.actionClick)
                        }
                    }
                }
                Spacer(Modifier.height(49.dp))
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
                            if(device.first != "My Device"){
                                showToast(context, "Requesting server DIR...")

                                requestFilesInServerDirectory(device.second, FileManagerData.currentServerFolder.value,
                                    onSuccess = { files ->
                                        scope.launch {
                                            FileManagerData.serverFolderContents.clear()
                                            FileManagerData.serverFolderContents.addAll(files)
                                            currentServerName.value = device.first
                                            showToast(context, "Success in requesting server DIR...")
                                            FileManagerData.isNavigatingServer.value = true
                                            FileManagerData.currentServerAddress.value = device.second
                                            onClose()
                                        }
                                    },
                                    onError = { error ->
                                        scope.launch {
                                            println("Failed to fetch files: $error")
                                            showToast(context, "Server went offline unexpectedly...")
                                            FileManagerData.isNavigatingServer.value = false
                                            FileManagerData.accessedServers.remove(device)
                                            onClose()
                                        }
                                    }
                                )
                            }else{
                                FileManagerData.isNavigatingServer.value = false
                                FileManagerData.currentServerFolder.value = ""
                                navigateToParentFolder()
                                onClose()
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

    //construct-Storage menu
    @Composable
    fun AnimatedReplaceFileMenuContent(isOpen: Boolean, onClose: () -> Unit) {
        Log.d("ContextMenu", "Menu state: isOpen=$isOpen")
        // Cache the actions based on the caller context

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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .width((animatedProgress * 330).dp)
                    .height((animatedProgress * 335).dp) // Animate the height
                    //.align(Alignment.BottomCenter) // Anchor to bottom of the screen
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
                    .background(color = MaterialTheme.colorScheme.background)
                    .clickable(enabled = false) { /* Prevent click-through */ }
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(color = MaterialTheme.colorScheme.surface),
                    horizontal = Arrangement.Center,
                ) {
                    Text(
                        text = "Network Drive: File Manager",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bahnschriftFamily,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider()
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(10.dp)){
                    Text(
                        text = "Do you wanna replace existing files in destination with current?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bahnschriftFamily,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(37.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(color = MaterialTheme.colorScheme.surface)
                        .clickable(onClick = {
                            showMenu.replaceSingle.value = true
                            //replace first file in the list (which caused this menu from the LFTUCxHandler)
                            //and transfer rest like normal
                            if (!FileManagerData.isBatchCopy.value) {
                                mapFileObjectToLFTUCServer(
                                    fileObjectList = listOf(FileManagerData.filesLeftWhileMappingToLFTUCServer[0]),
                                    replaceFiles = true
                                )
                                if (!FileManagerData.filesLeftWhileMappingToLFTUCServer.isEmpty()) {
                                    val startIndex = 1
                                    val lastIndex =
                                        FileManagerData.filesLeftWhileMappingToLFTUCServer.size
                                    mapFileObjectToLFTUCServer(
                                        fileObjectList = FileManagerData.filesLeftWhileMappingToLFTUCServer.subList(
                                            startIndex,
                                            lastIndex
                                        ),
                                        replaceFiles = false
                                    )
                                }
                            } else {
                                pasteFileObject()
                            }
                            FileManagerData.refreshExtFileManager.value = true
                            onClose()
                        }
                        ),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = "Replace",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(37.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            color = if (FileManagerData.filesLeftWhileMappingToLFTUCServer.lastIndex > 0) MaterialTheme.colorScheme.surface else Color.Gray.copy(
                                0.5f
                            )
                        )
                        .clickable(onClick = {
                            showMenu.replaceAll.value = true
                            //replace all files in destination
                            if (!FileManagerData.isBatchCopy.value) {
                                mapFileObjectToLFTUCServer(
                                    fileObjectList = FileManagerData.filesLeftWhileMappingToLFTUCServer,
                                    replaceFiles = true
                                )
                            } else {
                                pasteFileObject(true)
                            }
                            FileManagerData.refreshExtFileManager.value = true
                            onClose()
                        }
                        ),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = "Replace All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(37.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(color = ColorManager(frigontech0warningred))
                        .clickable(onClick = { onClose() }),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = "Cancel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily
                    )
                }

            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedReplaceFileMenuContent(
        isOpen = showMenu.replaceMenu.value,
        onClose = { showMenu.replaceMenu.value = false }
    )

    //construct-Storage menu
    @Composable
    fun AnimatedCreateFolderMenuContent(isOpen: Boolean, onClose: () -> Unit) {
        Log.d("ContextMenu", "Menu state: isOpen=$isOpen")
        // Cache the actions based on the caller context

        // Animation value from 0 to 1
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 270),
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

        val folderName = remember{ mutableStateOf("") }

        // Sidebar content properly aligned at the bottom
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .width((animatedProgress * 330).dp)
                    .height((animatedProgress * 335).dp) // Animate the height
                    //.align(Alignment.BottomCenter) // Anchor to bottom of the screen
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
                    .background(color = MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(color = MaterialTheme.colorScheme.surface),
                    horizontal = Arrangement.Center,
                ) {
                    Text(
                        text = "Network Drive: File Manager",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bahnschriftFamily,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider()
                Column(modifier = Modifier
                    .height(47.dp)
                    .padding(10.dp)){
                    Text(
                        text = "Name new folder",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(99.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    horizontal = Arrangement.Center,
                ){
                    OutlinedTextField(
                        modifier=Modifier.fillMaxWidth(),
                        value = folderName.value,  // Access the value property here
                        onValueChange = {
                            val invalidChars = listOf('*', '`', '/', '\\', '?', ':', ';', '"', '\'', '<', '>', '=', '~', '|', '.')
                            if(it.isNotEmpty() && it.last() !in invalidChars){
                                if(folderName.value != "\u0000") {
                                    folderName.value = it.trim()
                                }else{
                                    folderName.value = ""
                                }
                            }else{
                                folderName.value = ""
                            }
                        },
                        label = { Text("Enter Folder Name", fontFamily = bahnschriftFamily, fontSize = 13.sp) }
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(37.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            color = if (FileManagerData.filesLeftWhileMappingToLFTUCServer.lastIndex > 0) MaterialTheme.colorScheme.surface else Color.Gray.copy(
                                0.5f
                            )
                        )
                        .clickable(onClick = {
                            //create new folder
                            if (folderName.value.isNotEmpty()) {
                                createFolder(folderName.value)
                                folderName.value = ""
                                onClose()
                            } else {
                                showToast(context, "You have to enter a valid folder name.")
                            }
                        }
                        ),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = "Create Folder",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(37.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(color = ColorManager(frigontech0warningred))
                        .clickable(onClick = { folderName.value = "";onClose() }),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = "Cancel",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily
                    )
                }

            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedCreateFolderMenuContent(
        isOpen = showMenu.createFolderDialogue.value,
        onClose = { showMenu.createFolderDialogue.value = false }
    )

    //construct-Storage menu
    @Composable
    fun AnimatedDownloadMenuContent(isOpen: Boolean, onClose: () -> Unit) {
        Log.d("ContextMenu", "Menu state: isOpen=$isOpen")
        // Cache the actions based on the caller context

        // Animation value from 0 to 1
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 270),
            label = "sidebar scale anim"
        )
        val animatedOpacity = animatedProgress * 0.9f

        val downloadProgress by FileManagerData.lftuc_DownloadProgress
        val downloadProgressText by FileManagerData.lftuc_DownloadCompleteMessage
        val downloadFileSize by FileManagerData.lftuc_RequestedFileSize
        val totalDownloadTime by FileManagerData.lftuc_totalDownloadDuration

        // Background overlay
        if (animatedOpacity > 0f || isOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { if (!FileManagerData.lftuc_isDownloadingFromServer.value) onClose() }
                    .alpha(animatedOpacity)
                    .background(Color.Black.copy(alpha = animatedOpacity))
            )
        }

        // Sidebar content properly aligned at the bottom
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .width((animatedProgress * 330).dp)
                    .height((animatedProgress * 380).dp) // Animate the height
                    //.align(Alignment.BottomCenter) // Anchor to bottom of the screen
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(color = MaterialTheme.colorScheme.surface)
                        .padding(3.dp),
                    horizontal = Arrangement.Center,
                ) {
                    Text(
                        text = "Network Drive: Download Manager",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bahnschriftFamily,
                        overflow = TextOverflow.Ellipsis,
                        //modifier = Modifier.padding(5.dp)
                    )
                }
                HorizontalDivider()
                Column(modifier = Modifier
                    .height(47.dp)
                    .padding(3.dp)){
                    Text(
                        text = "Downloading File From LFTUC Server...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    horizontal = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(11.dp)
                            .clip(RoundedCornerShape(17.dp)),
                        color = ColorManager(frigontech0green),
                        trackColor = Color.Black
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            color = (MaterialTheme.colorScheme.surface)
                        ),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = if(downloadProgressText.isEmpty())
                            "${(downloadProgress*100).toInt()}% Complete of $downloadFileSize; recording download duration..."
                        else "Download Complete: $downloadProgressText, file size: $downloadFileSize; took $totalDownloadTime",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(37.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            color = if (downloadProgressText.isEmpty()) ColorManager(
                                frigontech0warningred
                            ) else ColorManager(frigontech0green)
                        )
                        .clickable(onClick = {
                            if (downloadProgressText.isEmpty()) {
                                cancelLFTUCFileDownload()
                                FileManagerData.lftuc_isDownloadingFromServer.value = false
                                FileManagerData.lftuc_DownloadProgress.floatValue = 0f
                                FileManagerData.lftuc_RequestedFileSize.value = ""
                                FileManagerData.lftuc_DownloadCompleteMessage.value = ""
                                showToast(context, "File download cancelled!")
                            } else {
                                //If download is complete
                                onClose()
                                FileManagerData.lftuc_isDownloadingFromServer.value = false
                                FileManagerData.lftuc_DownloadProgress.floatValue = 0f
                                FileManagerData.lftuc_RequestedFileSize.value = ""
                                FileManagerData.lftuc_DownloadCompleteMessage.value = ""
                            }
                        }),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = if(downloadProgressText.isEmpty()) "Cancel" else "Done",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily
                    )
                }

            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedDownloadMenuContent(
        isOpen = showMenu.downloadFileDialogue.value,
        onClose = { showMenu.downloadFileDialogue.value = false }
    )

    //construct-find directory menu
    @Composable
    fun AnimatedFindPathMenuContent(isOpen: Boolean, onClose: () -> Unit) {

        // Animation value from 0 to 1
        val animatedProgress by animateFloatAsState(
            targetValue = if (isOpen) 1f else 0f,
            animationSpec = tween(durationMillis = 270),
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

        val pathName = remember{ mutableStateOf("") }

        // Sidebar content properly aligned at the bottom
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .width((animatedProgress * 330).dp)
                    .height((animatedProgress * 335).dp) // Animate the height
                    //.align(Alignment.BottomCenter) // Anchor to bottom of the screen
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
                    .background(color = MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(color = MaterialTheme.colorScheme.surface),
                    horizontal = Arrangement.Center,
                ) {
                    Text(
                        text = "Network Drive: File Manager",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = bahnschriftFamily,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider()
                Column(modifier = Modifier
                    .height(47.dp)
                    .padding(10.dp)){
                    Text(
                        text = "Find Directory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(99.dp)
                        .background(color = MaterialTheme.colorScheme.background),
                    horizontal = Arrangement.Center,
                ){
                    OutlinedTextField(
                        modifier=Modifier.fillMaxWidth(),
                        value = pathName.value,  // Access the value property here
                        onValueChange = {
                            val invalidChars = listOf('*', '`', '\\', '?', ':', ';', '"', '\'', '<', '>', '=', '~', '|')
                            if(it.isNotEmpty() && it.last() !in invalidChars){
                                if(pathName.value != "\u0000") {
                                    pathName.value = it.trim()
                                }else{
                                    pathName.value = ""
                                }
                            }else{
                                pathName.value = ""
                            }
                        },
                        label = { Text("Enter Directory Path", fontFamily = bahnschriftFamily, fontSize = 13.sp) }
                    )
                }
                FrigonTechRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(37.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            color = if (FileManagerData.filesLeftWhileMappingToLFTUCServer.lastIndex > 0) MaterialTheme.colorScheme.surface else Color.Gray.copy(
                                0.5f
                            )
                        )
                        .clickable(onClick = {
                            //check and navigate to path if it exists
                            val path = File(pathName.value)
                            if (path.isDirectory) {
                                FileManagerData.currentFolder.value = pathName.value
                                onClose()
                            } else {
                                showToast(context, "Invalid Path")
                                onClose()
                            }
                        }
                        ),
                    horizontal = Arrangement.Center,
                ){
                    Text(
                        text = "Find Directory",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = bahnschriftFamily
                    )
                }
            }
        }
    }
    // Render the sidebar overlay and content
    AnimatedFindPathMenuContent(
        isOpen = showMenu.findPathMenu.value,
        onClose = { showMenu.findPathMenu.value = false }
    )
}