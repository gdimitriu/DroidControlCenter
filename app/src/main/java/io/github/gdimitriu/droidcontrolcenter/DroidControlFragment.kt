package io.github.gdimitriu.droidcontrolcenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothSocket
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.ArrayList

private const val TAG = "DroidControl"
private const val USE_TEXTURE_VIEW = false
private const val ENABLE_SUBTITLES = true

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

    private var mLibVLC: LibVLC? = null
    private var mMediaPlayer: MediaPlayer? = null
    private lateinit var videoLayout : VLCVideoLayout
    private var isStart : Boolean = false

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            powerBar.min = droidSettingsViewModel.minPower.toInt()
        }
        powerBar.max = droidSettingsViewModel.maxPower.toInt()
        powerBar.progress = droidSettingsViewModel.maxPower.toInt()
        powerBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, currentValue: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    powerBar.min = droidSettingsViewModel.minPower.toInt()
                }
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
        //camera operations
        mLibVLC = LibVLC(context, ArrayList<String>().apply {
            add("--no-drop-late-frames")
            add("--no-skip-frames")
            add("--rtsp-tcp")
            add("-vvv")
        })
        videoLayout = view.findViewById(R.id.droid_videoLayout)
        videoLayout.setOnClickListener {
            if (isStart) {
                stopReceivingStreamFromCamera()
            } else {
                startReceivingStreamFromCamera();
            }
        }
        if (droidSettingsViewModel.isCameraStreaming)
            startReceivingStreamFromCamera()
        return view;
    }

    private fun stopReceivingStreamFromCamera() {
        Log.d(TAG,"Streaming camera is stopping")
        mMediaPlayer?.stop()
        mMediaPlayer?.detachViews()
        mMediaPlayer?.release()
        sendOneWayCommandToDroid("t#\n")
        droidSettingsViewModel.isCameraStreaming = false
        isStart = false
        Log.d(TAG,"Streaming camera is stopped")
    }

    private fun startReceivingStreamFromCamera() {
        if (droidSettingsViewModel.cameraStatus.compareTo("off") == 0) {
            Log.i(TAG,"Stream Camera is disabled")
            return
        }
        Log.d(TAG,"Streaming camera is starting")
        if (!sendOneWayCommandToDroid("T#\n",true))
            return
        mMediaPlayer = MediaPlayer(mLibVLC)
        mMediaPlayer?.attachViews(videoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)
        try {
            val httpUrl = "${droidSettingsViewModel.cameraProtocol}/${droidSettingsViewModel.cameraCodec}://${droidSettingsViewModel.wifiAddress}:${droidSettingsViewModel.cameraPort}"
            val uri = Uri.parse(httpUrl)

            Media(mLibVLC, uri).apply {
                setHWDecoderEnabled(true, false);
                addOption(":network-caching=150");
                addOption(":clock-jitter=0");
                addOption(":clock-synchro=0");
                mMediaPlayer?.media = this

            }.release()

            mMediaPlayer?.play()
            droidSettingsViewModel.isCameraStreaming = true
            isStart = true
            Log.d(TAG,"Streaming camera is started")
        } catch (e: IOException) {
            Log.e(TAG, e.localizedMessage)
            e.printStackTrace()
        }
    }

    private fun sendOneWayCommandToDroid(message : String, hasAck : Boolean = false) : Boolean = runBlocking {
        if (droidSettingsViewModel.connectionType == ConnectionType.WIFI && validateWiFiSocketConnection(droidSettingsViewModel.socket)) {
            var job = GlobalScope.launch {
                val outputStreamWriter =
                    OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                val inputStreamReader =
                    BufferedReader(InputStreamReader(droidSettingsViewModel.socket?.getInputStream()))
                sendCurrentPowerToDroid(inputStreamReader,outputStreamWriter)
                outputStreamWriter.write(message)
                outputStreamWriter.flush()
                if (hasAck) {
                    val status = inputStreamReader.readLine()
                    Log.d(TAG,"s=$status")
                }
            }
            job.join()
            return@runBlocking true
        } else if (droidSettingsViewModel.connectionType == ConnectionType.BLE && validateBleSocketConnection(droidSettingsViewModel.bleSocket)) {
            var job = GlobalScope.launch {
                val outputStreamWriter =
                    OutputStreamWriter(droidSettingsViewModel.bleSocket?.getOutputStream())
                val inputStreamReader =
                    BufferedReader(InputStreamReader(droidSettingsViewModel.bleSocket?.getInputStream()))
                sendCurrentPowerToDroid(inputStreamReader,outputStreamWriter)
                outputStreamWriter.write(message)
                outputStreamWriter.flush()
                if (hasAck) {
                    val status = inputStreamReader.readLine()
                    Log.d(TAG,"s=$status")
                }
            }
            job.join()
            return@runBlocking true
        } else if (droidSettingsViewModel.connectionType == ConnectionType.NONE) {
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage("Connect first the droid !")?.setTitle("Connection failed !")
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()
        }
        return@runBlocking false
    }

    @Synchronized
    private fun sendCurrentPowerToDroid(inputStreamReader: BufferedReader, outputStreamWriter: OutputStreamWriter) {
        if ((!isCurrentPowerChanged) && (!droidSettingsViewModel.isPowerChanged))
            return
        if (droidSettingsViewModel.isPowerChanged) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                powerBar.min = droidSettingsViewModel.minPower.toInt()
            }
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

    override fun onStop() {
        super.onStop()
        mMediaPlayer?.stop()
        mMediaPlayer?.detachViews()
    }
    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()
        mLibVLC?.release()
        sendOneWayCommandToDroid("t#\n")
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