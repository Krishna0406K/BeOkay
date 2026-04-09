-- ManoDost AI Supabase Database Schema
-- Run this in your Supabase SQL Editor

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==================== USER PROFILES TABLE ====================
CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT,
    parent_phone TEXT,
    is_junior BOOLEAN DEFAULT FALSE,
    preferred_language TEXT DEFAULT 'en' CHECK (preferred_language IN ('en', 'hi')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id)
);

-- ==================== CHAT SESSIONS TABLE ====================
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    language TEXT NOT NULL CHECK (language IN ('en', 'hi')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ended_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,
    current_phq9_score INTEGER DEFAULT 0,
    current_gad7_score INTEGER DEFAULT 0,
    max_phq9_score INTEGER DEFAULT 0,
    max_gad7_score INTEGER DEFAULT 0,
    risk_level TEXT DEFAULT 'Low' CHECK (risk_level IN ('Low', 'Mid', 'High'))
);

-- ==================== CHAT MESSAGES TABLE ====================
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES chat_sessions(id) ON DELETE CASCADE,
    user_message TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    phq9_score INTEGER,
    gad7_score INTEGER,
    risk_level TEXT CHECK (risk_level IN ('Low', 'Mid', 'High')),
    primary_emotion TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== EMERGENCY CONTACTS TABLE ====================
CREATE TABLE IF NOT EXISTS emergency_contacts (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT REFERENCES chat_sessions(id) ON DELETE CASCADE,
    contact TEXT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ==================== INDEXES FOR PERFORMANCE ====================
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id ON chat_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_active ON chat_sessions(is_active);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_emergency_contacts_session_id ON emergency_contacts(session_id);

-- ==================== ROW LEVEL SECURITY (RLS) ====================
-- Enable RLS on all tables
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE emergency_contacts ENABLE ROW LEVEL SECURITY;

-- User Profiles Policies
CREATE POLICY "Users can view own profile" ON user_profiles
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own profile" ON user_profiles
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own profile" ON user_profiles
    FOR UPDATE USING (auth.uid() = user_id);

-- Chat Sessions Policies
CREATE POLICY "Users can view own sessions" ON chat_sessions
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own sessions" ON chat_sessions
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own sessions" ON chat_sessions
    FOR UPDATE USING (auth.uid() = user_id);

-- Chat Messages Policies
CREATE POLICY "Users can view own messages" ON chat_messages
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM chat_sessions
            WHERE chat_sessions.id = chat_messages.session_id
            AND chat_sessions.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert own messages" ON chat_messages
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM chat_sessions
            WHERE chat_sessions.id = chat_messages.session_id
            AND chat_sessions.user_id = auth.uid()
        )
    );

-- Emergency Contacts Policies
CREATE POLICY "Users can view own emergency contacts" ON emergency_contacts
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM chat_sessions
            WHERE chat_sessions.id = emergency_contacts.session_id
            AND chat_sessions.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert own emergency contacts" ON emergency_contacts
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM chat_sessions
            WHERE chat_sessions.id = emergency_contacts.session_id
            AND chat_sessions.user_id = auth.uid()
        )
    );

-- ==================== FUNCTIONS ====================
-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for user_profiles
CREATE TRIGGER update_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
