import socket
import threading
import json
import time

class QualityTestServer:
    def __init__(self, host='0.0.0.0', port=9999):
        self.host = host
        self.port = port
        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind((self.host, self.port))
        self.server_socket.listen(5)
        self.clients = []
        self.running = False
        
    def start(self):
        self.running = True
        print(f"Server started on {self.host}:{self.port}")
        
        while self.running:
            try:
                client_socket, address = self.server_socket.accept()
                print(f"New connection from {address}")
                client_thread = threading.Thread(target=self.handle_client, args=(client_socket,))
                client_thread.start()
                self.clients.append(client_socket)
            except:
                break
                
    def handle_client(self, client_socket):
        while self.running:
            try:
                data = client_socket.recv(1024)
                if not data:
                    break
                    
                message = data.decode('utf-8')
                print(f"Received: {message}")
                
                # Echo the message back to all clients
                self.broadcast(message)
            except:
                break
                
        self.clients.remove(client_socket)
        client_socket.close()
        
    def broadcast(self, message):
        for client in self.clients:
            try:
                client.send(message.encode('utf-8'))
            except:
                self.clients.remove(client)
                
    def stop(self):
        self.running = False
        for client in self.clients:
            client.close()
        self.server_socket.close()
        
if __name__ == "__main__":
    server = QualityTestServer()
    server.start()
