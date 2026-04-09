"""
Real-Time Voice Agent for ManoDost AI
Uses Groq API for fast conversational AI with friendly mental health screening
"""

import os
import json
from typing import Dict, Optional
from dotenv import load_dotenv
from groq import Groq
import re

load_dotenv()

# Initialize Groq client
groq_client = Groq(api_key=os.getenv("GROQ_API_KEY"))

# Voice chat history store
voice_store = {}

def get_voice_session_history(session_id: str):
    """Get or create a voice chat history for a session"""
    if session_id not in voice_store:
        voice_store[session_id] = []
    return voice_store[session_id]

# Friendly Voice Agent System Prompt
VOICE_AGENT_PROMPT = """ROLE: beokay (Bilingual Voice Mental Health Companion)

CONTEXT:
You are a real-time conversational voice AI designed for mental health screening (NOT diagnosis).
You talk like a supportive friend while silently analyzing mental health signals and adapting your response dynamically.

--------------------------------------------
1. IDENTITY & VOICE STYLE
--------------------------------------------
- Name: beokay
- Tone: Calm, friendly, motivating
- Style:
  - Short responses (2–3 paragraphs max)
  - Speak like a real person on a call
  - No long explanations
  - No bullet lists
  - Always conversational

Examples:
- "Hey… lag raha hai thoda heavy chal raha hai sab. Tu thoda share karega kya?"
- "That sounds tough… I’m here, tell me what’s been going on."

--------------------------------------------
2. LANGUAGE ADAPTATION
--------------------------------------------
- Detect user language:
  - Hindi → Hindi
  - English → English
  - Mixed → Hinglish
- Always mirror tone and comfort level

--------------------------------------------
3. SILENT MENTAL HEALTH ANALYSIS
--------------------------------------------
Track internally (DO NOT show):

PHQ-9:
- mood, sleep, energy, interest, appetite, self-worth, focus, suicidal thoughts

GAD-7:
- worry, anxiety, restlessness, irritability, fear

Score each:
0 = none  
1 = mild  
2 = moderate  
3 = severe  

--------------------------------------------
4. REAL-TIME THRESHOLD SYSTEM
--------------------------------------------
threshold_score = phq9_total + gad7_total

Levels:
- 0–4 → Low
- 5–10 → Mild
- 11–20 → Moderate
- 21+ → High

--------------------------------------------
5. ADAPTIVE RESPONSE BEHAVIOR (CORE)
--------------------------------------------

🔹 LOW:
- Build comfort
- Ask simple open-ended questions

🔹 MILD (≥5):
- Start giving light suggestions
- Example:
  "Tu thoda break leke walk try kare… thoda mind fresh ho jata hai"

🔹 MODERATE (≥10):
- Give actionable suggestions + motivation
- Example:
  "Tu ek kaam kar… aaj thoda phone side rakh ke 10 min deep breathing try kar… kaafi help karta hai"

🔹 HIGH:
- Show strong concern
- Encourage reaching out
- Stay present and supportive

--------------------------------------------
6. CONTINUOUS CALL-LIKE FLOW
--------------------------------------------
- Always:
  👉 Suggest → motivate → ask follow-up  
- Never stop conversation

Example:
"Thoda overwhelming lag raha hai… tu ek kaam kar, thoda bahar walk pe ja ya music sun… waise aaj ka din kaisa tha?"

--------------------------------------------
7. MOTIVATION STYLE
--------------------------------------------
- Use:
  - "Tu kar sakta hai"
  - "Slowly thoda better hoga"
  - "Main hoon yahan"

- Keep it real, not cheesy

--------------------------------------------
8. EXIT HANDLING
--------------------------------------------
If user says:
- bye / stop / later

Then:
- Give short summary
- Mention mental state
- Give 2–3 suggestions

Example:
"Okay… take care. Lag raha hai tu thoda stressed aur tired feel kar raha hai. Thoda rest, kisi se baat karna aur routine thoda improve karna help karega."

--------------------------------------------
9. SAFETY MODE (HIGH RISK)
--------------------------------------------
Trigger:
- suicidal intent
OR
- threshold ≥ 10 with strong distress

Action:
- Stay calm
- Encourage help
- Offer support

Example:
"Yeh thoda serious lag raha hai… tu kisi trusted person se baat kar sakta hai kya? Main yahin hoon tere saath."

--------------------------------------------
10. LOCAL HELP SUGGESTION
--------------------------------------------
If threshold ≥ 10:

- Gently suggest:
"Agar tu chahe toh kisi professional se baat karna bhi help kar sakta hai… main nearby options suggest kar sakta hoon"

--------------------------------------------
11. HARD CONSTRAINTS
--------------------------------------------
- DO NOT diagnose
- DO NOT sound clinical
- DO NOT give medical advice
- DO NOT overwhelm user
- DO NOT speak too long

--------------------------------------------
12. RESPONSE LENGTH RULE (VERY IMPORTANT)
--------------------------------------------
- Max: 2–3 short paragraphs
- No long answers
- Keep it voice-friendly

--------------------------------------------
13. CORE PRINCIPLE
--------------------------------------------
👉 Talk like a friend  
👉 Adapt like a smart system  
👉 Support like a human  

--------------------------------------------
"""

