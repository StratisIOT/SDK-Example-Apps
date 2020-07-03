package com.example.sdkexample

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.stratisiot.stratissdk.StratisSDK
import com.stratisiot.stratissdk.constants.ServerEnvironment
import com.stratisiot.stratissdk.error.StratisError
import com.stratisiot.stratissdk.error.StratisErrorCode
import com.stratisiot.stratissdk.listener.DeviceActivationEvent
import com.stratisiot.stratissdk.listener.StratisDeviceAccessListener
import com.stratisiot.stratissdk.listener.StratisDeviceActivationListener
import com.stratisiot.stratissdk.listener.StratisDeviceDiscoveryListener
import com.stratisiot.stratissdk.model.Configuration
import com.stratisiot.stratissdk.model.lock.BLELock
import com.stratisiot.stratissdk.model.lock.StratisLock
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "ExampleApp"
private const val REQUEST_ENABLE_BT = 1
const val SETTINGS_REQUEST_CODE = 99
const val ACCESS_TOKEN = "ACCESS_TOKEN"
const val PROPERTY_ID = "PROPERTY_ID"
private const val REQUEST_ACCESS_LOCATION = 2

open class MainActivity : AppCompatActivity(), StratisDeviceAccessListener, StratisDeviceDiscoveryListener {
    private fun mainHandler(): Handler { return Handler(mainLooper) }
    private var stratisSDK: StratisSDK? = null
    private var serverEnvironment: ServerEnvironment? = null
    private var accessToken: String = ""
    private var propertyID: String = ""
    private lateinit var listView: ListView
    private lateinit var deviceListAdapter: DeviceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        listView = findViewById(R.id.device_list_view)
        deviceListAdapter = DeviceListAdapter(applicationContext)
        listView.adapter = deviceListAdapter

        toolbar.title = ""
        setSupportActionBar(toolbar)

