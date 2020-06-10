package com.client.util;

import com.messages.Message;

// the interface between TextClientConsole and SipLayerFacade
public interface SipMessageListener {
    public void processReceivedMessage(String sender, Message message);
    public void processError(String errorMessage);
    public void processInfo(String infoMessage);
}
