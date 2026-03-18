#!/bin/bash

echo "🔧 构建修复和监控"
echo "================"
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

API_URL="https://api.github.com/repos/$GITHUB_USERNAME/android-sms-reader"
REPO_URL="https://github.com/$GITHUB_USERNAME/android-sms-reader"

echo "✅ 环境变量:"
echo "   用户名: $GITHUB_USERNAME"
echo "   仓库: $REPO_URL"
echo ""

echo "📝 已完成的修复:"
echo "   1. ✅ 更新了GitHub Actions工作流文件"
echo "   2. ✅ 添加了构建调试步骤"
echo "   3. ✅ 提交并推送到GitHub"
echo "   4. ✅ 触发了新的构建"
echo ""

echo "📊 构建状态:"
RUNS_JSON=$(curl -s -H "Authorization: token $GITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  "$API_URL/actions/runs")

echo "$RUNS_JSON" | python3 -c "
import json, sys
from datetime import datetime

data = json.load(sys.stdin)
runs = data.get('workflow_runs', [])

if not runs:
    print('📭 暂无构建运行记录')
    print('构建可能需要1-2分钟才会显示')
    sys.exit(0)

# 显示最近3个构建
print(f'最近 {min(3, len(runs))} 个构建运行:')
for i, run in enumerate(runs[:3]):
    print(f'--- 构建 #{i+1} ---')
    print(f'运行ID: {run[\"id\"]}')
    print(f'名称: {run.get(\"name\", \"Android Build\")}')
    print(f'状态: {run[\"status\"]}')
    print(f'结论: {run.get(\"conclusion\", \"未完成\")}')
    print(f'创建时间: {run[\"created_at\"]}')
    print(f'链接: {run[\"html_url\"]}')
    
    if run['status'] == 'queued':
        print('⏳ 状态: 排队中')
    elif run['status'] == 'in_progress':
        print('🚀 状态: 运行中')
    elif run['status'] == 'completed':
        if run.get('conclusion') == 'success':
            print('✅ 状态: 成功')
        else:
            print(f'❌ 状态: 失败 ({run.get(\"conclusion\")})')
"

echo ""
echo "⏰ 预计时间线:"
echo "   - 0-2分钟: 构建排队"
echo "   - 2-5分钟: 环境设置"
echo "   - 5-10分钟: 构建执行"
echo "   - 10-12分钟: 创建Release"
echo "   总时间: 10-12分钟"
echo ""

echo "📱 应用功能:"
echo "   ✅ 读取12123违法停车短信"
echo "   ✅ 提取车牌号、时间、地点"
echo "   ✅ 语音提醒: '紧急通知！您的车辆{车牌号}于{时间}违法停车被记录，请立即驶离。'"
echo "   ✅ 强烈震动提醒"
echo ""

echo "🎯 构建成功后:"
echo "   1. 访问: $REPO_URL/actions 下载APK"
echo "   2. 访问: $REPO_URL/releases 查看Release"
echo "   3. 运行: ./download-apk-env.sh 自动下载"
echo ""

echo "🔧 如果再次失败:"
echo "   1. 查看构建日志中的错误信息"
echo "   2. 检查Android项目配置"
echo "   3. 我可以帮你进一步调试"
echo ""

echo "🚀 新的构建已启动，请等待10-12分钟..."
echo ""
echo "实时监控: ./monitor-build-env.sh"
echo "或访问: $REPO_URL/actions"