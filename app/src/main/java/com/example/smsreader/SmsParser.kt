package com.example.smsreader

import android.util.Log
import java.util.regex.Pattern

class SmsParser {
    
    companion object {
        private const val TAG = "SmsParser"
        
        // 车牌号正则表达式（匹配中文省份简称+字母+数字组合）
        private val PLATE_NUMBER_PATTERN = Pattern.compile("([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5})")
        
        // 违法行为关键词
        private val VIOLATION_KEYWORDS = listOf(
            "未按规定停放",
            "违章停车",
            "违法停车",
            "违停",
            "违反禁令标志",
            "违反禁止标线",
            "闯红灯",
            "超速",
            "不按导向车道行驶",
            "违反信号灯"
        )
    }
    
    /**
     * 从短信内容中提取车牌号
     */
    fun extractPlateNumber(smsBody: String): String {
        return try {
            val matcher = PLATE_NUMBER_PATTERN.matcher(smsBody)
            if (matcher.find()) {
                matcher.group(1) ?: ""
            } else {
                // 尝试其他格式的车牌号
                extractPlateNumberFallback(smsBody)
            }
        } catch (e: Exception) {
            Log.e(TAG, "提取车牌号失败", e)
            ""
        }
    }
    
    /**
     * 备用方法提取车牌号
     */
    private fun extractPlateNumberFallback(smsBody: String): String {
        // 尝试匹配 "豫A" 开头的格式
        val patterns = listOf(
            Pattern.compile("([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]\\s*[A-Z0-9]{5})"),
            Pattern.compile("([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,6})"),
            Pattern.compile("车牌[：:]\\s*([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,6})")
        )
        
        patterns.forEach { pattern ->
            val matcher = pattern.matcher(smsBody)
            if (matcher.find()) {
                return matcher.group(1)?.replace("\\s".toRegex(), "") ?: ""
            }
        }
        
        return ""
    }
    
    /**
     * 从短信内容中提取违法行为
     * 1. 优先提取『』符号包裹的内容
     * 2. 如果没有『』符号，检查是否包含"未按规定停放"或"停车"
     * 3. 如果包含，则解析为"违法停车"
     */
    fun extractViolation(smsBody: String): String {
        return try {
            // 1. 首先尝试提取『』符号包裹的内容
            val doubleAngleBracketViolation = extractViolationFromDoubleAngleBrackets(smsBody)
            if (doubleAngleBracketViolation.isNotEmpty()) {
                return doubleAngleBracketViolation
            }
            
            // 2. 如果没有『』符号，检查是否包含特定关键词
            if (containsParkingKeywords(smsBody)) {
                return "违法停车"
            }
            
            // 3. 最后尝试其他提取方法
            extractViolationFallback(smsBody)
        } catch (e: Exception) {
            Log.e(TAG, "提取违法行为失败", e)
            "未知违法行为"
        }
    }
    
    /**
     * 提取违法行为上下文
     */
    private fun extractViolationContext(smsBody: String, keyword: String): String {
        val index = smsBody.indexOf(keyword)
        val start = maxOf(0, index - 20)
        val end = minOf(smsBody.length, index + keyword.length + 30)
        
        return smsBody.substring(start, end).trim()
    }
    
    /**
     * 专门提取『』符号包裹的内容
     * 只提取『』符号，不提取其他括号
     */
    private fun extractViolationFromDoubleAngleBrackets(smsBody: String): String {
        // 只匹配『』符号
        val doubleAngleBracketPattern = Pattern.compile("『([^』]+)』")
        val matcher = doubleAngleBracketPattern.matcher(smsBody)
        
        if (matcher.find()) {
            val content = matcher.group(1)?.trim() ?: ""
            if (content.isNotEmpty()) {
                return content
            }
        }
        
        return ""
    }
    
    /**
     * 检查是否包含停车相关关键词
     */
    private fun containsParkingKeywords(smsBody: String): Boolean {
        val parkingKeywords = listOf(
            "未按规定停放",
            "停车"
        )
        
        return parkingKeywords.any { smsBody.contains(it) }
    }
    
    /**
     * 备用提取方法（当没有『』符号且没有停车关键词时使用）
     */
    private fun extractViolationFallback(smsBody: String): String {
        // 1. 尝试其他中文括号
        val otherBrackets = listOf(
            Pattern.compile("【([^】]+)】"),  // 【内容】
            Pattern.compile("「([^」]+)」"),  // 「内容」
            Pattern.compile("《([^》]+)》"),  // 《内容》
            Pattern.compile("（([^）]+)）")   // （内容）
        )
        
        for (pattern in otherBrackets) {
            val matcher = pattern.matcher(smsBody)
            if (matcher.find()) {
                val content = matcher.group(1)?.trim() ?: ""
                if (content.isNotEmpty()) {
                    return content
                }
            }
        }
        
        // 2. 查找其他违法行为关键词
        VIOLATION_KEYWORDS.forEach { keyword ->
            if (smsBody.contains(keyword)) {
                return extractViolationContext(smsBody, keyword)
            }
        }
        
        // 3. 尝试提取"已被记录"附近的内容
        val recordIndex = smsBody.indexOf("已被记录")
        if (recordIndex > 0) {
            val start = maxOf(0, recordIndex - 50)
            val end = minOf(smsBody.length, recordIndex + 10)
            return smsBody.substring(start, end).trim()
        }
        
        return "未明确违法行为"
    }
    
    /**
     * 判断内容是否可能是违法行为描述
     */
    private fun isLikelyViolation(content: String): Boolean {
        // 违法行为关键词
        val violationIndicators = listOf(
            "未按规定停放", "违章停车", "违法停车", "违停",
            "违反禁令标志", "违反禁止标线", "闯红灯", "超速",
            "不按导向车道", "违反信号灯", "逆行", "压线",
            "不系安全带", "开车打电话", "酒驾", "醉驾",
            "无证驾驶", "遮挡号牌", "超载", "超员"
        )
        
        // 长度检查：违法行为描述通常在2-20个字符之间
        if (content.length < 2 || content.length > 20) {
            return false
        }
        
        // 检查是否包含违法行为关键词
        return violationIndicators.any { content.contains(it) }
    }
    
    /**
     * 从"已被记录"附近提取违法行为
     */
    private fun extractViolationFromRecord(smsBody: String): String {
        val recordIndex = smsBody.indexOf("已被记录")
        if (recordIndex > 0) {
            val start = maxOf(0, recordIndex - 50)
            val end = minOf(smsBody.length, recordIndex + 10)
            return smsBody.substring(start, end).trim()
        }
        
        return "未明确违法行为"
    }
    
    /**
     * 判断是否为12123的违章短信
     * 支持发件人为12123401或以12123开头
     */
    fun isTrafficViolationSms(sender: String, body: String): Boolean {
        // 检查发件人是否为12123401或以12123开头
        if (sender != "12123401" && !sender.startsWith("12123")) {
            return false
        }
        
        // 检查内容是否包含关键词
        val keywords = listOf(
            "交警", "违章", "违法", "记录", "处罚", "驶离", "拖移", "车牌", "车辆"
        )
        
        return keywords.any { body.contains(it) }
    }
    
    /**
     * 获取语音提醒内容
     */
    fun getVoiceAlertContent(plateNumber: String, violation: String): String {
        return if (plateNumber.isNotEmpty()) {
            "您的车辆${plateNumber}未按规定停放已被记录，请立即驶离。"
        } else {
            "您的车辆未按规定停放已被记录，请立即驶离。"
        }
    }
}