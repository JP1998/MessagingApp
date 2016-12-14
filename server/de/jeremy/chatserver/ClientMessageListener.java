package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientMessageListener extends Thread {

	
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
					case 1:
						System.out.println(input.readUTF());
						break;
					case 99:
						switch(input.readByte()){
						case 100:
							System.out.println("Server noch verbunden");
							client.setConnected();
							break;
						default:
							break;
						}
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
