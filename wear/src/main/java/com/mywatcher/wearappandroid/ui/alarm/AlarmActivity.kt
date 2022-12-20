package com.mywatcher.wearappandroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.ambient.AmbientModeSupport.AmbientCallback
import com.google.android.gms.tasks.Task
import com.mywatcher.wearappandroid.databinding.ActivityAlarmBinding
import com.google.android.gms.wearable.*
import com.mywatcher.wearappandroid.ui.run.RunActivity
import java.nio.charset.StandardCharsets

class AlarmActivity : AppCompatActivity(), AmbientModeSupport.AmbientCallbackProvider,
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private var activityContext: Context? = null
    // pour connecter les données avec le front
    private lateinit var binding: ActivityAlarmBinding
    private val TAG_MESSAGE_RECEIVED = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private var mobileDeviceConnected: Boolean = false
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"
    private var messageEvent: MessageEvent? = null
    private var mobileNodeUri: String? = null
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    private lateinit var rocketAnimation: AnimationDrawable

    private fun printLogSendDataSuccess(text: String) {
        Log.d("send1", "Message sent successfully")
        val sbTemp = StringBuilder()
        sbTemp.append("\n")
        sbTemp.append(text)
        sbTemp.append(" (Sent to mobile)")
        Log.d("receive1", " $sbTemp")
    }

    private fun printLogSendDataFail() {
        Log.d("send1", "Message failed.")
    }

    private fun sendData(text: String): Task<Int> {
        val nodeId: String = messageEvent?.sourceNodeId!!
        val payload: ByteArray = text.toByteArray()
        return Wearable.getMessageClient(activityContext!!)
            .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)
    }

    private fun handleReceiveDataMain(data: String) {
        try {
            if (data == "stop") {
                printLogReceiveData(data)
                val intent = Intent(this, RunActivity::class.java)
                startActivity(intent)
            } else {
                printLogReceiveData(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        activityContext = this
        ambientController = AmbientModeSupport.attach(this)
        // fonction pour animer
        val rocketImage = findViewById<ImageView>(binding.padou.id).apply {
            setBackgroundResource(R.drawable.animation_list)
            rocketAnimation = background as AnimationDrawable
        }
        rocketAnimation.start()
        rocketImage.setOnClickListener { rocketAnimation.start() }
        binding.stopAlarmButton.setOnClickListener {
            if (mobileDeviceConnected) {
                val text: String = "stop"

                val sendMessageTask = sendData(text)
                sendMessageTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        printLogSendDataSuccess(text)
                        val intent = Intent(this, RunActivity::class.java)
                        startActivity(intent)
                    } else {
                        printLogSendDataFail()
                    }
                }
            }
        }

        binding.stopAlarmButtonSecond.setOnClickListener {
            if (mobileDeviceConnected) {
                val text: String = "stop"

                val sendMessageTask = sendData(text)
                sendMessageTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        printLogSendDataSuccess(text)
                        val intent = Intent(this, RunActivity::class.java)
                        startActivity(intent)
                    } else {
                        printLogSendDataFail()
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

                    val sbTemp = StringBuilder()
                    sbTemp.append("\nMobile device connected.")
                    Log.d("receive1", " $sbTemp")

                    mobileDeviceConnected = true

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
