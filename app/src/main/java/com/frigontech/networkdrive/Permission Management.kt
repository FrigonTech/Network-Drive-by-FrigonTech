package com.frigontech.networkdrive

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//Permissions along with Request Code; !?! Don't change the sequence its hardcoded
val permissions:List<String> = mutableListOf("android.permission.ACCESS_WIFI_STATE", "android.permission.CHANGE_WIFI_STATE",
    "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CHANGE_WIFI_MULTICAST_STATE", "android.permission.NEARBY_WIFI_DEVICES",
    "android.permission.READ_EXTERNAL_STORAGE")

//check for all permissions; used in 'requestPermissions'
fun checkPermissions(context: Context): MutableList<Int> {
    val requestPermissionIndex = mutableListOf<Int>()
    for (i in permissions.indices){
        if(ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_GRANTED){
            continue
        }else{
            requestPermissionIndex.add(i)
        }
    }

    return requestPermissionIndex
}

fun areAllPermissionsGranted(context: Context): Boolean{
    var foundAnyUngranted = mutableStateOf(false)
    for (i in permissions.indices){
        if(ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_GRANTED){
            foundAnyUngranted.value = false
        }else{
            foundAnyUngranted.value = true
        }
    }
    return foundAnyUngranted.value
}

fun checkSpecificPermission(context: Context, requestPermissionIndex:Int): Boolean {
    return ContextCompat.checkSelfPermission(context, permissions[requestPermissionIndex]) == PackageManager.PERMISSION_GRANTED
}

//request Permission is called in Launched Effect (Unit) of the application main page so that all the permissions are cross-checked along with their request codes
//at once
fun requestPermissions(context: Context) {
    val permissionIndexToRequest: MutableList<Int> = checkPermissions(context)
    val permissionsToRequest = permissionIndexToRequest.map {permissions[it]}
    if (context is Activity) {
        ActivityCompat.requestPermissions(
            context,
            permissionsToRequest.toTypedArray(),
            1
        )
    }
}

//check if specific permissions are granted before doing an important process
fun requestSpecificPermission(context: Context, requestPermissionIndex:Int) {
    if (context is Activity) {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(permissions[requestPermissionIndex]),
            requestPermissionIndex
        )
    }
}