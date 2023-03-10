package com.pokemonCompagnon.wearappandroid

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import com.pokemonCompagnon.wearappandroid.databinding.ActivityMainBinding
import com.google.android.gms.wearable.*
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private var activityContext: Context? = null
    private lateinit var binding: ActivityMainBinding
    private val TAG_MESSAGE_RECEIVED = "wear receive"
    private val TAG_MESSAGE_SEND = "wear send"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private var mobileDeviceConnected: Boolean = false
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"
    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private var pikachu: Pokemon = Pokemon("Pikachu")

    private fun sendData()
    {
        val nodeId: String = messageEvent?.sourceNodeId!!
        val text: String = pikachu.getHappiness().toString() + ";" + pikachu.getFood().toString() + ";" + pikachu.getEnergy().toString()
        val payload: ByteArray = text.toByteArray()
        val sendMessageTask =
            Wearable.getMessageClient(activityContext!!)
                .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
        binding.deviceConnectionStatusTv.visibility = View.GONE
        sendMessageTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG_MESSAGE_SEND, "Message sent successfully")
            } else {
                Log.d(TAG_MESSAGE_SEND, "Message failed.")
            }
        }
    }

    private fun handleReceiveDataMain(data: String)
    {
        try {
            binding.pokemonName.text = data
            pikachu.setName(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        activityContext = this
        ambientController = AmbientModeSupport.attach(this)

        binding.playButton.setOnClickListener {
            if (mobileDeviceConnected) {
                if (pikachu.getEnergy() == 0 || pikachu.getFood() == 0) {
                    Toast.makeText(
                        activityContext,
                        "No energy, need to sleep or eat !",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    pikachu.setHappiness(pikachu.getHappiness() + 1)
                    pikachu.setEnergy(pikachu.getEnergy() - 1)
                    pikachu.setFood(pikachu.getFood() - 1)
                    sendData()
                }
            }
        }

        binding.eatButton.setOnClickListener {
            if (mobileDeviceConnected) {
                pikachu.setFood(pikachu.getFood() + 1)
                sendData()
            }
        }

        binding.sleepButton.setOnClickListener {
            if (mobileDeviceConnected) {
                pikachu.setEnergy(pikachu.getEnergy() + 1)
                sendData()
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer)
    {
    }

    override fun onCapabilityChanged(p0: CapabilityInfo)
    {
    }

    private fun firstHandleReceiveData(p0: MessageEvent)
    {
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
                    binding.deviceConnectionStatusTv.visibility = View.GONE
                    binding.title.visibility = View.GONE
                    binding.pokemonAction.visibility = View.VISIBLE
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
    override fun onMessageReceived(p0: MessageEvent)
    {
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

    override fun onPause()
    {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume()
    {
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

    private inner class MyAmbientCallback : AmbientCallback()
    {
        override fun onEnterAmbient(ambientDetails: Bundle)
        {
            super.onEnterAmbient(ambientDetails)
        }

        override fun onUpdateAmbient()
        {
            super.onUpdateAmbient()
        }

        override fun onExitAmbient()
        {
            super.onExitAmbient()
        }
    }
}
