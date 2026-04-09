"""
Simple file-based backend for testing without Supabase
"""
import json
import os
from datetime import datetime
from typing import Dict, Optional
import hashlib

class SimpleBackend:
    def __init__(self, storage_file="users.json"):
        self.storage_file = storage_file
        self.users = self._load_users()
    
    def _load_users(self) -> Dict:
        """Load users from file"""
        if os.path.exists(self.storage_file):
            try:
                with open(self.storage_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except:
                return {}
        return {}
    
    def _save_users(self):
        """Save users to file"""
        with open(self.storage_file, 'w', encoding='utf-8') as f:
            json.dump(self.users, f, indent=2, ensure_ascii=False)
    
    def _hash_password(self, password: str) -> str:
        """Simple password hashing"""
        return hashlib.sha256(password.encode()).hexdigest()
    
    def sign_up(self, email: str, password: str, metadata: Dict = None) -> Dict:
        """Register a new user"""
        try:
            if email in self.users:
                return {"success": False, "error": "User already exists"}
            
            user_id = f"user_{len(self.users) + 1}"
            self.users[email] = {
                "user_id": user_id,
                "email": email,
                "password": self._hash_password(password),
                "metadata": metadata or {},
                "created_at": datetime.now().isoformat()
            }
            self._save_users()
            
            return {
                "success": True,
                "user": type('User', (), {"id": user_id})(),
                "session": {
                    "access_token": f"token_{user_id}",
                    "refresh_token": f"refresh_{user_id}",
                    "expires_in": 3600
                }
            }
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def sign_in(self, email: str, password: str) -> Dict:
        """Sign in existing user"""
        try:
            if email not in self.users:
                return {"success": False, "error": "User not found"}
            
            user = self.users[email]
            if user["password"] != self._hash_password(password):
                return {"success": False, "error": "Invalid password"}
            
            return {
                "success": True,
                "user": type('User', (), {"id": user["user_id"]})(),
                "session": {
                    "access_token": f"token_{user['user_id']}",
                    "refresh_token": f"refresh_{user['user_id']}",
                    "expires_in": 3600
                }
            }
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def sign_out(self) -> Dict:
        """Sign out user"""
        return {"success": True}
    
    def get_current_user(self) -> Optional[Dict]:
        """Get currently authenticated user"""
        return None
    
    def create_user_profile(self, user_id: str, name: str, phone: str = None, 
                           parent_phone: str = None, is_junior: bool = False,
                           preferred_language: str = "en") -> Dict:
        """Create user profile"""
        try:
            # Find user by user_id
            for email, user in self.users.items():
                if user["user_id"] == user_id:
                    user["profile"] = {
                        "name": name,
                        "phone": phone,
                        "parent_phone": parent_phone,
                        "is_junior": is_junior,
                        "preferred_language": preferred_language
                    }
                    self._save_users()
                    return {"success": True, "data": user["profile"]}
            return {"success": False, "error": "User not found"}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    def get_user_profile(self, user_id: str) -> Optional[Dict]:
        """Get user profile"""
        for email, user in self.users.items():
            if user["user_id"] == user_id:
                return user.get("profile")
        return None
    
    def update_user_language(self, user_id: str, language: str) -> Dict:
        """Update user's preferred language"""
        try:
            for email, user in self.users.items():
                if user["user_id"] == user_id:
                    if "profile" not in user:
                        user["profile"] = {}
                    user["profile"]["preferred_language"] = language
                    self._save_users()
                    return {"success": True}
            return {"success": False, "error": "User not found"}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    # Dummy methods for chat (not implemented in simple version)
    def create_chat_session(self, user_id: str, language: str) -> Dict:
        return {"success": True, "session_id": 1}
    
    def add_message(self, session_id: int, user_message: str, ai_response: str, **kwargs) -> Dict:
        return {"success": True}
    
    def get_session_summary(self, session_id: int) -> Dict:
        return {"session": None, "message_count": 0, "messages": [], "emergency_contacts": []}
    
    def end_chat_session(self, session_id: int) -> Dict:
        return {"success": True}
    
    def get_user_sessions(self, user_id: str) -> list:
        return []
    
    def add_emergency_contact(self, session_id: int, contact: str) -> Dict:
        return {"success": True}
