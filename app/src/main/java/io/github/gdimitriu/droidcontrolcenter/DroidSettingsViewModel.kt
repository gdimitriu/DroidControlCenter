package io.github.gdimitriu.droidcontrolcenter

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Socket
private const val TAG = "DroidSettingsViewModel"
class DroidSettingsViewModel : ViewModel()  {
    var isChanged : Boolean

    var ipAddress : String

    var portValue : String

    fun setAddress(address : String) {
        ipAddress = address
        isChanged = true
    }

    fun setPort(port: String) {
        portValue = port
        isChanged = true
    }

    var socket: Socket?

    init {
        Log.d(TAG, "Initialized the model view")
        ipAddress  = ""
        portValue  = ""
        isChanged = true
        socket = null
    }
}