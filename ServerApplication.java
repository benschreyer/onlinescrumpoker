import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.awt.*;
public class ServerApplication extends Frame implements ActionListener
{
	/**
	 * 
	 */
	private boolean flag = true;
	
	private static final long serialVersionUID = 1L;
	private Socket[] socket;
	private ServerSocket server = null;
	private DataInputStream[] input;
	private DataOutputStream[] output;
	private String[] name;
	private int[] choice;
	private String unit = "days";
	private String job = "build Rome";
	private int clients = 2;
	private boolean isPickPhase = true;
	private int submitCount = 0;
	private String hostName = "unset";
	private boolean start = false;
	
	private TextArea chat = new TextArea();
	private TextArea chatBar = new TextArea();
	private Button sendMessageButton = new Button("Send");
	private Button startServerButton = new Button("Start");
	
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
	private boolean allChoiceSame()
	{
		for(int i = 1; i < clients;i++)
		{
			if(choice[i] != choice[i-1])
				return false;
		}
		return true;
	}
	public ServerApplication()
	{
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
		startServerDialog.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {startServerDialog.setVisible(false);}});
		
		startServerDialog.add(startServerDialogClientCount);
		startServerDialog.add(startServerDialogClientsLabel);
		startServerDialog.add(startServerDialogBackText1);
		startServerDialog.add(startServerDialogBackText2);
		startServerDialog.add(dialogStartServerButton);
		startServerDialog.add(startServerDialogJob);
		startServerDialog.add(startServerDialogName);
		startServerDialog.add(startServerDialogNameLabel);
		startServerDialog.add(startServerDialogUnit);
		
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
		add(startServerButton);
		add(sendMessageButton);
		add(chatBar);
		add(chat);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e) {			
			flag = false;start = true;dispose();}});
		setVisible(true);
		while(!start)
		{
			System.out.println("USE THE START DIALOG TO START SERVER");
		}
		sendMessageButton.setEnabled(true);
		try
		{
			
			server = new ServerSocket(1420);
			System.out.println("Waiting for client...");
			choice = new int[clients];
			for(int i = 0;i < clients;i++)
			{
				choice[i] = i * -1 + 3;
			}
			socket = new Socket[clients];
			input = new DataInputStream[clients];
			output = new DataOutputStream[clients];
			name = new String[clients];
			
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
			for(int i = 0;i < clients;i++)
			{
				output[i].writeUTF("C{]Game started! Pick how many " + unit + " you think it will take to " + job);
			}
			String line = "";
			flag = true;
			while(flag)
			{
				for(int i = 0;i < clients;i++)
				{
					if(!socket[i].isClosed())
					{
						if(input[i].available() > 0)
						{
							line = input[i].readUTF();
							System.out.println(line);
						}
						else
							continue;
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
						if(line.substring(0,3).equals("S{]"))
						{
							if(isPickPhase)
							{
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
				if(allChoiceSame())
				{
					for(int i = 0;i < clients;i++)
					{
						if(!socket[i].isClosed())
							output[i].writeUTF("C{]" + " You all chose " + choice[0] +" "+unit);
					}
					flag = false;
					chat.append("All players agreed it will take " + choice[0] + " " + unit + "\n");
				}
				if(submitCount >= clients && flag)
				{
					isPickPhase = !isPickPhase;
					submitCount = 0;
					for(int i = 0;i < clients;i++)
					{
						if(isPickPhase)
						{
							output[i].writeUTF("G{]P");
							output[i].writeUTF("C{] Consider what was discussed and pick how many " + unit + " " + job + " will take");
						}
						else
						{
							output[i].writeUTF("G{]T");
							for(int j = 0;j < clients;j++)
							{
								output[i].writeUTF("C{]" + name[j] + " chose " + choice[j]);
							}
							output[i].writeUTF("C{]You did not all agree, take time to discuss, then hit the done discussing button when you are done.");
						}
					}
					for(int w = 0;w < clients;w++)
					{
						choice[w] = w * -1 - 12;
					}
				}

		
			}
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
	public static void main(String[] args)
	{
		new ServerApplication();
	}
	@Override
	public void actionPerformed(ActionEvent action) 
	{
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
			chatBar.setText("");
		}
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
		if(action.getActionCommand().equals("StartServerButton"))
		{
			startServerDialog.setVisible(true);
		}
		
	}
}
