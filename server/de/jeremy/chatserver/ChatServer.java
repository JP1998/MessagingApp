package de.jeremy.chatserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

	
	private ServerSocket server;
	private ArrayList<Socket> clients;
	
	
	public ChatServer() {
		clients = new ArrayList<>();
		System.out.println("Server started");
				
		try {
			server = new ServerSocket(1234);
			
			waitForConnection();
			
		} catch (IOException e) {
			System.out.println("failed to initialize server");
		}finally{
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new ChatServer();
	}

	public synchronized void sendMsg(String msg, Socket sender) throws IOException{
		for(int i = 0; i < clients.size(); i++){
			
			if(clients.get(i) != null && !clients.get(i).getInetAddress().equals(sender.getInetAddress())){
				DataOutputStream output =  new DataOutputStream(clients.get(i).getOutputStream());
			
				try {
					output.writeByte(1);
					output.writeUTF(msg);
					output.flush();
				} catch (IOException e) {
					
				}
			}
		}
	}
	
	public void waitForConnection(){
		
		while(true){
			try {
				System.out.println("waiting for clients...");
				Socket client = server.accept();
				clients.add(client);
				new ClientThread(this, client).start();
				
			} catch (IOException e) {
				System.out.println("Client couldnt connect");
			}
		}
		
		
	}
	
	public synchronized void fixStreams(Socket sender){
		for(int i = 0; i < clients.size(); i++){
			
			if(clients.get(i) == null || clients.get(i).getInetAddress().equals(sender.getInetAddress())){
					clients.remove(i);
					System.out.println("fixed streams...");
			}
		}
	}
	
}
