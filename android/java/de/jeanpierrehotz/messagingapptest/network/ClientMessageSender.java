/*
 *     Copyright 2016 Jeremy Schiemann, Jean-Pierre Hotz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.jeanpierrehotz.messagingapptest.network;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Admin on 09.12.2016.
 */
public class ClientMessageSender implements ClientConnected {

    private static final long PING_TIMEOUT = 1000;

    /**
     * Der OutputStream. mit dem wir Nachrichten versenden
     */
    private DataOutputStream sendingStream;

    private PingListener pingListener;

    private Runnable pingRunnable = new Runnable(){
        @Override
        public void run(){
            try{
                Thread.sleep(PING_TIMEOUT);
            } catch(Exception e) {}

            if(pingRunning) {
                onPingReceived(false);
            }
        }
    };

    private boolean pingRunning;

    public ClientMessageSender(DataOutputStream out) {
        sendingStream = out;
        pingRunning = false;
    }

    public void setPingListener(PingListener pingListener){
        this.pingListener = pingListener;
    }

    public void pingServer() {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    sendingStream.writeByte(BYTECODE_SERVERMESSAGE);
                    sendingStream.writeByte(BYTECODE_SERVERPING);
                    sendingStream.flush();

                    pingRunning = true;

                    new Thread(pingRunnable).start();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void close() {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    sendingStream.write(BYTECODE_CLOSECONNECTION);
                    sendingStream.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void send(final String msg) {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    sendingStream.writeByte(BYTECODE_USERMESSAGE);
                    sendingStream.writeUTF(msg);
                    sendingStream.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onPingReceived(boolean success) {
        if(pingListener != null){
            if(pingRunning){
                pingListener.onConnectionDetected(success);
            }
            pingRunning = false;
        }
    }

    public interface PingListener {
        void onConnectionDetected(boolean connected);
    }

}
