# Claude Development Guide - UrbanWay Android

## 🚀 Build Commands Optimization

### Build Strategy for Development

**DO NOT use `./gradlew build` during development** - it's slow and generates unnecessary APKs.

### Recommended Build Commands

| Command | Time | Use Case | What it does |
|---------|------|----------|-------------|
| `./gradlew compileDebugKotlin` | ~6s | Quick syntax check | Compiles Kotlin only |
| `./gradlew check` | ~20s | **MAIN DEVELOPMENT** | Compile + Tests + Lint (no APK) |
| `./gradlew build` | ~64s | Before push/release only | Full build with APKs |

### Development Workflow

1. **Daily coding**: Use `./gradlew compileDebugKotlin` for rapid feedback
2. **Feature testing**: Use `./gradlew check` for complete validation
3. **Pre-push only**: Use `./gradlew build` when APKs are needed

### Android Environment Setup

```bash
export ANDROID_HOME=/home/codespace/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:/usr/bin
```

### Project Status

- ✅ **Reset completed** - App has been completely reset to clean state
- ✅ **Framework**: Android + Jetpack Compose
- ✅ **Current state**: Minimal MainActivity with "UrbanWay - Ready for a fresh start"
- ✅ **Build system**: Optimized for fast development builds
- ✅ **Git**: Clean history with reset commit

### Key Files Structure

```
app/src/main/java/com/av/urbanway/
├── MainActivity.kt (clean, minimal)
└── ui/theme/
    ├── Color.kt (blue/green palette)
    ├── Theme.kt (standard Compose theme)
    └── Type.kt (typography)
```

### Notes for Claude

- Always use `check` command for development builds
- Only use `build` when explicitly requested or before git push
- App is completely reset - no old UI components remain
- All data models, services, and complex UI have been removed
- Ready for fresh development from clean foundation