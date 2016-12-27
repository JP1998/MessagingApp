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
import de.jeanpierrehotz.messaging.javaclient.androidcompat.ErrorLogger;
import de.jeanpierrehotz.messaging.javaclient.androidcompat.Preference;
import de.jeanpierrehotz.messaging.javaclient.androidcompat.Snackbar;
import de.jeanpierrehotz.messaging.javaclient.messagescompat.MessageLoader;
import de.jeanpierrehotz.messaging.javaclient.ui.Colors;
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

/**
 *
 */
public class MessagesMain extends JFrame {

    private static final String MESSAGES_PREFERENCE = "Messaging_Messages_SaveFile";
    private static final String SETTINGS_PREFERENCE = "Messaging_Settings_SaveFile";

    private static final String PREF_CURRENT_NAME = "private static final String PREF_CURRENT_NAME";
    private static final String PREF_LIMIT_SAVED_MESSAGES = "private static final String PREF_LIMIT_SAVED_MESSAGES";
    private static final String PREF_LIMIT_SAVED_MESSAGES_AMOUNT = "private static final String PREF_LIMIT_SAVED_MESSAGES_AMOUNT";

    private static final String SERVER_URL = "verelpi.ddns.net";
    private static final int SERVER_PORT = 1234;

    private ErrorLogger logger;

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

    private JList<Message> messageList;
    private JScrollPane messageWrapperScrollPane;
    private MessageListModel messageListModel;

    private JEditorPane messageEditText;
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
//          Wir zeigen dem User, ob der Ping erfolgreich war
        if(connectionDetected){
            new Snackbar.SnackbarFactory(MessagesMain.this, "You are connected to the server.", Snackbar.LENGTH_LONG)
                    .setBackgroundColor(Colors.i_CONNECTION_RESTORED)
                    .setFontColor(Colors.i_CONNECTION_FONT)
                    .setPosition(Snackbar.POSITION_BOTTOM_MIDDLE)
                    .setRelativeTo(MessagesMain.this)
                    .create()
                    .show();
        }else{
            new Snackbar.SnackbarFactory(MessagesMain.this, "Your connection has timed out.", Snackbar.LENGTH_LONG)
                    .setBackgroundColor(Colors.i_CONNECTION_LOST)
                    .setFontColor(Colors.i_CONNECTION_FONT)
                    .setPosition(Snackbar.POSITION_BOTTOM_MIDDLE)
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

    private MessageListener.OnDisconnectListener disconnectListener = new MessageListener.OnDisconnectListener() {
        @Override
        public void onDisconnect() {
            showDisconnectMessage();
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
        this.setTitle("Messaging");

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onStop();
                dispose();
            }
        });

        onCreate();

        setSize(1000, 600);
        setVisible(true);

