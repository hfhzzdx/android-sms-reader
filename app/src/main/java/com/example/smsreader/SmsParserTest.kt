package com.example.smsreader

import android.util.Log

/**
 * SmsParser的测试类
 * 用于验证违法行为提取和时间格式化功能
 */
object SmsParserTest {
    
    private const val TAG = "SmsParserTest"
    
    /**
     * 运行所有测试
     */
    fun runAllTests() {
        Log.d(TAG, "开始SmsParser测试...")
        
        testChineseBracketExtraction()
        testTimeFormatting()
        testPlateNumberExtraction()
        
        Log.d(TAG, "SmsParser测试完成")
    }
    
    /**
     * 测试中文括号提取功能
     */
    private fun testChineseBracketExtraction() {
        Log.d(TAG, "测试中文括号提取...")
        
        val parser = SmsParser()
        val testCases = listOf(
            // 测试用例1：方头括号【】
            "【豫A12345】在建设路未按规定停放已被记录，请立即驶离。" to "豫A12345",
            
            // 测试用例2：尖括号「」
            "您的车辆「豫B67890」在中山路违反禁令标志已被记录。" to "豫B67890",
            
            // 测试用例3：双尖括号『』
            "『豫C24680』在解放路闯红灯已被电子监控记录。" to "豫C24680",
            
            // 测试用例4：书名号《》
            "《豫D13579》在长江路超速行驶已被记录。" to "豫D13579",
            
            // 测试用例5：圆括号（）
            "(豫E98765)在黄河路违章停车已被记录。" to "豫E98765",
            
            // 测试用例6：混合括号
            "【豫F55555】在人民路「未按规定停放」已被记录。" to "豫F55555",
            
            // 测试用例7：无括号
            "豫G33333在东风路违停已被记录。" to "豫G33333",
            
            // 测试用例8：违法行为在括号中
            "豫H77777在建设路【未按规定停放】已被记录。" to "未按规定停放"
        )
        
        var passed = 0
        var failed = 0
        
        testCases.forEachIndexed { index, (input, expected) ->
            val result = parser.extractViolation(input)
            val plateResult = parser.extractPlateNumber(input)
            
            val testPassed = when (index) {
                7 -> result == expected // 最后一个测试检查违法行为提取
                else -> plateResult == expected // 其他测试检查车牌提取
            }
            
            if (testPassed) {
                passed++
                Log.d(TAG, "测试 ${index + 1} 通过: 输入='$input', 结果='${if (index == 7) result else plateResult}'")
            } else {
                failed++
                Log.w(TAG, "测试 ${index + 1} 失败: 输入='$input', 期望='$expected', 实际='${if (index == 7) result else plateResult}'")
            }
        }
        
        Log.d(TAG, "中文括号提取测试结果: $passed 通过, $failed 失败")
    }
    
    /**
     * 测试时间格式化功能
     */
    private fun testTimeFormatting() {
        Log.d(TAG, "测试时间格式化...")
        
        val testTimestamps = listOf(
            System.currentTimeMillis() - 1000, // 1秒前
            System.currentTimeMillis() - 300000, // 5分钟前
            System.currentTimeMillis() - 7200000, // 2小时前
            System.currentTimeMillis() - 172800000, // 2天前
            System.currentTimeMillis() - 604800000, // 7天前
            1672531200000L // 固定时间: 2023-01-01 00:00:00
        )
        
        testTimestamps.forEachIndexed { index, timestamp ->
            val chineseFormat = TimeFormatter.formatToChineseDateTime(timestamp)
            val relativeFormat = TimeFormatter.formatToRelativeTime(timestamp)
            val smartFormat = TimeFormatter.formatSmart(timestamp)
            
            Log.d(TAG, "时间测试 ${index + 1}:")
            Log.d(TAG, "  原始时间戳: $timestamp")
            Log.d(TAG, "  中文格式: $chineseFormat")
            Log.d(TAG, "  相对时间: $relativeFormat")
            Log.d(TAG, "  智能格式: $smartFormat")
        }
        
        Log.d(TAG, "时间格式化测试完成")
    }
    
    /**
     * 测试车牌号提取功能
     */
    private fun testPlateNumberExtraction() {
        Log.d(TAG, "测试车牌号提取...")
        
        val parser = SmsParser()
        val testCases = listOf(
            "豫A12345在建设路违停" to "豫A12345",
            "车牌: 京B88888在长安街超速" to "京B88888",
            "沪C66666 在外滩违停" to "沪C66666",
            "粤D99999在珠江路闯红灯" to "粤D99999",
            "川E11111在春熙路违章停车" to "川E11111",
            "无车牌信息的测试短信" to "",
            "12123提醒: 豫F22222在建设路【未按规定停放】已被记录" to "豫F22222"
        )
        
        var passed = 0
        var failed = 0
        
        testCases.forEachIndexed { index, (input, expected) ->
            val result = parser.extractPlateNumber(input)
            
            if (result == expected) {
                passed++
                Log.d(TAG, "车牌测试 ${index + 1} 通过: 输入='$input', 车牌='$result'")
            } else {
                failed++
                Log.w(TAG, "车牌测试 ${index + 1} 失败: 输入='$input', 期望='$expected', 实际='$result'")
            }
        }
        
        Log.d(TAG, "车牌号提取测试结果: $passed 通过, $failed 失败")
    }
    
    /**
     * 生成测试短信内容
     */
    fun generateTestSmsContent(): String {
        val testCases = listOf(
            """
            【豫A12345】您的小型汽车于2023年10月15日14时30分在建设路未按规定停放已被记录，请立即驶离。
            """,
            """
            「豫B67890」您的车辆在中山路违反禁令标志指示行驶，违法行为已被记录。
            """,
            """
            『豫C24680』在解放路与人民路交叉口闯红灯，电子监控设备于2023年10月15日15时45分记录。
            """,
            """
            豫D13579在长江路超速行驶（限速60km/h，实际速度85km/h），违法行为已被记录。
            """,
            """
            《豫E98765》在黄河路违章停车，请立即驶离，否则将依法拖移。
            """,
            """
            (豫F55555)在人民路未按规定停放，已被交通技术监控设备记录。
            """
        )
        
        return testCases.random()
    }
    
    /**
     * 验证时间格式化是否正确
     */
    fun verifyTimeFormatting(): Boolean {
        val testTimestamp = 1672531200000L // 2023-01-01 00:00:00
        val formatted = TimeFormatter.formatToChineseDateTime(testTimestamp)
        
        // 检查是否包含中文时间格式元素
        val containsChineseElements = formatted.contains("年") && 
                                     formatted.contains("月") && 
                                     formatted.contains("日")
        
        val isNotTimestamp = !formatted.matches(Regex("\\d+")) // 不是纯数字
        
        Log.d(TAG, "时间格式化验证: 输入=$testTimestamp, 输出='$formatted'")
        Log.d(TAG, "包含中文元素: $containsChineseElements, 不是时间戳: $isNotTimestamp")
        
        return containsChineseElements && isNotTimestamp
    }
}