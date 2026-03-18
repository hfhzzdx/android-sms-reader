package com.example.smsreader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Locale

class AlertManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AlertManager"
        private const val NOTIFICATION_CHANNEL_ID = "traffic_violation_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "违章提醒"
        private const val NOTIFICATION_ID = 1001
        
        // 震动模式：等待0.5秒，震动1秒，等待0.5秒，震动1秒
        private val VIBRATION_PATTERN = longArrayOf(500, 1000, 500, 1000)
    }
    
    private var vibrator: Vibrator? = null
    private var tts: TextToSpeech? = null
    private var notificationManager: NotificationManager? = null
    private var isTtsInitialized = false
    
    init {
        initialize()
    }
    
    private fun initialize() {
        // 初始化震动器
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        
        // 初始化语音合成
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.CHINA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "中文TTS不支持")
                } else {
                    isTtsInitialized = true
                    Log.d(TAG, "TTS初始化成功")
                }
            } else {
                Log.e(TAG, "TTS初始化失败")
            }
        }
        
        // 初始化通知管理器
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        createNotificationChannel()
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "交通违章提醒通知"
                enableLights(true)
                enableVibration(true)
                
                // 设置声音
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    /**
     * 触发震动提醒
     */
    fun vibrate() {
        vibrator?.let { vibrator ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 使用新的震动API
                val effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, 0)
                vibrator.vibrate(effect)
            } else {
                // 旧版本API
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_PATTERN, -1)
            }
            Log.d(TAG, "震动提醒已触发")
        } ?: run {
            Log.w(TAG, "震动器不可用")
        }
    }
    
    /**
     * 语音播报提醒
     */
    fun speak(text: String) {
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "traffic_alert")
            Log.d(TAG, "语音播报: $text")
        } else {
            Log.w(TAG, "TTS未初始化，无法播报语音")
        }
    }
    
    /**
     * 显示通知
     */
    fun showNotification(plateNumber: String, violation: String, body: String) {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 交通违章提醒")
            .setContentText("车牌 ${plateNumber} 有新的违章记录")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("车牌号: $plateNumber\n违法行为: $violation\n$body")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        
        notificationManager?.notify(NOTIFICATION_ID, builder.build())
        Log.d(TAG, "通知已显示: 车牌=$plateNumber")
    }
    
    /**
     * 停止语音播报
     */
    fun stopSpeaking() {
        tts?.stop()
    }
    
    /**
     * 停止震动
     */
    fun stopVibration() {
        vibrator?.cancel()
    }
    
    /**
     * 释放资源
     */
    fun release() {
        stopSpeaking()
        stopVibration()
        tts?.shutdown()
    }
}