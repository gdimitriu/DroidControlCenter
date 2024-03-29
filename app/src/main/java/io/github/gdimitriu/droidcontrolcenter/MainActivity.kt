package io.github.gdimitriu.droidcontrolcenter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.net.toUri
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

private const val TAG = "DroidControlMain"
class MainActivity : AppCompatActivity() {
    private val droidSettingViewModel: DroidSettingsViewModel by viewModels()
    private val _saveFile = 2
    private val _loadFile = 1
    private val _appendFile = 3

    private val isExternalStorageReadOnly: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
    }
    private val isExternalStorageAvailable: Boolean get() {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }

    private fun openFile(pickerInitialUri: Uri, requestCode: Int) {
        var typeOp = android.content.Intent.ACTION_CREATE_DOCUMENT
        if (requestCode != 2) {
            typeOp = android.content.Intent.ACTION_OPEN_DOCUMENT
        }
        var intent = Intent(typeOp).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/*"
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == _saveFile && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri ->
                // Perform operations on the document using its URI.
                try {
                    val fileOutPutStream = contentResolver.openOutputStream(uri)
                    for (dataModel: String in droidSettingViewModel.commands) {
                        if (dataModel != null) {
                            fileOutPutStream?.write(dataModel.toByteArray())
                            fileOutPutStream?.write("\n".toByteArray())
                        }
                    }
                    fileOutPutStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (requestCode == _loadFile && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            droidSettingViewModel.commands.clear()
            resultData?.data?.also { uri ->
                // Perform operations on the document using its URI.
                try {
                    val fileInputStream = contentResolver.openInputStream(uri)
                    var inputStreamReader = InputStreamReader(fileInputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    var text: String? = bufferedReader.readLine()
                    while (text != null) {
                        if (text != "")
                            droidSettingViewModel.commands.add(text + "\n")
                        text = bufferedReader.readLine()
                    }
                    fileInputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (requestCode == _appendFile && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri ->
                // Perform operations on the document using its URI.
                try {
                    val fileInputStream = contentResolver.openInputStream(uri)
                    var inputStreamReader = InputStreamReader(fileInputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    var text: String? = bufferedReader.readLine()
                    while (text != null) {
                        droidSettingViewModel.commands.add(text + "\n")
                        text = bufferedReader.readLine()
                    }
                    fileInputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            val fragment = DroidControlFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    /* create the menu */
    override fun onCreateOptionsMenu(menu: Menu) :Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "DroidConnectionFragment")
        return when (item.itemId) {
            R.id.droid_connection_wifi -> {
                val fragment =
                    DroidConnectionWiFiFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
                true
            }
            R.id.droid_connection_ble -> {
                val fragment =
                    DroidConnectionBleFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
                true
            }
            R.id.droid_settings_move -> {
                val fragment =
                    DroidSettingsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
                true
            }
            R.id.droid_settings_camera -> {
                val fragment =
                    DroidSettingsCameraFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
                true
            }
            R.id.droid_navigation_file_load -> {
                val myExternalFile = File(Environment.getExternalStoragePublicDirectory("Downloads/Droid"),"deploy.dat")
                openFile(myExternalFile!!.toUri(),_loadFile)
                true
            }
            R.id.droid_navigation_file_save -> {
                val myExternalFile = File(Environment.getExternalStoragePublicDirectory("Downloads/Droid"),"deploy.dat")
                openFile(myExternalFile!!.toUri(),_saveFile)
                true
            }
            R.id.droid_navigation_file_edit -> {
                val fragment = DroidNavigationFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
                    .addToBackStack(null).commit()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}