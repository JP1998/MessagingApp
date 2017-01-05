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

/**
 *  This interface makes it possible to easily create a little chat game for the messaging application we're currently creating.<br>
 * Here's a little template on how to implement a game (please be aware that it will have to be approved and implemented by an admin):<br>
 * <pre><code>
<span style="color: #000080;">package</span> mySimpleChatGame;

<span style="color: #000080;">import</span> de.jeremy.chatserver.ChatGame;
<span style="color: #000080;">import</span> <span style="color: #808000;">com.sun.istack.internal.Nullable</span>;

<span style="color: #808080;">/&#42;&#42;
 &#42; Maybe describe your game a little here, to make the code easier to understand.&lt;br&gt;
 &#42; You can also make us give you credit with the author-JavaDoc tag:&lt;br&gt;
 &#42; &#64;author I am the author of the class
 &#42;/</span>
<span style="color: #000080;">public class</span> HelloGame <span style="color: #000080;">implements</span> ChatGame {

    <span style="color: #000080;">private static final</span> String <span style="color: #6600cc;">INSTRUCTIONS</span> = <span style="color: #007700;">"Simply type <span style="color: #0000FF;">\"</span>@Game Hello<span style="color: #0000FF;">\"</span>.<span style="color: #0000FF;">\n</span>There is not more to this game."</span>;
    <span style="color: #000080;">private static final</span> String <span style="color: #6600cc;">FAILED</span> = <span style="color: #007700;">"Are you retarded?!"</span>;
    <span style="color: #000080;">private static final</span> String <span style="color: #6600cc;">SUCCESS</span> = <span style="color: #007700;">"Close enough."</span>;
    <span style="color: #000080;">private static final</span> String <span style="color: #6600cc;">EXIT</span> = <span style="color: #007700;">"Thanks for playing! See you soon."</span>;
    <span style="color: #000080;">private static final</span> String <span style="color: #6600cc;">WELCOME</span> = <span style="color: #007700;">"Hey! Could you please type <span style="color: #0000FF;">\"</span>@Game Hello<span style="color: #0000FF;">\"</span>?"</span>;

    <span style="color: #000080;">private boolean</span> <span style="color: #6600cc;">exit</span>;
    <span style="color: #000080;">private boolean</span> <span style="color: #6600cc;">instructions</span>;

    <span style="color: #000080;">public</span> HelloGame(){
        <span style="color: #6600cc;">exit</span> = <span style="color: #000080;">false</span>;
        <span style="color: #6600cc;">instructions</span> = <span style="color: #000080;">false</span>;
    }

    <span style="color: #808000;">&#64;Override</span>
    <span style="color: #000080;">public</span> String getInstructions(){
        <span style="color: #6600cc;">instructions</span> = <span style="color: #000080;">false</span>;
        <span style="color: #000080;">return</span> <span style="color: #6600cc;">INSTRUCTIONS</span>;
    }

    <span style="color: #808000;">&#64;Nullable</span>
    <span style="color: #808000;">&#64;Override</span>
    <span style="color: #000080;">public</span> String handleMessage(String msg) {
<span style="color: #808080;">//      Always first check whether the user wants to exit the game or whether he wants the instructions repeated</span>
        <span style="color: #000080;">if</span>(msg.trim().toLowerCase().equals(<span style="color: #007700;">"exit"</span>)) {
            <span style="color: #6600cc;">exit</span> = <span style="color: #000080;">true</span>;
            <span style="color: #000080;">return null</span>;
        } <span style="color: #000080;">else if</span>(msg.trim().toLowerCase().equals(<span style="color: #007700;">"instructions"</span>)){
            <span style="color: #6600cc;">instructions</span> = <span style="color: #000080;">true</span>;
            <span style="color: #000080;">return null</span>;
        }

<span style="color: #808080;">//      Only after doing that you'll want to check for the actual game response</span>
<span style="color: #808080;">//      And just to be clear: This will be where you'll want to process input by the user, and respond accordingly</span>
        <span style="color: #000080;">if</span>(msg.trim().toLowerCase().equals(<span style="color: #007700;">"hello"</span>)){
            <span style="color: #6600cc;">exit</span> = <span style="color: #000080;">true</span>;
            <span style="color: #000080;">return</span> <span style="color: #6600cc;">SUCCESS</span>;
        } <span style="color: #000080;">else</span> {
            <span style="color: #000080;">return</span> <span style="color: #6600cc;">FAILED</span>;
        }
    }

    <span style="color: #808000;">&#64;Override</span>
    <span style="color: #000080;">public</span> String getExitMessage() {
        <span style="color: #000080;">return</span> <span style="color: #6600cc;">EXIT</span>;
    }

    <span style="color: #808000;">&#64;Override</span>
    <span style="color: #000080;">public</span> String getWelcomeMessage() {
        <span style="color: #000080;">return</span> <span style="color: #6600cc;">WELCOME</span>;
    }

    <span style="color: #808000;">&#64;Override</span>
    <span style="color: #000080;">public boolean</span> showInstructions() {
        <span style="color: #000080;">return</span> <span style="color: #6600cc;">instructions</span>;
    }

    <span style="color: #808000;">&#64;Override</span>
    <span style="color: #000080;">public boolean</span> getWelcomeMessage() {
        <span style="color: #000080;">return</span> <span style="color: #6600cc;">exit</span>;
    }
}
 * </code></pre>
 */
public interface ChatGame {

    /**
     * The game has to return its instructions, and set the condition for {@link #showInstructions()} to false.
     *
     * @return the instructions for the game
     */
    String getInstructions();

    /**
     * In this methode the game has to handle what the user wrote, it returns the games answer. If the game was won/lost, you want to show instructions,
     * and / or you want it to exit set the according conditions in {@link #showInstructions()} and / or {@link #exitGame()}.<br>
     * Instructions will not be shown, if you are exiting the game.
     *
     * @param message A String of what the user wrote
     * @return A string what you want to tell the user;<br>
     *      {@code null} if there is no message to the user (example if he asks for instructions; wants to exit)
     */
    @Nullable
    String handleMessage(String message);

    /**
     * The game has to return its exit message.<br>
     * It does not have to set the condition for {@link #exitGame()} to false, since any {@link ChatGame}-object will be destroyed after a call to this method
     *
     * @return the exit message for the game
     */
    String getExitMessage();

    /**
     * The game has to return its welcoming message
     *
     * @return the welcoming message for the game
     */
    String getWelcomeMessage();

    /**
     * @return whether or not the instructions are to be shown
     */
    boolean showInstructions();

    /**
     * @return whether or not to exit the current game
     */
    boolean exitGame();

}
