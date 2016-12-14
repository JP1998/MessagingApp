package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	
	private boolean connected;
	private Socket client;
	private DataOutputStream out;
	
	public Client() {
		try {
			setup();
			connected = true;
			System.out.println("start");
			boolean done = false;
			while(!done){
				
				Scanner in = new Scanner(System.in);
				String s = in.nextLine();
				
				if(s.equals("servercheck")){
					connected = false;
					out.writeByte(99);
					out.writeByte(100);
					out.flush();
				}else if(s.equals("exit")){
					done = true;
					out.writeByte(-1);
					out.close();
					client.close();
					System.exit(1);
				}else{
					out.writeByte(1);
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
	
	public void setup() throws UnknownHostException, IOException{
		client = new Socket("192.168.0.110", 1234);
	    out = new DataOutputStream(client.getOutputStream());
		
		new ClientMessageListener(this, new DataInputStream(client.getInputStream())).start();
	}
	
	public void setConnected(){
		connected = true;
	}

}
