# Google Maps API Setup Guide

## 🗝️ Getting Your Google Maps API Key

### Step 1: Create a Google Cloud Project
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Note your project ID

### Step 2: Enable Required APIs
Enable the following APIs in your project:

**Required APIs:**
- ✅ **Maps SDK for Android** - For displaying maps
- ✅ **Places API** - For place search and autocomplete  
- ✅ **Geocoding API** - For address resolution

**Optional APIs (for advanced features):**
- 🔧 **Roads API** - For route snapping and optimization
- 🔧 **Distance Matrix API** - For travel time calculations

### Step 3: Create API Key
1. Go to **APIs & Services > Credentials**
2. Click **+ CREATE CREDENTIALS > API key**
3. Copy your API key

### Step 4: Secure Your API Key (Recommended)
1. Click on your API key to edit it
2. Under **Application restrictions**, select **Android apps**
3. Add your package name: `com.av.urbanway`
4. Add your SHA-1 certificate fingerprint:
   ```bash
   # Get debug certificate fingerprint
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey
   # Password: android
   ```

### Step 5: Add API Key to Your Project
1. Open `local.properties` in your project root
2. Replace `YOUR_API_KEY_HERE` with your actual API key:
   ```properties
   GOOGLE_MAPS_API_KEY=AIzaSyBdVl-cTICSwYKrZ95SuvNw7dbMuDt1KG0
   ```

## 🔒 Security Best Practices

### API Key Restrictions
- **Always** restrict your API key to specific APIs
- **Always** restrict to your Android app package name
- **Never** commit API keys to version control

### Application Restrictions
```
Application restrictions: Android apps
Package name: com.av.urbanway
SHA-1 certificate fingerprint: [Your certificate fingerprint]
```

### API Restrictions
```
Restrict key to specific APIs:
- Maps SDK for Android
- Places API  
- Geocoding API
- Roads API (optional)
```

## 🚀 Testing Your Setup

Once you've added your API key, the app will:

1. ✅ Display Google Maps in the bottom sheet
2. ✅ Enable place search with autocomplete
3. ✅ Show transit stations and addresses
4. ✅ Convert coordinates to addresses
5. ✅ Filter results to Turin metropolitan area

## 📍 Turin-Specific Configuration

The app is pre-configured for Turin, Italy:

- **Bounding Box**: Restricts search results to Turin area
- **Default Center**: Piazza Castello (45.0703°, 7.6869°)
- **Country Filter**: Italy (IT)
- **Languages**: Italian (it), English (en)

## 🆘 Troubleshooting

### Common Issues

**"API key not configured" error:**
- Check that your API key is in `local.properties`
- Verify the key format: `GOOGLE_MAPS_API_KEY=AIza...`

**"This API project is not authorized" error:**
- Enable required APIs in Google Cloud Console
- Wait 5-10 minutes for changes to propagate

**Empty search results:**
- Check API key restrictions
- Verify Places API is enabled
- Check app package name matches restriction

**Map not loading:**
- Verify Maps SDK for Android is enabled
- Check SHA-1 certificate fingerprint
- Clear app data and restart

### Getting Help
1. Check [Google Maps Platform documentation](https://developers.google.com/maps/documentation)
2. Verify API quotas in Google Cloud Console
3. Check Android Studio Logcat for detailed error messages

## 💰 Pricing

Google Maps Platform uses pay-per-use pricing:

- **Maps SDK for Android**: Free up to 100k map loads/month
- **Places API**: $2.83-$17 per 1,000 requests  
- **Geocoding API**: $5 per 1,000 requests

**For development:** Google provides $200/month free credits, which is typically sufficient for testing.

---

**🎯 Ready to go!** Once your API key is configured, rebuild the app and start exploring Turin! 🚌🗺️