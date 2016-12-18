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

package de.jeanpierrehotz.messagingapptest.messages;

import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Diese Klasse kann genutyt werden, um Nachrichten zu speichern, wobei man die Anzahl an gespeicherten Nachrichten
 * auf eine bestimmte Anzahl limitieren kann, damit die App nicht zu viel Speicher verbraucht.
 */
public class MessageLoader {

    /**
     * Die Codes, mit denen die einzelnen Komponenten einer Nachricht in einer SharedPreference gespeichert wird.
     */
    private static final String
            PREFERENCECODE_MESSAGEAMOUNT = "private static final String PREFERENCECODE_MESSAGEAMOUNT",
            PREFERENCECODE_MESSAGE = "private static final String PREFERENCECODE_MESSAGE",
            PREFERENCECODE_NAME = "private static final String PREFERENCECODE_NAME",
            PREFERENCECODE_TIME = "private static final String PREFERENCECODE_TIME",
            PREFERENCECODE_TYPE = "private static final String PREFERENCECODE_TYPE",
            PREFERENCECODE_SAVEDNAME = "private static final String PREFERENCECODE_SAVEDNAME";

    /**
     * Die Konstante, die anzeigt, dass alle Nachrichten in der Liste gespeichert werden sollen.
     */
    public static final int AMOUNT_UNLIMITED = -1;

    private MessageLoader(){}

    /**
     * Diese Methode speichert alle Nachrichten in der gegebenen Liste in der gegebenen SharedPreference.<br>
     * Gleiches Resultat wie:
     * <pre><code>
     *     SharedPreferences prefs = <span style="color: #808080;">...</span>;
     *     ArrayList&lt;Message&gt; msg = <span style="color: #808080;">...</span>;
     *     MessageLoader.saveMessages(prefs, msg, MessageLoader.AMOUNT_UNLIMITED);
     * </code></pre>
     *
     * @param prefs Die SharedPreferences in die die Nachrichten gespeichert werden sollen
     * @param msg Die Nachrichten, die gespeichert werden sollen
     */
    public static void saveMessages(SharedPreferences prefs, ArrayList<Message> msg){
        saveMessages(prefs, msg, AMOUNT_UNLIMITED);
    }

    /**
     * Diese Methode speichert die Nachrichten in der gegebenen Liste in der gegebenen SharedPreference.<br>
     * Dabei wird das gegebene Limit beachtet.
     *
     * @param prefs Die SharedPreferences in die die Nachrichten gespeichert werden sollen
     * @param msg Die Nachrichten, die gespeichert werden sollen
     * @param limit Die Anzahl an Nachrichten, die höchstens gespeichert werden sollen
     * @see #AMOUNT_UNLIMITED
     */
    public static void saveMessages(SharedPreferences prefs, ArrayList<Message> msg, int limit){
        int amount = (limit == AMOUNT_UNLIMITED)? msg.size(): Math.min(limit, msg.size());

        SharedPreferences.Editor editor = prefs.edit().clear().putInt(PREFERENCECODE_MESSAGEAMOUNT, amount);

        int delta = msg.size() - amount;

        for(int ctr = 0, i = msg.size() - 1; ctr < amount; ctr++, i--){
            Message currMsg = msg.get(i);

            int newIndex = i - delta;

            editor.putString(PREFERENCECODE_MESSAGE + newIndex, currMsg.getMessage())
                    .putLong(PREFERENCECODE_TIME + newIndex, currMsg.getTime())
                    .putInt(PREFERENCECODE_TYPE + newIndex, currMsg.getMessageType().getCode());

            if(currMsg instanceof ReceivedMessage){
                editor.putString(PREFERENCECODE_NAME + newIndex, ((ReceivedMessage) currMsg).getUserName())
                        .putBoolean(PREFERENCECODE_SAVEDNAME + newIndex, true);
            }
        }

        editor.apply();
    }

    /**
     * Diese Methode lädt alle Nachrichten aus der gegebenen SharedPreference.
     *
     * @param prefs Die SharedPreferences aus der die Nachrichten geladen werden sollen
     * @return die Nachrichten in der SharedPreference
     */
    public static ArrayList<Message> loadMessages(SharedPreferences prefs){
        ArrayList<Message> msg = new ArrayList<>();

        int amount = prefs.getInt(PREFERENCECODE_MESSAGEAMOUNT, 0);

        for(int i = 0; i < amount; i++) {
            String message = prefs.getString(PREFERENCECODE_MESSAGE + i, "");
            long time = prefs.getLong(PREFERENCECODE_TIME + i, System.currentTimeMillis());
            Message.Type type = Message.Type.fromCode(prefs.getInt(PREFERENCECODE_TYPE + i, -1));

            if(prefs.getBoolean(PREFERENCECODE_SAVEDNAME + i, false)){
                String name = prefs.getString(PREFERENCECODE_NAME + i, "");

                msg.add(new ReceivedMessage(name, message, time, type));
            } else {
                msg.add(new Message(message, time, type));
            }
        }

        return msg;
    }

}
