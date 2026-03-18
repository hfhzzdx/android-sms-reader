#!/bin/bash

# 使用环境变量从GitHub下载APK

set -e

echo "📥 从GitHub下载APK (使用环境变量)"
echo "================================"
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

echo "✅ 环境变量检查通过:"
echo "   用户名: $GITHUB_USERNAME"
echo "   仓库: $REPO_URL"
echo ""

# 检查构建状态
echo "🔍 检查构建状态..."
RUNS_JSON=$(curl -s -H "Authorization: token $GITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  "$API_URL/actions/runs")

STATUS_INFO=$(echo "$RUNS_JSON" | python3 -c "
import json, sys
from datetime import datetime

data = json.load(sys.stdin)
runs = data.get('workflow_runs', [])

if not runs:
    print('none,none,无构建运行')
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
    html_url = latest['html_url']
    
    if status == 'queued':
        print(f'{run_id},queued,构建排队中')
    elif status == 'in_progress':
        print(f'{run_id},in_progress,构建进行中')
    elif status == 'completed':
        if conclusion == 'success':
            print(f'{run_id},success,构建成功')
        else:
            print(f'{run_id},{conclusion},构建失败: {conclusion}')
    else:
        print(f'{run_id},{status},状态: {status}')
")

RUN_ID=$(echo "$STATUS_INFO" | cut -d',' -f1)
STATUS=$(echo "$STATUS_INFO" | cut -d',' -f2)
MESSAGE=$(echo "$STATUS_INFO" | cut -d',' -f3)

echo "📊 构建状态: $MESSAGE"

if [ "$STATUS" = "queued" ]; then
    echo "⏳ 构建排队中，请等待1-2分钟"
    echo "监控: ./monitor-build-env.sh"
    exit 0
elif [ "$STATUS" = "in_progress" ]; then
    echo "🚀 构建进行中，请等待5-8分钟"
    echo "监控: ./monitor-build-env.sh"
    exit 0
elif [ "$STATUS" != "success" ]; then
    echo "❌ 构建未成功: $MESSAGE"
    echo "请检查: $REPO_URL/actions"
    exit 1
fi

echo "✅ 构建成功！开始下载APK..."
echo ""

# 从Releases下载APK
echo "📦 检查Releases..."
RELEASES_JSON=$(curl -s -H "Authorization: token $GITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  "$API_URL/releases/latest")

DOWNLOAD_URL=$(echo "$RELEASES_JSON" | python3 -c "
import json, sys
data = json.load(sys.stdin)
for asset in data.get('assets', []):
    if asset['name'].endswith('.apk'):
        print(asset['browser_download_url'])
        break
" 2>/dev/null)

if [ -z "$DOWNLOAD_URL" ]; then
    echo "⚠️ 未在Releases中找到APK，尝试从Artifacts下载..."
    
    # 从Artifacts下载
    ARTIFACTS_JSON=$(curl -s -H "Authorization: token $GITHUB_PASSWORD" \
      -H "Accept: application/vnd.github.v3+json" \
      "$API_URL/actions/runs/$RUN_ID/artifacts")
    
    DOWNLOAD_URL=$(echo "$ARTIFACTS_JSON" | python3 -c "
import json, sys
data = json.load(sys.stdin)
for artifact in data.get('artifacts', []):
    if 'apk' in artifact['name'].lower():
        print(artifact['archive_download_url'])
        break
" 2>/dev/null)
    
    if [ -z "$DOWNLOAD_URL" ]; then
        echo "❌ 未找到APK文件"
        echo "请手动下载: $REPO_URL/actions"
        exit 1
    fi
fi

# 下载APK
APK_FILE="android-sms-reader.apk"
echo "⬇️ 下载APK: $DOWNLOAD_URL"
curl -s -L -H "Authorization: token $GITHUB_PASSWORD" \
  -o "$APK_FILE" "$DOWNLOAD_URL"

if [ $? -eq 0 ] && [ -f "$APK_FILE" ]; then
    SIZE=$(du -h "$APK_FILE" | cut -f1)
    echo "✅ 下载成功: $APK_FILE ($SIZE)"
    
    echo ""
    echo "📋 APK信息:"
    echo "   文件: $(readlink -f "$APK_FILE")"
    echo "   大小: $SIZE"
    echo "   MD5: $(md5sum "$APK_FILE" | cut -d' ' -f1)"
    
    echo ""
    echo "🚀 安装命令:"
    echo "   adb install $APK_FILE"
    echo "   adb install -r $APK_FILE  # 重新安装"
    
    echo ""
    echo "📱 应用功能:"
    echo "   ✅ 读取12123违法停车短信"
    echo "   ✅ 语音+震动提醒"
    echo "   ✅ 自动解析车牌号、时间、地点"
    
else
    echo "❌ 下载失败"
    echo "请手动下载: $REPO_URL/actions"
fi

echo ""
echo "🌐 相关链接:"
echo "   Actions: $REPO_URL/actions"
echo "   Releases: $REPO_URL/releases"
echo "   仓库: $REPO_URL"
echo ""
echo "🎯 测试方法:"
echo "   发送测试短信（发件人: 12123）验证功能"