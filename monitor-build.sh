#!/bin/bash

# 实时监控GitHub Actions构建

echo "🔍 GitHub Actions构建监控"
echo "========================"
echo "仓库: hfhzzdx/android-sms-reader"
echo "开始时间: $(date)"
echo ""

# 获取最新的构建运行
get_build_status() {
    curl -s -H "Authorization: token $GITHUB_PASSWORD" \
        -H "Accept: application/vnd.github.v3+json" \
        https://api.github.com/repos/hfhzzdx/android-sms-reader/actions/runs \
        | python3 -c "
import json, sys, time
from datetime import datetime

data = json.load(sys.stdin)
runs = data.get('workflow_runs', [])

if not runs:
    print('📭 暂无构建运行')
    sys.exit(0)

# 找到最新的Android构建
latest = None
for run in runs:
    if run.get('name') == 'Android Build and Release':
        latest = run
        break

if not latest:
    print('📭 未找到Android构建运行')
    sys.exit(0)

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

print(f'🏗️  构建运行ID: {run_id}')
print(f'📊 状态: {status}')
print(f'🎯 结论: {conclusion}')
print(f'⏰ 运行时间: {minutes}分{seconds}秒')
print(f'🔗 链接: {html_url}')
print('')

# 状态说明
if status == 'queued':
    print('⏳ 构建排队中...')
    print('预计1-2分钟内开始执行')
elif status == 'in_progress':
    print('🚀 构建正在运行中...')
    print('预计还需5-8分钟完成')
elif status == 'completed':
    if conclusion == 'success':
        print('✅ 构建成功！')
        print('APK已生成，可以下载了')
    elif conclusion == 'failure':
        print('❌ 构建失败')
        print('请查看构建日志排查问题')
    elif conclusion == 'cancelled':
        print('⚠️ 构建被取消')
else:
    print(f'📝 状态: {status}')

print('')
"
}

# 第一次检查
get_build_status

echo "📱 应用功能摘要:"
echo "   ✅ 读取12123违法停车短信"
echo "   ✅ 提取车牌号、时间、地点"
echo "   ✅ 语音提醒功能"
echo "   ✅ 震动提醒功能"
echo ""

echo "🎯 APK下载位置（构建成功后）:"
echo "   1. 访问: https://github.com/hfhzzdx/android-sms-reader/actions"
echo "   2. 点击最新的构建运行"
echo "   3. 下载 'sms-reader-apk' artifact"
echo "   4. 或者使用: ./download-apk.sh"
echo ""

echo "⏰ 预计时间线:"
echo "   - 排队: 1-2分钟"
echo "   - 环境设置: 2-3分钟"
echo "   - 依赖下载: 3-5分钟"
echo "   - 编译构建: 2-3分钟"
echo "   - 总计: 8-13分钟"
echo ""

echo "🔄 实时监控（每30秒更新）:"
echo "按 Ctrl+C 停止监控"
echo ""

# 循环监控
counter=0
while true; do
    sleep 30
    counter=$((counter + 1))
    clear
    echo "🔍 GitHub Actions构建监控 (第 $counter 次检查)"
    echo "=========================================="
    echo "时间: $(date)"
    echo ""
    get_build_status
done
