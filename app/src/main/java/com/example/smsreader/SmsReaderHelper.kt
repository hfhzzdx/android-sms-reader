package com.example.smsreader

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log

/**
 * 短信读取助手
 */
object SmsReaderHelper {
    
    private const val TAG = "SmsReaderHelper"
    
    /**
     * 读取12123短信
     * 支持发件人为12123401或以12123开头
     */
    fun readSmsFrom12123(context: Context): List<SmsInfo> {
        val smsList = mutableListOf<SmsInfo>()
        
        try {
            val uri = Telephony.Sms.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            )
            
            // 筛选发件人为12123401或以12123开头的短信
            val selection = "${Telephony.Sms.ADDRESS} = ? OR ${Telephony.Sms.ADDRESS} LIKE ?"
            val selectionArgs = arrayOf("12123401", "12123%")
            val sortOrder = "${Telephony.Sms.DATE} DESC"
            
            val cursor = context.contentResolver.query(
                uri, projection, selection, selectionArgs, sortOrder
            )
            
            cursor?.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                Log.d(TAG, "找到 ${it.count} 条符合条件的短信")
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex)
                    val body = it.getString(bodyIndex)
                    val date = it.getLong(dateIndex)
                    
                    // 解析短信内容
                    val parser = SmsParser()
                    val plateNumber = parser.extractPlateNumber(body)
                    val violation = parser.extractViolation(body)
                    
                    if (plateNumber.isNotEmpty()) {
                        val smsInfo = SmsInfo(
                            address = address,
                            body = body,
                            date = date,
                            plateNumber = plateNumber,
                            violation = violation
                        )
                        smsList.add(smsInfo)
                        
                        Log.d(TAG, "解析成功: 车牌=$plateNumber, 违法=$violation, 发件人=$address")
                    } else {
                        Log.d(TAG, "未提取到车牌号: $body")
                    }
                }
            }
            
            Log.d(TAG, "总共解析到 ${smsList.size} 条有效违章记录")
            
        } catch (e: Exception) {
            Log.e(TAG, "读取短信失败", e)
        }
        
        return smsList
    }
    
    /**
     * 获取最近N条违章记录
     */
    fun getRecentViolations(context: Context, limit: Int = 10): List<SmsInfo> {
        return readSmsFrom12123(context).take(limit)
    }
    
    /**
     * 获取指定车牌号的违章记录
     */
    fun getViolationsByPlateNumber(context: Context, plateNumber: String): List<SmsInfo> {
        return readSmsFrom12123(context)
            .filter { it.plateNumber == plateNumber }
    }
    
    /**
     * 获取今天的违章记录
     */
    fun getTodayViolations(context: Context): List<SmsInfo> {
        val today = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        
        return readSmsFrom12123(context)
            .filter { today - it.date < oneDay }
    }
    
    /**
     * 统计违章类型
     */
    fun getViolationStatistics(context: Context): Map<String, Int> {
        val violations = readSmsFrom12123(context)
        val statistics = mutableMapOf<String, Int>()
        
        violations.forEach { smsInfo ->
            val violationType = getViolationType(smsInfo.violation)
            statistics[violationType] = statistics.getOrDefault(violationType, 0) + 1
        }
        
        return statistics
    }
    
    /**
     * 根据违法行为描述获取类型
     */
    private fun getViolationType(violation: String): String {
        return when {
            violation.contains("未按规定停放") -> "违停"
            violation.contains("停车") -> "违停"
            violation.contains("超速") -> "超速"
            violation.contains("闯红灯") -> "闯红灯"
            violation.contains("违反禁令标志") -> "违反禁令"
            violation.contains("违反禁止标线") -> "违反标线"
            violation.contains("不系安全带") -> "安全带"
            violation.contains("开车打电话") -> "分心驾驶"
            else -> "其他"
        }
    }
    
    /**
     * 检查是否有新违章
     */
    fun hasNewViolations(context: Context, lastCheckTime: Long): Boolean {
        val newViolations = readSmsFrom12123(context)
            .filter { it.date > lastCheckTime }
        
        return newViolations.isNotEmpty()
    }
    
    /**
     * 获取违章记录摘要
     */
    fun getViolationSummary(context: Context): String {
        val violations = readSmsFrom12123(context)
        
        return if (violations.isEmpty()) {
            "暂无违章记录"
        } else {
            val todayCount = getTodayViolations(context).size
            val totalCount = violations.size
            
            "今日新增: $todayCount 条，总计: $totalCount 条"
        }
    }
}