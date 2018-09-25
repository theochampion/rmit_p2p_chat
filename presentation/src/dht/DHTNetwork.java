package dht;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import network.*;

import java.io.File;
import java.lang.*;
import java.net.*;


public class DHTNetwork {
	
	private String ip;
	private int port;
	private int id;
	private int storeNo= 3;
	private static int tabSize = 9;
	private DHTNode[] successorList;
	private DHTNode[] predecessorList;
	private FingerTable fTable;
	private List<String> fileNames;
	
	//Constructor for the DHT system.
	public DHTNetwork()
	{}
	
	public static int getTabSize()
	{
		return tabSize;
	}
	
	public void initDHT(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
		id = hash(ip + port);
		fTable = new FingerTable(id, ip, port, tabSize);
		successorList = new DHTNode[storeNo];
		predecessorList = new DHTNode[storeNo];
		fileNames = new CopyOnWriteArrayList<String>();
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getAddress()
	{
		return ip;
	}
	
	public int getPort()
	{
		return port;
	}
	
	//Hash function.
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
		
		//Get possible values in id space.
		idSpace = (int)Math.pow(2, tabSize-1);
		//Use mod operator to get something in
		//between 0 and idSpace.
		id = hashVal.intValue()%idSpace;
		id = Math.abs(id);
		return id;
	}
	
	public DHTNode query(int itemId)
	{
		DHTNode searchNode = null;
		//Case where the current node is responsible for this
		//item id.
		if(predecessorList[0] == null)
		{
			return searchNode;
		}
		if((itemId > predecessorList[0].getId()))
		{
			if(itemId <= id)
			{
				return searchNode;
			}
			if(id < predecessorList[0].getId())
			{
				return searchNode;
			}
		}
		if((itemId < predecessorList[0].getId()))
		{
			if((id < predecessorList[0].getId())&&(id >= itemId))
			{
				return searchNode;
			}
		}

		searchNode = fTable.findSuccessor(itemId);
			
		if(searchNode.getId() == id)
		{
			searchNode = null;
		}
		
		return searchNode;
	}
	
	public void addNode(String ip, int port)
	{
		int newId;
		
		newId = hash(ip + port);
		fTable.addNode(newId, ip, port);
		
		int counter;
		
		for(counter=0; counter<storeNo; counter++)
		{
			if(attemptSetSuccessor(ip, port, counter))
			{
				break;
			}
		}
		
		for(counter=0; counter<storeNo ; counter++)
		{
			if(attemptSetPredecessor(ip, port, counter))
			{
				break;
			}
		}
	}
	
	//Method to set one of the successors to a new node.
	public boolean attemptSetSuccessor(String newIp, int newPort, int index)
	{
		int newId;
		DHTNode tempNode;
		
		newId = hash(newIp + newPort);
		tempNode = new DHTNode(newId, newIp, newPort);
		
		if(newId == id)
		{
			return true;
		}
		
		if(successorList[index] == null)
		{
			setSuccessor(tempNode, index);
			return true;
		}
		else if(newId == successorList[index].getId())
		{
			return true;
		}
		else if(newId > id)
		{
			if(successorList[index].getId() < id)
			{
				setSuccessor(tempNode, index);
				return true;
			}
			if(successorList[index].getId() > newId)
			{
				setSuccessor(tempNode, index);
				return true;
			}
		}
		else
		{
			if(successorList[index].getId() > id)
			{
				return false;
			}
			if(successorList[index].getId() > newId)
			{
				setSuccessor(tempNode, index);
				return true;
			}
		}
		return false;
	}
	
	public void setSuccessor(DHTNode newNode, int index)
	{
		int counter;
		
		//Shift back every successor after the one we're 
		//inserting.
		for(counter=(storeNo-1); counter>index; counter--)
		{
			successorList[counter] = successorList[counter-1];
		}
		
		successorList[index] = newNode;
	}
	
	//Method to set one of the predecessors to a new node.
	public boolean attemptSetPredecessor(String newIp, int newPort, int index)
	{
		int newId;
		DHTNode tempNode;
		
		newId = hash(newIp + newPort);
		tempNode = new DHTNode(newId, newIp, newPort);
		
		if(newId == id)
		{
			return true;
		}
		
		if(predecessorList[index] == null)
		{
			setPredecessor(tempNode, index);
			return true;
		}
		else if(newId == predecessorList[index].getId())
		{
			return true;
		}
		else if(newId > id)
		{
			if(predecessorList[index].getId() < id)
			{
				return false;
			}
			if(predecessorList[index].getId() < newId)
			{
				setPredecessor(tempNode, index);
				return true;
			}
		}
		else
		{
			if(predecessorList[index].getId() > id)
			{
				setPredecessor(tempNode, index);
				return true;
			}
			if(predecessorList[index].getId() < newId)
			{
				setPredecessor(tempNode, index);
				return true;
			}
		}
		return false;
	}
	
