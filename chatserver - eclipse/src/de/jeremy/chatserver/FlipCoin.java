package de.jeremy.chatserver;

public class FlipCoin implements ChatGame {

	
	private int tosses;
	private int HeadsCount;
	private int TailsCount;
	
	
	private static final String STRING_INSTRUCTIONS = "Tell the game how many coins you want to flip, and let them flip!";
	private static final String STRING_EXITMESSAGE = "Thanks for flipping a coin. See you next time";
	private static final String STRING_WELCOMEMESSAGE = "Welcome to flipcoin, how many coins do you want to flip?";
	private static final String STRING_WON_SINGLE = "@game It's [COINSIDE]";
	private static final String STRING_WON_MULTI = "@game You flipped [TOSSEDCOINS] coins. /n Heads: [HEADS]. /n Tails: [TAILS].";
	private static final String STRING_NOTANUMBER = "That wasn't a number";
	private static final String STRING_NUMBERTOOLOW = "Number must be higher than 0";

	
	public FlipCoin() {
		HeadsCount = 0;
		TailsCount = 0;
	}

	@Override
	public String getInstructions() {
		return STRING_INSTRUCTIONS;
	}

	@Override
	public String handleMessage(String message) {
		
		String returnString = null;
		
		try{
			tosses = Integer.parseInt(message);
		}catch (NumberFormatException e) {
			return STRING_NOTANUMBER;
		}
		if(tosses <= 0){
			return STRING_NUMBERTOOLOW;
		}else if(tosses == 1){
			if(Math.random() < 0.5){
				returnString = STRING_WON_SINGLE.replace("[COINSIDE]", "Heads");
			}else{
				returnString = STRING_WON_SINGLE.replace("[COINSIDE]", "Tails");
			}
		}else{
			for(int i = 0; i < tosses; i++){
				if(Math.random() < 0.5){
					HeadsCount++;
				}else{
					TailsCount++;
				}
			}
			
			returnString = STRING_WON_MULTI.replace("[TOSSEDCOINS]", String.valueOf(tosses)).replace("[HEADS]", String.valueOf(HeadsCount)).replace("[TAILS]", String.valueOf(TailsCount));
		}
		
		return returnString;
		
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
