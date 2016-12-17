package de.jeremy.chatserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

	private ServerSocket server;
	private ArrayList<Socket> clients;
	private ArrayList<String> names;

	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	private static final byte BYTECODE_CHANGENAME = 3;
	private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_NAMES = 5;
	private static final byte BYTECODE_NAMESCOUNT = 6;

	private static final int INT_PORT = 1234;

	public ChatServer() {
		clients = new ArrayList<>();
		names = new ArrayList<>();
		System.out.println("Server started");

		try {
			server = new ServerSocket(INT_PORT);
			waitForConnection();
		} catch (IOException e) {
			System.out.println("failed to initialize server");
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new ChatServer();
	}

	public void waitForConnection() {

		while (true) {
			try {
				System.out.println("waiting for clients...");
				Socket client = server.accept();
				clients.add(client);
				new ClientThread(this, client).start();

			} catch (IOException e) {
				System.out.println("Client couldnt connect");
			}
		}
	}

	public synchronized void fixStreams(Socket sender) {
		for (int i = 0; i < clients.size(); i++) {

			if (clients.get(i) == null || clients.get(i).getInetAddress().equals(sender.getInetAddress())) {
				clients.remove(i);
				System.out.println("fixed streams...");
			}
		}
	}

	public synchronized void addName(String name) {
		names.add(name);
		sendNamesCount();
	}

	public synchronized void removeName(String name) {
		names.remove(name);
		sendNamesCount();
	}

	public synchronized void changeName(String oldName, String newName) {

		names.replaceAll((t) -> {
			if (t.equals(oldName)) {
				t = newName;
			}
			return t;
		});
	}

	public synchronized void sendNamesCount() {
		for (Socket client : clients) {
			try {
				DataOutputStream output = new DataOutputStream(client.getOutputStream());
				output.writeByte(BYTECODE_NAMESCOUNT);
				output.writeInt(names.size());
				output.flush();
			} catch (IOException e) {
			}
		}
	}

	public synchronized void sendNamesCountSingle(String userName, Socket client){
		
		System.out.println(userName + client.getInetAddress() + " requested user count");

		
		try {
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			output.writeByte(BYTECODE_NAMESCOUNT);
			output.writeInt(names.size());
			output.flush();
		} catch (IOException e) {
		}
	}
	
	public synchronized void sendAllNames(String userName, Socket client) {

		System.out.println(userName + client.getInetAddress() + " requested all names");

		StringBuilder sb = new StringBuilder();
		names.forEach((s) -> sb.append(s + ";"));
		try {
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			output.writeByte(BYTECODE_NAMES);
			output.writeUTF(sb.toString());
			output.flush();
		} catch (IOException e) {
		}
	}

	public synchronized void sendMsg(String name, String msg, Socket sender) throws IOException {

		for (Socket client : clients) {
			if (client != null && !client.getInetAddress().equals(sender.getInetAddress())) {
				DataOutputStream output = new DataOutputStream(client.getOutputStream());

				try {
					output.writeByte(BYTECODE_MESSAGE);
					output.writeUTF(name);
					output.writeUTF(msg);
					output.flush();
				} catch (IOException e) {

				}
			}
		}
	}

	public synchronized void serverMessage(String message, Socket sender, boolean showSender) {
		for (Socket client : clients) {
			if (client != null) {
				if (!client.getInetAddress().equals(sender.getInetAddress()) || showSender) {
					try {
						DataOutputStream output = new DataOutputStream(client.getOutputStream());
						output.writeByte(BYTECODE_SERVERMESSAGE);
						output.writeUTF(message);
						output.flush();
					} catch (IOException e) {
						System.out.println("couldnt send message");
					}
				}
			}
		}
	}
}
