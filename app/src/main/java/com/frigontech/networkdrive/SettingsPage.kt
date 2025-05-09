package com.frigontech.networkdrive

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.frigontech.networkdrive.ui.theme.ColorManager
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0green
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0warningred
import kotlinx.coroutines.delay

//get android shared preferences to know if the app has been started first time since install
fun isAgreementAcknowledged(context: Context): Boolean {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("AgreementAcknowledged", false)
}

@SuppressLint("UseKtx")
fun setAgreementAcknowledged(context: Context, acknowledged: Boolean) {
    val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    if(!(sharedPreferences.getBoolean("AgreementAcknowledged", false))){
        sharedPreferences.edit().putBoolean("AgreementAcknowledged", acknowledged).apply()
    }
}

@Composable
fun SettingsPage(navSystem: NavController, focusManager: FocusManager) {

    val context = LocalContext.current
    var agreementAcknStatus by remember { mutableStateOf(isAgreementAcknowledged(context)) }
    val provideRootAccessOrNot = remember{mutableStateOf(false)}



    LaunchedEffect(Unit) {
        agreementAcknStatus =isAgreementAcknowledged(context)
        provideRootAccessOrNot.value = retrieveTextData(context, "rootAccess").let { text ->
            if (text.isNullOrBlank()) false else (text == "true")
            //^^^^^^^^^^^ this is a way to directly pass bool value
        }
    }

    Box(modifier = Modifier
        .fillMaxSize().pointerInput(Unit){detectTapGestures {focusManager.clearFocus()}}
    ){
        Column(modifier=Modifier.fillMaxSize()
        ) {
            TitleBar(title="Settings", navSystem=navSystem, {})

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp)),
                verticalArrangement = Arrangement.Top
            ){
                //Content is added by a separate func in order for the content to be able to get scrolled through.
                //all settings are collapsed in a folder next to 'item{}'
                item{ //Lazy Column requires its children to be structured inside 'item {}'
                    Column(modifier=Modifier
                        .fillMaxWidth()
                        .padding(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)){

                        //DISCLAIMER================================================================
                        var isDisclaimerVis by remember { mutableStateOf(false) }
                        var isWarningVis by remember { mutableStateOf(false) }

                        FrigonTechBox {
                            Column {
                                Row(modifier = Modifier
                                    .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ){
                                    Button(colors = ButtonDefaults.buttonColors(
                                    containerColor = if(isDisclaimerVis) ColorManager(
                                        frigontech0green) else MaterialTheme.colorScheme.tertiary),
                                    onClick = {isDisclaimerVis = !isDisclaimerVis ; isWarningVis = false}
                                    )
                                    {
                                        Text(
                                            text = if(isDisclaimerVis) "Hide Disclaimer" else "Read Disclaimer",
                                            fontSize = 16.sp,
                                            fontFamily = bahnschriftFamily,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Button(colors = ButtonDefaults.buttonColors(
                                        containerColor = if(isWarningVis) ColorManager(
                                            frigontech0green) else ColorManager(
                                            frigontech0warningred)
                                    ),
                                        onClick = {isWarningVis = !isWarningVis ; isDisclaimerVis = false}
                                    )
                                    {
                                        Text(
                                            text = if(isWarningVis) "Hide Warning" else "Read Warning",
                                            fontSize = 16.sp,
                                            fontFamily = bahnschriftFamily,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }


                                }


                                if(isDisclaimerVis){
                                    Row(modifier=Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp)
                                    ){
                                        Text(
                                            text = "This application requires access to your device’s location and Wi-Fi state " +
                                                    "permissions in order to retrieve technical information about your device’s WLAN " +
                                                    "network interface, including the ability to access local IP addresses. " +
                                                    "While the request for location access may raise concerns, " +
                                                    "please be advised that this requirement is explicitly defined by the Android " +
                                                    "operating system for obtaining Wi-Fi-related data. In this distribution of the " +
                                                    "application, the device’s local IPv6 address is utilized to instantiate a local " +
                                                    "server that facilitates file sharing between devices on the same Local Area Network " +
                                                    "(LAN). The server operates on a port number specified by the user within the “Configure " +
                                                    "Details” section of the application. For device discovery across the LAN, the application " +
                                                    "employs multicast communication on the standard group address 239.255.255.250. This " +
                                                    "method restricts visibility to devices connected to the same Wi-Fi network. However, " +
                                                    "please note that any device present on the same network, including unauthorized or " +
                                                    "untrusted ones, may receive these multicast packets. A user with malicious intent " +
                                                    "and access to the same subnet may be able to scan for active servers and identify " +
                                                    "associated IP addresses. It is further acknowledged that the LFTUC server implementation " +
                                                    "does not incorporate encryption or security mechanisms. This design decision is intentional " +
                                                    "to reduce computational overhead and to prioritize transfer speed and operational " +
                                                    "efficiency. Users are hereby notified that the use of this application entails " +
                                                    "inherent security trade-offs, and it is recommended that file transfers be conducted " +
                                                    "only on trusted and secure local networks.",
                                            fontSize = 14.sp,
                                            fontFamily = bahnschriftFamily,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                if(isWarningVis){
                                    Row(modifier=Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp)
                                    ){
                                        Text(
                                            text = "You are strictly advised not to Press the 'Start Service' button when on Public Wi-Fi networks" +
                                                    "like when connected to an network in any Cafe, Airport, Hotel, Railway Stations," +
                                                    " etc. Any untrusted Wi-Fi Networks that's open to everyone in public or at someone's house" +
                                                    " who might have wrong intentions. Your IP Address will be openly hosted to your network so " +
                                                    "in public places attackers can get your IP Address and break into your device and get low-level" +
                                                    "(very deep) access. Having Low-Level access can also allow them to mirror your screen and have access" +
                                                    "of all your apps, files, browsing history, credit card information,etc. remotely very easily.",
                                            fontSize = 14.sp,
                                            fontFamily = bahnschriftFamily,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(3.dp)){
                                        val uriHandler = LocalUriHandler.current
                                        Text(
                                            text = "Learn More",
                                            fontSize = 14.sp,
                                            fontFamily = bahnschriftFamily,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            textDecoration = TextDecoration.Underline,
                                            modifier = Modifier.clickable {
                                                uriHandler.openUri("https://sites.google.com/view/frigontech/network-drive-documentation-warning")
                                            }
                                        )
                                    }
                                }

                                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Start) {
                                    Checkbox(
                                        checked = agreementAcknStatus,
                                        enabled = !agreementAcknStatus,
                                        onCheckedChange = {
                                            val newState = !agreementAcknStatus
                                            agreementAcknStatus = newState
                                            setAgreementAcknowledged(context, newState)
                                            showToast(context, "Please, configure...Forwarding to Configure Page")
                                            navSystem.navigate("configure-device-details")
                                        }
                                    )
                                    Spacer(modifier=Modifier.width(5.dp))
                                    Text(
                                        text = "I hereby confirm that I have read and fully understood the contents of the 'Disclaimer' " +
                                                "and 'Warning' sections, and I expressly acknowledge and accept all associated risks " +
                                                "and responsibilities as outlined therein.",
                                        fontSize = 14.sp,
                                        fontFamily = bahnschriftFamily,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        FrigonTechBox {
                            FrigonTechRow {
                                Checkbox(
                                    checked = provideRootAccessOrNot.value,
                                    onCheckedChange = {
                                        val newState = !provideRootAccessOrNot.value
                                        provideRootAccessOrNot.value = newState
                                        saveTextData(context, "rootAccess", "${provideRootAccessOrNot.value}")
                                        showToast(context, "Root Dir Acces will be provided to clients")
                                    }
                                )
                                Spacer(modifier=Modifier.width(5.dp))
                                Text(
                                    text = "Provide full device storage access to clients.",
                                    fontSize = 14.sp,
                                    fontFamily = bahnschriftFamily,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        //Add more components here
                    }
                }
            }
        }
    }
}