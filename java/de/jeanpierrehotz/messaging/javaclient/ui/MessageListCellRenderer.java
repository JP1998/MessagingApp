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

import de.jeanpierrehotz.messaging.messages.Message;
import de.jeanpierrehotz.messaging.messages.ReceivedMessage;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 */
public class MessageListCellRenderer implements ListCellRenderer<Message> {

    private static final String TIME_TEMPLATE = "HH:mm";

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        switch (value.getMessageType()) {
            case Sent:
                Calendar calSent = Calendar.getInstance();
                calSent.setTimeInMillis(value.getTime());

                return new SentMessageListItem(value.getMessage(), new SimpleDateFormat(TIME_TEMPLATE).format(calSent.getTime()));
            case Announcement:
                return new AnnouncementMessageListItem(value.getMessage());
            case Received:
                Calendar calReceived = Calendar.getInstance();
                calReceived.setTimeInMillis(value.getTime());

                return new ReceivedMessageListItem(value.getMessage(), ((ReceivedMessage) value).getUserName(), new SimpleDateFormat(TIME_TEMPLATE).format(calReceived.getTime()));
            default: return null;
        }
    }

}
