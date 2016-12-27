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

package de.jeanpierrehotz.messaging.javaclient.ui;

import java.awt.*;

/**
 *
 */
public class Colors {
    private Colors(){}

    public static final int i_WHITE = 0xFFFFFF;
    public static final int i_TRANSPARENT = 0x00000000;
    public static final int i_LIGHT_RED = 0xD85F46;

    public static final int i_PRIMARYCOLOR = 0xFF5722;
    public static final int i_PRIMARYCOLOR_DARK = 0xE64A19;
    public static final int i_PRIMARYCOLOR_LIGHT = 0xFFCCBC;

    public static final int i_ICON = i_WHITE;
    public static final int i_ACCENT = 0x4CAF50;
    public static final int i_TEXTPRIMARYCOLOR = 0x212121;
    public static final int i_TEXTSECONDARYCOLOR = 0x757575;
    public static final int i_DIVIDERCOLOR = 0xBDBDBD;

    public static final int i_BACKGROUND_SENT = 0x21bc43;
    public static final int i_BACKGROUND_RECEIVED = 0xcecece;
    public static final int i_BACKGROUND_ANNOUNCEMENT = 0xa4c5fc;

    public static final int i_CONNECTION_RESTORED = 0x5bbc8c;
    public static final int i_CONNECTION_LOST = i_LIGHT_RED;
    public static final int i_CONNECTION_FONT = i_WHITE;
    public static final int i_CONNECTION_ACTIONFONT = 0xBBBBBB;//0x08BD00;
    public static final int i_RECEIVED_NAMECOLOR = 0xaa00ff;
    public static final int i_ERROR_MESSAGE = i_LIGHT_RED;

    public static final int i_BACKGROUND_ADMINPASSWORDDIALOG = i_LIGHT_RED;


    public static final Color WHITE = new Color(i_WHITE);
    public static final Color TRANSPARENT = new Color(i_TRANSPARENT, true);
    public static final Color LIGHT_RED = new Color(i_LIGHT_RED);

    public static final Color PRIMARYCOLOR = new Color(i_PRIMARYCOLOR);
    public static final Color PRIMARYCOLOR_DARK = new Color(i_PRIMARYCOLOR_DARK);
    public static final Color PRIMARYCOLOR_LIGHT = new Color(i_PRIMARYCOLOR_LIGHT);

    public static final Color ICON = new Color(i_ICON);
    public static final Color ACCENT = new Color(i_ACCENT);
    public static final Color TEXTPRIMARYCOLOR = new Color(i_TEXTPRIMARYCOLOR);
    public static final Color TEXTSECONDARYCOLOR = new Color(i_TEXTSECONDARYCOLOR);
    public static final Color DIVIDERCOLOR = new Color(i_DIVIDERCOLOR);

    public static final Color BACKGROUND_SENT = new Color(i_BACKGROUND_SENT);
    public static final Color BACKGROUND_RECEIVED = new Color(i_BACKGROUND_RECEIVED);
    public static final Color BACKGROUND_ANNOUNCEMENT = new Color(i_BACKGROUND_ANNOUNCEMENT);

    public static final Color CONNECTION_RESTORED = new Color(i_CONNECTION_RESTORED);
    public static final Color CONNECTION_LOST = new Color(i_CONNECTION_LOST);
    public static final Color CONNECTION_FONT = new Color(i_CONNECTION_FONT);
    public static final Color CONNECTION_ACTIONFONT = new Color(i_CONNECTION_ACTIONFONT); // i_CONNECTION_ACTIONFONT
    public static final Color RECEIVED_NAMECOLOR = new Color(i_RECEIVED_NAMECOLOR);
    public static final Color ERROR_MESSAGE = new Color(i_ERROR_MESSAGE);

    public static final Color BACKGROUND_ADMINPASSWORDDIALOG = new Color(i_BACKGROUND_ADMINPASSWORDDIALOG);

    public static final class toolbar {
        private toolbar(){}

        public static final Color BACKGROUND = PRIMARYCOLOR;
        public static final Color FOREGROUND = WHITE;
    }

    public static final class messagebar {
        private messagebar(){}

        public static final Color SENDBUTTONBACKGROUND = ACCENT;
        public static final Color SENDBUTTONFOREGROUND = WHITE;
    }

}
