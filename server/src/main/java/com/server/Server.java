package com.server;

import com.exception.DuplicateUsernameException;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import com.messages.User;
import com.util.SipLayerFacade;
import com.util.SipMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TooManyListenersException;

public class Server implements SipMessageListener {

    /* Setting up variables */
    private static final int PORT = 8080;
//    HashMap实现name（用户名）对User（用户）的反查
    private static final HashMap<String, User> names = new HashMap<>();
    // writers 用于管理对每个客户端的写入流，虽然写入流在sip中没有必要，但是这个也是对在线用户的管理，所以在删除writers需要用另一个东西来完成这个功能
//    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    // sipClients 用于代替writers的功能，保存在线用户
    private static HashSet<String> sipClients = new HashSet<>();
    private static ArrayList<User> users = new ArrayList<>();
    static Logger logger = LoggerFactory.getLogger(Server.class);
    static SipLayerFacade sipLayer;

    public Server() {
        try {
            sipLayer = new SipLayerFacade("SERVER", "localhost", 8080);
            sipLayer.addSipMessageListener(this);
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        } catch (TransportNotSupportedException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ObjectInUseException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
    }
    //从发来的消息里解析sip地址，如"Alias" <sip:Alias@127.0.0.1:8090>
    public String getSipAddress(String sender) {
        String temp = sender.substring(sender.indexOf('<'), sender.indexOf('>'));
        temp = temp.substring(temp.indexOf(':')+1);
        return temp;
    }
    public static void main(String[] args) throws Exception {
        logger.info("The chat server is running.");
//        ServerSocket listener = new ServerSocket(PORT);
//        try {
//            while (true) {
//                new Handler(listener.accept()).start();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            listener.close();
//        }
        new Server();
    }

    @Override
    public void processReceivedMessage(String sender, Message message) throws ParseException, InvalidArgumentException, SipException, DuplicateUsernameException {
        if (message != null) {
            logger.info(message.getType() + " - " + message.getName() + ": " + message.getMsg());
            switch (message.getType()) {
                case USER:
                    write(message);
                    break;
                case VOICE:
                    write(message);
                    break;
                case CONNECTED:
                    checkDuplicateUsername(getSipAddress(sender), message);
                    addToList(getSipAddress(sender));
                    sendNotification(message);
                    break;
                case DISCONNECTED:
                    closeConnections(getSipAddress(sender));
                    break;
                case STATUS:
                    changeStatus(message);
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

    /*
     * Creates and sends a Message type to the listeners.
     * 工具类？这个要重写
     * 注意这里每发送的一个消息都会附带当前在线人数
     */
    private void write(Message msg) throws ParseException, SipException, InvalidArgumentException {
        for (String toClient : sipClients) {
            msg.setUserlist(names);
            msg.setUsers(users);
            msg.setOnlineCount(names.size());
            sipLayer.sendMessage(toClient, msg);
        }
    }

    // 处理用户加入事件：更新在线用户组并将反馈信息通知加入者
    private Message addToList(String to) throws ParseException, InvalidArgumentException, SipException {
//        User user = new User();
//        user.setName(message.getName());
//        user.setPicture(message.getPicture());
//        user.setStatus(Status.ONLINE);
//        users.add(user);
        Message msg = new Message();
        msg.setMsg("Welcome, You have now joined the server! Enjoy chatting!");
        msg.setType(MessageType.CONNECTED);
        msg.setName("SERVER");
        msg.setUserlist(names);
        msg.setUsers(users);
        msg.setOnlineCount(names.size());
        sipLayer.sendMessage(to,msg);
        return msg;
    }

    // 改变在线状态
    private Message changeStatus(Message inputmsg) throws ParseException, InvalidArgumentException, SipException {
        logger.debug(inputmsg.getName() + " has changed status to  " + inputmsg.getStatus());
        Message msg = new Message();
        msg.setName(inputmsg.getName());
        msg.setType(MessageType.STATUS);
        msg.setMsg("");
        User userObj = names.get(inputmsg.getName());
        userObj.setStatus(inputmsg.getStatus());
        write(msg);
        return msg;
    }

    // 处理用户加入：通知组内人员
    private Message sendNotification(Message firstMessage) throws ParseException, InvalidArgumentException, SipException {
        Message msg = new Message();
        msg.setMsg("has joined the chat.");
        msg.setType(MessageType.NOTIFICATION);
        msg.setName(firstMessage.getName());
        msg.setPicture(firstMessage.getPicture());
        write(msg);
        return msg;
    }

    // 处理用户加入：处理消息
    private void checkDuplicateUsername(String from, Message firstMessage) throws DuplicateUsernameException {
        logger.info(firstMessage.getName() + " is trying to connect");
        if (!names.containsKey(firstMessage.getName())) {
            String name = firstMessage.getName();
            User user = new User();
            user.setName(firstMessage.getName());
            user.setStatus(Status.ONLINE);
            user.setPicture(firstMessage.getPicture());

            users.add(user);
            names.put(name, user);
            sipClients.add(from);

            logger.info(name + " has been added to the list");
        } else {
            logger.error(firstMessage.getName() + " is already connected");
            throw new DuplicateUsernameException(firstMessage.getName() + " is already connected");
        }
    }

    // 处理用户离去
    private Message removeFromList() throws ParseException, InvalidArgumentException, SipException {
        logger.debug("removeFromList() method Enter");
        Message msg = new Message();
        msg.setMsg("has left the chat.");
        msg.setType(MessageType.DISCONNECTED);
        msg.setName("SERVER");
        msg.setUserlist(names);
        write(msg);
        logger.debug("removeFromList() method Exit");
        return msg;

    }

//    private static class Handler extends Thread {
        //handle 是针对每一个用户的处理类，与用户是一对一的关联
//        private String name;
//        private Socket socket;
//        private  SipLayerFacade sipLayer;
//        private Logger logger = LoggerFactory.getLogger(Handler.class);
//        private User user;
//        private ObjectInputStream input;
//        private OutputStream os;
//        private ObjectOutputStream output;
//        private InputStream is;

//        public Handler(SipLayerFacade sipLayer) throws IOException {
//            this.sipLayer = sipLayer;
//        }

//        public void run() {
//            logger.info("Attempting to connect a user...");
//            try {
////                is = socket.getInputStream();
////                input = new ObjectInputStream(is);
////                os = socket.getOutputStream();
////                output = new ObjectOutputStream(os);
//
//                Message firstMessage = (Message) input.readObject();
//                checkDuplicateUsername(firstMessage);
//                writers.add(output);
//                sendNotification(firstMessage);
//                addToList();
//
//                while (socket.isConnected()) {
////                    Message inputmsg = (Message) input.readObject();
//                    if (inputmsg != null) {
//                        logger.info(inputmsg.getType() + " - " + inputmsg.getName() + ": " + inputmsg.getMsg());
//                        switch (inputmsg.getType()) {
//                            case MessageType.USER:
//                                write(inputmsg);
//                                break;
//                            case MessageType.VOICE:
//                                write(inputmsg);
//                                break;
//                            case MessageType.CONNECTED:
//                                addToList();
//                                break;
//                            case MessageType.STATUS:
//                                changeStatus(inputmsg);
//                                break;
//                        }
//                    }
//                }
//            } catch (SocketException socketException) {
//                logger.error("Socket Exception for user " + name);
//            } catch (DuplicateUsernameException duplicateException){
//                logger.error("Duplicate Username : " + name);
//            } catch (Exception e){
//                logger.error("Exception in run() method for user: " + name, e);
//            } finally {
//                closeConnections();
//            }
//        }

        // 改变在线状态
//        private Message changeStatus(Message inputmsg) throws ParseException, InvalidArgumentException, SipException {
//            logger.debug(inputmsg.getName() + " has changed status to  " + inputmsg.getStatus());
//            Message msg = new Message();
//            msg.setName(user.getName());
//            msg.setType(MessageType.STATUS);
//            msg.setMsg("");
//            User userObj = names.get(name);
//            userObj.setStatus(inputmsg.getStatus());
//            write(msg);
//            return msg;
//        }

//        // 处理用户加入：处理消息
//        private synchronized void checkDuplicateUsername(Message firstMessage) throws DuplicateUsernameException {
//            logger.info(firstMessage.getName() + " is trying to connect");
//            if (!names.containsKey(firstMessage.getName())) {
//                this.name = firstMessage.getName();
//                user = new User();
//                user.setName(firstMessage.getName());
//                user.setStatus(Status.ONLINE);
//                user.setPicture(firstMessage.getPicture());
//
//                users.add(user);
//                names.put(name, user);
//
//                logger.info(name + " has been added to the list");
//            } else {
//                logger.error(firstMessage.getName() + " is already connected");
//                throw new DuplicateUsernameException(firstMessage.getName() + " is already connected");
//            }
//        }

//        // 处理用户加入：通知组内人员
//        private Message sendNotification(Message firstMessage) throws ParseException, InvalidArgumentException, SipException {
//            Message msg = new Message();
//            msg.setMsg("has joined the chat.");
//            msg.setType(MessageType.NOTIFICATION);
//            msg.setName(firstMessage.getName());
//            msg.setPicture(firstMessage.getPicture());
//            write(msg);
//            return msg;
//        }

//        // 处理用户离去
//        private Message removeFromList() throws ParseException, InvalidArgumentException, SipException {
//            logger.debug("removeFromList() method Enter");
//            Message msg = new Message();
//            msg.setMsg("has left the chat.");
//            msg.setType(MessageType.DISCONNECTED);
//            msg.setName("SERVER");
//            msg.setUserlist(names);
//            write(msg);
//            logger.debug("removeFromList() method Exit");
//            return msg;
//        }

//        // 处理用户加入事件：通知加入者
//        private Message addToList() throws ParseException, InvalidArgumentException, SipException {
//            Message msg = new Message();
//            msg.setMsg("Welcome, You have now joined the server! Enjoy chatting!");
//            msg.setType(MessageType.CONNECTED);
//            msg.setName("SERVER");
//            write(msg);
//            return msg;
//        }

//        /*
//         * Creates and sends a Message type to the listeners.
//         * 工具类？这个要重写
//         * 注意这里每发送的一个消息都会附带当前在线人数
//         */
//        private void write(Message msg) throws ParseException, SipException, InvalidArgumentException {
//
//
//            for (String toClient : clientSet) {
//                msg.setUserlist(names);
//                msg.setUsers(users);
//                msg.setOnlineCount(names.size());
//                sipLayer.sendMessage(toClient,msg);
//            }
//        }

        /*
         * Once a user has been disconnected, we close the open connections and remove the writers
         * 资源回收，sip里应该是没有这里的
         */
        private void closeConnections(String from)  {
            logger.debug("closeConnections() method Enter");
            logger.info("HashMap names:" + names.size() + " past clients:" + sipClients.size() + " usersList size:" + users.size());
            String name = from.substring(0,from.indexOf('@'));
            if (name != null) {
                users.remove(names.get(name));
                names.remove(name);
                sipClients.remove(from);
                logger.info("User: " + name + " has been removed!");
            }
            try {
                removeFromList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("HashMap names:" + names.size() + " current clientSet:" + sipClients.size() + " usersList size:" + users.size());
            logger.debug("closeConnections() method Exit");
        }
//    }
}
