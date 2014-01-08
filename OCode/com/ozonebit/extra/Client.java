package com.ozonebit.extra;


import java.util.Collection;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.RosterEntry;

/**
 * @author Ozone Bit
 *
 */
public class Client {
	
	
private static final int packetReplyTimeout = 500; // millis
    

    private String server;
    private int port;
    
   
    private ConnectionConfiguration config;
    private XMPPConnection connection;

    /* Chat Manager to keep track of all open chats
     * Message listener to listen for messages on any open chats
     * ChatManagerListener to listen for any incoming chats that haven't been initaited earlier.
     */
    
    private ChatManager chatManager;
    private MessageListener messageListener;
    private ChatManagerListener chatListener;
    
    
    public Client(String server, int port) {
        this.server = server;
        this.port = port;
    }
    
    
    /**
     * 
     * @throws XMPPException
     */
    public void init() throws XMPPException {
        
        System.out.println(String.format("Initializing connection to server %1$s port %2$d", server, port));

        SmackConfiguration.setPacketReplyTimeout(packetReplyTimeout);
        
        config = new ConnectionConfiguration(server, port);
        config.setSASLAuthenticationEnabled(false);
        config.setSecurityMode(SecurityMode.disabled);
        
        connection = new XMPPConnection(config);
        connection.connect();
        
        System.out.println("Connected: " + connection.isConnected());
        
        chatManager = connection.getChatManager();
        messageListener = new MyMessageListener();
        chatListener = new MyChatListener();
        chatManager.addChatListener(chatListener);
    }
    
    public void performLogin(String username, String password) throws XMPPException {
        if (connection!=null && connection.isConnected()) {
            connection.login(username, password);
        }
    }
    public void printRoster() throws Exception {
        Roster roster = connection.getRoster();
        Collection<RosterEntry> entries = roster.getEntries();  
        for (RosterEntry entry : entries) {
           System.out.println(String.format("Buddy:%1$s - Status:%2$s", 
             entry.getName(), entry.getStatus()));
         }
      }

    public void setStatus(boolean available, String status) {
        
        Presence.Type type = available? Type.available: Type.unavailable;
        Presence presence = new Presence(type);
        
        presence.setStatus(status);
        connection.sendPacket(presence);
        
    }
    
    public void destroy() {
        if (connection!=null && connection.isConnected()) {
            connection.disconnect();
        }
    }
    
    public void sendMessage(String message, String buddyJID) throws XMPPException {
        System.out.println(String.format("Sending mesage '%1$s' to user %2$s", message, buddyJID));
        Chat chat = chatManager.createChat(buddyJID, messageListener);
        chat.sendMessage(message);
    }
    
    public void createEntry(String user, String name) throws Exception {
        System.out.println(String.format("Creating entry for buddy '%1$s' with name %2$s", user, name));
        Roster roster = connection.getRoster();
        roster.createEntry(user, name, null);
    }
    
    class MyMessageListener implements MessageListener {

        @Override
        public void processMessage(Chat chat, Message message) {
            String from = message.getFrom();
            from = from.substring(0,from.indexOf("@"));
            String body = message.getBody();
            System.out.println(String.format("%1$s: %2$s",from,body));
        }


        
    }
    
    class MyChatListener implements ChatManagerListener {
        @Override
        public void chatCreated (Chat chat, boolean createdLocally)
        {
            if(createdLocally != true)
            {
                chat.addMessageListener(messageListener);
            }
        }
    }
	

}
