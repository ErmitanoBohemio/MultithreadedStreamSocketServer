/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.Messager;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author OscarFabianHP
 */
public class MultithreadedServer extends JFrame{
    private JTextField enterField;
    private JTextArea displayArea;
   
    private ServerSocket server;
    private Socket connection;
    
    private ExecutorService runServer;
    private List<Clients> clientsList;
    private int counterClients = 1;

    public MultithreadedServer() {
        super("multithreaded server");
        
        //create ExecutorService with a thread for each player
        runServer = Executors.newFixedThreadPool(10);
        
        clientsList = new ArrayList<Clients>(); //lista de clientes conectados al servidor
        
        /*try {
            server = new ServerSocket(12345);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }*/
        
        enterField = new JTextField();
        enterField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendDataToClients(e.getActionCommand()); //llama metodo que enviar mensajes 
                enterField.setText("");
            }
        });
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        add(enterField, BorderLayout.NORTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
        
        setSize(300, 150);
        setVisible(true);
        
    }
    
    public void runServer() {
        try {
            server = new ServerSocket(12345, 10);
            while (true) {
                try {
                    waitConnection();
                    counterClients++; //incrementa contador de clientes conectados al server
                } catch (EOFException eofException) {
                    eofException.printStackTrace();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1);
        }

    }
    
    //wait for connection to arrive, then display connection info
    private void waitConnection() throws IOException{
        displayMessage("SERVER>> Waiting connections " + clientsList.size() + " Connected");
        connection = server.accept();
        Clients client = new Clients(connection, counterClients);
        clientsList.add(client); //añade cliente al lista de clientes conectados al servidor
        runServer.execute(client); //ejecuta thread Runnable (run) 
        displayMessage("SERVER>> CLIENT " + counterClients + ", HostName: " + connection.getInetAddress().getHostName() + " Connected");
    }
    
    
    //metodo que muestra mensajes en el servidor
    private void displayMessage(String messageToDisplay){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                displayArea.append(messageToDisplay);
                displayArea.append(System.lineSeparator());
            }
        });
    }
    
    //metodo utilitario que enviar mensajes desde el servidor a cada uno de los clientes conectados
    private void sendDataToClients(String message){
        for(Clients client : clientsList)
            client.sendData("SERVER>> " + message);
        displayMessage("SERVER>> "+message); //muestra mensaje enviado a los clientes en app server
    }

    private class Clients implements Runnable{

        private Socket connection;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        private int idClient;
        
        //set up client thread
        public Clients(Socket socket, int id) {
            try {
                connection = socket;
                idClient = id;
                
                output = new ObjectOutputStream(connection.getOutputStream());
                output.flush();
                
                input = new ObjectInputStream(connection.getInputStream());
                
                displayMessage("SERVER>> Got I/O Streams for cliente "+ counterClients);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }

        @Override
        public void run() {
            String message = null;
            do{
                try {
                    message = (String) input.readObject(); //lee entrada desde el cliente conectado al socket (mensaje)
                    displayMessage("CLIENT "+ idClient + ">> " + message); //muestra mensaje del cliente en servidor
                    //sendData("CLIENT "+ idClient + ">> " + message); //llama metodo enviar mensaje a app cliente para mostrarlo en el cliente, no se necesita ya que esos mensajes ya se 
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }while(!message.equals("CLIENT>> TERMINATE"));
            closeConnectionClient();
        }
        
        //metodo utilitario para enviar mensaje desde el servidor a la app cliente conectado al socket
        private void sendData(String message){
            try {
                output.writeObject(message); //envia mensaje al cliente para que lo vea en su app
                output.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        private void closeConnectionClient(){
            try {
                input.close();
                output.close();
                connection.close();
                displayMessage("SERVER>> CLIENT "+ counterClients + ", HostName: " + this.connection.getInetAddress().getHostName() + " Disconnected");
                clientsList.remove(this); //remueve el cliente de la lista de clientes conectados al servidor
                --counterClients; //disminuye en uno contador clientes conectados
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
    } 
}
