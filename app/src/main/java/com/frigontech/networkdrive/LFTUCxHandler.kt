package com.frigontech.networkdrive

import android.content.Context
import com.frigontech.lftuc_1.lftuc_main_lib.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

fun startServer(context: Context, deviceName:String,){
    startLFTUCServer(context)
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

fun mapFileObjectToLFTUCServer(fileObjectList: List<String>){
    //WARNING: send absolute file path strings in this list
    for(file in fileObjectList){
        if(moveFileObjectToLFTUCSharedDir(file)){
            lftuc_receivedMessages.add("file/folder$file moved")
        }else{
            if(lftuc_needToReplaceObject){
                //ask for replacement
                continue
            }else{
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
    })
}
