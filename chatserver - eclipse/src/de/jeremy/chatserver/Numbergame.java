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

public class Numbergame implements ChatGame {

    private int number;
    private int usernumber;
    private int tries;

    private boolean exit;
    private boolean instructions;

    private static final String STRING_INSTRUCTIONS = "Guess a number between 0 and 100";
    private static final String STRING_EXITMESSAGE = "Thanks for playing the Numbergame. See you next time";
    private static final String STRING_WELCOMEMESSAGE = "Welcome to the Numbergame, guess the number between 0 and 100";
    private static final String STRING_WON = "Congrats you guessed " + "[NUMBER]" + " in " + "[TRIES]" + " tries";
    private static final String STRING_TOOHIGH = "Too high";
    private static final String STRING_TOOSMALL = "Too small";
    private static final String STRING_NOTANUMBER = "That wasn't a number";


    public Numbergame() {
        number = (int) (Math.random() * 100) + 1;
        tries = 0;
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

        try {
            usernumber = Integer.parseInt(message);
        } catch (NumberFormatException e) {
            return STRING_NOTANUMBER;
        }

        String returnMessage;

        if (usernumber == number) {
            returnMessage = STRING_WON.replace("[NUMBER]", String.valueOf(number)).replace("[TRIES]", String.valueOf(tries));
            exit = true;
        } else {
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

    @Override
    public boolean exitGame() {
        return exit;
    }

    @Override
    public boolean showInstructions() {
        return instructions;
    }
}
