package de.jeremy.chatserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ChatServer {

	private ServerSocket server;
	HashMap<Integer, Socket> clientMap;
	HashMap<Integer, String> nameMap;

	//private static final byte BYTECODE_CLOSECONNECTION = -1;	
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	//private static final byte BYTECODE_CHANGENAME = 3;
	//private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_NAMES = 5;
	private static final byte BYTECODE_NAMESCOUNT = 6;

	private static final int INT_PORT = 1234;

	public ChatServer() {

		nameMap = new HashMap<>();
		clientMap = new HashMap<>();
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
				clientMap.put(client.hashCode(), client);
				new ClientThread(this, client.hashCode(), client.getInputStream(), client.getOutputStream()).start();

			} catch (IOException e) {
				System.out.println("Client couldnt connect");
			}
		}
	}

	public synchronized void fixStreams(int hashCode) {
		try {
			clientMap.get(hashCode).close();
		} catch (IOException e) {
		}
		clientMap.remove(hashCode);
		System.out.println("fixed streams...");

	}

	public synchronized void addName(String name, int hashCode) {
		nameMap.put(hashCode, name);
		sendNamesCount();
	}

	public synchronized void removeName(int hashCode) {
		nameMap.remove(hashCode);
		sendNamesCount();
	}

	public synchronized void changeName(String name, int hashCode) {
		nameMap.replace(hashCode, name);
	}

	public synchronized void sendNamesCount() {
		for (Socket client : clientMap.values()) {
			try {
				DataOutputStream output = new DataOutputStream(client.getOutputStream());
				output.writeByte(BYTECODE_NAMESCOUNT);
				output.writeInt(nameMap.size());
				output.flush();
			} catch (IOException e) {
			}
		}
	}

	public synchronized void sendNamesCountSingle(int hashCode) {

		String name = nameMap.get(hashCode);
		Socket s = clientMap.get(hashCode);

		System.out.println(name + s.getInetAddress() + " requested user count");
		try {
			DataOutputStream output = new DataOutputStream(s.getOutputStream());
			output.writeByte(BYTECODE_NAMESCOUNT);
			output.writeInt(nameMap.size());
			output.flush();
		} catch (IOException e) {
		}
	}

	public synchronized void sendAllNames(int hashCode) {

		String name = nameMap.get(hashCode);
		Socket s = clientMap.get(hashCode);

		System.out.println(name + s.getInetAddress() + " requested all names");
		StringBuilder sb = new StringBuilder();
		nameMap.forEach((k, v) -> sb.append(v + ";"));
		try {
			DataOutputStream output = new DataOutputStream(s.getOutputStream());
			output.writeByte(BYTECODE_NAMES);
			output.writeUTF(sb.toString());
			output.flush();
		} catch (IOException e) {
		}
	}

	public synchronized void sendMsg(String msg, int hashCode) throws IOException {

		String name = nameMap.get(hashCode);
		Socket s = clientMap.get(hashCode);

		for (Socket client : clientMap.values()) {
			if (client != null && !client.getInetAddress().equals(s.getInetAddress())) {
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

	public synchronized void serverMessage(String message, int hashCode, boolean showSender) {

		Socket s = clientMap.get(hashCode);

		for (Socket client : clientMap.values()) {
			if (client != null) {
				if (!client.getInetAddress().equals(s.getInetAddress()) || showSender) {
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

	public InetAddress getInetAddress(int hashCode) {
		return clientMap.get(hashCode).getInetAddress();
	}
}
