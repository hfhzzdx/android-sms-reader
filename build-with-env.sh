#!/bin/bash

# 使用环境变量进行GitHub Actions构建和监控

set -e

echo "🚀 GitHub Actions构建脚本 (使用环境变量)"
echo "========================================"
echo "时间: $(date)"
echo ""

# 检查环境变量
if [ -z "$GITHUB_USERNAME" ] || [ -z "$GITHUB_PASSWORD" ]; then
    echo "❌ 错误: 缺少环境变量"
    echo "请设置:"
    echo "  export GITHUB_USERNAME=你的GitHub用户名"
    echo "  export GITHUB_PASSWORD=你的GitHub Token"
    exit 1
fi

echo "✅ 环境变量检查通过:"
echo "   用户名: $GITHUB_USERNAME"
echo "   Token: ${GITHUB_PASSWORD:0:10}..."
echo "   仓库: https://github.com/$GITHUB_USERNAME/android-sms-reader"
echo ""

# 触发构建
echo "🔨 触发GitHub Actions构建..."
RESPONSE=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  -H "Content-Type: application/json" \
  -d '{"ref":"main"}' \
  "https://api.github.com/repos/$GITHUB_USERNAME/android-sms-reader/actions/workflows/android-build.yml/dispatches" 2>&1)

if echo "$RESPONSE" | grep -q "204 No Content\|201 Created"; then
    echo "✅ 构建已触发成功"
else
    echo "⚠️ 构建触发响应: $RESPONSE"
fi

echo ""
echo "⏰ 构建预计时间线:"
echo "   - 0-2分钟: 排队中"
echo "   - 2-5分钟: 环境设置"
echo "   - 5-8分钟: 下载依赖"
echo "   - 8-11分钟: 编译构建"
echo "   - 11-12分钟: 创建Release和Tag"
echo "   总时间: 11-12分钟"
echo ""

echo "📊 构建状态监控:"
echo "   网页查看: https://github.com/$GITHUB_USERNAME/android-sms-reader/actions"
echo "   或运行: ./monitor-build-env.sh"
echo ""

echo "📱 应用功能:"
echo "   ✅ 读取12123违法停车短信"
echo "   ✅ 提取车牌号、时间、地点"
echo "   ✅ 语音提醒: '紧急通知！您的车辆{车牌号}于{时间}违法停车被记录，请立即驶离。'"
echo "   ✅ 强烈震动提醒"
echo ""

echo "🎯 构建完成后:"
echo "   1. APK位置: https://github.com/$GITHUB_USERNAME/android-sms-reader/releases"
echo "   2. 下载脚本: ./download-apk-env.sh"
echo "   3. 测试方法: 发送12123测试短信"
echo ""

echo "✅ 脚本执行完成"
echo "构建已启动，请等待11-12分钟..."