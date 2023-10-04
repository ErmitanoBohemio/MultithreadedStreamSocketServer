/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import javax.swing.JFrame;

/**
 *
 * @author OscarFabianHP
 */
//Ejecuta aplicacion de servidor de mesanjes multihilos

public class MultithreadedServerTest {
    public static void main(String[] args) {
        MultithreadedServer application = new MultithreadedServer();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.runServer();
    }
    
}
