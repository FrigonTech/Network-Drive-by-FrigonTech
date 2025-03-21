package com.frigontech.networkdrive

import android.content.Context
import android.os.Looper
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


//device json class
data class deviceData(
    val deviceName: String,
    val deviceIPv4: String
)

//==================================================================================================Starting the server

private var server: ApplicationEngine? = null
// File path stored in the server instance
private var jsonFilePath: String? = null

fun entryPointJSON(context: Context, deviceName: String, ipv4: String) {
    // Get the file path using the context
    val file = File(context.filesDir, "data.json")
    // Store the path for later use
    jsonFilePath = file.absolutePath

    println("DEBUG: JSON File Path -> ${file.absolutePath}")

    // Create parent directories if needed
    file.parentFile?.mkdirs()

    // Create a new json object
    val jsonObject = JSONObject()
    jsonObject.put("name", deviceName)
    jsonObject.put("ipv4", ipv4)

    // Write to the file
    file.writeText(jsonObject.toString(4))
    println("DEBUG: JSON File Content -> ${file.readText()}")
}

fun startServer(context: Context) {
    var loadPort = retrieveTextData(context, "port").toIntOrNull()?: 8080
    if (server != null) {
        println("Server is already running!")
        return
    }

    //val deviceName = android.os.Build.MODEL ?: "Unknown"
    val localIPv4 = getLocalIpAddress()

    entryPointJSON(context, "Admin-$displayName", localIPv4)

    server = embeddedServer(CIO, port = loadPort, host = "0.0.0.0") {
        install(ContentNegotiation) {
            jackson()
        }
        routing {
            get("/data.json") {
                jsonFilePath?.let { path ->
                    val file = File(path)
                    if (file.exists() && file.length() > 0) {
                        call.respondText(file.readText(), ContentType.Application.Json)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "File not found or empty")
                    }
                } ?: call.respond(HttpStatusCode.InternalServerError, "No file path set")
            }
        }
    }.start(wait = false)

    println("Server running on: http://$localIPv4:$loadPort/data.json")
    ServerState.serverLive.value = true
    ServerState.serverAddress.value = "http://$localIPv4:$loadPort/data.json"
}

fun stopServer() {
    server?.stop(1000, 1000)
    server = null
    ServerState.serverLive.value = false
    println("Server stopped.")
}


//==================================================================================================Syncing Device List from the Json at localhost

// This will hold your persistent list of devices
val deviceList = mutableListOf<deviceData>()
val hostList = mutableListOf<deviceData>()

// Update fetchAndSyncDeviceList to use a callback
fun fetchAndSyncDeviceList(ipv4: String, port: Int, isHosting:Boolean=true, onComplete: (Boolean) -> Unit = {}) {
    hostList.clear()
    val url = "http://$ipv4:$port/data.json"
    println("Attempting to connect to: $url")

    // Use a coroutine or thread for network operations
    Thread {
        var success = false
        try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            try {
                val responseCode = (connection as? HttpURLConnection)?.responseCode
                println("Response code: $responseCode")

                if (responseCode == 200) {
                    val jsonString = connection.getInputStream().bufferedReader().use { it.readText() }
                    println("Successfully retrieved JSON: $jsonString")

                    // Parse the JSON to get a device
                    val jsonObject = JSONObject(jsonString)
                    if (jsonObject.has("name") && jsonObject.has("ipv4")) {
                        val deviceName = jsonObject.getString("name")
                        val deviceIPv4 = jsonObject.getString("ipv4")

                        // Create a new device from the parsed data
                        val newDevice = deviceData(deviceName, deviceIPv4)

                        if(isHosting){
                            // Add it to your list
                            synchronized(deviceList) {
                                val exists = deviceList.any { it.deviceIPv4 == newDevice.deviceIPv4 }
                                if (!exists) {
                                    deviceList.add(newDevice)
                                    println("✅ ADDED: ${newDevice.deviceName} (${newDevice.deviceIPv4})")
                                }
                            }
                        }else{
                            // Add it to your list
                            synchronized(hostList) {
                                val exists = hostList.any { it.deviceIPv4 == newDevice.deviceIPv4 }
                                if (!exists) {
                                    hostList.add(newDevice)
                                    println("✅ ADDED: ${newDevice.deviceName} (${newDevice.deviceIPv4})")
                                }
                            }
                        }
                        success = true
                    } else {
                        println("JSON doesn't contain required fields 'name' and 'ipv4'")
                    }
                }
            } catch (e: Exception) {
                println("Connection error: ${e.javaClass.simpleName} - ${e.message}")
                e.printStackTrace()
            }
        } catch (e: Exception) {
            println("Network error: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
        }

        // Call the callback on the main thread with the result
        android.os.Handler(Looper.getMainLooper()).post {
            onComplete(success)
        }
    }.start()
}

// Function to compare and sync the device list
/*private fun syncDeviceList(newDeviceList: List<deviceData>) {
    // Remove devices that no longer exist in JSON
    deviceList.removeAll { existingDevice ->
        newDeviceList.none { newDevice -> newDevice.deviceIPv4 == existingDevice.deviceIPv4 }
    }

    // Now add new devices
    newDeviceList.forEach { newDevice ->
        val exists = deviceList.any { it.deviceIPv4 == newDevice.deviceIPv4 }
        println("💻 Checking: ${newDevice.deviceName} (${newDevice.deviceIPv4}) - Exists? $exists")

        if (!exists) {
            deviceList.add(newDevice)
            println("✅ ADDED: ${newDevice.deviceName} (${newDevice.deviceIPv4})")
        }
    }

    println("Updated device list: $deviceList")
}*/

// Function to get the current device list
fun getDevicesList(isHosting:Boolean=true): List<deviceData> {
    return(if(isHosting){  deviceList }else{ hostList})
}