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
 * Um die Ping-Funktion zu nutzen muss das ClientMessageSender-Objekt auf folgende Art und Weise
 * mit einem {@link ClientMessageListener}-Objekt gebunden sein:
 * <pre><code>
    <span style="color: #808080;">// setup the {@link java.net.Socket} to connect to</span>
    Socket client = <span style="color: #000080;">new</span> Socket(<span style="color: #007700;">"yoururl.ddns.net"</span>, <span style="color: #0000FF;">8080</span>);

    <span style="color: #808080;">// setup the ClientMessageSender according to the Socket, and give it a {@link de.jeanpierrehotz.messagingapptest.network.ClientMessageSender.PingListener}</span>
    ClientMessageSender clientMessageSender = <span style="color: #000080;">new</span> ClientMessageSender(<span style="color: #808080;">[...]</span>);
    clientMessageSender.setPingListener(<span style="color: #808080;">[...]</span>);

    <span style="color: #808080;">// setup the {@link ClientMessageListener}, give it a {@link de.jeanpierrehotz.messagingapptest.network.ClientMessageListener.ClosingDetector},</span>
    <span style="color: #808080;">// bind the ClientMessageSender to it and start listening for messages</span>
    ClientMessageListener clientMessageListener = <span style="color: #000080;">new</span> ClientMessageListener(<span style="color: #808080;">[...]</span>);
    clientMessageListener.setClosingDetector(<span style="color: #808080;">[...]</span>);
    clientMessageListener.bindMessageSender(clientMessageSender);
    clientMessageListener.start();
 * </code></pre>
 */
public class ClientMessageSender implements ClientConnected{

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
     * Dieser Konstruktor erstellt einen ClientMessageSender für den gegebenen DataOutputStream
     *
     * @param out der DataOutputStream mit dem kommuniziert werden soll
     */
    public ClientMessageSender(DataOutputStream out){
        sendingStream = out;
        pingRunning = false;
        pingTimeoutRunning = false;
    }

    public void setPingListener(PingListener pingListener){
        this.pingListener = pingListener;
    }

    /**
     * Diese Methode leitet einen Ping an den Server ein.<br>
     * Um nach dem Erhalt des Resultats des Pings benachrichtigt werden können sie den {@link de.jeanpierrehotz.messagingapptest.network.ClientMessageSender.PingListener} nutzen.<br>
     * Die Methode sendet den Ping-Befehl an den Server, stellt ein, dass er eine Ping-Antwort erwartet (mit {@link #pingRunning}), und
     * startet den TimeOutDetector, damit nach {@link #PING_TIMEOUT}ms der Ping abgerochen wird
     */
    public void pingServer(){
//      nur einen Ping starten, falls er nicht mit dem vorhergehenden in Konflikt gerät
        if(!pingTimeoutRunning){
//          auf einem neuen Thread (Android erlaubt keine Netzwerkkommunikation auf dem Mainthread)
            new Thread(new Runnable(){
                @Override
                public void run(){
                    try{
//                      sende den Pingbefehl
                        sendingStream.writeByte(BYTECODE_SERVERMESSAGE);
                        sendingStream.writeByte(BYTECODE_SERVERPING);
                        sendingStream.flush();

//                      auf Ping-Antwort warten
                        pingRunning = true;

//                      TimeOutDetector starten
                        new Thread(pingRunnable).start();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * Diese Methode schließt den Nachrichtenstream, und gibt dem Server davor die Nachricht, dass der Teilnehmer
     * keine Nachrichten mehr erhalten soll.
     */
    public void close(){
//      auf einem neuen Thread (Android erlaubt keine Netzwerkkommunikation auf dem Mainthread)
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
//                  den Server benachrichtigen, dass der Teilnehmer sich trennt
                    sendingStream.write(BYTECODE_CLOSECONNECTION);
                    sendingStream.flush();
//                  danach den Stream schließen
                    sendingStream.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Diese Methode schickt eine Usernachricht an den Server, der diese an alle Teilnehmer versenden soll
     *
     * @param msg die Nachricht, die zu verschicken ist
     */
    public void send(final String msg){
//      auf einem neuen Thread (Android erlaubt keine Netzwerkkommunikation auf dem Mainthread)
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
//                  die Nachricht senden, wobei wir davor sagen, dass diese Nachricht für die User ist
                    sendingStream.writeByte(BYTECODE_MESSAGE);
                    sendingStream.writeUTF(msg);
                    sendingStream.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Diese Methode benachrichtigt den Server, dass der User einen neuen Namen benutzen möchte
     * @param newName der neue Name,der benutzt werden soll
     */
    public void changeName(final String newName) {
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
//                  den Namen senden, wobei wir davor sagen, dass das der neue Name ist
                    sendingStream.writeByte(BYTECODE_SERVERMESSAGE);
                    sendingStream.writeByte(BYTECODE_MESSAGE);
                    sendingStream.writeUTF(newName);
                    sendingStream.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Diese Methode zeigt dem ClientMessageSender-Objekt an, dass eine Antwort auf den Ping erhalten wurde.<br>
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
