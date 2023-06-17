package io.github.gdimitriu.droidcontrolcenter

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private const val TAG = "DroidNavigation"
class DroidNavigationFragment : Fragment(), OnItemClickListener {
    private lateinit var distanceText: EditText
    private lateinit var rotateText: EditText
    private lateinit var linearGroup: RadioGroup
    private lateinit var rotateGroup: RadioGroup
    private lateinit var pushFrontButton: Button
    private lateinit var pushBackButton: Button
    private lateinit var uploadButton: Button
    private lateinit var updateButton: Button
    private lateinit var powerText: EditText
    private lateinit var navigationCommandList : ListView
    private lateinit var runDirectButton : Button
    private lateinit var runReverseButton : Button
    private lateinit var navigationCommandListAdapter: ArrayAdapter<String>

    private val droidSettingsViewModel: DroidSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_droid_navigation_edit, container, false)
        distanceText = view.findViewById(R.id.linear_value)
        rotateText = view.findViewById(R.id.rotate_value)
        linearGroup = view.findViewById(R.id.radiogroup_liniar)
        linearGroup.check(R.id.navigation_forward)
        rotateGroup = view.findViewById(R.id.radiogroup_rotate)
        rotateGroup.check(R.id.navigation_right)
        distanceText.setText("0")
        rotateText.setText("0")
        powerText = view.findViewById(R.id.navigation_power)
        powerText.setText(droidSettingsViewModel.maxPower)
        val distanceTextWatcher = object : TextWatcher {
            override fun beforeTextChanged( sequence: CharSequence?, start: Int, count: Int, after: Int ) {
                //
            }

            override fun onTextChanged( sequence: CharSequence?, start: Int, before: Int, count: Int ) {
                droidSettingsViewModel.navigationDistance = sequence.toString()
                droidSettingsViewModel.isNavigationDistanceChanged = true
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        distanceText.addTextChangedListener(distanceTextWatcher)
        val rotateTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int ) {
                //
            }

            override fun onTextChanged( sequence: CharSequence?, start: Int, before: Int, count: Int ) {
                droidSettingsViewModel.navigationRotate = sequence.toString()
                droidSettingsViewModel.isNavigationRotateChanged = true
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        rotateText.addTextChangedListener(rotateTextWatcher)
        val powerTextWatcher = object : TextWatcher {
            override fun beforeTextChanged( sequence: CharSequence?, start: Int, count: Int, after: Int ) {
                //
            }

            override fun onTextChanged( sequence: CharSequence?, start: Int, before: Int, count: Int ) {
                val str = sequence.toString()
                if ( str.isEmpty() ) {
                    return
                }
                if ( ( droidSettingsViewModel.maxPower.toInt() < str.toInt() ) ||
                    ( droidSettingsViewModel.minPower.toInt() > str.toInt())) {
                    return
                }
                droidSettingsViewModel.navigationPower = str
                droidSettingsViewModel.isNavigationPowerChanged = true
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }
        powerText.addTextChangedListener(powerTextWatcher)
        //buttons
        pushFrontButton = view.findViewById(R.id.navigation_front)
        pushFrontButton.setOnClickListener {
            addCommand(true)
        }
        pushBackButton = view.findViewById(R.id.navigation_back)
        pushBackButton.setOnClickListener {
            addCommand(false)
        }
        uploadButton = view.findViewById(R.id.navigation_upload)
        uploadButton.setOnClickListener {
            uploadData()
        }
        updateButton = view.findViewById(R.id.navigation_update)
        updateButton.setOnClickListener {
            updateGetData()
        }

        runDirectButton = view.findViewById(R.id.droid_navigation_run_direct)
        runDirectButton.setOnClickListener {
            sendOneWayCommandToDroid("D#\n",true)
            sendOneWayCommandToDroid("C#\n",true)
        }

        runReverseButton = view.findViewById(R.id.droid_navigation_run_reverse)
        runReverseButton.setOnClickListener {
            sendOneWayCommandToDroid("B#\n", true)
            sendOneWayCommandToDroid("C#\n",true)
        }

        navigationCommandList = view.findViewById(R.id.run_on_droid_list)
        navigationCommandListAdapter = ArrayAdapter(requireActivity().applicationContext,
            android.R.layout.simple_list_item_single_choice,
            droidSettingsViewModel.commands
            )
        navigationCommandList.adapter = navigationCommandListAdapter
        navigationCommandList.choiceMode = ListView.CHOICE_MODE_SINGLE
        navigationCommandList.onItemClickListener = this
        var checkListenerDistance = OnCheckedChangeListener { _, _ -> droidSettingsViewModel.isNavigationDistanceChanged = true }
        var checkListenerRotate = OnCheckedChangeListener { _, _ -> droidSettingsViewModel.isNavigationRotateChanged = true }
        linearGroup.setOnCheckedChangeListener(checkListenerDistance)
        rotateGroup.setOnCheckedChangeListener(checkListenerRotate)
        droidSettingsViewModel.isNavigationDistanceChanged = false
        droidSettingsViewModel.isNavigationRotateChanged = false
        droidSettingsViewModel.isPowerChanged = false
        return view
    }

    private fun createCommand() : String {
        var command: String

        if (droidSettingsViewModel.isNavigationDistanceChanged) {
            command = if ( R.id.navigation_forward == linearGroup.checkedRadioButtonId) {
                "m" + droidSettingsViewModel.navigationDistance + ",0#"
            } else {
                "m-" + droidSettingsViewModel.navigationDistance + ",0#"
            }
            droidSettingsViewModel.isNavigationDistanceChanged = false
        } else if (droidSettingsViewModel.isNavigationRotateChanged) {
            command = if ( R.id.navigation_right == rotateGroup.checkedRadioButtonId ) {
                "m0," + droidSettingsViewModel.navigationRotate + "#"
            } else {
                "m0,-" + droidSettingsViewModel.navigationRotate + "#"
            }
            droidSettingsViewModel.isNavigationRotateChanged = false
        } else if (droidSettingsViewModel.isNavigationPowerChanged) {
            command = "c" + droidSettingsViewModel.navigationPower + "#"
            droidSettingsViewModel.isNavigationPowerChanged = false
        } else {
            command = ""
        }
        return command
    }

    private fun addCommand(type: Boolean) {
        val command = createCommand()
        if (!command.isNullOrEmpty()) {
            if (type) {
                droidSettingsViewModel.commands.add(0,command)
                Log.d(TAG, "added first $command")
            } else {
                droidSettingsViewModel.commands.add(command)
                Log.d(TAG, "added last $command")
            }
            navigationCommandListAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        fun newInstance(): DroidNavigationFragment {
            return DroidNavigationFragment()
        }
    }

    private fun updateGetData() {
        Log.d(TAG, navigationCommandList.checkedItemPosition.toString())
        if ( droidSettingsViewModel.listSelectedPosition < 0 ) {
            return
        }
        val command = createCommand()
        droidSettingsViewModel.commands[droidSettingsViewModel.listSelectedPosition] = command
        droidSettingsViewModel.listSelectedPosition = -1
        navigationCommandList.clearChoices()
        navigationCommandListAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
        droidSettingsViewModel.listSelectedPosition = position
        val command : String = droidSettingsViewModel.commands[position];
        if (command.startsWith("c")) {
            powerText.setText(command.subSequence(1, command.length - 1))
        } else if ( command.startsWith("m")) {
            val comaIndex = command.indexOf(",")
            val distance = command.substring(1, comaIndex)
            val rotate = command.substring(comaIndex + 1, command.length - 2)
            if (distance.isNotEmpty()) {
                if (distance.toInt() < 0) {
                    linearGroup.check(R.id.navigation_backward)
                    distanceText.setText(distance.substring(1))
                } else {
                    linearGroup.check(R.id.navigation_forward)
                    distanceText.setText(distance)
                }
            }
            if (rotate.isNotEmpty()) {
                if (rotate.toInt() < 0) {
                    rotateGroup.check(R.id.navigation_left)
                    rotateText.setText(rotate.substring(1))
                } else {
                    rotateGroup.check(R.id.navigation_right)
                    rotateText.setText(rotate)
                }
            }
        }
    }

    private fun uploadData() {
        //send clear command
        sendOneWayCommandToDroid("n#\n", true)
        for ( str: String in droidSettingsViewModel.commands) {
            sendOneWayCommandToDroid("N$str\n", true)
        }
    }
    private fun sendOneWayCommandToDroid(message : String, hasAck : Boolean = false) : Boolean = runBlocking {
        if (droidSettingsViewModel.connectionType == ConnectionType.WIFI && CommUtils.validateWiFiSocketConnection(droidSettingsViewModel.socket, activity)) {
            var job = GlobalScope.launch {
                val outputStreamWriter =
                    OutputStreamWriter(droidSettingsViewModel.socket?.getOutputStream())
                val inputStreamReader =
                    BufferedReader(InputStreamReader(droidSettingsViewModel.socket?.getInputStream()))
                outputStreamWriter.write(message)
                outputStreamWriter.flush()
                if (hasAck) {
                    val status = inputStreamReader.readLine()
                    Log.d(TAG,"s=$status")
                }
            }
            job.join()
            return@runBlocking true
        } else if (droidSettingsViewModel.connectionType == ConnectionType.BLE && CommUtils.validateBleSocketConnection(droidSettingsViewModel.bleSocket, activity)) {
            var job = GlobalScope.launch {
                val outputStreamWriter =
                    OutputStreamWriter(droidSettingsViewModel.bleSocket?.getOutputStream())
                val inputStreamReader =
                    BufferedReader(InputStreamReader(droidSettingsViewModel.bleSocket?.getInputStream()))
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
}

