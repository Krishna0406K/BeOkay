-- TEMPORARY: Disable RLS for Testing
-- Run this in Supabase SQL Editor to disable RLS temporarily
-- WARNING: This makes your database publicly accessible! Only use for testing!

-- Disable RLS on all tables
ALTER TABLE user_profiles DISABLE ROW LEVEL SECURITY;
ALTER TABLE chat_sessions DISABLE ROW LEVEL SECURITY;
ALTER TABLE chat_messages DISABLE ROW LEVEL SECURITY;
ALTER TABLE emergency_contacts DISABLE ROW LEVEL SECURITY;

-- Verify RLS is disabled (should show false)
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('user_profiles', 'chat_sessions', 'chat_messages', 'emergency_contacts');

-- TO RE-ENABLE RLS LATER (IMPORTANT!):
-- ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE chat_sessions ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE emergency_contacts ENABLE ROW LEVEL SECURITY;
