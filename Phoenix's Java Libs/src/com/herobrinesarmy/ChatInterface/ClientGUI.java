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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javax.swing.*;

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
 * @version 1.1
 * @since 1.0
 * @see ChatRoom
 */

public class ClientGUI {
	//Encoding Format
	private static String enc = "UTF-8";
		
	//Cookie Manager
	private static CookieManager cookieManager = new CookieManager();
	
	//Collection for chats
	private LinkedHashMap<Integer, ChatRoom> chats = new LinkedHashMap<Integer, ChatRoom>();
	
	//The scheduler service for the getMessages() thread
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	static ChatRoom chat = new ChatRoom(8613406);
	
	//GUI Components
	private static JFrame frame = new JFrame("HA Chat Client v1.0");
	//private Container contentPane;
	private JMenuBar menuBar = new JMenuBar();
	private JMenu chatMenu = new JMenu("Options");
	private JMenuItem auth = new JMenuItem("Authenticate");
	private JMenuItem quit = new JMenuItem("Quit");
	private static JTextArea chatArea = new JTextArea(40, 80);
	private JTextArea messageArea = new JTextArea(5, 80);
	private static JTextArea userList = new JTextArea(50,10);
	private static JScrollPane scrollArea = new JScrollPane(chatArea);
	
	public ClientGUI() {		
//		contentPane = frame.getContentPane();
//		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		frame.setJMenuBar(menuBar);
		menuBar.add(chatMenu);
		chatMenu.add(auth);
		//TODO actionhandler
		chatMenu.add(quit);
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

		messageArea.addKeyListener(new MessageActionHandler());
//		frame.add(scrollArea);
//		frame.add(messageArea);
		frame.add(textPanels, BorderLayout.CENTER);
		frame.add(userList, BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		LoginDialog loginDialog = new LoginDialog(frame);
		loginDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		loginDialog.setSize(new Dimension(420,150));
		loginDialog.setVisible(true);
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
					userList.setText(null);
					for(User u : chat.getUsers()) {
						userList.append(u.getUsername() + "\n");
					}					
				} catch (JSONException e) {
					
				} catch (IOException e) {
					
				}
			}
		};
		@SuppressWarnings("unused")
		final ScheduledFuture<?> messages = scheduler.scheduleWithFixedDelay(outputMessages, 0, 1, SECONDS);
		@SuppressWarnings("unused")
		final ScheduledFuture<?> users = scheduler.scheduleWithFixedDelay(outputUsers, 0, 1, SECONDS);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new ClientGUI();
		getNewEntities(chat);
	}
	
	private class MessageActionHandler implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				try {
					chat.postMessage(messageArea.getText());
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
