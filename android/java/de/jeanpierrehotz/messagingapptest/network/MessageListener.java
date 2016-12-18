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

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Diese Klasse bietet die Funktionalität Nachrichten des Servers leicht zu erhalten.<br>
 * Bevor der Thread gestartet wird muss dem Objekt ein {@link MessageListener.ClosingDetector}-Objekt
 * übergeben werden, in dem eine Terminierungsbedingung gestellt wird. Bspw.:
 * <pre><code>
 *  <span style="color: #808080;">// setup the MessageListener with a DataInputStream of your choice</span>
 *  MessageListener cml = <span style="color: #000080;">new</span> MessageListener(<span style="color: #808080;">[...]</span>);
 *  <span style="color: #808080;">// give it a {@link MessageListener.ClosingDetector}</span>
 *  cml.setClosingDetector(<span style="color: #000080;">new</span> MessageListener.ClosingDetector(){
 *      <span style="color: #808000;">&#64;Override</span>
 *      <span style="color: #000080;">public boolean</span> isNotToBeClosed(Thread runningThr){
 *          <span style="color: #000080;">return</span> cml == runningThr && isToBeKeptOnline();
 *      }
 *  });
 *  <span style="color: #808080;">// and start listening to the messages</span>
 *  cml.start():
 *
 *  <span style="color: #808080;">// as soon as the application exits you'll have to set cml to null, so it terminates</span>
 *  cml = <span style="color: #000080;">null</span>;
 *
 *  <span style="color: #808080;">// in isToBeKeptOnline()-method you can give further conditions</span>
 *  <span style="color: #808080;">// to terminate listening to messages from the stream</span>
 * </code></pre>
 */
public class MessageListener extends Thread implements Connected{

    /**
     * Der Stream, von dem wir Nachrichten erhalten möchten
     */
    private DataInputStream input;

    /**
     * Der ClosingDetector, der die Terminierungsbedingung darstellt.<br>
     * Diese Variable muss mit der {@link #setClosingDetector(ClosingDetector)}-Methode initialisiert worden sein,
     * bevor der MessageListener gestartet werden kann.
     */
    private ClosingDetector closingDetector;
    /**
     * Der OnMessageReceivedListener, der den User benachrichtigt, sobald eine Nachricht erhalten wird
     */
    private OnMessageReceivedListener messageReceivedListener;

    /**
     * Der MessageSender, der für die Ping-Funktion an diesen MessageListener gebunden wurde
     */
    private MessageSender boundMessageSender;

    /**
     * Dieser Konstruktor erstellt einen MessageListener für den gegebenen DataInputStream
     *
     * @param input der DataInputStream mit dem kommuniziert werden soll
     */
    public MessageListener(DataInputStream input){
        this.input = input;
        this.closingDetector = null;
    }

    /**
     * Diese Methode setzt den {@link MessageListener.OnMessageReceivedListener},
     * mit welchem Nachrichten, die empfangen wurden verwertet werden können
     *
     * @param listener der Listener, der auf Nachrichten warten soll
     */
    public void setOnMessageReceivedListener(OnMessageReceivedListener listener){
        this.messageReceivedListener = listener;
    }

    /**
     * Diese Methode setzt den {@link MessageListener.ClosingDetector},
     * welcher die Terminierungsbedingung darstellt.<br>
     * Diese Methode muss aufgerufen werden, bevor der MessageListener gestartet wird.
     *
     * @param closingDetector der ClosingDetector, der die Terminierungsbedingung darstellt
     */
    public void setClosingDetector(ClosingDetector closingDetector){
        this.closingDetector = closingDetector;
    }

    /**
     * Diese Methode bindet den gegebenen {@link MessageSender} an dieses Objekt, um mit diesem für die Ping-Funktion zu kommunizieren
     *
     * @param sender Der MessageSender, der mit diesem MessageListener zusammenarbeiten soll
     */
    public void bindMessageSender(MessageSender sender){
        this.boundMessageSender = sender;
    }

    /**
     * @throws IllegalStateException falls kein {@link MessageListener.ClosingDetector} gegeben ist.
     */
    @Override
    public synchronized void start(){
        super.start();
    }

