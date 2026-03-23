package com.example.smsreader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smsreader.R
import com.example.smsreader.TimeFormatter
import com.example.smsreader.model.SmsInfo

/**
 * 违章记录适配器
 */
class ViolationAdapter(
    private var violations: List<SmsInfo> = emptyList(),
    private val onItemClick: (SmsInfo) -> Unit = {}
) : RecyclerView.Adapter<ViolationAdapter.ViolationViewHolder>() {

    /**
     * 更新数据
     */
    fun updateData(newViolations: List<SmsInfo>) {
        violations = newViolations
        notifyDataSetChanged()
    }

    /**
     * 获取数据数量
     */
    override fun getItemCount(): Int = violations.size

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViolationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_violation_record, parent, false)
        return ViolationViewHolder(view)
    }

    /**
     * 绑定数据
     */
    override fun onBindViewHolder(holder: ViolationViewHolder, position: Int) {
        val violation = violations[position]
        holder.bind(violation)
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClick(violation)
        }
    }

    /**
     * ViewHolder类
     */
    class ViolationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val tvPlateNumber: TextView = itemView.findViewById(R.id.tv_plate_number)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvViolation: TextView = itemView.findViewById(R.id.tv_violation)
        private val tvContentPreview: TextView = itemView.findViewById(R.id.tv_content_preview)
        private val tvSender: TextView = itemView.findViewById(R.id.tv_sender)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        /**
         * 绑定数据到视图
         */
        fun bind(smsInfo: SmsInfo) {
            // 车牌号
            tvPlateNumber.text = smsInfo.plateNumber
            
            // 时间（使用相对时间）
            val relativeTime = TimeFormatter.formatToRelativeTime(smsInfo.date)
            tvTime.text = relativeTime
            
            // 违法行为
            tvViolation.text = smsInfo.violation
            
            // 根据违法类型设置颜色
            val violationColor = when {
                smsInfo.violation.contains("未按规定停放") -> R.color.violation_normal
                smsInfo.violation.contains("停车") -> R.color.violation_normal
                smsInfo.violation.contains("超速") -> R.color.violation_serious
                smsInfo.violation.contains("闯红灯") -> R.color.violation_serious
                else -> R.color.violation_minor
            }
            tvViolation.setTextColor(itemView.context.getColor(violationColor))
            
            // 短信内容预览（截取前50个字符）
            val preview = if (smsInfo.body.length > 50) {
                smsInfo.body.substring(0, 50) + "..."
            } else {
                smsInfo.body
            }
            tvContentPreview.text = preview
            
            // 发件人
            tvSender.text = smsInfo.address
            
            // 状态（根据时间判断）
            val status = getStatusText(smsInfo.date)
            tvStatus.text = status
            
            // 设置状态背景
            val statusBg = when (status) {
                "新记录" -> R.drawable.bg_status_new
                "今天" -> R.drawable.bg_status_today
                "近期" -> R.drawable.bg_status_recent
                else -> R.drawable.bg_status_old
            }
            tvStatus.setBackgroundResource(statusBg)
        }

        /**
         * 根据时间获取状态文本
         */
        private fun getStatusText(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60 * 60 * 1000 -> "新记录"      // 1小时内
                diff < 24 * 60 * 60 * 1000 -> "今天"   // 24小时内
                diff < 7 * 24 * 60 * 60 * 1000 -> "近期" // 7天内
                else -> "历史"
            }
        }
    }
}