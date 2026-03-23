package com.example.smsreader.model

/**
 * 短信信息数据类
 */
data class SmsInfo(
    val address: String,      // 发件人
    val body: String,         // 短信内容
    val date: Long,           // 时间戳
    val plateNumber: String,  // 车牌号
    val violation: String     // 违法行为
) {
    
    /**
     * 获取格式化时间
     */
    fun getFormattedTime(): String {
        return com.example.smsreader.TimeFormatter.formatToChineseDateTime(date)
    }
    
    /**
     * 获取相对时间
     */
    fun getRelativeTime(): String {
        return com.example.smsreader.TimeFormatter.formatToRelativeTime(date)
    }
    
    /**
     * 获取智能时间格式
     */
    fun getSmartTime(): String {
        return com.example.smsreader.TimeFormatter.formatSmart(date)
    }
    
    /**
     * 判断是否为今天
     */
    fun isToday(): Boolean {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        return now - date < oneDay
    }
    
    /**
     * 判断是否为近期（7天内）
     */
    fun isRecent(): Boolean {
        val now = System.currentTimeMillis()
        val sevenDays = 7 * 24 * 60 * 60 * 1000L
        return now - date < sevenDays
    }
    
    /**
     * 获取违章严重程度
     */
    fun getSeverity(): Severity {
        return when {
            violation.contains("超速") -> Severity.HIGH
            violation.contains("闯红灯") -> Severity.HIGH
            violation.contains("酒驾") -> Severity.HIGH
            violation.contains("醉驾") -> Severity.HIGH
            violation.contains("未按规定停放") -> Severity.MEDIUM
            violation.contains("停车") -> Severity.MEDIUM
            violation.contains("违反禁令标志") -> Severity.MEDIUM
            violation.contains("违反禁止标线") -> Severity.MEDIUM
            violation.contains("不系安全带") -> Severity.LOW
            violation.contains("开车打电话") -> Severity.LOW
            else -> Severity.LOW
        }
    }
    
    /**
     * 获取违章类型
     */
    fun getViolationType(): ViolationType {
        return when {
            violation.contains("未按规定停放") -> ViolationType.PARKING
            violation.contains("停车") -> ViolationType.PARKING
            violation.contains("超速") -> ViolationType.SPEEDING
            violation.contains("闯红灯") -> ViolationType.RED_LIGHT
            violation.contains("违反禁令标志") -> ViolationType.TRAFFIC_SIGN
            violation.contains("违反禁止标线") -> ViolationType.TRAFFIC_MARKING
            violation.contains("不系安全带") -> ViolationType.SEAT_BELT
            violation.contains("开车打电话") -> ViolationType.PHONE
            else -> ViolationType.OTHER
        }
    }
    
    /**
     * 获取违章图标
     */
    fun getViolationIcon(): String {
        return when (getViolationType()) {
            ViolationType.PARKING -> "🅿️"
            ViolationType.SPEEDING -> "🚗💨"
            ViolationType.RED_LIGHT -> "🚦"
            ViolationType.TRAFFIC_SIGN -> "🚸"
            ViolationType.TRAFFIC_MARKING -> "🛣️"
            ViolationType.SEAT_BELT -> "🦺"
            ViolationType.PHONE -> "📱"
            ViolationType.OTHER -> "⚠️"
        }
    }
    
    /**
     * 获取违章颜色资源ID
     */
    fun getViolationColorResId(): Int {
        return when (getSeverity()) {
            Severity.HIGH -> com.example.smsreader.R.color.violation_serious
            Severity.MEDIUM -> com.example.smsreader.R.color.violation_normal
            Severity.LOW -> com.example.smsreader.R.color.violation_minor
        }
    }
}

/**
 * 违章严重程度枚举
 */
enum class Severity {
    HIGH,   // 严重
    MEDIUM, // 中等
    LOW     // 轻微
}

/**
 * 违章类型枚举
 */
enum class ViolationType {
    PARKING,        // 违停
    SPEEDING,       // 超速
    RED_LIGHT,      // 闯红灯
    TRAFFIC_SIGN,   // 违反禁令标志
    TRAFFIC_MARKING,// 违反禁止标线
    SEAT_BELT,      // 不系安全带
    PHONE,          // 开车打电话
    OTHER           // 其他
}