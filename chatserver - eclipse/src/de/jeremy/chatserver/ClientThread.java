package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClientThread extends Thread {

	private ChatServer cs;
	private int hashCode;
	private DataInputStream input;
	private DataOutputStream output;
	private String name;

	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	private static final byte BYTECODE_CHANGENAME = 3;
	private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_NAMES = 5;
	private static final byte BYTECODE_NAMESCOUNT = 6;

	public ClientThread(ChatServer cs, int hashCode, InputStream in, OutputStream out) {
		this.cs = cs;
		this.hashCode = hashCode;
		input = new DataInputStream(in);
		output = new DataOutputStream(out);
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
					cs.sendAllNames(hashCode());
					break;
				case BYTECODE_NAMESCOUNT:
					cs.sendNamesCountSingle(hashCode);
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
		System.out.println(name + cs.getInetAddress(hashCode) + " said: " + msg);
		cs.sendMsg(msg, hashCode);
	}

	private void serverMessage() throws IOException {
		String message = input.readUTF();
		System.out.println("Server: " + message);
		cs.serverMessage(message, hashCode, true);
	}

	private void changeName() throws IOException {

		if (name == null) {
			name = input.readUTF();
			System.out.println("client connected: " + name + cs.getInetAddress(hashCode));
			String messsage = name + " joined the chat";
			cs.serverMessage(messsage, hashCode, false);
			cs.addName(name, hashCode);

		} else {
			String oldName = name;
			name = input.readUTF();
			String message = oldName + " changed name to " + name;
			System.out.println("Server: " + message);
			cs.serverMessage(message, hashCode, true);
			cs.changeName(name, hashCode);
		}
	}

	private void ping() throws IOException {
		System.out.println(name + cs.getInetAddress(hashCode) + " checked for connection");
		output.writeByte(BYTECODE_SERVERPING);
		output.flush();
	}

	private void closeConenction() throws IOException {
		System.out.println("Client " + name + cs.getInetAddress(hashCode) + " left");
		String messsage = "Client " + name + " has left the chat.";
		cs.serverMessage(messsage, hashCode, false);
		cs.fixStreams(hashCode);
		input.close();
		output.close();
		cs.removeName(hashCode);
	}

	private void lostConnection() {
		System.out.println("Client " + name + cs.getInetAddress(hashCode) + " lost connection");
		try {
			input.close();
			output.close();
		} catch (IOException e) {
		}
		cs.fixStreams(hashCode);
		String messsage = "Client " + name + " lost connection.";
		cs.serverMessage(messsage, hashCode, false);
		cs.removeName(hashCode);
	}

}
