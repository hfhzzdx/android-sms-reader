package com.example.smsreader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SmsReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "收到短信广播")
        
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") {
            return
        }
        
        val bundle = intent.extras ?: return
        val parser = SmsParser()
        
        try {
            val pdus = bundle.get("pdus") as? Array<*> ?: return
            val messages = arrayOfNulls<SmsMessage>(pdus.size)
            
            for (i in pdus.indices) {
                val pdu = pdus[i] as ByteArray
                messages[i] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SmsMessage.createFromPdu(pdu, bundle.getString("format"))
                } else {
                    @Suppress("DEPRECATION")
                    SmsMessage.createFromPdu(pdu)
                }
            }
            
            for (message in messages) {
                message?.let { sms ->
                    val sender = sms.displayOriginatingAddress ?: ""
                    val body = sms.displayMessageBody ?: ""
                    
                    Log.d(TAG, "收到短信 - 发件人: $sender, 内容: $body")
                    
                    // 检查是否为12123的违章短信
                    if (parser.isTrafficViolationSms(sender, body)) {
                        Log.d(TAG, "检测到12123违章短信")
                        
                        // 提取车牌号和违法行为
                        val plateNumber = parser.extractPlateNumber(body)
                        val violation = parser.extractViolation(body)
                        
                        Log.d(TAG, "提取结果 - 车牌: $plateNumber, 违法: $violation")
                        
                        if (plateNumber.isNotEmpty()) {
                            // 触发提醒
                            triggerAlert(context, plateNumber, violation, body)
                            
                            // 保存到数据库或显示通知
                            saveSmsRecord(context, sender, body, plateNumber, violation)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理短信失败", e)
        }
    }
    
    private fun triggerAlert(context: Context, plateNumber: String, violation: String, body: String) {
        // 创建提醒管理器
        val alertManager = AlertManager(context)
        
        // 震动提醒
        alertManager.vibrate()
        
        // 语音提醒
        val voiceContent = SmsParser().getVoiceAlertContent(plateNumber, violation)
        alertManager.speak(voiceContent)
        
        // 显示通知
        alertManager.showNotification(plateNumber, violation, body)
        
        // 在UI线程显示Toast
        android.os.Handler(context.mainLooper).post {
            Toast.makeText(
                context,
                "检测到违章通知！车牌: $plateNumber",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun saveSmsRecord(
        context: Context,
        sender: String,
        body: String,
        plateNumber: String,
        violation: String
    ) {
        // 这里可以保存到数据库或SharedPreferences
        // 暂时先记录到日志
        Log.i(TAG, "保存短信记录: 发件人=$sender, 车牌=$plateNumber, 违法=$violation")
        
        // 也可以发送广播给Activity更新UI
        val updateIntent = Intent("com.example.smsreader.SMS_UPDATED")
        updateIntent.putExtra("plate_number", plateNumber)
        updateIntent.putExtra("violation", violation)
        updateIntent.putExtra("body", body)
        context.sendBroadcast(updateIntent)
    }
}