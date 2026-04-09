"""
Flask API for ManoDost AI - No Authentication Required
Simplified version that works without login
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from supabase_backend import SupabaseBackend
from langchain_core.chat_history import InMemoryChatMessageHistory
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_groq import ChatGroq
import os
from dotenv import load_dotenv
import re
import json
import uuid
from datetime import datetime

load_dotenv()

app = Flask(__name__)
CORS(app)

# Initialize backends
supabase = SupabaseBackend()

# Chat history store
store = {}

def get_session_history(session_id: str) -> InMemoryChatMessageHistory:
    """Get or create a chat history for a session"""
    if session_id not in store:
        store[session_id] = InMemoryChatMessageHistory()
    return store[session_id]

# System prompt
SYSTEM_PROMPT = """# ROLE: ManoDost AI (Universal Bilingual Friend & Safety Screener)
# CONTEXT: Mental Health Screening Tool for Jaipur-based Users
# ARCHITECTURE: Single-Agent with Hidden Metadata Output

### 1. IDENTITY & PERSONA (THE "FRIEND" INTERFACE)
- **Name:** ManoDost.
- **Background:** A warm, empathetic buddy from Jaipur. Uses "Hinglish" naturally.
- **Tone:** Casual, supportive, and non-clinical. Use words like "Bhai," "Dost," "Chill," and "Sahi hai."
- **Communication Style:** Never list questions. If you need to know about sleep (PHQ-9 Item 3), say: "Yaar, meri toh neend kharab chal rahi hai, tera kya haal hai? Sone mein dikkat toh nahi ho rahi?"

### 2. DYNAMIC BILINGUAL ENGINE
- **Input Variable:** user_language (Values: "hi" | "en").
- **Strict Rule:** You MUST respond in the language defined by user_language. 
- **Mirroring:** If user_language is "hi", use Devanagari script for Hindi. If "en", use conversational English.

### 3. DOMAIN-SPECIFIC CLINICAL SCREENING (PHQ-9 & GAD-7)
- **Constraint:** You are a screening tool, NOT a doctor. Do not diagnose.
- **Methodology:** Silently track the following 16 items across the conversation:
    - PHQ-9: Interest, Mood, Sleep, Energy, Appetite, Self-worth, Concentration, Psychomotor, Suicidal Ideation.
    - GAD-7: Nervousness, Uncontrollable worry, Worrying too much, Trouble relaxing, Restlessness, Irritability, Feeling afraid.
- **Scoring:** Rate each item 0-3 based on user intensity.

### 4. THE "HIDDEN METADATA" PROTOCOL (BACKEND ONLY)
To prevent the user from seeing clinical data, you MUST append a hidden metadata block at the VERY END of every response. This block is for the backend parser only.
- **Format:** ###METADATA_START### {{json_data}} ###METADATA_END###
- **Fields to include:**
    - phq9_total: Integer
    - gad7_total: Integer
    - risk_level: "Low" | "Mid" | "High"
    - emergency_contact: "null" OR "detected_phone_number"
    - primary_emotion: "Happy" | "Anxious" | "Depressed" | "Neutral"

### 5. EMERGENCY & EDGE CASE LOGIC (HIGH RISK)
- **Trigger:** If phq9_total > 10 OR gad7_total > 10 OR any mention of self-harm.
- **Action:**
    1. Pivot the conversation to "Serious Support" mode.
    2. Ask: "Dost, things sound really heavy right now. Can you share an emergency contact number with me? Just so I know you're safe and have someone to lean on."
    3. **Extraction:** Once the user provides a number, store it in the emergency_contact field in the metadata block.
- **Hallucination Guard:** If a user says "I'm fine" after saying "I want to quit," do NOT reset the score to 0. Maintain high-risk status for the remainder of the session.

### 6. NEGATIVE CONSTRAINTS (ANTI-HALLUCINATION)
- DO NOT provide medical prescriptions.
- DO NOT claim to be a licensed therapist.
- DO NOT show the METADATA block to the user in the UI text.
- If the user asks "Are you testing me?", reply: "Arre nahi bhai, bas tera haal-chaal pooch raha hoon."

