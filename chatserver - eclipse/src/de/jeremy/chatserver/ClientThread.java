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
import java.io.InputStream;
import java.io.OutputStream;

public class ClientThread extends Thread {

    private static final String GAME_INSTRUCTIONS = "How to use @game:"
            + "\n@game titels	displays available games"
            + "\n@game <titel>	starts a game"
            + "\n@game exit	exits the current game"
            + "\n@game <msg>	to communicate with the game";

    private ChatServer cs;
    private int hashCode;
    private DataInputStream input;
    private DataOutputStream output;
    private String name;

    private ChatGame game = null;

    private static final byte BYTECODE_CLOSECONNECTION = -1;
    private static final byte BYTECODE_MESSAGE = 1;
    private static final byte BYTECODE_SERVERMESSAGE = 2;
    private static final byte BYTECODE_CHANGENAME = 3;
    private static final byte BYTECODE_SERVERPING = 4;
    private static final byte BYTECODE_NAMES = 5;
    private static final byte BYTECODE_NAMESCOUNT = 6;

    public ClientThread(ChatServer cs, int hashCode, InputStream in, OutputStream out) {
        this.cs = cs;
        this.hashCode = hashCode;
        input = new DataInputStream(in);
        output = new DataOutputStream(out);
    }

    public void run() {

        try {
            boolean done = false;
            while (!done) {

                switch (input.readByte()) {

                    case BYTECODE_CLOSECONNECTION:
                        done = true;
                        closeConenction();
                        break;
                    case BYTECODE_SERVERPING:
                        ping();
                        break;
                    case BYTECODE_MESSAGE:
                        userMessage();
                        break;
                    case BYTECODE_SERVERMESSAGE:
                        serverMessage();
                        break;
                    case BYTECODE_CHANGENAME:
                        changeName();
                        break;
                    case BYTECODE_NAMES:
                        cs.sendAllNames(hashCode);
                        break;
                    case BYTECODE_NAMESCOUNT:
                        cs.sendNamesCountSingle(hashCode);
                        break;
                    default:
                        break;
                }
            }

        } catch (IOException e) {
            lostConnection();
        }
    }

    private void playGame(String msg) throws IOException {
        String originalMsg = msg;
        msg = msg.toLowerCase(); // Um "@Game" o.ä. zu erlauben

        if (msg.startsWith("@game")) {
            String name2 = "Gamemaster";
            String[] games = {"numbergame", "flipcoin"};

            if (msg.equals("@game")) { // game instrucutons
                cs.sendGameMessage(name2, GAME_INSTRUCTIONS, hashCode);
            } else if (msg.equals("@game titels")) { // titel wahl
                String titels = "Available games:";
                for (String titel : games) {
                    titels = titels + "\n" + titel;
                }

                cs.sendGameMessage(name2, titels, hashCode);
            } else if (msg.startsWith("@game ")) { // check ob titel gewählt
                if(game == null) { // nur Titel überprüfen, wenn man in keinem Spiel ist
                    if (msg.equals("@game numbergame")) { // starte entsprechendes Spiel
                        game = new Numbergame();
                    } else if (msg.equals("@game flipcoin")) {
                        game = new FlipCoin();
                    }else {
                        cs.sendGameMessage(name2, "You are not in-game!\nType \"@Game\" for more information.", hashCode);
                        return;
                    }

//                  User begrüßen
                    cs.sendGameMessage(name2, game.getWelcomeMessage(), hashCode);
                } else {
                    String gamemessage = game.handleMessage(originalMsg.substring(6));

                    if(gamemessage != null) {
                        cs.sendGameMessage(name2, gamemessage, hashCode);
                    }

                    if(game.exitGame()) { // User verabschieden, und Spiel beenden
                        cs.sendGameMessage(name2, game.getExitMessage(), hashCode);
                        game = null;
                    } else if(game.showInstructions()) { // User die Instruktionen schicken
                        cs.sendGameMessage(name2, game.getInstructions(), hashCode);
                    }
                }
            } // ende ifs
        }// ende if "@game"
    }

    private void userMessage() throws IOException {
        String msg = input.readUTF();

        if (msg.startsWith("@")) {

            playGame(msg);

            return;
        }

        System.out.println(name + cs.getInetAddress(hashCode) + " said: " + msg);
        cs.sendMsg(msg, hashCode);
    }

    private void serverMessage() throws IOException {
        String message = input.readUTF();
        System.out.println("Server: " + message);
        cs.serverMessage(message, hashCode, true);
    }

    private void changeName() throws IOException {

        if (name == null) {
            name = input.readUTF();
            System.out.println("client connected: " + name + cs.getInetAddress(hashCode));
            String messsage = name + " joined the chat";
            cs.serverMessage(messsage, hashCode, false);
            cs.addName(name, hashCode);

        } else {
            String oldName = name;
            name = input.readUTF();
            String message = oldName + " changed name to " + name;
            System.out.println("Server: " + message);
            cs.serverMessage(message, hashCode, true);
            cs.changeName(name, hashCode);
        }
    }

    private void ping() throws IOException {
        System.out.println(name + cs.getInetAddress(hashCode) + " checked for connection");
        output.writeByte(BYTECODE_SERVERPING);
        output.flush();
    }

    private void closeConenction() throws IOException {
        System.out.println("Client " + name + cs.getInetAddress(hashCode) + " left");
        String messsage = "Client " + name + " has left the chat.";
        cs.serverMessage(messsage, hashCode, false);
        cs.fixStreams(hashCode);
        input.close();
        output.close();
        cs.removeName(hashCode);
    }

    private void lostConnection() {
        System.out.println("Client " + name + cs.getInetAddress(hashCode) + " lost connection");
        try {
            input.close();
            output.close();
        } catch (IOException e) {
        }
        cs.fixStreams(hashCode);
        String messsage = "Client " + name + " lost connection.";
        cs.serverMessage(messsage, hashCode, false);
        cs.removeName(hashCode);
    }

}
