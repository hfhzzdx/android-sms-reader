#!/bin/bash

echo "🔍 验证GitHub仓库状态"
echo "====================="
echo "时间: $(date)"
echo ""

# 检查仓库URL
echo "1. 检查仓库URL:"
echo "   https://github.com/hfhzzdx/android-sms-reader"
echo ""

# 测试仓库访问
echo "2. 测试仓库访问:"
if curl -s -I https://github.com/hfhzzdx/android-sms-reader | grep -q "200 OK"; then
    echo "   ✅ 仓库可访问 (HTTP 200)"
else
    echo "   ❌ 仓库无法访问"
    echo "   请直接访问: https://github.com/hfhzzdx/android-sms-reader"
fi
echo ""

# 检查Actions
echo "3. 检查GitHub Actions:"
echo "   Actions页面: https://github.com/hfhzzdx/android-sms-reader/actions"
echo ""

# 检查代码推送状态
echo "4. 代码推送状态:"
cd /home/hfh/.openclaw/workspace/android-sms-reader
if git remote -v | grep -q "hfhzzdx/android-sms-reader"; then
    echo "   ✅ 远程仓库配置正确"
    echo "   远程URL: $(git remote get-url origin)"
else
    echo "   ❌ 远程仓库配置错误"
    git remote -v
fi
echo ""

# 检查本地提交
echo "5. 本地提交状态:"
git log --oneline -3
echo ""

echo "📱 应用功能摘要:"
echo "   ✅ 读取12123违法停车短信"
echo "   ✅ 提取车牌号、时间、地点"
echo "   ✅ 语音提醒: '紧急通知！您的车辆{车牌号}于{时间}违法停车被记录，请立即驶离。'"
echo "   ✅ 强烈震动提醒"
echo ""

echo "🎯 测试方法:"
echo "   1. 等待GitHub Actions构建完成"
echo "   2. 从Actions页面下载APK"
echo "   3. 安装到Android设备"
echo "   4. 发送测试短信:"
echo "      发件人: 12123"
echo "      内容: 【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录..."
echo ""

echo "🌐 直接访问链接:"
echo "   1. 仓库主页: https://github.com/hfhzzdx/android-sms-reader"
echo "   2. Actions构建: https://github.com/hfhzzdx/android-sms-reader/actions"
echo "   3. 如果遇到404，请等待1-2分钟刷新"
echo ""

echo "✅ 验证完成"