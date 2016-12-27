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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class may be used to easily store low-level data (meaning primitive
 * typed data including Strings).<br>
 * Each value saved is assigned to a key of the type String, with which you can get the
 * value.<br>
 * A key does not have to be unique within the whole preference, wvwn though it has to
 * within a datatype and it is good practice to keep your keys unique.<br>
 * A preference may be edited by obtaining a {@link Editor}-object by
 * calling the {@link #edit()}-method on the preference you wish to edit.
 * @author Jean-Pierre Hotz 10. May 2016
 */
public class Preference {
    /*
     * The file in which this preferences values were or are supposed to be saved in
     */
    private final File mXMLFile;
    /*
     * The name of this preference (must equal the filename of mXMLFile)
     */
    private String mName;

    /*
     * A HashMap for each datatype to make types easier to keep apart thus
     * preventing complicated typeof-Code in each get[...]-method
     * Though it makes it possible to multiply use a key.
     * Little fuin-fact: I tested the class SharedPreferences in Android
     * (doing the same thing as this class; this one is inspired by SharedPreferences)
     * and apparantly they saved all values in one HashMap but still didn't include any
     * typeof-testing, which makes it give you a ClassCastException when using a
     * key which has a value of another datatype than the methods return-datatype.
     * TODO: Prevent a key from being assigned multiple times
     */
    private final HashMap<String, Boolean> mBooleanValues;
    private final HashMap<String, Character> mCharValues;
    private final HashMap<String, String> mStringValues;
    private final HashMap<String, Byte> mByteValues;
    private final HashMap<String, Short> mShortValues;
    private final HashMap<String, Integer> mIntValues;
    private final HashMap<String, Long> mLongValues;
    private final HashMap<String, Float> mFloatValues;
    private final HashMap<String, Double> mDoubleValues;

    /**
     * This constructor gives you the Preference with the given name in the
     * default-folder, which is: "&lt;first root directory&gt;\preferences\"
     * (or as example in Windows "C:\preferences\").<br>
     * The name of the preference only differentiates which dataset to use.<br>
     * You may create a subdirectory in the default-directory for your application
     * by doing following (please be aware that the subdirectories are then part of
     * the preferences name):<br>
     * <pre><code>
     *      name of the preference = "bar"
     *      first folders name = "foo"
     *      second folders name = "lol"
     *      =&gt; prefName = "foo" + File.separatorChar + "lol" + File.separatorChar + "bar"
     *      =&gt; (under windows) "foo\\lol\\bar"
     * </code></pre>
     * If the directories are not existent yet they will be created.<br>
     * If the preference itself doesn't yet exist it will be created without any values.
     * @param prefName the name of the preference you want
     */
    public Preference(String prefName) {
        mBooleanValues = new HashMap<>();
        mCharValues = new HashMap<>();
        mStringValues = new HashMap<>();
        mByteValues = new HashMap<>();
        mShortValues = new HashMap<>();
        mIntValues = new HashMap<>();
        mLongValues = new HashMap<>();
        mFloatValues = new HashMap<>();
        mDoubleValues = new HashMap<>();

        mXMLFile = new File(File.listRoots()[0].getAbsolutePath()
                + File.separatorChar
                + "preferences"
                + File.separatorChar
                + prefName + ".xml");

        initializeValues(prefName);
    }

    /**
     * This constructor gives you the preference with the given name in the folder
     * whose path is given.<br>
     * Please note:<br>
     * <pre><code>
     *      The folders path has to be absolute:
     *      e.g.: "C:\Users\Admin\Documents\MyApplication\preferences\"
     *
     *      It might be good practice to keep your applications preference-folder-path
     *      as a constant somewhere and if you need a subdirectory just add the directories
     *      to the beginning of the preferences name (see {@link #Preference(java.lang.String)}):
     *
     *      name to the preferences name:
     *      name of the preference = "bar"
     *      first folders name = "foo"
     *      second folders name = "lol"
     *      =&gt; prefName = "foo" + File.separatorChar + "lol" + File.separatorChar + "bar"
     *      =&gt; (under windows) "foo\\lol\\bar"
     * </code></pre>
     * The name of the preference only differentiates which dataset to use.<br>
     * If the directories are not existent yet they will be created.<br>
     * If the preference itself doesn't yet exist it will be created without any values.
     * @param folder the folder in which the preference you want lies
     * @param prefName the name of the preference you want
     */
    public Preference(String folder, String prefName) {
        mBooleanValues = new HashMap<>();
        mCharValues = new HashMap<>();
        mStringValues = new HashMap<>();
        mByteValues = new HashMap<>();
        mShortValues = new HashMap<>();
        mIntValues = new HashMap<>();
        mLongValues = new HashMap<>();
        mFloatValues = new HashMap<>();
        mDoubleValues = new HashMap<>();

        if(!folder.endsWith(File.separator)){
            folder += File.separator;
        }

        mXMLFile = new File(folder + prefName + ".xml");

        initializeValues(prefName);
    }

    /**
     * This method checks whether the preference already exists.<br>
     * If it does it loads the content (in form of text) and then extracts all
     * the values out of that text.<br>
     * If it doesn't it simply writes the empty XML-file to the preference
     * @param prefName the name of the preference (to check whether it is
     *                 equal to the real name of the preference in the file)
     * @throws RuntimeException if the content of the file has been detected to have been changed
     */
    private void initializeValues(String prefName){
        if(mXMLFile.exists()){//                                        if the preference exists
            String fileContent = "";
            BufferedReader read = null;

            try {
                read = new BufferedReader(new FileReader(mXMLFile));
                String temp;

                while ((temp = read.readLine()) != null) {//            we read in the text
                    fileContent += temp + "\n";
                }
            } catch (IOException ex) {
                ex.printStackTrace();
//                System.out.println(ex.getMessage());
            } finally {
                if(read != null){
                    try {
                        read.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
//                        System.out.println(ex.getMessage());
                    }
                }
            }

//                                                                      and extract the actual values (with updateContent([...]))
            if (!updateContent(fileContent) || !prefName.equals(mName)) {
                throw new RuntimeException("The file which contains the data was illegally changed!");
            }
        }else{//                                                        if it doesn't exist
            mXMLFile.getParentFile().mkdirs();//                        we create the directory
            mName = prefName;
            this.new Editor().commit();//                               and save the empty XML-file
        }
    }

    /**
     * This method updates the values of the preference, by extracting the single values
     * out of the given text, which should be the content of the XML-file.
     * @param content the content of the XML-file
     * @return whether the update was successful
     */
    private boolean updateContent(String content) {
        try {
            Token prefToken = Token.getTokensInside(content)[0];//              we get the global parent
            mName = prefToken.getAttribute("name");//                           whose name-attribute should equal the filename

            Token[] valueranges = prefToken.getTokensInside();//                load the single datatype-trees

            Token[][] values = new Token[valueranges.length][];
            for(int i = 0; i < valueranges.length; i++){
                values[i] = valueranges[i].getTokensInside();//                 load all the values as Token-objects
            }

            mBooleanValues.clear();
            mCharValues.clear();
            mStringValues.clear();
            mByteValues.clear();
            mShortValues.clear();
            mIntValues.clear();
            mLongValues.clear();
            mFloatValues.clear();
            mDoubleValues.clear();

            for(int valrange = 0; valrange < values.length; valrange++){//      assign every key with its value in its datatype
                for(Token t : values[valrange]){
                    switch(valrange){
                        case 0:
                            mBooleanValues.put(t.getAttribute("key"), Boolean.parseBoolean(t.getInterior()));
                            break;
                        case 1:
                            mCharValues.put(t.getAttribute("key"), t.getInterior().charAt(0));
                            break;
                        case 2:
                            mStringValues.put(t.getAttribute("key"), t.getInterior());
                            break;
                        case 3:
                            mByteValues.put(t.getAttribute("key"), Byte.parseByte(t.getInterior()));
                            break;
                        case 4:
                            mShortValues.put(t.getAttribute("key"), Short.parseShort(t.getInterior()));
                            break;
                        case 5:
                            mIntValues.put(t.getAttribute("key"), Integer.parseInt(t.getInterior()));
                            break;
                        case 6:
                            mLongValues.put(t.getAttribute("key"), Long.parseLong(t.getInterior()));
                            break;
                        case 7:
                            mFloatValues.put(t.getAttribute("key"), Float.parseFloat(t.getInterior()));
                            break;
                        case 8:
                            mDoubleValues.put(t.getAttribute("key"), Double.parseDouble(t.getInterior()));
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
//            System.out.println(ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * This method gives you either the boolean value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public boolean getBoolean(String key, boolean def) {
        if (mBooleanValues.containsKey(key)) {
            return mBooleanValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the char value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public char getChar(String key, char def) {
        if (mCharValues.containsKey(key)) {
            return mCharValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the String value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public String getString(String key, String def) {
        if (mStringValues.containsKey(key)) {
            return mStringValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the byte value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public byte getByte(String key, byte def) {
        if (mByteValues.containsKey(key)) {
            return mByteValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the short value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public short getShort(String key, short def) {
        if (mShortValues.containsKey(key)) {
            return mShortValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the int value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public int getInt(String key, int def) {
        if (mIntValues.containsKey(key)) {
            return mIntValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the long value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public long getLong(String key, long def) {
        if (mLongValues.containsKey(key)) {
            return mLongValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the float value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public float getFloat(String key, float def) {
        if (mFloatValues.containsKey(key)) {
            return mFloatValues.get(key);
        }
        return def;
    }

    /**
     * This method gives you either the double value saved under the given key, or the given
     * default-value if given key has no assigned value in this datatype
     * @param key the key of the wanted value
     * @param def the default-value which is returned if the key has no value
     * @return either the value with given key or the default-value
     */
    public double getDouble(String key, double def) {
        if (mDoubleValues.containsKey(key)) {
            return mDoubleValues.get(key);
        }
        return def;
    }

    /**
     * This method shows you whether there is a value of any datatype assigned to
     * the given key
     * @param key the key which is to be checked, whether it has a value assigned to it
     * @return whether there is a value assigned to given key
     */
    public boolean contains(String key) {
        return mBooleanValues.containsKey(key) || mCharValues.containsKey(key)
                || mStringValues.containsKey(key) || mByteValues.containsKey(key)
                || mShortValues.containsKey(key) || mIntValues.containsKey(key)
                || mLongValues.containsKey(key) || mFloatValues.containsKey(key)
                || mDoubleValues.containsKey(key);
    }

    /**
     * This method  gives you a {@link Editor}-object to edit the Preference
     * you called this method on.
     * @return a {@link Editor}-object to edit the Preference
     */
    public Preference.Editor edit() {
        return new Preference.Editor();
    }

    /**
     * This class is used to edit a Preference without it losing its consistency.<br>
     * As long as the changes of this object has not yet been applied or commited the old values
     * of the Preference remain valid.
     */
    public class Editor {

        private static final String HEADER = "<!--\r\n" +
                "    DO NOT MODIFY THIS FILE OR ITS FILENAME!\r\n" +
                "    IT WILL NOT BE GUARANTEED TO BE USABLE WITH THE FOR THIS FILE AWAITED ALGORITHM ANYMORE!\r\n" +
                " \r\n" +
                "    Copyright 2016 Jean-Pierre Hotz\r\n" +
                "-->\r\n";

        /*
         * A HashMap to cache the values of the preference and changes to it.
         * TODO: Prevent a key from being assigned multiple times
         */
        private final HashMap<String, Boolean> mTempBooleanValues;
        private final HashMap<String, Character> mTempCharValues;
        private final HashMap<String, String> mTempStringValues;
        private final HashMap<String, Byte> mTempByteValues;
        private final HashMap<String, Short> mTempShortValues;
        private final HashMap<String, Integer> mTempIntValues;
        private final HashMap<String, Long> mTempLongValues;
        private final HashMap<String, Float> mTempFloatValues;
        private final HashMap<String, Double> mTempDoubleValues;

        private Editor() {
            mTempBooleanValues = new HashMap<>(mBooleanValues);//               cache all the values currently saved in the Preference
            mTempCharValues = new HashMap<>(mCharValues);
            mTempStringValues = new HashMap<>(mStringValues);
            mTempByteValues = new HashMap<>(mByteValues);
            mTempShortValues = new HashMap<>(mShortValues);
            mTempIntValues = new HashMap<>(mIntValues);
            mTempLongValues = new HashMap<>(mLongValues);
            mTempFloatValues = new HashMap<>(mFloatValues);
            mTempDoubleValues = new HashMap<>(mDoubleValues);
        }

        /**
         * This method deletes all the values in this Editor
         * @return the modified Editor
         */
        public Preference.Editor clear(){
            mTempBooleanValues.clear();
            mTempCharValues.clear();
            mTempStringValues.clear();
            mTempByteValues.clear();
            mTempShortValues.clear();
            mTempIntValues.clear();
            mTempLongValues.clear();
            mTempFloatValues.clear();
            mTempDoubleValues.clear();

            return this;
        }

        /**
         * This method removes <u>all</u> values with given key from the Editor
         * @param key the key whose values are supposed to be deleted
         * @return the modified Editor
         */
        public Preference.Editor remove(String key){
            mTempBooleanValues.remove(key);
            mTempCharValues.remove(key);
            mTempStringValues.remove(key);
            mTempByteValues.remove(key);
            mTempShortValues.remove(key);
            mTempIntValues.remove(key);
            mTempLongValues.remove(key);
            mTempFloatValues.remove(key);
            mTempDoubleValues.remove(key);

            return this;
        }

        /**
         * This method puts a given boolean value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putBoolean(String key, boolean val){
            mTempBooleanValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given char value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putChar(String key, char val){
            mTempCharValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given String value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putString(String key, String val){
            mTempStringValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given byte value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putByte(String key, byte val){
            mTempByteValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given short value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putShort(String key, short val){
            mTempShortValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given int value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putInt(String key, int val){
            mTempIntValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given long value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putLong(String key, long val){
            mTempLongValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given float value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putFloat(String key, float val){
            mTempFloatValues.put(key, val);

            return this;
        }

        /**
         * This method puts a given double value with given key to the Editor
         * @param key the key to assign the value to
         * @param val the value to be assigned to the key
         * @return the modified Editor
         */
        public Preference.Editor putDouble(String key, double val){
            mTempDoubleValues.put(key, val);

            return this;
        }

        /**
         * This method starts a thread to apply the changes you did to this Editor
         * to the XML-file and the Preference itself.<br>
         * You should definitely consider using this method in case you have many values to save
         * and don't need to access them immediately after, since it will relieve
         * the strain on your current thread.
         */
        public void apply(){
            new Thread(() -> {
                try{
                    writeToFile();
                    initializeValues(mName);
                }catch(Exception exc){}
            }).start();
        }

        /**
         * This method immediately commits the changes you did to this Editor to the XML-file
         * and the Preference itself.<br>
         * In the end it shows you whether the values were successfully saved.<br>
         * You should only use this method if you either need to access the changed values immediately
         * after being changed or if you need feedback whether the values really were
         * successfully saved.
         * @return whether the values were successfully saved
         */
        public boolean commit(){
            try{
                writeToFile();
                initializeValues(mName);
            }catch(Exception exc){
                return false;
            }

            return true;
        }


        /**
         * This method saves the changed values to the XML-file in following form
         * (the order of datatypes is of importance; the order of the values is not):
         * <pre><code>
         * &lt;preference name="preferenceName"&gt;
         *     &lt;boolean_vals&gt;
         *         &lt;-- template for every value --&gt;
         *         &lt;value key="key"&gt;value&lt;/value&gt;
         *     &lt;/boolean_vals&gt;
         *
         *     &lt;char_vals&gt;
         *     &lt;/char_vals&gt;
         *
         *     &lt;string_vals&gt;
         *     &lt;/string_vals&gt;
         *
         *     &lt;byte_vals&gt;
         *     &lt;/byte_vals&gt;
         *
         *     &lt;short_vals&gt;
         *     &lt;/short_vals&gt;
         *
         *     &lt;int_vals&gt;
         *     &lt;/int_vals&gt;
         *
         *     &lt;long_vals&gt;
         *     &lt;/long_vals&gt;
         *
         *     &lt;float_vals&gt;
         *     &lt;/float_vals&gt;
         *
         *     &lt;double_vals&gt;
         *     &lt;/double_vals&gt;
         * &lt;/preference&gt;
         * </code></pre>
         * @throws  IOException see {@link FileWriter#FileWriter(File)}, {@link BufferedWriter#write(String)}, {@link BufferedWriter#close()}
         *          for possible reasons for this Exception being thrown
         */
        private void writeToFile() throws IOException{
            String content = HEADER + "<preference name=\"" + mName + "\">\n\n";

            content += "\t<boolean_vals>\n\n";
            for(String key : mTempBooleanValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempBooleanValues.get(key) + "</value>\n";
            }
            content += "\n\t</boolean_vals>\n\n";

            content += "\t<char_vals>\n\n";
            for(String key : mTempCharValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempCharValues.get(key) + "</value>\n";
            }
            content += "\n\t</char_vals>\n\n";

            content += "\t<string_vals>\n\n";
            for(String key : mTempStringValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempStringValues.get(key) + "</value>\n";
            }
            content += "\n\t</string_vals>\n\n";

            content += "\t<byte_vals>\n\n";
            for(String key : mTempByteValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempByteValues.get(key) + "</value>\n";
            }
            content += "\n\t</byte_vals>\n\n";

            content += "\t<short_vals>\n\n";
            for(String key : mTempShortValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempShortValues.get(key) + "</value>\n";
            }
            content += "\n\t</short_vals>\n\n";

            content += "\t<int_vals>\n\n";
            for(String key : mTempIntValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempIntValues.get(key) + "</value>\n";
            }
            content += "\n\t</int_vals>\n\n";

            content += "\t<long_vals>\n\n";
            for(String key : mTempLongValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempLongValues.get(key) + "</value>\n";
            }
            content += "\n\t</long_vals>\n\n";

            content += "\t<float_vals>\n\n";
            for(String key : mTempFloatValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempFloatValues.get(key) + "</value>\n";
            }
            content += "\n\t</float_vals>\n\n";

            content += "\t<double_vals>\n\n";
            for(String key : mTempDoubleValues.keySet()){
                content += "\t\t<value key=\"" + key + "\">" + mTempDoubleValues.get(key) + "</value>\n";
            }
            content += "\n\t</double_vals>\n\n";

            content += "</preference>";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(mXMLFile))) {
                writer.write(content);
                writer.close();
            }
        }
    }

    /**
     * This class represents a xml-token with attributes and something written between
     * the opening- and closing-tags.
     */
    private static class Token {
        private final HashMap<String, String> mAttributes;
        private final String mInterior;

        /**
         * This constructor constructs a Token with given attributes and the given text
         * inbetween its tags
         * @param attr a HashMap containing the attributes
         * @param inter the text between the tags
         */
        private Token(HashMap<String, String> attr, String inter){
            mAttributes = new HashMap<>(attr);
            mInterior = inter;
        }

        /**
         * This method gives you the tokens inside the token you called this method on
         * @return the tokens inside the token you called this method on
         */
        public Token[] getTokensInside(){
            return Token.getTokensInside(mInterior);
        }

        /**
         * This method gives you only (but all) tokens on top-level of the given text.<br>
         * Though you must assert that there is no token with the same name inside of a token.
         * @param tokenize the text you want the tokens from
         * @return all tokens on the top-level of the given text
         */
        public static Token[] getTokensInside(String tokenize){

            // remove all the comments inside the xml-files
            int com_beg;
            int com_end;

            for(int i = 0; i < tokenize.length(); i++) {
                if(tokenize.substring(i).startsWith("<!--")) {
                    com_beg = i;
                    com_end = i;

                    while(!tokenize.substring(0, com_end).endsWith("-->")) com_end++;

                    tokenize = tokenize.substring(0, com_beg) + tokenize.substring(com_end);
                }
            }


            ArrayList<Token> tokens = new ArrayList<>();

            int beg = 0;
            int name_end;
            int end = 0;

            while(beg < tokenize.length()){// we repeat until the beginning is at the end of the text
                while(beg < tokenize.length() && tokenize.charAt(beg) != '<'){// we look for the beginning-tag
                    beg++;
                }

                name_end = beg;
                while(name_end < tokenize.length() && tokenize.charAt(name_end) != ' ' && tokenize.charAt(name_end) != '>'){
                    name_end++;
                }

                // if there is no more token before the end of the text we end the searching
                if(beg >= tokenize.length() || name_end >= tokenize.length())
                    break;

                // we obtain the name of the beginning-tag
                String name = tokenize.substring(beg + 1, name_end);

                //with the name of the beginning-tag we search for the end-tag
                while(end < tokenize.length() && !tokenize.substring(end).startsWith("</" + name)){
                    end++;
                }

                while(end < tokenize.length() && tokenize.charAt(end) != '>'){
                    end++;
                }

                // if we found the end of the token before the end of the text
                if(end > beg && end < tokenize.length()){
                    // we try to obtain the actual token in this text section
                    Token tempToken = getTokenIn(tokenize.substring(beg, end + 1));

                    // if it was successfully obtained we add it to the list
                    if(tempToken != null)
                        tokens.add(tempToken);
                }

                beg = end;
            }

            // then we convert the ArrayList into an Array
            Token[] toRet = new Token[tokens.size()];
            for(int i = 0; i < toRet.length; i++){
                toRet[i] = tokens.get(i);
            }

            return toRet;
        }

        /**
         * This method gives you the top-level token in given string assuming that there only is one.
         * @param tokenize the string with the token
         * @return the token in the string
         */
        private static Token getTokenIn(String tokenize){
            int beg_beg = 0;
            int name_end = 0;
            int beg_end = 0;
            int end_beg = tokenize.length() - 1;
            int end_end = tokenize.length() - 1;

            // we search for the beginning-tag
            while (tokenize.charAt(beg_beg) != '<' && beg_beg < tokenize.length()) {
                beg_beg++;
            }

            while (tokenize.charAt(beg_end) != '>' && beg_end < tokenize.length()) {
                beg_end++;
            }

            // we search for the end-tag
            while (tokenize.charAt(end_beg) != '<' && end_beg >= 0) {
                end_beg--;
            }

            while (tokenize.charAt(end_end) != '>' && end_end >= 0) {
                end_end--;
            }

            // and we search for the name of the beginning-tag
            while(tokenize.charAt(name_end) != ' ' && tokenize.charAt(name_end) != '>' && name_end < tokenize.length()){
                name_end++;
            }

            // we obtain the names of the tags
            String beginningTag = tokenize.substring(beg_beg + 1, name_end);
            String endingTag = tokenize.substring(end_beg + 2, end_end);

            HashMap<String, String> attributes = new HashMap<>();

            if (name_end != beg_end) {// if there are attributes
                String attributeString = tokenize.substring(name_end + 1, beg_end);

                // TODO: Change since there might be a space in the key :/
                String[] attr = splitAttributes(attributeString);
//                        attributeString.split(" ");// we separate all the attributes
                for (int i = 0; i < attr.length; i++) {
                    attr[i] = attr[i].trim();

                    int key_beg = 0;
                    int key_end = 0;
                    int val_beg = 0;
                    int val_end = attr[i].length() - 1;

                    // search for the key
                    while (attr[i].charAt(key_end) != '=' && key_end < attr[i].length()) {
                        key_end++;
                    }

                    // search for its value
                    while (attr[i].charAt(val_beg) != '\"' && val_beg < attr[i].length()) {
                        val_beg++;
                    }

                    while (attr[i].charAt(val_end) != '\"' && val_end >= 0) {
                        val_end--;
                    }

                    // obtain key and value
                    String key = attr[i].substring(key_beg, key_end);
                    String val = attr[i].substring(val_beg + 1, val_end);

                    // and add those to the attributes
                    attributes.put(key, val);
                }
            }

            // then we obtain the interior
            String interior = tokenize.substring(beg_end + 1, end_beg);

            // if there's something weird going on we throw a RuntimeException
            if (tokenize.charAt(end_beg + 1) != '/' || !beginningTag.equals(endingTag)) {
                throw new RuntimeException("The file which contains the data was illegally changed!");
            }

            // otherwise we return a token with the determined attributes and interior
            return new Token(attributes, interior);
        }

        private static String[] splitAttributes(String attrString) {
            ArrayList<String> attrs = new ArrayList<>();

            while(attrString.length() > 0){
                int i = 0;
                int ctr = 0;
                boolean end = false;

                while(i < attrString.length() && !end){
                    i++;
                    if(attrString.charAt(i) == '\"'){
                        end = ctr > 0;
                        ctr++;

                        if(end){
                            i++;
                        }
                    }
                }

                String attr = attrString.substring(0, i);
                attrString = attrString.substring(i);
                if(!attr.trim().equals("")){
                    attrs.add(attr);
                }
            }

            String[] attrsToReturn = new String[attrs.size()];
            for(int i = 0; i < attrsToReturn.length; i++) {
                attrsToReturn[i] = attrs.get(i);
            }

            return attrsToReturn;
        }

        /**
         * This method gives you the attribute of the token with given key
         * @param key the key whose value you want
         * @return the attribute with given key
         */
        public String getAttribute(String key) {
            return mAttributes.get(key);
        }

        /**
         * This method gives you what was written inside the tag
         * @return the interior
         */
        public String getInterior(){
            return mInterior;
        }
    }
}
