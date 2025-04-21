package com.frigontech.networkdrive

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.health.connect.datatypes.units.Length
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.rounded.Adb
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.LibraryAddCheck
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.RemoveDone
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextField
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//vars
object showMenu{
    var menuVisible= mutableStateOf(false)
    var caller= mutableStateOf("")
    var folderPath: MutableState<String?> = mutableStateOf<String?>("")
    var filePath: MutableState<String?> = mutableStateOf<String?>("")
    var currentServer = mutableStateOf("")
    var currentServerName = mutableStateOf("")
    var showInputDialogue = mutableStateOf(false)
    var storageMenu = mutableStateOf(false)
    var replaceMenu = mutableStateOf(false)
    var replaceSingle = mutableStateOf(false)
    var replaceAll = mutableStateOf(false)
    var onDecisionMade: (() -> Unit)? = null
    var createFolderDialogue = mutableStateOf(false)
    val downloadFileDialogue = mutableStateOf(false)
    val findPathMenu = mutableStateOf(false)
}

//controlling instead of setting all variables individually
fun snackbarController(message: String, actionLabel:String, dur: Long, navController: NavController?){
    showSnackBarMenu.message.value = message
    showSnackBarMenu.actionLabel.value = actionLabel
    showSnackBarMenu.dur.value = dur
    showSnackBarMenu.navSystem.value = navController
}
//mutable state to make snackbar appear on screen
object showSnackBarMenu{
    var show = mutableStateOf(false)
    var message = mutableStateOf("")
    var actionLabel = mutableStateOf("")
    var dur = mutableStateOf<Long>(2000)
    var navSystem: MutableState<NavController?> = mutableStateOf<NavController?>(null)
}

@Immutable
data class ContextAction(
    val icon: ImageVector,
    val actionName:String,//name of context action
    val actionClick: () -> Unit = {}//event that'll happen on clicking that context action
)

