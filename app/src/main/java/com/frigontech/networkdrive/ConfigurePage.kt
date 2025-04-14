package com.frigontech.networkdrive

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

var displayName:String = ""
var specifiedPort:Int = 0
var sMBJ_ID = ""
var sMBJ_PASS = ""

@Composable
fun ConfigurePage(navSystem: NavController, focusManger: FocusManager) {
    //context
    val context = LocalContext.current

    // Using MutableState directly
    val deviceName = remember { mutableStateOf("") }
    val myPort = remember {mutableIntStateOf(8080)}
    val myID = remember {mutableStateOf("")}
    val myPASS = remember {mutableStateOf("")}

    //Things to do when UI first Composed!
    LaunchedEffect(Unit) {
        deviceName.value = if(displayName!="")displayName else (if(retrieveTextData(context, "device-name").isNotBlank()) retrieveTextData(context, "device-name") else localIPv4AD) //retrieve or initialize the name of the device!
        myPort.intValue = if(specifiedPort!=0)specifiedPort else (retrieveTextData(context, "port").toIntOrNull()?: 8080)
        myID.value = if(sMBJ_ID!="")sMBJ_ID else (if(retrieveTextData(context, "SMBJ1").isNotBlank()) retrieveTextData(context, "SMBJ1") else deviceName.value)
        myPASS.value = if(sMBJ_PASS!="")sMBJ_PASS else (if(retrieveTextData(context, "SMBJ2").isNotBlank()) retrieveTextData(context, "SMBJ2") else (localIPv4AD + "45ctuiy1b39f3"))
    }

    LaunchedEffect(deviceName.value) {
        displayName = deviceName.value
    }
    LaunchedEffect(myPort.intValue) {
        specifiedPort = myPort.intValue
    }
    LaunchedEffect(myID.value) {
        sMBJ_ID = myID.value
    }
    LaunchedEffect(myPASS.value) {
        sMBJ_PASS = myPASS.value
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) { detectTapGestures { focusManger.clearFocus() } }) {
        TitleBar(title = "Configure Details", navSystem = navSystem, {})

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            item {
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    OutlinedTextField(
                        modifier=Modifier.fillMaxWidth(),
                        value = deviceName.value,  // Access the value property here
                        onValueChange = {
                            if(it.last()!= ' '){
                                if(deviceName.value != "MyDevice") {
                                    deviceName.value = it
                                }else{
                                    deviceName.value = ""
                                }
                            }
                        },
                        label = { Text("Enter a Device Name", fontFamily = bahnschriftFamily, fontSize = 14.sp) }
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    FrigonTechGenButton(text = "Save Device Name", onClick = { focusManger.clearFocus(); saveTextData(context, "device-name", deviceName.value, "Device Name Saved") })
                }
                HorizontalDivider()
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    OutlinedTextField(
                        modifier=Modifier.fillMaxWidth(),
                        value = if(myPort.intValue != 0) myPort.intValue.toString() else "",  // Access the value property here
                        onValueChange = { newValue ->
                            if(newValue.isEmpty()){
                                myPort.intValue = 0
                            }else{
                                newValue.toIntOrNull()?.let { parsedvalue->
                                    myPort.intValue = parsedvalue
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Specify port to host and look for device(s)/host(s)", fontFamily = bahnschriftFamily, fontSize = 14.sp) }
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    FrigonTechGenButton(text = "Save Port", onClick = { focusManger.clearFocus(); saveTextData(context, "port", myPort.intValue.toString(), "New Port Specified") })
                }
                HorizontalDivider()
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    OutlinedTextField(
                        modifier=Modifier.fillMaxWidth(),
                        value = myID.value,  // Access the value property here
                        onValueChange = { myID.value = it },
                        label = { Text("Enter Authentication ID", fontFamily = bahnschriftFamily, fontSize = 14.sp) }
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    FrigonTechGenButton(text = "Save ID", onClick = { focusManger.clearFocus(); saveTextData(context, "SMBJ1", myID.value, "Auth ID Saved") })
                }
                HorizontalDivider()
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    OutlinedTextField(
                        modifier=Modifier.fillMaxWidth(),
                        value = myPASS.value,  // Access the value property here
                        onValueChange = { myPASS.value = it },
                        label = { Text("Enter Authentication Password", fontFamily = bahnschriftFamily, fontSize = 14.sp) }
                    )
                }
                FrigonTechRow(verticalAlignment = Alignment.CenterVertically, horizontal = Arrangement.Center) {
                    FrigonTechGenButton(text = "Save Password", onClick = { focusManger.clearFocus(); saveTextData(context, "SMBJ2", myPASS.value, "Auth Password Saved") })
                }
            }
        }
    }
}