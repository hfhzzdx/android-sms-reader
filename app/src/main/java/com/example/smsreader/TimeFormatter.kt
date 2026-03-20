package com.example.smsreader

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 时间格式化工具类
 * 将时间戳转换为中文格式的时间字符串
 */
object TimeFormatter {
    
    /**
     * 将时间戳转换为中文格式的日期时间字符串
     * 格式：yyyy年MM月dd日 HH:mm:ss
     */
    fun formatToChineseDateTime(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA)
            formatter.format(date)
        } catch (e: Exception) {
            "时间格式错误"
        }
    }
    
    /**
     * 将时间戳转换为相对时间描述
     * 例如：刚刚、5分钟前、1小时前、昨天、前天等
     */
    fun formatToRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> {
                val minutes = diff / (60 * 1000)
                "${minutes}分钟前"
            }
            diff < 24 * 60 * 60 * 1000 -> {
                val hours = diff / (60 * 60 * 1000)
                "${hours}小时前"
            }
            diff < 2 * 24 * 60 * 60 * 1000 -> "昨天"
            diff < 3 * 24 * 60 * 60 * 1000 -> "前天"
            else -> formatToChineseDateTime(timestamp)
        }
    }
    
    /**
     * 将时间戳转换为简洁的中文日期格式
     * 格式：MM月dd日 HH:mm
     */
    fun formatToShortChineseDateTime(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
            formatter.format(date)
        } catch (e: Exception) {
            "时间错误"
        }
    }
    
    /**
     * 将时间戳转换为只显示时间的格式
     * 格式：HH:mm:ss
     */
    fun formatToTimeOnly(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
            formatter.format(date)
        } catch (e: Exception) {
            "时间错误"
        }
    }
    
    /**
     * 将时间戳转换为只显示日期的格式
     * 格式：yyyy年MM月dd日
     */
    fun formatToDateOnly(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)
            formatter.format(date)
        } catch (e: Exception) {
            "日期错误"
        }
    }
    
    /**
     * 智能格式化时间，根据时间远近选择不同的格式
     */
    fun formatSmart(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 24 * 60 * 60 * 1000 -> formatToRelativeTime(timestamp)
            diff < 7 * 24 * 60 * 60 * 1000 -> formatToShortChineseDateTime(timestamp)
            else -> formatToDateOnly(timestamp)
        }
    }
}