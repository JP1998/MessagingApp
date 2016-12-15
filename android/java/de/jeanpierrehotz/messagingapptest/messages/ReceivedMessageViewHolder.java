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

package de.jeanpierrehotz.messagingapptest.messages;

import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.jeanpierrehotz.messagingapptest.R;

/**
 * Diese Klasse wird genutzt, um die Views für eine Received-Nachricht zu speichern
 */
public class ReceivedMessageViewHolder extends MessageViewHolder {

    /**
     * Das TextView, welches die eigentliche Nachricht anzeigt
     */
    private TextView receivedTextView;
    /**
     * Das TextView, welches die Zeit der Nachricht anzeigt
     */
    private TextView timeTextView;
    /**
     * Das TextView, welches den Namen des Users anzeigt, der die Nachricht verschickt hat
     */
    private TextView nameTextView;

    public ReceivedMessageViewHolder(View itemView){
        super(itemView);

        receivedTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        timeTextView = (TextView) itemView.findViewById(R.id.message_timeTextView);
        nameTextView = (TextView) itemView.findViewById(R.id.message_nameTextView);
    }

    @Override
    public void setData(Message msg){
        if(msg instanceof ReceivedMessage){
            receivedTextView.setText(msg.getMessage());

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(msg.getTime());
            timeTextView.setText(new SimpleDateFormat("HH:mm").format(cal.getTime()));

            nameTextView.setText(((ReceivedMessage) msg).getUserName());
        }
    }

}
