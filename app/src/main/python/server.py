from http.server import SimpleHTTPRequestHandler, HTTPServer
import json

# Path to the JSON file
JSON_FILE = 'data.json'

class Handler(SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/data.json':
            with open(JSON_FILE, 'r') as f:
                self.send_response(200)
                self.send_header('Content-type', 'application/json')
                self.end_headers()
                self.wfile.write(f.read().encode())
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == '/add-device':
            content_length = int(self.headers['Content-Length'])
            body = self.rfile.read(content_length)
            new_device = json.loads(body)

            # Load current data
            with open(JSON_FILE, 'r') as f:
                data = json.load(f)

            # Append the new device
            data.append(new_device)

            # Write it back
            with open(JSON_FILE, 'w') as f:
                json.dump(data, f, indent=4)

            self.send_response(200)
            self.end_headers()
            self.wfile.write(b'{"message": "Device added successfully"}')

    def do_DELETE(self):
        if self.path.startswith('/delete-device'):
            # Parse query like: /delete-device?name=John's PC or /delete-device?IPv4=192.168.1.15
            query = self.path.split('?')[1]
            key, value = query.split('=')

            # Load current data
            with open(JSON_FILE, 'r') as f:
                data = json.load(f)

            # Filter out the device to delete
            new_data = [device for device in data if device.get(key) != value]

            # Write the new data back
            with open(JSON_FILE, 'w') as f:
                json.dump(new_data, f, indent=4)

            self.send_response(200)
            self.end_headers()
            self.wfile.write(b'{"message": "Device deleted successfully"}')

server = HTTPServer(('0.0.0.0', 8080), Handler)
print("Server running at http://localhost:8080")
server.serve_forever()
