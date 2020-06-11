package com.messages;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Message implements Serializable {

    private String name;
    private MessageType type;
    private String msg;
    private int count;
    private HashMap<String, User> list;
    private ArrayList<User> users;

    private Status status;

    public byte[] getVoiceMsg() {
        return voiceMsg;
    }

    private byte[] voiceMsg;

    public String getPicture() {
        return picture;
    }

    private String picture;

    public Message() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {

        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public HashMap<String, User> getUserlist() {
        return list;
    }

    public void setUserlist(HashMap<String, User> userList) {
        this.list = userList;
    }

    public void setOnlineCount(int count) {
        this.count = count;
    }

    public int getOnlineCount() {
        return this.count;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }


    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setVoiceMsg(byte[] voiceMsg) {
        this.voiceMsg = voiceMsg;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static Message JsonToMessage(String Json) {
        return JSON.parseObject(Json, Message.class);
    }

    public static void main(String[] args) {
        Message msg = new Message();
        msg.setMsg("Welcome, You have now joined the server! Enjoy chatting!");
        msg.setType(MessageType.CONNECTED);
        msg.setName("SERVER");
        msg.setUserlist(new HashMap<String, User>());
        msg.setUsers(new ArrayList<User>());
        msg.setOnlineCount(0);
        System.out.println(msg);
        System.out.println(Message.JsonToMessage("{\"msg\":\"Welcome, You have now joined the server! Enjoy chatting!\",\"name\":\"SERVER\",\"onlineCount\":1,\"type\":\"CONNECTED\",\"userlist\":{\"wzy\":{\"name\":\"wzy\",\"picture\":\"Default\",\"status\":\"ONLINE\"}},\"users\":[{\"$ref\":\"$.userlist.wzy\"}]}"));
    }
}
