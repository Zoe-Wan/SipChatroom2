package com.client.chatwindow;

import com.client.login.LoginController;
import com.client.util.SipLayerFacade;
import com.client.util.SipMessageListener;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.*;
import java.io.*;
//import java.net.Socket;
import java.text.ParseException;
import java.util.TooManyListenersException;

import static com.messages.MessageType.CONNECTED;
import static com.messages.MessageType.DISCONNECTED;

public class Listener implements SipMessageListener {

    private static final String HASCONNECTED = "has connected";

    private static String picture;
//    private Socket socket;
    public String serverIP;
    public int serverPort;
    public static String username;
    public ChatController controller;
    public static SipLayerFacade sipLayer;
//    private static ObjectOutputStream oos;
//    private InputStream is;
//    private ObjectInputStream input;
//    private OutputStream outputStream;
    Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(String hostname, int port, String username, String picture, ChatController controller) throws InvalidArgumentException, TransportNotSupportedException, TooManyListenersException, PeerUnavailableException, ObjectInUseException {
        this.serverIP = hostname;
        this.serverPort = port;
        Listener.username = username;
        Listener.picture = picture;
        this.controller = controller;
//        sipLayer = new SipLayerFacade("SERVER@"+hostname+":"+port,username,"127.0.0.1",1111);
        int userPort = ((int)(Math.random()*10))*1000+((int)(Math.random()*10))*100+((int)(Math.random()*10))*10+((int)(Math.random()*10));

        try {
            sipLayer = new SipLayerFacade("server@"+hostname+":"+port,username,"127.0.0.1",userPort);
        }catch (javax.sip.InvalidArgumentException e){
            // 端口号被占用，给加个1
            e.printStackTrace();
            sipLayer = new SipLayerFacade("server@"+hostname+":"+port,username,"127.0.0.1",++userPort);
        }
        sipLayer.addSipMessageListener(this);

        try {
            connect();
        }  catch (ParseException e) {
            e.printStackTrace();
        } catch (SipException e) {
            e.printStackTrace();
        }
    }
//    public void run() {
//        try {
//            socket = new Socket(hostname, port);
//            LoginController.getInstance().showScene();
//            outputStream = socket.getOutputStream();
//            oos = new ObjectOutputStream(outputStream);
//            is = socket.getInputStream();
//            input = new ObjectInputStream(is);
//
//
//        } catch (IOException e) {
//            LoginController.getInstance().showErrorDialog("Could not connect to server");
//            logger.error("Could not Connect");
//        }
//        logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

//        try {
//            connect();
//            logger.info("Sockets in and out ready!");
//            while (socket.isConnected()) {
//                Message message = null;
//                message = (Message) input.readObject();
//
//                if (message != null) {
//                    logger.debug("Message recieved:" + message.getMsg() + " MessageType:" + message.getType() + "Name:" + message.getName());
//                    switch (message.getType()) {
//                        case USER:
//                            controller.addToChat(message);
//                            break;
//                        case VOICE:
//                            controller.addToChat(message);
//                            break;
//                        case NOTIFICATION:
//                            controller.newUserNotification(message);
//                            break;
//                        case SERVER:
//                            controller.addAsServer(message);
//                            break;
//                        case CONNECTED:
//                            controller.setUserList(message);
//                            break;
//                        case DISCONNECTED:
//                            controller.setUserList(message);
//                            break;
//                        case STATUS:
//                            controller.setUserList(message);
//                            break;
//                    }
//                }
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//            controller.logoutScene();
//        }
//    }

    /* This method is used for sending a normal Message
     * @param msg - The message which the user generates
     */
    // 外部调用
    public static void send(String msg) throws ParseException, SipException, InvalidArgumentException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.USER);
        createMessage.setStatus(Status.AWAY);
        createMessage.setMsg(msg);
        createMessage.setPicture(picture);
        sipLayer.sendMessage(createMessage);
    }

    public static void close() throws ParseException, SipException, InvalidArgumentException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(DISCONNECTED);
        sipLayer.sendMessage(createMessage);
    }

    /* This method is used for sending a voice Message
 * @param msg - The message which the user generates
 */
    // 外部调用
    public static void sendVoiceMessage(byte[] audio) throws ParseException, SipException, InvalidArgumentException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.VOICE);
        createMessage.setStatus(Status.AWAY);
        createMessage.setVoiceMsg(audio);
        createMessage.setPicture(picture);
        sipLayer.sendMessage(createMessage);
    }

    /* This method is used for sending a normal Message
 * @param msg - The message which the user generates
 */
    // 外部调用
    public static void sendStatusUpdate(Status status) throws ParseException, SipException, InvalidArgumentException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.STATUS);
        createMessage.setStatus(status);
        createMessage.setPicture(picture);
        sipLayer.sendMessage(createMessage);
    }

    /* This method is used to send a connecting message */
    // 内部调用
    public static void connect() throws ParseException, SipException, InvalidArgumentException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(CONNECTED);
        createMessage.setMsg(HASCONNECTED);
        createMessage.setPicture(picture);
        sipLayer.sendMessage(createMessage);
    }

    @Override
    public void processReceivedMessage(String sender, Message message) {
        if (message != null) {
            logger.debug("Message recieved:" + message.getMsg() + " MessageType:" + message.getType() + "Name:" + message.getName());
            switch (message.getType()) {
                case USER:
                    controller.addToChat(message);
                    break;
                case VOICE:
                    controller.addToChat(message);
                    break;
                case NOTIFICATION:
                    controller.newUserNotification(message);
                    break;
                case SERVER:
                    controller.addAsServer(message);
                    break;
                case CONNECTED:
                    try {
                        LoginController.getInstance().showScene();
                    }catch (IOException e) {
                        LoginController.getInstance().showErrorDialog("Could not connect to server");
                        logger.error("Could not Connect");
                    }
                    controller.setUserList(message);
                    break;
                case DISCONNECTED:
                    controller.setUserList(message);
                    break;
                case STATUS:
                    controller.setUserList(message);
                    break;
            }
        }
    }

    @Override
    public void processError(String errorMessage) {

    }

    @Override
    public void processInfo(String infoMessage) {

    }
}
