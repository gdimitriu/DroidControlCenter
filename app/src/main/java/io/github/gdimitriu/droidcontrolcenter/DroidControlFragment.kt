package io.github.gdimitriu.droidcontrolcenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

private const val TAG = "DroidControl"

@DelicateCoroutinesApi
class DroidControlFragment : Fragment() {
    private lateinit var forwardButton: Button
    private lateinit var backwardButton: Button
    private lateinit var leftButton: Button
    private lateinit var rightButton: Button
    private lateinit var stopButton: Button
    private lateinit var powerBar : SeekBar
    private lateinit var currentPower : EditText
    private var isCurrentPowerChanged : Boolean = false

    private val droidSettingsViewModel: DroidSettingsViewModel by activityViewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.droid_control, container, false)
        forwardButton = view.findViewById(R.id.forward)
        backwardButton = view.findViewById(R.id.backward)
        leftButton = view.findViewById(R.id.left)
        rightButton = view.findViewById(R.id.right)
        stopButton = view.findViewById(R.id.stop)
        currentPower = view.findViewById(R.id.currentPower)
        currentPower.setText(droidSettingsViewModel.maxPower)
        powerBar = view.findViewById(R.id.powerBar)
        powerBar.min = droidSettingsViewModel.minPower.toInt()
        powerBar.max = droidSettingsViewModel.maxPower.toInt()
        powerBar.progress = droidSettingsViewModel.maxPower.toInt()
        powerBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, currentValue: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                powerBar.min = droidSettingsViewModel.minPower.toInt()
                powerBar.max = droidSettingsViewModel.maxPower.toInt()
                powerBar.progress = droidSettingsViewModel.currentPower.toInt()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (!isCurrentPowerChanged)
                    isCurrentPowerChanged = true
                if (droidSettingsViewModel.isPowerChanged)
                    droidSettingsViewModel.isPowerChanged = false
                currentPower.setText(seekBar!!.progress.toString())
                droidSettingsViewModel.currentPower = seekBar!!.progress.toString()
                if (seekBar != null) {
                    Log.d(TAG,"Current Power=" + seekBar.progress.toString())
                }
            }
        })
        forwardButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if(sendOneWayCommandToDroid("M1,0#\n")) {
                    Log.d(TAG, "Move forward")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if(sendOneWayCommandToDroid("M0,0#\n")) {
                    Log.d(TAG, "Stop")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
            return@setOnTouchListener false
        }

        backwardButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if(sendOneWayCommandToDroid("M-1,0#\n")) {
                    Log.d(TAG, "Move backward")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if(sendOneWayCommandToDroid("M0,0#\n")) {
                    Log.d(TAG, "Stop")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
            return@setOnTouchListener false
        }
        leftButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if(sendOneWayCommandToDroid("M0,-1#\n")) {
                    Log.d(TAG, "Move left")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if(sendOneWayCommandToDroid("M0,0#\n")) {
                    Log.d(TAG, "Stop")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
            return@setOnTouchListener false
        }

        rightButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if(sendOneWayCommandToDroid("M0,1#\n")) {
                    Log.d(TAG, "Move right")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (sendOneWayCommandToDroid("M0,0#\n")) {
                    Log.d(TAG, "Stop")
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
            return@setOnTouchListener false
        }
        stopButton.setOnClickListener { view ->
            if (sendOneWayCommandToDroid("b#\n"))
                Log.d(TAG, "Full stop")
        }
        return view;
    }

    private fun sendOneWayCommandToDroid(message : String) : Boolean {
        if (droidSettingsViewModel.connectionType == ConnectionType.WIFI && validateWiFiSocketConnection(droidSettingsViewModel.socket)) {
            GlobalScope.launch {
                val outputStreamWriter =
                    OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                val inputStreamReader =
                    BufferedReader(InputStreamReader(droidSettingsViewModel.socket?.getInputStream()))
                sendCurrentPowerToDroid(inputStreamReader,outputStreamWriter)
                outputStreamWriter.write(message)
                outputStreamWriter.flush()
            }
            return true
        } else if (droidSettingsViewModel.connectionType == ConnectionType.BLE && validateBleSocketConnection(droidSettingsViewModel.bleSocket)) {
            GlobalScope.launch {
                val outputStreamWriter =
                    OutputStreamWriter(droidSettingsViewModel.bleSocket?.getOutputStream())
                val inputStreamReader =
                    BufferedReader(InputStreamReader(droidSettingsViewModel.bleSocket?.getInputStream()))
                sendCurrentPowerToDroid(inputStreamReader,outputStreamWriter)
                outputStreamWriter.write(message)
                outputStreamWriter.flush()
            }
            return true
        } else if (droidSettingsViewModel.connectionType == ConnectionType.NONE) {
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage("Connect first the droid !")?.setTitle("Connection failed !")
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()
        }
        return false
    }

    @Synchronized
    private fun sendCurrentPowerToDroid(inputStreamReader: BufferedReader, outputStreamWriter: OutputStreamWriter) {
        if ((!isCurrentPowerChanged) && (!droidSettingsViewModel.isPowerChanged))
            return
        if (droidSettingsViewModel.isPowerChanged) {
            powerBar.min = droidSettingsViewModel.minPower.toInt()
            powerBar.max = droidSettingsViewModel.maxPower.toInt()
            powerBar.progress = droidSettingsViewModel.currentPower.toInt()
            droidSettingsViewModel.isPowerChanged = false
            isCurrentPowerChanged = true
        }
        outputStreamWriter.write(String.format("c%s#\n",droidSettingsViewModel.currentPower))
        outputStreamWriter.flush()
        var status : String = inputStreamReader.readLine()
        Log.d(TAG,"c=$status")
        isCurrentPowerChanged = false
    }
    companion object {
        fun newInstance(): DroidControlFragment {
            return DroidControlFragment()
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
}