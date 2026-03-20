package com.example.smsreader

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvResult: TextView
    private lateinit var btnReadSms: Button
    private lateinit var btnStartService: Button
    private lateinit var btnTestParser: Button
    
    companion object {
        private const val TAG = "MainActivity"
        private const val SMS_PERMISSION_REQUEST_CODE = 100
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 101
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        tvResult = findViewById(R.id.tv_result)
        btnReadSms = findViewById(R.id.btn_read_sms)
        btnStartService = findViewById(R.id.btn_start_service)
        btnTestParser = findViewById(R.id.btn_test_parser)
        
        btnReadSms.setOnClickListener {
            checkAndRequestPermissions()
        }
        
        btnStartService.setOnClickListener {
            startSmsMonitorService()
        }
        
        btnTestParser.setOnClickListener {
            runParserTests()
        }
        
        // 检查权限
        if (!hasSmsPermission()) {
            requestSmsPermission()
        } else {
            Toast.makeText(this, "已获得短信读取权限", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
            ),
            SMS_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
                read12123Sms()
            } else {
                Toast.makeText(this, "权限被拒绝，部分功能无法使用", Toast.LENGTH_SHORT).show()
                // 显示解释对话框
                showPermissionExplanation()
            }
        }
    }
    
    private fun showPermissionExplanation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("需要权限")
            .setMessage("需要短信读取权限来监控12123的违章通知")
            .setPositiveButton("去设置") { _, _ ->
                // 打开应用设置页面
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                )
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun checkAndRequestPermissions() {
        if (hasSmsPermission()) {
            read12123Sms()
        } else {
            requestSmsPermission()
        }
    }
    
    private fun read12123Sms() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                readSmsFrom12123()
            }
            
            if (result.isNotEmpty()) {
                val displayText = StringBuilder()
                result.forEach { smsInfo ->
                    // 使用TimeFormatter将时间戳转换为中文格式
                    val chineseTime = TimeFormatter.formatToChineseDateTime(smsInfo.date)
                    displayText.append("📅 时间: $chineseTime\n")
                    displayText.append("🚗 车牌: ${smsInfo.plateNumber}\n")
                    displayText.append("⚠️  违法: ${smsInfo.violation}\n")
                    displayText.append("📱 内容: ${smsInfo.body}\n")
                    displayText.append("─".repeat(30) + "\n")
                }
                tvResult.text = displayText.toString()
            } else {
                tvResult.text = "📭 未找到12123发来的短信"
            }
        }
    }
    
    private fun readSmsFrom12123(): List<SmsInfo> {
        val smsList = mutableListOf<SmsInfo>()
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )
        
        // 筛选12123开头的发件人
        val selection = "${Telephony.Sms.ADDRESS} LIKE ?"
        val selectionArgs = arrayOf("12123%")
        val sortOrder = "${Telephony.Sms.DATE} DESC"
        
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            
            cursor?.use {
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex)
                    val body = it.getString(bodyIndex)
                    val date = it.getLong(dateIndex)
                    
                    // 解析短信内容
                    val parser = SmsParser()
                    val plateNumber = parser.extractPlateNumber(body)
                    val violation = parser.extractViolation(body)
                    
                    if (plateNumber.isNotEmpty()) {
                        smsList.add(
                            SmsInfo(
                                address = address,
                                body = body,
                                date = date,
                                plateNumber = plateNumber,
                                violation = violation
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取短信失败", e)
        } finally {
            cursor?.close()
        }
        
        return smsList
    }
    
    private fun startSmsMonitorService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(SmsMonitorService.getStartIntent(this))
        } else {
            startService(SmsMonitorService.getStartIntent(this))
        }
        Toast.makeText(this, "后台监控服务已启动", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 运行解析器测试
     */
    private fun runParserTests() {
        lifecycleScope.launch {
            val testResults = withContext(Dispatchers.IO) {
                val result = StringBuilder()
                
                // 运行SmsParser测试
                result.append("=== SmsParser 测试结果 ===\n\n")
                
                // 测试时间格式化
                result.append("1. 时间格式化测试:\n")
                val testTimestamp = 1672531200000L // 2023-01-01 00:00:00
                val formattedTime = TimeFormatter.formatToChineseDateTime(testTimestamp)
                result.append("   时间戳: $testTimestamp\n")
                result.append("   格式化: $formattedTime\n")
                result.append("   验证: ${if (formattedTime.contains("年") && formattedTime.contains("月") && formattedTime.contains("日")) "✅ 通过" else "❌ 失败"}\n\n")
                
                // 测试中文括号提取
                result.append("2. 中文括号提取测试:\n")
                val parser = SmsParser()
                val testSms = "【豫A12345】在建设路未按规定停放已被记录，请立即驶离。"
                val violation = parser.extractViolation(testSms)
                val plateNumber = parser.extractPlateNumber(testSms)
                result.append("   测试短信: $testSms\n")
                result.append("   提取车牌: $plateNumber\n")
                result.append("   提取违法: $violation\n")
                result.append("   验证: ${if (plateNumber == "豫A12345") "✅ 通过" else "❌ 失败"}\n\n")
                
                // 测试相对时间
                result.append("3. 相对时间测试:\n")
                val now = System.currentTimeMillis()
                val fiveMinutesAgo = now - 5 * 60 * 1000
                val relativeTime = TimeFormatter.formatToRelativeTime(fiveMinutesAgo)
                result.append("   5分钟前: $relativeTime\n")
                result.append("   验证: ${if (relativeTime.contains("5分钟前")) "✅ 通过" else "❌ 失败"}\n\n")
                
                // 测试智能时间格式化
                result.append("4. 智能时间格式化测试:\n")
                val twoDaysAgo = now - 2 * 24 * 60 * 60 * 1000
                val smartTime = TimeFormatter.formatSmart(twoDaysAgo)
                result.append("   2天前: $smartTime\n")
                result.append("   验证: ${if (smartTime == "前天" || smartTime.contains("月")) "✅ 通过" else "❌ 失败"}\n")
                
                result.toString()
            }
            
            tvResult.text = testResults
            Toast.makeText(this@MainActivity, "解析器测试完成", Toast.LENGTH_SHORT).show()
        }
    }
    
    data class SmsInfo(
        val address: String,
        val body: String,
        val date: Long,
        val plateNumber: String,
        val violation: String
    )
}