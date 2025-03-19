package com.frigontech.networkdrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.frigontech.networkdrive.ui.theme.NetworkDriveExplorerTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import androidx.navigation.compose.composable
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.frigontech.networkdrive.ui.theme.NetworkDriveExplorerTheme
import android.widget.Toast
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalFocusManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initializing important vars (saved in phome memory)
        displayName = retrieveTextData(this, "device-name")?: localIPv4AD //retrieve or initialize the name of the device!
        specifiedPort = retrieveTextData(this, "port").toIntOrNull()?: 8080
        sMBJ_ID = retrieveTextData(this, "SMBJ1")?: displayName
        sMBJ_PASS = retrieveTextData(this, "SMBJ2") ?: (localIPv4AD + "45ctuiy1b39f3")

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
                        composable("settings") { SettingsPage(navSystem) }
                        composable("network-interface") { NetworkInterfacePage(navSystem) }
                        composable("configure-device-details") { ConfigurePage(navSystem, focusManager) }
                        composable("search-host-page") { SearchHostPage(navSystem, focusManager) }
                        composable("file-manager") { FileManagerPage(navSystem, focusManager)}
                    }
                }
            }
        }
    }
}