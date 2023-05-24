package io.github.gdimitriu.droidcontrolcenter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

private const val TAG = "DroidNavigation"
class DroidNavigationFragment : Fragment() {
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
    private lateinit var navigationCommandListAdapter: CustomAdapter

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
        var checkListenerDistance = OnCheckedChangeListener { _, _ -> droidSettingsViewModel.isNavigationDistanceChanged = true }
        var checkListenerRotate = OnCheckedChangeListener { _, _ -> droidSettingsViewModel.isNavigationRotateChanged = true }
        linearGroup.setOnCheckedChangeListener(checkListenerDistance)
        rotateGroup.setOnCheckedChangeListener(checkListenerRotate)
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
        updateButton = view.findViewById(R.id.navigation_update)
        updateButton.setOnClickListener {
            updateGetData()
        }

        runDirectButton = view.findViewById(R.id.droid_navigation_run_direct)

        runReverseButton = view.findViewById(R.id.droid_navigation_run_reverse)

        navigationCommandList = view.findViewById(R.id.run_on_droid_list)
        navigationCommandListAdapter = CustomAdapter(
            droidSettingsViewModel.commands,
            requireActivity().applicationContext)
        navigationCommandList.adapter = navigationCommandListAdapter
        navigationCommandList.choiceMode = ListView.CHOICE_MODE_SINGLE
        return view
    }

    private fun addCommand(type: Boolean) {
        var command: String
        if (droidSettingsViewModel.isNavigationDistanceChanged) {
            if ( R.id.navigation_forward == linearGroup.checkedRadioButtonId) {
                command = "m" + droidSettingsViewModel.navigationDistance + ",0#"
            } else {
                command = "m-" + droidSettingsViewModel.navigationDistance + ",0#"
            }
            droidSettingsViewModel.isNavigationDistanceChanged = false
        } else if (droidSettingsViewModel.isNavigationRotateChanged) {
            if ( R.id.navigation_right == rotateGroup.checkedRadioButtonId ) {
                command = "m0," + droidSettingsViewModel.navigationRotate + "#"
            } else {
                command = "m0,-" + droidSettingsViewModel.navigationRotate + "#"
            }
            droidSettingsViewModel.isNavigationRotateChanged = false
        } else if (droidSettingsViewModel.isNavigationPowerChanged) {
            command = "c" + droidSettingsViewModel.navigationPower + "#"
            droidSettingsViewModel.isNavigationPowerChanged = false
        } else {
            command = ""
        }
        if (!command.isNullOrEmpty()) {
            if (type) {
                droidSettingsViewModel.commands.add(0,DataModel(command, false))
                Log.d(TAG, "added first $command")
            } else {
                droidSettingsViewModel.commands.add(DataModel(command, false))
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

    private fun updateData(command : String?) {

        Log.d(TAG, "Position selected $command")
    }

    private fun updateGetData() {
        val position = navigationCommandList.selectedItemPosition
        droidSettingsViewModel.listSelectedPosition = position
        val dataModel : DataModel = droidSettingsViewModel.commands!![position] as DataModel
        val checked = !dataModel.checked
        dataModel.checked = !dataModel.checked
        navigationCommandListAdapter.notifyDataSetChanged()
        updateData(dataModel.command)
    }
}

