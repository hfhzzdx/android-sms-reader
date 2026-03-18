#!/usr/bin/env python3
"""
短信解析器测试脚本
用于测试SmsParser.kt中的正则表达式和解析逻辑
"""

import re

def test_plate_number_extraction():
    """测试车牌号提取"""
    
    test_cases = [
        {
            "sms": "【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路未按规定停放已被记录",
            "expected": "豫ADS2567"
        },
        {
            "sms": "【山东交警】您的车辆鲁A12345于2025年10月15日14:20在济南市经十路违反禁令标志指示",
            "expected": "鲁A12345"
        },
        {
            "sms": "【北京交警】京B12345在朝阳区违章停车",
            "expected": "京B12345"
        },
        {
            "sms": "【上海交警】沪A88888在外滩超速行驶",
            "expected": "沪A88888"
        },
        {
            "sms": "【广东交警】粤B12345在深圳闯红灯",
            "expected": "粤B12345"
        },
        {
            "sms": "车牌：浙C12345 违法：未按规定停放",
            "expected": "浙C12345"
        }
    ]
    
    # 车牌号正则表达式（与Kotlin版本保持一致）
    plate_patterns = [
        r"([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5})",
        r"([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z]\s*[A-Z0-9]{5})",
        r"车牌[：:]\s*([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{4,6})"
    ]
    
    print("=== 车牌号提取测试 ===")
    
    for i, test in enumerate(test_cases, 1):
        sms = test["sms"]
        expected = test["expected"]
        
        found = None
        for pattern in plate_patterns:
            match = re.search(pattern, sms)
            if match:
                found = match.group(1).replace(" ", "")
                break
        
        status = "✓" if found == expected else "✗"
        print(f"{status} 测试用例 {i}:")
        print(f"  短信: {sms[:50]}...")
        print(f"  预期: {expected}")
        print(f"  实际: {found}")
        print()

def test_violation_extraction():
    """测试违法行为提取"""
    
    test_cases = [
        {
            "sms": "【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路未按规定停放已被记录",
            "expected": "未按规定停放"
        },
        {
            "sms": "【山东交警】您的车辆鲁A12345违反禁令标志指示",
            "expected": "违反禁令标志"
        },
        {
            "sms": "【北京交警】京B12345在朝阳区违章停车已被记录",
            "expected": "违章停车"
        },
        {
            "sms": "【上海交警】沪A88888在外滩超速行驶",
            "expected": "超速行驶"
        }
    ]
    
    # 违法行为关键词（与Kotlin版本保持一致）
    violation_keywords = [
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
    ]
    
    print("=== 违法行为提取测试 ===")
    
    for i, test in enumerate(test_cases, 1):
        sms = test["sms"]
        expected = test["expected"]
        
        found = None
        for keyword in violation_keywords:
            if keyword in sms:
                found = keyword
                break
        
        # 如果没有找到关键词，尝试提取"已被记录"附近的内容
        if not found and "已被记录" in sms:
            idx = sms.index("已被记录")
            start = max(0, idx - 20)
            end = min(len(sms), idx + 10)
            found = sms[start:end].strip()
        
        status = "✓" if found and expected in found else "✗"
        print(f"{status} 测试用例 {i}:")
        print(f"  短信: {sms[:50]}...")
        print(f"  预期包含: {expected}")
        print(f"  实际: {found}")
        print()

def test_sms_validation():
    """测试短信验证"""
    
    test_cases = [
        {
            "sender": "12123",
            "body": "【河南省交警】您的小型新能源汽车豫ADS2567未按规定停放已被记录",
            "expected": True
        },
        {
            "sender": "10086",
            "body": "您的余额不足，请及时充值",
            "expected": False
        },
        {
            "sender": "12123",
            "body": "欢迎使用12123服务",
            "expected": False  # 不包含关键词
        },
        {
            "sender": "10690123",
            "body": "【交警】您的车辆有违章记录",
            "expected": False  # 不是12123开头
        }
    ]
    
    # 验证关键词（与Kotlin版本保持一致）
    validation_keywords = [
        "交警", "违章", "违法", "记录", "处罚", "驶离", "拖移", "车牌", "车辆"
    ]
    
    print("=== 短信验证测试 ===")
    
    for i, test in enumerate(test_cases, 1):
        sender = test["sender"]
        body = test["body"]
        expected = test["expected"]
        
        # 检查发件人是否为12123开头
        is_12123 = sender.startswith("12123")
        
        # 检查内容是否包含关键词
        has_keyword = any(keyword in body for keyword in validation_keywords)
        
        result = is_12123 and has_keyword
        status = "✓" if result == expected else "✗"
        
        print(f"{status} 测试用例 {i}:")
        print(f"  发件人: {sender} (是12123开头: {is_12123})")
        print(f"  内容: {body[:30]}...")
        print(f"  包含关键词: {has_keyword}")
        print(f"  预期: {expected}, 实际: {result}")
        print()

def main():
    """主函数"""
    print("12123短信解析器测试")
    print("=" * 50)
    
    test_plate_number_extraction()
    test_violation_extraction()
    test_sms_validation()
    
    print("测试完成！")

if __name__ == "__main__":
    main()