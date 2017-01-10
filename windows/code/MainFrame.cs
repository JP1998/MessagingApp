using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.IO;

using Messaging.Messages;
using Messaging.Networking;
using Messaging.Storage;


namespace Messaging
{
    public partial class MainFrame : Form
    {

        private const string PREFS_MESSAGE = "Messaging_Messages_SaveFile";
        private const string PREFS_SETTINGS = "Messaging_Settings_SaveFile";
        private const string PREFS_SERVER_SETTINGS = "Messaging_Serversettings";

        private const string PREFS_SETTINGS_USERNAME = "private static final String PREF_CURRENT_NAME";
        private const string PREFS_SETTINGS_LIMITMESSAGES = "private static final String PREF_LIMIT_SAVED_MESSAGES";
        private const string PREFS_SETTINGS_LIMITAMOUNT = "private static final String PREF_LIMIT_SAVED_MESSAGES_AMOUNT";

        private const string PREFS_SERVER_SETTINGS_ADRESS = "prefs_serverinformation_serveradress";
        private const string PREFS_SERVER_SETTINGS_PORT = "prefs_serverinformation_serverport";

        private const int ACTION_NONE = -1;
        private const int ACTION_RECONNECT = 1;

        private const string DEFAULT_SERVERADRESS = "verelpi.ddns.net";
        private const int DEFAULT_SERVERPORT = 1234;

        private string CurrentName { get; set; }
        private bool LimitSavedMessages { get; set; }
        private int LimitSavedMessagesAmount { get; set; }

        private List<ChatMessage> CurrentChatMessages { get; set; }

        private MessageNetworkingCompat MessagesNetworker { get; set; }

        private bool Connected { get; set; }
        private bool Connecting { get; set; }
        private int ActionCode { get; set; }

        public MainFrame()
        {
            InitializeComponent();

            Connected = false;
            Connecting = false;
            ActionCode = ACTION_NONE;

            CurrentChatMessages = ChatMessageLoader.loadMessages(new Preference(GetSystemFolder(), PREFS_MESSAGE));

            foreach(ChatMessage msg in CurrentChatMessages)
            {
                AppendMessage(msg);
            }

            Preference settings = new Preference(GetSystemFolder(), PREFS_SETTINGS);

            CurrentName = settings.getString(PREFS_SETTINGS_USERNAME, "Unknown user");
            LimitSavedMessages = settings.getBoolean(PREFS_SETTINGS_LIMITMESSAGES, true);
            LimitSavedMessagesAmount = settings.getInt(PREFS_SETTINGS_LIMITAMOUNT, 5000);

            ConnectToServer();
        }

        private void SendMessage()
        {
            if (!messagebar_messagetextbox.Text.Trim().Equals(""))
            {
                if (Connected)
                {
                    string msg = messagebar_messagetextbox.Text;
                    MessagesNetworker.SendMessage(msg);
                    AddMessage(msg, MessageType.Sent);
                    messagebar_messagetextbox.Text = "";
                }
                else
                {
                    ShowInformation("The server seems to be down.", "Connect to server", ACTION_RECONNECT);
                }
            }
        }

        private void SendAdminMessage(string msg)
        {
            if (Connected)
            {
                MessagesNetworker.SendAdminMessage(msg);
            }
            else
            {
                ShowInformation("The server seems to be down.", "Connect to server", ACTION_RECONNECT);
            }
        }

        private void AppendMessage(ChatMessage msg)
        {
            if (!messagesTextBox.Text.Equals(""))
            {
                AppendText("\n", messagesTextBox.ForeColor);
            }

            AppendText(ConvertTime(msg), Color.SaddleBrown);

            if(msg.Type != MessageType.Announcement)
            {
                if(msg is ReceivedMessage)
                {
                    AppendText(((ReceivedMessage) msg).UserName, Color.Purple);
                }
                else
                {
                    AppendText("You", Color.Green);
                }

                AppendText(": " + msg.MessageString, messagesTextBox.ForeColor);
            }
            else
            {
                AppendText(msg.MessageString, Color.Red);
            }

            messagesTextBox.Select(messagesTextBox.Text.Length, 0);
            messagesTextBox.ScrollToCaret();
        }

        public void AppendText(string text, Color color)
        {
            messagesTextBox.SelectionStart = messagesTextBox.TextLength;
            messagesTextBox.SelectionLength = 0;

            messagesTextBox.SelectionColor = color;
            messagesTextBox.AppendText(text);
            messagesTextBox.SelectionColor = messagesTextBox.ForeColor;
        }

        private string ConvertTime(ChatMessage msg)
        {
            return String.Format("[{0}] ", DateTime.FromBinary(msg.Time).ToShortTimeString());
        }

        #region MessageNetworkingCompat delegates
        private void PingReceived(bool con)
        {
            this.Invoke((MethodInvoker)delegate {
                if (con)
                {
                    ShowInformation("You are connected to the server.", null, ACTION_NONE);
                }
                else
                {
                    ShowInformation("Your connection has timed out.", "Reconnect", ACTION_RECONNECT);
                }
            });
        }

        private void MessageReceived(string name, string msg)
        {
            this.Invoke((MethodInvoker)delegate {
                AddReceivedMessage(name, msg, MessageType.Received);
            });
        }

        private void ServerMessageReceived(string msg)
        {
            this.Invoke((MethodInvoker)delegate {
                AddMessage(msg, MessageType.Announcement);
            });
        }

