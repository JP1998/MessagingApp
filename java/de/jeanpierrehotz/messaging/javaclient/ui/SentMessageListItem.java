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

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class SentMessageListItem extends JPanel {

    public SentMessageListItem(String msg, String time) {
        super(new BorderLayout(10, 10));

        JPanel wrapper = new JPanel(new BorderLayout());

        JTextPane msgPane = new JTextPane();
        msgPane.setText(msg);
        msgPane.setBackground(new Color(0, 0, 0, 0));

        JLabel timeLabel = new JLabel(time);

        wrapper.add(msgPane, BorderLayout.CENTER);
        wrapper.add(timeLabel, BorderLayout.EAST);

        wrapper.setBackground(new Color(0x21BC43));

        this.add(wrapper, BorderLayout.EAST);

        this.setBorder(BorderFactory.createEmptyBorder(5, 100, 5, 0));
    }

}
