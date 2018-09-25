package view;

public class Communication {
	
	private Object sender;
	private Object receiver;
	private String type;
	
	public Communication(String sender, String receiver, String type)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
	}
	
	public Communication(int sender, int receiver, String type)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
	}
	
	public Object getSender()
	{
		return sender;
	}
	
	public Object getReceiver()
	{
		return receiver;
	}
	
	public String getType()
	{
		return type;
	}
	
	public String toString()
	{
		return type;
	}
}
