# Android短信读取应用 - GitHub Actions构建指南

## 📱 应用功能
- 自动读取短信收件箱
- 识别12123开头的交管短信
- 解析违法停车通知，提取车牌号、时间、地点
- 语音和震动提醒用户
- 显示详细的通知信息

## 🚀 GitHub Actions自动构建

### 如何获取APK

1. **自动构建**：每次推送到main/master分支时，GitHub Actions会自动构建APK
2. **手动构建**：在GitHub仓库的Actions标签页，点击"Android Build and Release"，然后点击"Run workflow"

### APK下载位置

构建成功后，APK可以在以下位置下载：
1. **GitHub Releases**：最新的发布版本
2. **Artifacts**：每次构建的产物（保留7天）
3. **Actions运行详情**：下载构建的APK文件

### 构建状态徽章

将以下Markdown添加到你的README中：

```markdown
![Android Build](https://github.com/你的用户名/仓库名/actions/workflows/android-build.yml/badge.svg)
```

## 🔧 本地开发

### 环境要求
- Android Studio Flamingo 或更高版本
- JDK 17
- Android SDK 34

### 构建命令
```bash
# 清理项目
./gradlew clean

# 构建调试版APK
./gradlew assembleDebug

# 构建发布版APK
./gradlew assembleRelease
```

## 📋 应用配置

### 权限要求
```xml
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-feature android:name="android.hardware.telephony" android:required="false" />
```

### 短信解析规则
应用专门解析以下格式的短信：
```
【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录...
```

**提取信息：**
- 车牌号：豫ADS2567
- 违法时间：2025年11月18日9时32分
- 违法地点：河南省郑州市建设路（京广路至大学路）路南

## 🎯 测试方法

### 1. 安装APK
```bash
adb install app-debug.apk
```

### 2. 授予权限
- 打开应用
- 授予短信读取权限
- 授予通知权限

### 3. 发送测试短信
使用以下内容发送短信到测试设备：
```
发件人：12123
内容：【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录，将依法予以处罚。并请立即驶离，未驶离的，将依法拖移，谢谢配合!
```

### 4. 预期结果
- 应用自动解析短信
- 语音提醒："紧急通知！您的车辆豫ADS2567于2025年11月18日9时32分违法停车被记录，请立即驶离。"
- 强烈震动提醒
- 显示通知详情

## 🔍 问题排查

### 构建失败
1. 检查GitHub Actions日志
2. 确保gradle-wrapper.properties中的Gradle版本正确
3. 检查Android SDK版本兼容性

### 应用崩溃
1. 检查权限是否已授予
2. 查看logcat日志：`adb logcat | grep SmsReader`
3. 检查Android版本兼容性（最低API 21）

### 短信无法解析
1. 检查短信格式是否匹配
2. 查看解析日志：`adb logcat | grep SmsParser`
3. 调整SmsParser.kt中的正则表达式

## 📞 支持与贡献

### 报告问题
在GitHub Issues中报告问题，包括：
1. Android设备型号和版本
2. 复现步骤
3. 错误日志
4. 测试短信内容

### 贡献代码
1. Fork仓库
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证。详见LICENSE文件。

## 🙏 致谢

感谢以下开源项目：
- Android官方文档和示例
- Kotlin协程库
- Android Jetpack组件

---

**构建时间**: 2026-03-18  
**版本**: 1.0.0  
**开发者**: 霍风浩