package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {

	private ChatServer cs;
	private Socket client;
	private DataInputStream input;
	private DataOutputStream output;
	private String name;

	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	private static final byte BYTECODE_CHANGENAME = 3;
	private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_NAMES = 5;

	public ClientThread(ChatServer cs, Socket client) {
		this.cs = cs;
		this.client = client;
		try {
			input = new DataInputStream(client.getInputStream());
			output = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		try {
			boolean done = false;
			while (!done) {

				switch (input.readByte()) {

				case BYTECODE_CLOSECONNECTION:
					done = true;
					closeConenction();
					break;
				case BYTECODE_SERVERPING:
					ping();
					break;
				case BYTECODE_MESSAGE:
					userMessage();
					break;
				case BYTECODE_SERVERMESSAGE:
					serverMessage();
					break;
				case BYTECODE_CHANGENAME:
					changeName();
					break;
				case BYTECODE_NAMES:
					cs.sendAllNames();
				default:
					break;
				}
			}

		} catch (IOException e) {
			lostConnection();
		}
	}

	private void userMessage() throws IOException {
		String msg = input.readUTF();
		System.out.println(name + client.getInetAddress() + " said: " + msg);
		cs.sendMsg(name, msg, client);
	}

	private void serverMessage() throws IOException {
		String message = input.readUTF();
		System.out.println("Server: " + message);
		cs.serverMessage(message, client, true);
	}

	private void changeName() throws IOException {

		if (name == null) {
			name = input.readUTF();
			System.out.println("client connected: " + name + client.getInetAddress());
			String messsage = name + " joined the chat";
			cs.serverMessage(messsage, client, false);

			cs.addName(name);

		} else {
			String oldName = name;
			name = input.readUTF();
			String message = oldName + " changed name to " + name;
			System.out.println("Server: " + message);
			cs.serverMessage(message, client, true);

			cs.changeName(oldName, name);
		}

	}

	private void ping() throws IOException {
		System.out.println(name + client.getInetAddress() + " checked for connection");
		output.writeByte(BYTECODE_SERVERPING);
		output.flush();
	}

	private void closeConenction() throws IOException {
		System.out.println("Client " + name + client.getInetAddress() + " left");
		String messsage = "Client " + name + " has left the chat.";
		cs.serverMessage(messsage, client, false);
		cs.fixStreams(client);
		input.close();
		output.close();
		client.close();
		cs.removeName(name);
	}

	private void lostConnection() {
		System.out.println("Client " + name + client.getInetAddress() + " lost connection");
		try {
			input.close();
			output.close();
			client.close();
		} catch (IOException e) {
		}
		cs.fixStreams(client);
		String messsage = "Client " + name + " lost connection.";
		cs.serverMessage(messsage, client, false);
		cs.removeName(name);
	}

}
