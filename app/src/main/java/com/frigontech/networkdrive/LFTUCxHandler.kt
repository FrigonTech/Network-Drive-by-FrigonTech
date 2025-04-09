package com.frigontech.networkdrive

import android.content.Context
import com.frigontech.lftuc_1.lftuc_main_lib.*

fun startServer(context: Context, deviceName:String,){
    startLFTUCServer(context)
    startLFTUCMulticastEcho(1, deviceName, lftuc_getLinkLocalIPv6Address(), specifiedPort, 1)
}

fun stopServer(){
    stopServer()
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

