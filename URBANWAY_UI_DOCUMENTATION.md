# UrbanWay Android - UI Architecture & Design Documentation

## Project Overview
Transit app for Android with iOS-inspired design patterns. Focus on clean, polished UI with smooth animations and intuitive UX.

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

**Lines 270-302**: Contextual toolbar integration
- Shows horizontal toolbar when bottom sheet expanded
- 5 buttons: Notifications, Walking, Close, History, Menu
- Positioned at -100dp offset, zIndex(4f) - behind FAB

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

### 3. FloatingBottomToolbar.kt (`/presentation/components/FloatingBottomToolbar.kt`)
**Contextual horizontal toolbar:**
- **Surface**: White translucent (92% opacity), 64dp height, 32dp border radius
- **Layout**: 5 evenly spaced IconButtons in Row
- **Animations**: Scale + alpha fade with staggered delays (0, 50, 100, 150, 200ms)
- **Icons**: 24dp standard icons, center button (Close) highlighted in brand color

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
│   └── MainScreen.kt          # Main UI orchestration, FAB, toolbar integration
├── components/
│   ├── ArrivalsCards.kt       # Route cards with 2x2 grid layout
│   ├── FloatingBottomToolbar.kt # Contextual horizontal toolbar
│   ├── HomePage.kt            # Scrollable main content
│   └── DraggableBottomSheet.kt # Bottom sheet component
└── viewmodels/
    └── MainViewModel.kt       # State management
```

## Key State Management
- `showBottomSheet`: Controls FAB and toolbar visibility
- `isBottomSheetExpanded`: Toggles FAB icon (chevron ↔ X) and toolbar appearance
- `waitingTimes`: Arrival data with real-time flags
- `pinnedArrivals`: User-pinned routes for "ARRIVI IN EVIDENZA" section

## UX Flow
1. **Initial state**: Pulsing chevron FAB invites interaction
2. **Tap FAB**: Bottom sheet expands, FAB becomes X, horizontal toolbar appears
3. **Contextual actions**: 5-button toolbar provides context-sensitive functions
4. **Tap X**: Everything collapses smoothly back to initial state

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

---
*Last updated: Based on UI refinement session focusing on iOS-style design patterns and contextual interactions.*