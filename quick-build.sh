#!/bin/bash

# 快速构建脚本 - 跳过测试和lint检查

echo "🚀 开始快速构建Android短信读取应用"
echo "时间: $(date)"

# 设置环境
export ANDROID_HOME=~/Android/Sdk
export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"

# 清理
echo "🧹 清理项目..."
./gradlew clean --no-daemon 2>&1 | grep -i "success\|BUILD" || true

# 只构建必要的任务
echo "🔨 构建APK（跳过测试和lint）..."
./gradlew assembleDebug \
  -x lint \
  -x test \
  -x lintVitalRelease \
  -x testReleaseUnitTest \
  --no-daemon \
  --console=plain \
  --build-cache \
  --parallel 2>&1 | tee quick-build.log

# 检查结果
if [ $? -eq 0 ]; then
    echo "✅ 构建成功！"
    
    # 查找APK
    APK_PATH=$(find . -name "app-debug.apk" -type f 2>/dev/null | head -1)
    
    if [ -n "$APK_PATH" ]; then
        echo "📱 APK文件: $APK_PATH"
        echo "📏 文件大小: $(du -h "$APK_PATH" | cut -f1)"
        
        # 复制到当前目录
        cp "$APK_PATH" ./sms-reader-quick.apk
        echo "📦 已复制到: ./sms-reader-quick.apk"
        
        echo ""
        echo "🎉 构建完成！"
        echo "APK路径: $(readlink -f ./sms-reader-quick.apk)"
        echo ""
        echo "安装命令:"
        echo "  adb install ./sms-reader-quick.apk"
        echo ""
        echo "测试短信示例:"
        echo "  发件人: 12123"
        echo "  内容: 【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录..."
    else
        echo "⚠️ 构建成功但未找到APK文件"
        echo "尝试查找其他APK文件:"
        find . -name "*.apk" -type f 2>/dev/null
    fi
else
    echo "❌ 构建失败"
    echo "最后错误信息:"
    tail -50 quick-build.log | grep -i "error\|fail\|exception"
fi

echo ""
echo "⏰ 构建结束时间: $(date)"