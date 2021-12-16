package io.github.gdimitriu.droidcontrolcenter

import android.annotation.SuppressLint
import android.app.AlertDialog
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
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Move forward")
                sendOneWayCommandToDroidOnWifi("M1,0#\n")
                return@setOnTouchListener true
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Stop")
                sendOneWayCommandToDroidOnWifi("M0,0#\n")
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        backwardButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Move backward")
                sendOneWayCommandToDroidOnWifi("M-1,0#\n")
                return@setOnTouchListener true
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Stop")
                sendOneWayCommandToDroidOnWifi("M0,0#\n")
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        leftButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Move left")
                sendOneWayCommandToDroidOnWifi("M0,-1#\n")
                return@setOnTouchListener true
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Stop")
                sendOneWayCommandToDroidOnWifi("M0,0#\n")
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        rightButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Move right")
                sendOneWayCommandToDroidOnWifi("M0,1#\n")
                return@setOnTouchListener true
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                if (!validateSocketConnection(droidSettingsViewModel.socket))
                    return@setOnTouchListener false
                Log.d(TAG, "Stop")
                sendOneWayCommandToDroidOnWifi("M0,0#\n")
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        stopButton.setOnClickListener { view ->
            if (validateSocketConnection(droidSettingsViewModel.socket)) {
                Log.d(TAG, "Full stop")
                sendOneWayCommandToDroidOnWifi("b#\n")
            }
        }
        return view;
    }

    private fun sendOneWayCommandToDroidOnWifi(message : String) {
        GlobalScope.launch {
            val outputStreamWriter =
                OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
            outputStreamWriter.write(message)
            outputStreamWriter.flush()
        }
    }

    companion object {
        fun newInstance(): DroidControlFragment {
            return DroidControlFragment()
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
}