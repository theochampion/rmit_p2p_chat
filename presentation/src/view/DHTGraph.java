package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;

import javax.swing.*;

import model.Peer;


public class DHTGraph extends JPanel implements Runnable{

	private Ellipse2D chordRing;
	private Map<Integer, Double> nodeList;
	private List<Communication> commList;
	private int tabSize = 9;
	private Communication newComm;
	private PresentationView view;
	private int currentId = -1;
	private Font nameFont;
	
	public DHTGraph(PresentationView view)
	{
		setVisible(true);
		nodeList = new HashMap<Integer, Double>();
		commList = new ArrayList<Communication>();
		setBackground(Color.WHITE);
		this.view = view;
		nameFont = new Font("nameFont", Font.BOLD, 14);
		new Thread(this).start();
		
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				triggerTableChange(e.getX(), e.getY());
			}
		});
	}
	
	public void triggerTableChange(int x, int y) 
	{
		double nodeX;
		double nodeY;
		double tempDist;
		Dimension size;
		int d;
		Double minDistance = null;
		int minId = -1;
		
		size = getSize();
		d = Math.min(size.width, size.height) - 50;
		
		for (Map.Entry<Integer, Double> entry : nodeList.entrySet())
		{
			nodeX = (d/2) * Math.cos(entry.getValue()) + chordRing.getCenterX() - 5;
			nodeY = (d/2) * Math.sin(entry.getValue()) + chordRing.getCenterY() - 5;
			
			tempDist = (double) Math.sqrt(
		            Math.pow(x - nodeX, 2) +
		            Math.pow(y - nodeY, 2) );
			
			if(minDistance == null)
			{
				minDistance = tempDist;
				minId = entry.getKey();
			}
			if(minDistance > tempDist)
			{
				minDistance = tempDist;
				minId = entry.getKey();
			}
		}
		
		if((minId == -1)||(minDistance > 20))
		{
			currentId = -1;
			refreshTable();
			return;
		}
		currentId = minId;
		refreshTable();
	}
	
	public void refreshTable()
	{
		view.refreshFingerTable(currentId);
		view.refreshLists(currentId);
		view.refreshFileNames(currentId);
	}

	public void paintComponent(Graphics g)
	{
		Dimension size;
		int d;
		int x;
		int y;
		Graphics2D g2d;
		double nodeX = -1;
		double nodeY = -1;
		double node2X = -1;
		double node2Y = -1;
		Ellipse2D newNode;
		Double senderAngle;
		Double receiverAngle;
		Line2D communicationLine;
		double midX;
		double midY;
		boolean errantComm = false;
		String commLabel = null;
		
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		g2d.setFont(nameFont);
		//Creating the chord ring.
		size = getSize();
		d = Math.min(size.width, size.height) - 50;
		x = (size.width - d)/2;
		y = (size.height - d)/2; 
		chordRing = new Ellipse2D.Double(x, y, d, d);
		g.setColor(Color.BLACK);
		g2d.draw(chordRing);
		
		//Redraw all the nodes.
		for (Map.Entry<Integer, Double> entry : nodeList.entrySet())
		{
			//Recalculate the actual points in case of
			//resizing.
			nodeX = (d/2) * Math.cos(entry.getValue()) + chordRing.getCenterX() - 5;
			nodeY = (d/2) * Math.sin(entry.getValue()) + chordRing.getCenterY() - 5;
			
			newNode = new Ellipse2D.Double(nodeX, nodeY, 10, 10);
			g.setColor(getNodeColor(entry.getKey()));
			g2d.fill(newNode);
			
			g.setColor(Color.BLACK);
			g2d.drawString(Integer.toString(entry.getKey()), 
					(int)newNode.getCenterX(), (int)newNode.getCenterY());
		}
		
		//Redraw all the communications.
		if(newComm != null)
		{
			if(newComm.getType().equalsIgnoreCase("DHT_JOIN"))
			{
				g.setColor(Color.GREEN);
				commLabel = "Join the network";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_SETUP"))
			{
				g.setColor(Color.BLUE);
				commLabel = "Send setup information";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_ADD"))
			{
				g.setColor(Color.CYAN);
				commLabel = "Pass on node addition";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_REMOVAL"))
			{
				g.setColor(Color.ORANGE);
				commLabel = "Pass on node removal";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_UP"))
			{
				g.setColor(Color.YELLOW);
				commLabel = "Forward upload request";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_UP_CONFIRM"))
			{
				g.setColor(Color.GRAY);
				commLabel = "Allow upload";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_DOWN"))
			{
				g.setColor(Color.MAGENTA);
				commLabel = "Forward download request"; 
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_DOWN_CONFIRM"))
			{
				g.setColor(Color.LIGHT_GRAY);
				commLabel = "Allow download"; 
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_PRED_CHECK"))
			{
				g.setColor(Color.RED);
				commLabel = "Check for node life";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_PRED_CONFIRM"))
			{
				g.setColor(Color.PINK);
				commLabel = "Respond to life check";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_DEATH"))
			{
				g.setColor(Color.DARK_GRAY);
				commLabel = "Pass on node death";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_FIX"))
			{
				g.setColor(Color.ORANGE);
				commLabel = "Fix predecessor & successor lists";
			}
			else if(newComm.getType().equalsIgnoreCase("DHT_TRANSFER"))
			{
				g.setColor(Color.GREEN);
				commLabel = "Transfer file";
			}
			
			senderAngle = nodeList.get(newComm.getSender());
			receiverAngle = nodeList.get(newComm.getReceiver());
			
			//Get coordinates of the current nodes.
			if((senderAngle == null))
			{
				errantComm = true;
			}
			else
			{
				nodeX = (d/2) * Math.cos(senderAngle) + chordRing.getCenterX() - 5;
				nodeY = (d/2) * Math.sin(senderAngle) + chordRing.getCenterY() - 5;
			}
			if(receiverAngle == null)
			{
				errantComm = true;
			}
			else
			{
				node2X = (d/2) * Math.cos(receiverAngle) + chordRing.getCenterX() - 5;
				node2Y = (d/2) * Math.sin(receiverAngle) + chordRing.getCenterY() - 5;
			}
			
			//Draw a line between the nodes.
			if(!errantComm)
			{
				communicationLine = new Line2D.Double(nodeX, nodeY, node2X, node2Y);
				g2d.draw(communicationLine);
			
				//Get midpoint.
				midX = (node2X - nodeX)/2 + nodeX;
				midY = (node2Y - nodeY)/2 + nodeY;
				
				//Write packet tag at midpoint.
				g.setColor(Color.BLACK);
				g2d.drawString(commLabel, (int)midX, (int)midY);
			}
		}
	}
	
	//What we're doing here is taking a random angle
	//somewhere in the chord ring and placing a small
	//filled ellipse there.
	public void addNode(String address, int port)
	{
		double angle;
		int id;
		
		id = hash(address + port);
		angle = nodeAngle(id);
		
		if(nodeList.containsKey(id))
		{
			return;
		}
	
		nodeList.put(id, angle);
		validate();
		repaint();
	}
	
	public void removeNode(String address, int port)
	{
		int id;
		
		id = hash(address + port);
		nodeList.remove(id);
		validate();
		repaint();
	}
	
	public double nodeAngle(Integer id)
	{
		//We want our chord ring to use degrees starting from 
		//north. Therefore add 90 degrees to make it look correct.
		double angle = (id/(Math.pow(2, tabSize-1)))*360 - 90;
		double radianVal;
		
		radianVal = Math.toRadians(angle);
		return radianVal;
	}
	
	public void addNodeComm(String senderAddress, int senderPort, 
			String recvAddress, int recvPort, String type)
	{
		Communication newComm;
		int senderId;
		int recvId;
		
		senderId = hash(senderAddress + senderPort);
		recvId = hash(recvAddress + recvPort);
		
		newComm = new Communication(senderId, recvId, type);
		commList.add(newComm);
		//validate();
		//repaint();
	}
	
	//Hash function.
	public int hash(String input)
	{
		MessageDigest hash;
		byte[] digestBytes;
		BigInteger hashVal = null;
		int id;
		int idSpace;
			
		try
		{
			//Hash the input string.
			hash = MessageDigest.getInstance("SHA-1");
			hash.reset();
			hash.update(input.getBytes("utf8"));
			digestBytes = hash.digest();
			//Get the numerical value of the digest.
			hashVal = new BigInteger(digestBytes);
		}
		catch(Exception e)
		{}
			
		//Get possible values in id space.
		idSpace = (int)Math.pow(2, tabSize-1);
		//Use mod operator to get something in
		//between 0 and idSpace.
		id = hashVal.intValue()%idSpace;
		id = Math.abs(id);
		return id;
	}
	
	public void cycleComms()
	{
		//revalidate();
		if(commList.size() > 0)
		{
			newComm = commList.get(0);
		}
		else
		{
			newComm = null;
		}
		repaint();
		if(commList.size()>0)
		{
			commList.remove(0);
		}
	}
	
	//Check that a new node is connecting to a node that already
	//exists in our chord ring.
	public boolean legitimateJoin(String ip, int port)
	{
		int id;
		
		id = hash(ip + port);
		
		if(nodeList.containsKey(id))
		{
			return true;
		}
		
		return false;
	}
	
	public void run()
	{
		while(true)
		{
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			
			}
			refreshTable();
		}
	}
	
	private Color getNodeColor(int id)
	{
		int remainder;
		remainder = id%10;
		
		if(remainder == 0)
		{
			return Color.CYAN;
		}
		else if(remainder == 1)
		{
			return Color.DARK_GRAY;
		}
		else if(remainder == 2)
		{
			return Color.GRAY;
		}
		else if(remainder == 3)
		{
			return Color.GREEN;
		}
		else if(remainder == 4)
		{
			return Color.LIGHT_GRAY;
		}
		else if(remainder == 5)
		{
			return Color.MAGENTA;
		}
		else if(remainder == 6)
		{
			return Color.ORANGE;
		}
		else if(remainder == 7)
		{
			return Color.PINK;
		}
		else if(remainder == 8)
		{
			return Color.RED;
		}
		else
		{
			return Color.YELLOW;
		}
	}
}