package com.example.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerClient implements Runnable{

    private static final Map<String, Socket> ONLINE_CLIENT_MAP =
            new ConcurrentHashMap<String, Socket>();

    private final Socket client;
    public HandlerClient(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            InputStream clientInput = client.getInputStream();
            Scanner scanner = new Scanner(clientInput);

            while (true) {
                String data = scanner.nextLine();
                if (data.startsWith("注册:")) {
                    String userName = data.split(":")[1];
                    register(userName);
                    continue;
                }

                if (data.startsWith("群聊:")) {
                    String message = data.split(":")[1];
                    groupChat(message);
                    continue;
                }

                if (data.startsWith("私聊:")) {
                    String [] segments = data.split(":");
                    String targetUserName = segments[1].split("\\-")[0];
                    String message = segments[1].split("\\-")[1];
                    privateChat(targetUserName, message);
                    continue;
                }

                if (data.equals("System.bye")) {
                    bye();
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void bye() {
        for (Map.Entry<String, Socket> entry : ONLINE_CLIENT_MAP.entrySet()) {
            Socket target = entry.getValue();
            if (target.equals(this.client)) {
                ONLINE_CLIENT_MAP.remove(entry.getKey());
                break;
            }
            System.out.println(getCurrentUserName() + "推出聊天室");
        }
        printOnlineClient();
    }

    private String getCurrentUserName() {
        for (Map.Entry<String, Socket> entry : ONLINE_CLIENT_MAP.entrySet()) {
            Socket target = entry.getValue();
            if (target.equals(this.client)) {
                return entry.getKey();
            }
        }
        return "";
    }

    private void privateChat(String targetUserName, String message) {
        Socket target = ONLINE_CLIENT_MAP.get(targetUserName);
        if (target == null) {
            this.sendMessage(this.client, "没有这个用户" + targetUserName, false);
        } else {
            this.sendMessage(target, message, true);
        }
    }

    private void groupChat(String message) {
        for (Map.Entry<String, Socket> entry: ONLINE_CLIENT_MAP.entrySet()) {
            Socket target = entry.getValue();
            if (target.equals(this.client)) {
                continue;
            }
            this.sendMessage(target, message, true);
        }
    }

    private void sendMessage(Socket target, String message, boolean prefix) {
        OutputStream clientOutput = null;
        try {
            clientOutput = target.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            if (prefix) {
                String currentUserName = this.getCurrentUserName();
                writer.write("<" + currentUserName + "说:>" + message + "\n");
            } else {
                writer.write(message + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printOnlineClient() {
        System.out.println("当前聊天室在线人数:" + ONLINE_CLIENT_MAP.size() + "," + "用户名如下列表:");
        for (String userName : ONLINE_CLIENT_MAP.keySet()) {
            System.out.println(userName);
        }
    }


    private void register(String userName) {
        if (ONLINE_CLIENT_MAP.containsKey(userName)) {
            this.sendMessage(this.client, "您已经注册过了，无需重复注册", false);
        } else {
            ONLINE_CLIENT_MAP.put(userName, this.client);
            printOnlineClient();
            this.sendMessage(this.client, "恭喜" + userName + "注册成功\n", false);
        }
    }
}
