package network;

import model.*;
import view.*;
import file.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.text.*;

import dht.DHTModel;

/**
 * This class is primarily designed to coordinate the network and deal with 
 * message sending and receiving.
 * <p>
 * The UDPPacketManager class is utilised to carry out these duties.
 * @author Alex
 * @version 0.2
 * @since 0.1
 */
public class PresentationNetwork extends Thread{

	private PresentationConsole console;
	private PresentationModel model;
	private CommManager comm;
	private PresentationView view;
	private FileManager file;
	private DHTModel dhtModel;
	
	private static String threadName = "P2P Server";
	private InetAddress serverAddress;
	private int serverPort;
	private boolean isRunning = false;
	private boolean existingDHT = false;
	
	/**
	 * The constructor for the ServerNetwork class.
	 * @throws IOException
	 */
	public PresentationNetwork()
	{
		super(threadName);
		isRunning = true;
	}
	
	/**
	 * Use the UDP class to initialise the socket.
	 */
	public void initSocket()
	{
		try
		{
			comm.initSocket();
		}
		catch(IOException ioException)
		{
			console.printError("Socket could not be created.");
		}
	}
	
	/**
	 * Use the UDP class to intialise the socket. The address
	 * and port can be specified in this version of the method.
	 * @param address The String representation of the requested IP address.
	 * @param port The requested port number.
	 */
	public void initSocket(String address, String port)
	{
		try
		{
			comm.initSocket(address, port);
		}
		catch(IOException ioException)
		{
			console.printError("Socket could not be created.");
		}
	}
	
	/**
	 * Returns the IP address of the local host. If host is unknown, 
	 * UnknownHostException will be thrown.
	 * @return A String containing the IP address of the local host.
	 */
	public String getPresentationAddress() throws UnknownHostException
	{
		return comm.getPresentationAddress();
	}
	
	/**
	 * Returns the port that the server application is using.
	 * @return An int giving the port number that the server application 
	 * is using on the local host.
	 */
	public int getPresentationPort()
	{
		return comm.getPresentationPort();
	}
	
	public void initView(PresentationView view)
	{
		this.view = view;
	}
	
	/**
	 * Method to pass a reference to the model to the network
	 * class.
	 * @param network The PresentationModel Object that is being passed in.
	 */
	public void initModel(PresentationModel model)
	{
		this.model = model;
	}
	
	public void initDHTModel(DHTModel dhtModel)
	{
		this.dhtModel = dhtModel;
	}
	
	/**
	 * Method to pass a reference to the console to the network
	 * class.
	 * @param network The PresentationConsole Object that is being passed in.
	 */
	public void initConsole(PresentationConsole console)
	{
		this.console = console;
	}
	
	/**
	 * Method to pass a reference to the comm to the network
	 * class.
	 * @param network The UDPPacketManager Object that is being passed in.
	 */
	public void initComm(CommManager comm)
	{
		this.comm = comm;
	}
	
	public void initFile(FileManager file)
	{
		this.file = file;
	}
	
