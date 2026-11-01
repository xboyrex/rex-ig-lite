#!/data/data/com.termux/files/usr/bin/bash
# ==============================================================================
# RexPrime (rex-ig-lite) — Termux One-Click GitHub Repo Push & Auto-Build Script
# Author: Mayank (@xboyrex) | xboyrex496@gmail.com
# ==============================================================================
set -e

REPO_OWNER="xboyrex"
REPO_NAME="rex-ig-lite"
GIT_EMAIL="xboyrex496@gmail.com"
GIT_NAME="xboyrex"

echo "========================================================"
echo "   RexPrime (rex-ig-lite) Termux GitHub Push & Build"
echo "   Developer: Mayank (@xboyrex)"
echo "========================================================"

if command -v pkg >/dev/null 2>&1; then
    echo "[*] Checking Termux dependencies (git, gh, unzip, openssh)..."
    pkg install -y git gh unzip openssh >/dev/null 2>&1 || true
fi

if [ ! -f "settings.gradle.kts" ] && [ -f "rex-ig-lite.zip" ]; then
    echo "[*] Extracting rex-ig-lite.zip..."
    rm -rf rex-ig-lite-extract
    mkdir -p rex-ig-lite-extract
    unzip -q -o rex-ig-lite.zip -d rex-ig-lite-extract
    cd rex-ig-lite-extract
    if [ -d "rex-ig-lite" ] && [ -f "rex-ig-lite/settings.gradle.kts" ]; then
        cd rex-ig-lite
    fi
fi

git config --global user.name "$GIT_NAME"
git config --global user.email "$GIT_EMAIL"
git config --global init.defaultBranch main

if ! gh auth status >/dev/null 2>&1; then
    echo ""
    echo "[!] Please authenticate with GitHub CLI:"
    gh auth login
fi

rm -rf .git
git init
git checkout -B main
chmod +x gradlew patch-ig.sh .github/scripts/release-prepare.sh 2>/dev/null || true
git add -A
git commit -m "fix(ci): upgrade setup-java@v5 and setup-android@v4 (platform-tools) for RexPrime .mpp build"

if gh repo view "${REPO_OWNER}/${REPO_NAME}" >/dev/null 2>&1; then
    echo "[*] Pushing updated RexPrime codebase & workflows to ${REPO_OWNER}/${REPO_NAME}..."
    git remote add origin "https://github.com/${REPO_OWNER}/${REPO_NAME}.git" 2>/dev/null ||         git remote set-url origin "https://github.com/${REPO_OWNER}/${REPO_NAME}.git"
    git push -u origin main --force
else
    echo "[*] Creating public repository ${REPO_OWNER}/${REPO_NAME} and pushing..."
    gh repo create "${REPO_OWNER}/${REPO_NAME}"         --public         --description "RexPrime (rex-ig-lite) — Unified Morphe Patches for Instagram & X with Custom Vector UI/UX by Mayank (@xboyrex)"         --source=.         --remote=origin         --push
fi

echo ""
echo "========================================================"
echo " [SUCCESS] Pushed to: https://github.com/${REPO_OWNER}/${REPO_NAME}"
echo " [*] Check live build & .mpp release:"
echo "     https://github.com/${REPO_OWNER}/${REPO_NAME}/actions"
echo "     https://github.com/${REPO_OWNER}/${REPO_NAME}/releases"
echo "========================================================"
