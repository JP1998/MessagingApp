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

/**
 * Dieses Interface kann implementiert werden, damit die konstanten byte-Codes global konsisitent sind.
 */
public interface Connected{

    /**
     * Die Konstante, die anzeigt, dass die Verbindung geschlossen wird
     */
    byte BYTECODE_CLOSECONNECTION = -1;
    /**
     * Die Konstante, welche eine Nachricht von einem User einleitet
     */
    byte BYTECODE_MESSAGE = 1;
    /**
     * Die Konstante, die eine Servernachricht einleitet
     */
    byte BYTECODE_SERVERMESSAGE = 2;
    /**
     * Die Konstante, die die Veränderung des Benutzernamens einleitet
     */
    byte BYTECODE_CHANGENAME = 3;
    /**
     * Die Konstante, die den Server anpingt (Antwort ist die selbe)
     */
    byte BYTECODE_SERVERPING = 4;
    /**
     * Die Konstante, die genutzt wird, um eine Liste der Namen der verbundenen Nutzer zu versenden
     */
    byte BYTECODE_NAMES = 5;
    /**
     * Die Konstante, die genutzt wird, um die Anzahl an verbundenen Nutzern zu versenden
     */
    byte BYTECODE_NAMESCOUNT = 6;

}
