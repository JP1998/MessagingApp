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
    private Message(String mes, long time, Type msgType){
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
        Announcement
    }

}
