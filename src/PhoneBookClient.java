/*
 *       - Client Program -
 *    PhoneBook Window Application
 *
 *       Author: Luka Mitrovic
 *       Date: 8 january 2021
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class PhoneBookClient extends JFrame implements ActionListener, Runnable {
    private static final long serialVersionUID = 1L;

    private JTextField messageField = new JTextField(20);
    private JTextArea  textArea     = new JTextArea(15,18);

    static final int SERVER_PORT = 25000;
    private String name;
    private String serverHost;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private PhoneBook phoneBook;

    PhoneBookClient(String name, String host) {
        super(name);
        this.name = name;
        this.serverHost = host;
        setSize(300, 310);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                try {
                    outputStream.close();
                    inputStream.close();
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            @Override
            public void windowClosed(WindowEvent event) {
                windowClosing(event);
            }
        });
        JPanel panel = new JPanel();
        JLabel messageLabel = new JLabel("Console Input:");
        JLabel textAreaLabel = new JLabel("Phone Book:");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        panel.add(messageLabel);
        panel.add(messageField);
        messageField.addActionListener(this);
        panel.add(textAreaLabel);
        JScrollPane scroll_bars = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll_bars);
        setContentPane(panel);
        phoneBook = new PhoneBook();
        textArea.setText("## NAME ## NUMBER ##");
        this.phoneBook.LOAD("/Users/lukamitrovic/IdeaProjects/PhoneBook/src/phoneBookFile.txt");
        textArea.setText(this.phoneBook.LIST());
        setVisible(true);
        new Thread(this).start();
    }

    public static void main(String[] args) {
        String name;
        String host;

        host = JOptionPane.showInputDialog("SERVER ADDRESS: ");
        name = JOptionPane.showInputDialog("CLIENT NAME: ");
        if (name != null && !name.equals("")) {
            new PhoneBookClient(name, host);
        }
    }

    synchronized public void printReceivedMessage(String message){
        String tmp_text = textArea.getText();
        textArea.setText(tmp_text + ">>> " + message + "\n");
    }

    synchronized public void printSentMessage(String message){
        String text = textArea.getText();
        textArea.setText(text + "<<< " + message + "\n");
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        String message;
        Object source = event.getSource();
        if (source == messageField)
        {
            try{
                message = messageField.getText();
                outputStream.writeObject(message);
                printSentMessage(message);
                if (message.equals("exit")){
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                    setVisible(false);
                    dispose();
                    return;
                }
            }catch(IOException e)
            { System.out.println("Client Thread " + e);
            }
        }
        repaint();
    }

    @Override
    public void run() {
        if (serverHost.equals("")) {
            // Connect tot the local computer
            serverHost = "localhost";
        }
        try{
            socket = new Socket(serverHost, SERVER_PORT);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(name);
        } catch(IOException e){
            JOptionPane.showMessageDialog(null, "Network connection for client cannot be created");
            setVisible(false);
            dispose();  // Clearing the graphic
            // Graphic window will not be created
            return;
        }
        try{
            while(true){
                String message = (String)inputStream.readObject();
                printReceivedMessage(message);
                if(message.equals("exit")){
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                    setVisible(false);
                    dispose();
                    break;
                }
            }
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Client Network connection has been disconected");
            setVisible(false);
            dispose();
        }
    }
}
