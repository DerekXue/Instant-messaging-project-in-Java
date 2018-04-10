import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Server extends JFrame{
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	public Server() {
		super("Instant Messenger : Server");
		userText=new JTextField();
		userText.setEditable(false);//cannot type message when not connected to any client
		userText.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						sendMessage(event.getActionCommand());
						userText.setText("");
					}
				});
		add(userText,BorderLayout.NORTH);
		chatWindow=new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true);
	}
	

	//set up and run the server
	public void startRunning() {
		try {
			server=new ServerSocket(6789,100);
			while(true) {
				try {
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException eofException) {
					showMessage("\n Server ended the connection!");
				}finally {
					closeStuff();
				}
			}
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private void waitForConnection() throws IOException{
		showMessage("waiting for someone to connect.... \n");
		connection =server.accept();
		showMessage("Now connected to "+connection.getInetAddress().getHostName());
	}
	
	private void setupStreams() throws IOException{
		output =new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input =new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now steup! \n");  	
	}
		
	private void whileChatting() throws IOException{
		String message="you are now connected";
		sendMessage(message);
		ableToType(true);
		do {
			try {
				message=(String) input.readObject();
				showMessage("\n"+message);
			}catch(ClassNotFoundException classNotFoundException) {
				showMessage("\n idk what that user send");
			}
		}while(!message.equals("CLIENT - END"));//break loop when client side user enters "END"
	}
	

	private void closeStuff() {
		showMessage("\n Closing connectiong ... \n");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
		
	}

	protected void sendMessage(String message) {
		try {
			output.writeObject("SERVER - " + message);
			output.flush();
			showMessage("\n SERVER - "+message);
		}catch(IOException ioException) {
			chatWindow.append("\n Error: I canot send that message");
		}
		
	}
	
	private void showMessage(final String text) {
		SwingUtilities.invokeLater(
			new Runnable() {	
				public void run() {
					chatWindow.append(text);
				}
			}
		);
	}
	
	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(
				new Runnable() {	
					public void run() {
						userText.setEditable(tof);//set weather user can type or not
					}
				}
			);
	}

	public static void main(String[] args) {
		Server myserver=new Server();
		myserver.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myserver.startRunning();
	}	
}
