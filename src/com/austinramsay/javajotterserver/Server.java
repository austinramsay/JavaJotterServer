package com.austinramsay.javajotterserver;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Collections;
import java.util.Properties;

public class Server {

	public static String dbUrl;
	private ServerNetworkManager netMgr;
	private DatabaseManager dbMgr;
	private List<ClientManager> clients;
	private HashSet<String> connectedUsernames;

	public Server(String dbUrl) {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (Exception e) {
			System.out.println("Could not load Derby EmbeddedDriver. Exiting..");
			System.exit(-1);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					Properties props = new Properties();
					props.put("user", "javajotter");
					props.put("password", "javajotter");
					props.put("shutdown", "true");
					java.sql.DriverManager.getConnection(Server.dbUrl, props);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));
		connectedUsernames = new HashSet<String>();
		Server.dbUrl = dbUrl;
		System.out.println("Welcome to JavaJotter Server!\n");
		System.out.println("Starting network manager..");
		netMgr = new ServerNetworkManager(this);
		System.out.println();
		System.out.println("Waiting for connections...\n");
		// Thread-safe list of connected clients
		clients = Collections.synchronizedList(new ArrayList());
	}

	public synchronized void addClient(ClientManager client) {
		clients.add(client);
	}

	public synchronized void removeClient(ClientManager client) {
		clients.remove(client);
	}

	public synchronized void registerSession(String username) {
		connectedUsernames.add(username);
	}

	public synchronized void removeSession(String username) {
		connectedUsernames.remove(username);
	}

	public synchronized boolean userIsLoggedIn(String username) {
		return connectedUsernames.contains(username);
	}
}