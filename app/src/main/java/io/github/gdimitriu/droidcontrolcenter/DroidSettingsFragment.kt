package io.github.gdimitriu.droidcontrolcenter

import android.app.AlertDialog
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
                droidSettingsViewModel.maxPower =sequence.toString()
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
                droidSettingsViewModel.minPower =sequence.toString()
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

    fun sendDataToDroid() {

    }
    private fun getDataFromDroid()  = runBlocking {
        if (validateSocketConnection(droidSettingsViewModel.socket)) {
            val job = GlobalScope.launch {
                val inputStreamReader = BufferedReader(InputStreamReader(droidSettingsViewModel.socket?.getInputStream()))
                val outputStreamWriter = OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                outputStreamWriter.write("V#")
                outputStreamWriter.flush()
                val buffer = CharArray(255)
                var value : String = inputStreamReader.readLine()
                droidSettingsViewModel.maxPower = value
                Log.d(TAG, value.toString());
                outputStreamWriter.write("v#")
                outputStreamWriter.flush()
                value = inputStreamReader.readLine()
                droidSettingsViewModel.minPower = value
                Log.d(TAG, value.toString());
                outputStreamWriter.write("d#")
                outputStreamWriter.flush()
                value = inputStreamReader.readLine()
                droidSettingsViewModel.lowPowerDistance = value
                Log.d(TAG, value.toString());
                outputStreamWriter.write("s#")
                outputStreamWriter.flush()
                value = inputStreamReader.readLine()
                droidSettingsViewModel.stopDistance = value
                Log.d(TAG, value.toString());
            }
            job.join()
        }
    }
    private fun validateSocketConnection(socket: Socket?): Boolean {
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
    companion object {
        fun newInstance(): DroidSettingsFragment {
            return DroidSettingsFragment()
        }
    }
}