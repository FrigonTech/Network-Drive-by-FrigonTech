package com.frigontech.networkdrive

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import java.io.File

object SMBJSession{
    var session: MutableState<Session?> =  mutableStateOf<Session?>(null)
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

