# GitHub Actions Workflows

This directory contains the CI/CD workflows for UrbanWay Android app.

## Workflows

### 1. Android CI (`android-ci.yml`)
**Triggers:** Push to `main`/`develop` branches, Pull Requests to `main`

**What it does:**
- ✅ Sets up JDK 17 and Android SDK
- ✅ Caches Gradle dependencies for faster builds
- ✅ Runs lint checks to catch code quality issues
- ✅ Executes unit tests
- ✅ Builds debug APK
- ✅ Uploads APK and lint results as artifacts

**Artifacts produced:**
- `app-debug.apk` - Debug build for testing
- `lint-results.html` - Code quality report

### 2. Release Build (`release.yml`)
**Triggers:** Push tags matching `v*.*.*` (e.g., `v1.0.0`)

**What it does:**
- ✅ Builds release APK
- ✅ Signs APK (requires signing secrets)
- ✅ Creates GitHub release
- ✅ Uploads signed APK to release

**Required Secrets:**
```
SIGNING_KEY          - Base64 encoded keystore file
ALIAS               - Keystore alias
KEY_STORE_PASSWORD  - Keystore password
KEY_PASSWORD        - Key password
```

## Setting Up Signing (Optional)

To enable release signing:

1. Generate a keystore:
   ```bash
   keytool -genkey -v -keystore urbanway-release-key.keystore -alias urbanway -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Convert keystore to base64:
   ```bash
   base64 urbanway-release-key.keystore > keystore-base64.txt
   ```

3. Add secrets to GitHub repo:
   - Go to Settings → Secrets and variables → Actions
   - Add the required secrets listed above

## Usage

**For Development:**
- Every commit to `main`/`develop` triggers automatic build
- Check Actions tab for build status
- Download debug APK from artifacts

**For Releases:**
- Create and push a version tag:
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```
- Release workflow creates a GitHub release with signed APK

## Build Status

Check the build status badges in the main README or visit the [Actions tab](../../actions).