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
import android.view.View;

/**
 * Diese Klasse wird genutzt, um die Views für eine Nachricht zu speichern, wobei diese Klasse
 * nur als Super-Klasse zu nutzen ist, die die Funktionalität {@link #setData(String)} hinzufügt,
 * und als allgemeiner ViewHolder des {@link MessageAdapter} gilt.
 */
public abstract class MessageViewHolder extends RecyclerView.ViewHolder {

    public MessageViewHolder(View itemView){
        super(itemView);
    }

    /**
     * Diese Methode soll die Nachricht in dem View des ViewHolders anzeigen
     * @param msg die Nachricht, die angezeigt werden soll
     */
    public abstract void setData(String msg);

}
