# ManoDost AI - Mental Health Screening App

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [File Structure](#file-structure)
5. [Features](#features)
6. [Setup & Installation](#setup--installation)
7. [Running the Application](#running-the-application)
8. [API Documentation](#api-documentation)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Project Overview

**ManoDost AI** is a bilingual (English/Hindi) mental health screening application that uses AI-powered conversational agents to assess users' mental well-being through PHQ-9 and GAD-7 screening protocols. The app features:

- **Text Chat Agent**: Conversational AI for mental health screening
- **Voice Agent**: Real-time voice interaction with TTS and STT
- **Facial Emotion Detection**: ML Kit-based emotion recognition during conversations
- **Wellness Games**: 5 browser-based games for stress relief
- **The Circle**: Community support feature
- **Bilingual Support**: Seamless English/Hindi switching

---

## 🏗️ System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Android App (Kotlin)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Chat Screen  │  │ Voice Screen │  │ Games Screen │      │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘      │
│         │                  │                                 │
│  ┌──────▼──────────────────▼─────────────────────┐          │
│  │         Emotion Detection (ML Kit)             │          │
│  │    (Analyzes facial expressions in real-time)  │          │
│  └────────────────────────┬───────────────────────┘          │
└───────────────────────────┼──────────────────────────────────┘
                            │
                    ┌───────▼────────┐
                    │   HTTP/REST    │
                    └───────┬────────┘
                            │
┌───────────────────────────▼──────────────────────────────────┐
│                  Python Backend (Flask)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Chat API    │  │  Voice API   │  │  Session Mgmt│       │
│  │(app_api_no_  │  │(voice_api.py)│  │              │       │
│  │  auth.py)    │  │              │  │              │       │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘       │
│         │                  │                                  │
│  ┌──────▼──────────────────▼─────────────────────┐           │
│  │         AI Agent (LangChain + Groq)            │           │
│  │  - PHQ-9/GAD-7 Screening Logic                 │           │
│  │  - Bilingual Response Generation               │           │
│  │  - Emotion Context Integration                 │           │
│  └────────────────────────┬───────────────────────┘           │
└───────────────────────────┼───────────────────────────────────┘
                            │
                    ┌───────▼────────┐
                    │   Supabase     │
                    │   (Optional)   │
                    │  - Sessions    │
                    │  - Messages    │
                    │  - Scores      │
                    └────────────────┘
```

### Data Flow

1. **User Interaction** → Android App captures text/voice + facial emotion
2. **Emotion Detection** → ML Kit analyzes face and detects emotion (Happy/Sad/Anxious/Neutral)
3. **API Request** → App sends message + emotion data to backend
4. **AI Processing** → LangChain agent processes input with emotion context
5. **Screening Logic** → Calculates PHQ-9/GAD-7 scores, determines risk level
6. **Response Generation** → AI generates bilingual response
7. **Backend Logging** → Terminal shows scores and emotion data
8. **Response Delivery** → App displays text or plays TTS audio

---

## 💻 Technology Stack

### Frontend (Android)
- **Language**: Kotlin
- **Framework**: Jetpack Compose (Multiplatform)
- **Architecture**: MVVM (ViewModel + Repository)
- **Key Libraries**:
  - Retrofit 2.9.0 (HTTP client)
  - OkHttp 4.11.0 (Networking)
  - CameraX 1.3.1 (Camera access)
  - ML Kit Face Detection 16.1.6 (Emotion recognition)
  - Kotlin Coroutines 1.7.3 (Async operations)

### Backend (Python)
- **Framework**: Flask
- **AI/ML**:
  - LangChain (Agent framework)
  - Groq API (LLM - llama-3.3-70b-versatile for chat, llama-3.1-8b-instant for voice)
  - Google TTS (Text-to-Speech)
- **Database**: Supabase (PostgreSQL) - Optional
- **Key Libraries**:
  - Flask-CORS (Cross-origin support)
  - python-dotenv (Environment variables)
  - SpeechRecognition (Voice input)
  - gTTS (Text-to-speech)

---

## 📁 File Structure

```
ChatBot/
├── ManoDost-AI-main/                    # Android App
│   ├── composeApp/
│   │   ├── src/androidMain/kotlin/com/vishal/manodost/
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── ChatScreen.kt          # Text chat UI
│   │   │   │   │   ├── CallScreen.kt          # Voice chat UI
│   │   │   │   │   ├── HomeScreen.kt          # Main screen
│   │   │   │   │   ├── GamesScreen.kt         # Games list
│   │   │   │   │   ├── GameWebViewScreen.kt   # Game player
│   │   │   │   │   └── TheCircleScreen.kt     # Community
│   │   │   │   └── AppSettings.kt             # Language settings
│   │   │   ├── viewmodel/
│   │   │   │   ├── ChatViewModelSimple.kt     # Chat logic
│   │   │   │   └── VoiceViewModel.kt          # Voice logic
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── ApiServiceSimple.kt    # API interface
│   │   │   │   │   └── RetrofitClient.kt      # HTTP client
│   │   │   │   ├── repository/
│   │   │   │   │   ├── ChatRepositorySimple.kt
│   │   │   │   │   └── VoiceRepository.kt
│   │   │   │   └── model/
│   │   │   │       ├── Message.kt
│   │   │   │       └── Game.kt
│   │   │   ├── ml/                            # Emotion Detection
│   │   │   │   ├── FaceEmotionDetector.kt     # ML Kit integration
│   │   │   │   └── EmotionAnalyzer.kt         # Camera + Analysis
│   │   │   ├── utils/
│   │   │   │   └── CameraPermissionHandler.kt
│   │   │   └── navigation/
│   │   │       └── NavGraph.kt                # App navigation
│   │   ├── build.gradle.kts                   # Dependencies
│   │   └── src/androidMain/AndroidManifest.xml
│   └── gradle/
│
├── Backend Files (Python)
│   ├── app_api_no_auth.py                     # Main chat API
│   ├── voice_api.py                           # Voice agent API
│   ├── agent.py                               # AI agent logic
│   ├── voice_agent.py                         # Voice agent prompts
│   ├── supabase_backend.py                    # Database integration
│   └── start_both_servers.bat                 # Startup script
│
├── Configuration
│   ├── .env                                   # Environment variables
│   └── .env.example                           # Template
│
└── PROJECT_DOCUMENTATION.md                   # This file
```

---

## ✨ Features

### 1. Text Chat Agent
- Conversational AI that screens for depression (PHQ-9) and anxiety (GAD-7)
- Bilingual support (English/Hindi)
- Real-time emotion detection via camera
- Hidden scoring system (visible only in backend logs)
- Emergency contact detection for high-risk users

### 2. Voice Agent
- Push-to-talk voice interaction
- Real-time speech-to-text
- AI-generated voice responses (TTS)
- Continuous conversation flow
- Emotion-aware responses

### 3. Facial Emotion Detection
- Uses Google ML Kit Face Detection
- Detects: Happy, Sad, Anxious, Surprised, Neutral
- Analyzes smile probability and eye openness
- Sends emotion data with each message
- Real-time visual indicator in UI

### 4. Mental Wellness Games
- 2048 Puzzle (Improves Focus)
- Sudoku (Reduces Anxiety)
- Solitaire (Stress Relief)
- Minesweeper (Improves Logic)
- Tetris (Mood Boost)
- All games load via WebView (no downloads)

### 5. The Circle
- Community support feature
- Share experiences anonymously
- Swipe to delete posts
- Bilingual interface

### 6. Backend Logging
```
[CHAT] PHQ-9: 5/27 | GAD-7: 3/21 | Total: 8/48 | Risk: Low | Emotion: Happy
[VOICE] PHQ-9: 7/27 | GAD-7: 6/21 | Total: 13/48 | Risk: Mid | Emotion: Anxious
[ML-KIT] ✅ Face detected! Emotion: Happy (confidence: 0.85)
```

---

## 🚀 Setup & Installation

### Prerequisites

1. **For Backend:**
   - Python 3.8+
   - pip (Python package manager)

2. **For Android App:**
   - Android Studio (latest version)
   - JDK 17
   - Android SDK (API 24+)
   - Physical Android device (recommended) or emulator

3. **API Keys Required:**
   - Groq API Key (free at https://console.groq.com)
   - Supabase credentials (optional, for database)

### Step 1: Clone Repository

```bash
git clone <repository-url>
cd ChatBot
```

### Step 2: Backend Setup

1. **Install Python dependencies:**
```bash
pip install flask flask-cors langchain langchain-groq python-dotenv supabase gtts SpeechRecognition pydub
```

2. **Configure environment variables:**

Create `.env` file in root directory:
```env
# Groq API
GROQ_API_KEY=your_groq_api_key_here

# Supabase (Optional)
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_anon_key

# Server Config
FLASK_ENV=development
```

3. **Get Groq API Key:**
   - Visit https://console.groq.com
   - Sign up for free account
   - Go to API Keys section
   - Create new API key
   - Copy and paste into `.env`

### Step 3: Android App Setup

1. **Open project in Android Studio:**
```bash
cd ManoDost-AI-main
# Open this folder in Android Studio
```

2. **Sync Gradle:**
   - Android Studio will automatically detect the project
   - Click "Sync Now" when prompted
   - Wait for dependencies to download

3. **Configure backend URL:**

Edit `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "http://YOUR_IP:8000/"  // Replace with your PC's IP
```

To find your IP:
- Windows: `ipconfig` (look for IPv4 Address)
- Mac/Linux: `ifconfig` (look for inet)

4. **Connect Android device:**
   - Enable Developer Options on phone
   - Enable USB Debugging
   - Connect via USB
   - Allow USB debugging when prompted

---

## ▶️ Running the Application

### Start Backend Servers

**Option 1: Using batch script (Windows)**
```bash
start_both_servers.bat
```

**Option 2: Manual start**

Terminal 1 - Chat API:
```bash
python app_api_no_auth.py
```

Terminal 2 - Voice API:
```bash
python voice_api.py
```

You should see:
```
 * Running on http://0.0.0.0:8000
 * Running on http://0.0.0.0:5000
```

### Build & Install Android App

**Option 1: Android Studio**
1. Click "Run" button (green play icon)
2. Select your device
3. Wait for build and installation

**Option 2: Command line**
```bash
cd ManoDost-AI-main
./gradlew installDebug
```

APK location: `ManoDost-AI-main/composeApp/build/outputs/apk/debug/composeApp-debug.apk`

### First Run

1. **Open app on device**
2. **Grant permissions:**
   - Camera (for emotion detection)
   - Microphone (for voice chat)
   - Storage (for audio recording)
3. **Test features:**
   - Tap "Chat via Text" → Grant camera permission → Start chatting
   - Tap "Talk on Call" → Use push-to-talk button
   - Tap "Wellness Games" → Play games
4. **Check backend logs:**
   - Watch terminal for emotion detection and scores
   - Look for `[CHAT]`, `[VOICE]`, `[ML-KIT]` logs

---

## 📡 API Documentation

### Base URLs
- Chat API: `http://localhost:8000`
- Voice API: `http://localhost:5000`

### Endpoints

#### 1. Start Chat Session
```http
POST /api/start
Content-Type: application/json

{
  "language": "en",  // "en" or "hi"
  "device_id": "unique_device_id"
}

Response:
{
  "success": true,
  "session_id": "123",
  "user_id": "anonymous_xxx",
  "message": "Chat session started"
}
```

#### 2. Send Chat Message
```http
POST /api/chat
Content-Type: application/json

{
  "session_id": "123",
  "message": "I'm feeling anxious",
  "language": "en",
  "emotion": "Anxious",           // Optional
  "emotion_confidence": 0.85      // Optional
}

Response:
{
  "success": true,
  "response": "I understand you're feeling anxious...",
  "metadata": {
    "phq9_total": 5,
    "gad7_total": 8,
    "risk_level": "Mid",
    "primary_emotion": "Anxious"
  }
}
```

#### 3. Update Language
```http
POST /api/language
Content-Type: application/json

{
  "session_id": "123",
  "language": "hi"
}

Response:
{
  "success": true,
  "message": "Language updated to hi"
}
```

#### 4. Start Voice Session
```http
POST /voice/start
Content-Type: application/json

{
  "language": "en"
}

Response:
{
  "success": true,
  "session_id": "voice_123",
  "greeting": "Hello! How are you feeling today?",
  "audio": "base64_encoded_audio"
}
```

#### 5. Process Voice Message
```http
POST /voice/process
Content-Type: application/json

{
  "session_id": "voice_123",
  "text": "I'm feeling stressed",
  "emotion": "Anxious",           // Optional
  "emotion_confidence": 0.78      // Optional
}

Response:
{
  "success": true,
  "response": "I hear you're stressed...",
  "session_id": "voice_123",
  "metadata": {
    "phq9_total": 6,
    "gad7_total": 9,
    "risk_level": "Mid"
  }
}
```

#### 6. Synthesize Speech
```http
POST /voice/synthesize
Content-Type: application/json

{
  "text": "Take a deep breath",
  "language": "en"
}

Response:
{
  "success": true,
  "audio": "base64_encoded_audio"
}
```

---

## 🔧 Troubleshooting

### Backend Issues

**Problem: "Module not found" errors**
```bash
# Solution: Install missing packages
pip install flask flask-cors langchain langchain-groq python-dotenv
```

**Problem: "GROQ_API_KEY not found"**
```bash
# Solution: Check .env file exists and has correct key
cat .env  # Should show GROQ_API_KEY=gsk_...
```

**Problem: Backend not accessible from phone**
```bash
# Solution: Check firewall
# Windows: Allow Python through firewall
# Check IP: ipconfig
# Test: curl http://YOUR_IP:8000/health
```

### Android App Issues

**Problem: "Connection refused" or "Unable to connect"**
- Ensure backend is running (`start_both_servers.bat`)
- Check IP address in `RetrofitClient.kt` matches your PC's IP
- Ensure phone and PC are on same WiFi network
- Disable VPN if active

**Problem: Camera permission not requested**
- Uninstall app completely
- Reinstall fresh APK
- Permission dialog should appear on first chat/voice use

**Problem: Emotion detection not working**
- Check logcat: `adb logcat | grep ML-KIT`
- Ensure good lighting on face
- Face should be clearly visible to front camera
- Look for `[ML-KIT] ✅ Face detected!` logs

**Problem: Build errors**
```bash
# Solution: Clean and rebuild
cd ManoDost-AI-main
./gradlew clean
./gradlew assembleDebug
```

**Problem: "JAVA_HOME is set to an invalid directory"**
```bash
# Solution: Set correct JAVA_HOME
# Windows PowerShell:
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.15.6-hotspot"
./gradlew assembleDebug
```

### Emotion Detection Issues

**Problem: Always shows "Neutral"**
- Ensure camera permission granted
- Check face is well-lit and clearly visible
- Try different facial expressions
- Check logs: `adb logcat | grep -E "ML-KIT|EMOTION"`

**Problem: No emotion in backend logs**
- Verify emotion data is being sent (check app logs)
- Ensure backend is receiving emotion field
- Check terminal for `[CHAT] ... | Emotion: XXX` logs

### Voice Agent Issues

**Problem: Voice not working**
- Grant microphone permission
- Check backend voice API is running on port 5000
- Ensure TTS dependencies installed: `pip install gtts`

**Problem: Latency in voice responses**
- Voice uses faster model (llama-3.1-8b-instant)
- Check internet connection
- Groq API should respond in <2 seconds

---

## 📊 System Design Details

### PHQ-9 Screening (Depression)
- 9 questions covering: interest, mood, sleep, energy, appetite, self-worth, concentration, psychomotor, suicidal ideation
- Scoring: 0-3 per question (0=Not at all, 3=Nearly every day)
- Total: 0-27
- Risk Levels:
  - 0-4: Minimal
  - 5-9: Mild
  - 10-14: Moderate
  - 15-19: Moderately Severe
  - 20-27: Severe

### GAD-7 Screening (Anxiety)
- 7 questions covering: nervousness, uncontrollable worry, excessive worry, trouble relaxing, restlessness, irritability, feeling afraid
- Scoring: 0-3 per question
- Total: 0-21
- Risk Levels:
  - 0-4: Minimal
  - 5-9: Mild
  - 10-14: Moderate
  - 15-21: Severe

### Emotion Detection Algorithm
```
ML Kit Face Detection analyzes:
1. Smile Probability (0.0 - 1.0)
2. Left Eye Open Probability (0.0 - 1.0)
3. Right Eye Open Probability (0.0 - 1.0)

Classification Logic:
- Happy: smile > 0.7
- Sad: smile < 0.2 AND eyes < 0.6
- Anxious: eyes > 0.8 AND smile < 0.4
- Surprised: eyes > 0.9
- Neutral: default
```

### Security & Privacy
- No authentication required (anonymous usage)
- Scores hidden from users (backend only)
- Optional Supabase integration for data persistence
- Camera data processed locally (not sent to server)
- Only emotion labels sent to backend (not images)

---

## 🎓 Development Notes

### Adding New Features

**Add new screen:**
1. Create screen in `ui/screens/`
2. Add route in `NavGraph.kt`
3. Add navigation button in relevant screen

**Add new API endpoint:**
1. Add function in `ApiServiceSimple.kt`
2. Add repository method in `ChatRepositorySimple.kt`
3. Call from ViewModel

**Modify AI behavior:**
1. Edit system prompt in `agent.py` or `voice_agent.py`
2. Adjust scoring logic in prompt
3. Test with various inputs

### Testing

**Backend testing:**
```bash
# Test chat API
curl -X POST http://localhost:8000/api/start \
  -H "Content-Type: application/json" \
  -d '{"language":"en","device_id":"test"}'

# Test health endpoint
curl http://localhost:8000/health
```

**Android testing:**
```bash
# View logs
adb logcat | grep -E "CHAT-SCREEN|ML-KIT|EMOTION"

# Install APK
adb install composeApp-debug.apk

# Clear app data
adb shell pm clear com.vishal.manodost
```

---

## 📝 License & Credits

- **ML Kit**: Google's ML Kit for face detection
- **Groq**: Fast LLM inference
- **LangChain**: AI agent framework
- **Jetpack Compose**: Modern Android UI

---

## 🤝 Support

For issues or questions:
1. Check logs: Backend terminal + `adb logcat`
2. Verify all prerequisites installed
3. Ensure API keys are valid
4. Check network connectivity

---

**Last Updated**: 2026-04-03
**Version**: 1.0.0
**Status**: Production Ready ✅
