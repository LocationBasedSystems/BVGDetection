package de.htwberlin.f4.ai.ma.indoorroutefinder.beacon;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Ursprüngliche Version/Quelle: https://stackoverflow.com/questions/38162775/really-simple-tcp-client
 * Ich habe diesen Simplen TCP Client als Hülle benutzt und extrem Modifiziert
 *
 *Diese Klasse nutzt eine normale Verbindung. Der Verbindungsaufbau ist in der MainActivity
 */
public class TcpClient {

    public String SERVER_IP; //server IP address
    public static final int SERVER_PORT = 2901;

    boolean gettingAdmin = false;
    boolean expectingRawData = false;
    private int rawDataSize = 0;

    private Socket socket;

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(String serverIp, OnMessageReceived listener) {
        mMessageListener = listener;
        SERVER_IP = serverIp;
    }

    public void sendMessage(String messageIn) {

        final String message = messageIn;
        Thread thread = new Thread(new Runnable() {
            public void run() {

                if (message.startsWith(PROTOKOLL.cs_dataBegin)) {
                    String[] dates = message.split(":", 2);
                    File sdcard = Environment.getExternalStorageDirectory();
                    File f = new File(sdcard, "IndoorPositioning/Exported/" + dates[1]);
                    DataInputStream in = null;
                    try {
                        in = new DataInputStream(new FileInputStream(f));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    String challengeString = "db:" + f.length() + ":" + dates[1];
                    byte[] chBytes = new byte[0];
                    try {
                        chBytes = challengeString.getBytes("UTF8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    byte[] signature = null;
                    System.out.println("CH:" + challengeString);
                    try {
                        File sdcar = Environment.getExternalStorageDirectory();
                        File fKey = new File(sdcar, "BeaconPiPrivKey");
                        fKey.createNewFile();
                        FileInputStream keyfis = new FileInputStream(fKey);
                        byte[] encKey = new byte[keyfis.available()];
                        keyfis.read(encKey);

                        keyfis.close();

                        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
                        Signature dsa = Signature.getInstance("SHA1withDSA");
                        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
                        PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

                        dsa.initSign(privKey);
                        dsa.update(chBytes);
                        signature = dsa.sign();

                    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    send(challengeString + ":" + signature.length);
                    try {
                        String tmpo = "";
                        for (byte b: signature) {
                            tmpo += b;
                        }
                        Log.d("TCPClient", tmpo );
                        socket.getOutputStream().write(signature);
                        socket.getOutputStream().flush();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    byte[] arr = new byte[1024];
                    try {
                        int len = 0;
                        while ((len = in.read(arr)) != -1) {
                            socket.getOutputStream().write(arr);
                            socket.getOutputStream().flush();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    send("n");

                    send(PROTOKOLL.cs_dataEnd);
                } else {
                    send(message);
                }

            }
        });
        thread.start();

    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void send(String message) {
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
        mServerMessage = null;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket();
            do{
                try {
                    socket = new Socket(serverAddr, SERVER_PORT);
                } catch(ConnectException e){
                    Log.d("TCPClient", "catched inside");
                    Log.e("TCP", "S: Error", e);
                }
            }while(!socket.isConnected());

            try {

                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    if(expectingRawData) {
                        if(gettingAdmin) {
                            byte[] tmp = new byte[rawDataSize];
                            socket.getInputStream().read(tmp);
                            //System.out.println("Der recKey ist lang: "+tmp.length);
                            File sdcard = Environment.getExternalStorageDirectory();
                            File f = new File(sdcard, "BeaconPiPrivKey");
                            f.createNewFile();
                            FileOutputStream fo = new FileOutputStream(f);
                            fo.write(tmp);
                            gettingAdmin = false;
                            expectingRawData = false;
                            //return "Raw data";
                        }

                    }
                    else {
                        mServerMessage = mBufferIn.readLine();
                        if(mServerMessage.startsWith(PROTOKOLL.sc_sendingKey)) {
                            String[] dates = mServerMessage.split(":", 3);
                            rawDataSize = Integer.parseInt(dates[1]);
                            gettingAdmin=true;
                            expectingRawData = true;
                        }
                        if(mServerMessage.startsWith(PROTOKOLL.sc_dataBegin)) {
                            System.out.println("Entered data begin block");
                            String[] dates = mServerMessage.split(":", 3);
                            int dataLength = Integer.parseInt(dates[1]);
                            int readBytes = 0;
                            int remainingBytes = dataLength - readBytes;
                            File sdcard = Environment.getExternalStorageDirectory();
                            //sdcard.mkdir();
                            File f = new File(sdcard, "IndoorPositioning/Exported/" + dates[2]);
                            f.createNewFile();
                            FileOutputStream fo = new FileOutputStream(f);

                            byte[] arr = new byte[1024];
                            int datgroe = 0;
                            try {
                                while(remainingBytes > 0)
                                {
                                    if(remainingBytes < arr.length) {
                                        arr=new byte[remainingBytes];
                                    }
                                    int soViel = socket.getInputStream().read(arr);
                                    byte[] outArr = new byte[soViel];
                                    for (int i = 0; i<outArr.length;i++) {
                                        outArr[i] = arr[i];
                                    }

                                    System.out.println("Uebertragen: " + soViel);
                                    readBytes += soViel;

                                    datgroe += outArr.length;
                                    fo.write(outArr);

                                    remainingBytes = dataLength - readBytes;
                                    System.out.println("Remaining Bytes: " + remainingBytes);
                                    System.out.println("DataLength: " + dataLength);
                                    System.out.println("ReadBytes: " + readBytes);
                                }
                                System.out.println("datgroe: " + datgroe);
                                fo.close();

                                //clearing overlying stream
                                String tmpTrsh = mBufferIn.readLine();
                                while(!tmpTrsh.startsWith(PROTOKOLL.cs_dataEnd)) {
                                    tmpTrsh = mBufferIn.readLine();
                                }

                                send(PROTOKOLL.sc_ok);
                            } catch(Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                    }
                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