        startStratis()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    putExtra(PROPERTY_ID, propertyID)
                }
                startActivityForResult(intent, SETTINGS_REQUEST_CODE)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "BLE ENABLED")
        } else if (requestCode == SETTINGS_REQUEST_CODE) {
            accessToken = data?.getStringExtra("accessToken") ?: ""
            propertyID = data?.getStringExtra("propertyID") ?: ""
            val serverEnv = data?.getStringExtra("serverEnvironment")
            if (serverEnv != null) {
                serverEnvironment = ServerEnvironment.valueOf(serverEnv)
            }
            startStratis()
        }
    }

    private fun startStratis() {
        val loggingMetadata = HashMap<String, String>()
        loggingMetadata["app"] = "SDK Example App"
        try {
            val pInfo = applicationContext.packageManager.getPackageInfo(packageName, 0)
            loggingMetadata["version"] = pInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val configuration = Configuration(
            serverEnvironment ?: ServerEnvironment.DEV,
            accessToken,
            true,
            loggingMetadata,
            propertyID
        )
        stratisSDK = StratisSDK(this@MainActivity, configuration)
        stratisSDK?.deviceAccessListener = this@MainActivity
        stratisSDK?.deviceDiscoveryListener = this@MainActivity
    }

    private fun showToast(message: String) {
        mainHandler().post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    //region Get Locks

    fun fetchAccessibleDevices(view: View) {
        val btn = findViewById<Button>(R.id.fetch_accessible_devices_button)
        btn.isEnabled = false
        btn.alpha = 0.5f

        deviceListAdapter.clearDevices()
        stratisSDK?.fetchAccessibleDevices()
    }

    override fun stratisDeviceAccessRequestCompleted(
        stratisSDK: StratisSDK,
        devices: Collection<StratisLock>,
        error: StratisError?
    ) {
        enableFetchAccessibleDevicesButton()
        devices.forEach {
            deviceListAdapter.addDevice(it)
        }
        error?.let {
            showToast(it.debugDescription)
        }
    }

    //endregion

    private fun enableFetchAccessibleDevicesButton() {
        mainHandler().post {
            val btn = findViewById<Button>(R.id.fetch_accessible_devices_button)
            btn.isEnabled = true
            btn.alpha = 1.0f
        }
    }

    private fun enableDiscoverActionableDevicesButton() {
        mainHandler().post {
            val btn = findViewById<Button>(R.id.discover_actionable_devices_button)
            btn.isEnabled = true
            btn.alpha = 1.0f
        }
    }

    fun activateDevice(view: View, device: StratisLock) {
        if (!device.isActionable) {
            return
        }

        mainHandler().post { view.alpha = 0.5f }

        device.listener = object : StratisDeviceActivationListener {
            override fun stratisDeviceActivationDidPostEvent(
                device: StratisLock,
                event: DeviceActivationEvent,
                error: StratisError?
            ) {
                when (event) {
                    DeviceActivationEvent.STARTED -> showToast("Activating ${device.name}")
                    DeviceActivationEvent.PRESENT_DORMA_KABA_INSTRUCTIONS -> showToast("Hold your phone up to the lock")
                    DeviceActivationEvent.COMPLETE -> {
                        error?.let {
                            showToast(error.debugMessage ?: "ERROR")
                            mainHandler().post { view.alpha = 1.0f }
                            return@let
                        }
                        showToast("Successfully unlocked: ${device.name}")
                        mainHandler().post { view.alpha = 1.0f }
                    }
                }
            }
        }
        device.activate()
    }

    //region Scanning and scan listeners.

    fun startDicovery(view: View) {
        val btn = findViewById<Button>(R.id.discover_actionable_devices_button)
        btn.isEnabled = false
        btn.alpha = 0.5f
        stratisSDK?.discoverActionableDevices(deviceListAdapter.devices)
    }

    override fun stratisDiscoveredDevices(stratisSDK: StratisSDK, devices: Collection<StratisLock>) {
        deviceListAdapter.doNotifyDataSetChanged()
    }

    override fun stratisDiscoveryEncounteredError(stratisSDK: StratisSDK, error: StratisError) {
        if (error.code == StratisErrorCode.BLUETOOTH_POWERED_OFF) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        if (error.code == StratisErrorCode.BLUETOOTH_PERMISSIONS_REQUIRED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_LOCATION
            )
        }
    }

    override fun stratisDiscoveryCompleted(stratisSDK: StratisSDK) {
        enableDiscoverActionableDevicesButton()
    }

    //endregion

    //region DeviceListAdapter

    inner class DeviceListAdapter(private val context: Context) : BaseAdapter() {
        val devices = ArrayList<StratisLock>()

        fun clearDevices() {
            devices.clear()
            doNotifyDataSetChanged()
        }

        fun addDevice(device: StratisLock) {
            devices.add(device)
            doNotifyDataSetChanged()
        }

        override fun getItem(position: Int): StratisLock = devices[position]

        override fun getItemId(position: Int): Long = devices[position].identifier.toLong()

        override fun getCount(): Int = devices.size

        internal fun doNotifyDataSetChanged() {
            mainHandler().post { notifyDataSetChanged() }
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var mConvertView = convertView
            if (mConvertView == null) {
                val inflater = LayoutInflater.from(this.context)
                mConvertView = inflater.inflate(R.layout.listview_item, parent, false)
            }
            val device = getItem(position)
            val textView = mConvertView?.findViewById(R.id.lock_item) as TextView
            var text = device.name
            if (device is BLELock) {
                if (device.rssi != null) {
                    text += " " + device.rssi
                }
            }
            textView.text = text
            mConvertView.setOnClickListener { this@MainActivity.activateDevice(it, device) }
            if (device.isActionable) {
                textView.alpha = 1.0f
            } else {
                textView.alpha = 0.5f
                mConvertView.setOnClickListener(null)
            }
            return mConvertView
        }
    }

    //endregion
}
