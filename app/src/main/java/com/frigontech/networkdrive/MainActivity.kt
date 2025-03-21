package com.frigontech.networkdrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.frigontech.networkdrive.ui.theme.NetworkDriveExplorerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContent {
            NetworkDriveExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    //Navigation Controller
                    val navSystem = rememberNavController()
                    val focusManager = LocalFocusManager.current

                    NavHost(navSystem, startDestination = "home"){
                        composable("home") { ExplorePage(navSystem) }
                        composable("settings") { SettingsPage(navSystem, focusManager) }
                        composable("network-interface") { NetworkInterfacePage(navSystem, focusManager) }
                        composable("configure-device-details") { ConfigurePage(navSystem, focusManager) }
                        composable("search-host-page") { SearchHostPage(navSystem, focusManager) }
                        composable("file-manager") { FileManagerPage(navSystem, focusManager)}
                    }
                }
            }
        }
    }
}