#!/bin/bash

# 自动推送到GitHub的脚本

set -e

echo "🚀 开始推送到GitHub..."
echo "项目目录: $(pwd)"

# 检查是否在git仓库中
if [ ! -d .git ]; then
    echo "错误: 当前目录不是git仓库"
    exit 1
fi

# 设置用户名和邮箱
git config user.email "hfhzzdx@hotmail.com"
git config user.name "霍风浩"

# 添加所有文件
echo "📁 添加文件到git..."
git add .

# 提交更改
echo "💾 提交更改..."
git commit -m "Android短信读取应用 - 自动解析12123违法停车通知" || echo "没有新更改"

# 设置远程仓库（如果还没有）
if ! git remote | grep -q origin; then
    echo "🌐 设置远程仓库..."
    git remote add origin https://github.com/hfhzzdx/android-sms-reader.git
fi

# 重命名分支为main
echo "🌿 设置分支..."
git branch -M main

# 尝试推送
echo "⬆️ 推送到GitHub..."
echo "注意: 如果这是第一次推送，可能需要输入GitHub用户名和密码"
echo "用户名: hfhzzdx@hotmail.com"
echo "密码: HFHzzdx@163"
echo ""

# 尝试推送，如果失败则显示帮助信息
if git push -u origin main; then
    echo ""
    echo "✅ 推送成功！"
    echo ""
    echo "🎉 下一步："
    echo "1. 访问: https://github.com/hfhzzdx/android-sms-reader"
    echo "2. 点击 'Actions' 标签页"
    echo "3. 点击 'Android Build and Release'"
    echo "4. 点击 'Run workflow' 开始构建"
    echo ""
    echo "⏰ 构建通常需要7-11分钟"
    echo "📱 APK将在构建完成后自动生成"
else
    echo ""
    echo "⚠️ 推送失败，可能的原因："
    echo "1. GitHub仓库尚未创建"
    echo "2. 认证失败"
    echo ""
    echo "🔧 手动解决方案："
    echo ""
    echo "步骤1: 创建GitHub仓库"
    echo "   访问: https://github.com/new"
    echo "   仓库名称: android-sms-reader"
    echo "   描述: Android短信读取应用 - 自动解析12123违法停车通知"
    echo "   选择: Public"
    echo "   不要勾选: Initialize this repository with a README"
    echo ""
    echo "步骤2: 获取仓库URL"
    echo "   创建后，复制: https://github.com/hfhzzdx/android-sms-reader.git"
    echo ""
    echo "步骤3: 设置远程仓库"
    echo "   运行: git remote add origin https://github.com/hfhzzdx/android-sms-reader.git"
    echo "   运行: git push -u origin main"
    echo ""
    echo "步骤4: 如果还是失败，使用GitHub网页上传"
    echo "   1. 压缩项目: cd /home/hfh/.openclaw/workspace && zip -r android-sms-reader.zip android-sms-reader/"
    echo "   2. 在GitHub网页上传ZIP文件"
    echo ""
    echo "📞 备用联系方式："
    echo "   我可以直接提供代码文件，你可以手动上传"
fi

echo ""
echo "📱 应用功能摘要："
echo "   ✅ 读取12123违法停车短信"
echo "   ✅ 提取车牌号、时间、地点"
echo "   ✅ 语音和震动提醒"
echo "   ✅ 自动通知"
echo ""
echo "🕒 脚本完成时间: $(date)"