package dht;


import java.util.ArrayList;
import java.util.List;

/**
 * This class is made to keep track of all finger table information.
 * Because we actually need to contact peers, we can't just have an 
 * id in the "target" field. This class uses DHT nodes to emulate 
 * a version of this "target" field where we can not only know the 
 * id of the node but also pull out contact information.
 * @author Alex
 *
 */
public class FingerTable {
	
	private String ip; //Ip of this peer.
	private int port; //Port of this peer.
	private int id; //Id of THIS peer.
	private int tabSize; //Size of table.
	Object[][] tabData; //Actual finger table data.
	
	//Constructor for finger table. Specify id hash and size of table.
	public FingerTable(int id, String ip, int port, int tabSize)
	{
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.tabSize = tabSize;
		tabData = new Object[tabSize][2]; //One field for target and another
		//for the link. The field "i" is the same as the actual index of this
		//structure.
		constructTable();
	}
	
	//Fill out the target fields.
	public void constructTable()
	{
		int counter;
		int maxIds;
		DHTNode self;
		
		//The entire ID space.
		maxIds = (int) Math.pow(2, tabSize-1);
		self = new DHTNode(id, ip, port);
		
		//Creating the target value for each field.
		for(counter=0; counter<tabSize; counter++)
		{
			tabData[counter][0] = (int)(id + Math.pow(2, counter))%maxIds;
			tabData[counter][1] = self;
		}
	}
	
	public void addNode(int newId, String ip, int port)
	{
		int counter;
		int tempTarget;
		DHTNode tempNode;
		DHTNode newNode;
		
		//Make the new DHT node object.
		newNode = new DHTNode(newId, ip, port);
		
		//Checking each field in the table.
		for(counter=0; counter<tabSize; counter++)
		{
			//Get target and link information one row.
			tempTarget = (int)tabData[counter][0];
			tempNode = (DHTNode)tabData[counter][1];
			
			//Normal case where the new target is closer than the
			//old one. 
			if(tempNode.getId() >= tempTarget)
			{
				if((newId > tempTarget) && (newId < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
			else
			{
				if((newId > tempTarget) || (newId < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
		}
	}
	
	public void addNode(DHTNode newNode)
	{
		int counter;
		int tempTarget;
		DHTNode tempNode;
		
		if(newNode == null)
		{
			return;
		}
		
		//Checking each field in the table.
		for(counter=0; counter<tabSize; counter++)
		{
			//Get target and link information one row.
			tempTarget = (int)tabData[counter][0];
			tempNode = (DHTNode)tabData[counter][1];
			
			//Normal case where the new target is closer than the
			//old one. 
			if(tempNode.getId() >= tempTarget)
			{
				if((newNode.getId() > tempTarget) && (newNode.getId() < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
			else
			{
				if((newNode.getId() > tempTarget) || (newNode.getId() < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
		}
	}
	
	public void printTable()
	{
		int counter;
		DHTNode tempNode;
		
		//Cycle through and print everything.
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			System.out.println("Number: " + counter + ", Target: " + 
		tabData[counter][0] + ", Link: " + tempNode.getId() + ", Address: "
		+ tempNode.getAddress() + ", Port: " + tempNode.getPort());
		}
	}
	
	public DHTNode findSuccessor(int itemId)
	{
		int counter;
		boolean smallestId = true;
		DHTNode targetNode = null;
		Integer targetId = null;
		DHTNode tempNode;
		Integer tempId = null;
		
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			tempId = (Integer)tabData[counter][0];
			//Check for the largest node that is smaller than the item id.
			if((tempId <= itemId) && ((targetId == null) || 
					(tempId > targetId)))
			{
				smallestId = false;
				targetId = tempId;
				targetNode = tempNode;
			}
		}
		
		//If we actually found one, return it.
		if(!smallestId)
		{
			return targetNode;
		}
		else
		{
			targetId = null;
			//Otherwise, it means they're all larger than the item id. That means 
			//we need to look for the  node with the largest id.
			for(counter=0; counter<tabSize; counter++)
			{
				tempId = (Integer)tabData[counter][0];
				tempNode = (DHTNode)tabData[counter][1];
				if((targetId == null) || (tempId > targetId))
				{
					targetId = tempId;
					targetNode = tempNode;
				}
			}
			
			return targetNode;
		}
	}
	
	//Get a list of all unique nodes in the finger table.
	public List<DHTNode> getNodes()
	{
		List<DHTNode> list = new ArrayList<DHTNode>();
		int prevId = -1;
		int counter = 0;
		DHTNode tempNode;
		
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			if(tempNode.getId() != prevId)
			{
				list.add(tempNode);
				prevId = tempNode.getId();
			}
		}
		
		return list;
	}
	
	public void replaceNode(DHTNode oldNode, DHTNode newNode)
	{
		int counter;
		DHTNode tempNode;
		
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			if(tempNode.getId() == oldNode.getId())
			{
				tabData[counter][1] = newNode;
			}
		}
	}
	
	public String[][] getTableInfo()
	{
		String[][] stringTable = new String[tabSize][5];
		int counter;
		DHTNode tempNode;
		
		for(counter=0;counter<tabSize;counter++)
		{
			stringTable[counter][0] = Integer.toString(counter);
			stringTable[counter][1] = Integer.toString((int)tabData[counter][0]);
			
			tempNode = (DHTNode)tabData[counter][1];
			
			stringTable[counter][2] = Integer.toString(tempNode.getId());
			stringTable[counter][3] = tempNode.getAddress();
			stringTable[counter][4] = Integer.toString(tempNode.getPort());
		}
		
		return stringTable;
	}
}
