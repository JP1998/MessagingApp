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

package de.jeremy.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BetterThread extends Thread {

    private static final byte BYTECODE_CLOSECONNECTION = -1;
    private static final byte BYTECODE_MESSAGE = 1;
    private static final byte BYTECODE_SERVERMESSAGE = 2;
    private static final byte BYTECODE_CHANGENAME = 3;
    private static final byte BYTECODE_SERVERPING = 4;
    private static final byte BYTECODE_NAMES = 5;
    private static final byte BYTECODE_NAMESCOUNT = 6;

    private final String STRING_IPADRESSE = "localhost";
    private final int INT_PORT = 1234;

    private BetterClient bc;
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;
    private boolean done;

    public BetterThread(BetterClient bc) {
        this.bc = bc;
        done = false;
        name = "Jeremy";
        try {
            client = new Socket(STRING_IPADRESSE, INT_PORT);
            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());
            out.writeByte(BYTECODE_CHANGENAME);
            out.writeUTF(name);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        while (!done) {
            try {
                switch (in.readByte()) {
                    case BYTECODE_MESSAGE:
                        String s = in.readUTF() + ": " + in.readUTF();
                        bc.addMessage(s);
                        break;
                    case BYTECODE_SERVERMESSAGE:
                        bc.addServerMessage(in.readUTF());
                        showAllNames();
                        break;
                    case BYTECODE_SERVERPING:
                        bc.addMessage("*****Server noch verbunden*****");
                        break;
                    case BYTECODE_CLOSECONNECTION:
                        done = true;
                        in.close();
                        System.exit(1);
                        break;
                    case BYTECODE_NAMES:
                        bc.updateNameList(in.readUTF().replaceAll(";", "\n"));
                        ;
                        break;
                    case BYTECODE_NAMESCOUNT:
                        bc.addMessage("Verbundenen Nutzer: " + in.readInt());
                        showAllNames();
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

    public void servercheck() {

        try {
            out.writeByte(BYTECODE_SERVERPING);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopChatting() {
        done = true;
        try {
            out.writeByte(BYTECODE_CLOSECONNECTION);
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public void changeName(String s) {
        s = s.substring(6, s.length());
        name = s;
        try {
            out.writeByte(BYTECODE_CHANGENAME);
            out.writeUTF(s);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAllNames() {
        try {
            out.writeByte(BYTECODE_NAMES);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void namesCount() {
        try {
            out.writeByte(BYTECODE_NAMESCOUNT);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendServerMessage(String msg) {
        msg = msg.substring(3, msg.length());
        try {
            out.writeInt(BYTECODE_SERVERMESSAGE);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        bc.addMessage(name + ": " + msg);
        try {
            out.writeByte(BYTECODE_MESSAGE);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
