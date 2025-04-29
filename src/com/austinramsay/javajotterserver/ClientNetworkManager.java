package com.austinramsay.javajotterserver;

import java.net.Socket;
import java.net.SocketException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.EOFException;

public class ClientNetworkManager {

	private ClientManager cm;
	private Socket clientConn;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ClientNetworkManager(ClientManager cm, Socket clientConn) {
		this.cm = cm;
		this.clientConn = clientConn;

		try {
			in = new ObjectInputStream(clientConn.getInputStream());
			out = new ObjectOutputStream(clientConn.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void receive() {
		try {
			while (true) {
				Object receivedObj = in.readObject();
				cm.process(receivedObj);
			}
		} catch (EOFException e) {
			System.out.println("User \'" + cm.getUsername() + "\' has disconnected.");
			cm.disconnect();
		} catch (SocketException e) {
			// Server may have forced client to disconnect
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(Object obj) {
		try {
			out.writeObject(obj);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			in.close();
			out.close();
			clientConn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}