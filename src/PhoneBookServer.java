/*
 *       - Server Program -
 *    PhoneBook Window Application
 *
 *       Author: Luka Mitrovic
 *       Date: 8 january 2021
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class PhoneBookServer extends JFrame implements ActionListener, Runnable  {

    private static final long serialVersionUID = 1L;

    static final int SERVER_PORT = 25000;

    private PhoneBook phoneBook;

    private JLabel clientLabel   = new JLabel("Reciever:");
    private JLabel messageLabel  = new JLabel("Command :");
    private JLabel phoneBookAreaLabel = new JLabel("Phone Book:");
    private JComboBox<ClientThread> clientMenu = new JComboBox<ClientThread>();
    private JTextField messageField = new JTextField(20);
    private JTextArea  phoneBookArea  = new JTextArea(15,20);
    private JScrollPane scroll = new JScrollPane(phoneBookArea,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);


    public static void main(String [] args){
        new PhoneBookServer();
    }

    PhoneBookServer(){
        super("SERVER");
        phoneBook = new PhoneBook();
        phoneBook.LOAD("/Users/lukamitrovic/IdeaProjects/PhoneBook/src/phoneBookFile.txt");
        setSize(300,380);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.add(clientLabel);
        clientMenu.setPrototypeDisplayValue(new ClientThread("#########################"));
        panel.add(clientMenu);
        panel.add(messageLabel);
        panel.add(messageField);
        messageField.addActionListener(this);
        phoneBookArea.setLineWrap(true);
        phoneBookArea.setWrapStyleWord(true);
        panel.add(phoneBookAreaLabel);
        phoneBookArea.setEditable(false);
        phoneBookArea.setText("--- Name ---- Number ----");
        phoneBookArea.setText(phoneBook.getTableFormat());
        panel.add(scroll);
        setContentPane(panel);
        setVisible(true);
        new Thread(this).start(); //Launching an additional thread
                                        // waiting for new clients
    }

    synchronized public void printReceivedMessage(ClientThread client, String message){
        String text = phoneBookArea.getText();
        phoneBookArea.setText(client.getName() + " >>> " + message + "\n" + text);
    }

    public String getCommandFromMessage(String message){
        for(int i = 0; i < message.length();++i){
            if(message.charAt(i) == ' '){
                return message.substring(0,i);
            }
        }
        return "ERROR";
    }

    public String getValueFromMessage(String message){
        for(int i = 0; i < message.length();++i){
            if(message.charAt(i) == ' '){
                return message.substring(i+1,message.length());
            }
        }
        return "ERROR";
    }
    public boolean singleValue(String value){
        for(int i = 0; i < value.length();++i){
            if(value.charAt(i) == ' '){
                return false;
            }
        }
        return true;
    }

    synchronized public void printSentMessage(ClientThread client, String message){
        String text = phoneBookArea.getText();
        String command = getCommandFromMessage(message);
        String value = getValueFromMessage(message);
        //GET COMMAND RECEIVED
        if(command == "GET" && singleValue(value) ){
            phoneBookArea.setText(phoneBook.GET(value));
        //PUT COMMAND RECEIVED
        }else if(command == "PUT" && !singleValue(value)){
                int separator = value.indexOf(' ');
                phoneBookArea.setText(phoneBook.PUT(
                            value.substring(0,separator),
                            value.substring(separator,value.length())));
        //REPLACE COMMAND RECEIVED
        }else if(command == "REPLACE" && !singleValue(value)){
                int separator = value.indexOf(' ');
                phoneBookArea.setText(phoneBook.REPLACE(
                        value.substring(0,separator),
                        value.substring(separator,value.length())));
        //DELETE COMMAND RECEIVED
        }else if (command == "DELETE" && singleValue(value)){
            phoneBookArea.setText(phoneBook.DELETE(value));
        //DELETE COMMAND RECEIVED
        }else if (command == "LIST"){
            phoneBookArea.setText(phoneBook.LIST());
        }

    }

    synchronized void addClient(ClientThread client){
        clientMenu.addItem(client);
    }

    synchronized void removeClient(ClientThread client){
        clientMenu.removeItem(client);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String message;
        Object source = event.getSource();
        if (source == messageField){
            ClientThread client = (ClientThread)clientMenu.getSelectedItem();
            if (client != null) {
                message = messageField.getText();
                printSentMessage(client, message);
                client.sendMessage(message);
            }
        }
        repaint();
    }

    @Override
    public void run() {
        boolean socket_created = false;

        // Initialization of network connections
        try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
            String host = InetAddress.getLocalHost().getHostName();
            System.out.println("Server is running on host:  " + host);
            socket_created = true;
        // End of initialization of network connections

            while (true) {  // Waiting for connection coming from client
                Socket socket = server.accept();
                if (socket != null) {
                    // Creates new thread for the client, which
                    // will be connected to the server
                    new ClientThread(this, socket);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            if (!socket_created) {
                JOptionPane.showMessageDialog(null, "Server socket cannot be created ");
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(null, "SERVER ERROR: Cannot connect to the server ");
            }
        }
    }

}

class ClientThread implements Runnable {
    private Socket socket;
    private String name;
    private PhoneBookServer myServer;

    private ObjectOutputStream outputStream = null;

    ClientThread(String prototypeDisplayValue){
        name = prototypeDisplayValue;
    }

    ClientThread(PhoneBookServer server, Socket socket) {
        myServer = server;
        this.socket = socket;
        new Thread(this).start();  // Creating an additional thread
                                         // for network communication management
    }

    public String getName(){ return name; }

    public String toString(){ return name; }

    public void sendMessage(String message){
        try {
            outputStream.writeObject(message);
            if (message.equals("exit")){
                myServer.removeClient(this);
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run(){
        String message;
        try( ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream()); )
        {
            outputStream = output;
            name = (String)input.readObject();
            myServer.addClient(this);
            while(true){
                message = (String)input.readObject();
                myServer.printReceivedMessage(this,message);
                if (message.equals("exit")){
                    myServer.removeClient(this);
                    break;
                }
            }
            socket.close();
            socket = null;
        } catch(Exception e) {
            myServer.removeClient(this);
        }
    }

}
