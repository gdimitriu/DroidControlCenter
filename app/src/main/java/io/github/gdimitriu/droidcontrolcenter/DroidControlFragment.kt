package io.github.gdimitriu.droidcontrolcenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
                outputStreamWriter.write(message)
                outputStreamWriter.flush()
            }
            return true
        } else if (droidSettingsViewModel.connectionType == ConnectionType.BLE && validateBleSocketConnection(droidSettingsViewModel.bleSocket)) {
            GlobalScope.launch {
                val outputStreamWriter =
                    OutputStreamWriter(droidSettingsViewModel.bleSocket?.getOutputStream())
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