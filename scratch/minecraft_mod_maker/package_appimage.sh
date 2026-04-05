#!/bin/bash
set -e

# Configuration
APP_NAME="minecraft_mod_maker"
APP_DIR="AppDir"
JAR_NAME="minecraft_mod_maker-1.0-SNAPSHOT-jar-with-dependencies.jar"
ICON_PATH="/home/vladimir/.gemini/antigravity/brain/713c7c47-512e-4b05-8fe3-78b5eeed1581/minecraft_mod_maker_icon_1775163197780.png"

echo "Building AppImage for $APP_NAME..."

# 1. Build project
mvn clean package

# 2. Setup AppDir
rm -rf $APP_DIR
mkdir -p $APP_DIR/usr/bin
mkdir -p $APP_DIR/usr/lib
mkdir -p $APP_DIR/usr/share/applications
mkdir -p $APP_DIR/usr/share/icons/hicolor/256x256/apps

# 3. Create Custom JRE
echo "Creating custom JRE..."
# Modules needed for Swing and networking (HTTPS for MDK download)
MODULES="java.base,java.desktop,java.logging,jdk.crypto.ec,java.management,java.naming,java.security.jgss,java.sql,java.xml"
jlink --add-modules $MODULES --output $APP_DIR/usr/lib/jre --strip-debug --compress 2 --no-header-files --no-man-pages

# 4. Copy JAR
cp target/$JAR_NAME $APP_DIR/usr/lib/app.jar

# 5. Create Launcher Script
cat > $APP_DIR/usr/bin/$APP_NAME <<EOF
#!/bin/bash
SELF=\$(dirname "\$(readlink -f "\$0")")
JRE_BIN="\$SELF/../lib/jre/bin/java"
JAR_PATH="\$SELF/../lib/app.jar"
"\$JRE_BIN" -jar "\$JAR_PATH" "\$@"
EOF
chmod +x $APP_DIR/usr/bin/$APP_NAME

# 6. Create AppRun (required for AppImage)
cat > $APP_DIR/AppRun <<EOF
#!/bin/bash
SELF=\$(dirname "\$(readlink -f "\$0")")
export PATH="\$SELF/usr/bin:\$PATH"
exec "$APP_NAME" "\$@"
EOF
chmod +x $APP_DIR/AppRun

# 7. Create Desktop File
cat > $APP_DIR/$APP_NAME.desktop <<EOF
[Desktop Entry]
Type=Application
Name=Minecraft Mod Maker
Exec=$APP_NAME
Icon=$APP_NAME
Categories=Development;
Comment=Create Minecraft Forge Mods easily
Terminal=false
EOF

# 8. Set Icon
cp "$ICON_PATH" $APP_DIR/usr/share/icons/hicolor/256x256/apps/$APP_NAME.png
cp "$ICON_PATH" $APP_DIR/$APP_NAME.png

# 10. Download appimagetool if not exists
if [ ! -s "appimagetool-x86_64.AppImage" ]; then
    echo "Downloading appimagetool..."
    rm -f appimagetool-x86_64.AppImage
    wget -O appimagetool-x86_64.AppImage https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
fi
chmod +x appimagetool-x86_64.AppImage

# 11. Build AppImage
echo "Building final AppImage..."
export ARCH=x86_64
./appimagetool-x86_64.AppImage --appimage-extract-and-run $APP_DIR

echo "Done! Final AppImage created: Minecraft_Mod_Maker-x86_64.AppImage"
