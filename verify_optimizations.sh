#!/bin/bash

echo "=== Android SMS Reader 优化验证 ==="
echo "验证时间: $(date '+%Y年%m月%d日 %H:%M:%S')"
echo ""

# 检查优化点1：短信筛选逻辑
echo "1. 检查短信筛选逻辑优化:"
if grep -q '12123401' "app/src/main/java/com/example/smsreader/MainActivity.kt"; then
    echo "  ✅ MainActivity支持12123401发件人"
else
    echo "  ❌ MainActivity不支持12123401发件人"
fi

if grep -q '12123401' "app/src/main/java/com/example/smsreader/SmsParser.kt"; then
    echo "  ✅ SmsParser支持12123401发件人"
else
    echo "  ❌ SmsParser不支持12123401发件人"
fi

if grep -q 'selection = ".*12123401.*12123%"' "app/src/main/java/com/example/smsreader/MainActivity.kt"; then
    echo "  ✅ 短信查询条件正确（12123401 OR 12123%）"
else
    echo "  ❌ 短信查询条件不正确"
fi

echo ""

# 检查优化点2：首页显示优化
echo "2. 检查首页显示优化:"

# 检查新布局文件
if [ -f "app/src/main/res/layout/activity_main_optimized.xml" ]; then
    echo "  ✅ 优化后的布局文件存在"
    
    # 检查关键组件
    if grep -q "recycler_records" "app/src/main/res/layout/activity_main_optimized.xml"; then
        echo "  ✅ 包含RecyclerView列表"
    else
        echo "  ❌ 不包含RecyclerView列表"
    fi
    
    if grep -q "card_status" "app/src/main/res/layout/activity_main_optimized.xml"; then
        echo "  ✅ 包含状态卡片"
    else
        echo "  ❌ 不包含状态卡片"
    fi
    
    if grep -q "支持发件人: 12123401" "app/src/main/res/layout/activity_main_optimized.xml"; then
        echo "  ✅ 底部信息显示正确的发件人支持"
    else
        echo "  ❌ 底部信息未显示发件人支持"
    fi
else
    echo "  ❌ 优化后的布局文件不存在"
fi

# 检查新Activity
if [ -f "app/src/main/java/com/example/smsreader/MainActivityOptimized.kt" ]; then
    echo "  ✅ 优化后的Activity存在"
    
    if grep -q "SmsReaderHelper" "app/src/main/java/com/example/smsreader/MainActivityOptimized.kt"; then
        echo "  ✅ 使用SmsReaderHelper工具类"
    else
        echo "  ❌ 未使用SmsReaderHelper工具类"
    fi
    
    if grep -q "ViolationAdapter" "app/src/main/java/com/example/smsreader/MainActivityOptimized.kt"; then
        echo "  ✅ 使用ViolationAdapter适配器"
    else
        echo "  ❌ 未使用ViolationAdapter适配器"
    fi
else
    echo "  ❌ 优化后的Activity不存在"
fi

# 检查适配器
if [ -f "app/src/main/java/com/example/smsreader/adapter/ViolationAdapter.kt" ]; then
    echo "  ✅ ViolationAdapter适配器存在"
else
    echo "  ❌ ViolationAdapter适配器不存在"
fi

# 检查列表项布局
if [ -f "app/src/main/res/layout/item_violation_record.xml" ]; then
    echo "  ✅ 列表项布局文件存在"
else
    echo "  ❌ 列表项布局文件不存在"
fi

# 检查工具类
if [ -f "app/src/main/java/com/example/smsreader/SmsReaderHelper.kt" ]; then
    echo "  ✅ SmsReaderHelper工具类存在"
else
    echo "  ❌ SmsReaderHelper工具类不存在"
fi

# 检查数据模型
if [ -f "app/src/main/java/com/example/smsreader/model/SmsInfo.kt" ]; then
    echo "  ✅ SmsInfo数据模型存在"
else
    echo "  ❌ SmsInfo数据模型不存在"
fi

echo ""

# 检查颜色资源
echo "3. 检查颜色资源:"
if [ -f "app/src/main/res/values/colors.xml" ]; then
    echo "  ✅ 颜色资源文件存在"
    
    if grep -q "primary" "app/src/main/res/values/colors.xml"; then
        echo "  ✅ 包含主色调定义"
    else
        echo "  ❌ 不包含主色调定义"
    fi
    
    if grep -q "violation_serious" "app/src/main/res/values/colors.xml"; then
        echo "  ✅ 包含违章颜色定义"
    else
        echo "  ❌ 不包含违章颜色定义"
    fi
else
    echo "  ❌ 颜色资源文件不存在"
fi

echo ""

# 检查状态背景
echo "4. 检查状态背景资源:"
status_bgs=("bg_status_new.xml" "bg_status_today.xml" "bg_status_recent.xml" "bg_status_old.xml")
all_bgs_exist=true

for bg in "${status_bgs[@]}"; do
    if [ -f "app/src/main/res/drawable/$bg" ]; then
        echo "  ✅ $bg 存在"
    else
        echo "  ❌ $bg 不存在"
        all_bgs_exist=false
    fi
done

echo ""

# 检查AndroidManifest配置
echo "5. 检查AndroidManifest配置:"
if grep -q "MainActivityOptimized" "app/src/main/AndroidManifest.xml"; then
    echo "  ✅ AndroidManifest配置了优化后的Activity"
    
    if grep -q 'android:theme="@style/Theme.Material3.Light.NoActionBar"' "app/src/main/AndroidManifest.xml"; then
        echo "  ✅ 使用了Material3主题"
    else
        echo "  ❌ 未使用Material3主题"
    fi
else
    echo "  ❌ AndroidManifest未配置优化后的Activity"
fi

echo ""

# 总结
echo "=== 优化验证总结 ==="
echo ""
echo "优化点1：短信筛选逻辑"
echo "  - 支持发件人: 12123401"
echo "  - 支持发件人: 12123开头"
echo "  - 查询条件: ADDRESS = '12123401' OR ADDRESS LIKE '12123%'"
echo ""
echo "优化点2：首页显示优化"
echo "  - 现代化Material Design 3界面"
echo "  - RecyclerView列表显示违章记录"
echo "  - 状态卡片显示应用状态"
echo "  - 底部信息显示发件人支持"
echo "  - 优化的颜色和视觉设计"
echo ""
echo "新增功能："
echo "  - SmsReaderHelper: 短信读取工具类"
echo "  - ViolationAdapter: 违章记录适配器"
echo "  - SmsInfo扩展: 增强的数据模型"
echo "  - 完整的颜色资源系统"
echo "  - 状态标签背景资源"
echo ""
echo "建议下一步："
echo "1. 在Android Studio中构建项目"
echo "2. 测试新界面布局和功能"
echo "3. 验证短信筛选逻辑是否正确"
echo "4. 测试RecyclerView列表显示"
echo "5. 验证所有优化功能正常工作"