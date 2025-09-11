# UrbanWay Android - UI Architecture & Design Documentation

## Project Overview
Transit app for Android with **complete iOS feature parity** achieved through systematic component porting. Focus on clean, polished UI with smooth animations and intuitive UX matching the iOS version exactly.

## Key Design Principles
- **iOS-style aesthetics**: Clean, minimal, translucent surfaces
- **Contextual interactions**: UI elements appear based on user context
- **Smooth animations**: Spring-based, natural feeling transitions
- **Transport type differentiation**: Visual icons for bus/tram/metro
- **Consistent spacing**: 16dp standard, 12-14dp for tighter layouts

## Main Components Architecture

### 1. MainScreen.kt (`/presentation/screens/MainScreen.kt`)
**Lines 186-267**: iOS-style FAB implementation
- **Collapsed state**: Chevron up (↑) with subtle pulsing animation
- **Expanded state**: Close icon (X) with rotation animation
- **Styling**: White translucent (95% opacity), subtle border, 56dp size
- **Positioning**: Bottom center, -106dp offset, zIndex(5f)

**Lines 295-321**: iOS-Style toolbar integration
- **IOSFloatingToolbar**: Clean 3-button design (Map, Favorites, Journey)
- **Material translucency**: White 90% opacity with rounded corners (25dp)
- **Navigation integration**: Map → FullscreenMap, Journey → JourneyPlanner
- **Positioning**: -20dp offset, zIndex(3f) - below FAB but above map

### 2. ArrivalsCards.kt (`/presentation/components/ArrivalsCards.kt`)
**2x2 Grid Layout for each route:**
```
[Route Badge]  [Times + Distance]
               [Destination Text]
```

**Route Badge Design (Lines 265-301):**
- Size: 60x60dp rounded rectangle (12dp corner radius)
- Transport icon (20dp) + Route number (20sp font, W300 weight)
- Background: Navy blue (0xFF0B3D91) at 55% opacity
- Auto-sizing font: 20sp base, adapts to route length

**Transport Icons:**
- **Tram**: Routes 3,4,9,10,13,15,16CS,16CD → `Icons.Default.Tram`
- **Metro**: M1S, M1, METRO* → `Icons.Default.Subway`  
- **Bus**: All others → `Icons.Filled.DirectionsBus`

**Time Display:**
- Real-time: Green color (0xFF34C759) + Sync icon (🔄)
- Scheduled: Gray + Clock icon (🕐)
- Font: 18sp, positioned after text with 2dp spacing

### 3. iOS-Ported Components (Complete Feature Parity)

#### 3.1 DestinationsCard.kt (`/presentation/components/DestinationsCard.kt`)
**Popular destinations with categories:**
- **Grid Layout**: 4-column LazyVerticalGrid with destination type chips
- **Category Icons**: Context-aware icons (culture, hospitals, universities, etc.)
- **Data Mapping**: Handles both `destinationsByType` and `allDestinations` formats
- **Sorting**: By importance (stations, hospitals, universities, culture, etc.)

#### 3.2 FullscreenMapCardView.kt (`/presentation/components/FullscreenMapCardView.kt`)
**Fullscreen map with iOS-style red dot overlay:**
- **Google Maps**: Full-screen interactive map with nearby stops
- **Red Dot Overlay**: Centered circle with white border (16dp)
- **Distance Threshold**: 60m movement triggers nearby data refresh
- **Animation**: Camera positioning to user location on load

#### 3.3 InlineJourneyResultsCard.kt (`/presentation/components/InlineJourneyResultsCard.kt`)
**Comprehensive journey results display:**
- **Route Filtering**: Smart filtering to avoid duplicate route combinations
- **Sectioned Results**: Direct journeys vs. transfer journeys
- **Route Badges**: Circular badges with route colors (Metro=orange, Urban=blue)
- **Journey Details**: Timing, stops, walking distances, headsigns

#### 3.4 JourneyCardView.kt (`/presentation/components/JourneyCardView.kt`)
**Detailed expandable journey cards:**
- **Expandable Design**: Animated expand/collapse with journey steps
- **Timeline UI**: Step-by-step visualization with icons and connecting lines
- **Journey Info**: Duration, walking distance, total stops with circular icons
- **Detail Panel**: Expandable section with comprehensive journey statistics

#### 3.5 JourneyResultsView.kt (`/presentation/components/JourneyResultsView.kt`)
**Journey grouping and optimization:**
- **Smart Grouping**: Groups journeys by route combinations, selects optimal times
- **Section Headers**: Clear separation between direct and transfer journeys
- **Recommendation System**: Highlights best journey options
- **Loading States**: Proper loading and empty state handling

