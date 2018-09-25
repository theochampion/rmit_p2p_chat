package dht;


/**
 * The purpose of this class is to keep track of ONE individual DHT node.
 * This includes the id, address and port of the node. These objects are
 * kept in the finger table.
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class DHTNode {
	
	//Information for each DHT node.
	private int id;
	private String address;
	private int port;
	
	//Constructor for a DHT node.
	public DHTNode(int id, String address, int port)
	{
		this.id = id;
		this.address = address;
		this.port = port;
	}
	
	//Getters for instance variables.
	public int getId()
	{
		return id;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}
}
