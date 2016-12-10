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

import de.jeanpierrehotz.messagingapptest.R;

/**
 *
 */
public class SentMessageViewHolder extends MessageViewHolder {

    private TextView sentTextView;

    public SentMessageViewHolder(View itemView){
        super(itemView);

        sentTextView = (TextView) itemView.findViewById(R.id.messageTextView);
    }

    @Override
    public void setData(String msg){
        sentTextView.setText(msg);
    }

}
