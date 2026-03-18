#!/bin/bash

# 从GitHub Actions下载APK

set -e

echo "📥 从GitHub Actions下载APK"
echo "========================="
echo "仓库: hfhzzdx/android-sms-reader"
echo "时间: $(date)"
echo ""

# 获取最新的工作流运行ID
echo "🔍 查找最新的构建..."
RUN_INFO=$(curl -s -H "Authorization: token $HITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/repos/hfhzzdx/android-sms-reader/actions/runs \
  | python3 -c "
import json, sys
data = json.load(sys.stdin)
runs = data.get('workflow_runs', [])

# 优先找成功的构建
for run in runs:
    if run.get('name') == 'Android Build and Release':
        if run.get('conclusion') == 'success':
            print(f'{run[\"id\"]},success')
            break
        elif run.get('status') == 'in_progress':
            print(f'{run[\"id\"]},in_progress')
            break
        elif run.get('status') == 'queued':
            print(f'{run[\"id\"]},queued')
            break
else:
    if runs:
        first_run = runs[0]
        print(f'{first_run[\"id\"]},{first_run.get(\"conclusion\", first_run.get(\"status\", \"unknown\"))}')
    else:
        print('none,none')
" 2>/dev/null)

RUN_ID=$(echo "$RUN_INFO" | cut -d',' -f1)
STATUS=$(echo "$RUN_INFO" | cut -d',' -f2)

if [ "$RUN_ID" = "none" ]; then
    echo "❌ 未找到任何构建运行"
    echo "请先触发构建: https://github.com/hfhzzdx/android-sms-reader/actions/workflows/android-build.yml"
    exit 1
fi

echo "✅ 找到构建运行ID: $RUN_ID"
echo "📊 状态: $STATUS"

if [ "$STATUS" = "queued" ]; then
    echo "⏳ 构建排队中，请等待1-2分钟"
    echo "实时监控: ./monitor-build.sh"
    exit 0
elif [ "$STATUS" = "in_progress" ]; then
    echo "🚀 构建进行中，请等待5-8分钟"
    echo "实时监控: ./monitor-build.sh"
    exit 0
elif [ "$STATUS" != "success" ]; then
    echo "⚠️ 构建状态: $STATUS"
    echo "请检查构建日志: https://github.com/hfhzzdx/android-sms-reader/actions/runs/$RUN_ID"
    exit 1
fi

# 获取Artifacts
echo "📦 获取Artifacts列表..."
ARTIFACTS=$(curl -s -H "Authorization: token $GITHUB_PASSWORD" \
  -H "Accept: application/vnd.github.v3+json" \
  "https://api.github.com/repos/hfhzzdx/android-sms-reader/actions/runs/$RUN_ID/artifacts")

# 查找APK artifact
APK_URL=$(echo "$ARTIFACTS" | python3 -c "
import json, sys
data = json.load(sys.stdin)
for artifact in data.get('artifacts', []):
    if 'apk' in artifact['name'].lower():
        print(artifact['archive_download_url'])
        break
" 2>/dev/null)

if [ -z "$APK_URL" ]; then
    echo "⚠️ 未找到APK artifact，尝试从Releases下载..."
    
    # 尝试从Releases下载
    RELEASE_URL=$(curl -s -H "Authorization: token $GITHUB_PASSWORD" \
      -H "Accept: application/vnd.github.v3+json" \
      https://api.github.com/repos/hfhzzdx/android-sms-reader/releases/latest \
      | python3 -c "
import json, sys
data = json.load(sys.stdin)
for asset in data.get('assets', []):
    if asset['name'].endswith('.apk'):
        print(asset['browser_download_url'])
        break
" 2>/dev/null)
    
    if [ -n "$RELEASE_URL" ]; then
        APK_URL="$RELEASE_URL"
        echo "✅ 找到Release APK: $APK_URL"
    else
        echo "❌ 未找到APK文件"
        echo "请手动下载: https://github.com/hfhzzdx/android-sms-reader/actions"
        exit 1
    fi
else
    echo "✅ 找到APK artifact"
fi

# 下载APK
echo "⬇️ 下载APK..."
APK_FILE="sms-reader-github.apk"
curl -s -L -H "Authorization: token $GITHUB_PASSWORD" \
  -o "$APK_FILE" "$APK_URL"

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
    echo "请手动下载: https://github.com/hfhzzdx/android-sms-reader/actions"
fi

echo ""
echo "🌐 相关链接:"
echo "   Actions: https://github.com/hfhzzdx/android-sms-reader/actions"
echo "   Releases: https://github.com/hfhzzdx/android-sms-reader/releases"
echo "   仓库: https://github.com/hfhzzdx/android-sms-reader"
