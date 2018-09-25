package view;

import java.util.List;

/**
 * This class controls output to console.
 * @author Alex
 * @version 0.2
 * @since 0.2
 */
public class PresentationConsole {

	/**
	 * Print local address and port to console.
	 * @param address IP address in String form.
	 * @param port The port number.
	 */
	public void printConnectionDetails(String address, int port)
	{
		System.out.println("Presentation Details:");
		System.out.println("IP Address: " + address);
		System.out.println("Port: " + port);
		System.out.println("");
	}
	
	/**
	 * Print customised error message to console.
	 * @param message Error message.
	 */
	public void printError(String message)
	{
		System.out.println("ERROR: " + message);
	}
	
	public void printLog(String time, String tag, String method, String
			encryption, String sUsername, String sAddress, String sPort, 
			String dUsername, String dAddress, String dPort, 
			List<String> contents, String size, String transTime)
	{
		System.out.print(time + ", " + tag + ", " + method + ", "
				+ encryption + ", ");
		System.out.print(sUsername + ", " + sAddress + ", " + sPort 
				+ ", ");
		System.out.print(dUsername + ", " + dAddress + ", " + dPort 
				+ ", ");
		System.out.print(contents + ", " + size + ", " + transTime
				+ "\n");
	}
	
}
