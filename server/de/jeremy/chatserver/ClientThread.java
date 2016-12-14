package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends Thread {
  
  private ChatServer cs;
  private Socket client;
  private DataInputStream input;
  private DataOutputStream output;
  private String name;
  
  private static final byte BYTECODE_MESSAGE = 1;
  private static final byte BYTECODE_SERVERMESSAGE = 99;
  private static final byte BYTECODE_CLOSECONNECTION = -1;
  private static final byte BYTECODE_SERVERPING = 100;
  
  
  public ClientThread(ChatServer cs, Socket client) {
    this.cs = cs;
    this.client = client;
    try {
      input = new DataInputStream(client.getInputStream());
      output = new DataOutputStream(client.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void run(){
    System.out.println("client connected: " + name + client.getInetAddress());
    
    String messsage = name + " joined the chat";
    cs.serverMessage(messsage, client);  
    
    
    try {
      boolean done = false;
      while(!done){
        
        switch(input.readByte()){
          
          case BYTECODE_MESSAGE: //textnachrichten von user
          String msg = input.readUTF();
          System.out.println("" + client.getInetAddress() +" said: " + msg);
          cs.sendMsg(msg, client);
          break;
          
          case BYTECODE_SERVERMESSAGE: //serverinterners
          switch(input.readByte()){
            
            case BYTECODE_SERVERMESSAGE: //ping ob noch verbunden
            System.out.println(client.getInetAddress() + " checked for connection");
            output.writeByte(BYTECODE_SERVERMESSAGE);
            output.writeByte(BYTECODE_SERVERPING);
            output.flush();
            break;
            
            case BYTECODE_MESSAGE:         //server messages
            String oldName =  name;
            name = input.readUTF();
            String messsage = oldName + " changed name to " + name;
            cs.serverMessage(messsage, client);  
            
            default:
            break;
          }
          break;
          
          case BYTECODE_CLOSECONNECTION:
          
          done = true;
          System.out.println("Client " + name + client.getInetAddress() + " left" );
          String messsage = "Client " + name + " has left the chat.";
          cs.serverMessage(messsage, client);
          
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
      System.out.println("Client " + name + client.getInetAddress() + " lost connection" );
      try {
        cs.fixStreams(client);
        String messsage = "Client " + name + " lost connection.";
        cs.serverMessage(messsage, client); 
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      
    }
    
  }
  
}
