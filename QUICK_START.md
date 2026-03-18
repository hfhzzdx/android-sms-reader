# 快速开始指南

## 项目概述
这是一个Android应用，专门用于读取12123发来的交通违章短信，自动提取车牌号和违法行为，并提供震动和语音提醒。

## 环境要求
- Android Studio Flamingo 或更高版本
- JDK 17 或更高版本
- Android SDK 34
- Gradle 8.5

## 导入项目到Android Studio

### 方法1：直接打开
1. 打开Android Studio
2. 选择 "Open" 或 "Open an Existing Project"
3. 导航到 `android-sms-reader` 文件夹
4. 点击 "Open"

### 方法2：从版本控制导入
1. 在Android Studio中，选择 "Get from VCS"
2. 输入项目路径
3. 点击 "Clone"

## 构建和运行

### 使用脚本构建
```bash
# 进入项目目录
cd android-sms-reader

# 运行构建脚本
./build.sh

# 构建并安装到设备
./build.sh --install
```

### 手动构建
```bash
# 清理项目
gradle clean

# 构建Debug版本
gradle assembleDebug

# 安装到连接的设备
gradle installDebug
```

## 测试应用

### 1. 在模拟器上测试
1. 创建一个Android模拟器（API 21+）
2. 运行应用
3. 授予短信读取权限
4. 测试功能

### 2. 在真机上测试
1. 开启手机的USB调试
2. 连接手机到电脑
3. 运行 `./build.sh --install`
4. 在手机上授予权限

### 3. 测试短信接收
由于12123短信无法模拟，可以：
1. 修改 `SmsParser.kt` 中的发件人检查逻辑
2. 临时允许其他号码进行测试
3. 发送测试短信到设备

## 项目结构说明

### 核心文件
- `app/src/main/java/com/example/smsreader/MainActivity.kt` - 主界面
- `app/src/main/java/com/example/smsreader/SmsParser.kt` - 短信解析器
- `app/src/main/java/com/example/smsreader/SmsReceiver.kt` - 短信接收器
- `app/src/main/java/com/example/smsreader/AlertManager.kt` - 提醒管理器
- `app/src/main/java/com/example/smsreader/SmsMonitorService.kt` - 后台服务

### 配置文件
- `app/build.gradle` - 模块构建配置
- `build.gradle` - 项目构建配置
- `settings.gradle` - 项目设置
- `app/src/main/AndroidManifest.xml` - 应用清单

## 功能测试步骤

### 步骤1：权限测试
1. 安装应用
2. 打开应用
3. 点击"读取12123短信"
4. 授予短信读取权限
5. 确认权限请求正常

### 步骤2：短信读取测试
1. 确保有12123发来的历史短信
2. 点击"读取12123短信"
3. 查看是否能正确显示历史记录
4. 确认车牌号和违法行为提取正确

### 步骤3：实时监控测试
1. 点击"启动后台监控"
2. 发送测试短信到设备
3. 确认收到震动和语音提醒
4. 检查通知栏显示

### 步骤4：后台服务测试
1. 启动后台监控
2. 退出应用
3. 发送测试短信
4. 确认仍能收到提醒

## 常见问题解决

### 问题1：构建失败
**症状**：Gradle构建失败
**解决**：
- 检查JDK版本（需要JDK 17+）
- 同步Gradle依赖
- 清理并重新构建

### 问题2：权限被拒绝
**症状**：应用无法读取短信
**解决**：
- 在系统设置中手动开启权限
- 重新安装应用
- 检查Android版本兼容性

### 问题3：语音不播报
**症状**：收到短信但没有语音
**解决**：
- 检查TTS引擎是否安装
- 检查媒体音量
- 重启应用

### 问题4：后台服务被杀死
**症状**：退出应用后收不到提醒
**解决**：
- 将应用加入电池优化白名单
- 检查手机的后台限制设置

## 自定义修改

### 修改提醒内容
编辑 `SmsParser.kt` 中的 `getVoiceAlertContent` 方法：
```kotlin
fun getVoiceAlertContent(plateNumber: String, violation: String): String {
    return "您的车辆${plateNumber}有新的违章记录，请及时处理。"
}
```

### 修改震动模式
编辑 `AlertManager.kt` 中的 `VIBRATION_PATTERN`：
```kotlin
private val VIBRATION_PATTERN = longArrayOf(200, 500, 200, 500, 200, 500)
```

### 添加新的违法行为识别
编辑 `SmsParser.kt` 中的 `VIOLATION_KEYWORDS`：
```kotlin
private val VIOLATION_KEYWORDS = listOf(
    "未按规定停放",
    "违章停车",
    "违法停车",
    // 添加新的关键词
    "超速行驶",
    "闯红灯"
)
```

## 发布准备

### 1. 生成签名密钥
```bash
keytool -genkey -v -keystore my-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-alias
```

### 2. 配置发布版本
编辑 `app/build.gradle`：
```gradle
android {
    signingConfigs {
        release {
            storeFile file("my-release-key.jks")
            storePassword "password"
            keyAlias "my-alias"
            keyPassword "password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 3. 构建发布版本
```bash
gradle assembleRelease
```

## 支持与反馈

如有问题或建议，请：
1. 检查本文档的常见问题部分
2. 查看代码注释
3. 提交Issue或Pull Request

## 许可证
本项目采用MIT许可证。详情请见LICENSE文件。