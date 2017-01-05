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

package de.jeremy.chatserver;

import com.sun.istack.internal.Nullable;

public class FlipCoin implements ChatGame {
	
	private int tosses;
	private int HeadsCount;
	private int TailsCount;

	private boolean exit;
	private boolean instructions;
	
	private static final String STRING_INSTRUCTIONS = "Tell the game how many coins you want to flip, and let them flip!";
	private static final String STRING_EXITMESSAGE = "Thanks for flipping a coin. See you next time";
	private static final String STRING_WELCOMEMESSAGE = "Welcome to flipcoin, how many coins do you want to flip?";
	private static final String STRING_WON_SINGLE = "It's [COINSIDE]";
	private static final String STRING_WON_MULTI = "You flipped [TOSSEDCOINS] coins.\nHeads: [HEADS].\nTails: [TAILS].";
	private static final String STRING_NOTANUMBER = "That wasn't a number";
	private static final String STRING_NUMBERTOOLOW = "Number must be higher than 0";

	
	public FlipCoin() {
		HeadsCount = 0;
		TailsCount = 0;
		exit = false;
        instructions = false;
	}

	@Override
	public String getInstructions() {
        instructions = false;
		return STRING_INSTRUCTIONS;
	}

	@Nullable
	@Override
	public String handleMessage(String message) {
        if(message.trim().toLowerCase().equals("exit")) {
            exit = true;
            return null;
        } else if(message.trim().toLowerCase().equals("instructions")){
            instructions = true;
            return null;
        }

		try{
			tosses = Integer.parseInt(message);
		}catch (NumberFormatException e) {
			return STRING_NOTANUMBER;
		}

        String returnString;

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

		exit = true;
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

	@Override
	public boolean exitGame() {
		return exit;
	}

    @Override
    public boolean showInstructions() {
        return instructions;
    }
}
