package com.example.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class WriteDateToServerThread extends Thread{
    private final Socket client;
    public WriteDateToServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            OutputStream clientOutput = this.client.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("请输入>>");
                String data = scanner.nextLine();
                writer.write(data + "\n");
                writer.flush();
                if (data.equals("System.bye")) {
                    System.out.print("您已经离开聊天室....");
                    break;
                }
            }
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
