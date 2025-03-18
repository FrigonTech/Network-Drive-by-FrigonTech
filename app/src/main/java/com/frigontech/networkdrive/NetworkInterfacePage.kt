package com.frigontech.networkdrive


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.frigontech.networkdrive.ui.theme.ColorManager
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0green
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0terminal
import java.net.NetworkInterface
import java.util.Collections

var localIPv4AD: String = getLocalIpAddress()

@Composable
fun NetworkInterfacePage(navSystem: NavController) {

    //Making a Terminal like Vertical Text containing structure
    Column(modifier = Modifier.fillMaxSize()) {
        TitleBar(title = "Network Interface", navSystem = navSystem)
        //Code Style Body
        // Terminal-like black box (fills remaining space)
        Box(
            modifier = Modifier
                .weight(1f) // This makes the Box take all remaining space
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // Fill the parent Box
                    .background(color = ColorManager(frigontech0terminal).copy(alpha = 0.5f), shape = RoundedCornerShape(5.dp))
            ) {
                // Terminal content can go here
                // For example, showing the IP address

                Text(
                    text = "⚠️Local IPv4 Address: $localIPv4AD",
                    color = ColorManager(frigontech0green),
                    modifier = Modifier.padding(16.dp)
                )
            }

        }
    }
}

// Function to get the device's IP address
fun getLocalIpAddress(): String {
    try {
        // Get all network interfaces
        val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())

        for (networkInterface in networkInterfaces) {
            // Skip loopback interfaces and interfaces that are down
            if (networkInterface.isLoopback || !networkInterface.isUp) {
                continue
            }

            // Check each address in the interface
            val addresses = Collections.list(networkInterface.inetAddresses)
            for (address in addresses) {
                // Skip loopback addresses and IPv6 addresses
                if (address.isLoopbackAddress || address.hostAddress.contains(":")) {
                    continue
                }

                // Return the first valid IPv4 address
                return address.hostAddress
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return "null"
}