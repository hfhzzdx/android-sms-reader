#!/bin/bash

# 检查GitHub Actions构建状态

set -e

echo "🔍 检查GitHub Actions构建状态"
echo "============================="
echo "仓库: hfhzzdx/android-sms-reader"
echo "时间: $(date)"
echo ""

# 检查最近的工作流运行
echo "📊 最近的工作流运行:"
curl -s -H "Authorization: token $GITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/hfhzzdx/android-sms-reader/actions/runs \
  | python3 -c "
import json, sys
data = json.load(sys.stdin)
runs = data.get('workflow_runs', [])[:3]
for run in runs:
    print(f'运行ID: {run[\"id\"]}')
    print(f'状态: {run[\"status\"]}')
    print(f'结论: {run.get(\"conclusion\", \"未完成\")}')
    print(f'创建时间: {run[\"created_at\"]}')
    print(f'HTML URL: {run[\"html_url\"]}')
    print('---')
" 2>/dev/null || echo "无法获取运行状态"

echo ""
echo "🌐 直接访问链接:"
echo "1. Actions页面: https://github.com/hfhzzdx/android-sms-reader/actions"
echo "2. 仓库主页: https://github.com/hfhzzdx/android-sms-reader"
echo ""

echo "📱 APK下载位置（构建成功后）:"
echo "1. Actions页面 → 点击最新运行 → Artifacts"
echo "2. 或者: Releases页面"
echo ""

echo "⏰ 构建预计时间:"
echo "- 环境设置: 2-3分钟"
echo "- 依赖下载: 3-5分钟"
echo "- 编译构建: 2-3分钟"
echo "- 总计: 7-11分钟"
echo ""

echo "🔧 手动触发构建（如果需要）:"
echo "访问: https://github.com/hfhzzdx/android-sms-reader/actions/workflows/android-build.yml"
echo "点击 'Run workflow' 按钮"
echo ""

echo "📋 应用功能回顾:"
echo "✅ 读取12123违法停车短信"
echo "✅ 提取车牌号、时间、地点"
echo "✅ 语音提醒: '紧急通知！您的车辆{车牌号}于{时间}违法停车被记录，请立即驶离。'"
echo "✅ 强烈震动提醒"
echo "✅ 详细通知显示"
echo ""

echo "🎯 测试方法:"
echo "1. 下载构建的APK"
echo "2. 安装到Android设备"
echo "3. 授予短信读取权限"
echo "4. 发送测试短信:"
echo "   发件人: 12123"
echo "   内容: 【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录..."
echo ""

echo "🔄 实时监控:"
echo "在浏览器中打开Actions页面查看实时构建日志"
echo ""

echo "✅ 检查完成"
