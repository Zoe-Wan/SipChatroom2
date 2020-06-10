package com.server;

import com.messages.Message;
import com.messages.MessageType;
import com.util.SipLayerFacade;

import javax.sip.*;
import java.text.ParseException;
import java.util.TooManyListenersException;

public class ClientTest {


    public static void main(String[] args) throws InvalidArgumentException, SipException, TooManyListenersException, ParseException {
        SipLayerFacade sipLayer = new SipLayerFacade("server","localhost",9001);
        Message message = new Message();

        message.setName("server");
        message.setType(MessageType.CONNECTED);
        message.setMsg("Welcome, You have now joined the server! Enjoy chatting!");
        sipLayer.sendMessage("wzy@127.0.0.1:1111",message);
    }
}
