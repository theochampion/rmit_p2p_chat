package model;

import java.util.*;

import view.PresentationView;

/**
 * The purpose of this class is to manage peers. Peers can be registered 
 * and kept track of or deregistered in the PresentationModel class. 
 * @author Alex
 * @version 0.2
 * @since 0.1
 */
public class PresentationModel {
	
	//A hash map containing all of the peer objects 
	//with the username as the key.
	private Map<String, Peer> peerList;
	private PresentationView view;
	
	/**
	 * The constructor for the ServerModel class.
	 */
	public PresentationModel()
	{
		peerList = new HashMap<String, Peer>();
	}
	
	public void initView(PresentationView view)
	{
		this.view = view;
	}
	
	/**
	 * Used to register a peer with the server and add to the map holding the peer data.
	 * @param peer The peer Object for the peer that is being registered.
	 */
	public void registerPeer(Peer newPeer)
	{
		peerList.put(newPeer.getUsername(), newPeer);
		view.refreshPeerList();
	}
	
	/**
	 * Used to deregister a peer with the server and remove from the map holding the peer data.
	 * @param username The username of the peer that is being deregistered.
	 */
	public void removePeer(String username)
	{
		peerList.remove(username);
		view.refreshPeerList();
	}
	
	public void forceVisualPeerRemoval(String username)
	{
		removePeer(username);
		view.removePeer(username);
	}
	
	/*
	 * Checks if username availability
	 * Returns T if username HAS NOT been registered already
	 * Returns F if username HAS been registered already
	 */
	/**
	 * Used to check if a peer with a certain username already exists.
	 * @param username The string for the username that is being checked.
	 * @return Returns T if user has NOT been registered and F if username has been registered.
	 */
	public boolean usernameAvailable(String username)
	{
		return (!peerList.containsKey(username));
	}
	
	/**
	 * Returns a collection view of all of the peer data stored in the peers Map structure. Peers can be 
	 * removed from the collection and these changes will be reflected in the map.
	 * @return The collection object.
	 */
	public Collection<Peer> getPeers()
	{
		return peerList.values();
	}
	
	/**
	 * Getter for the peer list.
	 * @return A Map structure containing the listed peer information.
	 */
	public Map<String, Peer> getPeerList()
	{
		return peerList;
	}
	
	/**
	 * Used to retrieve an ArrayList of Strings containing information about all the peers in the peer list.
	 * Each string in the returned ArrayList represents one peer's information and is given in the form:
	 * username;address;port
	 * @return The ArrayList of peer strings.
	 */
	public List<String> getPeersList() {
		List<String> newList = new ArrayList<String>();
		
		for(Peer peer : peerList.values()) {
			newList.add(peer.getUsername() + ";" + 
					peer.getAddress() + ";" + peer.getPort());
		}
		
		return newList;
	}
}