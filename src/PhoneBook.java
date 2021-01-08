/*
 *       - Phone Book Class -
 *    PhoneBook Window Application
 *
 *       Author: Luka Mitrovic
 *       Date: 8 january 2021
 */

import java.io.*;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


public class PhoneBook {

    public ConcurrentHashMap<String, String> clientData = new ConcurrentHashMap<String,String>();

// Function LOAD assumes that
// every new contact starts on the new line
// and has a 'coma' separator between clientName and clientNumber.
// After loading to the file function
// returns updated ConcurrentHashMap<String, String> in String format.

    public String LOAD(String filename){
        try{
            File file = new File(filename);
            if(file.length() == 0){
                return null;
            }
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                // When separator appears String is split into 2 parts.
                // First one (before the separator) is the String clientName.
                // Second one (after the separator) is the String clientNumber.
                int separatorIndex = data.indexOf(',');
                clientData.put(data.substring(0,separatorIndex),data.substring(separatorIndex+1,data.length()));
            }
            reader.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return this.clientData.toString();
    }


    public String SAVE(String filename){
        try{
            //File will be rewritten
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false));
            Enumeration keys = this.clientData.keys();
            while(keys.hasMoreElements()){
                String keyToAppend = (String) keys.nextElement();
                writer.append(keyToAppend + "," + this.clientData.get(keyToAppend) + '\n');
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.clientData.toString();
    }

    public String GET(String name){
        return this.clientData.get(name);
    }

    public String PUT( String name, String number){
        this.clientData.put(name,number);

        return this.clientData.toString();
    }

    public String REPLACE(String name, String number){
        this.clientData.put(name,number);

       return this.clientData.toString();
    }

    public String DELETE(String name){
        this.clientData.remove(name);

        return this.clientData.toString();
    }
//  Because to each added new name a newLine separator
//  In the return statement last separator is deleted
    public String LIST(){
        String nameList = "";
        Enumeration keys = this.clientData.keys();
        while(keys.hasMoreElements()){
            nameList += keys.nextElement() + "\n";
        }
        return nameList.substring(0,nameList.length() - 1);
    }

}// End of class PhoneBook
