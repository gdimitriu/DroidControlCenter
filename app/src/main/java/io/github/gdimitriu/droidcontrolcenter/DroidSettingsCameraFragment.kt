package io.github.gdimitriu.droidcontrolcenter

import android.app.AlertDialog
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

private const val DROID_WIFI = "DROID_WIFI"
private const val DROID_CAMERA_STATUS = "DROID_CAMERA_STATUS"
private const val DROID_CAMERA_PROTOCOL = "DROID_CAMERA_PROTOCOL"
private const val DROID_CAMERA_PORT = "DROID_CAMERA_PORT"
private const val DROID_CAMERA_CODEC = "DROID_CAMERA_CODEC"

private const val TAG = "DroidSettingsCamera"
class DroidSettingsCameraFragment : Fragment() {

    private lateinit var wifiIPAddressText : TextView
    private lateinit var cameraStatusText : TextView
    private lateinit var cameraProtocolText : TextView
    private lateinit var cameraPortText : TextView
    private lateinit var cameraCodecText : TextView
    private val droidSettingsViewModel: DroidSettingsViewModel by activityViewModels()

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString(DROID_WIFI,droidSettingsViewModel.wifiAddress)
        savedInstanceState.putString(DROID_CAMERA_STATUS, droidSettingsViewModel.cameraStatus)
        savedInstanceState.putString(DROID_CAMERA_PROTOCOL, droidSettingsViewModel.cameraProtocol)
        savedInstanceState.putString(DROID_CAMERA_PORT, droidSettingsViewModel.cameraPort)
        savedInstanceState.putString(DROID_CAMERA_CODEC,droidSettingsViewModel.cameraCodec)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_droid_info, container, false)
        wifiIPAddressText = view.findViewById(R.id.wifi_ip_address)
        cameraStatusText = view.findViewById(R.id.camera_status)
        cameraProtocolText = view.findViewById(R.id.camera_protocol)
        cameraPortText = view.findViewById(R.id.camera_port)
        cameraCodecText = view.findViewById(R.id.camera_codec)
        getDataFromDroid()
        wifiIPAddressText.text = droidSettingsViewModel.wifiAddress
        cameraStatusText.text = droidSettingsViewModel.cameraStatus
        cameraProtocolText.text = droidSettingsViewModel.cameraProtocol
        cameraPortText.text = droidSettingsViewModel.cameraPort
        cameraCodecText.text = droidSettingsViewModel.cameraCodec
        return view
    }

    private fun getDataFromDroid(inputStreamReader : BufferedReader, outputStreamWriter: OutputStreamWriter) {
        outputStreamWriter.write("I#")
        outputStreamWriter.flush()
        var value : String = inputStreamReader.readLine()
        var values = value.split("#")
        if (values.isEmpty())
            return
        if (values.size == 1) {
            if (values[0] == "unsupported") {
                Log.i(TAG,"")
                droidSettingsViewModel.wifiAddress = "na"
                droidSettingsViewModel.cameraStatus = "off"
                droidSettingsViewModel.cameraProtocol = "na"
                droidSettingsViewModel.cameraPort = "na"
                droidSettingsViewModel.cameraCodec = "na"
                return
            }
        }
        for (info in values) {
            if (info.startsWith("wifi:")) {
                value = info.removePrefix("wifi:")
                var ips = value.split(",")
                Log.d(TAG, "IPS:$ips")
                droidSettingsViewModel.wifiAddress = ips[0]
            } else if (info.startsWith("StreamCamera:")) {
                value = info.removePrefix("StreamCamera:")
                var settings = value.split(",")
                droidSettingsViewModel.cameraStatus = settings[0]
                droidSettingsViewModel.cameraProtocol = settings[1]
                droidSettingsViewModel.cameraPort = settings[2]
                droidSettingsViewModel.cameraCodec = settings[3]
            }
        }
    }

    private fun getDataFromDroid()  = runBlocking {
        if (droidSettingsViewModel.connectionType == ConnectionType.WIFI && validateWiFiSocketConnection(droidSettingsViewModel.socket)) {
            val job = GlobalScope.launch {
                val inputStreamReader = BufferedReader(InputStreamReader(droidSettingsViewModel.socket?.getInputStream()))
                val outputStreamWriter = OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                getDataFromDroid(inputStreamReader, outputStreamWriter)
            }
            job.join()
        } else if (droidSettingsViewModel.connectionType == ConnectionType.BLE && validateBleSocketConnection(droidSettingsViewModel.bleSocket)) {
            val job = GlobalScope.launch {
                val inputStreamReader = BufferedReader(InputStreamReader(droidSettingsViewModel.bleSocket?.inputStream))
                val outputStreamWriter = OutputStreamWriter(droidSettingsViewModel.bleSocket?.outputStream)
                getDataFromDroid(inputStreamReader, outputStreamWriter)
            }
            job.join()
        }
    }
    private fun validateWiFiSocketConnection(socket: Socket?): Boolean {
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
    private fun validateBleSocketConnection(socket: BluetoothSocket?): Boolean {
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
    companion object {
        fun newInstance(): DroidSettingsCameraFragment {
            return DroidSettingsCameraFragment()
        }
    }
}