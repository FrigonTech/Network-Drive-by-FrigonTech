package com.frigontech.networkdrive

import android.content.Context

// Function to save text data
fun saveTextData(context: Context, key: String, value: String, toastMessage:String="") {
    val sharedPreferences = context.getSharedPreferences("AppData", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(key, value)
    editor.apply() // Asynchronous saving (use commit() for synchronous)
    showToast(context, toastMessage)
}

// Function to retrieve text data
fun retrieveTextData(context: Context, key: String, defaultValue: String = ""): String {
    val sharedPreferences = context.getSharedPreferences("AppData", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, defaultValue) ?: defaultValue
}