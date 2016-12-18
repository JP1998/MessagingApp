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
 * Diese Klasse kann genutzt werden, um auf einfache Art und Weise Nachrichten vom
 * Endnutzer-Gerät an den Server zu schicken.
 * Um die Ping-Funktion zu nutzen muss das MessageSender-Objekt auf folgende Art und Weise
 * mit einem {@link MessageListener}-Objekt gebunden sein:
 * <pre><code>
    <span style="color: #808080;">// setup the {@link java.net.Socket} to connect to</span>
    Socket client = <span style="color: #000080;">new</span> Socket(<span style="color: #007700;">"yoururl.ddns.net"</span>, <span style="color: #0000FF;">8080</span>);

    <span style="color: #808080;">// setup the MessageSender according to the Socket, and give it a {@link MessageSender.PingListener}</span>
    MessageSender clientMessageSender = <span style="color: #000080;">new</span> MessageSender(<span style="color: #808080;">[...]</span>);
    clientMessageSender.setPingListener(<span style="color: #808080;">[...]</span>);

    <span style="color: #808080;">// setup the {@link MessageListener}, give it a {@link MessageListener.ClosingDetector},</span>
    <span style="color: #808080;">// bind the MessageSender to it and start listening for messages</span>
    MessageListener clientMessageListener = <span style="color: #000080;">new</span> MessageListener(<span style="color: #808080;">[...]</span>);
    clientMessageListener.setClosingDetector(<span style="color: #808080;">[...]</span>);
    clientMessageListener.bindMessageSender(clientMessageSender);
    clientMessageListener.start();
 * </code></pre>
 */
public class MessageSender implements Connected{

    /**
     * Die Zeit in ms, die auf eine Antwort auf einen Ping gewartet wird, bevor die Verbindung als unterbrochen angesehen wird
     */
    private static final long PING_TIMEOUT = 1500;

    /**
     * Der OutputStream. mit dem wir Nachrichten versenden
     */
    private DataOutputStream sendingStream;

    /**
     * Der PingListener, der das Resultat eines Pings gesagt bekommt.<br>
     * Wird nur nach Aufrufen der Methode {@link #pingServer()},
     * und spätestens {@link #PING_TIMEOUT} nach dessen Aufruf Events erhalten.
     */
    private PingListener pingListener;

    /**
     * Dieses Runnable ist dafür zuständig den Pingvorgang nach {@link #PING_TIMEOUT}ms abzubrechen.
     */
    private Runnable pingRunnable = new Runnable(){
        @Override
        public void run(){
//          Wir zeigen an, dass der TimeOutDetector derzeit am Laufen ist
            pingTimeoutRunning = true;
//          warten die Zeit, bis der Ping als Timeout gilt
            try{
                Thread.sleep(PING_TIMEOUT);
            }catch(Exception e){
                e.printStackTrace();
            }

//          falls der Ping immer noch am Laufen ist
            if(pingRunning){
//              geben wir an, dass die Verbindung unterbrochen ist
                onPingReceived(false);
            }
//          und zeigen schließlich an, dass der TimeOutDetector nicht mehr am Laufen ist
            pingTimeoutRunning = false;
        }
    };

    /**
     * Diese Variable zeigt an, ob noch auf eine Antwort auf einen Ping gewartet wird
     */
    private boolean pingRunning;
    /**
     * Diese Variable zeigt an, ob der PingTimeOutDetector derzeit am Laufen ist
     */
    private boolean pingTimeoutRunning;

    /**
     * Dieser Konstruktor erstellt einen MessageSender für den gegebenen DataOutputStream
     *
     * @param out der DataOutputStream mit dem kommuniziert werden soll
     */
    public MessageSender(DataOutputStream out){
        sendingStream = out;
        pingRunning = false;
        pingTimeoutRunning = false;
    }

    public void setPingListener(PingListener pingListener){
        this.pingListener = pingListener;
    }

