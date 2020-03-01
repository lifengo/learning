package com.webserver.core;

import com.webserver.exception.EmptyRequestException;
import com.webserver.http.HTTPResponse;
import com.webserver.http.HttpRequest;
import com.webserver.servlet.*;

import java.io.*;
import java.net.Socket;

/**
 * 客户端处理器，负责处理与客户端的交互工作
 *
 * @Data 2020/2/13 0013
 */
public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            //1、解析请求
            HttpRequest request = new HttpRequest(socket);
            HTTPResponse response = new HTTPResponse(socket);
            //2、处理请求
            //2.1 根据request 获取资源的抽象路径
            String path = request.getRequestURI();
            //2.2 根据请求路径判断是否为请求一个业务操作
            HttpServlet servlet = ServerContext.getServlet(path);
            if (servlet != null) {
                servlet.service(request,response);
            } else {
                //2.3 根据该抽象路径从webapps下寻找该资源
                File file = new File("./WebServer_v19/webapps" + path);
                if (file.exists()) {
                    System.out.println("该资源已找到");
                    //发送一个标准的HTTP响应给客户端回应该资源

                    //将响应的资源设置到response中
                    response.setEntity(file);
                } else {
                    System.out.println("该资源不存在");
                    response.setStatusCode(404);
                    response.setStatusReason("NOT FOUND");
                    response.setEntity(new File("./WebServer_v19/webapps/myweb/404/404.html"));
                }
            }
            //3、响应客户端
            response.flush();
            System.out.println("响应完毕");
        } catch (EmptyRequestException e) {
            System.out.println("空请求..");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /*
            处理完请求并响应客户端后与其断开连接
             */
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