### 4. FloatingBottomToolbar.kt (`/presentation/components/FloatingBottomToolbar.kt`)
**iOS-matching toolbar design:**
- **IOSFloatingToolbar**: 3-button layout (Map, Favorites, Journey)
- **Surface**: White translucent (90% opacity), 80dp height, 25dp border radius
- **Button Design**: Vertical icon + text layout, 20dp icons, 11sp text
- **Animations**: Scale + alpha fade with spring animations

## Color Palette
- **Brand Orange**: `Color(0xFFD9731F)` - FAB, accents, highlights
- **Navy Blue**: `Color(0xFF0B3D91)` - Route badges, borders
- **Real-time Green**: `Color(0xFF34C759)` - Live arrival times
- **Background Orange**: Same as brand - Main screen background
- **Text Gray**: `Color(0xFF555555)` - Secondary text
- **White Translucent**: `Color.White.copy(alpha = 0.95f)` - Surfaces

## Animation Patterns

### FAB Animations
```kotlin
// Icon rotation: 0° → 180° with spring damping
val iconRotation by animateFloatAsState(
    targetValue = if (expanded) 180f else 0f,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
)

// Pulsing (collapsed state only): Scale 1.0 → 1.1, Alpha 0.2 → 0
val pulseScale = remember { Animatable(1f) }
LaunchedEffect(expanded) {
    if (!expanded) {
        while (true) {
            pulseScale.animateTo(1.1f, tween(1000))
            pulseScale.snapTo(1f)
        }
    }
}
```

### Contextual Toolbar
```kotlin
// Staggered button appearance with spring animation
val buttonScale by animateFloatAsState(
    targetValue = if (showButtons) 1f else 0f,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
)
```

## File Structure
```
/presentation/
├── screens/
│   └── MainScreen.kt                    # Main UI orchestration, iOS toolbar integration
├── components/
│   ├── ArrivalsCards.kt                 # Route cards with 2x2 grid layout
│   ├── FloatingBottomToolbar.kt         # iOS-style 3-button toolbar
│   ├── HomePage.kt                      # Scrollable main content
│   ├── DraggableBottomSheet.kt          # Bottom sheet component
│   ├── DestinationsCard.kt              # 🍎 iOS-ported: Popular destinations
│   ├── FullscreenMapCardView.kt         # 🍎 iOS-ported: Fullscreen map with red dot
│   ├── InlineJourneyResultsCard.kt      # 🍎 iOS-ported: Journey results display
│   ├── JourneyCardView.kt               # 🍎 iOS-ported: Expandable journey details
│   └── JourneyResultsView.kt            # 🍎 iOS-ported: Journey grouping & optimization
└── viewmodels/
    └── MainViewModel.kt                 # State management with iOS flow support
```

## Key State Management
- `showBottomSheet`: Controls FAB and toolbar visibility
- `isBottomSheetExpanded`: Toggles FAB icon (chevron ↔ X) and toolbar appearance
- `waitingTimes`: Arrival data with real-time flags
- `pinnedArrivals`: User-pinned routes for "ARRIVI IN EVIDENZA" section

## UX Flow (iOS-Matching Experience)
1. **Initial state**: Pulsing chevron FAB invites interaction
2. **Place Selection**: Tap search suggestion → map shows with pin + draggable sheet
3. **iOS Toolbar**: Clean 3-button toolbar appears (Map, Favorites, Journey)
4. **Journey Planning**: Tap "Percorso" → navigates to journey planner with pre-filled destination
5. **Navigation**: Map button → fullscreen map, all flows work seamlessly

## Typography Scale
- **Route numbers**: 20sp (badge), 18sp (arrival times)
- **Destinations**: 18sp (primary), 15sp (secondary)
- **UI labels**: 14-16sp (buttons, headers)
- **Distances**: 12-14sp (subtle info)

## Performance Notes
- Use `@Preview` composables for isolated component development
- Complex layouts may affect hot reload - break into smaller components for faster iteration
- Prefer `weight(1f)` over `fillMaxWidth()` in scrollable containers

## Design References
- iOS Maps app toolbar pattern (horizontal contextual buttons)
- iOS Control Center translucent surfaces
- Material 3 elevation and shadow guidelines
- Spring animation curves for natural motion

## iOS Parity Achievement

### Component Porting Summary
- **8 iOS Components** successfully ported to Android Kotlin/Compose
- **1:1 Feature Parity** with iOS version maintained
- **Material 3 Integration** while preserving iOS UX patterns
- **Navigation Flow** matching iOS behavior exactly

### Key Technical Achievements
- **Smart Journey Filtering**: Avoids duplicate route combinations
- **iOS-Style Animations**: Spring-based transitions throughout
- **Contextual UI**: Components appear/disappear based on user state
- **Route Color Coding**: Consistent transport type differentiation
- **Responsive Design**: Adapts to different screen sizes and orientations

---
*Last updated: After completing systematic iOS component porting and achieving complete feature parity between iOS and Android versions.*