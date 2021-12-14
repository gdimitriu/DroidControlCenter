package io.github.gdimitriu.droidcontrolcenter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Socket

private const val KEY_IPADDRESS="KEY_IP_ADDRESS"
private const val KEY_PORT = "KEY_PORT"
class DroidConnectionFragment : Fragment() {
    private lateinit var ipEditText: EditText
    private lateinit var portEditText: EditText
    private lateinit var connectButton : Button

    private val droidSettingsViewModel: DroidSettingsViewModel by activityViewModels()


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString(KEY_IPADDRESS,droidSettingsViewModel.ipAddress)
        savedInstanceState.putString(KEY_PORT,droidSettingsViewModel.portValue)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_droid_connection, container, false)
        ipEditText = view.findViewById(R.id.address)
        portEditText = view.findViewById(R.id.port)
        connectButton = view.findViewById(R.id.connection)
        connectButton.setOnClickListener { view ->
            if (droidSettingsViewModel.isChanged == true) {
                GlobalScope.launch {
                    droidSettingsViewModel.socket = Socket(droidSettingsViewModel.ipAddress, droidSettingsViewModel.portValue.toInt())
                    droidSettingsViewModel.isChanged = false
                }
            }
            droidSettingsViewModel.socket?.tcpNoDelay= true
        }
        val portWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                droidSettingsViewModel.setPort(sequence.toString())
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }

        val addressWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                droidSettingsViewModel.setAddress(sequence.toString())
            }

            override fun afterTextChanged(sequence: Editable?) {
                //
            }
        }

        if (savedInstanceState != null) {
            droidSettingsViewModel.setAddress(
                savedInstanceState?.getString(
                    KEY_IPADDRESS,
                    ipEditText.text.toString()
                ) ?: ipEditText.text.toString()
            )
            droidSettingsViewModel.setPort(
                savedInstanceState?.getString(
                    KEY_PORT,
                    portEditText.text.toString()
                ) ?: portEditText.text.toString()
            )
        } else {
            if (droidSettingsViewModel.ipAddress == "") {
                droidSettingsViewModel.setAddress(ipEditText.text.toString())
            }
            if (droidSettingsViewModel.portValue == "") {
                droidSettingsViewModel.setPort(portEditText.text.toString())
            }
        }
        ipEditText.setText(droidSettingsViewModel.ipAddress)
        portEditText.setText(droidSettingsViewModel.portValue)
        portEditText.addTextChangedListener(portWatcher)
        ipEditText.addTextChangedListener(addressWatcher)
        return view
    }

    companion object {
        fun newInstance(): DroidConnectionFragment {
            return DroidConnectionFragment()
        }
    }
}