fun GetContextActions(context: Context):List<ContextAction>{//setup actions based on context
    val contextActionsList = mutableListOf<ContextAction>()
    when{
        showMenu.caller.value.contains("NHC")-> {//Network Host Card
            contextActionsList.clear()
            // Uses a pop-up on the other device // Still experimental!
            contextActionsList.add(ContextAction(Icons.Rounded.Adb, "Grab Access Point", {
                try{
                    if(!FileManagerData.accessedServers.any { it.second.contains(showMenu.currentServer.value)}){
                        FileManagerData.accessedServers.add(Triple(showMenu.currentServerName.value, showMenu.currentServer.value, ""))
                        showToast(context, "Successfully grabbed access point...")
                    }else{
                        showToast(context, "Access point already exists...")
                    }
                }catch(e: Exception){
                    showToast(context, "There was a problem in grabbing access point...")
                }

            }))
        }
        showMenu.caller.value.contains("NDC")-> {//Network Device Card (devices that are trying to connect or connected to host)
            contextActionsList.clear()
            contextActionsList.add(ContextAction(Icons.Rounded.LibraryAddCheck, "Allow Join Request", {}))
            contextActionsList.add(ContextAction(Icons.Rounded.RemoveDone, "Dismiss Join Request", {}))
        }
        showMenu.caller.value.contains("FolderCard")-> {//Folders
            contextActionsList.clear()
            contextActionsList.add(ContextAction(Icons.Rounded.FolderOpen, "Open Folder", {
                //add a safe check to prevent this opetion frmo working while multiselect
                if(!FileManagerData.isMultiSelectOn.value){
                    showMenu.menuVisible.value = false
                    FileManagerData.currentFolder.value = showMenu.folderPath.value?:""
                }else{
                    showToast(context, "This operation is invalid in multi-select mode")
                }
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.FolderCopy, "Copy Folder", {
                //remove all previous copied files for reserving space for just this folder
                if(!showMenu.folderPath.value!!.isEmpty()){
                    FileManagerData.batchFilesToReplace.clear()
                    FileManagerData.cutFiles.clear()
                    FileManagerData.copiedFiles.clear()
                    FileManagerData.copiedFiles.addAll(listOf((showMenu.folderPath.value)?:""))
                    showToast(context, "folder copied")
                }
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.ContentCut, "Cut Folder", {
                //remove all previous cut files to reserve space in memory for this file
                if(!showMenu.folderPath.value!!.isEmpty()) {
                    FileManagerData.batchFilesToReplace.clear()
                    FileManagerData.copiedFiles.clear()
                    FileManagerData.cutFiles.clear()
                    FileManagerData.cutFiles.addAll(listOf((showMenu.folderPath.value) ?: ""))
                    showToast(context, "folder cut into memory")
                }
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.ContentPaste, "Paste Here", {
                pasteFileObject()
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.WifiTethering, "Map To Network",
                {//perform a check for if the id and password is configured!
                    val resolvedPath = showMenu.folderPath.value?:"invalid"
                    if(resolvedPath.isNotEmpty()){
                        mapFileObjectToLFTUCServer(
                            fileObjectList = listOf()
                        )
                        showToast(context, "folder mapped to server")
                    }else{
                        showToast(context, "folder not mapped to server")
                    }
                }))
            contextActionsList.add(ContextAction(Icons.Rounded.CreateNewFolder, "Create Folder Here", {
                showMenu.createFolderDialogue.value = true
                showToast(context, "folder created")
            }))
        }
        showMenu.caller.value.contains("FileCard")-> {//Files
            contextActionsList.clear()
            contextActionsList.add(ContextAction(Icons.Rounded.FileOpen, "Open File", {}))
            contextActionsList.add(ContextAction(Icons.Rounded.FileCopy, "Copy File", {
                if(!showMenu.filePath.value!!.isEmpty()) {
                    FileManagerData.batchFilesToReplace.clear()
                    FileManagerData.cutFiles.clear()
                    FileManagerData.copiedFiles.clear()
                    FileManagerData.copiedFiles.addAll(listOf((showMenu.filePath.value) ?: ""))
                    showToast(context, "file copied")
                }
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.ContentCut, "Cut File", {
                if(!showMenu.filePath.value!!.isEmpty()) {
                    FileManagerData.batchFilesToReplace.clear()
                    FileManagerData.copiedFiles.clear()
                    FileManagerData.cutFiles.clear()
                    FileManagerData.cutFiles.addAll(listOf((showMenu.filePath.value) ?: ""))
                    showToast(context, "file cut into memory")
                }
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.ContentPaste, "Paste Here", {
                pasteFileObject()
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.CreateNewFolder, "Create Folder Here", {
                showMenu.createFolderDialogue.value = true
                showToast(context, "folder created")
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.CreateNewFolder, "Map To Network", {
                //perform a check for if the id and password is configured!
                val resolvedPath = showMenu.filePath.value?:"invalid"
                if(resolvedPath.isNotEmpty()){
                    mapFileObjectToLFTUCServer(
                        fileObjectList = listOf()
                    )
                    showToast(context, "file mapped to server")
                }else{
                    showToast(context, "file not mapped to server")
                }
            }))
        }
//        showMenu.caller.value.contains("File-Cards")-> {//Files
//            contextActionsList.clear()
//            contextActionsList.add(ContextAction(Icons.Rounded.FileCopy, "Copy Files", {
//                if(!FileManagerData.multiSelectFiles.isEmpty()) {
//                    FileManagerData.batchFilesToReplace.clear()
//                    FileManagerData.cutFiles.clear()
//                    FileManagerData.copiedFiles.clear()
//                    FileManagerData.copiedFiles.addAll(FileManagerData.multiSelectFiles)
//                    showToast(context, "multiple files copied")
//                }
//            }))
//            contextActionsList.add(ContextAction(Icons.Rounded.ContentCut, "Cut Files", {
//                if(!FileManagerData.multiSelectFiles.isEmpty()) {
//                    FileManagerData.batchFilesToReplace.clear()
//                    FileManagerData.copiedFiles.clear()
//                    FileManagerData.cutFiles.clear()
//                    FileManagerData.cutFiles.addAll(FileManagerData.multiSelectFiles)
//                    showToast(context, "multiple files cut")
//                }
//            }))
//        }
        showMenu.caller.value.contains("NetworkFile")-> {//Files
            contextActionsList.clear()
            contextActionsList.add(ContextAction(Icons.Rounded.CreateNewFolder, "Download File", {
                downloadFileFromServer(serverAddress = FileManagerData.currentServerAddress.value,
                    path = FileManagerData.currentServerFolder.value +"/"+ FileManagerData.lftuc_FileToRequest.value,

                    onProgress = {progress->
                        FileManagerData.lftuc_DownloadProgress.value = progress.toFloat()/100f
                    },
                    onComplete = {completeMessage ->
                        FileManagerData.lftuc_DownloadCompleteMessage.value = completeMessage
                        FileManagerData.lftuc_downloadFinishTime.value = getCurrentTimeInString()
                        FileManagerData.lftuc_totalDownloadDuration.value = getDuration(FileManagerData.lftuc_downloadStartTime.value, FileManagerData.lftuc_downloadFinishTime.value)
                    }
                )
                FileManagerData.lftuc_isDownloadingFromServer.value=true
                FileManagerData.lftuc_downloadStartTime.value = getCurrentTimeInString()
            }))
        }
        showMenu.caller.value.contains("NetworkFolder")-> {/*no options*/}
        showMenu.caller.value.contains("FileManagerPage")-> {//Files
            contextActionsList.clear()
            contextActionsList.add(ContextAction(Icons.Rounded.FileCopy, "Paste Here", {
                pasteFileObject()
                showToast(context, "multiple files pasted")
            }))
            contextActionsList.add(ContextAction(Icons.Rounded.CreateNewFolder, "Create Folder", {
                showMenu.createFolderDialogue.value = true
                showToast(context, "folder created")
            }))
        }
        showMenu.caller.value.contains("NetworkFileManagerPage")-> {/*empty*/}
    }
    return contextActionsList
}
//Logic that is used frequently or collectively
fun getCurrentTimeInString():String{
    val currentTime = System.currentTimeMillis()
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val formattedTime = formatter.format(Date(currentTime))
    return formattedTime
}

fun getDuration(startTimeStr: String, endTimeStr: String): String {
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    val startTime = formatter.parse(startTimeStr)
    val endTime = formatter.parse(endTimeStr)

    val durationMillis = endTime!!.time - startTime!!.time

    // Handle negative durations (if needed)
    val safeDuration = if (durationMillis < 0) 0 else durationMillis

    return String.format(
        "%02d:%02d:%02d.%03d",
        TimeUnit.MILLISECONDS.toHours(safeDuration),
        TimeUnit.MILLISECONDS.toMinutes(safeDuration) % 60,
        TimeUnit.MILLISECONDS.toSeconds(safeDuration) % 60,
        safeDuration % 1000
    )
}
//Defining UI Items that can be repeatedly used!

//Toast Messages
// Toast Messages 1 (SHORT)
fun showToast(context: Context, message: String, toastLength: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, toastLength).show()
}
//SnackBar
@Composable
fun showSnackBar(){
    val snackbarHostState = remember{ SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch{
            val result = snackbarHostState.showSnackbar(
                message=showSnackBarMenu.message.value,
                actionLabel = showSnackBarMenu.actionLabel.value
            )
            if(result == SnackbarResult.ActionPerformed){
                if(showSnackBarMenu.navSystem.value!=null){
                    showSnackBarMenu.navSystem.value?.navigate("configure-device-details")
                }
            }
            delay(showSnackBarMenu.dur.value)
            showSnackBarMenu.show.value=false
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }
    SnackbarHost(hostState = snackbarHostState)
}
//Alert Dialogues
fun showCustomPrompt(
    context: Context,
    message: String,
    positiveText: String,
    negativeText: String,
    onPositive: (dialog: DialogInterface) -> Unit,
    onNegative: (dialog: DialogInterface) -> Unit
) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(message)
        .setPositiveButton(positiveText) { dialog, _ ->
            onPositive(dialog) // Pass dialog to the callback
        }
        .setNegativeButton(negativeText) { dialog, _ ->
            onNegative(dialog) // Pass dialog to the callback
        }
        .setCancelable(false)

    val alert = builder.create()
    alert.show()
}

//Settings Menu Items
@Composable
fun SidebarMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit = {}
) {
    val currenticon = remember { icon }
    val currenttitle = remember{title}
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color.Transparent)
            .clickable(onClick = { onClick();showMenu.menuVisible.value = false })
            .padding(7.dp)
    ) {
        Icon(
            imageVector = currenticon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = currenttitle,
            fontSize = 14.sp,
            fontFamily = bahnschriftFamily,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis
        )
    }
}

//Page TitleBar
@Composable
fun TitleBar(title:String, navSystem: NavController, Content: @Composable () -> Unit, onGoToHome: ()->Unit = {
    FileManagerData.isNavigatingServer.value = false
    FileManagerData.currentFolder.value = ""
}){//The Title-bar box Saying Settings
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(90.dp)
        .background(
            color = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(16.dp)
        )
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    showMenu.menuVisible.value = false;
                }
            )
        },
        contentAlignment = Alignment.CenterStart
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 38.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ){
            Row(Modifier.padding(0.dp)) { Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                Modifier.clickable(enabled = true, onClick = { navSystem.navigate("home"); onGoToHome() })
            )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text=title,
                    fontSize = 20.sp,
                    fontFamily = bahnschriftFamily,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(Modifier.padding(0.dp), verticalAlignment = Alignment.CenterVertically) {
                Content()
            }
        }
    }
}

