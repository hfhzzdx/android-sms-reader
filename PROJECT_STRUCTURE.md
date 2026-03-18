# 项目结构说明

## 项目目录结构
```
android-sms-reader/
├── app/                          # 主模块
│   ├── src/main/
│   │   ├── java/com/example/smsreader/
│   │   │   ├── MainActivity.kt      # 主界面
│   │   │   ├── SmsParser.kt         # 短信解析器
│   │   │   ├── SmsReceiver.kt       # 短信广播接收器
│   │   │   ├── AlertManager.kt      # 提醒管理器
│   │   │   └── SmsMonitorService.kt # 后台监控服务
│   │   ├── res/                     # 资源文件
│   │   │   ├── layout/              # 布局文件
│   │   │   ├── values/              # 字符串、颜色、主题
│   │   │   └── ...
│   │   └── AndroidManifest.xml      # 应用清单
│   └── build.gradle                 # 模块构建配置
├── build.gradle                     # 项目构建配置
├── settings.gradle                  # 项目设置
├── gradlew                          # Gradle包装器
├── README.md                        # 项目说明
├── USAGE.md                         # 使用说明
├── PROJECT_STRUCTURE.md             # 项目结构说明
└── build.sh                         # 构建脚本
```

## 核心类说明

### 1. MainActivity.kt
- **功能**：主界面，权限请求，短信读取
- **主要方法**：
  - `checkAndRequestPermissions()`：检查并请求权限
  - `read12123Sms()`：读取12123短信
  - `startSmsMonitorService()`：启动后台服务

### 2. SmsParser.kt
- **功能**：解析短信内容，提取关键信息
- **主要方法**：
  - `extractPlateNumber()`：提取车牌号
  - `extractViolation()`：提取违法行为
  - `isTrafficViolationSms()`：判断是否为违章短信
  - `getVoiceAlertContent()`：生成语音内容

### 3. SmsReceiver.kt
- **功能**：接收短信广播，实时处理新短信
- **主要方法**：
  - `onReceive()`：接收广播，解析短信
  - `triggerAlert()`：触发提醒
  - `saveSmsRecord()`：保存短信记录

### 4. AlertManager.kt
- **功能**：管理震动、语音、通知提醒
- **主要方法**：
  - `vibrate()`：触发震动
  - `speak()`：语音播报
  - `showNotification()`：显示通知
  - `createNotificationChannel()`：创建通知渠道

### 5. SmsMonitorService.kt
- **功能**：后台服务，保持应用运行
- **主要方法**：
  - `startForegroundService()`：启动前台服务
  - `onStartCommand()`：服务启动命令处理

## 权限配置

### AndroidManifest.xml中的权限
```xml
<!-- 短信读取权限 -->
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />

<!-- 震动权限 -->
<uses-permission android:name="android.permission.VIBRATE" />

<!-- 前台服务权限 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<!-- 唤醒锁权限 -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## 广播接收器配置
```xml
<receiver
    android:name=".SmsReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter android:priority="999">
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

## 依赖库说明

### build.gradle中的依赖
```gradle
dependencies {
    // Android基础库
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    
    // 权限请求库
    implementation 'com.guolindev.permissionx:permissionx:1.7.1'
    
    // 生命周期管理
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
}
```

## 构建配置

### 编译选项
```gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 21        # 支持Android 5.0及以上
        targetSdk 34
    }
}
```

## 测试数据

### 测试短信示例
```
【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录，将依法予以处罚。并请立即驶离，未驶离的，将依法拖移，谢谢配合！
```

### 预期提取结果
- **车牌号**：豫ADS2567
- **违法行为**：未按规定停放
- **语音提醒**：您的车辆豫ADS2567未按规定停放已被记录，请立即驶离。

## 扩展建议

### 1. 数据库存储
- 添加Room数据库存储历史记录
- 支持查询、统计、导出功能

### 2. 云同步
- 添加云备份功能
- 多设备同步违章记录

### 3. 高级功能
- 违章地点地图显示
- 缴费提醒功能
- 驾驶证扣分统计

### 4. 界面优化
- 图表展示违章统计
- 主题切换功能
- 多语言支持

## 注意事项

### 1. 权限处理
- 动态请求危险权限
- 优雅处理权限拒绝
- 提供权限说明

### 2. 后台限制
- 适配不同厂商的后台限制
- 使用前台服务保持运行
- 处理省电模式限制

### 3. 兼容性
- 测试不同Android版本
- 处理API差异
- 适配不同屏幕尺寸

## 构建和运行

### 构建APK
```bash
./build.sh
```

### 安装到设备
```bash
./build.sh --install
```

### 直接运行
```bash
./gradlew installDebug
```