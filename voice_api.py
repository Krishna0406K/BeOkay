"""
Flask API for Real-Time Voice Agent
Handles voice calls with speech-to-text and text-to-speech
"""

from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
from flask_socketio import SocketIO, emit, join_room, leave_room
import os
import uuid
import tempfile
from datetime import datetime
from dotenv import load_dotenv
import asyncio
from functools import wraps

from voice_agent import voice_agent
from supabase_backend import SupabaseBackend
from groq import Groq

load_dotenv()

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})
socketio = SocketIO(app, cors_allowed_origins="*", async_mode='threading')

# Initialize backends
supabase = SupabaseBackend()
groq_client = Groq(api_key=os.getenv("GROQ_API_KEY"))

# Active voice sessions
active_sessions = {}

def async_route(f):
    """Decorator to run async functions in Flask routes"""
    @wraps(f)
    def wrapped(*args, **kwargs):
        return asyncio.run(f(*args, **kwargs))
    return wrapped

# ==================== REST API ENDPOINTS ====================

@app.route('/voice/health', methods=['GET'])
def voice_health():
    """Health check for voice service"""
    return jsonify({
        "status": "healthy",
        "service": "ManoDost Voice Agent",
        "version": "1.0.0",
        "groq_api": "connected" if os.getenv("GROQ_API_KEY") else "missing"
    })

@app.route('/voice/start', methods=['POST'])
def start_voice_session():
    """Start a new voice call session"""
    data = request.json
    language = data.get('language', 'en')
    device_id = data.get('device_id', str(uuid.uuid4()))
    
    # Create session ID
    session_id = str(uuid.uuid4())
    user_id = f"voice_{device_id}"
    
    # Store session info
    active_sessions[session_id] = {
        "session_id": session_id,
        "user_id": user_id,
        "device_id": device_id,
        "language": language,
        "started_at": datetime.now().isoformat(),
        "status": "active"
    }
    
    # Create session in Supabase
    try:
        result = supabase.create_chat_session_no_auth(user_id, language)
        if result['success']:
            active_sessions[session_id]['db_session_id'] = result['session_id']
    except Exception as e:
        print(f"Error creating DB session: {e}")
    
    # Generate varied, friendly greetings based on language
    if language == "hi":
        greetings = [
            "Arre! Kaise ho yaar? Aaj kaisa mood hai?",
            "Hey dost! Sab badhiya? Kya chal raha hai?",
            "Namaste! Kaisa lag raha hai aaj?",
            "Arre bhai! Kya haal chaal? Batao na!",
            "Hey! Aaj ka din kaisa raha? Sab theek?",
            "Yaar! Kaise ho? Kuch share karna hai?",
            "Arre! Bahut din baad! Kya haal hai?",
            "Hey buddy! Aaj kya plan hai? Sab sahi?"
        ]
    else:
        greetings = [
            "Hey! How's your day going?",
            "Hi there! What's on your mind today?",
            "Hey buddy! How are you feeling?",
            "Hi! Good to hear from you. What's up?",
            "Hey! How's everything with you?",
            "Hi friend! What's been happening?",
            "Hey! How are you doing today?",
            "Hi! Nice to talk to you. How's life?"
        ]
    
    import random
    greeting = random.choice(greetings)
    
    return jsonify({
        "success": True,
        "session_id": session_id,
        "user_id": user_id,
        "message": "Voice session started",
        "greeting": greeting
    })

