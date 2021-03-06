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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ClientMessageListener extends Thread {

    private static final byte BYTECODE_CLOSECONNECTION = -1;
    private static final byte BYTECODE_MESSAGE = 1;
    private static final byte BYTECODE_SERVERMESSAGE = 2;
    // private static final byte BYTECODE_CHANGENAME = 3;
    private static final byte BYTECODE_SERVERPING = 4;
    private static final byte BYTECODE_NAMES = 5;
    private static final byte BYTECODE_NAMESCOUNT = 6;

    private DataInputStream input;

    public ClientMessageListener(DataInputStream input) {
        this.input = input;
    }

    public void run() {

        boolean done = false;
        while (!done) {
            try {

                switch (input.readByte()) {
                    case BYTECODE_MESSAGE:
                        String s = input.readUTF() + ": " + input.readUTF();
                        System.out.println(s);
                        break;
                    case BYTECODE_SERVERMESSAGE:
                        System.out.println("Server: " + input.readUTF());
                        break;
                    case BYTECODE_SERVERPING:
                        System.out.println("Server noch verbunden");
                        break;
                    case BYTECODE_CLOSECONNECTION:
                        done = true;
                        input.close();
                        System.exit(1);
                        break;
                    case BYTECODE_NAMES:
                        List<String> list = Arrays.asList(input.readUTF().split(";"));
                        list.forEach(System.out::println);
                        break;
                    case BYTECODE_NAMESCOUNT:
                        System.out.println("Verbundenen Nutzer: " + input.readInt());
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

}