	public void setPredecessor(DHTNode newNode, int index)
	{
		int counter;
		
		//Shift back every successor after the one we're 
		//inserting.
		for(counter=(storeNo-1); counter>index; counter--)
		{
			predecessorList[counter] = predecessorList[counter-1];
		}
		
		predecessorList[index] = newNode;
	}
	
	public void clearNode(DHTNode oldNode)
	{
		//Firstly replace the deleted node with ourself (the least optimal
		//node before trying to add the rest). Typically, the successor 0
		//should take it's place but we don't need to dictate that in code.
		//Our add function should take appropriate action.
		fTable.replaceNode(oldNode, new DHTNode(id, ip, port));
		
		int counter;
		for(counter=0; counter<storeNo; counter++)
		{
			attemptRemovePredecessor(oldNode, counter);
			attemptRemoveSuccessor(oldNode, counter);
		}
	}
	
	public void attemptRemovePredecessor(DHTNode oldNode, int index)
	{
		int counter;
		
		if(predecessorList[index] == null)
		{
			return;
		}
		
		if(oldNode.getId() != predecessorList[index].getId())
		{
			return;
		}
		
		//Push everything forwards.
		for(counter=index; counter<(storeNo-1); counter++)
		{
			predecessorList[counter] = predecessorList[counter+1];
		}
		
		predecessorList[storeNo-1] = null;
	}
	
	public void attemptRemoveSuccessor(DHTNode oldNode, int index)
	{
		int counter;
		
		if(successorList[index] == null)
		{
			return;
		}
		
		if(oldNode.getId() != successorList[index].getId())
		{
			return;
		}
		
		//Shift back every successor after the one we're 
		//inserting.
		for(counter=index; counter<(storeNo-1); counter++)
		{
			successorList[counter] = successorList[counter+1];
		}
		
		successorList[storeNo-1] = null;
	}
	
	public void addFileName(String newName)
	{
		fileNames.add(newName);
	}
	
	public void removeFileName(String newName)
	{
		int counter;
		
		for(counter=0; counter<fileNames.size(); counter++)
		{
			if(fileNames.get(counter).equalsIgnoreCase(newName))
			{
				fileNames.remove(counter);
				return;
			}
		}
	}
	
	public void printDetails()
	{
		System.out.println("ID: " + id);
		System.out.println("Address: " + ip);
		System.out.println("Port: " + port);
		
		int counter;
		
		for(counter=0; counter<storeNo; counter++)
		{
			if(successorList[counter] != null)
			{
				System.out.println("Successor " + (counter+1) + ": " + 
					successorList[counter].getId());
			}
			else
			{
				System.out.println("Successor " + (counter+1) + ": Not set");
			}
		}
		
		for(counter=0; counter<storeNo; counter++)
		{
			if(predecessorList[counter] != null)
			{
				System.out.println("Predecessor " + (counter+1) + ": " + 
					predecessorList[counter].getId());
			}
			else
			{
				System.out.println("Predecessor " + (counter+1) + ": Not set");
			}
		}
		fTable.printTable();
		printFileNames();
	}
	
	public void printFileNames()
	{
		int counter;
		int fileId;
		
		for(counter=0; counter<fileNames.size(); counter++)
		{
			fileId = hash(fileNames.get(counter));
			System.out.println(fileNames.get(counter) + " (" + fileId + ")");
		}
	}
	
	public String[][] getTableInfo()
	{
		return fTable.getTableInfo();
	}
	
	public String[][] getListInfo()
	{
		String[][] stringList = new String[storeNo*2][3];
		
		int counter;
		int index = 0;
		
		for(counter=0; counter<storeNo; counter++)
		{
			addListInfo(stringList, predecessorList[counter], index);
			index++;
		}
		for(counter=0; counter<storeNo; counter++)
		{
			addListInfo(stringList, successorList[counter], index);
			index++;
		}
		
		return stringList;
	}
	
	public void addListInfo(String[][] stringList, DHTNode addNode, int index)
	{
		if(addNode != null)
		{
			stringList[index][0] = Integer.toString(addNode.getId());
			stringList[index][1] = addNode.getAddress();
			stringList[index][2] = Integer.toString(addNode.getPort());
		}
		else
		{
			stringList[index][0] = "Not set";
			stringList[index][1] = "Not set";
			stringList[index][2] = "Not set";
		}
	}
	
	public String[][] getFileNames()
	{
		String[][] fileList;
		fileList = new String[2][fileNames.size()];
		int counter;
		
		for(counter=0;counter<fileNames.size();counter++)
		{
			fileList[0][counter] = fileNames.get(counter);
			fileList[1][counter] = Integer.toString(hash(fileNames.get(counter)));
		}
		
		return fileList;
	}
}
