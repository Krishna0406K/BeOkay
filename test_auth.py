"""
Test authentication endpoints
"""
from supabase_backend import SupabaseBackend
import json
import random
import string

print("=" * 60)
print("  SUPABASE AUTHENTICATION TEST")
print("=" * 60)

sb = SupabaseBackend()

# Generate random email to avoid conflicts
random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))
test_email = f"testuser{random_suffix}@gmail.com"
test_password = "TestPassword123!"

print(f"\nTest Email: {test_email}")
print(f"Test Password: {test_password}")

# Test 1: Sign up
print("\n" + "=" * 60)
print("TEST 1: SIGN UP")
print("=" * 60)

signup_result = sb.sign_up(
    email=test_email,
    password=test_password,
    metadata={"name": "Test User"}
)

if signup_result['success']:
    user_id = signup_result['user'].id
    print(f"✅ Sign up successful!")
    print(f"   User ID: {user_id}")
    print(f"   Email: {signup_result['user'].email}")
    print(f"   Session: {'Active' if signup_result['session'] else 'None (email confirmation required)'}")
    
    # Test 2: Create profile
    print("\n" + "=" * 60)
    print("TEST 2: CREATE USER PROFILE")
    print("=" * 60)
    
    profile_result = sb.create_user_profile(
        user_id=user_id,
        name="Test User",
        phone="1234567890",
        preferred_language="en"
    )
    
    if profile_result['success']:
        print("✅ Profile created successfully!")
        print(f"   Name: Test User")
        print(f"   Phone: 1234567890")
        print(f"   Language: en")
    else:
        print(f"❌ Profile creation failed!")
        print(f"   Error: {profile_result.get('error')}")
        print("\n   SOLUTION:")
        print("   1. Go to: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/sql/new")
        print("   2. Run: ALTER TABLE user_profiles DISABLE ROW LEVEL SECURITY;")
    
    # Test 3: Sign in
    print("\n" + "=" * 60)
    print("TEST 3: SIGN IN")
    print("=" * 60)
    
    signin_result = sb.sign_in(test_email, test_password)
    
    if signin_result['success']:
        print("✅ Sign in successful!")
        print(f"   User ID: {signin_result['user'].id}")
        print(f"   Email: {signin_result['user'].email}")
        print(f"   Session: Active")
    else:
        print(f"❌ Sign in failed!")
        print(f"   Error: {signin_result.get('error')}")
        
        if "Email not confirmed" in str(signin_result.get('error')):
            print("\n   SOLUTION:")
            print("   1. Go to: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/auth/providers")
            print("   2. Click on 'Email' provider")
            print("   3. Disable 'Confirm email' toggle")
            print("   4. Save and run this test again")
    
    # Test 4: Create chat session
    print("\n" + "=" * 60)
    print("TEST 4: CREATE CHAT SESSION")
    print("=" * 60)
    
    session_result = sb.create_chat_session(user_id, "en")
    
    if session_result['success']:
        print("✅ Chat session created!")
        print(f"   Session ID: {session_result['session_id']}")
        print(f"   Language: en")
    else:
        print(f"❌ Chat session creation failed!")
        print(f"   Error: {session_result.get('error')}")
        print("\n   SOLUTION:")
        print("   1. Go to: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/sql/new")
        print("   2. Run: ALTER TABLE chat_sessions DISABLE ROW LEVEL SECURITY;")
    
    print("\n" + "=" * 60)
    print("  TEST SUMMARY")
    print("=" * 60)
    
    all_passed = (
        signup_result['success'] and
        profile_result['success'] and
        signin_result['success'] and
        session_result['success']
    )
    
    if all_passed:
        print("✅ ALL TESTS PASSED!")
        print("\nYour Supabase backend is configured correctly.")
        print("You can now run: python app_api.py")
    else:
        print("⚠️  SOME TESTS FAILED")
        print("\nPlease follow the solutions above to fix the issues.")
        print("Then run this test again: python test_auth.py")
    
else:
    print(f"❌ Sign up failed!")
    print(f"   Error: {signup_result.get('error')}")
    
    if "already registered" in str(signup_result.get('error')).lower():
        print("\n   SOLUTION: Email already exists. Run the test again (it will use a new random email)")
    elif "invalid" in str(signup_result.get('error')).lower():
        print("\n   SOLUTION: Check that Supabase Auth is enabled")
        print("   Go to: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/auth/providers")
    else:
        print("\n   Check your Supabase configuration:")
        print("   1. SUPABASE_URL in .env")
        print("   2. SUPABASE_KEY in .env")
        print("   3. Auth is enabled in Supabase dashboard")

print("\n" + "=" * 60)
