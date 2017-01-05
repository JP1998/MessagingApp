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

package de.jeanpierrehotz.messaging.javaclient;

import com.sun.istack.internal.Nullable;
import de.jeanpierrehotz.messaging.javaclient.androidcompat.Preference;
import de.jeanpierrehotz.messaging.javaclient.androidcompat.Snackbar;
import de.jeanpierrehotz.messaging.javaclient.context.Context;
import de.jeanpierrehotz.messaging.javaclient.context.R;
import de.jeanpierrehotz.messaging.javaclient.messagescompat.MessageLoader;
import de.jeanpierrehotz.messaging.javaclient.ui.MessageListCellRenderer;
import de.jeanpierrehotz.messaging.javaclient.ui.MessageListModel;
import de.jeanpierrehotz.messaging.messages.Message;
import de.jeanpierrehotz.messaging.messages.ReceivedMessage;
import de.jeanpierrehotz.messaging.network.MessageListener;
import de.jeanpierrehotz.messaging.network.MessageSender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MessagesMain extends JFrame implements Context {

    /**
     * Die Position, die für alle Snackbars eingehalten werden soll
     */
    private static final int SNACKBARPOSITION = Snackbar.POSITION_BOTTOM_RIGHT;

    /**
     * Der derzeitige UserName
     */
    private String currentName;
    /**
     * Ob derzeitig die Anzahl gespeicherter Nachrichten limitiert werden soll
     */
    private boolean limitSavedMessages;
    /**
     * Die derzeitige Höchstanzahl an gespeicherten Nachrichten
     */
    private int limitSavedMessagesAmount;

    /**
     * Die Liste von Nachrichten, die erstmal nur für eine Sitzung gespeichert werden
     */
    private ArrayList<Message> mMessages;

    /**
     * Die Liste, die die Nachrichten anzeigt
     */
    private JList<Message> messageList;
    /**
     * Weil Java kacke ist müssen wir noch n ScrollPane um die JList machen xD
     */
    private JScrollPane messageWrapperScrollPane;
    /**
     * Das Modell, das Daten mit Liste verbindet (praktisch der Adapter in Android)
     */
    private MessageListModel messageListModel;

    /**
     * Das Eingabefeld für die Nachrichten
     */
    private JEditorPane messageEditText;
    /**
     * Der Button, der zum Senden da ist
     */
    private JButton connectedCountBtn;

    /**
     * Der Socket, der das Handy mit dem Server verbindet, der die Nachrichten verschickt
     */
    private Socket server;
    /**
     * Der MessageSender, der es ermöglicht Nachrichten einfach zu versenden
     */
    private MessageSender serverMessageSender;
    /**
     * Der PingListener wartet auf die Nachricht, ob ein Pind erfolgreich war, oder nicht
     */
    private MessageSender.PingListener pingListener = (connectionDetected) -> {
//      Wir zeigen dem User, ob der Ping erfolgreich war
        if(connectionDetected){
            new Snackbar.SnackbarFactory(MessagesMain.this, getString(R.string.pingmessage_connected), Snackbar.LENGTH_LONG)
                    .setBackgroundColor(getIntColor(R.color.connection_restored))
                    .setFontColor(getIntColor(R.color.connection_font))
                    .setPosition(SNACKBARPOSITION)
                    .setRelativeTo(MessagesMain.this)
                    .create()
                    .show();
        }else{
            new Snackbar.SnackbarFactory(MessagesMain.this, getString(R.string.pingmessage_disconnected), Snackbar.LENGTH_LONG)
                    .setBackgroundColor(getIntColor(R.color.connection_lost))
                    .setFontColor(getIntColor(R.color.connection_font))
                    .setPosition(SNACKBARPOSITION)
                    .setRelativeTo(MessagesMain.this)
                    .create()
                    .show();
        }
    };
    /**
     * Der MessageListener, der auf Nachrichten vom Server wartet, und sobald eine
     * empfangen wurde, ein OnMessageReceived-Event abschickt
     */
    private MessageListener serverMessageListener;
    /**
     * Der {@link MessageListener.OnMessageReceivedListener}, der
     * darauf wartet, dass eine Nachricht erhalten wird.
     */
    private MessageListener.OnMessageReceivedListener receivedListener = new MessageListener.OnMessageReceivedListener(){
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

        @Override
        public void onUserCountReceived(int count){
            setNameCount(count);
        }

        @Override
        public void onUserNamesReceived(String[] names){
            new UsersListDialog(MessagesMain.this, names).setVisible(true);
        }
    };
    /**
     * Der DisconnectListener, der dem User anzeigen soll, dass die Ver¬indung unterbrochen wurde, falls das der Fall ist
     */
    private MessageListener.OnDisconnectListener disconnectListener = new MessageListener.OnDisconnectListener() {
        @Override
        public void onDisconnect() {
            showDisconnectMessage();
            setNameCount(0);
            connected = false;
        }
    };
    /**
     * Dieser ClosingDetector frägt ab, ob der MessageListener aufhören soll auf Nachrichten zu hören.
     */
    private MessageListener.ClosingDetector closingDetector = new MessageListener.ClosingDetector(){
        @Override
        public boolean isNotToBeClosed(Thread runningThr){
//          Da serverMessageListener in #onStop auf null gesetzt wird, und geschleifte Threads auf den ausführenden
//          Thread abhängig gemacht werden soll, können wir Referenzen des ausführenden Threads und des Listeners vergleichen
            return serverMessageListener == runningThr;
        }
    };

    /**
     * Diese Variable zeigt an, ob der Server erreicht werden konnte, oder nicht
     */
    private boolean connected;
    /**
     * Diese Variable zeigt an, ob wir gerade versuchen uns mit dem Server zu verbinden
     */
    private boolean tryingToConnect;

    public MessagesMain(){
        this.setTitle(getString(R.string.app_title));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onStop();
                dispose();
            }
        });

        onCreate();

        setSize(getInt(R.integer.default_window_width), getInt(R.integer.default_window_height));
        setVisible(true);

        postCreate();
    }

    /**
     * Diese Methode erstellt alles nötige, bevor das Fenster sichtbar ist
     */
    private void onCreate() {
        setupUI();

        mMessages = MessageLoader.loadMessages(new Preference(getSystemFolder(), getString(R.string.messages_preference)));

        messageListModel = new MessageListModel(mMessages);
        messageList.setCellRenderer(new MessageListCellRenderer());
        messageList.setModel(messageListModel);

        Preference settingsPreference = new Preference(getSystemFolder(), getString(R.string.settings_preference));

        currentName = settingsPreference.getString(getString(R.string.pref_current_name), getString(R.string.default_username));
        limitSavedMessages = settingsPreference.getBoolean(getString(R.string.pref_limit_saved_messages), true);
        limitSavedMessagesAmount = settingsPreference.getInt(getString(R.string.pref_limit_saved_messages_amount), getInt(R.integer.default_messageslimitamount));


        connectToServer();
    }

    /**
     * Diese Methode stellt den Erstzustand des Fensters her
     */
    private void postCreate() {
        scrollToMessageBottom();
    }

    /**
     * Diese Methode wird ausgeführt sobald das Fenster geschlossen wird, und speichert alle Einstellungen und so
     */
    private void onStop() {
        if(connected) {
            serverMessageListener = null;
            serverMessageSender.close();
        }

        if(limitSavedMessages) {
            MessageLoader.saveMessages(new Preference(getSystemFolder(), getString(R.string.messages_preference)), mMessages, limitSavedMessagesAmount);
        } else {
            MessageLoader.saveMessages(new Preference(getSystemFolder(), getString(R.string.messages_preference)), mMessages);
        }
    }

    /**
     * Diese Methode versucht eine Verbindung zum Server aufzubauen
     */
    private void connectToServer(){
        if(!tryingToConnect && !connected){
            tryingToConnect = true;

//          Initialisierung der Serververbindung auf einem eigenen Thread, da Android
//          keine Netzwerkkommunikation auf dem MainThread erlaubt
            new Thread(() -> {
                try{
                    server = new Socket(getString(R.string.server_url), getInt(R.integer.server_port));

                    serverMessageSender = new MessageSender(new DataOutputStream(server.getOutputStream()));
                    serverMessageSender.setPingListener(pingListener);

                    serverMessageListener = new MessageListener(new DataInputStream(server.getInputStream()));
                    serverMessageListener.setClosingDetector(closingDetector);
                    serverMessageListener.setOnMessageReceivedListener(receivedListener);
                    serverMessageListener.setOnDisconnectListener(disconnectListener);
                    serverMessageListener.bindMessageSender(serverMessageSender);
                    serverMessageListener.start();

                    serverMessageSender.changeName(currentName);

                    connected = true;
                }catch(IOException e){
                    e.printStackTrace();
                    connected = false;
                    showDisconnectedErrorMessage();
                }

                tryingToConnect = false;
            }).start();
        }
    }

    /**
     * Diese Methode scrollt zum Boden der Nachrichten
     */
    private void scrollToMessageBottom(){
        messageListModel.notifyDataSetChanged();
        messageWrapperScrollPane.validate();

        JScrollBar bar = messageWrapperScrollPane.getVerticalScrollBar();
        if(bar != null){
            try{
                bar.setValue(bar.getMaximum());
            }catch(Exception e){
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
     * Diese Methode fügt eine gegebene Nachricht des gegebenen Typs zu der Liste hinzu, zeigt diese in der Liste an,
     * und lässt das RecyclerView zu dieser Nachricht scrollen.
     *
     * @param msg  Die Nachricht, die hinzugefügt werden soll
     * @param type Der Typ der Nachricht, die hinzugefügt werden soll
     */
    private void addMessage(String msg, Message.Type type) {
        testForDateNeeded(type);
        mMessages.add(new Message(msg, type));
        scrollToMessageBottom();
    }

    /**
     * Diese Methode fügt eine empfangene Nachricht zu der Liste hinzu, zeigt diese in der Liste an,
     * und lässt das RecyclerView zu dieser Nachricht scrollen.
     *
     * @param name Der Name des Users von dem die Nachricht stammt
     * @param msg  Die Nachricht, die hinzugefügt werden soll
     * @param type Der Typ der Nachricht, die hinzugefügt werden soll
     */
    private void addReceivedMessage(String name, String msg, Message.Type type) {
        testForDateNeeded(type);
        mMessages.add(new ReceivedMessage(name, msg, type));
        scrollToMessageBottom();
    }

    /**
     * Diese Methode sendet die Nachricht, die momentan im TextFeld steht an den Server, und
     * fügt diese zum Chat hinzu
     */
    private void sendMessage(){
        if(!messageEditText.getText().trim().equals("")){
            if(connected){
                String msg = messageEditText.getText();
                serverMessageSender.sendMessage(msg);
                addMessage(msg, Message.Type.Sent);
                messageEditText.setText("");
            } else {
                showDisconnectedErrorMessage();
            }
        }
    }

    /**
     * Diese Methode sendet den gegebenen String als Admin-/Server-Nachricht an den Server,
     * der diese an alle verbundenen Nutzer weiterleitet.
     *
     * @param msg Die Nachricht, die zu versenden ist
     */
    private void sendAdminMessage(String msg){
        if(connected) {
            serverMessageSender.sendAdminMessage(msg);
        } else {
            showDisconnectedErrorMessage();
        }
    }

    /**
     * Diese Methode leitet einen Ping des Servers ein
     */
    private void pingServer(){
        if(connected){
            serverMessageSender.pingServer();
        } else {
            showDisconnectedErrorMessage();
        }
    }

    /**
     * Diese Methode fordert die Anzahl an verbundenen Nutzern vom Server an
     */
    private void requestNameCount(){
        if(connected){
            serverMessageSender.sendNameCountRequest();
        } else {
            setNameCount(0);
            showDisconnectedErrorMessage();
        }
    }

    /**
     * Diese Methode aktualisiert die Anzahl an verbundenen Nutzern
     *
     * @param count die Anzahl an verbundenen Nutzern
     */
    private void setNameCount(int count) {
        connectedCountBtn.setText(String.format(getString(R.string.connectedusers_amount_template), count));
    }

    /**
     * Diese Methode fordert eine Liste der Namen der verbundenen Nutzern vom Server an
     */
    private void requestNames(){
        if(connected){
            serverMessageSender.sendNamesRequest();
        } else {
            showDisconnectedErrorMessage();
        }
    }

    /**
     * Diese Methode zeigt dem User, dass er gerade keine Verbindung zu dem Server hat, woraus dieser ableiten
     * können sollte, dass er keine Nachrichten versenden kann.
     * Außerdem wird ihm die Möglichkeit geboten zu versuchen sich mit dem Server zu verbinden.
     */
    private void showDisconnectedErrorMessage(){
        new Snackbar.SnackbarFactory(this, getString(R.string.errormessage_disconnected), Snackbar.LENGTH_LONG)
                .setBackgroundColor(getIntColor(R.color.connection_lost))
                .setFontColor(getIntColor(R.color.connection_font))
                .setActionFontColor(getIntColor(R.color.connection_actionfont))
                .setRelativeTo(this)
                .setPosition(SNACKBARPOSITION)
                .setAction(getString(R.string.errormessage_disconnected_actiontext), e -> connectToServer())
                .create()
                .show();
    }

    /**
     * Diese Methode zeigt dem User, dass sein eingegebener Name nicht gültig ist
     */
    private void showInvalidUserNameErrorMessage(){
        new Snackbar.SnackbarFactory(this, getString(R.string.errormessage_invalidusername), Snackbar.LENGTH_LONG)
                .setBackgroundColor(getIntColor(R.color.connection_lost))
                .setFontColor(getIntColor(R.color.connection_font))
                .setRelativeTo(this)
                .setPosition(SNACKBARPOSITION)
                .create()
                .show();
    }

    /**
     * Diese Methode zeigt dem User an, dass er das falsche Passwort eingegeben hat.
     */
    private void showInvalidAdminPasswordMessage(){
        new Snackbar.SnackbarFactory(this, getString(R.string.errormessage_invalidpassword), Snackbar.LENGTH_LONG)
                .setBackgroundColor(getIntColor(R.color.errormessage))
                .setFontColor(getIntColor(R.color.connection_font))
                .setRelativeTo(this)
                .setPosition(SNACKBARPOSITION)
                .create()
                .show();
    }

    /**
     * Diese Methode speichert die Einstellungen, und lässt das Einstellungs-Panel verschwinden
     * @param dialog der Dialog, der zum Ändern der Einstellungen benutzt wurde
     */
    private void saveSettings(SettingsDialog dialog){
        String newName = dialog.getWrittenName();

        if(!newName.trim().equals("")){
            if(!newName.trim().equals(currentName)){
                currentName = newName;

                if(connected){
                    serverMessageSender.changeName(newName);
                } else {
                    showDisconnectedErrorMessage();
                }
            }
        } else {
            showInvalidUserNameErrorMessage();
        }

        limitSavedMessages = dialog.isMessagesToBeLimited();
        limitSavedMessagesAmount = dialog.getMessagesLimit();

        new Preference(getSystemFolder(), getString(R.string.settings_preference))
                .edit()
                .putString(getString(R.string.pref_current_name), currentName)
                .putBoolean(getString(R.string.pref_limit_saved_messages), limitSavedMessages)
                .putInt(getString(R.string.pref_limit_saved_messages_amount), limitSavedMessagesAmount)
                .apply();

        dialog.dispose();
    }

    /**
     * Diese Methode wird ausgeführt, sobald der User eine Server-Nachricht versenden möchte,
     * und fordert diesen erst auf das Admin-Password einzugeben.
     */
    private void sendAdminMessage(){
        new AdminPasswordDialog(this, () -> new AdminMessageDialog(this).setVisible(true)).setVisible(true);
    }

    /**
     * Diese Methode zeigt dem User seine Einstellungen, und lässt ihn diese ändern
     */
    private void showSettings(){
        new SettingsDialog(this).setVisible(true);
    }

    /**
     * Diese Methode löscht alle Nachrichten
     */
    private void deleteMessages(){
        mMessages.clear();
        messageListModel.notifyDataSetChanged();
    }

    /**
     * Diese Methode zeigt dem User, dass seine Verbindung zum Server gerade unterbrochen wurde.
     * Außerdem wird ihm die Möglichkeit geboten zu versuchen sich mit dem Server zu verbinden.
     */
    private void showDisconnectMessage() {
        new Snackbar.SnackbarFactory(this, getString(R.string.errormessage_disconnect), Snackbar.LENGTH_LONG)
                .setBackgroundColor(getIntColor(R.color.errormessage))
                .setFontColor(getIntColor(R.color.connection_font))
                .setRelativeTo(this)
                .setPosition(SNACKBARPOSITION)
                .create()
                .show();
    }

    /**
     * Diese Methode initialisiert die UI
     */
    private void setupUI(){
        setLayout(new BorderLayout());
        add(setupToolbar(), BorderLayout.NORTH);
        add(setupMessagebar(), BorderLayout.SOUTH);
        add(setupMessageList(), BorderLayout.CENTER);
    }

    /**
     * Diese Methode erstellt die Toolbar, und gibt diese zurück
     *
     * @return die erstellte Toolbar
     */
    private JPanel setupToolbar(){
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(getColor(R.color.primarycolor));

        JLabel title = new JLabel(getString(R.string.toolbar_title));
        title.setForeground(getColor(R.color.white));
        toolbar.add(title, BorderLayout.WEST);

        JPanel toolbarBtnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbarBtnWrapper.setBackground(getColor(R.color.primarycolor));

        connectedCountBtn = new JButton();
        connectedCountBtn.addActionListener(e -> requestNameCount());
        connectedCountBtn.setBackground(getColor(R.color.primarycolor));
        connectedCountBtn.setForeground(getColor(R.color.white));
        connectedCountBtn.setBorder(null);
        toolbarBtnWrapper.add(connectedCountBtn);
        setNameCount(0);

        JButton listConnectedPeopleBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_users.png")));
        listConnectedPeopleBtn.addActionListener(e -> requestNames());
        listConnectedPeopleBtn.setBackground(getColor(R.color.primarycolor));
        listConnectedPeopleBtn.setForeground(getColor(R.color.white));
        listConnectedPeopleBtn.setBorder(null);
        toolbarBtnWrapper.add(listConnectedPeopleBtn);

        JButton sendAdminMessageBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_adminmessage.png")));
        sendAdminMessageBtn.addActionListener(e -> sendAdminMessage());
        sendAdminMessageBtn.setBackground(getColor(R.color.primarycolor));
        sendAdminMessageBtn.setForeground(getColor(R.color.white));
        sendAdminMessageBtn.setBorder(null);
        toolbarBtnWrapper.add(sendAdminMessageBtn);

        JButton pingServerBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_ping.png")));
        pingServerBtn.addActionListener(e -> pingServer());
        pingServerBtn.setBackground(getColor(R.color.primarycolor));
        pingServerBtn.setForeground(getColor(R.color.white));
        pingServerBtn.setBorder(null);
        toolbarBtnWrapper.add(pingServerBtn);

        JButton showSettingsBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_settings.png")));
        showSettingsBtn.addActionListener(e -> showSettings());
        showSettingsBtn.setBackground(getColor(R.color.primarycolor));
        showSettingsBtn.setForeground(getColor(R.color.white));
        showSettingsBtn.setBorder(null);
        toolbarBtnWrapper.add(showSettingsBtn);

        JButton deleteMessagesBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_deletemessages.png")));
        deleteMessagesBtn.addActionListener(e -> deleteMessages());
        deleteMessagesBtn.setBackground(getColor(R.color.primarycolor));
        deleteMessagesBtn.setForeground(getColor(R.color.white));
        deleteMessagesBtn.setBorder(null);
        toolbarBtnWrapper.add(deleteMessagesBtn);

        toolbar.add(toolbarBtnWrapper, BorderLayout.EAST);
        return toolbar;
    }

    /**
     * Diese Methode erstellt die Messagebar, und gibt diese zurück
     *
     * @return die erstellte Messagebar
     */
    private JPanel setupMessagebar(){
        JPanel messagesWrapper = new JPanel(new BorderLayout());

        messageEditText = new JEditorPane();
        messageEditText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if(!e.isAltDown()) {
                        sendMessage();
                        e.consume();
                    } else if(e.getSource() instanceof JEditorPane) {
                        JEditorPane src = (JEditorPane) e.getSource();
                        src.setText(src.getText() + System.lineSeparator());
                    }
                }
            }
        });
        JScrollPane messageEditTextWrapper = new JScrollPane(messageEditText, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagesWrapper.add(messageEditTextWrapper, BorderLayout.CENTER);

        JButton sendBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("post_control_message.png")));
        sendBtn.setBackground(getColor(R.color.accentcolor));
        sendBtn.addActionListener(e -> sendMessage());
        messagesWrapper.add(sendBtn, BorderLayout.EAST);

        return messagesWrapper;
    }

    /**
     * Diese Methode erstellt die Nachrichtenliste, und gibt diese zurück
     *
     * @return die erstellte Nachrichtenliste
     */
    private JScrollPane setupMessageList(){
        messageList = new JList<>();

        messageWrapperScrollPane = new JScrollPane(messageList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageWrapperScrollPane.setViewportBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageWrapperScrollPane.setWheelScrollingEnabled(true);

        return messageWrapperScrollPane;
    }

    /**
     * Diese Methode gibt ihnen den absoluten Pfad des Ordners, in dem sich das derzeit ausgeführte
     * Programm befindet
     *
     * @return den Ordner, in dem dieses Programm liegt
     */
    private static String getSystemFolder() {
        return new File("").getAbsolutePath();
    }

    /**
     * Diese Methode erstellt aus dem gegebenen char-Array einen String mit dem Inhalt des Arrays
     *
     * @param array Das Array, aus dem man den String erstellen soll
     * @return Der String mit dem Inhalt des Arrays
     */
    private static String convertToString(char[] array){
        return new String(array);
    }

    public static void main(String[] args) {
        new MessagesMain();
    }

    /**
     * Diese Klasse ist ein Dialog, der dem User die Einstellungen zeigt, und ihn diese verändern lässt
     */
    private class SettingsDialog extends JDialog {

        private static final int WIDTH = 500;
        private static final int HEIGHT = 150;

        private JTextField userNameTextField;
        private JSlider messagesLimitSlider;
        private JCheckBox messagesLimitCheckbox;

        public SettingsDialog(Frame owner) {
            super(owner, getString(R.string.dialog_settings_caption), true);

            this.setLayout(new BorderLayout());

            JPanel centerWrapper = new JPanel(new BorderLayout());

            userNameTextField = new JTextField(currentName);
            centerWrapper.add(userNameTextField, BorderLayout.NORTH);

            messagesLimitCheckbox = new JCheckBox(getString(R.string.dialog_settings_limitmessages_hint), limitSavedMessages);
            centerWrapper.add(messagesLimitCheckbox, BorderLayout.CENTER);

            messagesLimitSlider = new JSlider(JSlider.HORIZONTAL, 0, 10000, limitSavedMessagesAmount);
            centerWrapper.add(messagesLimitSlider, BorderLayout.SOUTH);

            this.add(centerWrapper, BorderLayout.CENTER);

            this.add(new JPanel(), BorderLayout.NORTH);
            this.add(new JPanel(), BorderLayout.EAST);
            this.add(new JPanel(), BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton abortButton = new JButton(getString(R.string.dialog_abort));
            abortButton.addActionListener(e -> dispose());
            buttonPanel.add(abortButton);

            JButton saveButton = new JButton(getString(R.string.dialog_save));
            saveButton.addActionListener(e -> saveSettings(this));
            buttonPanel.add(saveButton);

            this.add(buttonPanel, BorderLayout.SOUTH);

            this.setResizable(false);
            this.setBounds(
                    owner.getX() + (owner.getWidth() - WIDTH) / 2,
                    owner.getY() + (owner.getHeight() - HEIGHT) / 2,
                    WIDTH,
                    HEIGHT
            );
        }

        /**
         * Diese Methode gibt ihnen den Namen, der derzeitig im TextFeld steht
         *
         * @return der derzeitig geschriebene Benutzername
         */
        public String getWrittenName(){
            return userNameTextField.getText();
        }

        /**
         * Diese Methode gibt ihnen, ob die Nachrichtenanzahl limitiert werden soll
         *
         * @return ob die Nachrichtenanzahl limitiert werden soll
         */
        public boolean isMessagesToBeLimited(){
            return messagesLimitCheckbox.isSelected();
        }

        /**
         * Diese Methode gibt ihnen die Anzahl auf die die Nachrichtenanzahl limitiert werden soll
         *
         * @return die Anzahl auf die die Nachrichtenanzahl limitiert werden soll
         */
        public int getMessagesLimit(){
            return messagesLimitSlider.getValue();
        }
    }

    /**
     * Dieser Dialog lässt den User eine Adminnachricht versenden.
     */
    private class AdminMessageDialog extends JDialog {

        private static final int WIDTH = 300;
        private static final int HEIGHT = 100;

        public AdminMessageDialog(Frame owner) {
            super(owner, getString(R.string.dialog_adminmessage_caption), true);

            this.setLayout(new BorderLayout());

            final JTextPane messageTextField = new JTextPane();
            this.add(messageTextField, BorderLayout.CENTER);

            this.add(new JPanel(), BorderLayout.NORTH);
            this.add(new JPanel(), BorderLayout.EAST);
            this.add(new JPanel(), BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton abortButton = new JButton(getString(R.string.dialog_abort));
            abortButton.addActionListener(e -> dispose());
            buttonPanel.add(abortButton);

            JButton okButton = new JButton(getString(R.string.dialog_send));
            okButton.addActionListener(e -> {
                dispose();
                if(!messageTextField.getText().trim().equals("")) {
                    sendAdminMessage(messageTextField.getText());
                }
            });
            buttonPanel.add(okButton);

            this.add(buttonPanel, BorderLayout.SOUTH);

            this.setResizable(false);
            this.setBounds(
                    owner.getX() + (owner.getWidth() - WIDTH) / 2,
                    owner.getY() + (owner.getHeight() - HEIGHT) / 2,
                    WIDTH,
                    HEIGHT
            );
        }
    }

    /**
     * Dieser Dialog lässt den User das Admin-Passwort eingeben, und führt, falls das Passwort richtig war
     * das gegebene Runnable aus.
     */
    private class AdminPasswordDialog extends JDialog {

        private static final int WIDTH = 300;
        private static final int HEIGHT = 100;

        public AdminPasswordDialog(Frame owner, Runnable adminExecutable) {
            super(owner, getString(R.string.dialog_adminpassword_caption), true);

            this.setLayout(new BorderLayout());

            final JPasswordField passwordField = new JPasswordField();
            passwordField.setBackground(getColor(R.color.background_adminpassworddialog));
            this.add(passwordField, BorderLayout.CENTER);

            JPanel gapN = new JPanel();
            gapN.setBackground(getColor(R.color.background_adminpassworddialog));
            this.add(gapN, BorderLayout.NORTH);

            JPanel gapE = new JPanel();
            gapE.setBackground(getColor(R.color.background_adminpassworddialog));
            this.add(gapE, BorderLayout.EAST);

            JPanel gapW = new JPanel();
            gapW.setBackground(getColor(R.color.background_adminpassworddialog));
            this.add(gapW, BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(getColor(R.color.background_adminpassworddialog));

            JButton abortButton = new JButton(getString(R.string.dialog_abort));
            abortButton.addActionListener(e -> dispose());
            buttonPanel.add(abortButton);

            JButton okButton = new JButton(getString(R.string.dialog_ok));
            okButton.addActionListener(e -> {
                dispose();
                if(convertToString(passwordField.getPassword()).equals(getString(R.string.adminpassword))) {
                    if(adminExecutable != null){
                        adminExecutable.run();
                    }
                } else {
                    showInvalidAdminPasswordMessage();
                }
            });
            buttonPanel.add(okButton);

            this.add(buttonPanel, BorderLayout.SOUTH);

            this.setResizable(false);
            this.setBounds(
                    owner.getX() + (owner.getWidth() - WIDTH) / 2,
                    owner.getY() + (owner.getHeight() - HEIGHT) / 2,
                    WIDTH,
                    HEIGHT
            );
        }
    }

    /**
     * Dieser Dialog zeigt die gegebene Liste an Benutzernamen an.
     */
    private class UsersListDialog extends JDialog {

        private static final int WIDTH = 500;
        private static final int HEIGHT = 350;

        public UsersListDialog(Frame owner, String[] userNames) {
            super(owner, getString(R.string.dialog_userlist_caption), true);

            this.setLayout(new BorderLayout());

            JList<String> userList = new JList<>(userNames);
            JScrollPane scrollWrapper = new JScrollPane(userList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            this.add(scrollWrapper, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton okButton = new JButton(getString(R.string.dialog_ok));
            okButton.addActionListener(e -> dispose());
            buttonPanel.add(okButton);

            this.add(buttonPanel, BorderLayout.SOUTH);

            this.setResizable(false);
            this.setBounds(
                    owner.getX() + (owner.getWidth() - WIDTH) / 2,
                    owner.getY() + (owner.getHeight() - HEIGHT) / 2,
                    WIDTH,
                    HEIGHT
            );
        }
    }
}
