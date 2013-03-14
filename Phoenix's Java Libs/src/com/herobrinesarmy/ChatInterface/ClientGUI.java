/**
 * Copyright (c) 2012 Phoenix, Herobrine and herobrinesarmy.com
 * YOU WILL FUCKING FREELY USE MY CODE HOWEVER YOU GODDAMN WANT TO. (sic) - Kitcat490
 */
package com.herobrinesarmy.ChatInterface;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javax.swing.*;
//import javax.swing.text.AttributeSet;
//import javax.swing.text.BadLocationException;
//import javax.swing.text.DefaultStyledDocument;
//import javax.swing.text.SimpleAttributeSet;
//import javax.swing.text.Style;
//import javax.swing.text.StyleConstants;
//import javax.swing.text.StyleContext;

import org.json.JSONException;

import com.herobrinesarmy.ChatInterface.Entities.ChatRoom;
import com.herobrinesarmy.ChatInterface.Entities.Message;
import com.herobrinesarmy.ChatInterface.Entities.User;

/**
 * This class manages the GUI and only interacts with ChatRooms. It holds a <code>LinkedHashMap</code> of
 * all the ChatRooms that have been connected to by invoking the <code>connectChat()</code> method. Authentication
 * to the server is also handled here, as well as the cookies passed to and from the server, this is done by the 
 * <code>authenticate()</code> method that takes the users username and password as parameters. A separate method,
 * <code>checkAuth()</code> is used to check authentication by the <code>authenticate()</code> method, and it returns
 * a boolean value depending on the string returned by <code>checkAuth()</code>.
 * <p>
 * 
 * @author Phoenix
 * @version 1.3
 * @since 1.0
 * @see ChatRoom
 */

public class ClientGUI {
	//GUI Client Version
	private final static String VERSION = "v1.3";
	
	//Encoding Format
	private static String enc = "UTF-8";
		
	//Cookie Manager
	private static CookieManager cookieManager = new CookieManager();
	
	//Collection for chats
	private LinkedHashMap<Integer, ChatRoom> chats = new LinkedHashMap<Integer, ChatRoom>();
	
	//Collection for styles and user tags
//	private static LinkedHashMap<Integer, SimpleAttributeSet> userStyles = new LinkedHashMap<Integer, SimpleAttributeSet>();
	
	//The scheduler service for the getMessages() thread
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	static ChatRoom chat = new ChatRoom(8613406);
	
	//
	static User[] ul = new User[0];
	
//	//String to hold username
//	static String username;
	
	//GUI Components
	private static JFrame frame = new JFrame("HA Chat Client " + VERSION);
	//private Container contentPane
	private JMenuBar menuBar = new JMenuBar();
	private JMenu mute = new JMenu("Mute");
	private JMenuItem muteUser = new JMenuItem("Mute User");
	private JMenuItem unmuteUser = new JMenuItem("Unmute User");
	private JMenu auth = new JMenu("Authenticate");
	private JMenu about = new JMenu("About");
	private JMenu quit = new JMenu("Quit");
	private static JTextArea chatArea = new JTextArea(40, 80);
	private JTextArea messageArea = new JTextArea(5, 80);
	private static JTextArea userList = new JTextArea(50,10);
	private static JScrollPane scrollArea = new JScrollPane(chatArea);
//	private final static StyleContext sc = new StyleContext();
//	private final static DefaultStyledDocument doc = new DefaultStyledDocument(sc);
//	private static JTextPane userPane = new JTextPane(doc);
	private LoginDialog loginDialog;
	