@Composable
fun FrigonTechBox(
    modifier: Modifier = Modifier, // Allows further customization
    content: @Composable () -> Unit // Accepts any composable as a child
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.onSecondary)
            .padding(horizontal = 11.dp, vertical = 20.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        showMenu.menuVisible.value = false
                    }
                )
            }
    ) {
        content() // Inserts the given composable content inside the box
    }
}

@Composable
fun FrigonTechRow(modifier:Modifier = Modifier,
                  verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
                  horizontal: Arrangement.Horizontal = Arrangement.Center,
                  content: @Composable () -> Unit
){
    // Hardcoded base modifiers that will always be applied
    val baseModifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { offset ->
                    showMenu.menuVisible.value = false
                }
            )
        }

    Row(
        modifier = baseModifier.then(modifier),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontal
    ) {
        content()
    }
}

@Composable
fun FrigonTechGenButton(modifier:Modifier = Modifier,
                        text:String="Button",
                        enabled:Boolean = true, //when has 'hasCancelFunc' enable turns into a start/cancel button
                        onClick: () -> Unit
){
    val baseModifier = Modifier.background(
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.tertiary
    )

    Button(
        modifier = baseModifier.then(Modifier),
        onClick = {onClick() ;showMenu.menuVisible.value = false},
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            // Make the difference more noticeable
            containerColor = if(enabled)MaterialTheme.colorScheme.tertiary else (Color.Gray.copy(alpha = 0.5f)),
            contentColor = if(enabled)MaterialTheme.colorScheme.primary else (Color.Gray.copy(alpha = 0.5f))
        )
    ){
        Text(
            text = text,
            fontFamily = bahnschriftFamily,
            fontSize = 16.sp
        )
    }

}

