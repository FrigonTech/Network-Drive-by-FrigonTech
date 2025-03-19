package com.frigontech.networkdrive

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.rounded.Adb
import androidx.compose.material.icons.rounded.LibraryAddCheck
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.RemoveDone
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay

//vars
object showMenu{
    var menuVisible= mutableStateOf(false)
    var caller= mutableStateOf("")
}

data class ContextAction(
    val icon: ImageVector,
    val actionName:String,//name of context action
    val actionClick: () -> Unit = {}//event that'll happen on clicking that context action
)

fun GetContextActions():List<ContextAction>{//setup actions based on context
    val contextActionsList = mutableListOf<ContextAction>()
    when(showMenu.caller.value){
        "NHC"-> {//Network Host Card
            contextActionsList.add(ContextAction(Icons.Rounded.Adb, "Request Direct Access", {})) // Uses a pop-up on the other device // Still experimental!
            contextActionsList.add(ContextAction(Icons.Rounded.Password,"Join Using Credentials", {}))
        }
        "NDC"-> {//Network Device Card (devices that are trying to connect or connected to host)
            contextActionsList.add(ContextAction(Icons.Rounded.LibraryAddCheck, "Allow Join Request", {}))
            contextActionsList.add(ContextAction(Icons.Rounded.RemoveDone, "Dismiss Join Request", {}))
        }
    }
    return contextActionsList
}

//Defining UI Items that can be repeatedly used!

//Toast Messages
// Toast Messages 1 (SHORT)
fun showToast(context: Context, message: String, toastLength: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, toastLength).show()
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color.Transparent)
            .clickable(onClick = {onClick() ;showMenu.menuVisible.value = false})
            .padding(7.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
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
fun TitleBar(title:String, navSystem: NavController){
    Box(modifier = Modifier                                                                 //The Title-bar box Saying Settings
        .fillMaxWidth()
        .height(90.dp)
        .background(
            color = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(16.dp)
        )
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { offset ->
                    showMenu.menuVisible.value = false
                }
            )
        },
        contentAlignment = Alignment.CenterStart
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 38.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                Modifier.clickable(enabled = true, onClick = { navSystem.navigate("home") })
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text=title,
                fontSize = 20.sp,
                fontFamily = bahnschriftFamily,
                color = MaterialTheme.colorScheme.primary
            )
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
                        cancelState: Boolean=false,
                        content: @Composable () -> Unit = {}
){
    val baseModifier = Modifier
        .background(
            shape = RoundedCornerShape(25.dp),
            color = if (cancelState) (Color.Red) else MaterialTheme.colorScheme.tertiary
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
            containerColor = if(cancelState) (Color.Red.copy(alpha=0.5f)) else MaterialTheme.colorScheme.tertiary,
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
fun NetworkHostCard(deviceName: String, deviceIPv4: String, port: String) {

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
                        onLongPress = { showMenu.menuVisible.value = true ; showMenu.caller.value="NHC" },
                        onTap = {showMenu.menuVisible.value = false}
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
                        text = "Host Address and Data: ${deviceIPv4}:${port}/data.json",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}