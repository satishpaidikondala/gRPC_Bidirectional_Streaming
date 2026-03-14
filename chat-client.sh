#!/bin/bash

# Make sure the script stops if any command fails
set -e

echo "--- gRPC Chat Client Test Script ---"

# 1. List all available services to verify the server is up
echo "Listing services..."
grpcurl -plaintext localhost:9090 list

# 2. Describe the ChatService to see the methods
echo "Describing ChatService..."
grpcurl -plaintext localhost:9090 describe chat.ChatService

# 3. Test: Get Room Users (Unary RPC)
echo "Fetching users in room 'general'..."
grpcurl -plaintext -d '{"room_id": "general"}' \
    localhost:9090 chat.ChatService/GetRoomUsers

# 4. Test: Join Room and Stream (Bidirectional)
# Note: This opens an interactive session. You can type JSON messages.
echo "Joining 'general' room. Type your message JSON and press enter."
echo "Example: {\"room_id\": \"general\", \"user_id\": \"user1\", \"username\": \"Satish\", \"content\": \"Hello!\", \"type\": \"TEXT\"}"

grpcurl -plaintext -d @ localhost:9090 chat.ChatService/ChatStream