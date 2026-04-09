from flask import Flask, jsonify, request
from backend import ConversationBackend

app = Flask(__name__)
backend = ConversationBackend()

@app.route('/', methods=['GET'])
def home():
    """API status endpoint"""
    return jsonify({
        "status": "running",
        "message": "ManoDost API is active",
        "endpoints": [
            "/api/sessions",
            "/api/session/<session_id>",
            "/api/session/<session_id>/score",
            "/api/session/<session_id>/history",
            "/api/session/<session_id>/messages"
        ]
    })

@app.route('/api/session/<session_id>', methods=['GET'])
def get_session(session_id):
    """Get complete session data"""
    data = backend.get_session_data(session_id)
    if data:
        return jsonify(data)
    return jsonify({"error": "Session not found"}), 404

@app.route('/api/session/<session_id>/score', methods=['GET'])
def get_score(session_id):
    """Get current mental health score"""
    score = backend.get_current_score(session_id)
    if score is not None:
        return jsonify({"session_id": session_id, "current_score": score})
    return jsonify({"error": "Session not found"}), 404

@app.route('/api/session/<session_id>/history', methods=['GET'])
def get_score_history(session_id):
    """Get score progression over time"""
    history = backend.get_score_history(session_id)
    return jsonify({"session_id": session_id, "score_history": history})

@app.route('/api/session/<session_id>/messages', methods=['GET'])
def get_messages(session_id):
    """Get conversation messages (clean responses only)"""
    data = backend.get_session_data(session_id)
    if data:
        messages = [
            {
                "timestamp": msg["timestamp"],
                "user": msg["user"],
                "ai": msg["ai_clean"]
            }
            for msg in data["messages"]
        ]
        return jsonify({"session_id": session_id, "messages": messages})
    return jsonify({"error": "Session not found"}), 404

@app.route('/api/sessions', methods=['GET'])
def get_all_sessions():
    """Get all sessions"""
    return jsonify(backend.get_all_sessions())

if __name__ == '__main__':
    app.run(debug=True, port=5000)
