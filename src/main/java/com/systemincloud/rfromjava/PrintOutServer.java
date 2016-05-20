package com.systemincloud.rfromjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public enum PrintOutServer {
	INSTACE;

	private int MAIN_PORT = 49252;

	private int port = 0;
	public  int getPort() { return port; }

	private ServerSocket serverSocket;

	private Executor acceptor = Executors.newCachedThreadPool();
	private static volatile boolean run = true;

	private class Cmd implements Runnable {
		@Override
		public void run() {
			try {
				Socket client = serverSocket.accept();
				acceptor.execute(new Cmd());
				BufferedReader d = new BufferedReader(new InputStreamReader(client.getInputStream()));
		        while(run) {
		        	String line = d.readLine();
		        	if(line == null) break;
		        	System.out.println(line);
		        }
			} catch (IOException e) { }
		}
	};

	public synchronized void start() {
		port = Util.findFreeLocalPort(MAIN_PORT);
		try {
			serverSocket = new ServerSocket(port);
			acceptor.execute(new Cmd());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void stop() {
		try { serverSocket.close();
		PrintOutServer.run = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
