"""
Automated Setup Checker for ManoDost AI Backend
Checks if Supabase is configured correctly
"""

import os
from dotenv import load_dotenv
from supabase import create_client
import sys

load_dotenv()

def print_header(text):
    print("\n" + "=" * 60)
    print(f"  {text}")
    print("=" * 60)

def print_success(text):
    print(f"✅ {text}")

def print_error(text):
    print(f"❌ {text}")

def print_warning(text):
    print(f"⚠️  {text}")

def print_info(text):
    print(f"ℹ️  {text}")

def check_env_variables():
    """Check if .env file has required variables"""
    print_header("CHECKING ENVIRONMENT VARIABLES")
    
    supabase_url = os.getenv("SUPABASE_URL")
    supabase_key = os.getenv("SUPABASE_KEY")
    groq_key = os.getenv("GROQ_API_KEY")
    
    all_good = True
    
    if supabase_url:
        print_success(f"SUPABASE_URL found: {supabase_url}")
    else:
        print_error("SUPABASE_URL not found in .env")
        all_good = False
    
    if supabase_key:
        if supabase_key.startswith("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"):
            print_success(f"SUPABASE_KEY found (JWT format): {supabase_key[:50]}...")
        else:
            print_error("SUPABASE_KEY has wrong format (should start with eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9)")
            all_good = False
    else:
        print_error("SUPABASE_KEY not found in .env")
        all_good = False
    
    if groq_key:
        print_success(f"GROQ_API_KEY found: {groq_key[:20]}...")
    else:
        print_warning("GROQ_API_KEY not found (needed for AI chat)")
    
    return all_good

def check_supabase_connection():
    """Check if Supabase connection works"""
    print_header("CHECKING SUPABASE CONNECTION")
    
    try:
        supabase_url = os.getenv("SUPABASE_URL")
        supabase_key = os.getenv("SUPABASE_KEY")
        
        client = create_client(supabase_url, supabase_key)
        print_success("Supabase client created successfully")
        
        # Try a simple query
        response = client.table("user_profiles").select("*").limit(1).execute()
        print_success(f"Database query successful (found {len(response.data)} records)")
        
        return True
    except Exception as e:
        print_error(f"Connection failed: {e}")
        print_info("Solution: Check SUPABASE_URL and SUPABASE_KEY in .env")
        return False

def check_tables_exist():
    """Check if required tables exist"""
    print_header("CHECKING DATABASE TABLES")
    
    try:
        supabase_url = os.getenv("SUPABASE_URL")
        supabase_key = os.getenv("SUPABASE_KEY")
        client = create_client(supabase_url, supabase_key)
        
        tables = ["user_profiles", "chat_sessions", "chat_messages", "emergency_contacts"]
        all_exist = True
        
        for table in tables:
            try:
                client.table(table).select("*").limit(1).execute()
                print_success(f"Table '{table}' exists")
            except Exception as e:
                print_error(f"Table '{table}' not found or not accessible")
                all_exist = False
        
        if not all_exist:
            print_info("Solution: Run supabase_schema.sql in Supabase SQL editor")
            print_info("URL: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/sql/new")
        
        return all_exist
    except Exception as e:
        print_error(f"Could not check tables: {e}")
        return False

def check_rls_status():
    """Check if RLS is disabled (for testing)"""
    print_header("CHECKING ROW LEVEL SECURITY STATUS")
    
    try:
        supabase_url = os.getenv("SUPABASE_URL")
        supabase_key = os.getenv("SUPABASE_KEY")
        client = create_client(supabase_url, supabase_key)
        
        # Try to query the pg_tables view
        query = """
        SELECT tablename, rowsecurity 
        FROM pg_tables 
        WHERE schemaname = 'public' 
        AND tablename IN ('user_profiles', 'chat_sessions', 'chat_messages', 'emergency_contacts')
        """
        
        # Note: This might not work with anon key, so we'll just give advice
        print_warning("Cannot check RLS status with anon key")
        print_info("If you get 'row level security policy' errors:")
        print_info("1. Go to: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/sql/new")
        print_info("2. Run: ALTER TABLE user_profiles DISABLE ROW LEVEL SECURITY;")
        print_info("3. Repeat for: chat_sessions, chat_messages, emergency_contacts")
        
        return True
    except Exception as e:
        print_warning(f"Could not check RLS: {e}")
        return True

def check_auth_config():
    """Check authentication configuration"""
    print_header("CHECKING AUTHENTICATION CONFIGURATION")
    
    print_warning("Cannot automatically check email confirmation setting")
    print_info("Please manually verify:")
    print_info("1. Go to: https://supabase.com/dashboard/project/rpdaelzwiubhaxvodwxj/auth/providers")
    print_info("2. Click on 'Email' provider")
    print_info("3. Ensure 'Confirm email' is DISABLED (for testing)")
    
    return True

def main():
    print("\n" + "🔍" * 30)
    print("  ManoDost AI - Setup Checker")
    print("🔍" * 30)
    
    checks = [
        ("Environment Variables", check_env_variables),
        ("Supabase Connection", check_supabase_connection),
        ("Database Tables", check_tables_exist),
        ("Row Level Security", check_rls_status),
        ("Authentication Config", check_auth_config),
    ]
    
    results = []
    for name, check_func in checks:
        try:
            result = check_func()
            results.append((name, result))
        except Exception as e:
            print_error(f"Check failed: {e}")
            results.append((name, False))
    
    # Summary
    print_header("SUMMARY")
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        if result:
            print_success(f"{name}: PASSED")
        else:
            print_error(f"{name}: FAILED")
    
    print(f"\n{passed}/{total} checks passed")
    
    if passed == total:
        print("\n" + "🎉" * 30)
        print("  ALL CHECKS PASSED!")
        print("  You can now run: python app_api.py")
        print("🎉" * 30)
        return 0
    else:
        print("\n" + "⚠️ " * 30)
        print("  SOME CHECKS FAILED")
        print("  Please fix the issues above and run this script again")
        print("  For detailed help, see: README_SUPABASE_SETUP.md")
        print("⚠️ " * 30)
        return 1

if __name__ == "__main__":
    sys.exit(main())
