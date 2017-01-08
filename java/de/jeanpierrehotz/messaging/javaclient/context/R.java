/*
 *    Copyright 2016 Jean-Pierre Hotz
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
package de.jeanpierrehotz.messaging.javaclient.context;

/**
 * Created by the program Java Ressource Generator built by Jean-Pierre Hotz
 * This class is (together with the class Context) created to prevent hardcoding and stuff like that
 * This class provides ids to values accessible via the static methods in the Context-class.
 * The values are stored in separate package-visible classes for each datatype.
 */
public class R{
    /**
     * This class contains all the publicly readable ids for strings.
     * Their language is handled in the method {@link Context#getString(int)}
     * You may get a string by calling following:
     * <pre><code>
     * String example = Context.getString(R.string.exampleid);
     * </code></pre>
     */
    public static class string{
        public static final int messages_preference = 0;
        public static final int settings_preference = 1;
        public static final int pref_current_name = 2;
        public static final int pref_limit_saved_messages = 3;
        public static final int pref_limit_saved_messages_amount = 4;
        public static final int serverinformation_defaultadress = 5;
        public static final int pingmessage_connected = 6;
        public static final int pingmessage_disconnected = 7;
        public static final int app_title = 8;
        public static final int default_username = 9;
        public static final int dateannouncement_template = 10;
        public static final int connectedusers_amount_template = 11;
        public static final int errormessage_disconnected = 12;
        public static final int errormessage_disconnected_actiontext = 13;
        public static final int errormessage_invalidusername = 14;
        public static final int errormessage_invalidpassword = 15;
        public static final int errormessage_disconnect = 16;
        public static final int toolbar_title = 17;
        public static final int dialog_settings_caption = 18;
        public static final int dialog_settings_limitmessages_hint = 19;
        public static final int dialog_abort = 20;
        public static final int dialog_save = 21;
        public static final int dialog_adminmessage_caption = 22;
        public static final int dialog_send = 23;
        public static final int adminpassword = 24;
        public static final int dialog_adminpassword_caption = 25;
        public static final int dialog_ok = 26;
        public static final int dialog_userlist_caption = 27;
        public static final int serverinfo_preference = 28;
        public static final int prefs_serverinformation_serveradress = 29;
        public static final int prefs_serverinformation_serverport = 30;
        public static final int dialog_serversettings_caption = 31;
        public static final int dialog_serversettings_warning = 32;
    }

    /**
     * This class contains all the publicly readable ids for integer-values.
     * You may get a integer by calling following:
     * <pre><code>
     * int example = Context.getInt(R.integer.exampleid);
     * </code></pre>
     */
    public static class integer{
        public static final int serverinformation_defaultport = 0;
        public static final int default_messageslimitamount = 1;
        public static final int default_window_width = 2;
        public static final int default_window_height = 3;
    }

    /**
     * This class contains all the publicly readable ids for color-values.
     * You may get a color by calling following:
     * <pre><code>
     * Color example = Context.getColor(R.color.exampleid);
     * </code></pre>
     */
    public static class color{
        public static final int white = 0;
        public static final int light_red = 1;
        public static final int primarycolor = 2;
        public static final int primarycolor_dark = 3;
        public static final int primarycolor_light = 4;
        public static final int iconcolor = 5;
        public static final int accentcolor = 6;
        public static final int textprimarycolor = 7;
        public static final int textsecondarycolor = 8;
        public static final int dividercolor = 9;
        public static final int background_sent = 10;
        public static final int background_received = 11;
        public static final int background_announcement = 12;
        public static final int connection_restored = 13;
        public static final int connection_lost = 14;
        public static final int connection_font = 15;
        public static final int connection_actionfont = 16;
        public static final int received_namecolor = 17;
        public static final int errormessage = 18;
        public static final int background_adminpassworddialog = 19;
    }

}
