package com.hgplsoft.vrprelimutensdesktop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by horvath3ga on 2017.10.19..
 */

public class TcpServer implements Runnable {
    public int SERVER_PORT = 9000;
    @Override
    public void run() {
        Socket socket = null;
        ServerSocket serverSocket=null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class CommunicationThread implements Runnable {

    private Socket clientSocket;

    private BufferedReader input;

    private PrintWriter mBufferOut;
    private InputStream mBufferIn;

    public CommunicationThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            mBufferIn = clientSocket.getInputStream();
            mBufferOut = new PrintWriter(clientSocket.getOutputStream(), true);


            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e56){}
    }

    public void run() {


        while (!Thread.currentThread().isInterrupted()) {

            try {

                String read = input.readLine();

                if (read == null ){
                    Thread.currentThread().interrupt();
                }else{
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    out.write("TstMsg");
                    //updateConversationHandler.post(new updateUIThread(read));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
