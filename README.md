# UrbanWay Android

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-34%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=34)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg)](https://kotlinlang.org)

**UrbanWay** is a modern Android transit app for Turin, Italy, providing real-time public transportation information, journey planning, and interactive maps.

## ✨ Features

- 🚌 **Real-time Arrivals**: Live bus, tram, and metro departure times
- 🗺️ **Interactive Maps**: Google Maps integration with route visualization  
- 📍 **Journey Planning**: Multi-modal route optimization
- ⭐ **Favorites**: Pin frequently used routes and stops
- 🔍 **Smart Search**: Places autocomplete with transit station support
- 📱 **Modern UI**: Material 3 design with Jetpack Compose

## 🏗️ Architecture

This app follows **Clean Architecture** principles with **MVVM pattern**:

```
presentation/     # UI layer (Compose, ViewModels)
├── components/   # Reusable UI components  
├── screens/      # App screens
├── viewmodels/   # State management
└── navigation/   # Navigation logic

domain/          # Business logic layer
├── models/      # Domain entities
├── repository/  # Repository interfaces  
└── usecase/     # Business use cases

data/            # Data layer
├── api/         # Retrofit API services
├── models/      # Data transfer objects
├── repository/  # Repository implementations
└── local/       # Local data sources
```

## 🚀 Quick Start

### Prerequisites
- Android Studio Iguana or later
- Android SDK 34+
- Google Maps API key (see setup below)

### 1. Clone Repository
```bash
git clone <repository-url>
cd UrbanWayAndroid
```

### 2. Configure Google Maps API
1. Get an API key from [Google Cloud Console](https://console.cloud.google.com)
2. Enable required APIs (see [setup guide](./GOOGLE_MAPS_SETUP.md))
3. Add your key to `local.properties`:
   ```properties
   GOOGLE_MAPS_API_KEY=AIzaSyBdVl-cTICSwYKrZ95SuvNw7dbMuDt1KG0
   ```

### 3. Build and Run
```bash
./gradlew assembleDebug
```

## 🗝️ Google Maps Setup

The app requires several Google APIs to be enabled:

| API | Purpose | Required |
|-----|---------|----------|
| **Maps SDK for Android** | Display interactive maps | ✅ Yes |
| **Places API** | Search places and addresses | ✅ Yes |
| **Geocoding API** | Convert coordinates to addresses | ✅ Yes |  
| **Roads API** | Route optimization (optional) | 🔧 Optional |

**📖 Full setup guide: [GOOGLE_MAPS_SETUP.md](./GOOGLE_MAPS_SETUP.md)**

## 🛠️ Technology Stack

### Core
- **Kotlin** - Modern Android development
- **Jetpack Compose** - Declarative UI framework
- **Material 3** - Modern design system

### Architecture  
- **MVVM** - Presentation layer pattern
- **Clean Architecture** - Separation of concerns
- **Repository Pattern** - Data abstraction
- **Dependency Injection** - Hilt (configured)

### Networking & Data
- **Retrofit** - HTTP client
- **Gson** - JSON serialization  
- **DataStore** - Preferences storage
- **Coroutines + Flow** - Asynchronous programming

### Maps & Location
- **Google Maps SDK** - Map display
- **Google Places API** - Place search
- **Google Location Services** - GPS and location

### Navigation
- **Navigation Compose** - Screen navigation
- **Hilt Navigation Compose** - DI integration

## 📱 Screenshots

*Screenshots will be added once the UI is complete*

## 🌍 Turin Transit API

The app integrates with Turin's public transit API:

- **Base URL**: `https://av-gtfsfuncs.azurewebsites.net`
- **Nearby Departures**: Real-time arrival information
- **Journey Planning**: Multi-modal route calculation  
- **Route Details**: Stop lists and schedules
- **Destinations**: Popular places and categories

## 🗂️ Project Structure

```
app/src/main/java/com/av/urbanway/
├── data/
│   ├── api/              # Retrofit API interfaces
│   ├── models/           # Data transfer objects
│   ├── repository/       # Repository implementations  
│   └── local/            # Local storage & services
├── domain/
│   ├── models/           # Business entities
│   ├── repository/       # Repository contracts
│   └── usecase/          # Business logic
├── presentation/
│   ├── components/       # Reusable UI components
│   ├── screens/          # App screens  
│   ├── viewmodels/       # State management
│   ├── navigation/       # Navigation setup
│   └── theme/            # Design system
├── di/                   # Dependency injection
└── UrbanWayApplication   # Application class
```

## 🎨 Design System

The app uses **UrbanWay brand colors**:

- **Primary Orange**: `#D9731F` - Brand color, buttons, highlights
- **Orange Dark**: `#BF5D0A` - Pressed states, shadows  
- **Orange Light**: `#E6953F` - Disabled states, backgrounds

**Transport Type Colors**:
- **Bus**: `#007ACC` (Blue)
- **Metro**: `#00B050` (Green)  
- **Tram**: `#FF6B35` (Orange-Red)

## 🚧 Development Status

### ✅ Completed (Phase 1)
- [x] Project setup and dependencies
- [x] Clean architecture implementation
- [x] Data models and API integration
- [x] Location services setup
- [x] Google Maps API configuration
- [x] Basic UI structure with Compose
- [x] Navigation framework

### 🔄 In Progress (Phase 2)  
- [ ] Google Maps integration and display
- [ ] Places search implementation
- [ ] Real-time arrivals UI
- [ ] Journey planning screens
- [ ] Route visualization

### 📋 Planned (Phase 3)
- [ ] Advanced map features (route overlays)
- [ ] Push notifications for favorites
- [ ] Offline mode and caching
- [ ] Accessibility improvements  
- [ ] Performance optimization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- 📖 [Setup Guide](./GOOGLE_MAPS_SETUP.md) - Google Maps API configuration
- 🐛 [Issues](https://github.com/your-repo/issues) - Report bugs
- 💬 [Discussions](https://github.com/your-repo/discussions) - Ask questions

---

**Made with ❤️ for Turin's public transportation** 🚌🚊🚇