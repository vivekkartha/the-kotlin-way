package dev.vivek.smsapilab

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : Activity() {
    private lateinit var destinationInput: EditText
    private lateinit var messageInput: EditText
    private lateinit var simButtons: LinearLayout
    private lateinit var logView: TextView
    private val requestIds = AtomicInteger(100)
    private val history = StringBuilder()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    private val smsResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val simId = intent.getIntExtra("subId", -1)
            val testId = intent.getIntExtra("testId", -1)
            val event = if (intent.action == ACTION_SMS_SENT) "SENT" else "DELIVERED"
            val status = if (resultCode == Activity.RESULT_OK) "OK" else "FAILED (resultCode=$resultCode)"
            addLog("$event: test=$testId subId=$simId $status")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerResultCallbacks()
        buildScreen()
        if (permissionsGranted()) {
            showSimButtons()
        } else {
            requestRuntimePermissions()
        }
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)
        val screen = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(24))
            setBackgroundColor(Color.WHITE)
        }
        scroll.addView(screen)
        setContentView(scroll)

        screen.addView(text("SMS API Lab", 25, true))
        screen.addView(text("Send the same SMS using SIM 1, then SIM 2, and compare the device's Messaging app.", 14))

        screen.addView(text("Recipient phone number", 16, true))
        destinationInput = EditText(this).apply {
            hint = "+91..."
            inputType = InputType.TYPE_CLASS_PHONE
            setSingleLine(true)
        }
        screen.addView(destinationInput, fullWidth())

        screen.addView(text("SMS text", 16, true))
        messageInput = EditText(this).apply {
            setSingleLine(true)
            setText("SMS API Lab test")
        }
        screen.addView(messageInput, fullWidth())

        screen.addView(Button(this).apply {
            text = "Refresh SIMs / grant permissions"
            setOnClickListener {
                if (permissionsGranted()) showSimButtons() else requestRuntimePermissions()
            }
        }, fullWidth())

        simButtons = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        screen.addView(simButtons, fullWidth())

        screen.addView(text("Result log", 17, true))
        logView = text("No requests yet.", 13)
        logView.setTextIsSelectable(true)
        screen.addView(logView, fullWidth())

        screen.addView(Button(this).apply {
            text = "Copy log"
            setOnClickListener {
                val clipboard = getSystemService(ClipboardManager::class.java)
                clipboard.setPrimaryClip(ClipData.newPlainText("SMS API Lab log", history.toString()))
                Toast.makeText(this@MainActivity, "Log copied", Toast.LENGTH_SHORT).show()
            }
        }, fullWidth())
        screen.addView(text(
            "This app selects the outbound Android subscription. SMS thread grouping is controlled by the phone's Messaging app and must be checked there.",
            12
        ))
    }

    private fun showSimButtons() {
        simButtons.removeAllViews()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_MESSAGING)) {
            simButtons.addView(text("This device does not support SMS.", 14))
            return
        }
        try {
            val manager = getSystemService(SubscriptionManager::class.java)
            val subscriptions = manager.activeSubscriptionInfoList.orEmpty()
                .sortedBy { it.simSlotIndex }
            if (subscriptions.isEmpty()) {
                simButtons.addView(text("No active SIM subscriptions found.", 14))
                return
            }
            for (info in subscriptions) {
                val simLabel = "SIM ${info.simSlotIndex + 1}"
                val subId = info.subscriptionId
                simButtons.addView(text("$simLabel · ${info.displayName} · subId=$subId", 14))
                simButtons.addView(Button(this).apply {
                    text = "Send SMS via $simLabel"
                    setOnClickListener { sendSms(subId, simLabel) }
                }, fullWidth())
            }
            addLog("Loaded ${subscriptions.size} active SIM(s)")
        } catch (error: SecurityException) {
            simButtons.addView(text("READ_PHONE_STATE permission is required.", 14))
        }
    }

    private fun sendSms(subId: Int, simLabel: String) {
        if (!permissionsGranted()) {
            requestRuntimePermissions()
            return
        }
        val destination = destinationInput.text.toString().trim()
        val message = messageInput.text.toString()
        if (destination.isBlank() || message.isBlank()) {
            Toast.makeText(this, "Enter a recipient and SMS text", Toast.LENGTH_LONG).show()
            return
        }
        if (!destination.matches(Regex("^[+0-9][0-9 ]{5,19}$"))) {
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_LONG).show()
            return
        }

        val testId = requestIds.getAndIncrement()
        val sentIntent = Intent(ACTION_SMS_SENT).setPackage(packageName)
            .putExtra("subId", subId).putExtra("testId", testId)
        val deliveredIntent = Intent(ACTION_SMS_DELIVERED).setPackage(packageName)
            .putExtra("subId", subId).putExtra("testId", testId)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val sentPendingIntent = PendingIntent.getBroadcast(this, testId * 2, sentIntent, flags)
        val deliveredPendingIntent = PendingIntent.getBroadcast(this, testId * 2 + 1, deliveredIntent, flags)

        try {
            val smsManager = getSystemService(SmsManager::class.java)
                .createForSubscriptionId(subId)
            addLog("REQUEST: test=$testId $simLabel subId=$subId")
            smsManager.sendTextMessage(
                destination.replace(" ", ""),
                null,
                message,
                sentPendingIntent,
                deliveredPendingIntent
            )
        } catch (error: Exception) {
            addLog("ERROR: test=$testId subId=$subId ${error.javaClass.simpleName}: ${error.message}")
        }
    }

    private fun registerResultCallbacks() {
        val filter = IntentFilter().apply {
            addAction(ACTION_SMS_SENT)
            addAction(ACTION_SMS_DELIVERED)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(smsResultReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(smsResultReceiver, filter)
        }
    }

    private fun permissionsGranted(): Boolean =
        checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
        checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    private fun requestRuntimePermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE),
            PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (permissionsGranted()) showSimButtons()
            else addLog("Permissions denied. Enable SMS and Phone access in Settings.")
        }
    }

    private fun addLog(line: String) {
        history.append("[").append(timeFormat.format(Date())).append("] ")
            .append(line).append("\n")
        logView.text = history.toString()
    }

    private fun text(value: String, sp: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = sp.toFloat()
            setTextColor(Color.rgb(35, 39, 45))
            setPadding(0, dp(9), 0, dp(9))
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        }

    private fun fullWidth(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        unregisterReceiver(smsResultReceiver)
        super.onDestroy()
    }

    companion object {
        private const val PERMISSION_REQUEST = 40
        private const val ACTION_SMS_SENT = "dev.vivek.smsapilab.SMS_SENT"
        private const val ACTION_SMS_DELIVERED = "dev.vivek.smsapilab.SMS_DELIVERED"
    }
}
