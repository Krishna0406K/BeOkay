-- Fix Supabase Row Level Security Policies
-- Run this in your Supabase SQL Editor: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/sql

-- ==================== DISABLE EMAIL CONFIRMATION (FOR DEVELOPMENT) ====================
-- This allows users to sign in immediately without email confirmation
-- WARNING: Re-enable this in production!

-- You need to do this in the Supabase Dashboard:
-- 1. Go to: Authentication > Settings
-- 2. Find "Enable email confirmations"
-- 3. Toggle it OFF for development
-- 4. Save changes

-- ==================== DROP EXISTING RLS POLICIES ====================

DROP POLICY IF EXISTS "Users can view own profile" ON user_profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON user_profiles;
DROP POLICY IF EXISTS "Users can insert own profile" ON user_profiles;
DROP POLICY IF EXISTS "Users can view own sessions" ON chat_sessions;
DROP POLICY IF EXISTS "Users can create own sessions" ON chat_sessions;
DROP POLICY IF EXISTS "Users can view session messages" ON chat_messages;
DROP POLICY IF EXISTS "Users can create messages" ON chat_messages;
DROP POLICY IF EXISTS "Users can view emergency contacts" ON emergency_contacts;
DROP POLICY IF EXISTS "Users can create emergency contacts" ON emergency_contacts;

-- ==================== CREATE PERMISSIVE RLS POLICIES ====================

-- User Profiles: Allow authenticated users to manage their own profiles
CREATE POLICY "Users can view own profile"
ON user_profiles FOR SELECT
TO authenticated
USING (auth.uid()::text = user_id);

CREATE POLICY "Users can insert own profile"
ON user_profiles FOR INSERT
TO authenticated
WITH CHECK (auth.uid()::text = user_id);

CREATE POLICY "Users can update own profile"
ON user_profiles FOR UPDATE
TO authenticated
USING (auth.uid()::text = user_id)
WITH CHECK (auth.uid()::text = user_id);

-- Chat Sessions: Allow authenticated users to manage their own sessions
CREATE POLICY "Users can view own sessions"
ON chat_sessions FOR SELECT
TO authenticated
USING (auth.uid()::text = user_id);

CREATE POLICY "Users can create own sessions"
ON chat_sessions FOR INSERT
TO authenticated
WITH CHECK (auth.uid()::text = user_id);

CREATE POLICY "Users can update own sessions"
ON chat_sessions FOR UPDATE
TO authenticated
USING (auth.uid()::text = user_id)
WITH CHECK (auth.uid()::text = user_id);

-- Chat Messages: Allow users to view and create messages for their sessions
CREATE POLICY "Users can view session messages"
ON chat_messages FOR SELECT
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM chat_sessions
    WHERE chat_sessions.id = chat_messages.session_id
    AND chat_sessions.user_id = auth.uid()::text
  )
);

CREATE POLICY "Users can create messages"
ON chat_messages FOR INSERT
TO authenticated
WITH CHECK (
  EXISTS (
    SELECT 1 FROM chat_sessions
    WHERE chat_sessions.id = chat_messages.session_id
    AND chat_sessions.user_id = auth.uid()::text
  )
);

-- Emergency Contacts: Allow users to manage emergency contacts for their sessions
CREATE POLICY "Users can view emergency contacts"
ON emergency_contacts FOR SELECT
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM chat_sessions
    WHERE chat_sessions.id = emergency_contacts.session_id
    AND chat_sessions.user_id = auth.uid()::text
  )
);

CREATE POLICY "Users can create emergency contacts"
ON emergency_contacts FOR INSERT
TO authenticated
WITH CHECK (
  EXISTS (
    SELECT 1 FROM chat_sessions
    WHERE chat_sessions.id = emergency_contacts.session_id
    AND chat_sessions.user_id = auth.uid()::text
  )
);

-- ==================== GRANT PERMISSIONS ====================

-- Grant usage on sequences (for auto-increment IDs)
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO authenticated;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO anon;

-- Grant permissions on tables
GRANT ALL ON user_profiles TO authenticated;
GRANT ALL ON chat_sessions TO authenticated;
GRANT ALL ON chat_messages TO authenticated;
GRANT ALL ON emergency_contacts TO authenticated;

-- ==================== VERIFICATION ====================

-- Check if RLS is enabled (should return true for all tables)
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('user_profiles', 'chat_sessions', 'chat_messages', 'emergency_contacts');

-- List all policies
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual, with_check
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, policyname;
