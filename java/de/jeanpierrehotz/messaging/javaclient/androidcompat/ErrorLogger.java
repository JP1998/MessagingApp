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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class is used to log errors or messages to a text-file.<br>
 * These messages are each documented with precise timestamps and are supposed to clear users up about
 * any errors occurring in their code or IDE.<br>
 * The log is not synchronized at all times but saves messages internally to make logging faster.<br>
 * If you want to make sure that the log is synchronized call the {@link #writeLog()}-method.<br>
 * The {@link #logThrowable(Throwable)}-method logs a throwable as follows:
 * <ol>
 *     <li>the type of throwable (its classname)</li>
 *     <li>the message of the Throwable (if one exists)</li>
 *     <li>the stacktrace of the error</li>
 * </ol>
 * Also the Logger keeps track of whether there was something changed in the logs content, and if there was no
 * change a call to {@link #writeLog()} simply won't do anything. This is also done to keep things faster.<br>
 * In case the log has been cleared by calling {@link #clearLog()} and saved thereafter using {@link #writeLog()} the
 * log file will simply be deleted, so you should be aware that the log file may cease to exist from time to time!
 */
public class ErrorLogger {

    private static final String ERROR_LOGGER_PREFIX = "['Messaging Java Client': EEE yyyy-MM-dd HH:mm:ss.SSS zzz]: ";
    private static final String ERROR_LOGGER_STACKTRACE_MESSAGE_INTRO = "Message:";
    private static final String ERROR_LOGGER_STACKTRACE_PREFIX = "    at ";
    private static final String ERROR_LOGGER_STACKTRACE_MESSAGE_PREFIX = "        ";

    /**
     * Whether this log has had activity since its creation
     */
    private boolean activity;

    /**
     * The path to the log file this ErrorLogger logs to
     */
    private String logFilePath;
    /**
     * The current content of the log.<br>
     * Is very likely to be out of synchronization with the actual content of the log file.
     */
    private String logContent;

    /**
     * This method creates a log file inside of the given folder.<br>
     * The content of the log file (in case it already exists) is loaded, and messages will simply be appended.
     * @param applicationFolder the folder this application is currently in
     */
    public ErrorLogger(String applicationFolder){
//      create the file "log.txt" inside of the application folder
        logFilePath = ((applicationFolder.endsWith(File.separator))? applicationFolder: applicationFolder + File.separator) + "log.txt";

//      indicate that there has not been any activity yet
        activity = false;


        if(new File(logFilePath).exists()){
//          load the logs content if it already exists
            logContent = getLogFileContent();
        } else {
//          if the log doesn't exist yet we'll simply have an empty log
            logContent = "";
        }
    }

    /**
     * This method gives you the path to the log file of this logger
     * @return the logs file path
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * This method logs the given message.<br>
     * If the message is multiline the message will be split up and a timestamp will be
     * added at the begining of every line
     * @param msg the message to log
     */
    public void logMessage(String msg){
//      split the message up into several lines
        String[] messageLines = msg.split("\\r?\\n");

        for(String message : messageLines){
//          log every line with an timestamp in front of it
            logContent += new SimpleDateFormat(ERROR_LOGGER_PREFIX).format(Calendar.getInstance().getTime()) + message + System.lineSeparator();
        }

//      and indicate that there has been activity
        activity = true;
    }

    /**
     * This method logs a given Throwable in following manner:
     * <ol>
     *     <li>the name of the Throwable (the class name of it)</li>
     *     <li>the Throwables message (if there is a message; split up into lines and with timestamp in front of every one)</li>
     *     <li>the stack trace of the Throwable</li>
     * </ol>
     * @param t the Throwable that is to log
     */
    public void logThrowable(Throwable t){
//      log the class name of the Throwable object
        String logText = new SimpleDateFormat(ERROR_LOGGER_PREFIX).format(Calendar.getInstance().getTime()) + t.getClass().toString();

//      if there is a message to log
        if(t.getMessage() != null && !t.getMessage().trim().equals("")){
//          we'll split that message
            String[] messageLines = t.getMessage().split("\\r?\\n");

//          log that there is a message we need to show
            logText += System.lineSeparator() + new SimpleDateFormat(ERROR_LOGGER_PREFIX).format(Calendar.getInstance().getTime()) + ERROR_LOGGER_STACKTRACE_MESSAGE_INTRO;

//          and log every line of the message
            for(String line: messageLines){
                logText += System.lineSeparator() + new SimpleDateFormat(ERROR_LOGGER_PREFIX).format(Calendar.getInstance().getTime()) + ERROR_LOGGER_STACKTRACE_MESSAGE_PREFIX + line;
            }
        }

//      then we'll log the stacktrace
        for(StackTraceElement element : t.getStackTrace()){
            logText += System.lineSeparator() + new SimpleDateFormat(ERROR_LOGGER_PREFIX).format(Calendar.getInstance().getTime()) + ERROR_LOGGER_STACKTRACE_PREFIX + element.toString();
        }

//      add a new line to the log text
        logText += System.lineSeparator();

//      append the log text to the actual log
        logContent += logText;
//      and indicate that there has been activity
        activity = true;
    }

    /**
     * This method clears the log internally.<br>
     * To make sure the actual log file doesn't exist anymore call {@link #writeLog()} after calling this method
     */
    public void clearLog(){
//      delete the logs content
        logContent = "";
//      and indicate that there has been activity
        activity = true;
    }

    /**
     * This method writes the content of the log that has been stored internally
     * to the external file.<br>
     * This will only happen if there has been activity.<br>
     * If the log is empty the log file will be deleted by this method.
     */
    public void writeLog(){
//      if there has been activity
        if(activity){
//          and the log is not empty
            if(!logContent.trim().equals("")) {
//              we'll write the content of the log to the log file
                try (BufferedWriter write = new BufferedWriter(new FileWriter(new File(logFilePath)))) {
                    write.write(logContent);
                } catch (IOException e) {}
            }
//          otherwise if the log is empty and the log file exists
            else if(new File(logFilePath).exists()){
//              we'll delete it
                new File(logFilePath).delete();
            }
        }

//      lastly we'll just indicate that there has been no activity since the last time saving
        activity = false;
    }

    /**
     * This method gives you the content of the current log file
     * @return the content of the log file
     */
    private String getLogFileContent(){
        try(BufferedReader read = new BufferedReader(new FileReader(new File(logFilePath)))){

            String temp;
            String content = "";

//          we'll read every line
            while((temp = read.readLine()) != null){
                content += temp + System.lineSeparator();
            }

//          and return the content if there has been no error
            return content;
        } catch (IOException e) {}

//      if there has been an error we'll return null
        return null;
    }

}
