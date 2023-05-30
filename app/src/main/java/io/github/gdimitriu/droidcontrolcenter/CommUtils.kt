package io.github.gdimitriu.droidcontrolcenter

import android.app.AlertDialog
import android.bluetooth.BluetoothSocket
import androidx.fragment.app.FragmentActivity
import java.net.Socket

class CommUtils {

    companion object {
        fun validateWiFiSocketConnection(socket: Socket?, activity: FragmentActivity?): Boolean {
            if (socket == null || socket.isClosed) {
                val builder: AlertDialog.Builder? = activity?.let {
                    AlertDialog.Builder(it)
                }
                builder?.setMessage("Connect first the droid !")?.setTitle("Connection failed !")
                val dialog: AlertDialog? = builder?.create()
                dialog?.show()
                return false
            }
            return true
        }

        fun validateBleSocketConnection( socket: BluetoothSocket?, activity: FragmentActivity? ): Boolean {
            if (socket == null || !socket.isConnected) {
                val builder: AlertDialog.Builder? = activity?.let {
                    AlertDialog.Builder(it)
                }
                builder?.setMessage("Connect first the droid !")?.setTitle("Connection failed !")
                val dialog: AlertDialog? = builder?.create()
                dialog?.show()
                return false
            }
            return true
        }
    }
}