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

	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 99;
	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_SERVERPING = 100;

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
		System.out.println("client connected: " + name + client.getInetAddress());

		String messsage = name + " joined the chat";
		cs.serverMessage(messsage, client);

		try {
			boolean done = false;
			while (!done) {

				switch (input.readByte()) {

				case BYTECODE_MESSAGE: // textnachrichten von user
					userMessage();
					break;

				case BYTECODE_SERVERMESSAGE: // serverinterners
					switch (input.readByte()) {

					case BYTECODE_SERVERMESSAGE: // ping ob noch verbunden
						ping();
						break;

					case BYTECODE_MESSAGE: // server messages
						serverMessage();

					default:
					}
					break;

				case BYTECODE_CLOSECONNECTION:
					done = true;
					closeConenction();
					break;

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
		System.out.println("" + client.getInetAddress() + " said: " + msg);
		cs.sendMsg(msg, client);
	}

	private void serverMessage() throws IOException {
		String oldName = name;
		name = input.readUTF();
		String message = oldName + " changed name to " + name;
		cs.serverMessage(message, client);
	}

	private void ping() throws IOException {
		System.out.println(client.getInetAddress() + " checked for connection");
		output.writeByte(BYTECODE_SERVERMESSAGE);
		output.writeByte(BYTECODE_SERVERPING);
		output.flush();
	}

	private void closeConenction() throws IOException {
		System.out.println("Client " + name + client.getInetAddress() + " left");
		String messsage = "Client " + name + " has left the chat.";
		cs.serverMessage(messsage, client);
		cs.fixStreams(client);
		input.close();
		output.close();
		client.close();

	}

	private void lostConnection() {
		System.out.println("Client " + name + client.getInetAddress() + " lost connection");
		try {
			input.close();
			output.close();
			client.close();
		} catch (IOException e) {}
		cs.fixStreams(client);
		String messsage = "Client " + name + " lost connection.";
		cs.serverMessage(messsage, client);
	}

}