        postCreate();
    }

    private void onCreate() {
        logger = new ErrorLogger(getSystemFolder());

        setupUI();

        mMessages = MessageLoader.loadMessages(new Preference(getSystemFolder(), MESSAGES_PREFERENCE));

        messageListModel = new MessageListModel(mMessages);
        messageList.setCellRenderer(new MessageListCellRenderer());
        messageList.setModel(messageListModel);

        Preference settingsPreference = new Preference(getSystemFolder(), SETTINGS_PREFERENCE);

        currentName = settingsPreference.getString(PREF_CURRENT_NAME, "Unknown User");
        limitSavedMessages = settingsPreference.getBoolean(PREF_LIMIT_SAVED_MESSAGES, true);
        limitSavedMessagesAmount = settingsPreference.getInt(PREF_LIMIT_SAVED_MESSAGES_AMOUNT, 5000);


        connectToServer();
    }

    private void postCreate() {
        scrollToMessageBottom();
    }

    private void onStop() {
        if(connected) {
            try {
                serverMessageListener = null;
                serverMessageSender.close();
                server.close();
            } catch (IOException e){
                logger.logThrowable(e);
            }
        }

        if(limitSavedMessages) {
            MessageLoader.saveMessages(new Preference(getSystemFolder(), MESSAGES_PREFERENCE), mMessages, limitSavedMessagesAmount);
        } else {
            MessageLoader.saveMessages(new Preference(getSystemFolder(), MESSAGES_PREFERENCE), mMessages);
        }

        logger.writeLog();
    }

    private void connectToServer(){
        if(!tryingToConnect && !connected){
            tryingToConnect = true;

//          Initialisierung der Serververbindung auf einem eigenen Thread, da Android
//          keine Netzwerkkommunikation auf dem MainThread erlaubt
            new Thread(() -> {
                try{
                    server = new Socket(SERVER_URL, SERVER_PORT);

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
                    logger.logThrowable(e);
//                        e.printStackTrace();
                    connected = false;
                    showDisconnectedErrorMessage();
                }

                tryingToConnect = false;
            }).start();
        }
    }

    private void scrollToMessageBottom(){
        messageListModel.notifyDataSetChanged();
        messageWrapperScrollPane.validate();

        JScrollBar bar = messageWrapperScrollPane.getVerticalScrollBar();
        if(bar != null){
            try{
                bar.setValue(bar.getMaximum());
            }catch(Exception e){
                logger.logThrowable(e);
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
                addMessage(new SimpleDateFormat("EEEE dd. MMMM yyyy").format(Calendar.getInstance().getTime()), Message.Type.Announcement);
            }else{
//              falls eine Nachricht da ist, bekommen wir deren Zeit
                Calendar lastMessageTime = Calendar.getInstance();
                lastMessageTime.setTimeInMillis(getLastMessage().getTime());
//              und die derzeitige Systemzeit
                Calendar currentTime = Calendar.getInstance();

//              und falls diese nicht am selben Tag sind
                if(notSameDay(lastMessageTime, currentTime)){
//                  fügen wir das heutige Datum als Announcement hinzu
                    addMessage(new SimpleDateFormat("EEEE dd. MMMM yyyy").format(Calendar.getInstance().getTime()), Message.Type.Announcement);
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

    private void addMessage(String msg, Message.Type type) {
        testForDateNeeded(type);
        mMessages.add(new Message(msg, type));
        scrollToMessageBottom();
    }

    private void addReceivedMessage(String name, String msg, Message.Type type) {
        testForDateNeeded(type);
        mMessages.add(new ReceivedMessage(name, msg, type));
        scrollToMessageBottom();
    }

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

    private void sendAdminMessage(String msg){
        if(connected) {
            serverMessageSender.sendAdminMessage(msg);
        } else {
            showDisconnectedErrorMessage();
        }
    }

    private void pingServer(){
        if(connected){
            serverMessageSender.pingServer();
        } else {
            showDisconnectedErrorMessage();
        }
    }

    private void requestNameCount(){
        if(connected){
            serverMessageSender.sendNameCountRequest();
        } else {
            setNameCount(0);
            showDisconnectedErrorMessage();
        }
    }

    private void setNameCount(int count) {
        connectedCountBtn.setText(String.format("%1$1d Users connected", count));
    }

    private void requestNames(){
        if(connected){
            serverMessageSender.sendNamesRequest();
        } else {
            showDisconnectedErrorMessage();
        }
    }

    private void showDisconnectedErrorMessage(){
        new Snackbar.SnackbarFactory(this, "You have no connnection to the server.", Snackbar.LENGTH_LONG)
                .setBackgroundColor(Colors.i_CONNECTION_LOST)
                .setFontColor(Colors.i_CONNECTION_FONT)
                .setActionFontColor(Colors.i_CONNECTION_ACTIONFONT)
                .setRelativeTo(this)
                .setPosition(Snackbar.POSITION_BOTTOM_MIDDLE)
                .setAction("Try reconnecting", e -> connectToServer())
                .create()
                .show();
    }

    private void showInvalidUserNameErrorMessage(){
        new Snackbar.SnackbarFactory(this, "The username was invalid.", Snackbar.LENGTH_LONG)
                .setBackgroundColor(Colors.i_CONNECTION_LOST)
                .setFontColor(Colors.i_CONNECTION_FONT)
                .setRelativeTo(this)
                .setPosition(Snackbar.POSITION_BOTTOM_MIDDLE)
                .create()
                .show();
    }

    private void showInvalidAdminPasswordMessage(){
        new Snackbar.SnackbarFactory(this, "Given password is invalid", Snackbar.LENGTH_LONG)
                .setBackgroundColor(Colors.i_ERROR_MESSAGE)
                .setFontColor(Colors.i_CONNECTION_FONT)
                .setRelativeTo(this)
                .setPosition(Snackbar.POSITION_BOTTOM_MIDDLE)
                .create()
                .show();
    }

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

        new Preference(getSystemFolder(), SETTINGS_PREFERENCE)
                .edit()
                .putString(PREF_CURRENT_NAME, currentName)
                .putBoolean(PREF_LIMIT_SAVED_MESSAGES, limitSavedMessages)
                .putInt(PREF_LIMIT_SAVED_MESSAGES_AMOUNT, limitSavedMessagesAmount)
                .apply();

        dialog.dispose();
    }

    private void sendAdminMessage(){
        new AdminPasswordDialog(this, () -> new AdminMessageDialog(this).setVisible(true)).setVisible(true);
    }

    private void showSettings(){
        new SettingsDialog(this).setVisible(true);
    }

    private void showDisconnectMessage() {
        new Snackbar.SnackbarFactory(this, "You just disconnected from the server.", Snackbar.LENGTH_LONG)
                .setBackgroundColor(Colors.i_ERROR_MESSAGE)
                .setFontColor(Colors.i_CONNECTION_FONT)
                .setRelativeTo(this)
                .setPosition(Snackbar.POSITION_BOTTOM_MIDDLE)
                .create()
                .show();
    }

    private void setupUI(){
        setLayout(new BorderLayout());
        add(setupToolbar(), BorderLayout.NORTH);
        add(setupMessagebar(), BorderLayout.SOUTH);
        add(setupMessageList(), BorderLayout.CENTER);
    }

    private JPanel setupToolbar(){
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Colors.toolbar.BACKGROUND);

        JLabel title = new JLabel("    Messaging");
        title.setForeground(Colors.toolbar.FOREGROUND);
        toolbar.add(title, BorderLayout.WEST);

        JPanel toolbarBtnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbarBtnWrapper.setBackground(Colors.toolbar.BACKGROUND);

        connectedCountBtn = new JButton("0 Users connected");
        connectedCountBtn.addActionListener(e -> requestNameCount());
        connectedCountBtn.setBackground(Colors.toolbar.BACKGROUND);
        connectedCountBtn.setForeground(Colors.toolbar.FOREGROUND);
        connectedCountBtn.setBorder(null);
        toolbarBtnWrapper.add(connectedCountBtn);

        JButton listConnectedPeopleBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_users.png")));
        listConnectedPeopleBtn.addActionListener(e -> requestNames());
        listConnectedPeopleBtn.setBackground(Colors.toolbar.BACKGROUND);
        listConnectedPeopleBtn.setForeground(Colors.toolbar.FOREGROUND);
        listConnectedPeopleBtn.setBorder(null);
        toolbarBtnWrapper.add(listConnectedPeopleBtn);

        JButton sendAdminMessageBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_adminmessage.png")));
        sendAdminMessageBtn.addActionListener(e -> sendAdminMessage());
        sendAdminMessageBtn.setBackground(Colors.toolbar.BACKGROUND);
        sendAdminMessageBtn.setForeground(Colors.toolbar.FOREGROUND);
        sendAdminMessageBtn.setBorder(null);
        toolbarBtnWrapper.add(sendAdminMessageBtn);

        JButton pingServerBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_ping.png")));
        pingServerBtn.addActionListener(e -> pingServer());
        pingServerBtn.setBackground(Colors.toolbar.BACKGROUND);
        pingServerBtn.setForeground(Colors.toolbar.FOREGROUND);
        pingServerBtn.setBorder(null);
        toolbarBtnWrapper.add(pingServerBtn);

        JButton showSettingsBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("ic_settings.png")));
        showSettingsBtn.addActionListener(e -> showSettings());
        showSettingsBtn.setBackground(Colors.toolbar.BACKGROUND);
        showSettingsBtn.setForeground(Colors.toolbar.FOREGROUND);
        showSettingsBtn.setBorder(null);
        toolbarBtnWrapper.add(showSettingsBtn);

        toolbar.add(toolbarBtnWrapper, BorderLayout.EAST);
        return toolbar;
    }

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
                        src.setText(src.getText() + "\n");
                    }
                }
            }
        });
        JScrollPane messageEditTextWrapper = new JScrollPane(messageEditText, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagesWrapper.add(messageEditTextWrapper, BorderLayout.CENTER);

        JButton sendBtn = new JButton(new ImageIcon(MessagesMain.class.getResource("post_control_message.png")));
        sendBtn.setBackground(Colors.messagebar.SENDBUTTONBACKGROUND);
        sendBtn.addActionListener(e -> sendMessage());
        messagesWrapper.add(sendBtn, BorderLayout.EAST);

        messagesWrapper.setMaximumSize(new Dimension(10000, 48));

        return messagesWrapper;
    }

    private JScrollPane setupMessageList(){
        messageList = new JList<>();

        messageWrapperScrollPane = new JScrollPane(messageList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageWrapperScrollPane.setViewportBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageWrapperScrollPane.setWheelScrollingEnabled(true);

        return messageWrapperScrollPane;
    }

    private static String getSystemFolder() {
        return new File("").getAbsolutePath();
    }

    private static String convertToString(char[] array){
        String result = "";

        for(char c : array) {
            result += c;
        }

        return result;
    }

    public static void main(String[] args) {
        new MessagesMain();
    }

    private class SettingsDialog extends JDialog {

        private static final int WIDTH = 500;
        private static final int HEIGHT = 150;

        private JTextField userNameTextField;
        private JSlider messagesLimitSlider;
        private JCheckBox messagesLimitCheckbox;

        public SettingsDialog(Frame owner) {
            super(owner, "Settings", true);

            this.setLayout(new BorderLayout());

            JPanel centerWrapper = new JPanel(new BorderLayout());

            userNameTextField = new JTextField(currentName);
            centerWrapper.add(userNameTextField, BorderLayout.NORTH);

            messagesLimitCheckbox = new JCheckBox("Limit the amount of saved messages?", limitSavedMessages);
            centerWrapper.add(messagesLimitCheckbox, BorderLayout.CENTER);

            messagesLimitSlider = new JSlider(JSlider.HORIZONTAL, 0, 10000, limitSavedMessagesAmount);
            messagesLimitSlider.setMajorTickSpacing(1000);
            messagesLimitSlider.setMinorTickSpacing(200);
            centerWrapper.add(messagesLimitSlider, BorderLayout.SOUTH);

            this.add(centerWrapper, BorderLayout.CENTER);

            this.add(new JPanel(), BorderLayout.NORTH);
            this.add(new JPanel(), BorderLayout.EAST);
            this.add(new JPanel(), BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton abortButton = new JButton("Abort");
            abortButton.addActionListener(e -> dispose());
            buttonPanel.add(abortButton);

            JButton saveButton = new JButton("Save");
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

        public String getWrittenName(){
            return userNameTextField.getText();
        }

        public boolean isMessagesToBeLimited(){
            return messagesLimitCheckbox.isSelected();
        }

        public int getMessagesLimit(){
            return messagesLimitSlider.getValue();
        }

    }

    private class AdminMessageDialog extends JDialog {

        private static final int WIDTH = 300;
        private static final int HEIGHT = 100;

        public AdminMessageDialog(Frame owner) {
            super(owner, "Send admin message", true);

            this.setLayout(new BorderLayout());

            final JTextPane messageTextField = new JTextPane();
            this.add(messageTextField, BorderLayout.CENTER);

            this.add(new JPanel(), BorderLayout.NORTH);
            this.add(new JPanel(), BorderLayout.EAST);
            this.add(new JPanel(), BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton abortButton = new JButton("Abort");
            abortButton.addActionListener(e -> dispose());
            buttonPanel.add(abortButton);

            JButton okButton = new JButton("Send");
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

    private class AdminPasswordDialog extends JDialog {

        private static final int WIDTH = 300;
        private static final int HEIGHT = 100;

        private static final String adminPassword = "overFL0W8:";

        public AdminPasswordDialog(Frame owner, Runnable adminExecutable) {
            super(owner, "Admin rights required", true);

            this.setLayout(new BorderLayout());

            final JPasswordField passwordField = new JPasswordField();
            passwordField.setBackground(Colors.BACKGROUND_ADMINPASSWORDDIALOG);
            this.add(passwordField, BorderLayout.CENTER);

            JPanel gapN = new JPanel();
            gapN.setBackground(Colors.BACKGROUND_ADMINPASSWORDDIALOG);
            this.add(gapN, BorderLayout.NORTH);

            JPanel gapE = new JPanel();
            gapE.setBackground(Colors.BACKGROUND_ADMINPASSWORDDIALOG);
            this.add(gapE, BorderLayout.EAST);

            JPanel gapW = new JPanel();
            gapW.setBackground(Colors.BACKGROUND_ADMINPASSWORDDIALOG);
            this.add(gapW, BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Colors.BACKGROUND_ADMINPASSWORDDIALOG);

            JButton abortButton = new JButton("Abort");
            abortButton.addActionListener(e -> dispose());
            buttonPanel.add(abortButton);

            JButton okButton = new JButton("Ok");
            okButton.addActionListener(e -> {
                dispose();
                if(convertToString(passwordField.getPassword()).equals(adminPassword)) {
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

    private class UsersListDialog extends JDialog {

        private static final int WIDTH = 500;
        private static final int HEIGHT = 350;

        public UsersListDialog(Frame owner, String[] userNames) {
            super(owner, "Currently connected users", true);

            this.setLayout(new BorderLayout());

            JList<String> userList = new JList<>(userNames);
            JScrollPane scrollWrapper = new JScrollPane(userList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            this.add(scrollWrapper, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton okButton = new JButton("Ok");
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
