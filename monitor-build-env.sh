#!/bin/bash

# 使用环境变量监控GitHub Actions构建

set -e

echo "🔍 GitHub Actions构建监控 (使用环境变量)"
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

REPO_URL="https://github.com/$GITHUB_USERNAME/android-sms-reader"
API_URL="https://api.github.com/repos/$GITHUB_USERNAME/android-sms-reader"

echo "📦 仓库信息:"
echo "   用户名: $GITHUB_USERNAME"
echo "   仓库: $REPO_URL"
echo "   Actions: $REPO_URL/actions"
echo "   Releases: $REPO_URL/releases"
echo ""

# 获取构建状态
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

# 找到最新的Android构建
latest = None
for run in runs:
    if run.get('name') == 'Android Build and Release' or 'Android' in run.get('name', ''):
        latest = run
        break

if not latest and runs:
    latest = runs[0]

if latest:
    run_id = latest['id']
    status = latest['status']
    conclusion = latest.get('conclusion', '未完成')
    created = latest['created_at']
    html_url = latest['html_url']
    
    # 计算运行时间
    created_dt = datetime.fromisoformat(created.replace('Z', '+00:00'))
    now = datetime.utcnow()
    duration = now - created_dt
    minutes = int(duration.total_seconds() / 60)
    seconds = int(duration.total_seconds() % 60)
    
    print(f'🏗️  最新构建运行:')
    print(f'   运行ID: {run_id}')
    print(f'   状态: {status}')
    print(f'   结论: {conclusion}')
    print(f'   运行时间: {minutes}分{seconds}秒')
    print(f'   链接: {html_url}')
    print('')
    
    if status == 'queued':
        print('⏳ 状态: 排队中')
        print('   预计1-2分钟内开始执行')
    elif status == 'in_progress':
        print('🚀 状态: 运行中')
        print(f'   已运行: {minutes}分{seconds}秒')
        print('   预计还需5-8分钟完成')
    elif status == 'completed':
        if conclusion == 'success':
            print('✅ 状态: 成功完成')
            print('   APK已生成，可以下载了')
        else:
            print(f'❌ 状态: 完成 ({conclusion})')
            print('   请查看构建日志排查问题')
else:
    print('📭 未找到构建运行')
"

echo ""
echo "📦 Releases状态:"
RELEASES_JSON=$(curl -s -H "Authorization: token $GITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  "$API_URL/releases")

echo "$RELEASES_JSON" | python3 -c "
import json, sys
data = json.load(sys.stdin)
if isinstance(data, list):
    print(f'   当前有 {len(data)} 个Release:')
    for release in data[:2]:
        tag = release['tag_name']
        name = release.get('name', tag)
        assets = len(release.get('assets', []))
        print(f'   - {tag}: {name} ({assets}个文件)')
else:
    print('   📭 暂无Release')
    print('   构建成功后会自动创建第一个Release')
"

echo ""
echo "📱 应用功能摘要:"
echo "   ✅ 读取12123违法停车短信"
echo "   ✅ 提取车牌号、时间、地点"
echo "   ✅ 语音+震动提醒"
echo "   ✅ 自动通知"
echo ""

echo "📥 APK下载位置（构建成功后）:"
echo "   1. Releases页面: $REPO_URL/releases"
echo "   2. Actions页面: $REPO_URL/actions"
echo "   3. 使用脚本: ./download-apk-env.sh"
echo ""

echo "⏰ 预计剩余时间:"
echo "   根据构建状态显示剩余时间"
echo ""

echo "🔄 自动刷新（每30秒）:"
echo "按 Ctrl+C 停止监控"
echo ""

# 循环监控
counter=0
while true; do
    sleep 30
    counter=$((counter + 1))
    clear
    echo "🔍 GitHub Actions构建监控 (第 $counter 次检查)"
    echo "========================================"
    echo "时间: $(date)"
    echo "仓库: $GITHUB_USERNAME/android-sms-reader"
    echo ""
    
    # 获取构建状态
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
    sys.exit(0)

latest = None
for run in runs:
    if run.get('name') == 'Android Build and Release' or 'Android' in run.get('name', ''):
        latest = run
        break

if not latest and runs:
    latest = runs[0]

if latest:
    status = latest['status']
    conclusion = latest.get('conclusion', '未完成')
    created = latest['created_at']
    
    created_dt = datetime.fromisoformat(created.replace('Z', '+00:00'))
    now = datetime.utcnow()
    duration = now - created_dt
    minutes = int(duration.total_seconds() / 60)
    seconds = int(duration.total_seconds() % 60)
    
    print(f'🏗️  构建状态: {status}')
    print(f'⏰ 运行时间: {minutes}分{seconds}秒')
    
    if status == 'queued':
        print('⏳ 排队中，即将开始...')
    elif status == 'in_progress':
        if minutes < 3:
            print('🚀 环境设置阶段...')
        elif minutes < 6:
            print('📦 下载依赖阶段...')
        elif minutes < 9:
            print('🔨 编译构建阶段...')
        else:
            print('📱 APK生成阶段...')
    elif status == 'completed':
        if conclusion == 'success':
            print('✅ 构建成功！APK已生成')
            print('请访问Releases页面下载')
        else:
            print(f'❌ 构建失败: {conclusion}')
    print('')
"
done