package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	private static final byte BYTECODE_CHANGENAME = 3;
	private static final byte BYTECODE_NAMES = 5;

	private Socket client;
	private DataOutputStream out;

	public Client() {
		try {
			setup();
			System.out.println("start");
			boolean done = false;
			while (!done) {

				Scanner in = new Scanner(System.in);
				String s = in.nextLine();

				if (s.equals("servercheck")) {
					out.writeByte(BYTECODE_SERVERPING);
					out.flush();
				} else if (s.equals("exit")) {
					done = true;
					out.writeByte(BYTECODE_CLOSECONNECTION);
					out.close();
					client.close();
					System.exit(1);
				} else if (s.startsWith("/s ")) {
					s = s.substring(3, s.length());
					out.writeInt(BYTECODE_SERVERMESSAGE);
					out.writeUTF(s);
					out.flush();
				} else if (s.startsWith("/name ")) {
					s = s.substring(6, s.length());
					out.writeByte(BYTECODE_CHANGENAME);
					out.writeUTF(s);
					out.flush();
				} else if (s.startsWith("/shownames")) {
					out.writeByte(BYTECODE_NAMES);
					out.flush();
				} else {
					out.writeByte(BYTECODE_MESSAGE);
					out.writeUTF(s);
					out.flush();
				}

			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client();
	}

	public void setup() throws UnknownHostException, IOException {
		client = new Socket("localhost", 1234);
		out = new DataOutputStream(client.getOutputStream());
		out.writeByte(BYTECODE_CHANGENAME);
		out.writeUTF("Jeremy");
		out.flush();

		new ClientMessageListener(this, new DataInputStream(client.getInputStream())).start();
	}

}
