"""
Flask API for ManoDost AI - Simple Version (No Supabase Required)
Use this for testing until you get the correct Supabase key
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from simple_backend import SimpleBackend
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

# Initialize simple backend
backend = SimpleBackend()

# Chat history store
store = {}

def get_session_history(session_id: str) -> InMemoryChatMessageHistory:
    """Get or create a chat history for a session"""
    if session_id not in store:
        store[session_id] = InMemoryChatMessageHistory()
    return store[session_id]

# System prompt
SYSTEM_PROMPT = """# ROLE: ManoDost AI (Universal Bilingual Friend & Safety Screener)

### 1. IDENTITY & PERSONA
- **Name:** ManoDost.
- **Tone:** Casual, supportive, and non-clinical. Use words like "Bhai," "Dost," "Chill," and "Sahi hai."

### 2. DYNAMIC BILINGUAL ENGINE
- **Strict Rule:** You MUST respond in the language defined by user_language. 
- If user_language is "hi", use Hindi/Hinglish.
- If user_language is "en", use conversational English.

### 3. OUTPUT
Keep responses friendly and supportive. Be a good friend!
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

# ==================== AUTHENTICATION ENDPOINTS ====================

@app.route('/api/auth/signup', methods=['POST'])
def signup():
    """Register new user"""
    try:
        data = request.json
        print(f"📝 Signup request: {data.get('email')}")
        
        email = data.get('email')
        password = data.get('password')
        name = data.get('name')
        phone = data.get('phone')
        parent_phone = data.get('parent_phone')
        is_junior = data.get('is_junior', False)
        language = data.get('language', 'en')
        
        # Sign up with Simple Backend
        result = backend.sign_up(email, password, {"name": name})
        
        if result['success']:
            user_id = result['user'].id
            # Create profile
            profile_result = backend.create_user_profile(
                user_id, name, phone, parent_phone, is_junior, language
            )
            print(f"✅ Signup successful: {email}")
            return jsonify({
                "success": True,
                "user_id": user_id,
                "session": result['session']
            })
        
        print(f"❌ Signup failed: {result.get('error')}")
        return jsonify(result), 400
    except Exception as e:
        print(f"❌ Signup error: {str(e)}")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/auth/signin', methods=['POST'])
def signin():
    """Sign in user"""
    try:
        data = request.json
        print(f"🔐 Signin request: {data.get('email')}")
        
        email = data.get('email')
        password = data.get('password')
        
        result = backend.sign_in(email, password)
        
        if result['success']:
            user_id = result['user'].id
            profile = backend.get_user_profile(user_id)
            print(f"✅ Signin successful: {email}")
            return jsonify({
                "success": True,
                "user_id": user_id,
                "profile": profile,
                "session": result['session']
            })
        
        print(f"❌ Signin failed: {result.get('error')}")
        return jsonify(result), 401
    except Exception as e:
        print(f"❌ Signin error: {str(e)}")
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/auth/signout', methods=['POST'])
def signout():
    """Sign out user"""
    result = backend.sign_out()
    return jsonify(result)

# ==================== USER PROFILE ENDPOINTS ====================

@app.route('/api/user/<user_id>', methods=['GET'])
def get_user(user_id):
    """Get user profile"""
    profile = backend.get_user_profile(user_id)
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
    
    result = backend.update_user_language(user_id, language)
    return jsonify(result)

# ==================== CHAT ENDPOINTS ====================

@app.route('/api/chat/start', methods=['POST'])
def start_chat():
    """Start a new chat session"""
    try:
        data = request.json
        user_id = data.get('user_id')
        language = data.get('language', 'en')
        
        # Create session
        result = backend.create_chat_session(user_id, language)
        
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
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chat/message', methods=['POST'])
def send_message():
    """Send message and get AI response"""
    try:
        data = request.json
        session_id = data.get('session_id')
        user_message = data.get('message')
        
        if not session_id or not user_message:
            return jsonify({"success": False, "error": "Missing session_id or message"}), 400
        
        # Get AI response
        config = {"configurable": {"session_id": str(session_id)}}
        response = chain_with_history.invoke(
            {"input": user_message},
            config=config
        )
        
        # Store in backend
        backend.add_message(int(session_id), user_message, response.content)
        
        return jsonify({
            "success": True,
            "response": response.content,
            "metadata": None
        })
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/chat/session/<session_id>', methods=['GET'])
def get_session(session_id):
    """Get session details"""
    summary = backend.get_session_summary(int(session_id))
    return jsonify(summary)

@app.route('/api/chat/session/<session_id>/end', methods=['POST'])
def end_session(session_id):
    """End chat session"""
    result = backend.end_chat_session(int(session_id))
    return jsonify(result)

@app.route('/api/chat/history/<user_id>', methods=['GET'])
def get_user_history(user_id):
    """Get all sessions for a user"""
    sessions = backend.get_user_sessions(user_id)
    return jsonify({"success": True, "sessions": sessions})

# ==================== HEALTH CHECK ====================

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        "status": "healthy",
        "service": "ManoDost AI API (Simple Version)",
        "version": "1.0.0"
    })

if __name__ == '__main__':
    print("=" * 60)
    print("🚀 ManoDost AI API - Simple Version (No Supabase)")
    print("=" * 60)
    print("✅ Using file-based authentication")
    print("✅ API will be available at: http://0.0.0.0:8000")
    print("✅ Health check: http://localhost:8000/health")
    print("=" * 60)
    app.run(host='0.0.0.0', port=8000, debug=True)
