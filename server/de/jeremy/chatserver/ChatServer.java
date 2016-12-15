package de.jeremy.chatserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ChatServer {
  
  
  private ServerSocket server;
  private ArrayList<Socket> clients;
  
  private static final byte BYTECODE_MESSAGE = 1;
  private static final byte BYTECODE_SERVERMESSAGE = 99;
  private static final byte BYTECODE_CLOSECONNECTION = -1;
  private static final byte BYTECODE_SERVERPING = 100;
  
  private static final int INT_PORT = 1234;
  
  
  public ChatServer() {
    clients = new ArrayList<>();
    System.out.println("Server started");
    
    try {
      server = new ServerSocket(INT_PORT);
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
	  
    for(Socket client: clients){
      if(client != null && !client.getInetAddress().equals(sender.getInetAddress())){
        DataOutputStream output =  new DataOutputStream(client.getOutputStream());
        
        try {
          output.writeByte(BYTECODE_MESSAGE);
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
  
  public synchronized void serverMessage(String message, Socket sender){
    for(Socket client: clients){
      if(client != null && !client.getInetAddress().equals(sender.getInetAddress())){
        try {
          DataOutputStream output =  new DataOutputStream(client.getOutputStream());
          output.writeByte(BYTECODE_SERVERMESSAGE);
          output.writeByte(BYTECODE_MESSAGE);
          output.writeUTF(message);
          output.flush();
        } catch (IOException e) {
          System.out.println("couldnt send message");
        }
      }
    }
  }
}
