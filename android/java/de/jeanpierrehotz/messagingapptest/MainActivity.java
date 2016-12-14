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

package de.jeanpierrehotz.messagingapptest;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;

// See gradle dependencies for following classes
// compile 'com.facebook.rebound:rebound:0.3.8'
// compile 'com.tumblr:backboard:0.1.0'
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.tumblr.backboard.Actor;
import com.tumblr.backboard.MotionProperty;
import com.tumblr.backboard.imitator.SpringImitator;
import com.tumblr.backboard.performer.Performer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.jeanpierrehotz.messagingapptest.funuistuff.ColoredSnackbar;
import de.jeanpierrehotz.messagingapptest.messages.Message;
import de.jeanpierrehotz.messagingapptest.messages.MessageAdapter;
import de.jeanpierrehotz.messagingapptest.messages.ReceivedMessage;
import de.jeanpierrehotz.messagingapptest.network.ClientMessageListener;
import de.jeanpierrehotz.messagingapptest.network.ClientMessageSender;

public class MainActivity extends AppCompatActivity{

    /**
     * Das RelativeLayout, das die Einstellungen zeigt
     */
    private RelativeLayout settingsBottomSheetLayout;
    /**
     * Das BottomSheetBehavior, das die Einstellungen zeigt / versteckt.
     */
    private BottomSheetBehavior<RelativeLayout> settingsBottomSheetBehaviour;

    /**
     * Das EditText, in dem der User seinen UserNamen eingeben kann
     */
    private EditText nameTextView;
    /**
     * Der derzeitige UserName
     */
    private String currentName;


    /**
     * Die Liste von Nachrichten, die erstmal nur für eine Sitzung gespeichert werden
     */
    private ArrayList<Message> mMessages;

    /**
     * Das RecyclerView, das die Nachrichten anzeigt
     */
    private RecyclerView mMessagesView;
    /**
     * Der Adapter für die Nachrichten
     */
    private MessageAdapter mMessagesAdapter;
    /**
     * Der LayoutManager, der das Layout des RecyclerViews gut macht
     */
    private LinearLayoutManager mLayoutManager;

    /**
     * Der FloatingActionButton, mit dem eine Nachricht gesendet wird
     */
    private FloatingActionButton mSendBtn;
    /**
     * Das EditText, in dem die Nachricht eingegeben werden soll
     */
    private EditText mSendEditText;

    /**
     * Der Socket, der das Handy mit dem Server verbindet, der die Nachrichten verschickt
     */
    private Socket client;
    /**
     * Der ClientMessageSender, der es ermöglicht Nachrichten einfach zu versenden
     */
    private ClientMessageSender clientMessageSender;
    /**
     * Der PingListener wartet auf die Nachricht, ob ein Pind erfolgreich war, oder nicht
     */
    private ClientMessageSender.PingListener pingListener = new ClientMessageSender.PingListener(){
        @Override
        public void onConnectionDetected(boolean connected){
//          Wir zeigen dem User, ob der Ping erfolgreich war
            if(connected){
                ColoredSnackbar.make(
                        ContextCompat.getColor(MainActivity.this, R.color.colorConnectionRestored),
                        mMessagesView,
                        getString(R.string.pingresult_connected),
                        Snackbar.LENGTH_SHORT,
                        ContextCompat.getColor(MainActivity.this, R.color.colorConnectionFont)
                ).show();
            }else{
                ColoredSnackbar.make(
                        ContextCompat.getColor(MainActivity.this, R.color.colorConnectionLost),
                        mMessagesView,
                        getString(R.string.pingresult_timedout),
                        Snackbar.LENGTH_SHORT,
                        ContextCompat.getColor(MainActivity.this, R.color.colorConnectionFont)
                ).show();
            }
        }
    };
    /**
     * Der ClientMessageListener, der auf Nachrichten vom Server wartet, und sobald eine
     * empfangen wurde, ein OnMessageReceived-Event abschickt
     */
    private ClientMessageListener clientMessageListener;
    /**
     * Der {@link ClientMessageListener.OnMessageReceivedListener}, der
     * darauf wartet, dass eine Nachricht erhalten wird.
     */
    private ClientMessageListener.OnMessageReceivedListener receivedListener = new ClientMessageListener.OnMessageReceivedListener(){
        @Override
        public void onMessageReceived(String name, String msg){
//          Die Nachricht wird einfach hinzugefügt, wobei die Nachricht immer empfangen
//          (= Message.Type.Received) wurde
            addReceivedMessage(name, msg, Message.Type.Received);
        }

        @Override
        public void onServerMessageReceived(String msg){
            addMessage(msg, Message.Type.Announcement);
        }
    };
    /**
     * Dieser ClosingDetector frägt ab, ob der ClientMessageListener aufhören soll auf Nachrichten zu hören.
     */
    private ClientMessageListener.ClosingDetector closingDetector = new ClientMessageListener.ClosingDetector(){
        @Override
        public boolean isNotToBeClosed(Thread runningThr){
//          Da clientMessageListener in #onStop auf null gesetzt wird, und geschleifte Threads auf den ausführenden
//          Thread abhängig gemacht werden soll, können wir Referenzen des ausführenden Threads und des Listeners vergleichen
            return clientMessageListener == runningThr && online;
        }
    };

