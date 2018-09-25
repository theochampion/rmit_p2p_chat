package app;

import dht.DHTModel;
import network.*;
import view.*;
import model.*;
import file.*;

/**
 * The driver class that contains the main method and pulls the view, 
 * model and network components together.
 * @author Alex
 * @version 0.2
 * @since 0.2
 */
public class PresentationApp {

	public static void main(String[] args) {

		PresentationNetwork network = new PresentationNetwork();
		PresentationModel model = new PresentationModel();
		PresentationConsole console  = new PresentationConsole();
		FileManager file = new FileManager();
		PresentationLogin login = new PresentationLogin();
		PresentationView view = new PresentationView();
		DHTModel dhtModel = new DHTModel();
		
		network.initModel(model);
		network.initConsole(console);
		network.initFile(file);
		network.initView(view);
		file.initConsole(console);
		login.initNetwork(network);
		login.initView(view);
		view.initModel(model);
		view.initDhtModel(dhtModel);
		model.initView(view);
		network.initDHTModel(dhtModel);
		
		/*
		if(args.length == 2)
		{
			network.initSocket(args[0], args[1]);
		}
		else
		{
			network.initSocket();
		}
		
		new Thread(network).start();
		*/
	}

}
