package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {
        try {
            int port = 43819;

            ServerSocket serverSocket = new ServerSocket(port);//绑定服务器端口

            System.out.println("服务器启动...." + serverSocket.getLocalSocketAddress());//服务器启动，打印本地地址

            //线程池
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            /*
                频繁销毁创建线程会占用大量的系统资源
                ExecutorService是一个线程池，请求到达时，线程已经存在，响应延迟低，多个任务复用线程，避免了线程的重复创建和销毁
                并且可以规定线程数目，请求数目超过阈值时强制其等待直到有空闲线程
             */

            while (true) {
                Socket client = serverSocket.accept();//接受客户端的连接请求
                System.out.println("有客户端连接到服务器:" + client.getRemoteSocketAddress());//发送和接收数据报包的套接字
                executorService.execute(new HandlerClient(client));//用于执行返回多个结果集、多个更新计数或二者组合的语句
            }
        } catch (IOException e) {
            e.printStackTrace();//在命令行打印异常信息在程序中出错的位置及原因
        }
    }
}
