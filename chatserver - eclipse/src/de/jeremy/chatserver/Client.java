package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	private static final byte BYTECODE_CHANGENAME = 3;
	private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_NAMES = 5;
	private static final byte BYTECODE_NAMESCOUNT = 6;

	// private final String STRING_IPADRESSE = "192.168.0.110";
	private final String STRING_IPADRESSE = "localhost";

	private String name = "peter";

	private Socket client;
	private DataOutputStream out;

	public Client() {
		try {
			setup();
			System.out.println("start");
			boolean done = false;
			Scanner in = new Scanner(System.in);
			while (!done) {

				
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
					name = s;
					out.writeByte(BYTECODE_CHANGENAME);
					out.writeUTF(s);
					out.flush();
				} else if (s.startsWith("/shownames")) {
					out.writeByte(BYTECODE_NAMES);
					out.flush();
				} else if (s.startsWith("/namecount")) {
					out.writeByte(BYTECODE_NAMESCOUNT);
					out.flush();
				} else {
					out.writeByte(BYTECODE_MESSAGE);
					out.writeUTF(s);
					out.flush();
				}

			}
			in.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client();
	}

	public void setup() throws UnknownHostException, IOException {
		client = new Socket(STRING_IPADRESSE, 1234);
		out = new DataOutputStream(client.getOutputStream());
		out.writeByte(BYTECODE_CHANGENAME);
		out.writeUTF(name);
		out.flush();

		new ClientMessageListener(new DataInputStream(client.getInputStream())).start();
	}

}
