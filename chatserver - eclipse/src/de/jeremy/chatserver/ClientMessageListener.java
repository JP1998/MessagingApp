package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ClientMessageListener extends Thread {

	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	private static final byte BYTECODE_CHANGENAME = 3;
	private static final byte BYTECODE_NAMES = 5;

	private Client client;
	private DataInputStream input;

	public ClientMessageListener(Client client, DataInputStream input) {
		this.client = client;
		this.input = input;
	}

	public void run() {

		boolean done = false;
		while (!done) {
			try {

				switch (input.readByte()) {
				case BYTECODE_MESSAGE:
					String s = input.readUTF() + ": " + input.readUTF();
					System.out.println(s);
					break;
				case BYTECODE_SERVERMESSAGE:
					System.out.println("Server: " + input.readUTF());
					break;
				case BYTECODE_SERVERPING:
					System.out.println("Server noch verbunden");
					break;
				case BYTECODE_CLOSECONNECTION:
					done = true;
					input.close();
					System.exit(1);
					break;
				case BYTECODE_NAMES:
					List<String> list = Arrays.asList(input.readUTF().split(";"));
					list.forEach(System.out::println);
					break;
				default:
					break;
				}

			} catch (IOException e) {
				done = true;
				System.exit(1);
			}

		}
	}

}
