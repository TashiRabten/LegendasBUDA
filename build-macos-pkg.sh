#!/bin/bash
# ========================================
# LegendasBUDA - macOS PKG Installer Builder
# Uses jlink + jpackage for native macOS application
# ========================================
set -e

APP_NAME="LegendasBUDA"
APP_VERSION="1.0.0"
VENDOR="Associacao BUDA"

echo "========================================"
echo "$APP_NAME - macOS PKG Builder"
echo "========================================"
echo ""

# Build directories
BUILD_DIR="/Users/Shared/$APP_NAME"
INPUT_DIR="$BUILD_DIR/input"
OUTPUT_DIR="$BUILD_DIR/output-macos"
RUNTIME_DIR="$BUILD_DIR/runtime-macos"

# JDK module path
JDK_JMODS="/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home/jmods"

# Step 1: Ensure JDK path exists
echo "[1/4] Verifying JDK installation..."
if [ ! -d "$JDK_JMODS" ]; then
    echo "ERROR: JDK jmods not found at $JDK_JMODS"
    exit 1
fi
echo "JDK found"
echo ""

# Step 2: Verify JAR exists
echo "[2/4] Verifying JAR file..."
JAR_PATH="$BUILD_DIR/$APP_NAME-standalone.jar"
if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: JAR not found at $JAR_PATH"
    echo ""
    echo "Please copy the JAR from Windows to macOS VM:"
    echo "  1. Build on Windows: mvn clean package -DskipTests"
    echo "  2. Copy target/LegendasBUDA-standalone.jar to $JAR_PATH"
    exit 1
fi

JAR_SIZE=$(ls -lh "$JAR_PATH" | awk '{print $5}')
echo "JAR found: $JAR_PATH ($JAR_SIZE)"
echo ""

# Step 3: Prepare input directory
echo "[3/4] Preparing input directory..."
if [ -d "$INPUT_DIR" ]; then
    rm -rf "$INPUT_DIR"
fi
mkdir -p "$INPUT_DIR"

# Copy JAR
cp "$JAR_PATH" "$INPUT_DIR/$APP_NAME.jar"
echo "JAR copied to input directory"

echo "Input directory contents:"
ls -la "$INPUT_DIR"
echo ""

# Step 4: Create custom JRE with jlink
echo "[4/5] Creating custom JRE with jlink..."
if ! command -v jlink &> /dev/null; then
    echo "ERROR: jlink not found. Make sure you're using JDK 17 or later."
    exit 1
fi

if [ -d "$RUNTIME_DIR" ]; then
    echo "Runtime folder already exists at $RUNTIME_DIR. Removing..."
    rm -rf "$RUNTIME_DIR"
fi

echo "Creating custom JRE at $RUNTIME_DIR..."
jlink --module-path "$JDK_JMODS" \
      --add-modules java.base,java.desktop,java.logging,java.xml \
      --bind-services \
      --output "$RUNTIME_DIR" \
      --strip-debug \
      --compress=2 \
      --no-header-files \
      --no-man-pages

echo "Custom JRE created successfully."
echo ""

# Step 5: Create macOS PKG with jpackage
echo "[5/5] Creating macOS PKG with jpackage..."
if ! command -v jpackage &> /dev/null; then
    echo "ERROR: jpackage not found. Make sure you're using JDK 17 or later."
    exit 1
fi

if [ -d "$OUTPUT_DIR" ]; then
    rm -rf "$OUTPUT_DIR"
fi
mkdir -p "$OUTPUT_DIR"

# Check for icon
ICON_PATH="$BUILD_DIR/BUDA.icns"
ICON_ARG=""
if [ -f "$ICON_PATH" ]; then
    ICON_ARG="--icon $ICON_PATH"
    echo "Using icon: $ICON_PATH"
else
    echo "Warning: Icon not found at $ICON_PATH (will use default)"
fi

jpackage --type pkg \
    --input "$INPUT_DIR" \
    --dest "$OUTPUT_DIR" \
    --name "$APP_NAME" \
    --main-jar "$APP_NAME.jar" \
    --main-class "com.budaassociacao.legendas.LegendasApp" \
    --runtime-image "$RUNTIME_DIR" \
    $ICON_ARG \
    --app-version "$APP_VERSION" \
    --vendor "$VENDOR" \
    --description "LegendasBUDA - Visualizador de legendas para vídeos do YouTube"

echo ""
echo "========================================"
echo "Build completed successfully!"
echo "========================================"
echo ""
echo "Output location: $OUTPUT_DIR"
echo "- PKG installer: $OUTPUT_DIR/$APP_NAME-$APP_VERSION.pkg"
echo ""
echo "The installer includes:"
echo "  - Java runtime (~50MB)"
echo "  - Application JAR (~15MB)"
echo ""
echo "To install:"
echo "  1. Double-click the PKG file"
echo "  2. Follow the installation wizard"
echo "  3. The application will be installed to /Applications"
echo ""
echo "Note: You may need to allow the app in System Preferences > Security & Privacy"
echo ""