    /**
     * Diese Variale zeigt an, ob das Handy derzeit online ist, oder nicht
     */
    private boolean online;
    /**
     * Diese Variable zeigt an, ob der Server erreicht werden konnte, oder nicht
     */
    private boolean serverReached;
    /**
     * Dieses Callback soll uns so schnell wie möglich und automatisch wieder mit dem
     * Server verbinden.
     */
    private ConnectivityManager.NetworkCallback internetCallback = new ConnectivityManager.NetworkCallback(){
        @Override
        public void onAvailable(Network network){
            super.onAvailable(network);

            if(!online){
                ColoredSnackbar.make(
                        ContextCompat.getColor(MainActivity.this, R.color.colorConnectionRestored),
                        mMessagesView,
                        getString(R.string.internetmessage_reconnect),
                        Snackbar.LENGTH_SHORT,
                        ContextCompat.getColor(MainActivity.this, R.color.colorConnectionFont)
                ).show();
                connectToServer();
            }
            online = true;
        }

        @Override
        public void onLost(Network network){
            super.onLost(network);

            ColoredSnackbar.make(
                    ContextCompat.getColor(MainActivity.this, R.color.colorConnectionLost),
                    mMessagesView,
                    getString(R.string.internetmessage_disconnect),
                    Snackbar.LENGTH_SHORT,
                    ContextCompat.getColor(MainActivity.this, R.color.colorConnectionFont)
            ).show();
            online = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//      Initialisierung der Nachrichtenliste
        mMessages = new ArrayList<>();
//      TODO: Evtl. gespeicherte Nachrichten laden?

//      Initialisierug der Einstellungen
        settingsBottomSheetLayout = (RelativeLayout) findViewById(R.id.settingsBottomSheet);
        settingsBottomSheetBehaviour = BottomSheetBehavior.from(settingsBottomSheetLayout);

        nameTextView = (EditText) findViewById(R.id.userNameEditText);

        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_settings_preference), MODE_PRIVATE);
        if(prefs.getBoolean(getString(R.string.prefs_settings_firstLaunch), true)) {
            currentName = getString(R.string.defaultUserName);

            prefs.edit().putBoolean(getString(R.string.prefs_settings_firstLaunch), false).apply();
            settingsBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            currentName = prefs.getString(getString(R.string.prefs_settings_currentname), getString(R.string.defaultUserName));
            settingsBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        nameTextView.setText(currentName);

//      Intialisierung der UI
        mMessagesView = (RecyclerView) findViewById(R.id.messagesView);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        mMessagesView.setLayoutManager(mLayoutManager);

        mMessagesAdapter = new MessageAdapter(mMessages);
        mMessagesView.setAdapter(mMessagesAdapter);

        mSendBtn = (FloatingActionButton) findViewById(R.id.sendBtn);
        mSendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sendMessage();
            }
        });

        mSendEditText = (EditText) findViewById(R.id.sendEditText);

//      we'll check for internet, and if there is any, we'll start the connectivity
//      if not we'll simply state that we're offline
        if(isConnectionAvailable()){
            online = true;
            connectToServer();
        }else{
            online = false;
        }

//      and we'll set up the NetworkCallback that is to reconnect us as soon as possible
        ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).registerNetworkCallback(
                new NetworkRequest.Builder().build(),
                internetCallback
        );

