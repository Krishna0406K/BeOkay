#!/bin/bash

# ManoDost AI - Quick Start Script
# This script helps you set up the development environment quickly

echo "=================================="
echo "ManoDost AI - Quick Start Setup"
echo "=================================="
echo ""

# Check Python version
echo "Checking Python version..."
python_version=$(python3 --version 2>&1 | awk '{print $2}')
echo "Found Python $python_version"

if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is not installed. Please install Python 3.8 or higher."
    exit 1
fi

echo "✅ Python is installed"
echo ""

# Create virtual environment
echo "Creating virtual environment..."
if [ ! -d "venv" ]; then
    python3 -m venv venv
    echo "✅ Virtual environment created"
else
    echo "ℹ️  Virtual environment already exists"
fi
echo ""

# Activate virtual environment
echo "Activating virtual environment..."
source venv/bin/activate
echo "✅ Virtual environment activated"
echo ""

# Install dependencies
echo "Installing Python dependencies..."
pip install --upgrade pip
pip install -r requirements.txt
echo "✅ Dependencies installed"
echo ""

# Check for .env file
echo "Checking environment configuration..."
if [ ! -f ".env" ]; then
    echo "⚠️  .env file not found"
    echo "Creating .env from template..."
    cp .env.example .env
    echo "✅ .env file created"
    echo ""
    echo "⚠️  IMPORTANT: Please edit .env file and add your API keys:"
    echo "   - GROQ_API_KEY"
    echo "   - SUPABASE_URL"
    echo "   - SUPABASE_KEY"
    echo ""
    read -p "Press Enter after you've updated the .env file..."
else
    echo "✅ .env file exists"
fi
echo ""

# Test Supabase connection
echo "Testing Supabase connection..."
python3 -c "from supabase_backend import SupabaseBackend; SupabaseBackend(); print('✅ Supabase connection successful')" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "❌ Supabase connection failed. Please check your credentials in .env"
    echo ""
else
    echo "✅ Supabase connection successful"
    echo ""
fi

# Ask if user wants to start the API
echo "=================================="
echo "Setup Complete!"
echo "=================================="
echo ""
echo "What would you like to do?"
echo "1. Start Flask API server"
echo "2. Start terminal chat agent"
echo "3. Exit"
echo ""
read -p "Enter your choice (1-3): " choice

case $choice in
    1)
        echo ""
        echo "Starting Flask API server..."
        echo "API will be available at: http://localhost:8000"
        echo "Press Ctrl+C to stop"
        echo ""
        python3 app_api.py
        ;;
    2)
        echo ""
        echo "Starting terminal chat agent..."
        echo "Press Ctrl+C to exit"
        echo ""
        python3 agent.py
        ;;
    3)
        echo "Goodbye!"
        exit 0
        ;;
    *)
        echo "Invalid choice. Exiting."
        exit 1
        ;;
esac
