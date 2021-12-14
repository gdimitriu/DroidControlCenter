package io.github.gdimitriu.droidcontrolcenter

import android.annotation.SuppressLint
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
                Log.d(TAG, "Move forward")
                GlobalScope.launch {
                    val outputStreamWriter =
                        OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                    outputStreamWriter.write("M1,0#\n")
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }
                return@setOnTouchListener true
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Stop")
                GlobalScope.launch {
                    val outputStreamWriter =
                        OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                    outputStreamWriter.write("M0,0#\n")
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        backwardButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "Move backward")
                GlobalScope.launch {
                    val outputStreamWriter =
                        OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                    outputStreamWriter.write("M-1,0#\n")
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }
                return@setOnTouchListener true
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Stop")
                GlobalScope.launch {
                    val outputStreamWriter =
                        OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                    outputStreamWriter.write("M0,0#\n")
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }
        leftButton.setOnTouchListener { view, motionEvent ->
            val event = motionEvent as MotionEvent
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "Move left")
                GlobalScope.launch {
                    val outputStreamWriter =
                        OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                    outputStreamWriter.write("M0,-1#\n")
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }
                return@setOnTouchListener true
            } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Stop")
                GlobalScope.launch {
                    val outputStreamWriter =
                        OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                    outputStreamWriter.write("M0,0#\n")
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }
                return@setOnTouchListener true
            }
                return@setOnTouchListener false
            }

            rightButton.setOnTouchListener { view, motionEvent ->
                val event = motionEvent as MotionEvent
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "Move right")
                    GlobalScope.launch {
                        val outputStreamWriter =
                            OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                        outputStreamWriter.write("M0,1#\n")
                        outputStreamWriter.flush()
                        outputStreamWriter.close()
                    }
                    return@setOnTouchListener true
                } else if (event.actionMasked == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "Stop")
                    GlobalScope.launch {
                        val outputStreamWriter =
                            OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                        outputStreamWriter.write("M0,0#\n")
                        outputStreamWriter.flush()
                        outputStreamWriter.close()
                    }
                    return@setOnTouchListener true
                }
                return@setOnTouchListener false
            }
            stopButton.setOnClickListener { view ->
                Log.d(TAG, "Full stop")
                GlobalScope.launch {

                    val outputStreamWriter =
                        OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                    outputStreamWriter.write("b#\n")
                    outputStreamWriter.flush()
                    outputStreamWriter.close()
                }
            }
            return view;
        }

        companion object {
            fun newInstance(): DroidControlFragment {
                return DroidControlFragment()
            }
        }
    }