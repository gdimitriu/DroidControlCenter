package io.github.gdimitriu.droidcontrolcenter

import android.app.AlertDialog
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
import kotlinx.coroutines.runBlocking
import java.net.Socket

private const val KEY_IPADDRESS="KEY_IP_ADDRESS"
private const val KEY_PORT = "KEY_PORT"
class DroidConnectionWiFiFragment : Fragment() {
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
        val viewToInflate = inflater.inflate(R.layout.activity_droid_connection_wifi, container, false)
        ipEditText = viewToInflate.findViewById(R.id.address)
        portEditText = viewToInflate.findViewById(R.id.port)
        connectButton = viewToInflate.findViewById(R.id.connection)
        connectButton.setOnClickListener { view ->
            connectToDroid()
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
        droidSettingsViewModel.connectionType = ConnectionType.WIFI
        return viewToInflate
    }

    companion object {
        fun newInstance(): DroidConnectionWiFiFragment {
            return DroidConnectionWiFiFragment()
        }
    }

    private fun connectToDroid()  = runBlocking {
        var gotException : Exception? = null
        if (droidSettingsViewModel.socket != null) {
            droidSettingsViewModel.socket!!.close()
        }
        val job = GlobalScope.launch {
            try {
                droidSettingsViewModel.socket = Socket(
                    droidSettingsViewModel.ipAddress,
                    droidSettingsViewModel.portValue.toInt()
                )
                droidSettingsViewModel.socket?.tcpNoDelay= true
            } catch (e: Exception) {
                gotException = e
                droidSettingsViewModel.socket = null
            }
            droidSettingsViewModel.isChanged = false
        }
        job.join()
        if (gotException != null) {
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage(gotException!!.localizedMessage)?.setTitle("Connection failed !")
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()
        } else {
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage("Droid is connected on wifi !")?.setTitle("Connection succeeded !")
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()
        }
    }
}