package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.TylerOMeara.LolDataServer.Server.Exceptions.NullClientForRegionException;
import com.gvaneyck.rtmp.LoLRTMPSClient;

public class LoadBalancer 
{
	/**
	 * Stores all of the PvP.Net clients provided by the server admin.
	 */
	public static ConcurrentHashMap<String, HashMap<String,LoLRTMPSClient>> PvPNetClients = new ConcurrentHashMap<String, HashMap<String,LoLRTMPSClient>>();
	
	/**
	 * Returns a client from the specified region.
	 * @param region
	 * @return
	 * @throws NullClientForRegionException 
	 */
	public static LoLRTMPSClient returnClient(String region) throws NullClientForRegionException
	{
		if(PvPNetClients.get(region) == null)
		{
			throw new NullClientForRegionException("No client for region " + region + " available.", region);
		}
		int numClients = PvPNetClients.get(region).values().size();
		LoLRTMPSClient client = (LoLRTMPSClient) PvPNetClients.get(region).values().toArray()[(int)(Math.random()*numClients)];
		Main.log.fine("Loadbalancer returned " + region + "::" + client.getUserName());
		return client;
	}
	
	public static void registerNewClient(String x) throws IOException
	{
		String[] xsplit = x.split("::");
		//Creates a new client object for this particular username/pass combo
		LoLRTMPSClient client = new LoLRTMPSClient(xsplit[0], Main.PvPNetVersion.get(xsplit[0]), xsplit[1], xsplit[2]);
		
		HashMap<String, LoLRTMPSClient> region;
		//Handles if other clients from same region exist.
		if(PvPNetClients.containsKey(xsplit[0]))
		{
			region = PvPNetClients.get(xsplit[0]);
		}
		else //No other clients for this region
		{
			//Creates a new hashmap to hold all clients for the region
			region = new HashMap<String, LoLRTMPSClient>();
		}
		//Adds client to region hashmap
		region.put(xsplit[1], client);
		//Adds region hashmap to global clients hashmap
		PvPNetClients.put(xsplit[0], region);
		client.connectAndLogin();
		//TODO DEBUG CODE
		System.out.println("Connected to " + xsplit[0] + " with username: " + xsplit[1]);
		Main.log.info("Connected to " + xsplit[0] + " with username: " + xsplit[1]);
	}
}