class VoiceAgent:
    def __init__(self):
        pass
    
    def parse_metadata(self, response: str):
        """Extract metadata from AI response"""
        metadata_pattern = r'###METADATA_START###\s*(\{[^}]+\})\s*###METADATA_END###'
        metadata_match = re.search(metadata_pattern, response)
        
        if metadata_match:
            try:
                metadata = json.loads(metadata_match.group(1))
                clean_response = re.sub(metadata_pattern, '', response).strip()
                return clean_response, metadata
            except:
                pass
        
        return response.strip(), None
    
    async def process_voice_input(self, session_id: str, user_text: str, language: str = "en"):
        """Process voice input and generate response using Groq"""
        
        # Get chat history
        history = get_voice_session_history(session_id)
        
        # Build conversation context (last 4 messages only for speed)
        messages = [{"role": "system", "content": VOICE_AGENT_PROMPT}]
        
        for msg in history[-4:]:
            messages.append(msg)
        
        # Add language instruction
        lang_instruction = "Respond in natural Hinglish (mix Hindi and English)" if language == "hi" else "Respond in conversational English"
        messages.append({
            "role": "user",
            "content": f"[Language: {lang_instruction}]\n\nUser: {user_text}"
        })
        
        # Generate response with Groq (fast model)
        try:
            response = groq_client.chat.completions.create(
                model="llama-3.1-8b-instant",
                messages=messages,
                temperature=0.7,
                max_tokens=100,
            )
            
            response_text = response.choices[0].message.content
            
            # Parse response
            clean_response, metadata = self.parse_metadata(response_text)
            
            # Log only scores to backend terminal
            if metadata:
                total_score = metadata.get('phq9_total', 0) + metadata.get('gad7_total', 0)
                print(f"[VOICE] PHQ-9: {metadata.get('phq9_total', 0)}/27 | GAD-7: {metadata.get('gad7_total', 0)}/21 | Total: {total_score}/48 | Risk: {metadata.get('risk_level', 'Unknown')}")
            
            # Add to history
            history.append({"role": "user", "content": user_text})
            history.append({"role": "assistant", "content": clean_response})
            
            return {
                "response": clean_response,
                "metadata": metadata,
                "session_id": session_id
            }
        except Exception as e:
            print(f"\n❌ ERROR: {e}\n")
            return {
                "response": "Sorry, I didn't catch that. Can you say it again?" if language == "en" else "Sorry, samajh nahi aaya. Phir se bolo?",
                "metadata": None,
                "session_id": session_id
            }
    
    def transcribe_audio(self, audio_file_path: str, language: str = None):
        """Transcribe audio using Groq Whisper with language detection"""
        try:
            with open(audio_file_path, "rb") as audio_file:
                transcription = groq_client.audio.transcriptions.create(
                    file=audio_file,
                    model="whisper-large-v3-turbo",
                    response_format="text",
                    language=language if language else None
                )
            return transcription
        except Exception as e:
            print(f"Transcription error: {e}")
            return ""
    
    def get_session_summary(self, session_id: str):
        """Get summary of the voice session"""
        history = get_voice_session_history(session_id)
        
        # Extract all metadata from conversation
        all_metadata = []
        for msg in history:
            if msg.get("role") == "assistant":
                _, metadata = self.parse_metadata(msg.get("content", ""))
                if metadata:
                    all_metadata.append(metadata)
        
        # Get final scores
        if all_metadata:
            final_metadata = all_metadata[-1]
            return {
                "session_id": session_id,
                "total_messages": len(history),
                "phq9_score": final_metadata.get("phq9_total", 0),
                "gad7_score": final_metadata.get("gad7_total", 0),
                "risk_level": final_metadata.get("risk_level", "Unknown"),
                "primary_emotion": final_metadata.get("primary_emotion", "Neutral"),
                "emergency_contact": final_metadata.get("emergency_contact", None)
            }
        
        return {
            "session_id": session_id,
            "total_messages": len(history),
            "status": "incomplete"
        }

# Global voice agent instance
voice_agent = VoiceAgent()
# Global voice agent instance
voice_agent = VoiceAgent()
