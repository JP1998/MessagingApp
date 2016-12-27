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

package de.jeanpierrehotz.messaging.messages;


import com.sun.istack.internal.Nullable;

/**
 * Diese Klasse wird genutzt, um eine Nachricht darzustellen
 */
public class Message{

    /**
     * Die eigentliche Nachricht als Text
     */
    private String mMessage;
    /**
     * Der Zeitpunkt der Entstehung der Nachricht
     */
    private long mTime;
    /**
     * Der Typ der Nachricht
     */
    private Type mMessageType;

    /**
     * Dieser Konstruktor erstellt eine neue Nachricht, welche zur Zeit des Aufrufs des Konstruktors entstand
     *
     * @param mes     Die Nachricht als Text
     * @param msgType Der Typ der Nachricht
     */
    public Message(String mes, Type msgType){
//      Wir rufen den privaten Konstruktor auf, und geben ihm die derzeitige Zeit
        this(mes, System.currentTimeMillis(), msgType);
    }

    /**
     * Dieser Konstruktor erstellt eine neue Nachricht, welche zur gegebenen Zeit entstand.
     * Dieser Konstruktor sollte genutzt werden, um beispielsweise Nachrichten (woher auch immer) zu laden.
     *
     * @param mes     Die Nachricht als Text
     * @param time    Die Zeit, zu der die Nachricht entstand
     * @param msgType Der Typ der Nachricht
     */
    public Message(String mes, long time, Type msgType){
        this.mMessage = mes;
        this.mTime = time;
        this.mMessageType = msgType;
    }

    /**
     * Diese Methode gibt ihnen den Text der Nachricht
     *
     * @return den Text der Nachricht
     */
    public String getMessage(){
        return mMessage;
    }

    /**
     * Diese Methode gibt ihnen den Typen der Nachricht
     *
     * @return den Typ der Nachricht
     */
    public Type getMessageType(){
        return mMessageType;
    }

    /**
     * Diese Methode gibt ihnen die Zeit zu der die Nachricht entstand
     *
     * @return die Zeit zu der die Nachricht entstand
     */
    public long getTime(){
        return mTime;
    }

    /**
     * Diese Enumeration gibt an, welche Art von Nachricht eine Nachricht ist
     */
    public enum Type{
        Sent,
        Received,
        Announcement;

        /**
         * Die Codes, die die Nachrichtentypen darstellen.
         * Sollten nur zum Speichern genutzt werden.
         */
        private static final int
                CODE_SENT = 0x1234,
                CODE_RECEIVED = 0x2341,
                CODE_ANNOUNCEMENT = 0x3412,
                CODE_INVALID = 0x4123;

        /**
         * Diese Methode gibt ihnen den Code, mit dem sich der Nachrichtentyp darstellen lässt.
         *
         * @return der repräsentierende Code
         */
        public int getCode(){
            switch(this){
                case Sent: return CODE_SENT;
                case Received: return CODE_RECEIVED;
                case Announcement: return CODE_ANNOUNCEMENT;
                default: return CODE_INVALID;
            }
        }

        /**
         * Diese Methode erzeugt aus einem Code den entsprechenden Nachrichtentyp.
         *
         * @param code der Code, der decodiert werden soll
         * @return der Nachrichtentyp, der von dem gegebenen Code dargestellt wird; {@code null} bei ungültigem Code
         */
        @Nullable
        public static Type fromCode(int code){
            switch(code){
                case CODE_SENT: return Sent;
                case CODE_RECEIVED: return Received;
                case CODE_ANNOUNCEMENT: return Announcement;
                default: return null;
            }
        }
    }

}
