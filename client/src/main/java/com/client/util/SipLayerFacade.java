package com.client.util;

import com.messages.Message;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TooManyListenersException;

public class SipLayerFacade implements SipListener {
	private SipMessageListener 	sipMessageListener;
	private String username;
	private String to;
	private SipStack 			sipStack;
	private SipFactory 			sipFactory;
	private AddressFactory 		addressFactory;
	private HeaderFactory 		headerFactory;
	private MessageFactory 		messageFactory;
	private SipProvider 		sipProvider;

	// Here we initialize the SIP stack
	public SipLayerFacade(String server,String username, String ip, int port)
			throws PeerUnavailableException, TransportNotSupportedException,
			InvalidArgumentException, ObjectInUseException,
            TooManyListenersException {
		to = server;
		setUsername(username);
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "TextClient");
		properties.setProperty("javax.sip.IP_ADDRESS", ip);

		// DEBUGGING: Information will go to files textclient.log and textclientdebug.log
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "textclient.txt");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "textclientdebug.log");

		sipStack 		= sipFactory.createSipStack(properties);
		headerFactory 	= sipFactory.createHeaderFactory();
		addressFactory 	= sipFactory.createAddressFactory();
		messageFactory 	= sipFactory.createMessageFactory();

		ListeningPoint udp = sipStack.createListeningPoint(ip, port, "udp");
		sipProvider = sipStack.createSipProvider(udp);
		sipProvider.addSipListener(this);
	}

	// This method uses the SIP stack to send a message.

	public void sendMessage(Message message) throws ParseException,
			InvalidArgumentException, SipException {

		// to的格式为username@address:port
		SipURI from = addressFactory.createSipURI(getUsername(), getHost() + ":" + getPort());
		Address fromNameAddress = addressFactory.createAddress(from);
		fromNameAddress.setDisplayName(getUsername());
		FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "textclientv1.0");

		System.out.println(to);
		String username = to.substring(0, to.indexOf("@"));
		String address = to.substring(to.indexOf("@") + 1);

		System.out.println(username + " " + address);

		SipURI toAddress = addressFactory.createSipURI(username, address);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(username);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

		SipURI requestURI = addressFactory.createSipURI(username, address);
		requestURI.setTransportParam("udp");

		ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
		ViaHeader viaHeader = headerFactory.createViaHeader(getHost(), getPort(), "udp", "branch1");
		viaHeaders.add(viaHeader);

		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.MESSAGE);

		MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

		Request request = messageFactory.createRequest(requestURI,
				Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);

		SipURI contactURI = addressFactory.createSipURI(getUsername(), getHost());
		contactURI.setPort(getPort());
		Address contactAddress = addressFactory.createAddress(contactURI);
		contactAddress.setDisplayName(getUsername());
		ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);

		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
		System.out.println(message);
		request.setContent(message, contentTypeHeader);

		sipProvider.sendRequest(request);
	}

	// This method uses the SIP stack to send a message.

	public void sendMessage(String message) throws ParseException,
			InvalidArgumentException, SipException {

		SipURI from = addressFactory.createSipURI(getUsername(), getHost() + ":" + getPort());
		Address fromNameAddress = addressFactory.createAddress(from);
		fromNameAddress.setDisplayName(getUsername());
		FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, "textclientv1.0");

		System.out.println(to);
		String username = to.substring(0, to.indexOf("@"));
		String address = to.substring(to.indexOf("@") + 1);
		
		System.out.println(username + " " + address);
		
		SipURI toAddress = addressFactory.createSipURI(username, address);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(username);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

		SipURI requestURI = addressFactory.createSipURI(username, address);
		requestURI.setTransportParam("udp");

		ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
		ViaHeader viaHeader = headerFactory.createViaHeader(getHost(), getPort(), "udp", "branch1");
		viaHeaders.add(viaHeader);

		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.MESSAGE);

		MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

		Request request = messageFactory.createRequest(requestURI,
				Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
				toHeader, viaHeaders, maxForwards);

		SipURI contactURI = addressFactory.createSipURI(getUsername(), getHost());
		contactURI.setPort(getPort());
		Address contactAddress = addressFactory.createAddress(contactURI);
		contactAddress.setDisplayName(getUsername());
		ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
		request.addHeader(contactHeader);

		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
		System.out.println(message);
		request.setContent(message, contentTypeHeader);

		sipProvider.sendRequest(request);
	}

	// This method is called by the SIP stack when a response arrives.
	public void processResponse(ResponseEvent evt) {
		Response response = evt.getResponse();
		int status = response.getStatusCode();

		if ((status >= 200) && (status < 300)) { // Success!
			sipMessageListener.processInfo("--Sent");
			return;
		}

		sipMessageListener.processError("Previous message not sent: " + status);
	}

	// This method is called by the SIP stack when a new request arrives.
	public void processRequest(RequestEvent evt) {
		Request req = evt.getRequest();

		String method = req.getMethod();
		if (!method.equals("MESSAGE")) { // bad request type.
			sipMessageListener.processError("Bad request type: " + method);
			return;
		}

		FromHeader from = (FromHeader) req.getHeader("From");
		sipMessageListener.processReceivedMessage(from.getAddress().toString(), (Message)req.getContent());
		Response response = null;
		try { // Reply with OK
			response = messageFactory.createResponse(200, req);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("888"); // This is mandatory as per the spec.
			ServerTransaction st = sipProvider.getNewServerTransaction(req);
			st.sendResponse(response);
		} catch (Throwable e) {
			e.printStackTrace();
			sipMessageListener.processError("Can't send OK reply.");
		}
	}

	// This method is called by the SIP stack when there's no answer to a
	// message. Note that this is treated differently from an error message.
	public void processTimeout(TimeoutEvent evt) {
		sipMessageListener.processError("Previous message not sent: " + "timeout");
	}

	// This method is called by the SIP stack when there's an asynchronous
	// message transmission error.
	public void processIOException(IOExceptionEvent evt) {
		sipMessageListener.processError("Previous message not sent: " + "I/O Exception");
	}

	// This method is called by the SIP stack when a dialog (session) ends.
	public void processDialogTerminated(DialogTerminatedEvent evt) {
	}

	// This method is called by the SIP stack when a transaction ends.
	public void processTransactionTerminated(TransactionTerminatedEvent evt) {
	}

	public String getHost() {
		return sipStack.getIPAddress();
	}

	public int getPort() {
		int port = sipProvider.getListeningPoint("udp").getPort();
		return port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String newUsername) {
		username = newUsername;
	}

	public SipMessageListener getSipMessageListener() {
		return sipMessageListener;
	}

	public void addSipMessageListener(SipMessageListener newMessageProcessor) {
		sipMessageListener = newMessageProcessor;
	}

}
