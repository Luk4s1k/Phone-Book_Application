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

/*  ---------------- TESTING FORM ---------------------------------- */
//    public static void main(String [] args){
//        PhoneBook phoneBook = new PhoneBook();
//        System.out.println(phoneBook.clientData.toString());
//        System.out.println(phoneBook.LOAD("/Users/lukamitrovic/IdeaProjects/PhoneBook/src/phoneBookFile.txt"));
//        System.out.println(phoneBook.GET("Luka"));
//        System.out.println(phoneBook.REPLACE("Luka", "145"));
//        System.out.println(phoneBook.LIST());
//        System.out.println(phoneBook.clientData.toString());
//    }
/* ----------------------------------------------------------------- */

// Function LOAD assumes that
// every new contact starts on the new line
// and has a 'coma' separator between clientName and clientNumber.
// After loading to the file function
// Return 'OK' communicate if the file is loaded
// Error is printed if the File wasn't found
// Error can occur if file is empty

    public String LOAD(String filename){
        try{
            File file = new File(filename);
            if(file.length() == 0){
                return "ERROR FILE IS EMPTY";
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
            return "ERROR FILE NOT FOUND";
        }
        return "OK";
    }

//"OK" - the command SAVE was performed correctly.
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
            return "ERROR CANNOT WRITE TO THE FILE";
        }
        return "OK";
    }

//"OK name" - the command GET was performed correctly.
    public String GET(String name){
        return "OK " + this.clientData.get(name);
    }
//"OK" - the command PUT was performed correctly,
    public String PUT( String name, String number){
        this.clientData.put(name,number);

        return "OK";
    }
//"OK" - the command REPLACE was performed correctly.
    public String REPLACE(String name, String number){
        this.clientData.put(name,number);

        return "OK";
    }
//"OK" - the command DELETE was performed correctly.
    public String DELETE(String name){
        this.clientData.remove(name);

        return "OK";
    }

//"OK name1 name2 ..." - the LIST command was performed correctly.
// The message contains a list of names of people remembered in the collection.
    public String LIST(){
        String nameList = "";
        Enumeration keys = this.clientData.keys();
        while(keys.hasMoreElements()){
            nameList += keys.nextElement() + " ";
        }
        return "OK " + nameList;
    }
// Makes easier to print in the expose format
// --- " Name " ----- " Number " ------
    public String getTableFormat(){
        String tableToPrint = "";
        Enumeration keys = this.clientData.keys();
        while(keys.hasMoreElements()){
            String clientName = (String) keys.nextElement();
            tableToPrint += "\t" + clientName + this.clientData.get(clientName) + '\n';
        }
        return tableToPrint.substring(0,tableToPrint.length() - 1);
    }

}// End of class PhoneBook
