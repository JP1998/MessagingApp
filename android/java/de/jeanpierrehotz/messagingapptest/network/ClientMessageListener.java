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

import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 */
public class ClientMessageListener extends Thread implements ClientConnected {

    private DataInputStream input;

    private ClosingDetector closingDetector;
    private OnMessageReceivedListener messageReceivedListener;

    public ClientMessageListener(DataInputStream input) {
        this.input = input;
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageReceivedListener = listener;
    }

    public void setClosingDetector(ClosingDetector closingDetector) {
        this.closingDetector = closingDetector;
    }

    public void run(){
        if(closingDetector != null){
            Thread currThr = Thread.currentThread();
            while(closingDetector.isNotToBeClosed(currThr)){
                try{
                    byte readByte = input.readByte();

                    if(readByte == BYTECODE_USERMESSAGE){
                        String msg = input.readUTF();
                        if(messageReceivedListener != null && !msg.trim().equals("")){
                            messageReceivedListener.onMessageReceived(msg);
                        }
                    } else if(readByte == BYTECODE_SERVERMESSAGE) {
                        messageReceivedListener.onPing(input.readByte() == BYTECODE_SERVERPING);
                    }
                }catch(IOException e){
                }

                try{
                    Thread.sleep(10);
                }catch(Exception e){
                }
            }
        } else {
            throw new IllegalStateException("ClosingDetector is needed! Use ClientMessageListener#setClosingDetector(ClosingDetector).");
        }
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String msg);
        void onPing(boolean success);
    }

    public interface ClosingDetector{
        boolean isNotToBeClosed(Thread runningThr);
    }

}