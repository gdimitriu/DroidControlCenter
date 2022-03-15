package io.github.gdimitriu.droidcontrolcenter

import android.app.AlertDialog
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

private const val MAX_POWER_KEY = "DROID_MAX_POWER"
private const val MIN_POWER_KEY = "DROID_MIN_POWER"
private const val STOP_DISTANCE_KEY= "DROID_STOP_DISTANCE"
private const val LOW_POWER_DISTANCE_KEY = "DROID_LOW_POWER_DISTANCE"
private const val TAG = "DroidControl"
class DroidSettingsFragment : Fragment() {

    private lateinit var maxPowerText : EditText
    private lateinit var minPowerText : EditText
    private lateinit var stopDistanceText : EditText
    private lateinit var lowPowerDistanceText : EditText
    private lateinit var sendDataButton: Button
    private val droidSettingsViewModel: DroidSettingsViewModel by activityViewModels()

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString(MAX_POWER_KEY, droidSettingsViewModel.maxPower)
        savedInstanceState.putString(MIN_POWER_KEY, droidSettingsViewModel.minPower)
        savedInstanceState.putString(STOP_DISTANCE_KEY, droidSettingsViewModel.stopDistance)
        savedInstanceState.putString(LOW_POWER_DISTANCE_KEY, droidSettingsViewModel.lowPowerDistance)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_droid_settings, container, false)
        sendDataButton = view.findViewById(R.id.send_data)
        sendDataButton.setOnClickListener {
            sendDataToDroid()
        }
        maxPowerText = view.findViewById(R.id.max_power)
        val maxPowerWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                droidSettingsViewModel.setMaximumPower(sequence.toString())
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        minPowerText = view.findViewById(R.id.min_power)
        val minPowerWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                droidSettingsViewModel.setMinimumPower(sequence.toString())
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        stopDistanceText = view.findViewById(R.id.stop_distance)
        val stopDistanceWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                droidSettingsViewModel.stopDistance =sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        lowPowerDistanceText = view.findViewById(R.id.low_power_distance)
        val lowPowerDistanceWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                droidSettingsViewModel.lowPowerDistance =sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        getDataFromDroid()
        maxPowerText.setText(droidSettingsViewModel.maxPower)
        minPowerText.setText(droidSettingsViewModel.minPower)
        stopDistanceText.setText(droidSettingsViewModel.stopDistance)
        lowPowerDistanceText.setText(droidSettingsViewModel.lowPowerDistance)
        maxPowerText.addTextChangedListener(maxPowerWatcher)
        minPowerText.addTextChangedListener(minPowerWatcher)
        stopDistanceText.addTextChangedListener(stopDistanceWatcher)
        lowPowerDistanceText.addTextChangedListener(lowPowerDistanceWatcher)
        return view;
    }

    private fun sendDataToDroid(inputStreamReader: BufferedReader, outputStreamWriter: OutputStreamWriter) {
        outputStreamWriter.write(String.format("V%s#\n",droidSettingsViewModel.maxPower))
        outputStreamWriter.flush()
        var status : String = inputStreamReader.readLine()
        Log.d(TAG, "V=$status")
        outputStreamWriter.write(String.format("v%s#\n",droidSettingsViewModel.minPower))
        outputStreamWriter.flush()
        status = inputStreamReader.readLine()
        Log.d(TAG,"v=$status")
        outputStreamWriter.write("d"+droidSettingsViewModel.lowPowerDistance + "#\n")
        outputStreamWriter.flush()
        status = inputStreamReader.readLine()
        Log.d(TAG,"d=$status")
        outputStreamWriter.write("s"+droidSettingsViewModel.stopDistance+"#\n")
        outputStreamWriter.flush()
        status = inputStreamReader.readLine()
        Log.d(TAG,"s=$status")
    }

    private fun sendDataToDroid() = runBlocking  {
        if (droidSettingsViewModel.connectionType == ConnectionType.WIFI && validateWiFiSocketConnection(droidSettingsViewModel.socket)) {
            val job = GlobalScope.launch {
                val outputStreamWriter = OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                val inputStreamReader = BufferedReader(InputStreamReader(droidSettingsViewModel.socket?.getInputStream()))
                sendDataToDroid(inputStreamReader, outputStreamWriter)
            }
            job.join()
        } else if (droidSettingsViewModel.connectionType == ConnectionType.BLE && validateBleSocketConnection(droidSettingsViewModel.bleSocket)) {
            val job = GlobalScope.launch {
                val inputStreamReader = BufferedReader(InputStreamReader(droidSettingsViewModel.bleSocket?.inputStream))
                val outputStreamWriter = OutputStreamWriter(droidSettingsViewModel.bleSocket?.outputStream)
                sendDataToDroid(inputStreamReader, outputStreamWriter)
            }
            job.join()
        }
    }

    private fun getDataFromDroid(inputStreamReader : BufferedReader, outputStreamWriter: OutputStreamWriter) {
        outputStreamWriter.write("V#")
        outputStreamWriter.flush()
        var value : String = inputStreamReader.readLine()
        droidSettingsViewModel.maxPower = value
        Log.d(TAG, value.toString())
        outputStreamWriter.write("v#")
        outputStreamWriter.flush()
        value = inputStreamReader.readLine()
        droidSettingsViewModel.minPower = value
        Log.d(TAG, value.toString())
        outputStreamWriter.write("d#")
        outputStreamWriter.flush()
        value = inputStreamReader.readLine()
        droidSettingsViewModel.lowPowerDistance = value
        Log.d(TAG, value.toString())
        outputStreamWriter.write("s#")
        outputStreamWriter.flush()
        value = inputStreamReader.readLine()
        droidSettingsViewModel.stopDistance = value
        Log.d(TAG, value.toString())
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
        fun newInstance(): DroidSettingsFragment {
            return DroidSettingsFragment()
        }
    }
}