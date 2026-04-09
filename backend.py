import re
import json
from datetime import datetime
from typing import Dict, List, Optional
import os

class ConversationBackend:
    """Backend to store conversation data, scores, and metadata"""
    
    def __init__(self, storage_file="sessions.json"):
        self.storage_file = storage_file
        self.sessions = self._load_sessions()
    
    def _load_sessions(self) -> Dict:
        """Load sessions from file"""
        if os.path.exists(self.storage_file):
            try:
                with open(self.storage_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except Exception as e:
                print(f"Warning: Could not load sessions: {e}")
                return {}
        return {}
    
    def _save_sessions(self):
        """Save sessions to file"""
        try:
            with open(self.storage_file, 'w', encoding='utf-8') as f:
                json.dump(self.sessions, f, indent=2, ensure_ascii=False)
        except Exception as e:
            print(f"Warning: Could not save sessions: {e}")
    
    def create_session(self, session_id: str, user_language: str):
        """Initialize a new session"""
        self.sessions[session_id] = {
            "session_id": session_id,
            "user_language": user_language,
            "created_at": datetime.now().isoformat(),
            "messages": [],
            "scores": [],
            "emergency_numbers": [],
            "current_score": 0,
            "max_score": 0
        }
        self._save_sessions()
    
    def parse_ai_response(self, raw_response: str) -> Dict:
        """Extract metadata and clean response from AI output"""
        # Pattern to match ###METADATA_START### {json} ###METADATA_END###
        metadata_pattern = r'###METADATA_START###\s*(\{[^}]+\})\s*###METADATA_END###'
        metadata_match = re.search(metadata_pattern, raw_response)
        
        if metadata_match:
            try:
                metadata = json.loads(metadata_match.group(1))
                # Remove metadata from response
                clean_response = re.sub(metadata_pattern, '', raw_response).strip()
                
                return {
                    "score": metadata.get("phq9_total"),
                    "gad7_score": metadata.get("gad7_total"),
                    "risk_level": metadata.get("risk_level"),
                    "emergency_contact": metadata.get("emergency_contact"),
                    "primary_emotion": metadata.get("primary_emotion"),
                    "response": clean_response,
                    "raw_response": raw_response
                }
            except json.JSONDecodeError:
                pass
        
        # Fallback: no metadata found, return full response
        return {
            "score": None,
            "gad7_score": None,
            "risk_level": None,
            "emergency_contact": None,
            "primary_emotion": None,
            "response": raw_response.strip(),
            "raw_response": raw_response
        }
    
    def add_message(self, session_id: str, user_message: str, ai_response: str):
        """Store a conversation turn with extracted metadata"""
        if session_id not in self.sessions:
            raise ValueError(f"Session {session_id} not found")
        
        parsed = self.parse_ai_response(ai_response)
        
        message_data = {
            "timestamp": datetime.now().isoformat(),
            "user": user_message,
            "ai_raw": ai_response,
            "ai_clean": parsed["response"],
            "phq9_score": parsed["score"],
            "gad7_score": parsed["gad7_score"],
            "risk_level": parsed["risk_level"],
            "primary_emotion": parsed["primary_emotion"]
        }
        
        self.sessions[session_id]["messages"].append(message_data)
        
        # Update scores
        if parsed["score"] is not None:
            self.sessions[session_id]["scores"].append({
                "timestamp": datetime.now().isoformat(),
                "phq9_score": parsed["score"],
                "gad7_score": parsed["gad7_score"],
                "risk_level": parsed["risk_level"]
            })
            self.sessions[session_id]["current_score"] = parsed["score"]
            self.sessions[session_id]["max_score"] = max(
                self.sessions[session_id]["max_score"],
                parsed["score"]
            )
        
        # Store emergency contact if detected
        if parsed["emergency_contact"] and parsed["emergency_contact"] != "null":
            self.sessions[session_id]["emergency_numbers"].append({
                "timestamp": datetime.now().isoformat(),
                "contact": parsed["emergency_contact"]
            })
        
        self._save_sessions()
        return parsed["response"]
    
    def get_session_data(self, session_id: str) -> Optional[Dict]:
        """Retrieve all data for a session"""
        # Reload from file to get latest data
        self.sessions = self._load_sessions()
        return self.sessions.get(session_id)
    
    def get_current_score(self, session_id: str) -> Optional[int]:
        """Get the current mental health score"""
        self.sessions = self._load_sessions()
        if session_id in self.sessions:
            return self.sessions[session_id]["current_score"]
        return None
    
    def get_score_history(self, session_id: str) -> List[Dict]:
        """Get score progression over time"""
        self.sessions = self._load_sessions()
        if session_id in self.sessions:
            return self.sessions[session_id]["scores"]
        return []
    
    def export_session(self, session_id: str, filepath: str):
        """Export session data to JSON file"""
        if session_id not in self.sessions:
            raise ValueError(f"Session {session_id} not found")
        
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(self.sessions[session_id], f, indent=2, ensure_ascii=False)
    
    def get_all_sessions(self) -> Dict:
        """Get all session data"""
        self.sessions = self._load_sessions()
        return self.sessions
