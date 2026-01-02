# ElderCarePlus 👴👵

A comprehensive Android application designed to enhance elderly care through smart monitoring, health tracking, and caretaker coordination features.

## 📱 Features

### For Elders
- **Fall Detection** - Automatic fall detection using device sensors with emergency alert system
- **Sound Detection** - Audio monitoring for distress signals and unusual sounds
- **Sleep Tracking** - Monitor sleep patterns and receive sleep schedule reminders
- **Medicine Reminders** - Never miss medication with scheduled notifications
- **Appointment Management** - Keep track of doctor appointments and health checkups
- **Emergency Contacts** - Quick access to emergency contacts and SOS features
- **Government Schemes** - Information about government welfare schemes for seniors
- **Brain Games** - Cognitive exercises to keep the mind active

### For Caretakers
- **Real-time Alerts** - Receive instant notifications for falls, emergencies, and health events
- **Alert History** - View comprehensive history of all alerts and incidents
- **Multiple Elder Management** - Monitor and manage multiple elderly individuals
- **Remote Monitoring** - Track elder's health metrics and activity remotely
- **Elder Control Panel** - Manage settings and preferences for linked elders

### For Administrators
- **User Management** - Approve and manage caretaker-elder connections
- **System Oversight** - Monitor platform usage and system health

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Backend**: Firebase (Authentication, Firestore, Cloud Messaging)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Jetpack Navigation Compose
- **Dependency Injection**: Manual DI with ViewModels

### Key Components

#### Services
- `FallDetectionService` - Background service for fall detection using accelerometer
- `AudioSafetyService` - Monitors audio for distress signals
- `CaretakerAlertService` - Manages real-time alerts to caretakers

#### Core Features
- Phone-based authentication with OTP
- Role-based access (Elder, Caretaker, Admin)
- Real-time location tracking
- Push notifications for alerts
- Scheduled reminders for medicine and appointments

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK (API 24+)
- Firebase account
- Google Services configuration

### Installation

1. Clone the repository:
```bash
git clone https://github.com/ayushran32/ElderCarePlus.git
cd ElderCarePlus
```

2. Open the project in Android Studio

3. Add your `google-services.json` file:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Phone), Firestore, and Cloud Messaging
   - Download `google-services.json`
   - Place it in the `app/` directory

4. Sync Gradle and build the project

5. Run on an emulator or physical device

### Firebase Setup

1. **Authentication**:
   - Enable Phone authentication in Firebase Console
   - Configure your app's SHA-1 fingerprint

2. **Firestore Database**:
   - Create collections: `users`, `alerts`, `medicines`, `appointments`, `sleepSessions`
   - Set up security rules for data protection

3. **Cloud Messaging**:
   - Enable FCM for push notifications
   - Configure notification channels

## 📂 Project Structure

```
app/src/main/java/com/eldercareplus/
├── auth/                    # Authentication ViewModels
├── caretaker/              # Caretaker-specific features
├── elder/                  # Elder-specific features
├── model/                  # Data models and repositories
├── navigation/             # Navigation graph
├── receivers/              # Broadcast receivers for alarms
├── screens/                # UI screens (Compose)
├── services/               # Background services
├── ui/theme/               # App theming
└── util/                   # Utility classes
```

## 🔐 Permissions

The app requires the following permissions:
- **Location** - For fall detection location tracking
- **Activity Recognition** - For fall detection
- **Notifications** - For alerts and reminders
- **Phone** - For emergency calling
- **Record Audio** - For sound detection
- **Foreground Service** - For background monitoring

## 🎨 UI/UX

Built with Jetpack Compose for a modern, responsive interface:
- Material Design 3 components
- Dark/Light theme support
- Smooth animations and transitions
- Accessible design for elderly users

## 🔔 Alert System

The app features a comprehensive alert system:
- **Fall Alerts** - Triggered by fall detection with 30-second cancellation window
- **Emergency Alerts** - Manual SOS button for immediate help
- **Health Alerts** - Medicine and appointment reminders
- **Sound Alerts** - Triggered by distress sound detection

All alerts are:
- Stored in Firebase with timestamps and location
- Sent to all linked caretakers via push notifications
- Logged in alert history for review

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Developer

Developed by Ayush Ranjan

## 📞 Support

For support, please open an issue in the GitHub repository.

---

**Note**: This app is designed to assist with elderly care but should not replace professional medical advice or emergency services. Always call emergency services (911, 112, etc.) in case of serious emergencies.
