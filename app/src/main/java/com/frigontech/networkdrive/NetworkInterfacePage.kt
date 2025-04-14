@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.frigontech.networkdrive

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.frigontech.lftuc_1.lftuc_main_lib
import com.frigontech.lftuc_1.lftuc_main_lib.*
import com.frigontech.networkdrive.ui.theme.ColorManager
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0green
import com.frigontech.networkdrive.ui.theme.Colors.frigontech0terminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var localIPv4AD: String = lftuc_getLocalIpv4Address()

@Composable
fun NetworkInterfacePage(navSystem: NavController, focusManager: FocusManager) {
    val messages = remember { mutableStateListOf<String>() }  // State-backed list

    LaunchedEffect(Unit) {
        // Add initial static messages
        messages.add("--Tester Started Here--")
        messages.add("⚠️local ipv4 - $localIPv4AD")
        messages.add("⚠️link-local ipv6 address - ${lftuc_getLinkLocalIPv6Address()}")

        while (true) {
            // Get new messages from the module
            val newMessages = lftuc_getReceivedMessages()

            // Add only messages that aren't already in the list
            val updatedMessages = newMessages.filterNot { it in messages }

            if (updatedMessages.isNotEmpty()) {
                messages.addAll(updatedMessages)
            }

            delay(500) // Poll every 500ms
        }
    }

    //Making a Terminal like Vertical Text containing structure
    Column(modifier = Modifier.fillMaxSize().pointerInput(Unit){detectTapGestures {focusManager.clearFocus()}}) {
        TitleBar(title = "Logs", navSystem = navSystem, {})
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

                LazyColumn {
                    items(messages) { message ->  // Pass the state list directly
                        Text(
                            text = message,
                            color = ColorManager(frigontech0green),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}