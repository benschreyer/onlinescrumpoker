// Ben Schreyer BCC HS March/April AP Java scrum poker command line server for WAN and LAN planning poker sessions
import java.net.*;
import java.io.*;
public class ServerTestCommandline 
{
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
	private boolean allChoiceSame()
	{
		for(int i = 1; i < clients;i++)
		{
			if(choice[i] != choice[i-1])
				return false;
		}
		return true;
	}
	public ServerTestCommandline()
	{
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
			}
			System.out.println("All " + clients + "connected");
			for(int i = 0;i < clients;i++)
			{
				output[i].writeUTF("C{]Game started! Pick how many " + unit + " you think it will take to " + job);
			}
			String line = "";
			boolean flag = true;
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
							output[i].writeUTF("G{]C");
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
				/*else if(isPickPhase)
				{
					isPickPhase = false;
					for(int i = 0;i < clients;i++)
					{
						for(int j = 0;j < clients;j++)
						{
							output[i].("C{]" + name[j] + " chose " + choice[j]);
						}
						output[i].writeUTF("G{]D");
						output[i].writeUTF("C{]You did not all agree, take time to discuss, then hit the done discussing button when you are done.");
					}
				}*/
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
		new ServerTestCommandline();
	}
}