    /**
     * Diese Methode leitet einen Ping an den Server ein.<br>
     * Um nach dem Erhalt des Resultats des Pings benachrichtigt werden können sie den {@link MessageSender.PingListener} nutzen.<br>
     * Die Methode sendet den Ping-Befehl an den Server, stellt ein, dass er eine Ping-Antwort erwartet (mit {@link #pingRunning}), und
     * startet den TimeOutDetector, damit nach {@link #PING_TIMEOUT}ms der Ping abgerochen wird
     */
    public void pingServer(){
        if(!pingTimeoutRunning) {
            this.send(BYTECODE_SERVERPING, null, false, true);
        }
    }

    /**
     * Diese Methode schließt den Nachrichtenstream, und gibt dem Server davor die Nachricht, dass der Teilnehmer
     * keine Nachrichten mehr erhalten soll.
     */
    public void close(){
        this.send(BYTECODE_CLOSECONNECTION, null, true);
    }

    /**
     * Diese Methode schickt eine Usernachricht an den Server, der diese an alle Teilnehmer versenden soll
     *
     * @param msg die Nachricht, die zu verschicken ist
     */
    public void sendMessage(String msg){
        this.send(BYTECODE_MESSAGE, msg);
    }

    /**
     * Diese Methode benachrichtigt den Server, dass der User einen neuen Namen benutzen möchte
     * @param newName der neue Name,der benutzt werden soll
     */
    public void changeName(String newName) {
        this.send(BYTECODE_CHANGENAME, newName);
    }

    public void sendNameCountRequest(){
        this.send(BYTECODE_NAMESCOUNT, null);
    }

    public void sendNamesRequest(){
        this.send(BYTECODE_NAMES, null);
    }

    /**
     *
     * The field {@code param} is accounted as additional, and should only be given if needed.<br>
     * The position of the values in said field is of importance, and may lead to extreme misbehavior
     * if not taken into account.<br>
     * The needed order can be seen in the table below:
     * <table style="border: 1px solid black;">
     *     <tr>
     *         <th>index</th>
     *         <th>name</th>
     *         <th>further description</th>
     *     </tr>
     *     <tr>
     *         <td>0</td>
     *         <td>close</td>
     *         <td>whether to close the stream after sending the message or not</td>
     *     </tr>
     *     <tr>
     *         <td>1</td>
     *         <td>ping</td>
     *         <td>whether to listen for a ping answer after sending the message</td>
     *     </tr>
     * </table>
     *
     * @param code the bytecode to send before the message
     * @param msg the message to send
     * @param param additional parameters as explained above
     */
    private void send(final byte code, final String msg, final boolean... param) {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    sendingStream.writeByte(code);
                    if(msg != null) {
                        sendingStream.writeUTF(msg);
                    }
                    sendingStream.flush();

                    if(param.length >= 1 && param[0]){
                        sendingStream.close();
                    }

                    if(param.length >= 2 && param[1]){
                        pingRunning = true;
                        new Thread(pingRunnable).start();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Diese Methode zeigt dem MessageSender-Objekt an, dass eine Antwort auf den Ping erhalten wurde.<br>
     * Diese ermittelt dann, ob diese ausgewertet werden muss, und feuert, falls nötig ein Event für den PingListener
     *
     * @param success ob der erhaltene Ping Erfolg hatte
     */
    void onPingReceived(boolean success){
//      falls die Antwort ausgewertet werden muss
        if(pingListener != null && pingRunning){
//          feuern wir das genannte Event
            pingListener.onConnectionDetected(success);
        }
//      und geben an, dass keine Antwort bis zum nächsten Aufruf der Methode #pingServer() ausgewertet werden muss
        pingRunning = false;
    }

    /**
     * Dieses Interface bietet die Möglichkeit bei Erhalt einer Antwort auf einen Ping etwas auszuführen,
     * wie beispielsweise dem User anzuzeigen, dass die Verbindung besteht, oder nicht.
     */
    public interface PingListener{

        /**
         * Diese Methode wird beim ersten Erhalt einer Antwort auf einen Ping ausgeführt.<br>
         * Durch den TimeOutDetector wird diese spätestens {@link #PING_TIMEOUT}ms nach dem Aufruf
         * der {@link #pingServer()}-Methode ausgeführt.
         *
         * @param connected Ob die Antwort gültig war, oder nicht.
         */
        void onConnectionDetected(boolean connected);
    }

}
