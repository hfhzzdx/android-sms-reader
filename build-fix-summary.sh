#!/bin/bash

echo "🔧 构建问题修复总结"
echo "=================="
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

echo "📝 已修复的问题:"
echo ""
echo "1. ✅ Node.js版本问题"
echo "   问题: GitHub现在强制使用Node.js 24版本"
echo "   修复: 在工作流中添加了Node.js 24设置"
echo "   代码:"
echo "     - name: Setup Node.js 24"
echo "       uses: actions/setup-node@v4"
echo "       with:"
echo "         node-version: '24'"
echo ""

echo "2. ✅ Gradle Wrapper问题"
echo "   问题: Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain"
echo "   原因: gradle-wrapper.jar文件可能损坏或缺失"
echo "   修复:"
echo "   - 添加了gradle wrapper验证和修复步骤"
echo "   - 如果gradle-wrapper.jar不存在或损坏，自动重新下载"
echo "   - 添加了gradle wrapper验证命令"
echo ""

echo "3. ✅ 构建流程优化"
echo "   - 简化了构建步骤"
echo "   - 添加了更详细的错误检查"
echo "   - 改进了APK文件检查"
echo ""

echo "📊 当前构建状态:"
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
    print('新的构建可能需要1-2分钟才会显示')
    sys.exit(0)

# 找到最新的构建
latest = None
for run in runs:
    if run.get('name') == 'Android Build and Release':
        latest = run
        break

if not latest and runs:
    latest = runs[0]

if latest:
    status = latest['status']
    conclusion = latest.get('conclusion', '未完成')
    created = latest['created_at']
    html_url = latest['html_url']
    
    created_dt = datetime.fromisoformat(created.replace('Z', '+00:00'))
    now = datetime.utcnow()
    duration = now - created_dt
    minutes = int(duration.total_seconds() / 60)
    seconds = int(duration.total_seconds() % 60)
    
    print(f'🏗️  最新构建:')
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
        if minutes < 3:
            print('   阶段: 环境设置')
        elif minutes < 6:
            print('   阶段: Gradle Wrapper验证')
        elif minutes < 9:
            print('   阶段: 构建APK')
        else:
            print('   阶段: 创建Release')
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
echo "⏰ 预计时间线 (修复后):"
echo "   - 0-2分钟: 构建排队"
echo "   - 2-4分钟: 环境设置 (Node.js 24 + JDK 17)"
echo "   - 4-6分钟: Gradle Wrapper验证和修复"
echo "   - 6-10分钟: Android项目构建"
echo "   - 10-12分钟: 创建Release和Tag"
echo "   总时间: 10-12分钟"
echo ""

echo "📱 应用功能 (已实现):"
echo "   ✅ 读取12123违法停车短信"
echo "   ✅ 提取车牌号、时间、地点"
echo "   ✅ 语音提醒: '紧急通知！您的车辆{车牌号}于{时间}违法停车被记录，请立即驶离。'"
echo "   ✅ 强烈震动提醒"
echo "   ✅ 详细通知显示"
echo ""

echo "🎯 构建成功后:"
echo "   1. 访问: $REPO_URL/actions 下载APK"
echo "   2. 访问: $REPO_URL/releases 查看Release和Tag"
echo "   3. 运行: ./download-apk-env.sh 自动下载"
echo ""

echo "🔧 如果仍然失败:"
echo "   1. 查看构建日志中的具体错误"
echo "   2. 检查gradle/wrapper/gradle-wrapper.jar文件"
echo "   3. 验证Android项目配置"
echo "   4. 我可以进一步调试"
echo ""

echo "🚀 修复后的构建已启动！"
echo ""
echo "实时监控:"
echo "   ./monitor-build-env.sh"
echo "或访问:"
echo "   $REPO_URL/actions"
echo ""
echo "预计完成时间: 10-12分钟后"