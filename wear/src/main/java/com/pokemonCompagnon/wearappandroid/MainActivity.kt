package com.pokemonCompagnon.wearappandroid

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import com.google.android.gms.tasks.Task
import com.pokemonCompagnon.wearappandroid.databinding.ActivityMainBinding
import com.google.android.gms.wearable.*
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private var activityContext: Context? = null
    private lateinit var binding: ActivityMainBinding
    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private var mobileDeviceConnected: Boolean = false
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"
    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private var count: Int = 0

    private fun sendData(text: String): Task<Int> {
        val nodeId: String = messageEvent?.sourceNodeId!!
        val payload: ByteArray = text.toByteArray()
        return Wearable.getMessageClient(activityContext!!)
            .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
    }

    private fun handleReceiveDataMain(data: String) {
        try {
            binding.counter.text = data
            if (data == "start") {
                Log.d("wear receive", " $data")
            } else {
                Log.d("wear receive", " $data")
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
        ambientController = AmbientModeSupport.attach(this)

        binding.playButton.setOnClickListener {
            if (mobileDeviceConnected) {
                val nodeId: String = messageEvent?.sourceNodeId!!
                ++count
                val text: String = count.toString()
                val payload: ByteArray = text.toByteArray()
                val sendMessageTask =
                    Wearable.getMessageClient(activityContext!!)
                        .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
                binding.deviceconnectionStatusTv.visibility = View.GONE
                sendMessageTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("wear send", "Message sent successfully")
                    } else {
                        Log.d("wear send", "Message failed.")
                    }
                }
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
                    mobileDeviceConnected = true
                    binding.deviceconnectionStatusTv.visibility = View.GONE
                    binding.title.visibility = View.GONE
                    binding.pokemonAction.visibility = View.VISIBLE
                    //binding.counter.visibility = View.VISIBLE
                    //binding.countButton.visibility = View.VISIBLE
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

    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(p0: MessageEvent) {
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

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
