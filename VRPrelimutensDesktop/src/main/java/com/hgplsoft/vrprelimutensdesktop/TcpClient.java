package com.hgplsoft.vrprelimutensdesktop;

import android.util.Log;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by hgabor47 on 2017. 07. 15..
 */

public class TcpClient {

    //public static final String SERVER_IP = "192.168.1.102"; //server IP address  Home Intra
    public static String SERVER_IP = "192.168.42.100"; //server IP address  Android USB
    //public static final String SERVER_IP = "192.168.43.100"; //server IP address Android WLAN
    public int SERVER_PORT = 9000;
    // message to send to the server

    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    //private BufferedReader mBufferIn;
    private InputStream mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(String ipaddress, int port, OnMessageReceived listener) {
        mMessageListener = listener;
        SERVER_IP = ipaddress;
        SERVER_PORT = port;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;

    }

    public void run() {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.e(MyRenderer.APPID,"Connecting");
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            try {
                Log.e(MyRenderer.APPID, "TCP Connected");
                mBufferIn = socket.getInputStream();
                mBufferOut  = new PrintWriter(socket.getOutputStream(), true);
                int charsRead = 0; byte[] buffer = new byte[4096]; //choose your buffer size if you need other than 1024
                while (mRun) {
                    charsRead = mBufferIn.read(buffer);
                    if (charsRead>0 && mMessageListener != null) {
                        mMessageListener.messageReceived(buffer,charsRead);}
                }
                Log.e(MyRenderer.APPID, "TCP Response from server '" );
            } catch (Exception e) {
                Log.e(MyRenderer.APPID, "TCP: Error 2", e);
            } finally {
                Log.e(MyRenderer.APPID, "TCP: Close ");
                try {
                    socket.close();
                } catch (Exception e){}
            }
        } catch (Exception e) {
            Log.e(MyRenderer.APPID, "TCP: Error 1", e);
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        //public void messageReceived(String message);
        public void messageReceived(byte[] buffer, int count);
    }

}