        private void UserCountReceived(int count)
        {
            this.Invoke((MethodInvoker)delegate {
                connectedUsersButton.Text = count + " connected users";
            });
        }

        private void UserNamesReceived(string[] names)
        {
            this.Invoke((MethodInvoker)delegate {
                UsersListDialog dialog = new UsersListDialog(names);
                dialog.ShowDialog(this);
                dialog.Dispose();
            });
        }

        private void Disconnected()
        {
            this.Invoke((MethodInvoker)delegate {
                ShowInformation("Your connection has timed out.", "Reconnect", ACTION_RECONNECT);
                UserCountReceived(0);
                Connected = false;
            });
        }

        private bool IsNotToBeClosed(Thread thr)
        {
            return thr == MessagesNetworker.ReadingThread;
        }
        #endregion

        #region Event Handlers
        private void refreshToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (Connected)
            {
                MessagesNetworker.SendNameCountRequest();
            }
            else
            {
                UserCountReceived(0);
                ShowInformation("The server seems to be down.", "Connect to server", ACTION_RECONNECT);
            }
        }

        private void showNamesToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (Connected)
            {
                MessagesNetworker.SendNamesRequest();
            }
            else
            {
                ShowInformation("The server seems to be down.", "Connect to server", ACTION_RECONNECT);
            }
        }

        private void toolbar_deletemessages_item_Click(object sender, EventArgs e)
        {
            CurrentChatMessages.Clear();
            messagesTextBox.Text = "";
        }

        private void toolbar_settings_item_Click(object sender, EventArgs e)
        {
            this.Invoke((MethodInvoker)delegate {
                SettingsDialog dialog = new SettingsDialog(CurrentName, LimitSavedMessages, LimitSavedMessagesAmount);
                DialogResult res = dialog.ShowDialog(this);

                if(res == DialogResult.OK)
                {
                    string newName;

                    if (!dialog.getUserName().Equals(""))
                    {
                        newName = dialog.getUserName();
                    }
                    else
                    {
                        newName = CurrentName;
                    }

                    if (Connected)
                    {
                        if (!CurrentName.Equals(newName))
                        {
                            MessagesNetworker.ChangeName(newName);
                        }
                    }
                    else
                    {
                        ShowInformation("The server seems to be down.", "Connect to server", ACTION_RECONNECT);
                    }

                    new Preference(GetSystemFolder(), PREFS_SETTINGS)
                            .edit()
                            .putString(PREFS_SETTINGS_USERNAME, newName)
                            .putBoolean(PREFS_SETTINGS_LIMITMESSAGES, dialog.isAmountLimited())
                            .putInt(PREFS_SETTINGS_LIMITAMOUNT, dialog.getLimit())
                            .apply();
                }
            });
        }

        private void toolbar_ping_item_Click(object sender, EventArgs e)
        {
            if (Connected)
            {
                MessagesNetworker.PingServer();
            }
            else
            {
                ShowInformation("The server seems to be down.", "Connect to server", ACTION_RECONNECT);
            }
        }

        private void toolbar_adminmessage_item_Click(object sender, EventArgs e)
        {
            this.Invoke((MethodInvoker)delegate {
                AdminMessageDialog dialog = new AdminMessageDialog();
                DialogResult res = dialog.ShowDialog(this);

                if(res == DialogResult.OK)
                {
                    if (dialog.hasCorrectPasswordPut())
                    {
                        SendAdminMessage(dialog.getMessage());
                    }
                }
            });
        }

        private void messagebar_messagetextbox_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                if (!e.Alt)
                {
                    SendMessage();
                }
                else
                {
                    int caret = messagebar_messagetextbox.SelectionStart;
                    messagebar_messagetextbox.Text = messagebar_messagetextbox.Text.Substring(0, caret) + "\n" + messagebar_messagetextbox.Text.Substring(caret);
                    messagebar_messagetextbox.SelectionStart = caret + 1;
                }

                e.Handled = true;
            }
        }

        private void messagebar_sendbutton_Click(object sender, EventArgs e)
        {
            SendMessage();
        }

        private void toolbar_serversettings_item_Click(object sender, EventArgs e)
        {
            this.Invoke((MethodInvoker)delegate {
                Preference serverPrefs = new Preference(GetSystemFolder(), PREFS_SERVER_SETTINGS);

                ServerSettingsDialog dialog = new ServerSettingsDialog(
                        serverPrefs.getString(PREFS_SERVER_SETTINGS_ADRESS, DEFAULT_SERVERADRESS),
                        serverPrefs.getInt(PREFS_SERVER_SETTINGS_PORT, DEFAULT_SERVERPORT)
                );
                DialogResult res = dialog.ShowDialog(this);

                if (res == DialogResult.OK)
                {
                    SafelyDisconnect();

                    int newPort;

                    try
                    {
                        newPort = Int32.Parse(dialog.getPort());

                        if (newPort < 0 || newPort > 65535)
                        {
                            newPort = serverPrefs.getInt(PREFS_SERVER_SETTINGS_PORT, DEFAULT_SERVERPORT);
                        }
                    }
                    catch (Exception exc)
                    {
                        newPort = serverPrefs.getInt(PREFS_SERVER_SETTINGS_PORT, DEFAULT_SERVERPORT);
                    }

                    serverPrefs.edit()
                            .putString(PREFS_SERVER_SETTINGS_ADRESS, dialog.getAdress())
                            .putInt(PREFS_SERVER_SETTINGS_PORT, newPort)
                            .apply();

                    ConnectToServer();
                }
            });
        }

        private void toolStrip_ActionButton_Click(object sender, EventArgs e)
        {
            Console.WriteLine(ActionCode);
            switch (ActionCode)
            {
                case ACTION_RECONNECT:
                    ConnectToServer();
                    break;
                case ACTION_NONE:
                default:
                    break;
            }
        }
        #endregion

        public static string GetSystemFolder()
        {
            return Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().GetName().CodeBase);
        }

        private void ApplicationClosing(object sender, FormClosingEventArgs e)
        {
            SafelyDisconnect();

            if (LimitSavedMessages)
            {
                ChatMessageLoader.saveMessages(new Preference(GetSystemFolder(), PREFS_MESSAGE), CurrentChatMessages, LimitSavedMessagesAmount);
            }
            else
            {
                ChatMessageLoader.saveMessages(new Preference(GetSystemFolder(), PREFS_MESSAGE), CurrentChatMessages);
            }
        }

        private void ConnectToServer()
        {
            if(!Connecting && !Connected)
            {
                Connecting = true;
                ShowInformation("Connecting...", null, ACTION_NONE);

                new Thread(delegate ()
                {
                    try
                    {
                        Preference serverSettings = new Preference(GetSystemFolder(), PREFS_SERVER_SETTINGS);

                        TcpClient server = new TcpClient(
                            serverSettings.getString(PREFS_SERVER_SETTINGS_ADRESS, DEFAULT_SERVERADRESS),
                            serverSettings.getInt(PREFS_SERVER_SETTINGS_PORT, DEFAULT_SERVERPORT)
                        );

                        MessagesNetworker = new MessageNetworkingCompat(server.GetStream());

                        MessagesNetworker.OnConnectionDetected = PingReceived;
                        MessagesNetworker.OnMessageReceived = MessageReceived;
                        MessagesNetworker.OnServerMessageReceived = ServerMessageReceived;
                        MessagesNetworker.OnUserCountReceived = UserCountReceived;
                        MessagesNetworker.OnUserNamesReceived = UserNamesReceived;
                        MessagesNetworker.OnDisconnect = Disconnected;
                        MessagesNetworker.IsNotToBeClosed = IsNotToBeClosed;

                        MessagesNetworker.Start();

                        MessagesNetworker.ChangeName(CurrentName);

                        Connected = true;
                        ShowInformation("Successfully connected to server.", null, ACTION_NONE);
                    }
                    catch(Exception e)
                    {
                        Connected = false;
                        ShowInformation("Something went wrong while connecting to the server.", "Try again", ACTION_RECONNECT);
                    }

                    Connecting = false;
                }).Start();
            }
        }

        private void SafelyDisconnect()
        {
            if (Connected)
            {
                MessagesNetworker.Close();
                Connected = false;
            }
        }

        private void ShowInformation(string info, string action, int actionCode)
        {
            toolStrip_MessageLabel.Text = info;
            toolStrip_ActionButton.Text = action;
            toolStrip_ActionButton.Visible = action != null;
            ActionCode = actionCode;
        }

        private void TestForDateNeeded(MessageType type)
        {
            if(type != MessageType.Announcement)
            {
                if(CurrentChatMessages.Count == 0 || GetLastMessage() == null || NotSameDay(DateTime.FromBinary(GetLastMessage().Time), DateTime.Now))
                {
                    AddMessage(DateTime.Now.ToString(), MessageType.Announcement);
                }
            }
        }

        private bool NotSameDay(DateTime t1, DateTime t2)
        {
            return t1.Day != t2.Day || t1.Month != t2.Month || t1.Year != t2.Year;
        }

        private ChatMessage GetLastMessage()
        {
            for(int i = CurrentChatMessages.Count - 1; i >= 0; i--)
            {
                if(CurrentChatMessages[i].Type != MessageType.Announcement)
                {
                    return CurrentChatMessages[i];
                }
            }
            return null;
        }

        private void AddMessage(string msg, MessageType type)
        {
            TestForDateNeeded(type);
            CurrentChatMessages.Add(new ChatMessage(msg, type));
            AppendMessage(CurrentChatMessages[CurrentChatMessages.Count - 1]);
        }

        private void AddReceivedMessage(string name, string msg, MessageType type)
        {
            TestForDateNeeded(type);
            CurrentChatMessages.Add(new ReceivedMessage(name, msg, type));
            AppendMessage(CurrentChatMessages[CurrentChatMessages.Count - 1]);
        }
    }

    namespace Storage
    {
        public class Preference
        {
            #region Attributes
            private readonly string mXMLFile;
            private string mName;

            private readonly Dictionary<string, bool> mBooleanValues;
            private readonly Dictionary<string, char> mCharValues;
            private readonly Dictionary<string, string> mStringValues;
            private readonly Dictionary<string, byte> mByteValues;
            private readonly Dictionary<string, short> mShortValues;
            private readonly Dictionary<string, int> mIntValues;
            private readonly Dictionary<string, long> mLongValues;
            private readonly Dictionary<string, float> mFloatValues;
            private readonly Dictionary<string, double> mDoubleValues;
            #endregion

            #region Constructors
            public Preference(string name)
            {
                mBooleanValues = new Dictionary<string, bool>();
                mCharValues = new Dictionary<string, char>();
                mStringValues = new Dictionary<string, string>();
                mByteValues = new Dictionary<string, byte>();
                mShortValues = new Dictionary<string, short>();
                mIntValues = new Dictionary<string, int>();
                mLongValues = new Dictionary<string, long>();
                mFloatValues = new Dictionary<string, float>();
                mDoubleValues = new Dictionary<string, double>();

                mXMLFile = "C:\\preferences\\" + name + ".xml";

                initializeValues(name);
            }

            public Preference(string path, string name)
            {
                mBooleanValues = new Dictionary<string, bool>();
                mCharValues = new Dictionary<string, char>();
                mStringValues = new Dictionary<string, string>();
                mByteValues = new Dictionary<string, byte>();
                mShortValues = new Dictionary<string, short>();
                mIntValues = new Dictionary<string, int>();
                mLongValues = new Dictionary<string, long>();
                mFloatValues = new Dictionary<string, float>();
                mDoubleValues = new Dictionary<string, double>();

                if (path.StartsWith("file:"))
                {
                    path = path.Substring(6);
                }

                mXMLFile = path + (path.EndsWith("\\") ? "" : "\\") + name + ".xml";

                initializeValues(name);
            }
            #endregion

            #region Private Miscellanous
            private void initializeValues(string name)
            {
                if (File.Exists(mXMLFile))
                {
                    string fileContent = File.ReadAllText(mXMLFile);

                    if (!updateContent(fileContent) || !name.Equals(mName))
                        throw new ArgumentException("The file which contains the data was illegally changed!");
                }
                else
                {
                    mName = name;
                    edit().commit();
                }
            }

            private bool updateContent(string content)
            {
                try
                {
                    Token prefToken = Token.getTokensInside(content)[0];
                    mName = prefToken.getAttribute("name");

                    Token[] valuerange = prefToken.getTokensInside();

                    Token[][] values = new Token[valuerange.Length][];
                    for (int i = 0; i < valuerange.Length; i++)
                    {
                        values[i] = valuerange[i].getTokensInside();
                    }

                    mBooleanValues.Clear();
                    mCharValues.Clear();
                    mStringValues.Clear();
                    mByteValues.Clear();
                    mShortValues.Clear();
                    mIntValues.Clear();
                    mLongValues.Clear();
                    mFloatValues.Clear();
                    mDoubleValues.Clear();

                    for (int range = 0; range < values.Length; range++)
                    {
                        foreach (Token t in values[range])
                        {
                            switch (range)
                            {
                                case 0:
                                    mBooleanValues[t.getAttribute("key")] = Convert.ToBoolean(t.getInterior());
                                    break;
                                case 1:
                                    mCharValues[t.getAttribute("key")] = Convert.ToChar(t.getInterior());
                                    break;
                                case 2:
                                    mStringValues[t.getAttribute("key")] = t.getInterior();
                                    break;
                                case 3:
                                    mByteValues[t.getAttribute("key")] = Convert.ToByte(t.getInterior());
                                    break;
                                case 4:
                                    mShortValues[t.getAttribute("key")] = Convert.ToInt16(t.getInterior());
                                    break;
                                case 5:
                                    mIntValues[t.getAttribute("key")] = Convert.ToInt32(t.getInterior());
                                    break;
                                case 6:
                                    mLongValues[t.getAttribute("key")] = Convert.ToInt64(t.getInterior());
                                    break;
                                case 7:
                                    mFloatValues[t.getAttribute("key")] = Convert.ToSingle(t.getInterior());
                                    break;
                                case 8:
                                    mDoubleValues[t.getAttribute("key")] = Convert.ToDouble(t.getInterior());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                catch (Exception exc)
                {
                    Console.WriteLine(exc.Message);
                    return false;
                }
                return true;
            }
            #endregion

            #region Public Preferences Methods
            public bool getBoolean(string key, bool def)
            {
                if (mBooleanValues.ContainsKey(key))
                    return mBooleanValues[key];
                return def;
            }

            public char getChar(string key, char def)
            {
                if (mCharValues.ContainsKey(key))
                    return mCharValues[key];
                return def;
            }

            public string getString(string key, string def)
            {
                if (mStringValues.ContainsKey(key))
                    return mStringValues[key];
                return def;
            }

            public byte getByte(string key, byte def)
            {
                if (mByteValues.ContainsKey(key))
                    return mByteValues[key];
                return def;
            }

            public short getShort(string key, short def)
            {
                if (mShortValues.ContainsKey(key))
                    return mShortValues[key];
                return def;
            }

            public int getInt(string key, int def)
            {
                if (mIntValues.ContainsKey(key))
                    return mIntValues[key];
                return def;
            }

            public long getLong(string key, long def)
            {
                if (mLongValues.ContainsKey(key))
                    return mLongValues[key];
                return def;
            }

            public float getFloat(string key, float def)
            {
                if (mFloatValues.ContainsKey(key))
                    return mFloatValues[key];
                return def;
            }

            public double getDouble(string key, double def)
            {
                if (mDoubleValues.ContainsKey(key))
                    return mDoubleValues[key];
                return def;
            }

            public bool contains(string key)
            {
                return mBooleanValues.ContainsKey(key) || mCharValues.ContainsKey(key)
                    || mStringValues.ContainsKey(key) || mByteValues.ContainsKey(key)
                    || mShortValues.ContainsKey(key) || mIntValues.ContainsKey(key)
                    || mLongValues.ContainsKey(key) || mFloatValues.ContainsKey(key)
                    || mDoubleValues.ContainsKey(key);
            }

            public Editor edit()
            {
                return new Editor(this);
            }
            #endregion

            #region Inner classes
            public class Editor
            {
                private Preference parent;

                private readonly Dictionary<string, bool> mTempBooleanValues;
                private readonly Dictionary<string, char> mTempCharValues;
                private readonly Dictionary<string, string> mTempStringValues;
                private readonly Dictionary<string, byte> mTempByteValues;
                private readonly Dictionary<string, short> mTempShortValues;
                private readonly Dictionary<string, int> mTempIntValues;
                private readonly Dictionary<string, long> mTempLongValues;
                private readonly Dictionary<string, float> mTempFloatValues;
                private readonly Dictionary<string, double> mTempDoubleValues;

                internal Editor(Preference p)
                {
                    parent = p;

                    mTempBooleanValues = new Dictionary<string, bool>(p.mBooleanValues);
                    mTempCharValues = new Dictionary<string, char>(p.mCharValues);
                    mTempStringValues = new Dictionary<string, string>(p.mStringValues);
                    mTempByteValues = new Dictionary<string, byte>(p.mByteValues);
                    mTempShortValues = new Dictionary<string, short>(p.mShortValues);
                    mTempIntValues = new Dictionary<string, int>(p.mIntValues);
                    mTempLongValues = new Dictionary<string, long>(p.mLongValues);
                    mTempFloatValues = new Dictionary<string, float>(p.mFloatValues);
                    mTempDoubleValues = new Dictionary<string, double>(p.mDoubleValues);
                }

                public Editor clear()
                {
                    mTempBooleanValues.Clear();
                    mTempCharValues.Clear();
                    mTempStringValues.Clear();
                    mTempByteValues.Clear();
                    mTempShortValues.Clear();
                    mTempIntValues.Clear();
                    mTempLongValues.Clear();
                    mTempFloatValues.Clear();
                    mTempDoubleValues.Clear();

                    return this;
                }

                public Editor remove(string key)
                {
                    mTempBooleanValues.Remove(key);
                    mTempCharValues.Remove(key);
                    mTempStringValues.Remove(key);
                    mTempByteValues.Remove(key);
                    mTempShortValues.Remove(key);
                    mTempIntValues.Remove(key);
                    mTempLongValues.Remove(key);
                    mTempFloatValues.Remove(key);
                    mTempDoubleValues.Remove(key);

                    return this;
                }

                public Editor putBoolean(string key, bool val)
                {
                    mTempBooleanValues[key] = val;

                    return this;
                }

                public Editor putChar(string key, char val)
                {
                    mTempCharValues[key] = val;

                    return this;
                }

                public Editor putString(string key, string val)
                {
                    mTempStringValues[key] = val;

                    return this;
                }

                public Editor putByte(string key, byte val)
                {
                    mTempByteValues[key] = val;

                    return this;
                }

                public Editor putShort(string key, short val)
                {
                    mTempShortValues[key] = val;

                    return this;
                }

                public Editor putInt(string key, int val)
                {
                    mTempIntValues[key] = val;

                    return this;
                }

                public Editor putLong(string key, long val)
                {
                    mTempLongValues[key] = val;

                    return this;
                }

                public Editor putFloat(string key, float val)
                {
                    mTempFloatValues[key] = val;

                    return this;
                }

                public Editor putDouble(string key, double val)
                {
                    mTempDoubleValues[key] = val;

                    return this;
                }

                public void apply()
                {
                    new Thread(executeApply).Start();
                }

                private void executeApply()
                {
                    try
                    {
                        writeToFile();
                        parent.initializeValues(parent.mName);
                    }
                    catch (Exception exc)
                    {
                        Console.WriteLine(exc.Message);
                    }
                }

                public bool commit()
                {
                    try
                    {
                        writeToFile();
                        parent.initializeValues(parent.mName);
                    }
                    catch (Exception exc)
                    {
                        Console.WriteLine(exc.Message);
                        return false;
                    }

                    return true;
                }

                private void writeToFile()
                {
                    string content = "<preference name=\"" + parent.mName + "\">\n\n";

                    content += "\t<boolean_vals>\n\n";
                    foreach (string key in mTempBooleanValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempBooleanValues[key] + "</value>\n";
                    }
                    content += "\n\t</boolean_vals>\n\n";

                    content += "\t<char_vals>\n\n";
                    foreach (string key in mTempCharValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempCharValues[key] + "</value>\n";
                    }
                    content += "\n\t</char_vals>\n\n";

                    content += "\t<string_vals>\n\n";
                    foreach (string key in mTempStringValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempStringValues[key] + "</value>\n";
                    }
                    content += "\n\t</string_vals>\n\n";

                    content += "\t<byte_vals>\n\n";
                    foreach (string key in mTempByteValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempByteValues[key] + "</value>\n";
                    }
                    content += "\n\t</byte_vals>\n\n";

                    content += "\t<short_vals>\n\n";
                    foreach (string key in mTempShortValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempShortValues[key] + "</value>\n";
                    }
                    content += "\n\t</short_vals>\n\n";

                    content += "\t<int_vals>\n\n";
                    foreach (string key in mTempIntValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempIntValues[key] + "</value>\n";
                    }
                    content += "\n\t</int_vals>\n\n";

                    content += "\t<long_vals>\n\n";
                    foreach (string key in mTempLongValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempLongValues[key] + "</value>\n";
                    }
                    content += "\n\t</long_vals>\n\n";

                    content += "\t<float_vals>\n\n";
                    foreach (string key in mTempFloatValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempFloatValues[key] + "</value>\n";
                    }
                    content += "\n\t</float_vals>\n\n";

                    content += "\t<double_vals>\n\n";
                    foreach (string key in mTempDoubleValues.Keys)
                    {
                        content += "\t\t<value key=\"" + key + "\">" + mTempDoubleValues[key] + "</value>\n";
                    }
                    content += "\n\t</double_vals>\n\n";

                    content += "</preference>";

                    File.WriteAllText(parent.mXMLFile, content);
                }
            }

            private class Token
            {
                private readonly Dictionary<string, string> mAttributes;
                private readonly string mInterior;

                private Token(Dictionary<string, string> attr, string inter)
                {
                    mAttributes = new Dictionary<string, string>(attr);
                    mInterior = inter;
                }

                public Token[] getTokensInside()
                {
                    return Token.getTokensInside(mInterior);
                }

                public static Token[] getTokensInside(string tokenize)
                {
                    List<Token> tokens = new List<Token>();

                    int beg = 0;
                    int name_end;
                    int end = 0;

                    char[] tokenize_charAt = tokenize.ToCharArray();

                    while (beg < tokenize.Length)
                    {
                        while (beg < tokenize.Length && tokenize_charAt[beg] != '<') beg++;

                        name_end = beg;

                        while (name_end < tokenize.Length && tokenize_charAt[name_end] != ' ' && tokenize_charAt[name_end] != '>') name_end++;

                        if (beg >= tokenize.Length || name_end >= tokenize.Length) break;

                        string name = tokenize.Substring(beg + 1, name_end - beg - 1);

                        while (end < tokenize.Length && !tokenize.Substring(end).StartsWith("</" + name)) end++;

                        while (end < tokenize.Length && tokenize_charAt[end] != '>') end++;

                        if (end > beg && end < tokenize.Length)
                        {
                            Token temp = getTokenIn(tokenize.Substring(beg, end + 1 - beg));

                            if (temp != null)
                                tokens.Add(temp);
                        }

                        beg = end;
                    }

                    Token[] result = new Token[tokens.Count];
                    for (int i = 0; i < tokens.Count; i++)
                    {
                        result[i] = tokens[i];
                    }

                    return result;
                }

                private static Token getTokenIn(string tokenize)
                {
                    int beg_beg = 0;
                    int name_end = 0;
                    int beg_end = 0;
                    int end_beg = tokenize.Length - 1;
                    int end_end = tokenize.Length - 1;

                    char[] tokenize_charAt = tokenize.ToCharArray();

                    while (beg_beg < tokenize.Length && tokenize_charAt[beg_beg] != '<') beg_beg++;

                    while (beg_end < tokenize.Length && tokenize_charAt[beg_end] != '>') beg_end++;

                    while (end_beg >= 0 && tokenize_charAt[end_beg] != '<') end_beg--;

                    while (end_end >= 0 && tokenize_charAt[end_end] != '>') end_end--;

                    while (name_end < tokenize.Length && tokenize_charAt[name_end] != ' ' && tokenize_charAt[name_end] != '>') name_end++;

                    string beginningTag = tokenize.Substring(beg_beg + 1, name_end - beg_beg - 1);
                    string endingTag = tokenize.Substring(end_beg + 2, end_end - end_beg - 2);

                    Dictionary<string, string> attributes = new Dictionary<string, string>();

                    if (name_end < beg_end)
                    {
                        string attributeString = tokenize.Substring(name_end + 1, beg_end - name_end - 1);

                        char[] attributeString_charAt = attributeString.ToCharArray();

                        int key_beg = 0;
                        int key_end = 0;
                        int val_beg = 0;
                        int val_end = attributeString.Length - 1;

                        while (key_end < attributeString.Length && attributeString_charAt[key_end] != '=') key_end++;
                        while (val_beg < attributeString.Length && attributeString_charAt[val_beg] != '\"') val_beg++;
                        while (val_end >= 0 && attributeString_charAt[val_end] != '\"') val_end--;

                        string key = attributeString.Substring(key_beg, key_end - key_beg);
                        string val = attributeString.Substring(val_beg + 1, val_end - val_beg - 1);

                        attributes[key] = val;
                    }

                    String interior = tokenize.Substring(beg_end + 1, end_beg - beg_end - 1);

                    if (tokenize_charAt[end_beg + 1] != '/' || !beginningTag.Equals(endingTag))
                        throw new ArgumentException("The file which contains the data was illegally changed!");

                    return new Token(attributes, interior);
                }

                public string getAttribute(string key)
                {
                    if (mAttributes.ContainsKey(key))
                        return mAttributes[key];
                    else
                        return null;
                }

                public string getInterior()
                {
                    return mInterior;
                }
            }
            #endregion
        }
    }

    namespace Networking
    {
        public class MessageNetworkingCompat
        {
            private const int PING_TIMEOUT = 1500;

            private const sbyte BYTECODE_CLOSECONNECTION = -1;
            private const sbyte BYTECODE_MESSAGE = 1;
            private const sbyte BYTECODE_SERVERMESSAGE = 2;
            private const sbyte BYTECODE_CHANGENAME = 3;
            private const sbyte BYTECODE_SERVERPING = 4;
            private const sbyte BYTECODE_NAMES = 5;
            private const sbyte BYTECODE_NAMESCOUNT = 6;

            public Thread ReadingThread { get; private set; }
            private NetworkStream ConnectedStream { get; set; }

            public bool Closed { get; private set; }
            public bool PingRunning { get; private set; }
            public bool PingTimeoutRunning { get; private set; }

            public ClosingDetector IsNotToBeClosed { get; set; }
            public DisconnectListener OnDisconnect { get; set; }
            public MessageReceivedUserNames OnUserNamesReceived { get; set; }
            public MessageReceivedUserCount OnUserCountReceived { get; set; }
            public MessageReceivedServerMessage OnServerMessageReceived { get; set; }
            public MessageReceived OnMessageReceived { get; set; }
            public PingListener OnConnectionDetected { get; set; }

            public MessageNetworkingCompat(NetworkStream stream)
            {
                this.ConnectedStream = stream;
                this.ReadingThread = new Thread(Run);
                this.PingRunning = false;
                this.PingTimeoutRunning = false;
                this.Closed = false;
            }

            public void Start()
            {
                ReadingThread.Start();
            }

            public void PingServer()
            {
                if (!PingTimeoutRunning)
                {
                    Send(BYTECODE_SERVERPING, null, false, true);
                }
            }

            public void Close()
            {
                Send(BYTECODE_CLOSECONNECTION, null, true);
            }

            public void SendMessage(string msg)
            {
                Send(BYTECODE_MESSAGE, msg);
            }

            public void ChangeName(string name)
            {
                Send(BYTECODE_CHANGENAME, name);
            }

            public void SendNameCountRequest()
            {
                Send(BYTECODE_NAMESCOUNT, null);
            }

            public void SendNamesRequest()
            {
                Send(BYTECODE_NAMES, null);
            }

            public void SendAdminMessage(string msg)
            {
                Send(BYTECODE_SERVERMESSAGE, msg);
            }

            private void Send(sbyte code, string msg, params bool[] param)
            {
                new Thread(delegate ()
                {
                    try
                    {
                        ConnectedStream.WriteByte(unchecked((byte) code));

                        if(msg != null)
                        {
                            WriteUTF(msg);
                        }

                        if(param.Length >= 1 && param[0])
                        {
                            ConnectedStream.Close(500);
                            this.Closed = true;
                        }

                        if(param.Length >= 2 && param[1])
                        {
                            PingRunning = true;
                            new Thread(PingTimer).Start();
                        }
                    }
                    catch(Exception e)
                    {
                        Console.WriteLine(e);
                    }
                }).Start();
            }

            private void Run()
            {
                if(IsNotToBeClosed != null)
                {
                    bool connected = true;
                    while(IsNotToBeClosed(Thread.CurrentThread) && connected && !Closed)
                    {
                        try
                        {
                            sbyte readByte = unchecked((sbyte) ConnectedStream.ReadByte());

                            if(readByte == BYTECODE_MESSAGE)
                            {
                                string name = ReadUTF();
                                string msg = ReadUTF();

                                if(OnMessageReceived != null && !name.Trim().Equals("") && !msg.Trim().Equals(""))
                                {
                                    OnMessageReceived(name, msg);
                                }
                            }
                            else if(readByte == BYTECODE_SERVERMESSAGE)
                            {
                                string msg = ReadUTF();

                                if (OnServerMessageReceived != null && !msg.Trim().Equals(""))
                                {
                                    OnServerMessageReceived(msg);
                                }
                            }
                            else if(readByte == BYTECODE_SERVERPING)
                            {
                                OnPingReceived(true);
                            }
                            else if(readByte == BYTECODE_NAMESCOUNT)
                            {
                                int count = ReadInt();

                                if(OnUserCountReceived != null)
                                {
                                    OnUserCountReceived(count);
                                }
                            }
                            else if(readByte == BYTECODE_NAMES)
                            {
                                string[] names = ReadUTF().Split(';');

                                if(OnUserNamesReceived != null)
                                {
                                    OnUserNamesReceived(names);
                                }
                            }
                        }
                        catch(Exception e)
                        {
                            if(OnDisconnect != null && !Closed)
                            {
                                OnDisconnect();
                            }
                            connected = false;
                        }                        
                    }
                }
                else
                {
                    throw new InvalidOperationException("There must be a delegate given for MessageNetworkingCompat.IsNotToBeClosed");
                }
            }

            private int ReadInt()
            {
                return ((byte)ConnectedStream.ReadByte()) << 24 |
                    ((byte)ConnectedStream.ReadByte()) << 16 |
                    ((byte)ConnectedStream.ReadByte()) << 8 |
                    ((byte)ConnectedStream.ReadByte());
            }

            private string ReadUTF()
            {
                int length = ConnectedStream.ReadByte() * 256 + ConnectedStream.ReadByte();
                byte[] bytecontents = new byte[length];
                ConnectedStream.Read(bytecontents, 0, length);
                return Encoding.UTF8.GetString(bytecontents);
            }

            private void WriteUTF(string send)
            {
                byte[] convertedStringToSend = Encoding.UTF8.GetBytes(send);

                byte[] sending = new byte[convertedStringToSend.Length + 2];

                sending[0] = (byte)(convertedStringToSend.Length / 256);
                sending[1] = (byte)(convertedStringToSend.Length % 256);

                for (int i = 2; i < sending.Length; i++)
                {
                    sending[i] = convertedStringToSend[i - 2];
                }

                ConnectedStream.Write(sending, 0, sending.Length);
            }

            private void PingTimer()
            {
                PingTimeoutRunning = true;

                Thread.Sleep(PING_TIMEOUT);

                if (PingRunning)
                {
                    OnPingReceived(false);
                }

                PingTimeoutRunning = false;
            }

            private void OnPingReceived(bool connected)
            {
                if(OnConnectionDetected != null && PingRunning)
                {
                    OnConnectionDetected(connected);
                }
                PingRunning = false;
            }

            public delegate bool ClosingDetector(Thread thr);

            public delegate void DisconnectListener();

            public delegate void MessageReceivedUserNames(string[] names);

            public delegate void MessageReceivedUserCount(int count);

            public delegate void MessageReceivedServerMessage(string msg);

            public delegate void MessageReceived(string name, string msg);

            public delegate void PingListener(bool connected);
        }
    }

    namespace Messages
    {
        public class ChatMessage
        {
            public string MessageString { get; private set; }
            public long Time { get; private set; }
            public MessageType Type { get; private set; }

            public ChatMessage(String msg, MessageType type)
            {
                this.MessageString = msg;
                this.Time = DateTime.Now.ToBinary();
                this.Type = type;
            }

            internal ChatMessage(String msg, long time, MessageType type)
            {
                this.MessageString = msg;
                this.Time = time;
                this.Type = type;
            }
        }

        public class ReceivedMessage : ChatMessage
        {
            public string UserName { get; private set; }

            public ReceivedMessage(string name, string msg, MessageType type) : base(msg, type)
            {
                this.UserName = name;
            }

            internal ReceivedMessage(string name, string msg, long time, MessageType type) : base(msg, time, type)
            {
                this.UserName = name;
            }
        }

        public class ChatMessageLoader
        {
            private const string PREFERENCECODE_MESSAGEAMOUNT = "private static final String PREFERENCECODE_MESSAGEAMOUNT";
            private const string PREFERENCECODE_MESSAGE = "private static final String PREFERENCECODE_MESSAGE";
            private const string PREFERENCECODE_NAME = "private static final String PREFERENCECODE_NAME";
            private const string PREFERENCECODE_TIME = "private static final String PREFERENCECODE_TIME";
            private const string PREFERENCECODE_TYPE = "private static final String PREFERENCECODE_TYPE";
            private const string PREFERENCECODE_SAVEDNAME = "private static final String PREFERENCECODE_SAVEDNAME";

            public const int AMOUNT_UNLIMITED = -1;

            private ChatMessageLoader() { }

            public static void saveMessages(Preference prefs, List<ChatMessage> msg)
            {
                saveMessages(prefs, msg, AMOUNT_UNLIMITED);
            }

            public static void saveMessages(Preference prefs, List<ChatMessage> msg, int amt)
            {
                int amount = (amt == AMOUNT_UNLIMITED) ? msg.Count : Math.Min(amt, msg.Count);
                Preference.Editor editor = prefs.edit().clear().putInt(PREFERENCECODE_MESSAGEAMOUNT, amount);

                int delta = msg.Count - amount;

                for(int ctr = 0, i = msg.Count - 1; ctr < amount; ctr++, i--)
                {
                    ChatMessage currMsg = msg[i];

                    int newIndex = i - delta;

                    editor.putString(PREFERENCECODE_MESSAGE + newIndex, currMsg.MessageString)
                            .putLong(PREFERENCECODE_TIME + newIndex, currMsg.Time)
                            .putInt(PREFERENCECODE_TYPE + newIndex, currMsg.Type.Code);

                    if(currMsg is ReceivedMessage)
                    {
                        editor.putString(PREFERENCECODE_NAME + newIndex, ((ReceivedMessage)currMsg).UserName)
                                .putBoolean(PREFERENCECODE_SAVEDNAME + newIndex, true);
                    }
                }

                editor.apply();
            }

            public static List<ChatMessage> loadMessages(Preference prefs)
            {
                List<ChatMessage> msg = new List<ChatMessage>();

                int amount = prefs.getInt(PREFERENCECODE_MESSAGEAMOUNT, 0);

                for (int i = 0; i < amount; i++)
                {
                    string message = prefs.getString(PREFERENCECODE_MESSAGE + i, "");
                    long time = prefs.getLong(PREFERENCECODE_TIME + i, DateTime.Now.ToBinary());
                    MessageType type = MessageType.fromCode(prefs.getInt(PREFERENCECODE_TYPE + i, -1));

                    if (prefs.getBoolean(PREFERENCECODE_SAVEDNAME + i, false))
                    {
                        String name = prefs.getString(PREFERENCECODE_NAME + i, "");

                        msg.Add(new ReceivedMessage(name, message, time, type));
                    }
                    else
                    {
                        msg.Add(new ChatMessage(message, time, type));
                    }
                }

                return msg;
            }
        }

        public class MessageType
        {
            private const int CODE_SENT = 0x1234;
            private const int CODE_RECEIVED = 0x2341;
            private const int CODE_ANNOUNCEMENT = 0x3412;
            private const int CODE_INVALID = 0x4123;

            public static readonly MessageType Sent = new MessageType(CODE_SENT);
            public static readonly MessageType Received = new MessageType(CODE_RECEIVED);
            public static readonly MessageType Announcement = new MessageType(CODE_ANNOUNCEMENT);

            public int Code { get; private set; }

            private MessageType(int code)
            {
                this.Code = code;
            }

            public static MessageType fromCode(int code)
            {
                switch (code)
                {
                    case CODE_SENT: return Sent;
                    case CODE_RECEIVED: return Received;
                    case CODE_ANNOUNCEMENT: return Announcement;
                    default: return null;
                }
            }
        }
    }
}
