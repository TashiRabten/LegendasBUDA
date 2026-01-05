#!/bin/bash
# Build script for LegendasBUDA macOS PKG installer

echo "======================================"
echo "LegendasBUDA macOS Build Script"
echo "======================================"

# Step 1: Clean and build JAR
echo ""
echo "[1/3] Building JAR with dependencies..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "ERROR: Maven build failed!"
    exit 1
fi
echo "JAR created: target/LegendasBUDA-standalone.jar"

# Step 2: Create custom JRE
echo ""
echo "[2/3] Creating custom Java runtime..."
rm -rf target/java-runtime
jlink --add-modules java.base,java.desktop,java.logging,java.xml \
      --output target/java-runtime \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=2
if [ $? -ne 0 ]; then
    echo "ERROR: jlink failed!"
    exit 1
fi
echo "Custom JRE created at target/java-runtime"

# Step 3: Prepare jpackage input folder
echo ""
echo "[3/3] Preparing jpackage input folder..."
INPUT_FOLDER="target/jpackage-input"
rm -rf "$INPUT_FOLDER"
mkdir -p "$INPUT_FOLDER"
cp target/LegendasBUDA-standalone.jar "$INPUT_FOLDER/"

# Step 4: Create installer with jpackage
echo ""
echo "[4/4] Creating macOS PKG installer..."
rm -rf target/dist

jpackage --type pkg \
    --name "LegendasBUDA" \
    --app-version "1.0.0" \
    --vendor "Associacao BUDA" \
    --icon src/main/resources/icons/BUDA.icns \
    --input "$INPUT_FOLDER" \
    --main-jar LegendasBUDA-standalone.jar \
    --main-class com.budaassociacao.legendas.LegendasApp \
    --runtime-image target/java-runtime \
    --dest target/dist \
    --mac-package-identifier "com.budaassociacao.legendas" \
    --java-options "-Dfile.encoding=UTF-8" \
    --verbose

if [ $? -ne 0 ]; then
    echo ""
    echo "======================================"
    echo "ERROR: jpackage failed!"
    echo "======================================"
    exit 1
fi

echo ""
echo "======================================"
echo "Build Complete!"
echo "======================================"
echo ""
echo "Installer created: target/dist/LegendasBUDA-1.0.0.pkg"
echo ""

# Cleanup input folder
rm -rf "$INPUT_FOLDER"