    @Override
    public void run(){
//      falls wir eine Terminierungsbedingung haben
        if(closingDetector != null){
//          loopen wir den Thread
            Thread currThr = Thread.currentThread();
            while(closingDetector.isNotToBeClosed(currThr)){
                try{
                    byte readByte = input.readByte();

//                  falls wir den Code für eine Nachricht erhalten
                    if(readByte == BYTECODE_MESSAGE){
//                      lesen wir diese
                        String name = input.readUTF();
                        String msg = input.readUTF();
//                      und feuern ein Event, falls die Nachricht nicht leer ist
                        if(messageReceivedListener != null && !msg.trim().equals("") && !name.trim().equals("")){
                            messageReceivedListener.onMessageReceived(name, msg);
                        }
                    }
//                  falls wir den Code für eine Server-Nachricht erhalten
                    else if(readByte == BYTECODE_SERVERMESSAGE){
//                      lesen wir diese aus
                        String msg = input.readUTF();

//                      und falls möglich / nötig geben wir ein Event an den OnMessageReceivedListener
                        if(messageReceivedListener != null && !msg.trim().equals("")){
                            messageReceivedListener.onServerMessageReceived(msg);
                        }
                    }
//                  falls die Nachricht eine Ping-Antwort ist
                    else if(readByte == BYTECODE_SERVERPING){
//                      und wir ein gebundenes MessageSender-Objekt haben
                        if(boundMessageSender != null){
//                          informieren wir dieses, dass eine Antwort auf den Ping angekommen ist
                            boundMessageSender.onPingReceived(true);
                        }
                    }
//                  falls die Nachricht die Anzahl an Nutzern ist
                    else if(readByte == BYTECODE_NAMESCOUNT){
//                      lesen wir diese aus
                        int count = input.readInt();

//                      und falls möglich / nötig geben wir ein Event an den OnMessageReceivedListener
                        if(messageReceivedListener != null){
                            messageReceivedListener.onUserCountReceived(count);
                        }
                    }
//                  falls die Nachricht die Namen der verbundenen Nutzern ist
                    else if(readByte == BYTECODE_NAMES){
//                      lesen wir diese aus
                        String[] names = input.readUTF().split(";");

//                      und falls möglich / nötig geben wir ein Event an den OnMessageReceivedListener
                        if(messageReceivedListener != null){
                            messageReceivedListener.onUserNamesReceived(names);
                        }
                    }
                }catch(IOException e){
                    // TODO: Maybe implement ConnectionLossListener?
                    e.printStackTrace();
                }

//              wir warten 10ms zwischen den Loops
                try{
                    Thread.sleep(10);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

//          sobald der Listener terminieren soll schließen wir dessen Stream
            try{
                input.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
//      falls keine Terminierungsbedingung vorhanden ist
        else{
//          werfen wir eine Exception, da es ohne Terminierungsbedingung nicht geht ^^
            throw new IllegalStateException("ClosingDetector is needed! Use MessageListener#setClosingDetector(ClosingDetector).");
        }
    }

    /**
     * Dieses Interface gibt ihnen die Möglichkeit empfangene Nachrichten auszuwerten
     */
    public interface OnMessageReceivedListener{

        /**
         * Diese Methode wird ausgeführt, sobald der MessageListener vom Server die gegebene Nachricht erhält
         *
         * @param name der Name des Users, der die Nachricht verschickt hat
         * @param msg  die Nachricht, die vom Server erhalten wurde
         */
        void onMessageReceived(String name, String msg);

        /**
         * Diese Methode wird ausgeführt, sobald der MessageListener vom Server eine Server-Nachricht bekommt
         *
         * @param msg Die Server-Nachricht, die empfangen wurde
         */
        void onServerMessageReceived(String msg);

        /**
         * Diese Methode wird ausgeführt, sobald der MessageListener vom Server die Anzahl an Nutzern bekommt
         *
         * @param count Die Anzahl an Nutzern, die mit dem Server verbunden sind
         */
        void onUserCountReceived(int count);

        /**
         * Diese Methode wird ausgeführt, sobald der MessageListener vom Server die Namen der Nutzer bekommt
         *
         * @param names Die Namen der verbundenen Nutzer
         */
        void onUserNamesReceived(String[] names);
    }

    /**
     * Dieses Interface stellt eine Terminierungsbedingung dar.
     *
     * @see MessageListener
     */
    public interface ClosingDetector{

        /**
         * In dieser Methode muss zurückgegeben werden, ob der MessageListener weiterhin auf Nachrichten warten soll, oder nicht
         *
         * @param runningThr Die Referenz zu dem Thread, der auf die Nachrichten wartet
         * @return ob der MessageListener weiterhin auf Nachrichten warten soll, oder nicht
         */
        boolean isNotToBeClosed(Thread runningThr);
    }

}
