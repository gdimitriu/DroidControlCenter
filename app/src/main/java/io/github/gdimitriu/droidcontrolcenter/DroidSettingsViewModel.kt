package io.github.gdimitriu.droidcontrolcenter

import android.bluetooth.BluetoothSocket
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

    var bleSocket: BluetoothSocket?

    var connectionType : ConnectionType
    //droid settings
    var maxPower : String

    fun setMaximumPower(value : String) {
        maxPower = value
        if (currentPower > maxPower) {
            currentPower = maxPower
            isPowerChanged = true;
        }
    }
    var minPower : String

    fun setMinimumPower(value : String) {
        minPower = value;
        if (currentPower < minPower) {
            currentPower = minPower
            isPowerChanged = true
        }
    }

    var stopDistance : String
    var lowPowerDistance : String
    var currentPower : String
    var isPowerChanged : Boolean
    init {
        Log.d(TAG, "Initialized the model view")
        ipAddress  = ""
        portValue  = ""
        isChanged = true
        socket = null
        connectionType = ConnectionType.NONE
        maxPower = "255"
        minPower = "100"
        currentPower = maxPower
        stopDistance = ""
        lowPowerDistance = ""
        bleSocket = null
        isPowerChanged = false
    }
    override fun onCleared() {
        Log.d(TAG,"Clearing and close the socket.")
        super.onCleared()
        socket?.close()
        bleSocket?.close()
    }
}