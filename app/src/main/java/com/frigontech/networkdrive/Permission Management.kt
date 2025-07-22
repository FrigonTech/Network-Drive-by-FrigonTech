package com.frigontech.networkdrive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts

//Permissions along with Request Code; !?! Don't change the sequence its hardcoded
val permissions:List<String> = mutableListOf("android.permission.ACCESS_WIFI_STATE", "android.permission.CHANGE_WIFI_STATE",
    "android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION", "android.permission.CHANGE_WIFI_MULTICAST_STATE", "android.permission.NEARBY_WIFI_DEVICES",
    "android.permission.READ_EXTERNAL_STORAGE", "android.permission.MANAGE_EXTERNAL_STORAGE", "")

///0 - access wifi state
///1 - change wifi state
///2 - internet
///3 - access network state
///4 - access fine location
///5 - access coarse location
///6 - change wifi multicast state
///7 - nearby wifi devices
///8 - read external storage
///9 - manage external storage
//10 - reserved for SAF persistent permission

//check for all permissions; used in 'requestPermissions'
fun checkPermissions(context: Context): MutableList<Int> {
    val requestPermissionIndex = mutableListOf<Int>()
    for (i in permissions.indices){
        if(i!=10){
            if(ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_GRANTED){
                continue
            }else{
                requestPermissionIndex.add(i)
            }
        }
    }

    return requestPermissionIndex
}

fun areAllPermissionsGranted(context: Context): Boolean{
    var foundAnyUngranted = mutableStateOf(false)
    for (i in permissions.indices){
        if(i != 10){ // list all the reserved or null/empty request indices here
            if(ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_GRANTED){
                foundAnyUngranted.value = false
            }else{
                foundAnyUngranted.value = true
            }
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

fun requestSpecificPermission(context: Context, requestPermissionIndex: Int) {
    try {
        if (requestPermissionIndex == 9) {
            // Special case for managing all files access permission (Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                val activity = context as? Activity
                if (activity != null) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:" + activity.packageName)
                        activity.startActivityForResult(intent, requestPermissionIndex)
                    } catch (e: Exception) {
                        // If there's an issue with the intent, fall back to general settings
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        activity.startActivityForResult(intent, requestPermissionIndex)
                    }
                }
            }
        } else {
            // Check if the index is within the bounds of the permissions array
            if (requestPermissionIndex < 0 || requestPermissionIndex >= permissions.size) {
                return
            }

            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(permissions[requestPermissionIndex]),
                    requestPermissionIndex
                )
            } else {
                Log.e("PermissionRequest", "Context is not an Activity instance")
            }
        }
    } catch (e: Exception) {
        Log.e("PermissionRequest", "Exception during permission request: ${e.message}")
        e.printStackTrace()
    }
}