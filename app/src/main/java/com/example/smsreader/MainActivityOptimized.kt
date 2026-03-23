package com.example.smsreader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smsreader.adapter.ViolationAdapter
import com.example.smsreader.model.SmsInfo
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.ActivityManager
import android.content.Context
import android.app.ActivityManager
import android.content.Context

/**
 * 优化后的主界面
 */
class MainActivityOptimized : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivityOptimized"
        private const val SMS_PERMISSION_REQUEST_CODE = 100
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    }
    
    // UI组件
    private lateinit var tvMonitorStatus: TextView
    private lateinit var tvDetectionCount: TextView
    private lateinit var tvRecordCount: TextView
    private lateinit var btnReadSms: MaterialButton
    private lateinit var btnStartService: MaterialButton
    private lateinit var btnTestParser: MaterialButton
    private lateinit var btnSettings: MaterialButton
    private lateinit var recyclerRecords: RecyclerView
    private lateinit var layoutEmpty: androidx.constraintlayout.widget.ConstraintLayout
    
    // 适配器
    private lateinit var violationAdapter: ViolationAdapter
    
    // 数据
    private val violationList = mutableListOf<com.example.smsreader.model.SmsInfo>()
    
    
     /**
     * 检查服务是否正在运行
     */
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        return try {
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.getRunningServices(Int.MAX_VALUE).any { 
                it.service.className == serviceClass.name 
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
   

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_optimized)
        
        // 初始化UI组件
        initViews()
        
        // 初始化RecyclerView
        initRecyclerView()
        
        // 设置按钮点击事件
        setupClickListeners()
        
        // 检查权限
        checkAndRequestPermissions()
        
        // 更新状态显示
        updateStatusDisplay()
    }
    
    override fun onResume() {
        super.onResume()
        // 刷新数据
        refreshData()
    }
    
    /**
     * 初始化视图
     */
    private fun initViews() {
        tvMonitorStatus = findViewById(R.id.tv_monitor_status)
        tvDetectionCount = findViewById(R.id.tv_detection_count)
        tvRecordCount = findViewById(R.id.tv_record_count)
        btnReadSms = findViewById(R.id.btn_read_sms)
        btnStartService = findViewById(R.id.btn_start_service)
        btnTestParser = findViewById(R.id.btn_test_parser)
        btnSettings = findViewById(R.id.btn_settings)
        recyclerRecords = findViewById(R.id.recycler_records)
        layoutEmpty = findViewById(R.id.layout_empty)
    }
    
    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        violationAdapter = ViolationAdapter(violationList) { smsInfo ->
            // 点击项目时的操作
            showViolationDetail(smsInfo)
        }
        
        recyclerRecords.layoutManager = LinearLayoutManager(this)
        recyclerRecords.adapter = violationAdapter
    }
    
    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        btnReadSms.setOnClickListener {
            readSmsFrom12123()
        }
        
        btnStartService.setOnClickListener {
            startSmsMonitorService()
        }
        
        btnTestParser.setOnClickListener {
            runParserTests()
        }
        
        btnSettings.setOnClickListener {
            // TODO: 打开设置界面
            Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 检查并请求权限
     */
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // 检查短信权限
        if (!hasSmsPermission()) {
            permissionsToRequest.add(Manifest.permission.READ_SMS)
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
        }
        
        // 检查通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                SMS_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * 检查是否有短信权限
     */
    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查是否有通知权限
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 12及以下自动拥有通知权限
        }
    }
    
    /**
     * 读取12123短信
     */
    private fun readSmsFrom12123() {
        if (!hasSmsPermission()) {
            Toast.makeText(this, "请先授予短信读取权限", Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            btnReadSms.isEnabled = false
            btnReadSms.text = "读取中..."
            
            val result = withContext(Dispatchers.IO) {
                readSmsFrom12123InBackground()
            }
            
            if (result.isNotEmpty()) {
                violationList.clear()
                violationList.addAll(result)
                violationAdapter.updateData(violationList)
                
                // 更新显示
                updateRecordCount(result.size)
                updateDetectionCount(result.size)
                
                // 隐藏空状态
                layoutEmpty.visibility = if (result.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                
                Toast.makeText(this@MainActivityOptimized, "读取到 ${result.size} 条违章记录", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivityOptimized, "未找到12123发来的短信", Toast.LENGTH_SHORT).show()
                layoutEmpty.visibility = android.view.View.VISIBLE
            }
            
            btnReadSms.isEnabled = true
            btnReadSms.text = "读取短信"
        }
    }
    
    /**
     * 在后台读取短信
     */
    private fun readSmsFrom12123InBackground(): List<SmsInfo> {
        return SmsReaderHelper.readSmsFrom12123(this)
    }
    
    /**
     * 启动短信监控服务
     */
    private fun startSmsMonitorService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(SmsMonitorService.getStartIntent(this))
        } else {
            startService(SmsMonitorService.getStartIntent(this))
        }
        
        tvMonitorStatus.text = "运行中"
        tvMonitorStatus.setTextColor(ContextCompat.getColor(this, R.color.status_active))
        
        Toast.makeText(this, "后台监控服务已启动", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 运行解析器测试
     */
    private fun runParserTests() {
        CoroutineScope(Dispatchers.Main).launch {
            val testResults = withContext(Dispatchers.IO) {
                val result = StringBuilder()
                
                // 测试时间格式化
                result.append("=== 解析器测试结果 ===\n\n")
                
                val testTimestamp = 1672531200000L // 2023-01-01 00:00:00
                val formattedTime = TimeFormatter.formatToChineseDateTime(testTimestamp)
                result.append("时间格式化测试:\n")
                result.append("  时间戳: $testTimestamp\n")
                result.append("  格式化: $formattedTime\n")
                result.append("  验证: ${if (formattedTime.contains("年") && formattedTime.contains("月") && formattedTime.contains("日")) "✅ 通过" else "❌ 失败"}\n\n")
                
                // 测试中文括号提取
                val parser = SmsParser()
                val testSms = "『未按规定停放』您的车辆豫A12345在建设路已被记录。"
                val violation = parser.extractViolation(testSms)
                result.append("『』符号提取测试:\n")
                result.append("  测试短信: $testSms\n")
                result.append("  提取违法: $violation\n")
                result.append("  验证: ${if (violation == "未按规定停放") "✅ 通过" else "❌ 失败"}\n\n")
                
                // 测试停车关键词
                val testSms2 = "豫B67890在中山路未按规定停放已被记录。"
                val violation2 = parser.extractViolation(testSms2)
                result.append("停车关键词测试:\n")
                result.append("  测试短信: $testSms2\n")
                result.append("  提取违法: $violation2\n")
                result.append("  验证: ${if (violation2 == "违法停车") "✅ 通过" else "❌ 失败"}\n")
                
                result.toString()
            }
            
            // 显示测试结果
            showTestResultsDialog(testResults)
        }
    }
    
    /**
     * 显示测试结果对话框
     */
    private fun showTestResultsDialog(results: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("解析器测试结果")
            .setMessage(results)
            .setPositiveButton("确定", null)
            .show()
    }
    
    /**
     * 显示违章详情
     */
    private fun showViolationDetail(smsInfo: com.example.smsreader.model.SmsInfo) {
        val detailText = """
            📅 时间: ${TimeFormatter.formatToChineseDateTime(smsInfo.date)}
            🚗 车牌: ${smsInfo.plateNumber}
            ⚠️  违法: ${smsInfo.violation}
            📱 发件人: ${smsInfo.address}
            
            完整内容:
            ${smsInfo.body}
        """.trimIndent()
        
        android.app.AlertDialog.Builder(this)
            .setTitle("违章详情")
            .setMessage(detailText)
            .setPositiveButton("确定", null)
            .show()
    }
    
    /**
     * 更新状态显示
     */
    private fun updateStatusDisplay() {
        // 检查服务是否在运行
        val isServiceRunning = SmsMonitorService.isServiceRunning(this)
        tvMonitorStatus.text = if (isServiceRunning) "运行中" else "未启动"
        tvMonitorStatus.setTextColor(
            ContextCompat.getColor(
                this,
                if (isServiceRunning) R.color.status_active else R.color.status_inactive
            )
        )
    }
    
    /**
     * 更新记录数量
     */
    private fun updateRecordCount(count: Int) {
        tvRecordCount.text = "共 $count 条"
    }
    
    /**
     * 更新检测数量
     */
    private fun updateDetectionCount(count: Int) {
        tvDetectionCount.text = "$count 条"
    }
    
    /**
     * 刷新数据
     */
    private fun refreshData() {
        if (violationList.isNotEmpty()) {
            updateRecordCount(violationList.size)
            updateDetectionCount(violationList.size)
            layoutEmpty.visibility = android.view.View.GONE
        } else {
            layoutEmpty.visibility = android.view.View.VISIBLE
        }
    }
    
    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                if (allGranted) {
                    Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "需要短信权限才能读取违章记录", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