	/**
	 * This thread's run method. 
	 * <p>
	 * This method receives packets using
	 * the comm class and dissects their contents so that they can
	 * be stored appropriately in the log file.
	 */
	public void run()
	{
		List<String> packetContents;
		String time;
		String method;
		String packetTag;
		String encryption;
		String srcUsername;
		String srcAddress;
		String srcPort;
		String destUsername;
		String destAddress;
		String destPort;
		String size;
		String transTime;
		Peer newPeer;
		
		try
		{
			console.printConnectionDetails(getPresentationAddress(), 
					getPresentationPort());
		}
		catch(UnknownHostException uhException)
		{
			console.printError("Host address could not be identified.");
			System.exit(0);
		}
		
		file.initFile();
		
		while(isRunning)
		{
			comm.clearMsgCache();
			System.out.println("Trying to retrieve a packet.");
			try
			{
				packetContents = comm.receivePacket();
			}
			catch(IOException ioException)
			{
				console.printError("An IO error occured while " +
						"recieving a packet.");
				continue;
			}
			catch(ClassNotFoundException cnfException)
			{
				console.printError("An erroneous packet was received.");
				continue;
			}
			System.out.println("Retrieved a packet.");
			
			//Retrieve packet contents and allocate to variables.
			System.out.println(packetContents);
			packetTag = packetContents.remove(0);
			if(packetTag.equalsIgnoreCase("RCV_CONFIRM"))
			{
				continue;
			}
			if(packetTag.equalsIgnoreCase("DUPLICATE"))
			{
				continue;
			}
			destPort = packetContents.remove(packetContents.size()-1);
			destAddress = packetContents.remove(packetContents.size()-1);
			if(destAddress.equalsIgnoreCase("127.0.0.1"))
			{
				try {
					destAddress = comm.getPresentationAddress();
				} catch (UnknownHostException e) {
				}
			}
			if(packetTag.equalsIgnoreCase("PRES_FILE"))
			{
				dhtModel.addFile(destAddress, Integer.parseInt(destPort), packetContents.remove(0));
				continue;
			}
			if(packetTag.equalsIgnoreCase("PRES_DHT_ADD"))
			{
				if(!existingDHT)
				{
					dhtModel.initialDHT(destAddress, Integer.parseInt(destPort));
					view.addNode(destAddress, Integer.parseInt(destPort));
					existingDHT = true;
				}
				continue;
			}
			if(packetTag.equalsIgnoreCase("PRES_DEATH"))
			{
				model.forceVisualPeerRemoval(packetContents.remove(0));
				continue;
			}
			destUsername = packetContents.remove(packetContents.size()-1);
			size = packetContents.remove(packetContents.size()-1);
			srcPort = packetContents.remove(packetContents.size()-1);
			srcAddress = packetContents.remove(packetContents.size()-1);
			srcUsername = packetContents.remove(packetContents.size()-1);
			encryption = packetContents.remove(packetContents.size()-1);
			method = packetContents.remove(packetContents.size()-1);
			time = packetContents.remove(packetContents.size()-1);
			
			//Determine what kind of packet was sent and make changes
			//to model based on this.
			if(packetTag.equalsIgnoreCase("REGISTER"))
			{
				//The very first DHT node doesn't send any information to 
				//our network. We need to add the node by ourselves because 
				//it's actually starting the chord ring by itself.
				if(!existingDHT)
				{
					existingDHT = true;
					if(srcAddress.equalsIgnoreCase("127.0.0.1"))
					{
						try {
							srcAddress = comm.getPresentationAddress();
						} catch (UnknownHostException e) {
						}
					}
					view.addNode(srcAddress, Integer.parseInt(srcPort));
					dhtModel.initialDHT(srcAddress, Integer.parseInt(srcPort));
				}
				if(model.usernameAvailable(srcUsername))
				{
					view.addPeer(srcUsername);
				}
			}
			
			//Don't get loopback address. This will affect node IDs.
			if(srcAddress.equalsIgnoreCase("127.0.0.1"))
			{
				try {
					srcAddress = comm.getPresentationAddress();
				} catch (UnknownHostException e) {
				}
			}
			
			if(packetTag.equalsIgnoreCase("REGISTRATION_SUCCESS"))
			{
				newPeer = new Peer(destUsername, destAddress, Integer.parseInt(destPort));
				model.registerPeer(newPeer);
				view.addPeer(destUsername);
			}
			else if(packetTag.equalsIgnoreCase("DEREGISTER"))
			{
				model.removePeer(srcUsername);
			}
			else if(packetTag.equalsIgnoreCase("DHT_JOIN"))
			{
				if(!existingDHT)
				{
					existingDHT = true;
					view.addNode(destAddress, Integer.parseInt(destPort));
				}
				if(view.legitimateJoin(destAddress, Integer.parseInt(destPort)))
				{
					view.addNode(srcAddress, Integer.parseInt(srcPort));
					dhtModel.initialDHT(destAddress, Integer.parseInt(destPort));
					dhtModel.addDHT(destAddress, Integer.parseInt(destPort),
							srcAddress, Integer.parseInt(srcPort));
					dhtModel.addDHT(srcAddress, Integer.parseInt(srcPort),
							destAddress, Integer.parseInt(destPort));
				}
			}
			else if(packetTag.equalsIgnoreCase("DHT_SETUP"))
			{
				if(!existingDHT)
				{
					existingDHT = true;
					view.addNode(srcAddress, Integer.parseInt(srcPort));
				}
				if(view.legitimateJoin(srcAddress, Integer.parseInt(srcPort)))
				{
					view.addNode(destAddress, Integer.parseInt(destPort));
					dhtModel.fillDHT(destAddress, Integer.parseInt(destPort));
				}
			}
			else if((packetTag.equalsIgnoreCase("DHT_REMOVAL"))||
					(packetTag.equalsIgnoreCase("DHT_DEATH")))
			{
				String delAddress;
				int delPort;
				
				delAddress = packetContents.get(0);
				delPort = Integer.parseInt(packetContents.get(1));
				
				view.removeNode(delAddress, delPort);
				dhtModel.removeDHT(destAddress, Integer.parseInt(destPort), delAddress, delPort);
				dhtModel.fillDHT(destAddress, Integer.parseInt(destPort));
			}
			else if((packetTag.equalsIgnoreCase("DHT_ADD"))||
					(packetTag.equalsIgnoreCase("DHT_FIX")))
			{
				String addAddress;
				int addPort;
				
				addAddress = packetContents.get(0);
				addPort = Integer.parseInt(packetContents.get(1));
				
				dhtModel.addDHT(destAddress, Integer.parseInt(destPort), addAddress, addPort);
			}
			else if((packetTag.equalsIgnoreCase("DHT_UP_CONFIRM")))
			{
				String fileName;
				
				fileName = packetContents.get(0);
				dhtModel.addFile(srcAddress, Integer.parseInt(srcPort), fileName);
			}
			else if((packetTag.equalsIgnoreCase("DHT_TRANSFER")))
			{
				String fileName;
				
				fileName = packetContents.get(0);
				dhtModel.addFile(destAddress, Integer.parseInt(destPort), fileName);
			}
			if(packetTag.startsWith("DHT_"))
			{
				view.addNodeComm(srcAddress, Integer.parseInt(srcPort), 
						destAddress, Integer.parseInt(destPort), packetTag);
			}
			else
			{
				view.addPeerComm(srcUsername, destUsername, packetTag);
			}
			
			//Declare two dates. One is the current date.
			Date sendDate;
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat
					("yyyy-MM-dd hh:mm:ss.SSS");
			try
			{
				//Format the recieved date string into date
				//object.
				sendDate = dateFormat.parse(time);

			}
			catch(ParseException pException)
			{
				console.printError("Malformed date field in packet recieved.");
				continue;
			}
			
			//Subtract the two dates to find the transmission time in 
			//milliseconds.
			transTime = Long.toString(sendDate.getTime() - date.getTime());
			
			file.storeMessage(time, packetTag, method, encryption, srcUsername, srcAddress,
					srcPort, destUsername, destAddress, destPort, packetContents
					, size, transTime);
			console.printLog(time, packetTag, method, encryption, srcUsername, srcAddress,
					srcPort, destUsername, destAddress, destPort, packetContents
					, size, transTime);
			view.logData(time, packetTag, method, encryption, srcUsername, srcAddress,
					srcPort, destUsername, destAddress, destPort, packetContents
					, size, transTime);
			//view.addComm(srcUsername, destUsername, packetTag);
			dhtModel.printDHTInfo();
		}
	}
}