import json
import asyncio
import re
import threading
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from telethon import TelegramClient, events

# --- CONFIGURATION ---
API_ID = 'YOUR_API_ID'
API_HASH = 'YOUR_API_HASH'
BOT_USERNAME = '@SomeBotUsername'
HTTP_PORT = 9999
JSON_PATH = 'Rockstar/events.json'
# ---------------------

cached_events = []

class JSONServer(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/events.json':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # Filter out expired events before serving
            current_time = time.time()
            active_events = [e for e in cached_events if e.get('end_time', 0) > current_time or e.get('end_time', 0) == -1]
            
            self.wfile.write(json.dumps(active_events, ensure_ascii=False).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

def start_server():
    server = HTTPServer(('0.0.0.0', HTTP_PORT), JSONServer)
    print(f"HTTP Server started on port {HTTP_PORT}")
    server.serve_forever()

def parse_duration(duration_str):
    # Parse strings like "2 мин 49 сек", "27 сек", "1 мин 4 сек"
    total_seconds = 0
    min_match = re.search(r'(\d+)\s*мин', duration_str)
    sec_match = re.search(r'(\d+)\s*сек', duration_str)
    
    if min_match:
        total_seconds += int(min_match.group(1)) * 60
    if sec_match:
        total_seconds += int(sec_match.group(1))
        
    return total_seconds

def parse_events(text):
    sections = re.split(r'Анархия (\d+):', text)
    if len(sections) < 2:
        return []
    
    parsed_events = []
    current_time = time.time()
    
    for i in range(1, len(sections), 2):
        anarchy = sections[i]
        content = sections[i+1].strip()
        
        event_data = {
            "anarchy": anarchy,
            "name": "Unknown",
            "status": "Unknown",
            "loot": "None",
            "coords": None,
            "warp": None,
            "is_legendary": False,
            "end_time": -1 # -1 means no timer known
        }
        
        lines = content.split('\n')
        if not lines: continue
        
        first_line = lines[0]
        name_match = re.search(r'\]\s+(.*)', first_line)
        if name_match:
            event_data["name"] = name_match.group(1).strip()
            
        if "Легендарный" in content:
            event_data["is_legendary"] = True
            
        for line in lines:
            line = line.strip()
            if "Уровень лута:" in line:
                event_data["loot"] = line.replace("Уровень лута:", "").strip()
            elif "Статус:" in line:
                status_text = line.replace("Статус:", "").strip()
                event_data["status"] = status_text
                # Look for timer in status: "до деактивации: 27 сек"
                timer_match = re.search(r':\s*(.*сек.*)', status_text)
                if timer_match:
                    duration = parse_duration(timer_match.group(1))
                    if duration > 0:
                        event_data["end_time"] = current_time + duration
            elif "Координаты:" in line:
                event_data["coords"] = line.replace("Координаты:", "").strip()
            elif "Локация:" in line:
                event_data["warp"] = line.replace("Локация:", "").strip()
            elif "До следующего ивента:" in line:
                event_data["name"] = "Next Event"
                event_data["status"] = line.strip()
                duration = parse_duration(line)
                if duration > 0:
                    event_data["end_time"] = current_time + duration
        
        parsed_events.append(event_data)
    
    parsed_events.sort(key=lambda x: x['is_legendary'], reverse=True)
    return parsed_events

async def main():
    global cached_events
    
    threading.Thread(target=start_server, daemon=True).start()
    
    async with TelegramClient('rockstar_session', API_ID, API_HASH) as client:
        print("Connected to Telegram.")
        
        async def update_data():
            global cached_events
            print("Fetching events from bot...")
            await client.send_message(BOT_USERNAME, '/events')
            
            try:
                async with client.conversation(BOT_USERNAME) as conv:
                    response = await conv.get_response()
                    cached_events = parse_events(response.text)
                    
                    # Persistence
                    try:
                        import os
                        os.makedirs(os.path.dirname(JSON_PATH), exist_ok=True)
                        with open(JSON_PATH, 'w', encoding='utf-8') as f:
                            json.dump(cached_events, f, ensure_ascii=False, indent=4)
                    except: pass
                    
                    print(f"Updated {len(cached_events)} events.")
            except Exception as e:
                print(f"Error updating: {e}")

        await update_data()
        
        while True:
            await asyncio.sleep(30)
            await update_data()

if __name__ == '__main__':
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