	public ClientGUI() {		
//		contentPane = frame.getContentPane();
//		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		frame.setJMenuBar(menuBar);
		frame.addWindowFocusListener(new FocusHandler());
		menuBar.add(auth);
		//TODO actionhandler
//		menuBar.add(mute);
//		mute.add(muteUser);
//		muteUser.addActionListener(new MuteHandler());
//		mute.add(unmuteUser);
//		unmuteUser.addActionListener(new UnmuteHandler());
		menuBar.add(about);
		//TODO actionhandler
		menuBar.add(quit);
		//TODO actionhandler
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		scrollArea.setAutoscrolls(true);
		scrollArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		userList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		userList.setEditable(false);
		messageArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		scrollArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		chatArea.setBackground(Color.BLACK);
		chatArea.setForeground(Color.WHITE);
		messageArea.setBackground(Color.BLACK);
		messageArea.setForeground(Color.WHITE);
		messageArea.setCaretColor(Color.WHITE);
		userList.setBackground(Color.BLACK);
		userList.setForeground(Color.WHITE);
		JPanel textPanels = new JPanel();
		
		textPanels.setLayout(new BoxLayout(textPanels, BoxLayout.Y_AXIS));
		textPanels.add(scrollArea);
		textPanels.add(messageArea);
//		userPane.setVisible(true);
//		userPane.setEditable(false);
//		userPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		messageArea.addKeyListener(new MessageActionHandler());
		frame.add(textPanels, BorderLayout.CENTER);
		frame.add(userList, BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginDialog = new LoginDialog(frame);
		loginDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		loginDialog.setSize(new Dimension(420,150));
		loginDialog.setVisible(true);
//		initTagValues();
	}
	
	/**
	 * Sends a HTTP request to the authentication URL on <a href="herobrinesarmy.com">herobrinesarmy.com</a>
	 * takes the username and password of the user wishing to authenticate as parameters and sends them to the server 
	 * as an encoded array of bytes. Then uses the <code>checkAuth()</code> to see if authentication was successful.
	 * 
	 * @param username the username of the user wishing to authenticate
	 * @param password the password of the user wishing to authenticate
	 * @return true if authentication was successful
	 * @throws IOException if there is a general IO exception thrown
	 * @see checkAuth
	 */
	public static boolean authenticate(String username, String password) throws IOException {
		CookieManager.setDefault(cookieManager);
		URL url = new URL("http://herobrinesarmy.com/auth");
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setDoOutput(true);
	     
	 	String query = "user=" + URLEncoder.encode(username, enc) + "&pass=" + URLEncoder.encode(password, enc);
	    DataOutputStream outs = new DataOutputStream(conn.getOutputStream());
	    outs.writeBytes(query);
	    outs.close();
	     
	    //Receive a response from the server to enable auth.
	    @SuppressWarnings("unused")
		int response = conn.getResponseCode();
	     
		return checkAuth().equals("Yeah.");
	}
	
	/**
	 * Uses the authentication check URL on <a href="herobrinesarmy.com">herobrinesarmy.com</a> and returns the
	 * result on the page as a string.
	 * 
	 * @return the result of checking authentication
	 * @throws IOException if there is a general IO exception thrown
	 */
	private static String checkAuth() throws IOException {
		String result = "";
	    Scanner reader = null;
	    URL url = new URL("http://herobrinesarmy.com/amiauth");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");
	    conn.setDoInput(true);

	    reader = new Scanner(conn.getInputStream());
	    while(reader.hasNext()) {
	    	result += reader.nextLine();
	    }
	    return result;
	}
	
	/**
	 * Creates a new ChatRoom and then stores it in the <code>LinkedHashMap</code> chats.
	 * 
	 * @param chatChannel the channel of the ChatRoom to be created
	 * @throws JSONException when "things are amiss"
	 * @throws IOException if there is a general IO error
	 */
	public void connectChat(int chatChannel) throws JSONException, IOException {
		ChatRoom chat = new ChatRoom(chatChannel);
		chats.put(chat.getChannel(), chat);
	}
	
//	public void initTagValues() {
//		userPane.setBackground(Color.BLACK);
//		Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
//		StyleConstants.setFontFamily(defaultStyle, "Verdana");
//		StyleConstants.setFontSize(defaultStyle, 14);
//		//StyleConstants.setForeground(defaultStyle, Color.WHITE);
//		
////		final Style herobrine = sc.addStyle("herobrine", defaultStyle);
//		SimpleAttributeSet herobrine = new SimpleAttributeSet();
//		//decode("#D12626")
//		StyleConstants.setForeground(herobrine, Color.RED);
//		final Style highCommand = sc.addStyle("highcommand", defaultStyle);
//		//.decode("#ffff00")
//		StyleConstants.setForeground(highCommand, Color.YELLOW);
//		final Style science = sc.addStyle("science", defaultStyle);
//		StyleConstants.setForeground(science, Color.decode("#3888ff"));
//		final Style aerospace = sc.addStyle("aerospace", defaultStyle);
//		StyleConstants.setForeground(aerospace, Color.decode("#d46a00"));
//		final Style fleet = sc.addStyle("fleet", defaultStyle);
//		StyleConstants.setForeground(fleet, Color.decode("#dd2423"));
//		final Style acquisitor = sc.addStyle("acquisitor", defaultStyle);
//		StyleConstants.setForeground(acquisitor, Color.decode("#007500"));
//		final Style transport = sc.addStyle("transport", defaultStyle);
//		StyleConstants.setForeground(transport, Color.decode("#82329c"));
//		final Style notEnlisted = sc.addStyle("notenlisted", defaultStyle);
//		StyleConstants.setForeground(notEnlisted, Color.decode("#a6a6a6"));
//		
//		//Aerospace
////		userStyles.put(228373, aerospace);
////		//Science
////		userStyles.put(228361, science);
////		//Transport
////		userStyles.put(228640, transport);
////		//Fleet
////		userStyles.put(228677, fleet);
////		//Acquisitor
////		userStyles.put(232043, acquisitor);
////		//Not Enlisted
////		userStyles.put(271886, notEnlisted);
////		//High Command
////		userStyles.put(228080, highCommand);
//		//Herobrine
//		userStyles.put(223534, herobrine);
//	}
//	
//	public static void addUserColour(int usrTag, String usrName) throws BadLocationException {
//		doc.insertString(doc.getLength(), usrName + "\n", null);
//		doc.setCharacterAttributes(doc.getLength()+1, usrName.length(), (AttributeSet) Color.RED, true);
//	}
	
	public String[] chatUsers() throws JSONException, IOException {
		ArrayList<String> usernames = new ArrayList<String>();
		for(User u : chat.getUsers()) {
			usernames.add(u.getUsername());
		}
		return usernames.toArray(new String[0]);
	}
	
	/**
	 * A simple thread management method. It creates the thread to run the printing of messages, then schedules
	 * this thread to run every second, getting message updates from the chat every second. If no messages are
	 * present, then a JSON exception is thrown, but not caught. This exception isn't harmful, so it can be ignored.
	 */
	public final static void getNewEntities(final ChatRoom chat) {
		final Runnable outputMessages= new Runnable() { 
			public void run() {
				try {
					for(Message m : chat.getMessages()) {
						chatArea.append(m.getMessage() + "\n");
						chatArea.setCaretPosition(chatArea.getText().length());
						scrollArea.getVerticalScrollBar().setValue(chatArea.getCaretPosition());
					}
				} catch (IOException e) {
					
				} catch (JSONException e) {
					//Maybe add increasing interval operation here.
					//WILL BE THROWN IF NO NEW MESSAGES
				}
			}
		};
		final Runnable outputUsers = new Runnable() {
			public void run() {
				try {
					ul = chat.getUsers();
					userList.setText(null);
//					userPane.setText(null);
					for(User u : ul) {
						userList.append(u.getUsername() + "\n");
//						try {
//							addUserColour(u.getUserID(), u.getUsername());
//						} catch (BadLocationException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
					}
				} catch (JSONException e) {
					
				} catch (IOException e) {
					
				}
			}
		};
		@SuppressWarnings("unused")
		final ScheduledFuture<?> messages = scheduler.scheduleWithFixedDelay(outputMessages, 0, 1, SECONDS);
		@SuppressWarnings("unused")
		final ScheduledFuture<?> users = scheduler.scheduleWithFixedDelay(outputUsers, 0, 30, SECONDS);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new ClientGUI();
		getNewEntities(chat);
	}
	
	private class FocusHandler implements WindowFocusListener {

		@Override
		public void windowGainedFocus(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowLostFocus(WindowEvent e) {
			// TODO Auto-generated method stub
			if(chatArea.getText().length() != 0 && !frame.isFocusOwner()) {
				for(int i = 0; i < chatArea.getColumns(); i++) {
					chatArea.append("-");
				}
				chatArea.append("\n");
			}
		}

	}
	
	private class MessageActionHandler implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				try {
					if(messageArea.getText().startsWith("/mute")) {
						String user = messageArea.getText().substring(messageArea.getText().indexOf("/mute") + 5).trim();
						try {
							if(chat.muteUser(chat.getUserID(user))) {
								JOptionPane.showMessageDialog(frame, user + " muted.", "Success", JOptionPane.INFORMATION_MESSAGE);
							}
						} catch (JSONException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					else if(messageArea.getText().startsWith("/wolf")) {
						
					}
					else {
						chat.postMessage(messageArea.getText());
					}
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				messageArea.setText("");
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
//	private class MuteHandler implements ActionListener {
//
//		@Override
//		public void actionPerformed(ActionEvent e) {
////			String user = (String) JOptionPane.showInputDialog(frame, "Select the user to mute:", "Mute User", JOptionPane.PLAIN_MESSAGE,
//			try {
//				String username = (String) JOptionPane.showInputDialog(frame, "Select the user to mute:", "Mute User", JOptionPane.PLAIN_MESSAGE, null, ul, null);
//				for(User u : ul) {
//					if(u.getUsername().equals(username)) {
//						if(chat.muteUser(u.getUserID())) {
//							JOptionPane.showMessageDialog(frame, username + " muted.", "Success", JOptionPane.INFORMATION_MESSAGE);
//						}
//					}
//				}
//			} catch (HeadlessException er) {
//				// TODO Auto-generated catch block
//				er.printStackTrace();
//			} catch (JSONException er) {
//				// TODO Auto-generated catch block
//				er.printStackTrace();
//			} catch (IOException er) {
//				// TODO Auto-generated catch block
//				er.printStackTrace();
//			}
//		}
//		
//	}
	
//	private class UnmuteHandler implements ActionListener {
//
//		@Override
//		public void actionPerformed(ActionEvent e) {
////			String user = (String) JOptionPane.showInputDialog(frame, "Select the user to mute:", "Mute User", JOptionPane.PLAIN_MESSAGE,
//			try {
//				String username = (String) JOptionPane.showInputDialog(frame, "Select the user to unmute:", "Unmute User", JOptionPane.PLAIN_MESSAGE, null, chatUsers(), null);
//				for(User u : chat.getUsers()) {
//					if(u.getUsername().equals(username)) {
//						if(chat.unmuteUser(u.getUserID())) {
//							JOptionPane.showMessageDialog(frame, username + " unmuted.", "Success", JOptionPane.INFORMATION_MESSAGE);
//						}
//					}
//				}
//			} catch (HeadlessException er) {
//				// TODO Auto-generated catch block
//				er.printStackTrace();
//			} catch (JSONException er) {
//				// TODO Auto-generated catch block
//				er.printStackTrace();
//			} catch (IOException er) {
//				// TODO Auto-generated catch block
//				er.printStackTrace();
//			}
//		}
//		
//	}
	
	@SuppressWarnings("serial")
	private class LoginDialog extends JDialog {
		
		private JTextField usernameField;
		private JPasswordField passwordField;
		private JLabel usernameLabel;
		private JLabel passwordLabel;
		private JButton loginBtn = new JButton("Login");
		private JButton cancelBtn = new JButton("Cancel");
		
		public LoginDialog(Frame parent) {
			super(parent, "Authenticate", true);
			
			JPanel dialog = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			c.fill = GridBagConstraints.HORIZONTAL;
			
			usernameLabel = new JLabel("Username: ");
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			dialog.add(usernameLabel, c);
			
			usernameField = new JTextField(20);
			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 2;
			dialog.add(usernameField, c);
			
			passwordLabel = new JLabel("Password: ");
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 2;
			dialog.add(passwordLabel, c);
			
			passwordField = new JPasswordField(20);
			c.gridx = 1;
			c.gridy = 1;
			c.gridwidth = 2;
			dialog.add(passwordField, c);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(loginBtn);
			loginBtn.addActionListener(new loginHandler());
			buttonPanel.add(cancelBtn);
			cancelBtn.addActionListener(new cancelHandler());
			getContentPane().add(dialog, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
			usernameField.addKeyListener(new enterHandler());
			passwordField.addKeyListener(new enterHandler());
		}
		
		private class loginHandler implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(authenticate(usernameField.getText().trim(), new String(passwordField.getPassword()))) {
						JOptionPane.showMessageDialog(LoginDialog.this, "Login Successful!", "Authentication Success", JOptionPane.INFORMATION_MESSAGE);
//						username = usernameField.getText();
						dispose();
					}
					else {
						JOptionPane.showMessageDialog(LoginDialog.this, "Login Unsuccessful", "Authentication Failure", JOptionPane.ERROR_MESSAGE);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
		
		private class cancelHandler implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(rootPane, LoginDialog.this.getSize());
			}
			
		}
		
		private class enterHandler implements KeyListener {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						if(authenticate(usernameField.getText().trim(), new String(passwordField.getPassword()))) {
							JOptionPane.showMessageDialog(LoginDialog.this, "Login Successful!", "Authentication Success", JOptionPane.INFORMATION_MESSAGE);
							dispose();
						}
						else {
							JOptionPane.showMessageDialog(LoginDialog.this, "Login Unsuccessful", "Authentication Failure", JOptionPane.ERROR_MESSAGE);
						}
					} catch (HeadlessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
		}
	}
	
}
