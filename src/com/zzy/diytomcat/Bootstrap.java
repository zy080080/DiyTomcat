package com.zzy.diytomcat;

import com.zzy.diytomcat.catalina.*;

public class Bootstrap {

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
