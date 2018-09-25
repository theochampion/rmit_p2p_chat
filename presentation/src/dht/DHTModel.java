package dht;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Peer;

public class DHTModel {
	Map<Integer, DHTNetwork> dhtList;
	
	public DHTModel()
	{
		dhtList = new HashMap<Integer, DHTNetwork>();
	}
	
	public int getTabSize()
	{
		return DHTNetwork.getTabSize();
	}
	
	public int getSize()
	{
		return dhtList.size();
	}
	
	public void initialDHT(String addAddress, int addPort)
	{
		DHTNetwork newDHT = new DHTNetwork();
		newDHT.initDHT(addAddress, addPort);
		
		if(!dhtList.containsKey(newDHT.getId()))
		{
			dhtList.put(newDHT.getId(), newDHT);
		}
	}
	
	public void addDHT(String targetAddress, int targetPort, String addAddress, int addPort)
	{
		DHTNetwork newDHT = new DHTNetwork();
		newDHT.initDHT(addAddress, addPort);
		int targetId;
		DHTNetwork targetNetwork;
		
		targetId = DHTNetwork.hash(targetAddress + targetPort);
		
		targetNetwork = dhtList.get(targetId);
		
		if(targetNetwork != null)
		{
			targetNetwork.addNode(addAddress, addPort);
		}
		
		if(!dhtList.containsKey(newDHT.getId()))
		{
			dhtList.put(newDHT.getId(), newDHT);
		}
	}
	
	public void removeDHT(String targetAddress, int targetPort, String delAddress, int delPort)
	{
		int delId;
		int targetId;
		DHTNetwork targetNetwork;
		DHTNode delNode;
		
		delId = DHTNetwork.hash(delAddress + delPort);
		targetId = DHTNetwork.hash(targetAddress + targetPort);
		targetNetwork = dhtList.get(targetId);
		delNode = new DHTNode(delId, delAddress, delPort);
		targetNetwork.clearNode(delNode);
		
		dhtList.remove(delId);
	}

	public void fillDHT(String targetAddress, int targetPort)
	{
		int counter;
		int targetId;
		DHTNetwork targetNetwork;
		
		targetId = DHTNetwork.hash(targetAddress + targetPort);
		if(!dhtList.containsKey(targetId))
		{
			DHTNetwork newDHT = new DHTNetwork();
			newDHT.initDHT(targetAddress, targetPort);
			dhtList.put(newDHT.getId(), newDHT);
		}
	
		targetNetwork = dhtList.get(targetId);
		
		for(DHTNetwork dht : dhtList.values())
		{
			targetNetwork.addNode(dht.getAddress(), 
					dht.getPort());
		}
	}
	
	public void printDHTInfo()
	{
		for(DHTNetwork dht : dhtList.values()) {
			dht.printDetails();
		}
	}
	
	public String[][] getTableInfo(int targetId)
	{
		DHTNetwork tempDht;
		
		if(dhtList.containsKey(targetId))
		{
			tempDht = dhtList.get(targetId);
			return tempDht.getTableInfo();
		}
		
		return null;
	}
	
	public String[][] getListInfo(int targetId)
	{
		DHTNetwork tempDht;
		
		if(dhtList.containsKey(targetId))
		{
			tempDht = dhtList.get(targetId);
			return tempDht.getListInfo();
		}
		
		return null;
	}
	
	public void addFile(String address, int port, String fileName)
	{
		DHTNetwork tempDht;
		int targetId;
		
		targetId = DHTNetwork.hash(address + port);
		
		if(dhtList.containsKey(targetId))
		{
			tempDht = dhtList.get(targetId);
			tempDht.addFileName(fileName);
		}
	}
	
	public String[][] getFileNames(int targetId)
	{
		DHTNetwork tempDht;
		
		if(dhtList.containsKey(targetId))
		{
			tempDht = dhtList.get(targetId);
			return tempDht.getFileNames();
		}
		return null;
	}
}