//      Zum Schluss initialisieren wir die Funktion, mit der wir den FloatingActionButton dragbar machen :D
        buildFollowingFun();
    }

    /**
     * Diese Methode erstellt die Funktion, mit der man deL FloatingActionButton draggen
     * kann, woraufhin die CardView für die Nachricht folgt
     */
    private void buildFollowingFun(){
//      Frag einfach nicht, das ist Copy-Paste aus einem anderen
//      Projekt von mir, und funktioniert einfach
        FloatingActionButton fab = mSendBtn;
        CardView cv = (CardView) findViewById(R.id.sendCardView);

        final SpringSystem springSystem = SpringSystem.create();

        final Spring fabSpringX = springSystem.createSpring();
        final Spring fabSpringY = springSystem.createSpring();

        new Actor.Builder(springSystem, fab)
                .addMotion(fabSpringX, MotionProperty.X)
                .addMotion(fabSpringY, MotionProperty.Y)
                .build();

        final Spring cvFollowerSpringX = springSystem.createSpring();
        final Spring cvFollowerSpringY = springSystem.createSpring();

        cvFollowerSpringX.addListener(new Performer(cv, View.TRANSLATION_X));
        cvFollowerSpringY.addListener(new Performer(cv, View.TRANSLATION_Y));

        final SpringImitator cvImitatorX = new SpringImitator(cvFollowerSpringX);
        final SpringImitator cvImitatorY = new SpringImitator(cvFollowerSpringY);

        fabSpringX.addListener(cvImitatorX);
        fabSpringY.addListener(cvImitatorY);
    }

    @Override
    protected void onResume(){
        super.onResume();

//      we'll check for internet, and if there is any, we'll start the connectivity
//      if not we'll simply state that we're offline
        if(isConnectionAvailable()){
            online = true;
            connectToServer();
        }else{
            online = false;
        }

//      and we'll set up the NetworkCallback that is to reconnect us as soon as possible
        ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).registerNetworkCallback(
                new NetworkRequest.Builder().build(),
                internetCallback
        );
    }

    /**
     * Diese Methode ermittelt, ob derzeit Internet verfügbar ist.
     *
     * @return ob Internet verfügbar ist
     */
    private boolean isConnectionAvailable(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        Network[] networks = manager.getAllNetworks();
        for(Network net : networks){
            if(manager.getNetworkInfo(net).isAvailable()){
                return true;
            }
        }
        return false;
    }

    /**
     * Diese Methode baut eine Verbindung zum Server auf, von dem wir die Nachrichten bekommen
     */
    private void connectToServer(){
//      Initialisierung der Serververbindung auf einem eigenen Thread, da Android
//      keine Netzwerkkommunikation auf dem MainThread erlaubt
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    client = new Socket(getString(R.string.serverinfo_url), getResources().getInteger(R.integer.serverinfo_port));

                    clientMessageSender = new ClientMessageSender(new DataOutputStream(client.getOutputStream()));
                    clientMessageSender.setPingListener(pingListener);

                    clientMessageListener = new ClientMessageListener(new DataInputStream(client.getInputStream()));
                    clientMessageListener.setClosingDetector(closingDetector);
                    clientMessageListener.setOnMessageReceivedListener(receivedListener);
                    clientMessageListener.bindMessageSender(clientMessageSender);
                    clientMessageListener.start();

                    clientMessageSender.changeName(currentName);

                    serverReached = true;
                }catch(SocketException e){
                    e.printStackTrace();
                    serverReached = false;
                    showDisconnectedErrorMessage();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop(){
        super.onStop();

        if(online && serverReached){
            try{
//              Sobald die Activity endet lassen wir den ClientMessageListener auslaufen, indem wir clientMessageListener auf null setzen,
//              wodurch closingDetector#isNotToBeClosed(Thread) auf false gesetzt wird
                clientMessageListener = null;
//              Und schließen alle Streams
                client.close();
                clientMessageSender.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Diese Methode überprüft, ob wir für eine neue Nachricht des gegebenen Typs eine Datumsanzeige benötigen.
     * Eine Datumsanzeige ist benötigt, falls der Tag der letzten Nachricht, die keine Announcement-Nachricht ist nicht heute ist,
     * und der Typ der Nachricht selbst nicht Announcement ist.<br>
     * Falls die Datumsanzeige benötigt ist, wird diese von dieser Methode hinzugefügt.
     *
     * @param type Der Typ der hinzuzufügenden Nachricht
     */
    private void testForDateNeeded(Message.Type type) {
//      falls wir kein Announcement anzeigen wollen
        if(type != Message.Type.Announcement){
//          und keine Nachricht da ist
            if(mMessages.size() == 0 || getLastMessage() == null){
//              fügen wir das heutige Datum definitiv als Announcement hinzu
                addMessage(new SimpleDateFormat(getString(R.string.dateannouncement_template)).format(Calendar.getInstance().getTime()), Message.Type.Announcement);
            }else{
//              falls eine Nachricht da ist, bekommen wir deren Zeit
                Calendar lastMessageTime = Calendar.getInstance();
                lastMessageTime.setTimeInMillis(getLastMessage().getTime());
//              und die derzeitige Systemzeit
                Calendar currentTime = Calendar.getInstance();

//              und falls diese nicht am selben Tag sind
                if(notSameDay(lastMessageTime, currentTime)){
//                  fügen wir das heutige Datum als Announcement hinzu
                    addMessage(new SimpleDateFormat(getString(R.string.dateannouncement_template)).format(Calendar.getInstance().getTime()), Message.Type.Announcement);
                }
            }
        }
    }

    /**
     * Diese Methode fügt eine gegebene Nachricht des gegebenen Typs zu der Liste hinzu, zeigt diese in der Liste an,
     * und lässt das RecyclerView zu dieser Nachricht scrollen.
     *
     * @param msg  Die Nachricht, die hinzugefügt werden soll
     * @param type Der Typ der Nachricht, die hinzugefügt werden soll
     */
    private void addMessage(final String msg, final Message.Type type){
        testForDateNeeded(type);

//      Da diese Methode von anderen Threads aufgerufen werden können, wir allerdings auf Views zugreifen
//      müssen wir das Ganze mit der Methode runOnUiThread(Runnable) ausführen
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
//              wir fügen die Nachricht zur Liste hinzu
                mMessages.add(new Message(msg, type));
//              zeigen es sie im RecyclerView an
                mMessagesAdapter.notifyItemInserted(mMessages.size() - 1);
//              und scrollen zu der Nachricht
                mMessagesView.smoothScrollToPosition(mMessages.size() - 1);
            }
        });
    }

    /**
     * Diese Methode fügt eine empfangene Nachricht zu der Liste hinzu, zeigt diese in der Liste an,
     * und lässt das RecyclerView zu dieser Nachricht scrollen.
     *
     * @param name Der Name des Users von dem die Nachricht stammt
     * @param msg  Die Nachricht, die hinzugefügt werden soll
     * @param type Der Typ der Nachricht, die hinzugefügt werden soll
     */
    private void addReceivedMessage(final String name, final String msg, final Message.Type type){
        testForDateNeeded(type);

//      Da diese Methode von anderen Threads aufgerufen werden können, wir allerdings auf Views zugreifen
//      müssen wir das Ganze mit der Methode runOnUiThread(Runnable) ausführen
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
//              wir fügen die Nachricht zur Liste hinzu
                mMessages.add(new ReceivedMessage(name, msg, type));
//              zeigen es sie im RecyclerView an
                mMessagesAdapter.notifyItemInserted(mMessages.size() - 1);
//              und scrollen zu der Nachricht
                mMessagesView.smoothScrollToPosition(mMessages.size() - 1);
            }
        });
    }

    /**
     * Diese Methode ermittelt, ob zwei Calendar-Objekte eine Zeit an verschiedenen Tagen repräsentieren, oder nicht.
     * Die Parameter sind beliebig vertauschbar; deren Reihenfolge hat keinen Einfluss auf den
     *
     * @param time1 Das erste Calendar-Objekt
     * @param time2 Das erste Calendar-Ojekt
     * @return Ob die Calendar-Ojekte verschiedene Tage repräsentieren, oder nicht
     */
    private boolean notSameDay(Calendar time1, Calendar time2){
        return time1.get(Calendar.DAY_OF_MONTH) != time2.get(Calendar.DAY_OF_MONTH)
                || time1.get(Calendar.MONTH) != time2.get(Calendar.MONTH)
                || time1.get(Calendar.YEAR) != time2.get(Calendar.YEAR);
    }

    /**
     * Diese Methode gibt ihnen die letzte Nachricht, welche nicht vom Typ Announcement ist.
     * Falls keine solche Nachricht existiert wird {@code null} zurückgegeben.
     *
     * @return die letzte Nachricht, welche nicht vom Typ Announcement ist; {@code null}, falls keine existiert
     */
    @Nullable
    private Message getLastMessage(){
//      wir fangen bei der letzten Nachricht an
        for(int i = mMessages.size() - 1; i >= 0; i--){
//          bei der ersten Nachricht, die nicht vom Typ Announcement ist
            if(mMessages.get(i).getMessageType() != Message.Type.Announcement){
//              geben wir diese zurück
                return mMessages.get(i);
            }
        }
//      falls wir keine gefunden haben geben wir null zurück
        return null;
    }

    /**
     * Diese Klasse sendet die Nachricht, die momentan im TextFeld steht an den Server, und
     * fügt diese zum Chat hinzu
     */
    private void sendMessage(){
//      Wir verhindern leere Nachrichten
        if(!mSendEditText.getText().toString().trim().equals("")){
            if(online && serverReached){
//              bekommen die zu verschickende Nachricht
                String msg = mSendEditText.getText().toString();

//              Senden sie an den Server
                clientMessageSender.send(msg);

//              fügen sie zum Chat hinzu
                addMessage(msg, Message.Type.Sent);
//              und löschen das TextFeld
                mSendEditText.setText("");
            }else if(online){
                showDisconnectedErrorMessage();
            }else{
                showOfflineErrorMessage();
            }
        }
    }

    /**
     * Diese Methode leitet einen Ping des Servers ein
     */
    private void pingServer(){
        if(online && serverReached){
            clientMessageSender.pingServer();
        }else if(online){
            showDisconnectedErrorMessage();
        }else{
            showOfflineErrorMessage();
        }
    }

    /**
     * Diese Methode zeigt dem User, dass er gerade offline ist, woraus dieser ableiten
     * können sollte, dass er keine Nachrichten versenden kann.
     */
    private void showOfflineErrorMessage(){
        ColoredSnackbar.make(
                ContextCompat.getColor(this, R.color.colorConnectionLost),
                mMessagesView,
                getString(R.string.internetmessage_offline),
                Snackbar.LENGTH_SHORT,
                ContextCompat.getColor(this, R.color.colorConnectionFont)
        ).show();
    }

    /**
     * Diese Methode zeigt dem User, dass er gerade keine Verbindung zu dem Server hat, woraus dieser ableiten
     * können sollte, dass er keine Nachrichten versenden kann.
     * Außerdem wird ihm die Möglichkeit geboten zu versuchen sich mit dem Server zu verbinden.
     */
    private void showDisconnectedErrorMessage(){
        ColoredSnackbar.make(
                ContextCompat.getColor(this, R.color.colorConnectionLost),
                mMessagesView,
                getString(R.string.internetmessage_serverdown),
                Snackbar.LENGTH_SHORT,
                ContextCompat.getColor(this, R.color.colorConnectionFont)
        ).setAction(R.string.internetmessage_serverdown_reconnect, new View.OnClickListener(){
            @Override
            public void onClick(View view){
                connectToServer();
            }
        }).show();
    }

    /**
     * Diese Methode zeigt dem User, dass sein eingegebener Name nicht gültig ist
     */
    private void showInvalidUserNameErrorMessage(){
        ColoredSnackbar.make(
                ContextCompat.getColor(this, R.color.colorConnectionLost),
                mMessagesView,
                getString(R.string.settingsmessage_invalidusername),
                Snackbar.LENGTH_SHORT,
                ContextCompat.getColor(this, R.color.colorConnectionFont)
        ).show();
    }

    /**
     * Diese Methode speichert die Einstellungen, und lässt das Einstellungs-Panel verschwinden
     * @param v das View, das geklickt wurde
     */
    public void saveSettings(View v) {
//      wir bekommen den neuen Namen
        String newName = nameTextView.getText().toString();

//      verhindern den alten und alle leere Namen
        if(!newName.trim().equals(currentName) && !newName.trim().equals("")) {

//          übernehmen den neuen Namen, und speichern ihn intern
            currentName = newName;
            getSharedPreferences(getString(R.string.prefs_settings_preference), MODE_PRIVATE).edit().putString(getString(R.string.prefs_settings_currentname), currentName).apply();

            if(online) {
                if(serverReached) {
//                  falls wir mit dem Server verbunden sind schicken wir den Namen an den Server;
//                  ansonsten geben wir die entsprechende Fehlermeldung aus
                    clientMessageSender.changeName(newName);
                } else {
                    showDisconnectedErrorMessage();
                }
            } else {
                showOfflineErrorMessage();
            }
        } else {
            showInvalidUserNameErrorMessage();
        }

//      und lassen das Einstellungs-Panel verschwinden
        settingsBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
//      Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
//      Handle action bar item clicks here. The action bar will
//      automatically handle clicks on the Home/Up button, so long
//      as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//      noinspection SimplifiableIfStatement
        if(id == R.id.action_ping){
            pingServer();
            return true;
        } else if(id == R.id.action_settings) {
            settingsBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        return super.onOptionsItemSelected(item);
    }
}
