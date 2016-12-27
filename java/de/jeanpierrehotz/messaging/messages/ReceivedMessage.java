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

/**
 * Diese Klasse wird genutzt, um eine Nachricht darzustellen, die vom Server empfangen wurden.
 * Diese besitzen zusätzlich den Namen des Users, der diese Nachricht verschickt hat.
 */
public class ReceivedMessage extends Message {

    /**
     * Der Name des Users, der diese Nachricht verschickt hat
     */
    private String userName;

    /**
     * Dieser Konstruktor erstellt eine neue Nachricht, welche zur Zeit des Aufrufs des Konstruktors entstand
     *
     * @param name    Der Name des Users, der die Nachricht verschickt hat
     * @param mes     Die Nachricht als Text
     * @param msgType Der Typ der Nachricht
     */
    public ReceivedMessage(String name, String mes, Type msgType){
        super(mes, msgType);

        this.userName = name;
    }

    /**
     * Dieser Konstruktor erstellt eine neue Nachricht, welche zur gegebenen Zeit entstand.
     * Dieser Konstruktor sollte genutzt werden, um beispielsweise Nachrichten (woher auch immer) zu laden.
     *
     * @param name    Der Name des Users, der die Nachricht verschickt hat
     * @param mes     Die Nachricht als Text
     * @param time    Die Zeit, zu der die Nachricht entstand
     * @param msgType Der Typ der Nachricht
     */
    public ReceivedMessage(String name, String mes, long time, Type msgType){
        super(mes, time, msgType);

        this.userName = name;
    }

    /**
     * Diese Methode gibt ihnen den Namen des Users, der diese Nachricht verschickt hat
     * @return den Namen des Users, der diese Nachricht verschickt hat
     */
    public String getUserName(){
        return userName;
    }

}
