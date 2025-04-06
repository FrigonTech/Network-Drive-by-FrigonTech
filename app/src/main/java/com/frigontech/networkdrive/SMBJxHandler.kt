package com.frigontech.networkdrive

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import java.io.File
import java.util.Properties

// Object to store active SMB sessions
object SessionObjects {
    val sessionRef = mutableStateListOf<CIFSContext>()
}

fun authenticateAndStoreSession(
    server: String,
    username: String,
    password: String,
    domain: String = "WORKGROUP"
): Boolean {
    return try {
        // Create properties for SMB context configuration
        val properties = Properties().apply {
            setProperty("jcifs.smb.client.domain", domain)
            setProperty("jcifs.smb.client.username", username)
            setProperty("jcifs.smb.client.password", password)
        }

        val config = PropertyConfiguration(properties)
        val context: CIFSContext = BaseContext(config).withCredentials(
            NtlmPasswordAuthenticator(domain, username, password)
        )

        // Validate the session by attempting to access a shared resource
        val smbUrl = "smb://$server/" // Change to the appropriate resource URL
        val smbFile = SmbFile(smbUrl, context)
        if (smbFile.exists()) {
            // Store the session in the shared object
            SessionObjects.sessionRef.add(context)
            println("Authentication successful and session stored.")
            true // Authentication successful
        } else {
            println("Failed to connect to the specified resource.")
            false // Resource not found
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Authentication failed due to an error: ${e.message}")
        false // Authentication failed
    }
}

fun mapFolderToNetwork(context: Context, folderPath:String, id:String, password:String, domain:String? = null): DiskShare?{
    try{
        val folder = File(folderPath)
        if(!folder.exists() || !folder.isDirectory){
            println("Error: $folder might not a folder or directory")
            showToast(context, "Folder might not be a directory")
            return null
        }
        val folderName = folder.name

        println("making client and session for '${folderName}' on ${localIPv4AD}:${specifiedPort}")
        println("init SMB Client()")
        val client = SMBClient()
        println("establishing connection")
        val connection: Connection = client.connect("${localIPv4AD}:${specifiedPort}")
        println("Connection successful")
        println("authenticating with id and pass: $id and $password")
        val auth = AuthenticationContext(id, password.toCharArray(), domain)
        val session: Session = connection.authenticate(auth)
        println("auth successful")

        println("Connecting to SMB session with folder '${folderName}")
        //Connect to the shared folder (keeping its origignal name)
        val share = session.connectShare(folderName) as DiskShare

        showToast(context, "Folder '${folderName}' mapped to Network!")
        println("✅ Folder '${folderPath}' is now mapped to network as '${folderName}'")
        return share
    }
    catch(e:Exception){
        println("Error: ${e.message}")
        showToast(context, "Error: ${e.message?.takeLast(12)}")
        return null
        e.printStackTrace()
    }
}

