# UrbanWay Android - Chatbot Architecture Documentation

## Overview
This document describes the modular chatbot architecture implemented for the UrbanWay Android app - a Turin public transport application with a conversational UI.

## Core Architecture Principles

### 1. **Modular Message System**
- **Concept**: Chat as a list of messages (bot messages + user actions)
- **Bot Messages**: Generic `BotMessageContainer` wraps any component
- **User Messages**: `UserMessageView` for user selections
- **Golden Rule**: Tapping previous choices removes subsequent cards and re-triggers action

### 2. **Component Hierarchy**
```
ChatFlow
├── BotMessageContainer (generic wrapper)
│   ├── GreetingChatView
│   ├── ArrivalsChatView
│   ├── PinnedCardView
│   └── [Future Components]
├── UserMessageView (user selections)
└── Modal System (unified for all expansions)
```

## Key Components

### **BotMessageContainer**
`/app/src/main/java/com/av/urbanway/presentation/components/BotMessageContainer.kt`

**Purpose**: Generic wrapper for any bot component
- Handles all container styling (background, padding, rounded corners)
- **Fullscreen button** in top right corner (only on last message)
- Uses `OpenInFull` icon, 40dp button, 24dp icon
- Completely modular - works with any content

**Usage**:
```kotlin
BotMessageContainer(
    isLastMessage = lastExpandableMessage == "messageType",
    onExpandClick = { expandToFullscreen("messageType") }
) {
    AnyComponent(isPreview = true)
}
```

### **GreetingChatView**
`/app/src/main/java/com/av/urbanway/presentation/components/GreetingChatView.kt`

**Layout**:
- "Dove vuoi andare oggi?"
- Fake search bar: "Cerca una destinazione..." (triggers `onSearchClick`)
- "oppure:"
- Choice chips: [🚌 Arrivi] [🗺️ Mappa]

### **ArrivalsChatView**
`/app/src/main/java/com/av/urbanway/presentation/components/ArrivalsChatView.kt`

**Purpose**: Shows transit arrivals in preview/detail modes
- **Preview mode** (`isPreview = true`): Summary + choice chips
- **Detail mode** (`isPreview = false`): Full arrivals with route circles
- Clean content only (no container styling)

### **PinnedCardView**
`/app/src/main/java/com/av/urbanway/presentation/components/PinnedCardView.kt`

**Purpose**: Shows pinned/favorite routes
- Header with close button
- List of pinned routes with real-time data
- Clean content only (no container styling)

### **UserMessageView**
`/app/src/main/java/com/av/urbanway/presentation/components/UserMessageView.kt`

**Purpose**: Left-aligned user message bubbles
- Blue background, white text
- Clickable (implements golden rule)
- `onClick` parameter for re-triggering actions

## Chat Flow & State Management

### **HomePage Logic**
`/app/src/main/java/com/av/urbanway/presentation/components/HomePage.kt`

**Key State Variables**:
```kotlin
// Unified modal system
var showBottomSheet by remember { mutableStateOf(false) }
var bottomSheetContent by remember { mutableStateOf("") }
var lastUserChoice by remember { mutableStateOf("") }

// Track last expandable message for fullscreen button
var lastExpandableMessage by remember { mutableStateOf("") }

// User selections (control message visibility)
var userSelectedArrivi by remember { mutableStateOf(false) }
var userSelectedMappa by remember { mutableStateOf(false) }
var userSelectedCerca by remember { mutableStateOf(false) }
// ... more user selections
```

**Golden Rule Implementation**:
```kotlin
fun resetToLevel(level: Int) {
    when (level) {
        0 -> { /* Reset everything */ }
        1 -> { /* Reset from first level */ }
        2 -> { /* Reset from second level */ }
    }
}
```

**Fullscreen System**:
```kotlin
fun expandToFullscreen(contentType: String) {
    when (contentType) {
        "arrivals" -> {
            showBottomSheet = true
            bottomSheetContent = "arrivals_detail"
            lastUserChoice = "altreLinee"
        }
        "map" -> {
            showBottomSheet = true
            bottomSheetContent = "map_view"
            lastUserChoice = "mappa"
        }
    }
}
```

## Complete User Journey

### **1. Initial State**
```
GreetingChatView
├── "Dove vuoi andare oggi?"
├── [🔍 Cerca una destinazione...]
├── "oppure:"
└── [🚌 Arrivi] [🗺️ Mappa]
```

