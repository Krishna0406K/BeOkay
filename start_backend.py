"""
Complete Backend Startup Script
Checks everything and starts the backend
"""

import os
import sys
from dotenv import load_dotenv
from supabase import create_client

# Load environment
load_dotenv()

def print_header(text):
    print("\n" + "=" * 60)
    print(f"  {text}")
    print("=" * 60)

def print_success(text):
    print(f"✅ {text}")

def print_error(text):
    print(f"❌ {text}")

def print_info(text):
    print(f"ℹ️  {text}")

def check_environment():
    """Check if all environment variables are set"""
    print_header("CHECKING ENVIRONMENT VARIABLES")
    
    required_vars = {
        "SUPABASE_URL": os.getenv("SUPABASE_URL"),
        "SUPABASE_KEY": os.getenv("SUPABASE_KEY"),
        "GROQ_API_KEY": os.getenv("GROQ_API_KEY")
    }
    
    all_good = True
    for var_name, var_value in required_vars.items():
        if var_value:
            print_success(f"{var_name} is set")
        else:
            print_error(f"{var_name} is missing!")
            all_good = False
    
    return all_good

def check_supabase():
    """Check if Supabase connection works"""
    print_header("CHECKING SUPABASE CONNECTION")
    
    try:
        supabase_url = os.getenv("SUPABASE_URL")
        supabase_key = os.getenv("SUPABASE_KEY")
        
        client = create_client(supabase_url, supabase_key)
        print_success("Supabase connection successful")
        
        # Check if tables exist
        tables = ["user_profiles", "chat_sessions", "chat_messages", "emergency_contacts"]
        for table in tables:
            try:
                client.table(table).select("*").limit(1).execute()
                print_success(f"Table '{table}' exists")
            except Exception as e:
                print_error(f"Table '{table}' not accessible: {e}")
                return False
        
        return True
    except Exception as e:
        print_error(f"Supabase connection failed: {e}")
        return False

def get_local_ip():
    """Get local IP address"""
    import socket
    try:
        # Create a socket to get local IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except:
        return "localhost"

def start_backend():
    """Start the Flask backend"""
    print_header("STARTING BACKEND SERVER")
    
    local_ip = get_local_ip()
    
    print_info(f"Your local IP: {local_ip}")
    print_info("Backend will be accessible at:")
    print_info(f"  - From this computer: http://localhost:8000")
    print_info(f"  - From Android emulator: http://10.0.2.2:8000")
    print_info(f"  - From physical device: http://{local_ip}:8000")
    print()
    print_info("Starting Flask server...")
    print_info("Press Ctrl+C to stop")
    print()
    
    # Import and run Flask app
    from app_api import app
    app.run(host='0.0.0.0', port=8000, debug=True)

def main():
    print("\n" + "🚀" * 30)
    print("  ManoDost AI - Backend Startup")
    print("🚀" * 30)
    
    # Check environment
    if not check_environment():
        print_error("\nEnvironment variables missing!")
        print_info("Please check your .env file")
        return 1
    
    # Check Supabase
    if not check_supabase():
        print_error("\nSupabase connection failed!")
        print_info("Please check:")
        print_info("1. SUPABASE_URL and SUPABASE_KEY in .env")
        print_info("2. Email provider is enabled in Supabase dashboard")
        print_info("3. Tables are created (run supabase_schema.sql)")
        return 1
    
    print("\n" + "✅" * 30)
    print("  ALL CHECKS PASSED!")
    print("✅" * 30)
    
    # Start backend
    try:
        start_backend()
    except KeyboardInterrupt:
        print("\n\n" + "👋" * 30)
        print("  Backend stopped")
        print("👋" * 30)
        return 0
    except Exception as e:
        print_error(f"\nBackend failed to start: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
