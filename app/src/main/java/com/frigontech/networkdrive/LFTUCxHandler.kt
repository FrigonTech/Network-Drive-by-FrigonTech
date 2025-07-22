package com.frigontech.networkdrive

import android.content.Context
import com.frigontech.lftuc_1.lftuc_main_lib.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun startServer(context: Context, deviceName:String,){
    val rootAccess = retrieveTextData(context, "rootAccess").let { text->
        if(text.isNotEmpty()) false else (text=="true")
    }
    startLFTUCServer(context, rootAccess)
    startLFTUCMulticastEcho(1, deviceName, lftuc_getLinkLocalIPv6Address(), specifiedPort, 1)
}

fun stopServer(){
    stopLFTUCServer()
    stopLFTUCMulticastEcho()
}

fun startScanningForServers(context:Context, multicastGroup:String="239.255.255.250", port:Int=8080){
    startLFTUCMulticastListener(context, multicastGroup, port)
}

fun stopScanningForServers(){
    stopLFTUCMulticastListener()
}

fun mapFileObjectToLFTUCServer(fileObjectList: List<String>, replaceFiles:Boolean = false){
    //WARNING: send absolute file path strings in this list
    for(file in fileObjectList){
        if(moveFileObjectToLFTUCSharedDir(file, replaceFiles)){
            lftuc_receivedMessages.add("file/folder$file moved")
            FileManagerData.refreshExtFileManager.value=true
        }else{
            if (lftuc_getNeedToReplaceObject()) {
                val currentFileIndex = fileObjectList.indexOf(file)
                val lastIndex = fileObjectList.size
                FileManagerData.filesLeftWhileMappingToLFTUCServer.clear()
                FileManagerData.filesLeftWhileMappingToLFTUCServer.addAll(fileObjectList.subList(currentFileIndex, lastIndex))
                FileManagerData.isBatchCopy.value = false
                showMenu.replaceMenu.value=true
            } else {
                lftuc_receivedMessages.add("unknown problem while moving file to shared dir.")
            }
        }
    }
}

fun requestFilesInServerDirectory(
    serverAddress: String,
    path: String = "",
    onSuccess: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    LFTUCRequestSharedFolder(serverAddress, specifiedPort, path, object : LFTUCFolderCallback {
        override fun onResult(files: List<String>) {
            onSuccess(files)
        }

        override fun onError(errorMessage: String) {
            onError(errorMessage)
        }

        override fun onProgress(p0: Int) {}

        override fun onGotFileSize(p0: String?) {}

        override fun onDownloadComplete(p0: String?) {}
    })
}

fun downloadFileFromServer(
    serverAddress: String,
    path: String = "",
    onProgress: (Int) -> Unit,
    onComplete: (String) -> Unit
) {
    if(path.contains("[FILE]")){
        showMenu.downloadFileDialogue.value = true
        LFTUCRequestSharedFolder(serverAddress, specifiedPort, path, object : LFTUCFolderCallback {
            override fun onResult(files: List<String>) {}

            override fun onError(errorMessage: String) {}

            override fun onProgress(downloadProgress: Int) {
                onProgress(downloadProgress)
            }

            override fun onGotFileSize(fileSize: String) {
                FileManagerData.lftuc_RequestedFileSize.value = fileSize
            }

            override fun onDownloadComplete(downloadSuccessMessage: String) {
                onComplete(downloadSuccessMessage)
            }
        })
    }else{
        println("can't perform this task")
    }
}

fun cancelLFTUCFileDownload(){
    cancelFileDownload()
}
