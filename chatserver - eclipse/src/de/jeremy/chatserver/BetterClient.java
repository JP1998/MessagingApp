package de.jeremy.chatserver;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class BetterClient extends JFrame {

	private JTextArea chatarea;
	private JTextField textfield;
	private JTextArea user;
	private JButton btnSend;
	private JPanel senderArea;

	private BetterThread chatthread;

	public BetterClient() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setSize(900, 600);
		this.setLayout(new BorderLayout());

		// init
		senderArea = new JPanel(new BorderLayout());
		chatarea = new JTextArea();
		chatarea.setEditable(false);
		user = new JTextArea();
		user.setEditable(false);
		textfield = new JTextField();
		btnSend = new JButton("send");

		chatthread = new BetterThread(this);
		chatthread.start();

		// actionlistener
		textfield.addActionListener(e -> sendTextFieldText());
		btnSend.addActionListener(e -> sendTextFieldText());

		// add
		senderArea.add(textfield, BorderLayout.CENTER);
		senderArea.add(btnSend, BorderLayout.EAST);

		this.add(chatarea, BorderLayout.CENTER);
		this.add(senderArea, BorderLayout.SOUTH);
		this.add(user, BorderLayout.EAST);

		this.setVisible(true);

	}

	public static void main(String[] args) {
		new BetterClient();
	}

	public void sendTextFieldText() {
		String s = textfield.getText();

		if (!s.startsWith("/")) {
			chatthread.sendMessage(s);
		} else {
			if (s.equals("/servercheck")) {
				chatthread.servercheck();
			} else if (s.equals("/exit")) {
				chatthread.stopChatting();
			} else if (s.startsWith("/s ")) {
				chatthread.sendServerMessage(s);
			} else if (s.startsWith("/name ")) {
				chatthread.changeName(s);
			} else if (s.startsWith("/shownames")) {
				chatthread.showAllNames();
			} else if (s.startsWith("/namecount")) {
				chatthread.namesCount();
			} else {
				chatarea.append("**************************" + "\n" + "List of commands:" + "\n"
						+ "/servercheck		To check if still conencted" + "\n" + "/s <msg>		To send server message"
						+ "\n" + "/name <new name>	To change name" + "\n" + "/shownames		To see who's connected"
						+ "\n" + "/namecount		To see how many are connected" + "\n"
						+ "/exit		To leave the chat" + "\n" + "**************************" + "\n");
			}
		}

		textfield.setText("");
	}

	public void addMessage(String msg) {
		chatarea.append(msg + "\n");
		chatarea.validate();
	}

	public void addServerMessage(String msg) {
		chatarea.append("Server: " + msg + "\n");
	}

	public void updateNameList(String userlist) {
		user.setText(userlist);
	}

}
