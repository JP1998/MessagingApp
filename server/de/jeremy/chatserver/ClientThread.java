package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {

	private ChatServer cs;
	private Socket client;
	DataInputStream input;
	DataOutputStream output;
	
	public ClientThread(ChatServer cs, Socket client) {
		this.cs = cs;
		this.client = client;
		try {
			input = new DataInputStream(client.getInputStream());
			output = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run(){
		System.out.println("client connected: " + client.getInetAddress());
		
		try {
			boolean done = false;
			while(!done){
				
				switch(input.readByte()){
				
				case 1:	//textnachrichten von user
					String msg = input.readUTF();
					System.out.println("" + client.getInetAddress() +" said: " + msg);
					cs.sendMsg(msg, client);
					break;
					
				case 99: //serverinterners
					switch(input.readByte()){
					case 100: //ping ob noch verbunden
						System.out.println(client.getInetAddress() + " checked for connection");
						output.writeByte(99);
						output.writeByte(100);
						output.flush();
						break;
					default:
						break;
					}
					break;
					
				case -1:
					
					done = true;
					System.out.println("Client " + client.getInetAddress() + " left" );
					cs.sendMsg("Client " + client.getInetAddress() + " left" , client);
					cs.fixStreams(client);
					try {
						input.close();
						output.close();
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					
				default:
					break;
				}
			}
			
			
			
		} catch (IOException e) {
			System.out.println("Client " + client.getInetAddress() + " disconnected" );
			try {
				cs.fixStreams(client);
				cs.sendMsg("Client " + client.getInetAddress() + " disconnected" , client);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
	}

}
