package view;

import java.awt.*;
import java.awt.geom.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;

import javax.swing.*;

import model.Peer;


public class PresentationGraph extends JPanel{

	private Ellipse2D netRing;
	private Map<String, Double> nodeList;
	private List<Communication> commList;
	private String deregisteredUser = null;
	private Communication newComm = null;
	private PresentationView view;
	
	public PresentationGraph(PresentationView view)
	{
		setVisible(true);
		nodeList = new HashMap<String, Double>();
		commList = new ArrayList<Communication>();
		setBackground(Color.WHITE);
		this.view = view;
	}
	
	public void paintComponent(Graphics g)
	{
		Dimension size;
		int d;
		int x;
		int y;
		Graphics2D g2d;
		double nodeX;
		double nodeY;
		double node2X;
		double node2Y;
		Ellipse2D server;
		Ellipse2D newNode;
		Double senderAngle;
		Double receiverAngle;
		Line2D communicationLine;
		double midX;
		double midY;
		Font nameFont;
		String commLabel = null;
		
		super.paintComponent(g);
		nameFont = new Font("nameFont", Font.BOLD, 14);
		g2d = (Graphics2D) g;
		g2d.setFont(nameFont);
		
		//Creating the network ring.
		size = getSize();
		d = Math.min(size.width, size.height) - 50;
		x = (size.width - d)/2;
		y = (size.height - d)/2; 
		netRing = new Ellipse2D.Double(x, y, d, d);
		g.setColor(Color.BLACK);
		g2d.draw(netRing);
		
		g.setColor(Color.BLUE);
		server = new Ellipse2D.Double(netRing.getCenterX() - 10, netRing.getCenterY() - 10, 20, 20);
		g2d.fill(server);
		
		g.setColor(Color.BLACK);
		g2d.drawString("Server", (int)server.getCenterX(), (int)server.getCenterY());
		
		if(deregisteredUser != null)
		{
			removePeer(deregisteredUser);
			deregisteredUser = null;
		}
		
		//Redraw all the nodes.
		for (Map.Entry<String, Double> entry : nodeList.entrySet())
		{
			//Recalculate the actual points in case of
			//resizing.
			nodeX = (d/2) * Math.cos(entry.getValue()) + netRing.getCenterX() - 5;
			nodeY = (d/2) * Math.sin(entry.getValue()) + netRing.getCenterY() - 5;
			
			newNode = new Ellipse2D.Double(nodeX, nodeY, 10, 10);
			g.setColor(getNodeColor(hash(entry.getKey())));
			g2d.fill(newNode);
			
			g.setColor(Color.BLACK);
			g2d.drawString(entry.getKey(), (int)newNode.getCenterX(), (int)newNode.getCenterY());
		}
		
		//Redraw all the communications.
		if(newComm != null)
		{
			if(newComm.getType().equalsIgnoreCase("REGISTRATION_SUCCESS"))
			{
				g.setColor(Color.GREEN);
				commLabel = "Register confirmation";
			}
			else if(newComm.getType().equalsIgnoreCase("REGISTER"))
			{
				g.setColor(Color.CYAN);
				commLabel = "Register on the network";
			}
			else if(newComm.getType().equalsIgnoreCase("MESSAGE"))
			{
				g.setColor(Color.ORANGE);
				commLabel = "Send user message";
			}
			else if(newComm.getType().equalsIgnoreCase("PEER_LIST"))
			{
				g.setColor(Color.YELLOW);
				commLabel = "Distribute peer list";
			}
			else if(newComm.getType().equalsIgnoreCase("DEREGISTER"))
			{
				g.setColor(Color.MAGENTA);
				commLabel = "Leave network";
				deregisteredUser = (String)newComm.getSender();
			}
			else if(newComm.getType().equalsIgnoreCase("LIFE_CHECK"))
			{
				g.setColor(Color.BLUE);
				commLabel = "Check for peer life";
			}
			else if(newComm.getType().equalsIgnoreCase("LIFE_CONFIRM"))
			{
				g.setColor(Color.BLUE);
				commLabel = "Respond to life check";
			}
			else if(newComm.getType().equalsIgnoreCase("FILE_SEND"))
			{
				g.setColor(Color.RED);
				commLabel = "Send file";
			}
			else if(newComm.getType().equalsIgnoreCase("ERROR"))
			{
				g.setColor(Color.GRAY);
				commLabel = "Username taken";
			}
			
			senderAngle = nodeList.get(newComm.getSender());
			receiverAngle = nodeList.get(newComm.getReceiver());
			
			//Get coordinates of the current nodes.
			if((senderAngle == null))
			{
				nodeX = server.getCenterX();
				nodeY = server.getCenterY();
			}
			else
			{
				nodeX = (d/2) * Math.cos(senderAngle) + netRing.getCenterX() - 5;
				nodeY = (d/2) * Math.sin(senderAngle) + netRing.getCenterY() - 5;
			}
			if(receiverAngle == null)
			{
				node2X = server.getCenterX();
				node2Y = server.getCenterY();
			}
			else
			{
				node2X = (d/2) * Math.cos(receiverAngle) + netRing.getCenterX() - 5;
				node2Y = (d/2) * Math.sin(receiverAngle) + netRing.getCenterY() - 5;
			}
			
			//Draw a line between the nodes.
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
	
	//What we're doing here is taking a random angle
	//somewhere in the chord ring and placing a small
	//filled ellipse there.
	public void addPeer(String username)
	{
		double angle;
		
		angle = randAngle();
	
		if(nodeList.containsKey(username))
		{
			return;
		}
		
		nodeList.put(username, angle);
		//revalidate();
		repaint();
	}
	
	public void removePeer(String username)
	{
		nodeList.remove(username);
		//validate();
		//repaint();
	}
	
	public double randAngle()
	{
		//Generate a random angle value between 0 and 360.
		double angle = Math.random()*Math.PI*2;
		
		return angle;
	}
	
	public void addPeerComm(String sender, String receiver, String type)
	{
		Communication newComm;
		
		newComm = new Communication(sender, receiver, type);
		commList.add(newComm);
		//validate();
		//repaint();
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
	
	//Hash function. Need this purely for systematically
	//determining colors.
	public static int hash(String input)
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
		
		//Use mod operator to get something in
		//between 0 and idSpace.
		id = hashVal.intValue()%10;
		id = Math.abs(id);
		return id;
	}
}