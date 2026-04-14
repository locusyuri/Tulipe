#!/bin/bash

# Tulipe 项目自动化构建脚本

# 配置路径
GRAALVM_HOME="C:/Users/Violet/scoop/apps/graalvm/current"
BACKEND_DIR="src-backend"
TAURI_DIR="src-tauri"
BINARIES_DIR="$TAURI_DIR/binaries/x86_64-pc-windows-msvc"

# 1. 构建后端 Native Image
echo ">>> [1/4] 构建 SpringBoot Native Image..."
cd $BACKEND_DIR
export JAVA_HOME=$GRAALVM_HOME
export GRAALVM_HOME=$GRAALVM_HOME

# 注意：在 Windows 上构建需要 MSVC 环境。这里调用 powershell 脚本完成。
powershell.exe -ExecutionPolicy Bypass -File ./build_native.ps1
if [ $? -ne 0 ]; then
    echo "后端构建失败！"
    exit 1
fi
cd ..

# 2. 准备分发目录
echo ">>> [2/4] 准备分发目录..."
mkdir -p $BINARIES_DIR

# 3. 复制可执行文件
echo ">>> [3/4] 复制二进制文件到 Tauri 目录..."
# 注意：生成的 exe 名称通常是项目名，这里假设是 src-backend.exe
cp $BACKEND_DIR/build/native/nativeCompile/src-backend.exe $BINARIES_DIR/src-backend.exe
if [ $? -ne 0 ]; then
    echo "文件复制失败！请检查生成的文件路径。"
    exit 1
fi

# 4. 构建 Tauri 应用
echo ">>> [4/4] 构建 Tauri 安装包..."
npm run tauri build

echo ">>> 构建完成！安装包位于 $TAURI_DIR/target/release/bundle/"
