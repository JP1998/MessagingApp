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

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

/**
 *
 */
public class MessageListModel implements ListModel<Message> {

    private ArrayList<Message> data;
    private ArrayList<ListDataListener> listener;

    public MessageListModel(ArrayList<Message> msgs) {
        this.data = msgs;
        this.listener = new ArrayList<>();
    }

    public void notifyDataSetChanged() {
        ListDataEvent ev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size() - 1);

        for(ListDataListener list : listener) {
            if(list != null) {
                list.contentsChanged(ev);
            }
        }
    }

    public void notifyItemsAdded(int beginIndex, int endIndex) {
        ListDataEvent ev = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, beginIndex, endIndex);

        for(ListDataListener list : listener) {
            if(list != null) {
                list.intervalAdded(ev);
            }
        }
    }

    public void notifyItemsRemoved(int beginIndex, int endIndex) {
        ListDataEvent ev = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, beginIndex, endIndex);

        for(ListDataListener list : listener) {
            if(list != null) {
                list.intervalRemoved(ev);
            }
        }
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public Message getElementAt(int index) {
        return data.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        this.listener.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        this.listener.remove(l);
    }

}
