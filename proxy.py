"""
极简 HTTP 转发器 — 让 Android 模拟器能访问 TMDB API
启动: python proxy.py
原理: 模拟器 → 10.0.2.2:8888 → 转发到 api.themoviedb.org
"""
from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.request
import urllib.error

TARGET = "https://api.themoviedb.org/3"

class Proxy(BaseHTTPRequestHandler):
    def do_GET(self):
        url = f"{TARGET}{self.path}"
        # 保留原始请求头（主要是 Authorization）
        headers = {}
        for key in ("Authorization", "Accept", "Accept-Language"):
            val = self.headers.get(key)
            if val:
                headers[key] = val
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=15) as resp:
                self.send_response(resp.status)
                self.send_header("Content-Type", resp.headers.get("Content-Type", "application/json"))
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(resp.read())
        except urllib.error.HTTPError as e:
            self.send_response(e.code)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(e.read())
        except Exception as e:
            self.send_response(502)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(f'{{"error":"{e}"}}'.encode())

    def log_message(self, fmt, *args):
        print(f"[转发] {args[0]}")

if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", 8888), Proxy)
    print("转发器启动: http://0.0.0.0:8888 → TMDB API")
    print("Android 模拟器访问 http://10.0.2.2:8888 即可")
    server.serve_forever()