### 7. OUTPUT EXAMPLE (INTERNAL VIEW)
User: "Yaar bohot tension hai, man kar raha sab khatam kar doon."
Response: "Bhai, aisa mat bol. Main sun raha hoon, kya baat ho gayi? Tu akela nahi hai. Ek kaam kar, mujhe koi emergency contact number de de? Just for safety, taaki main tension-free rahoon ki tera koi dhyan rakhne wala hai."
###METADATA_START### {{"phq9_total": 15, "gad7_total": 12, "risk_level": "High", "emergency_contact": "pending", "primary_emotion": "Crisis"}} ###METADATA_END###
"""

# Create prompt
prompt = ChatPromptTemplate.from_messages([
    ("system", SYSTEM_PROMPT),
    MessagesPlaceholder(variable_name="history"),
    ("human", "{input}")
])

# Initialize LLM
llm = ChatGroq(
    model="llama-3.3-70b-versatile",
    api_key=os.getenv("GROQ_API_KEY")
)

# Create chain
chain = prompt | llm
chain_with_history = RunnableWithMessageHistory(
    chain,
    get_session_history,
    input_messages_key="input",
    history_messages_key="history",
)

def parse_ai_response(raw_response: str):
    """Extract metadata from AI response"""
    metadata_pattern = r'###METADATA_START###\s*(\{[^}]+\})\s*###METADATA_END###'
    metadata_match = re.search(metadata_pattern, raw_response)
    
    if metadata_match:
        try:
            metadata = json.loads(metadata_match.group(1))
            clean_response = re.sub(metadata_pattern, '', raw_response).strip()
            return clean_response, metadata
        except:
            pass
    
    return raw_response.strip(), None

# ==================== SIMPLIFIED ENDPOINTS (NO AUTH) ====================

@app.route('/api/start', methods=['POST'])
def start_session():
    """Start a new session without authentication"""
    data = request.json
    language = data.get('language', 'en')
    device_id = data.get('device_id', str(uuid.uuid4()))
    
    # Create anonymous user ID
    user_id = f"anonymous_{device_id}"
    
    # Create session in Supabase (without auth)
    try:
        result = supabase.create_chat_session_no_auth(user_id, language)
        
        if result['success']:
            session_id = str(result['session_id'])
            
            # Initialize AI chat history
            language_instruction = f"The user has selected {'Hindi/Hinglish' if language == 'hi' else 'English'}. You MUST respond in {'Hindi/Hinglish' if language == 'hi' else 'English'} for all messages."
            
            config = {"configurable": {"session_id": session_id}}
            chain_with_history.invoke(
                {"input": f"[System: User language preference is {language}. {language_instruction}]"},
                config=config
            )
            
            return jsonify({
                "success": True,
                "session_id": session_id,
                "user_id": user_id,
                "message": "Chat session started"
            })
    except Exception as e:
        print(f"Error creating session: {e}")
    
    # Fallback: Create session without Supabase
    session_id = str(uuid.uuid4())
    language_instruction = f"The user has selected {'Hindi/Hinglish' if language == 'hi' else 'English'}. You MUST respond in {'Hindi/Hinglish' if language == 'hi' else 'English'} for all messages."
    
    config = {"configurable": {"session_id": session_id}}
    chain_with_history.invoke(
        {"input": f"[System: User language preference is {language}. {language_instruction}]"},
        config=config
    )
    
    return jsonify({
        "success": True,
        "session_id": session_id,
        "user_id": user_id,
        "message": "Chat session started (in-memory only)"
    })

@app.route('/api/chat', methods=['POST'])
def chat():
    """Send message and get AI response"""
    data = request.json
    session_id = data.get('session_id')
    user_message = data.get('message')
    language = data.get('language', 'en')
    emotion = data.get('emotion')  # Facial emotion from camera
    emotion_confidence = data.get('emotion_confidence')
    
    if not session_id or not user_message:
        return jsonify({"success": False, "error": "Missing session_id or message"}), 400
    
    try:
        # Add emotion context to the message if available
        enhanced_message = user_message
        if emotion and emotion_confidence:
            emotion_context = f"[Facial Expression Detected: {emotion} (confidence: {emotion_confidence:.2f})]"
            enhanced_message = f"{emotion_context} {user_message}"
            print(f"[EMOTION] Detected: {emotion} ({emotion_confidence:.2f})")
        
        # Get AI response
        config = {"configurable": {"session_id": str(session_id)}}
        response = chain_with_history.invoke(
            {"input": enhanced_message},
            config=config
        )
        
        # Parse response
        clean_response, metadata = parse_ai_response(response.content)
        
        # Log only scores to backend terminal
        if metadata:
            total_score = metadata.get('phq9_total', 0) + metadata.get('gad7_total', 0)
            emotion_info = f" | Emotion: {emotion}" if emotion else ""
            print(f"[CHAT] PHQ-9: {metadata.get('phq9_total', 0)}/27 | GAD-7: {metadata.get('gad7_total', 0)}/21 | Total: {total_score}/48 | Risk: {metadata.get('risk_level', 'Unknown')}{emotion_info}")
        
        # Try to store in Supabase (optional)
        try:
            supabase.add_message(
                int(session_id) if session_id.isdigit() else 0,
                user_message,
                clean_response,
                phq9_score=metadata.get('phq9_total') if metadata else None,
                gad7_score=metadata.get('gad7_total') if metadata else None,
                risk_level=metadata.get('risk_level') if metadata else None,
                primary_emotion=emotion if emotion else (metadata.get('primary_emotion') if metadata else None)
            )
            
            # Check for emergency contact
            if metadata and metadata.get('emergency_contact') and metadata['emergency_contact'] != 'null':
                supabase.add_emergency_contact(int(session_id), metadata['emergency_contact'])
        except Exception as e:
            print(f"Could not save to Supabase: {e}")
            # Continue anyway - chat works without Supabase
        
        return jsonify({
            "success": True,
            "response": clean_response,
            "metadata": metadata
        })
    
    except Exception as e:
        print(f"\n❌ CHAT ERROR: {e}\n")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/language', methods=['POST'])
def update_language():
    """Update language preference"""
    data = request.json
    session_id = data.get('session_id')
    language = data.get('language', 'en')
    
    if language not in ['en', 'hi']:
        return jsonify({"success": False, "error": "Invalid language"}), 400
    
    # Update language instruction in chat history
    try:
        language_instruction = f"The user has switched to {'Hindi/Hinglish' if language == 'hi' else 'English'}. You MUST respond in {'Hindi/Hinglish' if language == 'hi' else 'English'} for all future messages."
        
        config = {"configurable": {"session_id": str(session_id)}}
        chain_with_history.invoke(
            {"input": f"[System: {language_instruction}]"},
            config=config
        )
        
        return jsonify({
            "success": True,
            "message": f"Language updated to {language}"
        })
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

# ==================== HEALTH CHECK ====================

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "service": "ManoDost AI API (No Auth)",
        "version": "2.0.0"
    })

@app.route('/', methods=['GET'])
def home():
    """Home endpoint"""
    return jsonify({
        "service": "ManoDost AI API",
        "version": "2.0.0",
        "authentication": "disabled",
        "endpoints": {
            "start_session": "POST /api/start",
            "send_message": "POST /api/chat",
            "update_language": "POST /api/language",
            "health_check": "GET /health"
        }
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)
