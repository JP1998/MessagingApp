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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.jeanpierrehotz.messagingapptest.R;

/**
 * Created by Admin on 06.12.2016.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private ArrayList<Message> data;

    public MessageAdapter(ArrayList<Message> data) {
        this.data = data;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        switch(viewType){
            case ViewTypes.SENT:
                return new SentMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false));
            case ViewTypes.RECEIVED:
                return new ReceivedMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false));
            case ViewTypes.ANNOUNCEMENT:
                return new AnnouncementMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_announcement, parent, false));
            default:
                throw new IllegalArgumentException("Value de.jeanpierrehotz.messagingapptest.messages.MessageAdapter.ViewTypes.INVALID (=" + ViewTypes.INVALID + ") is invalid.");
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position){
        holder.setData(data.get(position).getMessage());
    }

    @Override
    public int getItemCount(){
        return data.size();
    }

    @Override
    public int getItemViewType(int position){
        switch(data.get(position).getMessageType()) {
            case Sent: return ViewTypes.SENT;
            case Received: return ViewTypes.RECEIVED;
            case Announcement: return ViewTypes.ANNOUNCEMENT;
            default: return ViewTypes.INVALID;
        }
    }

    private static class ViewTypes {
        public static final int SENT = 0x4655434B;
        public static final int RECEIVED = 0x4655434C;
        public static final int ANNOUNCEMENT = 0x4655434D;
        public static final int INVALID = 0x4655434E;
    }
}
