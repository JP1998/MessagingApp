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
public class AnnouncementMessageListItem extends JPanel {

    public AnnouncementMessageListItem(String msg) {
        super(new BorderLayout(10, 10));

        JPanel wrapper = new JPanel(new BorderLayout());

        JLabel msgPane = new JLabel(msg);
        msgPane.setHorizontalAlignment(JLabel.CENTER);

        wrapper.add(msgPane, BorderLayout.CENTER);
        wrapper.setBackground(new Color(0xA4C5FC));

        this.add(wrapper, BorderLayout.CENTER);

        this.setBorder(BorderFactory.createEmptyBorder(5, 50, 5, 50));
    }

}
