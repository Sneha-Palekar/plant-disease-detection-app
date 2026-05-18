# 🌿 Plant Disease Detection App

Android application that identifies plant diseases from leaf images using TensorFlow Lite and Firebase authentication.

## 📱 Features

| Feature | Description |
|---------|-------------|
| 🔐 User Authentication | Login and registration using Firebase Email/Password |
| 📸 Camera Detection | Capture leaf images using device camera |
| 🖼️ Gallery Upload | Select images from device storage |
| 🤖 AI Detection | TensorFlow Lite model for disease identification |
| 👤 User Profile | Personal dashboard with history |

## 🛠️ Tech Stack
Android (Java) + Firebase Auth + TensorFlow Lite + CameraX

| Technology | Purpose |
|------------|---------|
| Android Studio | Development Environment |
| Java/XML | Frontend development |
| Firebase Authentication | User login/signup |
| TensorFlow Lite | On-device ML inference |
| CameraX | Camera integration |
| Kaggle | Model training |
| Roboflow | Dataset preparation |

## 🧠 Model Training

- **Platform**: Kaggle Notebook
- **Dataset**: Plant disease images from Roboflow
- **Architecture**: MobileNetV2
- **Output**: TensorFlow Lite (.tflite)

## 🚀 How to Run

### Prerequisites
- Android Studio (Latest version)
- Android device (API 21+)
- USB cable

### Steps
```bash
# Clone the repository
git clone https://github.com/Sneha-Palekar/plant-disease-detection-app.git

# Open in Android Studio
# Add google-services.json from Firebase Console
# Connect your Android device
# Click Run ▶️
