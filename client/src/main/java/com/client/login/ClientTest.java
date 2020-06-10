package com.client.login;

import com.client.util.SipLayerFacade;
import com.client.util.SipMessageListener;
import com.messages.Message;
import com.messages.MessageType;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.util.TooManyListenersException;

public class ClientTest implements SipMessageListener {
    public static void main(String[] args) throws InvalidArgumentException, SipException, TooManyListenersException, ParseException {
        SipLayerFacade sipLayer = new SipLayerFacade("wzy@127.0.0.1:1111","server","localhost",9001);
        sipLayer.addSipMessageListener(new ClientTest());
        Message message = new Message();
        message.setName("server");
        message.setType(MessageType.CONNECTED);
        message.setMsg("Welcome, You have now joined the server! Enjoy chatting!");
        sipLayer.sendMessage(message);
    }

    @Override
    public void processReceivedMessage(String sender, Message message) {
        System.out.println(sender);
        System.out.println(message);
    }

    @Override
    public void processError(String errorMessage) {

    }

    @Override
    public void processInfo(String infoMessage) {

    }
}
