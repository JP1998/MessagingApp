package de.jeremy.chatserver;


public class Numbergame implements ChatGame {
	
	private int number;
	private int usernumber;
	private int tries;
		
	private static final String STRING_INSTRUCTIONS = "Guess a number between 0 and 100";
	private static final String STRING_EXITMESSAGE = "Thanks for playing the Numbergame. See you next time";
	private static final String STRING_WELCOMEMESSAGE = "Welcome to the Numbergame, guess the number between 0 and 100";
	private static final String STRING_WON = "@game Congrats you guessed " + "[NUMBER]" + " in " + "[TRIES]" + " tries";
	private static final String STRING_TOOHIGH = "Too high";
	private static final String STRING_TOOSMALL = "Too small";
	private static final String STRING_NOTANUMBER = "That wasn't a number";
	
	
	public Numbergame() {
		number = (int)(Math.random()*100)+1;
		tries = 0;
	}

	@Override
	public String getInstructions() {
		return STRING_INSTRUCTIONS;
	}

	@Override
	public String handleMessage(String message) {
		
		String returnMessage = null;
		try{
			usernumber = Integer.parseInt(message);
		}catch(NumberFormatException e){
			return STRING_NOTANUMBER;
		}
		if(usernumber == number){
			returnMessage = STRING_WON.replace("[NUMBER]", String.valueOf(number)).replace("[TRIES]", String.valueOf(tries));
		}else{
			returnMessage = usernumber < number ? STRING_TOOSMALL : STRING_TOOHIGH;
		}
		
		tries++;
		System.out.println(tries);
		return returnMessage;
	}

	@Override
	public String getExitMessage() {
		return STRING_EXITMESSAGE;
	}

	@Override
	public String getWelcomeMessage() {
		return STRING_WELCOMEMESSAGE;
		
	}

	

}
