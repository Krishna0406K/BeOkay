"""
Test Supabase connection
"""
from supabase import create_client
import os
from dotenv import load_dotenv

load_dotenv()

supabase_url = os.getenv("SUPABASE_URL")
supabase_key = os.getenv("SUPABASE_KEY")

print(f"URL: {supabase_url}")
print(f"Key (first 50 chars): {supabase_key[:50] if supabase_key else 'None'}...")

try:
    client = create_client(supabase_url, supabase_key)
    print("✅ Supabase connection successful!")
    
    # Test a simple query
    response = client.table("user_profiles").select("*").limit(1).execute()
    print(f"✅ Database query successful! Found {len(response.data)} records")
    
except Exception as e:
    print(f"❌ Error: {e}")
    print("\nPlease check:")
    print("1. SUPABASE_URL is correct")
    print("2. SUPABASE_KEY is the 'anon' public key from Supabase dashboard")
    print("3. Database tables are created (run supabase_schema.sql)")
