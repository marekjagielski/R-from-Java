package com.systemincloud.rfromjava;

import java.io.IOException;
import java.net.ServerSocket;

public class Util {

    public static int findFreeLocalPort(int startPort) {
        int port = 0;
        int lastPort = startPort;
        while(port == 0) {
            lastPort++;
            ServerSocket socket = null;
            try {
                socket = new ServerSocket(lastPort);
            } catch (IOException e) { continue;
            } finally {
                if (socket != null)
                    try { socket.close();
                    } catch (IOException e) { /* e.printStackTrace(); */ }
            }
            port = lastPort;
        }
        return port;
    }

}
