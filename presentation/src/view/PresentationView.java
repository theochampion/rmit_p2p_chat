package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import dht.DHTModel;
/*
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
*/

import java.awt.*;
import java.awt.geom.Point2D;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Vector;
import java.util.List;

import network.*;
import model.*;

public class PresentationView implements Runnable{
	
	private PresentationModel model;
	private JLabel serverDetails;
	private CommManager comm;
	private JFrame frame;
	private Container panel;
	private JTable peers;
	private JTable log;
	private JTable files;
	private DefaultTableModel logModel;
	private DefaultTableModel peerModel;
	private DefaultTableModel fingerModel;
	private DefaultTableModel listModel;
	private DefaultTableModel fileModel;
	private JScrollPane spLog;
	private JScrollPane spPeers;
	private JScrollPane spFinger;
	private JScrollPane spLists;
	private JScrollPane spFiles;
	private PresentationGraph graph;
	private DHTGraph dhtGraph;
	private JTabbedPane graphTabs;
	private JTable fTable;
	private JTable lists;
	private DHTModel dhtModel;
	private JPanel dhtPanel;
	private JPanel centralisedPanel;
	private JPanel dhtNetPanel;
	
	public PresentationView()
	{
		logModel = new DefaultTableModel();
		peerModel = new DefaultTableModel();
		fingerModel = new DefaultTableModel();
		listModel = new DefaultTableModel();
		fileModel = new DefaultTableModel();
		frame = new JFrame("P2P Presentation");
		graphTabs = new JTabbedPane();
		serverDetails = new JLabel();
		panel = frame.getContentPane();
		peers = new JTable(peerModel);
		log = new JTable(logModel);
		fTable = new JTable(fingerModel);
		lists = new JTable(listModel);
		files = new JTable(fileModel);
		spLog = new JScrollPane(log);
		spPeers = new JScrollPane(peers);
		spFinger = new JScrollPane(fTable);
		spLists = new JScrollPane(lists);
		spFiles = new JScrollPane(files);
		peers.setEnabled(false);
		peers.setPreferredSize(new Dimension(400,400));
		peers.setPreferredScrollableViewportSize(peers.getPreferredSize());
		peers.setFillsViewportHeight(true);
		//log.setPreferredSize(new Dimension(600,100));
		log.setEnabled(false);
		//log.setPreferredScrollableViewportSize(log.getPreferredSize());
		log.setFillsViewportHeight(true);
		fTable.setPreferredSize(new Dimension(400,400));
		fTable.setEnabled(false);
		fTable.setPreferredScrollableViewportSize(fTable.getPreferredSize());
		fTable.setFillsViewportHeight(true);
		lists.setPreferredSize(new Dimension(200,400));
		lists.setEnabled(false);
		lists.setPreferredScrollableViewportSize(lists.getPreferredSize());
		lists.setFillsViewportHeight(true);
		files.setPreferredSize(new Dimension(200,400));
		files.setEnabled(false);
		files.setPreferredScrollableViewportSize(files.getPreferredSize());
		files.setFillsViewportHeight(true);
		graph = new PresentationGraph(this);
		dhtGraph = new DHTGraph(this);
		dhtPanel = new JPanel();
		centralisedPanel = new JPanel();
		dhtNetPanel = new JPanel();
		
		logModel.addColumn("Time"); 
		logModel.addColumn("Header");
		logModel.addColumn("Method");
		logModel.addColumn("Encryption");
		logModel.addColumn("Sender");
		logModel.addColumn("Address");
		logModel.addColumn("Port");
		logModel.addColumn("Receiver");
		logModel.addColumn("Address");
		logModel.addColumn("Port");
		logModel.addColumn("Contents");
		logModel.addColumn("Size");
		logModel.addColumn("Send Time");
		
		peerModel.addColumn("Username");
		peerModel.addColumn("Address");
		peerModel.addColumn("Port");
		
		fingerModel.addColumn("No");
		fingerModel.addColumn("Target");
		fingerModel.addColumn("ID");
		fingerModel.addColumn("Address");
		fingerModel.addColumn("Port");
		
		listModel.addColumn("ID");
		listModel.addColumn("Address");
		listModel.addColumn("Port");
		
		fileModel.addColumn("Filename");
		fileModel.addColumn("ID");
		
		panel.setLayout(new BorderLayout());
		dhtPanel.setLayout(new BorderLayout());
		centralisedPanel.setLayout(new BorderLayout());
		dhtNetPanel.setLayout(new GridLayout(3,1));
		dhtNetPanel.add(spLists);
		dhtNetPanel.add(spFinger);
		dhtNetPanel.add(spFiles);
		graphTabs.add("Centralised P2P", centralisedPanel);
		graphTabs.add("DHT", dhtPanel);
		panel.add(serverDetails, BorderLayout.NORTH);
		panel.add(graphTabs, BorderLayout.CENTER);
		panel.add(spLog, BorderLayout.SOUTH);
		centralisedPanel.add(graph, BorderLayout.CENTER);
		centralisedPanel.add(spPeers, BorderLayout.EAST);
		dhtPanel.add(dhtGraph, BorderLayout.CENTER);
		dhtPanel.add(dhtNetPanel, BorderLayout.EAST);
		frame.setSize(1024,768);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.pack();
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure you want to exit?", "EXIT SESSION", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		            System.exit(0);
		        }
		    }
		});
	}
	
	public void initialise()
	{
		try {
			serverDetails.setText("Presentation Server Address: " + comm.getPresentationAddress()
					+ ", Presentation Server Port: " + comm.getPresentationPort());
		} catch (UnknownHostException e) {
			
		}
		refreshPeerList();
		frame.setVisible(true);
		new Thread(this).start();
	}
	
	public void initComm(CommManager comm)
	{
		this.comm = comm;
	}
	
	public void initModel(PresentationModel model)
	{
		this.model = model;
	}
	
	public void initDhtModel(DHTModel dhtModel)
	{
		this.dhtModel =  dhtModel;
	}
	
	public void refreshPeerList()
	{
		Map<String, Peer> peerList;
		peerModel.setRowCount(0);
		

		peerList = model.getPeerList();
		
		for(Peer peer : peerList.values()) {
			Vector<Object> row = new Vector<Object>();
			row.add(peer.getUsername()); 
			row.add(peer.getAddress());
			row.add(peer.getPort());
			
			peerModel.addRow(row);
		}
	}
	
	public void refreshFingerTable(int id)
	{
		String[][] tableList;
		fingerModel.setRowCount(0);
		
		tableList = dhtModel.getTableInfo(id);
		if(tableList == null)
		{
			return;
		}
		int counter;
		
		for(counter=0;counter<dhtModel.getTabSize();counter++)
		{
			Vector<Object> row = new Vector<Object>();
			row.add(tableList[counter][0]);
			row.add(tableList[counter][1]);
			row.add(tableList[counter][2]);
			row.add(tableList[counter][3]);
			row.add(tableList[counter][4]);
			
			fingerModel.addRow(row);
		}
	}
	
	public void refreshFileNames(int id)
	{
		String[][] fileList;
		fileModel.setRowCount(0);
		int counter;
		
		fileList = dhtModel.getFileNames(id);
		if(fileList == null)
		{
			return;
		}
		for(counter=0;counter<fileList[0].length;counter++)
		{
			Vector<Object> row = new Vector<Object>();
			row.add(fileList[0][counter]);
			row.add(fileList[1][counter]);
			fileModel.addRow(row);
		}
	}
	
	public void refreshLists(int id)
	{
		String[][] tableList;
		listModel.setRowCount(0);
		
		tableList = dhtModel.getListInfo(id);
		if(tableList == null)
		{
			return;
		}
		int counter;
		
		for(counter=0;counter<6;counter++)
		{
			Vector<Object> row = new Vector<Object>();
			row.add(tableList[counter][0]);
			row.add(tableList[counter][1]);
			row.add(tableList[counter][2]);
			
			listModel.addRow(row);
		}
	}
	
	public void logData(String time, String packetTag, String method, String encryption, 
			String srcUsername, String srcAddress,
			String srcPort, String destUsername, String destAddress, String destPort, 
			List<String> packetContents
			, String size, String transTime)
	{		
		Vector<Object> row = new Vector<Object>();
		row.add(time); 
		row.add(packetTag);
		row.add(method);
		row.add(encryption);
		row.add(srcUsername);
		row.add(srcAddress);
		row.add(srcPort);
		row.add(destUsername);
		row.add(destAddress);
		row.add(destPort);
		row.add(packetContents);
		row.add(size);
		
		//Check time.
		if(Integer.parseInt(transTime) < 0)
		{
			transTime = "0";
		}
		
		row.add(transTime);
		logModel.addRow(row);
	}
	
	public void addPeer(String username)
	{
		graph.addPeer(username);
	}
	
	public void removePeer(String username)
	{
		graph.removePeer(username);
	}

	public void addPeerComm(String sender, String receiver, String type)
	{
		graph.addPeerComm(sender, receiver, type);
	}
	
	public void addNode(String ip, int port)
	{
		dhtGraph.addNode(ip, port);
	}
	
	public void removeNode(String ip, int port)
	{
		dhtGraph.removeNode(ip, port);
	}
	
	public void addNodeComm(String senderAddress, int senderPort, 
			String recvAddress, int recvPort, String type)
	{
		dhtGraph.addNodeComm(senderAddress, senderPort, 
				recvAddress, recvPort, type);
	}
	
	public void run()
	{
		while(true)
		{
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			
			}
			graph.cycleComms();
			dhtGraph.cycleComms();
		}
	}
	
	public boolean legitimateJoin(String ip, int port)
	{
		return dhtGraph.legitimateJoin(ip, port);
	}
}

