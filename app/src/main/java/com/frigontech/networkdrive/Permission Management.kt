package com.frigontech.networkdrive

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//Permissions along with Request Code; !?! Don't change the sequence its hardcoded
val permissions:List<String> = mutableListOf("android.permission.ACCESS_WIFI_STATE", "android.permission.CHANGE_WIFI_STATE",
    "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CHANGE_WIFI_MULTICAST_STATE", "android.permission.NEARBY_WIFI_DEVICES",
    "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")

//check for all permissions; used in 'requestPermissions'
fun checkPermissions(context: Context): MutableList<Int> {
    val requestCodes = mutableListOf<Int>()
    for (i in permissions.indices){
        if(ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_GRANTED){
            continue
        }else{
            requestCodes.add(i)
        }
    }

    return requestCodes
}

fun checkSpecificPermission(context: Context, requestCode:Int): Boolean {
    return ContextCompat.checkSelfPermission(context, permissions[requestCode]) == PackageManager.PERMISSION_GRANTED
}

//request Permission is called in Launched Effect (Unit) of the application main page so that all the permissions are cross-checked along with their request codes
//at once
fun requestPermissions(context: Context) {
    val permissionCodesToRequest: MutableList<Int> = checkPermissions(context)
    if (context is Activity) {
        for(i in permissionCodesToRequest){
            ActivityCompat.requestPermissions(
                context,
                arrayOf(permissions[i]),
                i
            )
        }
    }
}

//check if specific permissions are granted before doing an important process
fun requestSpecificPermission(context: Context, requestCode:Int) {
    if (context is Activity) {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(permissions[requestCode]),
            requestCode
        )
    }
}