package com.util;

import com.exception.DuplicateUsernameException;
import com.messages.Message;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;

// the interface between TextClientConsole and SipLayerFacade
public interface SipMessageListener {
    public void processReceivedMessage(String sender, Message message) throws ParseException, InvalidArgumentException, SipException, DuplicateUsernameException;
    public void processError(String errorMessage);
    public void processInfo(String infoMessage);
}
