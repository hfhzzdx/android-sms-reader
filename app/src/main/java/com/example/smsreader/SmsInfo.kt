package com.example.smsreader

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class SmsInfo(
    val id: Long = 0,
    val address: String = "",  // 发件人号码
    val body: String = "",     // 短信内容
    val date: Long = 0,        // 时间戳
    val type: Int = 1,         // 1=接收，2=发送
    val read: Boolean = false, // 是否已读
    val threadId: Long = 0     // 会话ID
) : Parcelable {
    
    // 格式化日期
    fun getFormattedDate(): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(date))
        } catch (e: Exception) {
            ""
        }
    }
    
    // 判断是否来自特定发件人
    fun isFromSender(sender: String): Boolean {
        return address.contains(sender) || address.replace(Regex("[^0-9]"), "") == sender
    }
    
    // 获取短内容预览
    fun getPreview(maxLength: Int = 30): String {
        return if (body.length <= maxLength) body else body.substring(0, maxLength) + "..."
    }
}
