package com.mywatcher.wearappandroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerCallback
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.lifecycle.lifecycleScope
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import com.mywatcher.wearappandroid.databinding.ActivityMainBinding
import com.google.android.gms.wearable.*
import com.mywatcher.wearappandroid.ui.run.RunActivity
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class PassiveDataService : PassiveListenerService() {
    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        // TODO: do something with dataPoints
        Log.d("DATAPOINTS 2", "I receive data here :p")
    }
}

class MainActivity : AppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private var activityContext: Context? = null
    // pour connecter les données avec le front
    private lateinit var binding: ActivityMainBinding
    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private var mobileDeviceConnected: Boolean = false
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"
    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null
    private lateinit var ambientController: AmbientModeSupport.AmbientController

    private fun handleReceiveDataMain(data: String) {
        try {
            if (data == "start") {
                val intent = Intent(this, RunActivity::class.java)
                startActivity(intent)
                printLogReceiveData(data)
            } else {
                printLogReceiveData(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        activityContext = this
        val healthClient = HealthServices.getClient(this /*context*/)
        val passiveMonitoringClient = healthClient.passiveMonitoringClient
        var supportsHeartRate: Boolean = false
        lifecycleScope.launchWhenCreated {
            val capabilities = passiveMonitoringClient.getCapabilitiesAsync().await()
            // Supported types for passive data collection
            supportsHeartRate =
                DataType.HEART_RATE_BPM in capabilities.supportedDataTypesPassiveMonitoring
            if (supportsHeartRate) {
                Log.d("victory heart rate", "JE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉJE SUIS CONNECTÉ")
                Log.d("set passive listener", "OK")
                val passiveListenerConfig = PassiveListenerConfig.builder()
                    .setDataTypes(setOf(DataType.HEART_RATE_BPM))
                    .build()
                Log.d("set passive listener callback", "OK")
                val passiveListenerCallback: PassiveListenerCallback = object : PassiveListenerCallback {
                    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                        // TODO: do something with dataPoints
                        Log.d("DATAPOINTS 1", "I receive data here :p")
                    }
                }

                Log.d("give parameters listener", "OK")
                passiveMonitoringClient.setPassiveListenerCallback(
                    passiveListenerConfig,
                    passiveListenerCallback
                )

                // to remove the listener
                //passiveMonitoringClient.clearPassiveListenerCallbackAsync()

                Log.d("don't know what you are", "WHO ARE YOU BRO ?")
                passiveMonitoringClient.setPassiveListenerServiceAsync(
                    PassiveDataService::class.java,
                    passiveListenerConfig
                )
            } else {
                Log.d("error heart rate", "cannot connect to data mon reuf je fais un long message pour le voir plus facilement haha qu'est ce qu'on rigole en faite, je vous emmerde tous.")
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
    }

    private fun firstHandleReceiveData(p0: MessageEvent) {
        try {
            val nodeId: String = p0.sourceNodeId.toString()
            val returnPayloadAck = wearableAppCheckPayloadReturnACK
            val payload: ByteArray = returnPayloadAck.toByteArray()
            val sendMessageTask =
                Wearable.getMessageClient(activityContext!!)
                    .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "Acknowledgement message successfully with payload : $returnPayloadAck"
            )

            messageEvent = p0
            mobileNodeUri = p0.sourceNodeId

            sendMessageTask.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG_MESSAGE_RECEIVED, "Message sent successfully")

                    val sbTemp = StringBuilder()
                    sbTemp.append("\nMobile device connected.")
                    Log.d("receive1", " $sbTemp")

                    mobileDeviceConnected = true

                    binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                    binding.deviceconnectionStatusTv.text = "Mobile device is connected"
                } else {
                    Log.d(TAG_MESSAGE_RECEIVED, "Message failed.")
                }
            }
        } catch (e: Exception) {
            Log.d(
                TAG_MESSAGE_RECEIVED,
                "Handled in sending message back to the sending node"
            )
            e.printStackTrace()
        }
    }

    private fun printLogReceiveData(data: String) {
        val sbTemp = StringBuilder()
        sbTemp.append("\n")
        sbTemp.append(data)
        sbTemp.append(" - (Received from mobile)")
        Log.d("receive1", " $sbTemp")
    }

    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(p0: MessageEvent) {
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            // contient données envoyé
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() A message from watch was received:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s1
            )

            if (messageEventPath.isNotEmpty() && messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                firstHandleReceiveData(p0)
            } else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {
                handleReceiveDataMain(s1)
            }
        } catch (e: Exception) {
            Log.d(TAG_MESSAGE_RECEIVED, "Handled in onMessageReceived")
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // fonction quand montre en "veille"
    override fun getAmbientCallback(): AmbientCallback = MyAmbientCallback()

    private inner class MyAmbientCallback : AmbientCallback() {
        override fun onEnterAmbient(ambientDetails: Bundle) {
            super.onEnterAmbient(ambientDetails)
        }

        override fun onUpdateAmbient() {
            super.onUpdateAmbient()
        }

        override fun onExitAmbient() {
            super.onExitAmbient()
        }
    }
}
