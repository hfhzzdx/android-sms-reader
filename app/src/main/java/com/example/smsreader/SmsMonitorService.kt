package com.example.smsreader

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class SmsMonitorService : Service() {
    
    companion object {
        private const val TAG = "SmsMonitorService"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "sms_monitor_channel"
        
        fun getStartIntent(context: Context): Intent {
            return Intent(context, SmsMonitorService::class.java)
        }
    }
    
    private lateinit var alertManager: AlertManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "短信监控服务创建")
        alertManager = AlertManager(this)
        
        // 启动前台服务
        startForegroundService()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "短信监控服务启动")
        
        // 这里可以添加定期检查短信的逻辑
        // 例如：每5分钟检查一次12123的短信
        
        return START_STICKY
    }
    
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("12123短信监控")
            .setContentText("正在监控12123的违章短信")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "前台服务已启动")
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "短信监控服务销毁")
        alertManager.release()
        
        // 可以在这里重新启动服务，保持后台监控
        val restartIntent = Intent(this, SmsMonitorService::class.java)
        startService(restartIntent)
    }
    
    /**
     * 定期检查短信（示例方法）
     */
    private fun checkSmsPeriodically() {
        // 这里可以实现定期检查短信的逻辑
        // 注意：频繁读取短信可能影响性能
        // 建议只在收到广播时处理，而不是定期轮询
    }
}