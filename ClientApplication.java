// Ben Schreyer BCC HS March/April AP Java scrum poker client for WAN and LAN planning poker sessions
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.awt.*;
//the application is a frame that uses event listeners for clicks and key input
public class ClientApplication extends Frame implements ActionListener
{
	//eclipse forced this for creation of runnable jar
	private static final long serialVersionUID = 1L;

	// the client application class has a socket that will connect to the server
	private volatile boolean connected = false;
	
	private Socket socket = new Socket();
	private DataInputStream input = null;
	private DataOutputStream output = null;
	//button to open server connection dialog
	private Button connectButton = new Button("Connect To Server");
	//game choices objects
	private int[] choice = {1,2,3,5,8,13,20,40,100};
	private Button[] cardButton = new Button[choice.length];
	//chat interface objects
	private TextArea chat = new TextArea();
	private TextArea chatBar = new TextArea();
	private Button sendMessageButton = new Button("Send");
	private Button doneDiscussingButton = new Button("Done Discussing");
	
	//connect to server dialog objects
	private Dialog connectDialog = new Dialog(this,"Connect To Server",true);
	private Label connectDialogLabel = new Label("IP:");
	private TextArea connectDialogTextArea = new TextArea();
	private Label connectDialogNameLabel = new Label("NAME:");
	private TextArea connectDialogNameArea = new TextArea();
	private Button confirmIpButton = new Button("Connect");
	
	ClientApplication()
	{
		//window config
		setTitle("Ben S Scrum Poker Client - Not Connected");
		setResizable(false);
		setSize(600,400);
		setLayout(null);
		//intialize card button objects and add them
		for(int i = 0;i < choice.length;i++)
		{
			cardButton[i] = new Button(""+choice[i]);
			cardButton[i].setBounds(i*65 + 10,40,60,100);
			cardButton[i].setActionCommand("CardButton" + (char)i);
			cardButton[i].addActionListener(this);
			add(cardButton[i]);
		}
		//configure dialog and normal elements 
		connectDialog.setLayout(null);
		connectDialog.setSize(450,130);
		connectDialog.setResizable(false);
		connectDialogLabel.setBounds(10,40,20,40);
		connectDialogNameLabel.setBounds(10,80,100,40);
		connectDialogTextArea.setBounds(80,40,200,40);
		connectDialogNameArea.setBounds(80,80,200,40);
		confirmIpButton.setBounds(300,70,100,20);
		confirmIpButton.setActionCommand("ConfirmIpButton");
		confirmIpButton.addActionListener(this);
		connectDialog.add(confirmIpButton);
		connectDialog.add(connectDialogLabel);
		connectDialog.add(connectDialogTextArea);
		connectDialog.add(connectDialogNameArea);
		connectDialog.add(connectDialogNameLabel);
		connectDialog.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {connectDialog.setVisible(false);}});
		connectButton.setActionCommand("ConnectDialogButton");
		connectButton.setBounds(10,375,150,20);
		connectButton.addActionListener(this);
		chatBar.setBounds(10,330,480,40);
		doneDiscussingButton.setBounds(490,360,100,20);
		doneDiscussingButton.setActionCommand("DoneDiscussingButton");
		doneDiscussingButton.addActionListener(this);
		doneDiscussingButton.setEnabled(false);
		sendMessageButton.setBounds(490,330,100,20);
		sendMessageButton.setActionCommand("SendMessageButton");
		sendMessageButton.addActionListener(this);
		sendMessageButton.setEnabled(false);
		chat.setBounds(10,150,580,180);
		chat.setEditable(false);
		
		//add elements to main window
		add(doneDiscussingButton);
		add(sendMessageButton);
		add(chatBar);
		add(chat);
		add(connectButton);
		//add listener for window closing to allow smooth closing of application
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {			
		dispose();}});
		//show the window to user
		setVisible(true);
		boolean flag = true;
		//wait for connection
		while(!connected)
		{

		}
		System.out.println("GAME LOOP STARTED");
		//game loop
		while(flag &&!socket.isClosed())
		{
			String line;
			//read data from server if possible
			try {
				if(input.available()<= 0)
					continue;
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			try {
				line = input.readUTF();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				continue;
			}
			//display a chat message correctly if the server intended a chat message
			if(line.substring(0,3).equals("C{]"))
			{
				chat.setText(chat.getText()+"\n"+line.substring(3));
				chat.setCaretPosition(chat.getText().length()-1);
			}
			//change window abilitys based on state of game, pick phase or discussion phase
			if(line.equals("G{]P"))
			{
				for(int i = 0;i < choice.length;i++)
				{
					cardButton[i].setEnabled(true);
				}
				sendMessageButton.setEnabled(false);
				doneDiscussingButton.setEnabled(false);
			}
			if(line.equals("G{]T"))
			{
				sendMessageButton.setEnabled(true);
				doneDiscussingButton.setEnabled(true);
			}
		}
	}
	//event handler
	public void actionPerformed(ActionEvent action) 
	{
		//send choice submit data to server when player chooses
		if(action.getActionCommand().substring(0,10).equals("CardButton"))
		{
			System.out.println(action.getActionCommand());
			//send chat message to server and disable buttons so you cannot send again
			try 
			{
				output.writeUTF("S{]" + (char)(choice[(int)action.getActionCommand().charAt(10)]));
				for(int i = 0;i < choice.length;i++)
				{
					cardButton[i].setEnabled(false);
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		//show dialog when they click connect to server
		if(action.getActionCommand().equals( "ConnectDialogButton"))
		{
			 connectDialog.setVisible(true);
		}
		//send a submit with no choice byte meaning it signifys the player being ready to choose a new card
		if(action.getActionCommand().equals("DoneDiscussingButton"))
		{
			doneDiscussingButton.setEnabled(false);
			try
			{
				output.writeUTF("S{]" + (char)0);
			}
			catch(IOException ie)
			{
				System.out.println(ie);
			}
		}
		//attempt server connection, leave dialog open if not successfull
		if(action.getActionCommand().equals( "ConfirmIpButton"))
		{
			
			System.out.println("CONFIRM PRESS");
			try
			{
				//initialize communication objects
				socket = new Socket(connectDialogTextArea.getText(),1420);
				
				output = new DataOutputStream(socket.getOutputStream());
				input = new DataInputStream(socket.getInputStream());
			}
			catch(UnknownHostException u)
			{
				System.out.println(u);
			}
			catch(IOException i)
			{
				System.out.println(i);
			}
			try
			{
				//send player name to server and close dialog
				output.writeUTF(connectDialogNameArea.getText());
				setTitle("Ben S Scrum Poker Client - Connected as " + connectDialogNameArea.getText());
				connected = true;
				connectDialog.setVisible(false);
			}
			catch(IOException ie)
			{
				System.out.println(ie);
			}
			//add connection closing method only after there is a connection to be closed, prevents null pointer errors
			addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {			
				try
				{
					output.writeUTF("D{]");
					input.close();
					output.close();
					socket.close();
				}
				catch(IOException i)
				{
					System.out.println(i);
				}
				dispose();}});
		}
		//send formatted message to server if user intends to send message
		if(action.getActionCommand().equals("SendMessageButton"))
		{
				String send = "C{]";
				
				for(int i = 0; i < chatBar.getText().length();i++)
				{
					send+= chatBar.getText().charAt(i);
					if(i%40 == 0 && i > 30)
					{
						send+="\n";
					}

				}
				chatBar.setText("");
				System.out.println("SENDING");
				try 
				{
					output.writeUTF(send);
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
		}
		
	}
	public static void main(String[] args)
	{
		//run the application
		new ClientApplication();
	}

}