### **2. User selects "🚌 Arrivi"**
```
UserMessageView: "🚌 Arrivi" (clickable - golden rule)
↓
BotMessageContainer (🔍 EXPANDABLE - lastExpandableMessage = "arrivals")
└── ArrivalsChatView (isPreview=true)
    ├── Summary: "Hai X linee preferite: 56, 18..."
    └── Choice chips: [📋 Dettagli] [📅 Orari] [🗺️ Mappa]
```

### **3A. User selects "📋 Dettagli"**
```
UserMessageView: "🚌 Altre linee" (clickable - golden rule)
↓
Modal opens with:
└── ArrivalsChatView (isPreview=false)
    ├── All pinned routes with real arrival times
    ├── Route circles for unpinned routes
    └── Choice chips: [🚌 Altre linee] [📅 Orari] [🗺️ Mappa]
```

### **3B. User selects "📅 Orari"**
```
UserMessageView: "📅 Orari" (clickable - golden rule)
↓
BotMessageContainer (🔍 EXPANDABLE - lastExpandableMessage = "pinned")
└── PinnedCardView
    ├── Header: "NEW ARRIVI IN EVIDENZA" [X]
    └── List of pinned routes (clickable for route details)
```

### **3C. User selects "🗺️ Mappa"**
```
UserMessageView: "🗺️ Mappa" (clickable - golden rule)
↓
Modal opens with:
└── UrbanWayMapView (fullscreen)
    ├── User location marker
    ├── Nearby stops markers
    └── UIState.NORMAL
```

## Modal System

### **Unified Modal Dialog**
- Replaces draggable BottomSheet (fixed gesture conflicts with map)
- 95% screen width, 85% screen height
- Semi-transparent overlay (tap outside to close)
- Dynamic header based on content type
- Close button always visible

**Content Types**:
- `"arrivals_detail"` → ArrivalsChatView (isPreview=false)
- `"map_view"` → UrbanWayMapView (fullscreen)

## Technical Implementation Details

### **Key Features Implemented**:
1. ✅ **Golden Rule**: Tap previous choice → reset subsequent content + re-trigger
2. ✅ **Modular Containers**: BotMessageContainer wraps any component
3. ✅ **Unified Modal**: Fixed dialog system (no gesture conflicts)
4. ✅ **Prominent Fullscreen**: OpenInFull icon, top-right placement
5. ✅ **Search-First UX**: Fake search bar in greeting
6. ✅ **State Management**: Clean separation of message visibility
7. ✅ **isPreview Logic**: Components adapt for chat vs modal display

### **Data Flow**:
- **MainViewModel**: Handles API calls, pinned arrivals, location
- **Waiting Times**: Real-time transit data with route suffix handling ("56U" → "56")
- **Nearby Stops**: Stop locations and distances
- **Route Selection**: Integrated with existing route detail system

### **Styling Consistency**:
- **Bot messages**: Right-aligned, 40dp left padding, rounded corners
- **User messages**: Left-aligned, 40dp right padding, blue background
- **Choice chips**: Consistent styling across all components
- **Typography**: Material Design 3 typography scale

## Future Extensibility

### **Adding New Bot Components**:
1. Create component with `isPreview` parameter
2. Remove container styling (pure content)
3. Wrap in `BotMessageContainer` in HomePage
4. Add to modal system if expandable
5. Update `lastExpandableMessage` tracking

### **Adding New Modal Content**:
1. Add content type to `bottomSheetContent`
2. Add case to modal's `when` statement
3. Update `expandToFullscreen` function
4. Ensure proper `isPreview = false` usage

## Current Status (January 2025)

- ✅ **Core architecture** implemented and tested
- ✅ **Golden rule** working across all levels
- ✅ **Unified modal** system operational
- ✅ **Search-first greeting** implemented
- ✅ **Prominent fullscreen** button added
- 🔄 **Search component** - ready for future implementation
- 🔄 **Additional modal content** - architecture supports easy additions

## Next Steps Recommendations

1. **Test user interaction** with fullscreen button visibility
2. **Implement search component** (triggered by fake search bar)
3. **Add visual hints** for expandable content if needed
4. **Consider animation** for fullscreen button (subtle pulse/glow)
5. **Add more modal content types** as needed

This architecture provides a solid, modular foundation for the conversational transport app while maintaining clean separation of concerns and excellent user experience.