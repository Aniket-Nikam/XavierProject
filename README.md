# XavierProject (BolBharat) 🇮🇳

XavierProject is a comprehensive civic issue reporting platform designed to bridge the gap between citizens, government officials, and administrators. It empowers users to actively participate in maintaining their communities by reporting issues, while giving officials the tools they need to track and resolve them efficiently.

## 🚀 Features

### 👤 Role-Based Access
- **Citizens:** Can report local issues (potholes, water leaks, etc.), attach photos, upvote community issues, and track the resolution status of their reports.
- **Government Officials:** Access a dedicated dashboard to view, manage, and update the status of assigned civic issues in their jurisdiction.
- **Administrators:** Have God-mode access to manage user roles, send provisioning invitations, and oversee platform-wide activity.

### 🗺️ Interactive Mapping
- **Google Maps Integration:** Civic issues are plotted on an interactive map. Users can see where issues are concentrated in real-time, and officials can visually manage their assigned territories.

### 🤖 AI-Powered Chatbot
- **Gemini AI Assistant:** A built-in, context-aware chatbot helps users navigate the app, understand how to report issues, and learn about civic processes. It is strictly sandboxed to only answer app-related queries.

### ☁️ Modern Backend Infrastructure
- **Firebase Realtime Database:** Provides ultra-fast, real-time syncing of complaints, posts, and user profiles across all devices.
- **Cloudinary Integration:** Securely handles user photo uploads for complaints and profile pictures.
- **Firebase Authentication:** Seamless and secure email/password and Google Sign-In options.

## 🛠️ Tech Stack
- **Frontend:** Java, Android SDK, XML Layouts, Material Design
- **Backend:** Firebase (Authentication, Realtime Database)
- **APIs:** Google Maps SDK, Cloudinary SDK, Google Gemini API
- **Networking/Async:** OkHttp, Glide

## ⚙️ Setup Instructions
1. Clone the repository: `git clone https://github.com/Aniket-Nikam/XavierProject.git`
2. Create a `local.properties` file in the root directory.
3. Add your API keys to `local.properties` to ensure they are never pushed to GitHub:
   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   GEMINI_API_KEY=your_gemini_api_key
   CLOUD_NAME=your_cloudinary_name
   UPLOAD_PRESET=your_cloudinary_preset
   ```
4. Place your `google-services.json` file in the `app/` directory (ignored by git for security).
5. Build and run in Android Studio.

## 🛡️ Security Note
All database queries are protected by robust Firebase Security Rules, ensuring users can only modify their own data, and strict indexes are placed on `email` and `userId` fields to guarantee O(1) query performance.
