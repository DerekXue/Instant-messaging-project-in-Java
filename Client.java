import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Client extends JFrame {
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message="";
	private String serverIP;
	private Socket connection;
	
	public Client(String host) {
		super("Instant Messenger : Client");
		serverIP=host;
		userText=new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
		new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendMessage(event.getActionCommand());
			userText.setText("");
			}
		}
	);
		add(userText,BorderLayout.NORTH);
		chatWindow=new JTextArea();
		add(new JScrollPane(chatWindow),BorderLayout.CENTER);
		setSize(300,150);
		setVisible(true);
	}
	
	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException e) {
			showMessage("\n Client terminated connection");
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}finally {
			closeStuff();
		}
	}
	
	private void connectToServer() throws IOException{
		showMessage("Attempting connection... \n");
		connection= new Socket(InetAddress.getByName(serverIP),6789);
		showMessage("Connected to:"+connection.getInetAddress().getHostName());
	}
	
	private void setupStreams() throws IOException{
		output=new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input=new ObjectInputStream(connection.getInputStream());
		showMessage("\n Your streams are now good to go \n");
	}
	
	private void whileChatting() throws IOException{
		ableToType(true);
		do {
			try {
				message=(String)input.readObject();
				showMessage("\n"+message);
			}catch(ClassNotFoundException e) {
				showMessage("\n	I dont know that object type");
			}
		}while(!message.equals("SERVER - END"));//break loop when server side user enters "END"
	}
	
	private void closeStuff() {
		showMessage("\n closing down connection...");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(String message) {
		try {
			output.writeObject("CLIENT - "+ message);
			output.flush();
			showMessage("\n CLIENT - "+message);
		}catch(IOException e) {
			chatWindow.append("\n something messed up");
		}
	}
	private void showMessage(final String m) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						chatWindow.append(m);
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
		Client myclient;
		myclient=new Client("127.254.254.254");
		myclient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myclient.startRunning();
	}
}
