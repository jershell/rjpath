#!/usr/bin/env bash
set -euo pipefail

# === CONFIGURATION ===
MODULE="rjpath"
SRC_KTS_FILE="./$MODULE/build.gradle.kts"
BUILD_DIR="./build"
DIST_DIR="$BUILD_DIR/dist"
M2_BASE="$HOME/.m2/repository"

# === STEP 1: Extract version and group from build.gradle.kts ===
echo "🔍 Extracting version and group from $SRC_KTS_FILE..."

VERSION=$(sed -n 's/^[[:space:]]*version[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$SRC_KTS_FILE")
GROUP=$(sed -n 's/^[[:space:]]*group[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$SRC_KTS_FILE")

ZIP_NAME="$MODULE-$VERSION-bundle.zip"
ZIP_OUTPUT="$BUILD_DIR/$ZIP_NAME"

if [[ -z "$VERSION" || -z "$GROUP" ]]; then
  echo "❌ Failed to extract version or group from $SRC_KTS_FILE"
  exit 1
fi

GROUP_PATH=$(echo "$GROUP" | sed 's/\./\//g')

echo "📦 Version: $VERSION"
echo "🏷️  Group: $GROUP"
echo "📁 Group path: $GROUP_PATH"

# === STEP 2: Build and publish to mavenLocal ===
echo "🛠️ Running Gradle build..."
./gradlew ":$MODULE:publishToMavenLocal"

# === STEP 3: Copy artifacts from ~/.m2/repository ===
SOURCE_PATH="$M2_BASE/$GROUP_PATH"
echo "📁 Copying artifacts from: $SOURCE_PATH"

rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

find "$SOURCE_PATH" -mindepth 1 -maxdepth 1 -type d | while read -r artifact_dir; do
    if [ -d "$artifact_dir/$VERSION" ]; then
        ARTIFACT_NAME=$(basename "$artifact_dir")
        mkdir -p "$DIST_DIR/$GROUP_PATH/$ARTIFACT_NAME"
        cp -r "$artifact_dir/$VERSION" "$DIST_DIR/$GROUP_PATH/$ARTIFACT_NAME/"
    fi
done

# === STEP 4: Clean up unwanted files ===
echo "🧹 Cleaning up *.md5, *.sha1, maven-metadata-local.xml..."
find "$DIST_DIR" -type f \( -name "*.md5" -o -name "*.sha1" -o -name "maven-metadata-local.xml" \) -delete

# === STEP 5: Generate .md5 and .sha1 ===
echo "🔐 Generating .md5 and .sha1 checksums..."
find "$DIST_DIR" -type f \
  ! -name "*.asc" \
  ! -name "*.md5" \
  ! -name "*.sha1" | while read -r file; do
    sha1sum "$file" | awk '{print $1}' > "$file.sha1"
    md5sum  "$file" | awk '{print $1}' > "$file.md5"
done

# === STEP 6: Create ZIP archive ===
echo "📦 Packaging ZIP: $DIST_DIR to $ZIP_OUTPUT"
ZIP_DIR=$(cd "$(dirname "$ZIP_OUTPUT")" && pwd)
ZIP_FILE=$(basename "$ZIP_OUTPUT")
ZIP_OUTPUT="$ZIP_DIR/$ZIP_FILE"

echo "📂 Dir: $ZIP_DIR"
echo "📄 File: $ZIP_FILE"
echo "🧩 Combined: $ZIP_OUTPUT"

cd "$DIST_DIR"
zip -r "$ZIP_OUTPUT" .
cd - > /dev/null

echo "✅ Done! Bundle created at: $ZIP_OUTPUT"
