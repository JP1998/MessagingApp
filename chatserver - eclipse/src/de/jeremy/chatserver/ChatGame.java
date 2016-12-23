package de.jeremy.chatserver;

public interface ChatGame {

	String getInstructions();
	/**
	 * In this methode the game has to handle what the user wrote, it returns the games answer. If the game was won/lost or you want it to exit return "@game " infront of your message.
	 * @param message A String of what the user wrote
	 * @return A string what you want to tell the user
	 */
	String handleMessage(String message);
	String getExitMessage();
	String getWelcomeMessage();
	
	
}
