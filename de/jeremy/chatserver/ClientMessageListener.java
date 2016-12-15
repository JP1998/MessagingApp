package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientMessageListener extends Thread {

	
	private static final byte BYTECODE_CLOSECONNECTION = -1;
	private static final byte BYTECODE_SERVERPING = 4;
	private static final byte BYTECODE_MESSAGE = 1;
	private static final byte BYTECODE_SERVERMESSAGE = 2;
	private static final byte BYTECODE_CHANGENAME = 3;
	
	private Client client;
	private DataInputStream input;
	
	public ClientMessageListener(Client client, DataInputStream input) {
		this.client = client;
		this.input = input;
	}

	
	public void run(){
		
		boolean done = false;
		while(!done){
			try {
				
				switch(input.readByte()){
					case BYTECODE_MESSAGE:
						System.out.println(input.readUTF());
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
