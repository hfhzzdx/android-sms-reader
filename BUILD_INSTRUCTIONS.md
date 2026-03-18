# Android短信读取应用 - 构建说明

## 应用功能
- 读取短信收件箱
- 自动识别12123开头的交管短信
- 解析违法停车通知（如："【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录"）
- 语音和震动提醒用户
- 显示解析后的通知信息

## 构建方法

### 方法1：使用Android Studio（推荐）
1. 打开Android Studio
2. 选择 "Open an existing Android Studio project"
3. 导航到 `/home/hfh/.openclaw/workspace/android-sms-reader` 并打开
4. 等待Gradle同步完成
5. 点击 "Build" → "Make Project"
6. APK文件将生成在：`app/build/outputs/apk/debug/app-debug.apk`

### 方法2：使用命令行
```bash
# 进入项目目录
cd /home/hfh/.openclaw/workspace/android-sms-reader

# 清理项目
./gradlew clean

# 构建调试版APK
./gradlew assembleDebug

# 构建发布版APK（需要签名配置）
./gradlew assembleRelease
```

### 方法3：使用Docker构建
如果本地环境有问题，可以使用Docker：
```bash
# 使用Android构建镜像
docker run --rm -v $(pwd):/project -w /project \
  registry.gitlab.com/gitlab-org/android-build/android-29:latest \
  ./gradlew assembleDebug
```

## APK文件位置
构建成功后，APK文件位于：
- 调试版：`app/build/outputs/apk/debug/app-debug.apk`
- 发布版：`app/build/outputs/apk/release/app-release.apk`

## 应用权限
应用需要以下权限：
1. `READ_SMS` - 读取短信
2. `RECEIVE_SMS` - 接收短信
3. `VIBRATE` - 震动提醒
4. `INTERNET` - 网络访问（未来可能扩展）

## 功能测试
### 测试短信格式
```
【河南省交警】您的小型新能源汽车豫ADS2567于2025年11月18日9时32分在河南省郑州市建设路（京广路至大学路）路南未按规定停放已被记录，将依法予以处罚。并请立即驶离，未驶离的，将依法拖移，谢谢配合!
```

### 预期行为
1. 收到短信后自动解析
2. 提取车牌号：豫ADS2567
3. 提取时间：2025年11月18日9时32分
4. 提取地点：河南省郑州市建设路（京广路至大学路）路南
5. 触发语音提醒："紧急通知！您的车辆豫ADS2567于2025年11月18日9时32分违法停车被记录，请立即驶离。"
6. 触发强烈震动
7. 显示通知提醒

## 代码结构
```
app/src/main/java/com/example/smsreader/
├── MainActivity.kt          # 主界面
├── SmsReceiver.kt          # 短信接收器
├── SmsParser.kt            # 短信解析器（已增强）
├── AlertManager.kt         # 提醒管理器（已增强）
└── TrafficViolation.kt     # 数据类
```

## 自定义配置
### 修改提醒方式
编辑 `AlertManager.kt`：
- 修改震动模式：`VIBRATION_PATTERN` 和 `URGENT_VIBRATION_PATTERN`
- 修改语音内容：`generateAlertMessage()` 方法
- 修改通知样式：`sendNotification()` 方法

### 扩展解析规则
编辑 `SmsParser.kt`：
- 添加新的短信模式：`ILLEGAL_PARKING_PATTERN`
- 添加新的关键词：`ILLEGAL_PARKING_KEYWORDS`
- 修改正则表达式以匹配更多格式

## 问题排查
### 构建失败
1. 检查Java版本（需要JDK 11+）
2. 检查Android SDK路径
3. 检查网络连接（下载依赖需要网络）
4. 清理项目：`./gradlew clean`

### 应用崩溃
1. 检查权限是否已授予
2. 检查Android版本兼容性（最低API 21）
3. 查看Logcat输出：`adb logcat | grep SmsReader`

### 短信无法解析
1. 检查短信格式是否匹配
2. 查看解析日志：`adb logcat | grep SmsParser`
3. 调整正则表达式模式

## 后续扩展
1. **数据库存储**：保存历史违章记录
2. **统计功能**：统计违章次数和地点
3. **地图集成**：在地图上显示违章地点
4. **提醒设置**：自定义提醒时间和方式
5. **多车辆支持**：管理多辆车的违章信息
6. **云同步**：将数据同步到云端

## 联系支持
如有问题，请联系：
- 开发者：霍风浩
- 项目路径：`/home/hfh/.openclaw/workspace/android-sms-reader`
- 构建时间：2026-03-18