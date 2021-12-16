package io.github.gdimitriu.droidcontrolcenter

import android.util.Log
import androidx.lifecycle.ViewModel
import java.net.Socket

private const val TAG = "DroidSettingsViewModel"
class DroidSettingsViewModel : ViewModel()  {
    var isChanged : Boolean

    var ipAddress : String

    fun setAddress(address : String) {
        ipAddress = address
        isChanged = true
    }

    var portValue : String

    fun setPort(port: String) {
        portValue = port
        isChanged = true
    }

    var socket: Socket?

    var connectionType : ConnectionType
    init {
        Log.d(TAG, "Initialized the model view")
        ipAddress  = ""
        portValue  = ""
        isChanged = true
        socket = null
        connectionType = ConnectionType.NONE
    }
    override fun onCleared() {
        Log.d(TAG,"Clearing and close the socket.")
        super.onCleared()
        socket?.close()
    }
}