import socket
from threading import Thread

ONLINE_CLIENT_MAP = {}

class HandlerClient(Thread):
    def __init__(self, client):
        Thread.__init__(self)
        self.client = client

    def run(self):
        try:
            client_input = self.client.makefile('r')
            while True:
                data = client_input.readline().strip()
                if data.startswith("注册:"):
                    username = data.split(":")[1]
                    self.register(username)
                    continue

                if data.startswith("群聊:"):
                    message = data.split(":")[1]
                    self.group_chat(message)
                    continue

                if data.startswith("私聊:"):
                    segments = data.split(":")[1].split("-")
                    target_username = segments[0]
                    message = segments[1]
                    self.private_chat(target_username, message)
                    continue

                if data == "System.bye":
                    self.bye()
                    break
        except:
            pass
        finally:
            self.client.close()

    def bye(self):
        for username, client_socket in ONLINE_CLIENT_MAP.items():
            if client_socket == self.client:
                del ONLINE_CLIENT_MAP[username]
                break
        print(f"{self.get_current_username()} 退出聊天室")
        self.print_online_clients()

    def get_current_username(self):
        for username, client_socket in ONLINE_CLIENT_MAP.items():
            if client_socket == self.client:
                return username
        return ""

    def private_chat(self, target_username, message):
        target = ONLINE_CLIENT_MAP.get(target_username)
        if target is None:
            self.send_message(self.client, f"没有这个用户 {target_username}", False)
        else:
            self.send_message(target, message, True)

    def group_chat(self, message):
        for client_socket in ONLINE_CLIENT_MAP.values():
            if client_socket != self.client:
                self.send_message(client_socket, message, True)

    def send_message(self, target, message, prefix):
        try:
            target_output = target.makefile('w')
            if prefix:
                current_username = self.get_current_username()
                target_output.write(f"<{current_username}说:>{message}\n")
            else:
                target_output.write(f"{message}\n")
            target_output.flush()
        except:
            pass

    def print_online_clients(self):
        print(f"当前聊天室在线人数: {len(ONLINE_CLIENT_MAP)}, 用户名如下列表:")
        for username in ONLINE_CLIENT_MAP.keys():
            print(username)

    def register(self, username):
        if username in ONLINE_CLIENT_MAP:
            self.send_message(self.client, "您已经注册过了，无需重复注册", False)
        else:
            ONLINE_CLIENT_MAP[username] = self.client
            self.print_online_clients()
            self.send_message(self.client, f"恭喜 {username} 注册成功\n", False)

def main():
    try:
        port = 43819

        server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_socket.bind(('localhost', port))
        server_socket.listen(5)

        print(f"服务器启动.... {server_socket.getsockname()}")

        while True:
            client, address = server_socket.accept()
            print(f"有客户端连接到服务器: {address}")
            handler = HandlerClient(client)
            handler.start()
    except KeyboardInterrupt:
        server_socket.close()

if __name__ == '__main__':
    main()
