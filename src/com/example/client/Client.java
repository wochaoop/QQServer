package com.example.client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            // 设置目标Python服务器的URL
            URL url = new URL("http://localhost:8000");

            while (true) {
                // 读取用户的键盘输入
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter message: ");
                String input = scanner.nextLine();

                if (input.equals("exit")) {
                    break;
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // 发送数据到Python服务器
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(input);
                writer.flush();

                // 读取Python服务器的响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Response from Python: " + line);
                }

                writer.close();
                reader.close();
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
