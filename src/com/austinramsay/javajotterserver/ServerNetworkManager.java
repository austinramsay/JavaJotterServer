package com.austinramsay.javajotterserver;

import java.net.Socket;
import java.net.ServerSocket;

public class ServerNetworkManager {

	public ServerNetworkManager(Server server) {
		new Thread(new Listener(server)).start();
	}
}

class Listener implements Runnable {

	private Server server;
	private final int serverPort = 7800;
	private ServerSocket serverConn;


	public Listener(Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		try {
			serverConn = new ServerSocket(serverPort);

			// Wait for client connections
			// When a client connects, start a new thread to handle their requests
			// and add them to the thread-safe client list.
			while (true) {
				Socket clientConn = serverConn.accept();

				ClientManager cm = new ClientManager(server, clientConn);
				server.addClient(cm);
				new Thread(cm).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


