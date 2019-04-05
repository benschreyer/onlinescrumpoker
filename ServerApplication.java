// Ben Schreyer BCC HS March/April AP Java scrum poker GUI server for WAN and LAN planning poker sessions
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.awt.*;
public class ServerApplication extends Frame implements ActionListener
{
	//used for globally ending server loop
	private boolean flag = true;
	//eclipse requires it?
	private static final long serialVersionUID = 1L;
	//server object, clients sockets and data streams
	private Socket[] socket;
	private ServerSocket server = null;
	private DataInputStream[] input;
	private DataOutputStream[] output;
	//game data
	private String[] name;
	private int[] choice;
	private String unit = "days";
	private String job = "build Rome";
	private int clients = 2;
	private boolean isPickPhase = true;
	private int submitCount = 0;
	//server username
	private String hostName = "unset";
	private boolean start = false;
	//awt objects for main window
	private TextArea chat = new TextArea();
	private TextArea chatBar = new TextArea();
	private Button sendMessageButton = new Button("Send");
	private Button startServerButton = new Button("Start");
	//awt objects for start server dialog
	private Dialog startServerDialog = new Dialog(this,"Start Server",true);
	private Button dialogStartServerButton = new Button("Start");
	private TextArea startServerDialogUnit = new TextArea();
	private TextArea startServerDialogJob = new TextArea();
	private TextArea startServerDialogName = new TextArea();
	private Label startServerDialogBackText1 = new Label("How many");
	private Label startServerDialogBackText2 = new Label("will it take to");
	private Label startServerDialogNameLabel = new Label("NAME:");
	private Label startServerDialogClientsLabel = new Label("PLAYERS:");
	private TextArea startServerDialogClientCount = new TextArea();
	//see if all players chose the same amount of time
	private boolean allChoiceSame()
	{
		for(int i = 1; i < clients;i++)
		{
			if(choice[i] != choice[i-1])
				return false;
		}
		return true;
	}
	//constructor and main server loop
	public ServerApplication()
	{
		//Setting style and position of start server dialog
		startServerDialog.setLayout(null);
		startServerDialog.setSize(550,130);
		startServerDialogName.setBounds(80,90,200,40);
		startServerDialogNameLabel.setBounds(10,90,100,40);
		startServerDialogUnit.setBounds(100,50,100,40);
		startServerDialogJob.setBounds(320,50,100,40);
		dialogStartServerButton.setBounds(440,100,100,20);
		dialogStartServerButton.setActionCommand("DialogStartServerButton");
		dialogStartServerButton.addActionListener(this);
		startServerDialogBackText1.setBounds(10,40,70,40);
		startServerDialogBackText2.setBounds(220,40,100,40);
		startServerDialogClientsLabel.setBounds(300,90,70,40);
		startServerDialogClientCount.setBounds(380, 90, 50, 40);
		//let window close button actually close window
		startServerDialog.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {startServerDialog.setVisible(false);}});
		//adding elements to dialog
		startServerDialog.add(startServerDialogClientCount);
		startServerDialog.add(startServerDialogClientsLabel);
		startServerDialog.add(startServerDialogBackText1);
		startServerDialog.add(startServerDialogBackText2);
		startServerDialog.add(dialogStartServerButton);
		startServerDialog.add(startServerDialogJob);
		startServerDialog.add(startServerDialogName);
		startServerDialog.add(startServerDialogNameLabel);
		startServerDialog.add(startServerDialogUnit);
		//configuring main window and its elements
		setTitle("Ben S Scrum Poker Server - Not Running");
		setResizable(false);
		setSize(600,400);
		setLayout(null);
		chatBar.setBounds(10,330,480,40);
		sendMessageButton.setBounds(490,330,100,20);
		sendMessageButton.setActionCommand("SendMessageButton");
		sendMessageButton.addActionListener(this);
		sendMessageButton.setEnabled(false);
		startServerButton.setBounds(490,350,100,20);
		startServerButton.setActionCommand("StartServerButton");
		startServerButton.addActionListener(this);
		chat.setBounds(10,150,580,180);
		chat.setEditable(false);
		//adding elements to main window
		add(startServerButton);
		add(sendMessageButton);
		add(chatBar);
		add(chat);
		//let main window be closed by button
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {			
			flag = false;start = true;dispose();}});
		//make the window visible to the user
		setVisible(true);
		//wait for user to configure server and start it
		while(!start)
		{
			System.out.println("USE THE START DIALOG TO START SERVER");
		}
		//allow user to send messages with button as the server is up
		sendMessageButton.setEnabled(true);
		try
		{
			//create server socket listening on port
			server = new ServerSocket(1420);
			System.out.println("Waiting for client...");
			//intialize game variables and datastreams
			choice = new int[clients];
			for(int i = 0;i < clients;i++)
			{
				choice[i] = i * -1 + 3;
			}
			socket = new Socket[clients];
			input = new DataInputStream[clients];
			output = new DataOutputStream[clients];
			name = new String[clients];
			//accept requests and configure client connections until player count is reached
			for(int i = 0;i < socket.length;i++)
			{
				socket[i] = server.accept();
				input[i] = new DataInputStream(new BufferedInputStream(socket[i].getInputStream()));
				output[i] = new DataOutputStream(socket[i].getOutputStream());
				name[i] = input[i].readUTF();
				output[i].writeUTF("C{]Connection Esablished");
				System.out.println(name[i] + " Connected::");
				chat.append(name[i]+" " + i + " out of " + clients + " joined\n");
			}
			System.out.println("All " + clients + "connected");
			//basic instructions sent to all players "C{]" is read as a chat message on the client side
			for(int i = 0;i < clients;i++)
			{
				output[i].writeUTF("C{]Game started! Pick how many " + unit + " you think it will take to " + job);
			}
			//game loop 
			String line = "";
			flag = true;
			while(flag)
			{
				//read incoming bytes from clients and interpret them to effect the game
				for(int i = 0;i < clients;i++)
				{
					//dont communicate with closed datastreams
					if(!socket[i].isClosed())
					{
						//dont try reading from an empty data buffer, read buffer as string if data available
						if(input[i].available() > 0)
						{
							line = input[i].readUTF();
							System.out.println(line);
						}
						else
							continue;
						//if a user has sent a chat message and the game currently permits chatting then send message to all clients and show it in the server application.
						if(line.substring(0,3).equals("C{]") && !isPickPhase);
						{
							//System.out.println(name[i].substring(0,name[i].length()) + ": " +line.substring(3));
							chat.append(name[i] + ": " + line.substring(3) + "\n");
							for(int j = 0;j < clients;j++)
							{
								if(!socket[j].isClosed())
									output[j].writeUTF("C{]" + name[i] + ": " + line.substring(3));
							}
						}
						//"S{]" means submit, depending on the game state submit with be followed by a byte that gives what number a client chose when choosing a card
						if(line.substring(0,3).equals("S{]"))
						{
							if(isPickPhase)
							{
								//use the last byte to find their choice
								choice[i] = line.charAt(3);
								submitCount++;
							}
							else
							{
								submitCount++;
							}
						}
					}
				}
				//end the game if all clients choose the same number
				if(allChoiceSame())
				{
					//tell clients they chose the same
					for(int i = 0;i < clients;i++)
					{
						if(!socket[i].isClosed())
							output[i].writeUTF("C{]" + " You all chose " + choice[0] +" "+unit);
					}
					//stop game loop
					flag = false;
					chat.append("All players agreed it will take " + choice[0] + " " + unit + "\n");
				}
				//if all players have submitted their choice or that they are done chatting the game state should change
				if(submitCount >= clients && flag)
				{
					isPickPhase = !isPickPhase;
					submitCount = 0;
					for(int i = 0;i < clients;i++)
					{
						if(isPickPhase)
						{
							//tell clients to set window as picking a card "G{]" means game state
							output[i].writeUTF("G{]P");
							//tell player through chat that it is time to pick again
							output[i].writeUTF("C{] Consider what was discussed and pick how many " + unit + " " + job + " will take");
						}
						else
						{
							//tell clients to set window for chat/discussing of results
							output[i].writeUTF("G{]T");
							//give who chose what so outliers know they have priority in discussing
							for(int j = 0;j < clients;j++)
							{
								output[i].writeUTF("C{]" + name[j] + " chose " + choice[j]);
							}
							//more chat to players
							output[i].writeUTF("C{]You did not all agree, take time to discuss, then hit the done discussing button when you are done.");
						}
					}
					//reset choices so they dont interfere with new picking phase
					for(int w = 0;w < clients;w++)
					{
						choice[w] = w * -1 - 12;
					}
				}

		
			}
			//clean up connection objects once game has over
			for(Socket s:socket)
			{
				s.close();
			}
			for(DataInputStream d:input)
			{
				d.close();
			}
			for(DataOutputStream d:output)
			{
				d.close();
			}
			


		}
		catch(IOException i)
		{
			System.out.println(i);
		}
		
	}
	@Override
	public void actionPerformed(ActionEvent action) 
	{
		//send out message sent by admin when they hit the send message button
		if(action.getActionCommand().equals("SendMessageButton"))
		{
			chat.append("ADMIN_" + hostName + ": " + chatBar.getText());
			for(int i = 0;i < clients;i++)
			{
				try {
					output[i].writeUTF("C{]ADMIN_" + hostName + ":" + chatBar.getText());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//clear chatbar
			chatBar.setText("");
		}
		//start server with given configuration when the user is done configuring and hits the button
		if(action.getActionCommand().equals("DialogStartServerButton"))
		{
			setTitle("Ben S Scrum Poker Server - Running");
			hostName = startServerDialogName.getText();
			unit = startServerDialogUnit.getText();
			job = startServerDialogJob.getText();
			clients = Integer.parseInt(startServerDialogClientCount.getText());
			start = true;
			startServerDialog.setVisible(false);
		}
		//bring up the start and configure dialog if the user clicks the button
		if(action.getActionCommand().equals("StartServerButton"))
		{
			startServerDialog.setVisible(true);
		}
		
	}
	public static void main(String[] args)
	{
		//run the application
		new ServerApplication();
	}

}