@app.route('/voice/transcribe', methods=['POST'])
def transcribe_audio():
    """Transcribe audio file to text using Groq Whisper"""
    if 'audio' not in request.files:
        return jsonify({"success": False, "error": "No audio file provided"}), 400
    
    audio_file = request.files['audio']
    session_id = request.form.get('session_id')
    
    if not session_id or session_id not in active_sessions:
        return jsonify({"success": False, "error": "Invalid session"}), 400
    
    try:
        # Get language from session
        language = active_sessions[session_id].get('language', 'en')
        whisper_lang = 'hi' if language == 'hi' else None  # Let Whisper auto-detect for English
        
        # Save audio temporarily
        with tempfile.NamedTemporaryFile(delete=False, suffix='.wav') as temp_audio:
            audio_file.save(temp_audio.name)
            temp_path = temp_audio.name
        
        # Transcribe using Groq Whisper with language hint
        transcription = voice_agent.transcribe_audio(temp_path, whisper_lang)
        
        # Clean up
        os.unlink(temp_path)
        
        return jsonify({
            "success": True,
            "transcription": transcription,
            "session_id": session_id
        })
    
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/voice/process', methods=['POST'])
@async_route
async def process_voice_message():
    """Process transcribed text and generate AI response"""
    data = request.json
    session_id = data.get('session_id')
    user_text = data.get('text')
    emotion = data.get('emotion')  # Facial emotion from camera
    emotion_confidence = data.get('emotion_confidence')
    
    if not session_id or session_id not in active_sessions:
        return jsonify({"success": False, "error": "Invalid session"}), 400
    
    if not user_text:
        return jsonify({"success": False, "error": "No text provided"}), 400
    
    try:
        session_info = active_sessions[session_id]
        language = session_info['language']
        
        # Add emotion context if available
        enhanced_text = user_text
        if emotion and emotion_confidence:
            emotion_context = f"[Facial Expression: {emotion} ({emotion_confidence:.2f})]"
            enhanced_text = f"{emotion_context} {user_text}"
            print(f"[VOICE-EMOTION] {emotion} ({emotion_confidence:.2f})")
        
        # Process with voice agent
        result = await voice_agent.process_voice_input(
            session_id=session_id,
            user_text=enhanced_text,
            language=language
        )
        
        # Save to database
        try:
            db_session_id = session_info.get('db_session_id')
            if db_session_id:
                metadata = result.get('metadata')
                supabase.add_message(
                    session_id=int(db_session_id),
                    user_message=user_text,
                    ai_response=result['response'],
                    phq9_score=metadata.get('phq9_total') if metadata else None,
                    gad7_score=metadata.get('gad7_total') if metadata else None,
                    risk_level=metadata.get('risk_level') if metadata else None,
                    primary_emotion=emotion if emotion else (metadata.get('primary_emotion') if metadata else None)
                )
                
                # Check for emergency contact
                if metadata and metadata.get('emergency_contact') and metadata['emergency_contact'] != 'null':
                    supabase.add_emergency_contact(int(db_session_id), metadata['emergency_contact'])
        except Exception as e:
            print(f"Error saving to DB: {e}")
        
        return jsonify({
            "success": True,
            "response": result['response'],
            "session_id": session_id,
            "metadata": result.get('metadata')
        })
    
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/voice/synthesize', methods=['POST'])
def synthesize_speech():
    """Convert text to speech using Groq TTS or alternative"""
    data = request.json
    text = data.get('text')
    language = data.get('language', 'en')
    
    if not text:
        return jsonify({"success": False, "error": "No text provided"}), 400
    
    try:
        # For now, return the text - client will use Android TTS
        # In future, can use Groq TTS or other service
        return jsonify({
            "success": True,
            "text": text,
            "language": language,
            "message": "Use client-side TTS for now"
        })
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/voice/end', methods=['POST'])
def end_voice_session():
    """End a voice call session"""
    data = request.json
    session_id = data.get('session_id')
    
    if not session_id or session_id not in active_sessions:
        return jsonify({"success": False, "error": "Invalid session"}), 400
    
    try:
        # Get session summary
        summary = voice_agent.get_session_summary(session_id)
        
        # Update session status
        active_sessions[session_id]['status'] = 'ended'
        active_sessions[session_id]['ended_at'] = datetime.now().isoformat()
        
        # Save summary to database
        try:
            db_session_id = active_sessions[session_id].get('db_session_id')
            if db_session_id and summary.get('phq9_score') is not None:
                # Update session with final scores
                supabase.supabase.table('chat_sessions').update({
                    'phq9_score': summary['phq9_score'],
                    'gad7_score': summary['gad7_score'],
                    'risk_level': summary['risk_level']
                }).eq('id', db_session_id).execute()
        except Exception as e:
            print(f"Error updating session summary: {e}")
        
        return jsonify({
            "success": True,
            "message": "Voice session ended",
            "summary": summary,
            "farewell": "Take care! I'm here whenever you need to talk." if active_sessions[session_id]['language'] == 'en' else "ध्यान रखना! जब भी बात करनी हो, मैं यहाँ हूँ।"
        })
    
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/voice/sessions', methods=['GET'])
def get_active_sessions():
    """Get all active voice sessions"""
    return jsonify({
        "success": True,
        "active_sessions": len([s for s in active_sessions.values() if s['status'] == 'active']),
        "total_sessions": len(active_sessions)
    })

# ==================== WEBSOCKET EVENTS ====================

@socketio.on('connect')
def handle_connect():
    """Handle client connection"""
    print(f"Client connected: {request.sid}")
    emit('connected', {'message': 'Connected to voice server'})

@socketio.on('disconnect')
def handle_disconnect():
    """Handle client disconnection"""
    print(f"Client disconnected: {request.sid}")

@socketio.on('join_voice')
def handle_join_voice(data):
    """Join a voice session room"""
    session_id = data.get('session_id')
    if session_id:
        join_room(session_id)
        emit('joined_voice', {'session_id': session_id}, room=session_id)

@socketio.on('leave_voice')
def handle_leave_voice(data):
    """Leave a voice session room"""
    session_id = data.get('session_id')
    if session_id:
        leave_room(session_id)
        emit('left_voice', {'session_id': session_id}, room=session_id)

@socketio.on('voice_chunk')
def handle_voice_chunk(data):
    """Handle real-time voice chunk (for future streaming implementation)"""
    session_id = data.get('session_id')
    # This can be used for real-time streaming in the future
    emit('voice_chunk_received', {'session_id': session_id}, room=session_id)

# ==================== HOME ROUTE ====================

@app.route('/', methods=['GET'])
def home():
    """Home endpoint"""
    return jsonify({
        "service": "ManoDost Voice Agent API",
        "version": "1.0.0",
        "endpoints": {
            "start_session": "POST /voice/start",
            "transcribe_audio": "POST /voice/transcribe",
            "process_message": "POST /voice/process",
            "end_session": "POST /voice/end",
            "health_check": "GET /voice/health"
        },
        "websocket": {
            "events": ["connect", "disconnect", "join_voice", "leave_voice", "voice_chunk"]
        }
    })

if __name__ == '__main__':
    print("=" * 60)
    print("🎙️  ManoDost Voice Agent API Starting...")
    print("=" * 60)
    print(f"Voice API: http://0.0.0.0:8001")
    print(f"WebSocket: ws://0.0.0.0:8001")
    print("=" * 60)
    
    socketio.run(app, host='0.0.0.0', port=8001, debug=True, allow_unsafe_werkzeug=True)
