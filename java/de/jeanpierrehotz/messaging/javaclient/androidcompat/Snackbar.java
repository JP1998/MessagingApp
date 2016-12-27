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

package de.jeanpierrehotz.messaging.javaclient.androidcompat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * This class can be used to show a simple text message to the user and (if needed) make him respond to
 * this message by providing an action.<br>
 * The message will disappear after a certain time, or (if explicitly said) persist until dismissed manually, and is for single-use only
 * (meaning that its original state is not restorable).<br>
 * The look of the Snackbar is (unlike the Android-counterpart) easily modifyable in the process of creation.<br>
 * A Snackbar can be created by using the {@link de.jeanpierrehotz.messaging.javaclient.androidcompat.Snackbar.SnackbarFactory}-class,
 * and calling the method {@link SnackbarFactory#create()} on a created SnackbarFactory.<br>
 * There are 9 different locations a Snackbar may appear, which are each represented by a integer-constant.<br>
 * There are also three durations pre-defined, although you can give any duration (in ms) between 0 and {@link #MAX_DURATION}.<br>
 * When using a setXColor(int)-method of the SnackbarFactory you'll have to be faced with following color-definition:<br>
 * 0xXX<span style="color: #FF0000;">RR</span><span style="color: #00FF00;">GG</span><span style="color: #0000FF;">BB</span> -
 * This represents an integer (as in Java Standard) represented in hexadecimal. the last 6 digits represent a color-channel,
 * two digits for each, which makes 256 values per channel. The two leading digits will be ignored.<br>
 * To create and show a fully customized Snackbar, do as follows:<br>
 * <pre><code>
 <span style="color: #000080;">new</span> Snackbar.SnackbarFactory(getMainFrame(), <span style="color: #007700;">"A little message"</span>, <span style="color: #0000FF;">10000</span>)
        .setBackgroundColor(<span style="color: #0000FF;">0xFF0000</span>)
        .setAction(<span style="color: #007700;">"A little action"</span>, e -> System.out.println(<span style="color: #007700;">"Action has performed"</span>))
        .setActionFontColor(<span style="color: #0000FF;">0x00FF00</span>)
        .setFontColor(<span style="color: #0000FF;">0x0000FF</span>)
        .setPosition(Snackbar.<span style="color: #6600cc;">POSITION_BOTTOM_MIDDLE</span>)
        .create()
        .show();
 * </code></pre>
 */
public class Snackbar {

    /**
     * The margin to the border of the screen
     */
    private static final int MARGIN = 16;

    /**
     * The highest allowed duration of a Snackbar
     */
    public static final int MAX_DURATION = 120000;

    /**
     * The constants representi all the available positions
     */
    public static final int
            POSITION_TOP_LEFT           = 1,
            POSITION_TOP_MIDDLE         = 2,
            POSITION_TOP_RIGHT          = 3,
            POSITION_MIDDLE_RIGHT       = 4,
            POSITION_BOTTOM_RIGHT       = 5,
            POSITION_BOTTOM_MIDDLE      = 6,
            POSITION_BOTTOM_LEFT        = 7,
            POSITION_MIDDLE_LEFT        = 8,
            POSITION_MIDDLE_MIDDLE      = 9;

    public static final int LENGTH_SHORT = 3000;
    public static final int LENGTH_LONG = 5000;
    public static final int LENGTH_INDEFINITE = -1;

    /**
     * The Popup that has to be controlled by this Snackbar
     */
    private Popup controlledPopup;
    /**
     * Whether the Snackbar has already been dismissed
     */
    private boolean dismissed;
    /**
     * The duration that this Snackbar will persist
     */
    private int duration;

    private Snackbar(Popup popup, int dur) {
        controlledPopup = popup;
        dismissed = false;
        duration = dur;
    }

    /**
     * This method shows the Snackbar to the user, and (if needed)
     * queues the Thread that dismisses it after its duration
     */
    public void show(){
        if(duration != 0) {
            controlledPopup.show();

            if (duration > 0) {
                new Thread(() -> {
                    long timeUntil = System.currentTimeMillis() + duration;
                    while(System.currentTimeMillis() < timeUntil && !dismissed){
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    dismiss();
                }).start();
            }
        }
    }

    /**
     * This method dismisses the Snackbar and also (if needed) destroys the Thread
     * that should automatically dismiss this Snackbar
     */
    public void dismiss(){
        if(!dismissed) {
            dismissed = true;
            controlledPopup.hide();
        }
    }

    public static class SnackbarFactory{

        private String message;
        private Component owner;
        private int duration;

        private int backgroundColor;
        private int fontColor;
        private int actionFontColor;

        private boolean actionAdded;
        private String actionText;
        private ActionListener actionListener;

        private Window relativeTo;

        private int position;

        public SnackbarFactory(Component owner, String msg, int duration){
            if((duration < 0 || duration > MAX_DURATION) && duration != LENGTH_INDEFINITE){
                throw new IllegalArgumentException("Duration is out of bounds!");
            }

            this.message = msg;
            this.owner = owner;
            this.duration = duration;

            this.backgroundColor = 0x3B3B3B;
            this.fontColor = 0xDEDEDE;
            this.actionFontColor = 0x82E0FF;

            this.actionAdded = false;
            this.actionText = null;
            this.actionListener = null;

            this.relativeTo = null;

            this.position = POSITION_BOTTOM_RIGHT;
        }

        public SnackbarFactory setBackgroundColor(int col) {
            this.backgroundColor = col;
            return this;
        }

        public SnackbarFactory setFontColor(int col) {
            this.fontColor = col;
            return this;
        }

        public SnackbarFactory setActionFontColor(int col) {
            this.actionFontColor = col;
            return this;
        }

        public SnackbarFactory setPosition(int pos) {
            if(pos < 1 || pos > 9){
                throw new IllegalArgumentException("Parameter pos must be 1 <= pos <= 9; See Documentation!");
            }

            this.position = pos;
            return this;
        }

        public SnackbarFactory setRelativeTo(Window w){
            this.relativeTo = w;
            return this;
        }

        public SnackbarFactory setAction(String actionText, ActionListener listener) {
            this.actionAdded = true;
            this.actionText = actionText;
            this.actionListener = listener;
            return this;
        }

        public Snackbar create() {
            Color bg = new Color(backgroundColor);
            Color font = new Color(fontColor);
            Color actionFont = new Color(actionFontColor);

            JPanel wrapper = new JPanel(new BorderLayout());

//          add JPanels for margins
            JPanel gapN = new JPanel();
            gapN.setBackground(bg);
            wrapper.add(gapN, BorderLayout.NORTH);
            JPanel gapE = new JPanel();
            gapE.setBackground(bg);
            wrapper.add(gapE, BorderLayout.EAST);
            JPanel gapS = new JPanel();
            gapS.setBackground(bg);
            wrapper.add(gapS, BorderLayout.SOUTH);
            JPanel gapW = new JPanel();
            gapW.setBackground(bg);
            wrapper.add(gapW, BorderLayout.WEST);

            JTextPane content = new JTextPane();
            content.setEditable(false);
            content.setText(message);
            content.setBackground(bg);
            content.setForeground(font);

            if(actionAdded) {
                JPanel centerWrapper = new JPanel(new BorderLayout());

                centerWrapper.add(content, BorderLayout.WEST);

                JButton action = new JButton(actionText);
                action.addActionListener(actionListener);
                action.setBackground(bg);
                action.setForeground(actionFont);
                action.setBorder(null);

                centerWrapper.add(action, BorderLayout.EAST);

                JPanel centerGap = new JPanel();
                centerGap.setBackground(bg);
                centerWrapper.add(centerGap, BorderLayout.CENTER);

                wrapper.add(centerWrapper, BorderLayout.CENTER);
            } else {
                wrapper.add(content, BorderLayout.CENTER);
            }

            Dimension snackbarPosition = calculatePosition(position, wrapper, relativeTo);

            return new Snackbar(
                    PopupFactory.getSharedInstance().getPopup(
                            owner,
                            wrapper,
                            snackbarPosition.width,
                            snackbarPosition.height
                    ),
                    duration
            );
        }

        private static Dimension calculatePosition(int pos, Container snackbar, Window relativeTo){
            Dimension contentSize = snackbar.getLayout().preferredLayoutSize(snackbar);
            Dimension screenSize;
            int x, y;

            if(relativeTo == null){
                screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                x = 0;
                y = 0;
            } else {
                screenSize = relativeTo.getSize();
                x = relativeTo.getX();
                y = relativeTo.getY();
            }

            switch (pos) {
                case POSITION_TOP_LEFT:
                    return new Dimension(x + MARGIN, y + MARGIN);
                case POSITION_TOP_MIDDLE:
                    return new Dimension(x + ((screenSize.width - contentSize.width) / 2), y + MARGIN);
                case POSITION_TOP_RIGHT:
                    return new Dimension(x + (screenSize.width - contentSize.width - MARGIN), y + MARGIN);
                case POSITION_MIDDLE_RIGHT:
                    return new Dimension(x + (screenSize.width - contentSize.width - MARGIN), y + ((screenSize.height - contentSize.height) / 2));
                case POSITION_BOTTOM_RIGHT:
                    return new Dimension(x + (screenSize.width - contentSize.width - MARGIN), y + (screenSize.height - contentSize.height - MARGIN));
                case POSITION_BOTTOM_MIDDLE:
                    return new Dimension(x + ((screenSize.width - contentSize.width) / 2), y + (screenSize.height - contentSize.height - MARGIN));
                case POSITION_BOTTOM_LEFT:
                    return new Dimension(x + MARGIN, y + (screenSize.height - contentSize.height - MARGIN));
                case POSITION_MIDDLE_LEFT:
                    return new Dimension(x + MARGIN, y + ((screenSize.height - contentSize.height) / 2));
                case POSITION_MIDDLE_MIDDLE:
                    return new Dimension(x + ((screenSize.width - contentSize.width) / 2), y + ((screenSize.height - contentSize.height) / 2));
                default:
                    return new Dimension();
            }
        }
    }
}
