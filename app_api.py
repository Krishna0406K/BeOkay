"""
Flask API for ManoDost AI - Connects Kotlin App with AI Agent
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

# ==================== AUTHENTICATION ENDPOINTS ====================

@app.route('/api/auth/signup', methods=['POST'])
def signup():
    """Register new user"""
    data = request.json
    email = data.get('email')
    password = data.get('password')
    name = data.get('name')
    phone = data.get('phone')
    parent_phone = data.get('parent_phone')
    is_junior = data.get('is_junior', False)
    language = data.get('language', 'en')
    
    # Sign up with Supabase Auth
    result = supabase.sign_up(email, password, {"name": name})
    
    if result['success']:
        user_id = result['user'].id
        # Create profile
        profile_result = supabase.create_user_profile(
            user_id, name, phone, parent_phone, is_junior, language
        )
        return jsonify({
            "success": True,
            "user_id": user_id,
            "session": result['session']
        })
    
    return jsonify(result), 400

@app.route('/api/auth/signin', methods=['POST'])
def signin():
    """Sign in user"""
    data = request.json
    email = data.get('email')
    password = data.get('password')
    
    result = supabase.sign_in(email, password)
    
    if result['success']:
        user_id = result['user'].id
        profile = supabase.get_user_profile(user_id)
        return jsonify({
            "success": True,
            "user_id": user_id,
            "profile": profile,
            "session": result['session']
        })
    
    return jsonify(result), 401

@app.route('/api/auth/signout', methods=['POST'])
def signout():
    """Sign out user"""
    result = supabase.sign_out()
    return jsonify(result)

# ==================== USER PROFILE ENDPOINTS ====================

@app.route('/api/user/<user_id>', methods=['GET'])
def get_user(user_id):
    """Get user profile"""
    profile = supabase.get_user_profile(user_id)
    if profile:
        return jsonify({"success": True, "profile": profile})
    return jsonify({"success": False, "error": "User not found"}), 404

@app.route('/api/user/<user_id>/language', methods=['PUT'])
def update_language(user_id):
    """Update user language preference"""
    data = request.json
    language = data.get('language')
    
    if language not in ['en', 'hi']:
        return jsonify({"success": False, "error": "Invalid language"}), 400
    
    result = supabase.update_user_language(user_id, language)
    return jsonify(result)

# ==================== CHAT ENDPOINTS ====================

@app.route('/api/chat/start', methods=['POST'])
def start_chat():
    """Start a new chat session"""
    data = request.json
    user_id = data.get('user_id')
    language = data.get('language', 'en')
    
    # Create session in Supabase
    result = supabase.create_chat_session(user_id, language)
    
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
            "message": "Chat session started"
        })
    
    return jsonify(result), 400

@app.route('/api/chat/message', methods=['POST'])
def send_message():
    """Send message and get AI response"""
    data = request.json
    session_id = data.get('session_id')
    user_message = data.get('message')
    
    if not session_id or not user_message:
        return jsonify({"success": False, "error": "Missing session_id or message"}), 400
    
    try:
        # Get AI response
        config = {"configurable": {"session_id": str(session_id)}}
        response = chain_with_history.invoke(
            {"input": user_message},
            config=config
        )
        
        # Parse response
        clean_response, metadata = parse_ai_response(response.content)
        
        # Store in Supabase
        supabase.add_message(
            int(session_id),
            user_message,
            clean_response,
            phq9_score=metadata.get('phq9_total') if metadata else None,
            gad7_score=metadata.get('gad7_total') if metadata else None,
            risk_level=metadata.get('risk_level') if metadata else None,
            primary_emotion=metadata.get('primary_emotion') if metadata else None
        )
        
        # Check for emergency contact
        if metadata and metadata.get('emergency_contact') and metadata['emergency_contact'] != 'null':
            supabase.add_emergency_contact(int(session_id), metadata['emergency_contact'])
        
        return jsonify({
            "success": True,
            "response": clean_response,
            "metadata": metadata
        })
    
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chat/session/<session_id>', methods=['GET'])
def get_session(session_id):
    """Get session details"""
    summary = supabase.get_session_summary(int(session_id))
    return jsonify(summary)

@app.route('/api/chat/session/<session_id>/end', methods=['POST'])
def end_session(session_id):
    """End chat session"""
    result = supabase.end_chat_session(int(session_id))
    return jsonify(result)

@app.route('/api/chat/history/<user_id>', methods=['GET'])
def get_user_history(user_id):
    """Get all sessions for a user"""
    sessions = supabase.get_user_sessions(user_id)
    return jsonify({"success": True, "sessions": sessions})

# ==================== HEALTH CHECK ====================

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "service": "ManoDost AI API",
        "version": "1.0.0"
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, debug=True)