@Composable
fun FrigonTechStateButton(modifier:Modifier = Modifier,
                        text:String="Button",
                        onClick: () -> Unit,
                        canCancel: Boolean=false,
                        content: @Composable () -> Unit = {}
){
    val baseModifier = Modifier
        .background(
            shape = RoundedCornerShape(25.dp),
            color = if (canCancel) (Color.Red) else MaterialTheme.colorScheme.tertiary
        )
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { offset ->
                    showMenu.menuVisible.value = false
                }
            )
        }

    Button(
        modifier = baseModifier.then(Modifier),
        onClick = {onClick() ;showMenu.menuVisible.value = false},
        colors = ButtonDefaults.buttonColors(
            // Make the difference more noticeable
            containerColor = if(canCancel) (Color.Red.copy(alpha=0.5f)) else MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ){
        content()
    }

}

@Composable
fun NetworkDeviceCard(deviceName: String, deviceIPv4: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(modifier = Modifier
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.secondary)) {
            //Left Column; Device Icon
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                //choose icon based on devices
                val iconRes = R.drawable.monitor
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "DeviceIcon",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = deviceName,
                    fontSize = 18.sp,
                    fontFamily = bahnschriftFamily,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "IP: ${deviceIPv4}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NetworkHostCard(deviceName: String, deviceIPAddress: String, port: String) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showMenu.menuVisible.value = true; showMenu.caller.value = "NHC"
                            showMenu.currentServer.value = deviceIPAddress
                            showMenu.currentServerName.value = deviceName
                        },
                        onTap = { showMenu.menuVisible.value = false }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                // Left Column; Device Icon
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    // pass the appropriate icon here
                    val iconRes = R.drawable.database
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "DeviceIcon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                //Right Column; Host Details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = deviceName,
                        fontSize = 18.sp,
                        fontFamily = bahnschriftFamily,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "lftuc://${deviceIPAddress}:${port}/",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun FolderCard_ListView(folderName: String, folderPath: String, onClick: () -> Unit, isNetworkFolder: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 2.dp, vertical = 2.dp)
                // Move the pointerInput to directly under the Card
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            // Debug log to verify the long press is detected
                            println("Long press detected on $folderName")
                            showMenu.menuVisible.value = true
                            showMenu.caller.value =
                                "${
                                    folderName.split('/').lastOrNull() ?: folderName
                                }- ${if (isNetworkFolder) "NetworkFolder" else "FolderCard"}"
                            showMenu.folderPath.value = folderPath
                        },
                        onTap = {
                            showMenu.menuVisible.value = false
                            println("Tap detected on $folderPath")
                            onClick()
                        }
                    )
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary) // matching the color pallete
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                // Left Column; Folder Icon
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight()
                        .padding(start = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.folder_icon),
                        contentDescription = "Folder Icon",
                        modifier = Modifier.size(45.dp),
                        // Make sure the tint is applied correctly
                        tint = Color.Unspecified
                    )
                }

                //Right Column; Folder Name
                Row(modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 5.dp, end = 5.dp, top = 21.dp)
                ) {
                    Text(
                        text = folderName,
                        fontSize = 14.sp,
                        fontFamily = bahnschriftFamily,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@DrawableRes
fun fileIcon(extension:String): Int{
    return when(extension){
    // images
        "png" -> R.drawable.extension_png
        "jpg" -> R.drawable.extension_jpg
        "jpeg" -> R.drawable.extension_jpeg
        "gif" -> R.drawable.extension_gif
        "bmp" -> R.drawable.extension_bmp
        "webp" -> R.drawable.extension_webp
        "tiff" -> R.drawable.extension_tiff
        "svg" -> R.drawable.extension_svg

    // text documents
        "txt" -> R.drawable.extension_txt
        "pdf" -> R.drawable.extension_pdf
        "doc" -> R.drawable.extension_doc
        "docx" -> R.drawable.extension_docx
        "xls" -> R.drawable.extension_xls
        "xlsx" -> R.drawable.extension_xlsx
        "ppt" -> R.drawable.extension_ppt
        "pptx" -> R.drawable.extension_pptx
        "rtf" -> R.drawable.extension_rtf
        "odt" -> R.drawable.extension_odt
        "json" -> R.drawable.extension_json

    // audio
        "mp3" -> R.drawable.extension_mp3
        "wav" -> R.drawable.extension_wav
        "ogg" -> R.drawable.extension_ogg
        "flac" -> R.drawable.extension_flac
        "aac" -> R.drawable.extension_aac
        "m4a" -> R.drawable.extension_m4a

    // video
        "mp4" -> R.drawable.extension_mp4
        "mkv" -> R.drawable.extension_mkv
        "avi" -> R.drawable.extension_avi
        "mov" -> R.drawable.extension_mov
        "wmv" -> R.drawable.extension_wmv
        "flv" -> R.drawable.extension_flv
        "webm" -> R.drawable.extension_webm

    // archives
        "zip" -> R.drawable.extension_zip
        "rar" -> R.drawable.extension_rar
        "tar" -> R.drawable.extension_tar
        "7z" -> R.drawable.extension_7z
        "gz" -> R.drawable.extension_gz
        "bz2" -> R.drawable.extension_bz2

    // code
        "java" -> R.drawable.extension_java
        "cpp" -> R.drawable.extension_cpp
        "js" -> R.drawable.extension_js
        "html" -> R.drawable.extension_html
        "css" -> R.drawable.extension_css
        "xml" -> R.drawable.extension_xml
        "py" -> R.drawable.extension_py
        "sh" -> R.drawable.extension_sh
        "kt" -> R.drawable.extension_kt

    // others
        "iso" -> R.drawable.extension_iso
        "exe" -> R.drawable.extension_exe
        "apk" -> R.drawable.extension_apk
        "csv" -> R.drawable.extension_csv
        "md" -> R.drawable.extension_md
        "epub" -> R.drawable.extension_epub

        else->R.drawable.extension_unknown//default fallback
    }
}

@Composable //see folders listed in the page in 'list view'
fun FileCard_ListView(fileName:String, filePath:String, onClick: () -> Unit, isNetworkFile: Boolean = false){
    val extension:String= fileName.split('.').last()
    Box(modifier=Modifier.fillMaxWidth()){
        Card(modifier= Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // Debug log to verify the long press is detected

                        showMenu.menuVisible.value = true
                        showMenu.caller.value =
                            "${fileName}-${if (isNetworkFile) "NetworkFile" else "FileCard"}"
                        println("Long press detected on ${showMenu.caller.value}")
                        showMenu.filePath.value = filePath
                        if (isNetworkFile) {
                            FileManagerData.lftuc_FileToRequest.value = "[FILE]${fileName}"
                        } else {
                            FileManagerData.lftuc_FileToRequest.value = ""
                        }
                    },
                    onTap = {
                        showMenu.menuVisible.value = false;
                        onClick()
                    }
                )
            },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary) // matching the color pallete
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                // Left Column; File Icon
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .padding(start = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id= fileIcon(extension)),
                        contentDescription = null,
                        modifier=Modifier
                            .size(70.dp)
                            .padding(end = 7.dp)
                    )
                }

                //Right Column; File Name
                Row(modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 5.dp, end = 5.dp, top = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text=fileName,
                        fontFamily = bahnschriftFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}