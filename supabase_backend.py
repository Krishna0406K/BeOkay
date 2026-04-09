"""
Supabase Backend Integration for ManoDost AI
Handles user authentication, session management, and chat data storage
"""

from supabase import create_client, Client
from datetime import datetime
from typing import Dict, List, Optional
import os
from dotenv import load_dotenv
import json

load_dotenv()

class SupabaseBackend:
    def __init__(self):
        supabase_url = os.getenv("SUPABASE_URL")
        supabase_key = os.getenv("SUPABASE_KEY")
        
        if not supabase_url or not supabase_key:
            raise ValueError("SUPABASE_URL and SUPABASE_KEY must be set in .env file")
        
        self.client: Client = create_client(supabase_url, supabase_key)
    
    # ==================== USER AUTHENTICATION ====================
    
    def sign_up(self, email: str, password: str, metadata: Dict = None) -> Dict:
        """Register a new user"""
        try:
            response = self.client.auth.sign_up({
                "email": email,
                "password": password,
                "options": {
                    "data": metadata or {}
                }
            })
            return {"success": True, "user": response.user, "session": response.session}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def sign_in(self, email: str, password: str) -> Dict:
        """Sign in existing user"""
        try:
            response = self.client.auth.sign_in_with_password({
                "email": email,
                "password": password
            })
            return {"success": True, "user": response.user, "session": response.session}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def sign_out(self) -> Dict:
        """Sign out current user"""
        try:
            self.client.auth.sign_out()
            return {"success": True}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def get_current_user(self) -> Optional[Dict]:
        """Get currently authenticated user"""
        try:
            user = self.client.auth.get_user()
            return user
        except:
            return None
    
    # ==================== USER PROFILE ====================
    
    def create_user_profile(self, user_id: str, name: str, phone: str = None, 
                           parent_phone: str = None, is_junior: bool = False,
                           preferred_language: str = "en") -> Dict:
        """Create user profile in database"""
        try:
            data = {
                "user_id": user_id,
                "name": name,
                "phone": phone,
                "parent_phone": parent_phone,
                "is_junior": is_junior,
                "preferred_language": preferred_language,
                "created_at": datetime.now().isoformat()
            }
            response = self.client.table("user_profiles").insert(data).execute()
            return {"success": True, "data": response.data}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def get_user_profile(self, user_id: str) -> Optional[Dict]:
        """Get user profile"""
        try:
            response = self.client.table("user_profiles").select("*").eq("user_id", user_id).execute()
            return response.data[0] if response.data else None
        except:
            return None
    
    def update_user_language(self, user_id: str, language: str) -> Dict:
        """Update user's preferred language"""
        try:
            response = self.client.table("user_profiles").update({
                "preferred_language": language
            }).eq("user_id", user_id).execute()
            return {"success": True, "data": response.data}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    # ==================== CHAT SESSIONS ====================
    
    def create_chat_session_no_auth(self, user_id: str, language: str) -> Dict:
        """Create a new chat session without authentication"""
        try:
            data = {
                "user_id": user_id,
                "language": language,
                "created_at": datetime.now().isoformat(),
                "current_phq9_score": 0,
                "current_gad7_score": 0,
                "max_phq9_score": 0,
                "max_gad7_score": 0,
                "risk_level": "Low",
                "is_active": True
            }
            response = self.client.table("chat_sessions").insert(data).execute()
            return {"success": True, "session_id": response.data[0]["id"], "data": response.data[0]}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def create_chat_session(self, user_id: str, language: str) -> Dict:
        """Create a new chat session"""
        try:
            data = {
                "user_id": user_id,
                "language": language,
                "created_at": datetime.now().isoformat(),
                "current_phq9_score": 0,
                "current_gad7_score": 0,
                "max_phq9_score": 0,
                "max_gad7_score": 0,
                "risk_level": "Low",
                "is_active": True
            }
            response = self.client.table("chat_sessions").insert(data).execute()
            return {"success": True, "session_id": response.data[0]["id"], "data": response.data[0]}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def get_active_session(self, user_id: str) -> Optional[Dict]:
        """Get user's active chat session"""
        try:
            response = self.client.table("chat_sessions").select("*").eq("user_id", user_id).eq("is_active", True).order("created_at", desc=True).limit(1).execute()
            return response.data[0] if response.data else None
        except:
            return None
    
    def end_chat_session(self, session_id: int) -> Dict:
        """Mark session as inactive"""
        try:
            response = self.client.table("chat_sessions").update({
                "is_active": False,
                "ended_at": datetime.now().isoformat()
            }).eq("id", session_id).execute()
            return {"success": True, "data": response.data}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    # ==================== CHAT MESSAGES ====================
    
    def add_message(self, session_id: int, user_message: str, ai_response: str,
                   phq9_score: int = None, gad7_score: int = None,
                   risk_level: str = None, primary_emotion: str = None) -> Dict:
        """Store a chat message with metadata"""
        try:
            data = {
                "session_id": session_id,
                "user_message": user_message,
                "ai_response": ai_response,
                "phq9_score": phq9_score,
                "gad7_score": gad7_score,
                "risk_level": risk_level,
                "primary_emotion": primary_emotion,
                "timestamp": datetime.now().isoformat()
            }
            response = self.client.table("chat_messages").insert(data).execute()
            
            # Update session scores if provided
            if phq9_score is not None or gad7_score is not None:
                self.update_session_scores(session_id, phq9_score, gad7_score, risk_level)
            
            return {"success": True, "data": response.data}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def get_session_messages(self, session_id: int) -> List[Dict]:
        """Get all messages for a session"""
        try:
            response = self.client.table("chat_messages").select("*").eq("session_id", session_id).order("timestamp").execute()
            return response.data
        except:
            return []
    
    def update_session_scores(self, session_id: int, phq9_score: int = None,
                             gad7_score: int = None, risk_level: str = None) -> Dict:
        """Update session mental health scores"""
        try:
            # Get current session
            session = self.client.table("chat_sessions").select("*").eq("id", session_id).execute()
            if not session.data:
                return {"success": False, "error": "Session not found"}
            
            current = session.data[0]
            update_data = {}
            
            if phq9_score is not None:
                update_data["current_phq9_score"] = phq9_score
                update_data["max_phq9_score"] = max(current.get("max_phq9_score", 0), phq9_score)
            
            if gad7_score is not None:
                update_data["current_gad7_score"] = gad7_score
                update_data["max_gad7_score"] = max(current.get("max_gad7_score", 0), gad7_score)
            
            if risk_level:
                update_data["risk_level"] = risk_level
            
            response = self.client.table("chat_sessions").update(update_data).eq("id", session_id).execute()
            return {"success": True, "data": response.data}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    # ==================== EMERGENCY CONTACTS ====================
    
    def add_emergency_contact(self, session_id: int, contact: str) -> Dict:
        """Store emergency contact"""
        try:
            data = {
                "session_id": session_id,
                "contact": contact,
                "timestamp": datetime.now().isoformat()
            }
            response = self.client.table("emergency_contacts").insert(data).execute()
            return {"success": True, "data": response.data}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def get_emergency_contacts(self, session_id: int) -> List[Dict]:
        """Get emergency contacts for a session"""
        try:
            response = self.client.table("emergency_contacts").select("*").eq("session_id", session_id).execute()
            return response.data
        except:
            return []
    
    # ==================== ANALYTICS ====================
    
    def get_user_sessions(self, user_id: str) -> List[Dict]:
        """Get all sessions for a user"""
        try:
            response = self.client.table("chat_sessions").select("*").eq("user_id", user_id).order("created_at", desc=True).execute()
            return response.data
        except:
            return []
    
    def get_session_summary(self, session_id: int) -> Dict:
        """Get comprehensive session summary"""
        try:
            session = self.client.table("chat_sessions").select("*").eq("id", session_id).execute()
            messages = self.get_session_messages(session_id)
            emergency_contacts = self.get_emergency_contacts(session_id)
            
            return {
                "session": session.data[0] if session.data else None,
                "message_count": len(messages),
                "messages": messages,
                "emergency_contacts": emergency_contacts
            }
        except Exception as e:
            return {"error": str(e)}
