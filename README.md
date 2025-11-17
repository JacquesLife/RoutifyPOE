# Routeify 

**Routeify** is a modern Android transit planning application built with Jetpack Compose that helps users navigate public transportation in Cape Town, South Africa. The app integrates with Google Maps and Google Places APIs to provide real-time transit information, route planning, and smart navigation features.

##  Features

### Core Functionality
- **Interactive Map View**: Real-time transit map with clustered bus stops and train stations
- **Route Planning**: Comprehensive route planning with multiple transport options (bus, train, tram, walking)
- **Nearby Transit**: Find transit stops, bus stations, and train stations near your location
- **Real-time Travel Times**: Get accurate travel time estimates and departure information
- **Smart Suggestions**: AI-powered route recommendations based on travel patterns

### User Experience
- **User Authentication**: Secure login/registration with Google SSO support
- **Offline Caching**: Intelligent data caching for improved performance
- **Modern UI**: Beautiful Material Design 3 interface with dark/light themes
- **Responsive Design**: Optimized for various screen sizes and orientations
- **Accessibility**: Wheelchair-accessible route options

### Advanced Features
- **Place Autocomplete**: Smart search with Google Places API integration
- **Route Comparison**: Compare multiple route options with detailed metrics
- **Favorites**: Save frequently used destinations and routes
- **Notifications**: Real-time transit alerts and updates
- **Travel Analytics**: Track your transit usage and patterns

##  Architecture

Routeify follows modern Android development best practices with a clean architecture pattern:

```
app/
├── data/           # Data layer (API, database, repositories)
├── domain/         # Business logic and use cases
├── presentation/   # UI screens and view models
├── ui/            # UI components and themes
└── utils/         # Utility classes and helpers
```

### Key Technologies
- **Jetpack Compose**: Modern declarative UI toolkit
- **Room Database**: Local data persistence
- **Retrofit**: Network API communication
- **Google Maps SDK**: Interactive maps and location services
- **Google Places API**: Place search and transit data
- **DataStore**: Secure user preferences storage
- **Navigation Compose**: Type-safe navigation
- **ViewModel & StateFlow**: Reactive state management

##  Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 24+ (Android 7.0)
- Google Maps API key
- Google Places API key

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/routeify.git
   cd routeify
   ```

2. **Get Google Maps API Key**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing one
   - Enable the following APIs:
     - Maps SDK for Android
     - Places API
     - Directions API
     - Distance Matrix API
   - Create credentials (API Key)
   - Restrict the key to your app's package name and SHA-1 fingerprint

3. **Configure API Keys**
   Create a `local.properties` file in the root directory:
   ```properties
   GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   
   Or use Android Studio:
   - Open the project in Android Studio
   - Sync the project with Gradle files
   - Run the app on an emulator or device

### Required Permissions
The app requires the following permissions (automatically granted):
- `ACCESS_FINE_LOCATION` - For precise location services
- `ACCESS_COARSE_LOCATION` - For approximate location services
- `INTERNET` - For API communication
- `ACCESS_NETWORK_STATE` - For network status monitoring
- `POST_NOTIFICATIONS` - For transit alerts (Android 13+)

## Usage Guide

### Getting Started
1. **Launch the app** and create an account or sign in with Google
2. **Grant location permissions** when prompted
3. **Explore the home screen** to see recent destinations and quick access features

### Planning a Route
1. Navigate to **Google Services** → **Route Planner**
2. Enter your **starting location** (or use current location)
3. Enter your **destination**
4. Select your **preferred departure time** or choose "Leave Now"
5. Choose **transport modes** (bus, train, tram, walking)
6. Review **route options** and select the best one
7. View the route on the **interactive map**

### Finding Nearby Transit
1. Go to **Google Services** → **Nearby Transit**
2. The app will show **transit stops** near your current location
3. **Tap any stop** to see detailed information
4. Get **directions** to the stop or plan a route from there

### Using the Map
- **Zoom in/out** to see more or fewer transit stops
- **Tap clustered markers** to expand and see individual stops
- **Switch between day/night** map styles
- **Toggle transit lines** visibility
- **Follow route polylines** for turn-by-turn navigation

## Configuration

### API Configuration
The app uses several Google APIs. Ensure your API key has access to:
- **Maps SDK for Android**: For map display
- **Places API**: For place search and autocomplete
- **Directions API**: For route planning
- **Distance Matrix API**: For travel time calculations

### Customization
- **Theme Colors**: Modify colors in `app/src/main/java/com/example/routeify/ui/theme/Color.kt`
- **Default Location**: Change the default map center in `MapScreen.kt`
- **API Endpoints**: Update base URLs in API interfaces
- **Transit Types**: Modify supported transit modes in `TransitModels.kt`

##  Testing

Run the test suite:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- **Unit Tests**: ViewModels, repositories, and business logic
- **Integration Tests**: API communication and database operations
- **UI Tests**: Screen navigation and user interactions

##  Dependencies

### Core Dependencies
- **AndroidX Core KTX**: 1.10.1
- **Jetpack Compose BOM**: 2024.09.00
- **Material 3**: Latest
- **Navigation Compose**: 2.9.5
- **Room**: 2.6.1
- **DataStore**: 1.1.7

### Google Services
- **Google Play Services Maps**: 18.2.0
- **Google Maps Compose**: 4.3.3
- **Google Play Services Auth**: 21.2.0

### Networking
- **Retrofit**: 2.9.0
- **Gson**: 2.10.1

##  Development Status

### Current Version: 1.0
-  User authentication and registration
-  Google Maps integration
-  Route planning with multiple transport modes
-  Nearby transit discovery
-  Real-time travel information
-  Interactive map with clustering
-  Smart route suggestions
-  Offline data caching

### Planned Features
-  Real-time transit tracking
-  Push notifications for delays
-  Multi-language support



##  Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m "Add amazing feature"`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests for new features
- Update documentation for API changes
- Ensure all tests pass before submitting

##  Support

### Common Issues

**App crashes on startup**
- Ensure you have a valid Google Maps API key in `local.properties`
- Check that all required APIs are enabled in Google Cloud Console

**No transit data showing**
- Verify your location permissions are granted
- Check your internet connection
- Ensure your Google Places API key has proper quotas

**Routes not loading**
- Verify your Google Directions API is enabled
- Check API key restrictions and quotas

## Acknowledgments

- **Google Maps Platform** for comprehensive mapping and transit APIs
- **Jetpack Compose** team for the amazing UI toolkit
- **Android community** for excellent libraries and resources
- **Cape Town transit authorities** for providing reliable transit data

---


