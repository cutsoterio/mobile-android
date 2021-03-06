package zw.co.soterio.monitor.ble

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import zw.co.guava.soterio.Constants
import zw.co.guava.soterio.Soterio
import zw.co.guava.soterio.core.classes.CentralLog
import zw.co.guava.soterio.core.classes.SoterioStorage
import zw.co.guava.soterio.core.classes.Utils
import zw.co.guava.soterio.db.entity.EntityEncounter
import java.util.*
import kotlin.properties.Delegates


class GattServer (private val context: Context, val scope: CoroutineScope) {
    private val TAG = "GattServer"

    private val mBluetoothManager:BluetoothManager = context.getSystemService(Service.BLUETOOTH_SERVICE) as BluetoothManager
    private lateinit var mGattServer: BluetoothGattServer
    val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
    private var serviceUUID: UUID by Delegates.notNull()


    init {
        this.serviceUUID = Constants.MONITOR_SERVICE_UUID
    }

     fun setupServer() {
         mGattServer = mBluetoothManager.openGattServer(context, gattServerCallback)
         Log.d("GattServer", "GattServer has been launched")

         val service = BluetoothGattService(Constants.MONITOR_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

         val identityCharacteristic = BluetoothGattCharacteristic(
             Constants.MONITOR_SERVICE_UUID,
             BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
             BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
         )
         service.addCharacteristic(identityCharacteristic)
         mGattServer.addService(service)

         Log.d("GattServer", "Launching Gatt Server")
    }

    private val gattServerCallback = object: BluetoothGattServerCallback() {
        val writeDataPayload: MutableMap<String, ByteArray> = HashMap()
        val readPayloadMap: MutableMap<String, ByteArray> = HashMap()

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    CentralLog.i(TAG, "${device.address} Connected to local GATT server")

                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    CentralLog.i(TAG, "${device.address} Disconnected from local GATT server.")
                    readPayloadMap.remove(device.address)
                    device.let {
                        Utils.broadcastDeviceConnected(context, device)
                    }

                }

                else -> {
                    CentralLog.i(TAG, "Connection status: $newState - ${device.address}")
                }
            }
        }


        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            Log.d("GattServer", "Added service to gatt $service")

        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            CentralLog.i(TAG, "onCharacteristicReadRequest")

            scope.launch {
                val streetPassStorage = SoterioStorage(context)
                val identifier = streetPassStorage.getActiveToken().Identifier

                mGattServer.sendResponse(device, requestId, 0, offset, identifier.toByteArray())

            }

        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)
            CentralLog.i(TAG, "onCharacteristicExecuteWriteRequest - - preparedWrite")

        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            CentralLog.i(TAG, "onCharacteristicWriteRequest from - $requestId - $offset with data ${String(value!!)}")
            val streetPassStorage = SoterioStorage(context)

            val encounter: EntityEncounter = EntityEncounter(
                Identifier = String(value),
                rssi = 0,
                txPower = 0,
                timestamp = System.currentTimeMillis()
            )

            // TODO - Save the encounter
            scope.launch {
                streetPassStorage.saveEncounter(encounter)
                Soterio.changeStreamSync!!.emmitEncounter(encounter)
                CentralLog.d(TAG, "Event:DatabaseSave:Success: ${encounter.Identifier}")
            }

            mGattServer.sendResponse(device, requestId, 0, offset, "XX".toByteArray())

        }


    }




}