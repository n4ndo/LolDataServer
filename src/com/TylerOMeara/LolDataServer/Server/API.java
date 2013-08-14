package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.gvaneyck.rtmp.LoLRTMPSClient;
import com.gvaneyck.rtmp.TypedObject;

public class API 
{
	//TODO HANDLE ERRORS!
	public static String manualRequest(String line)
	{
		//Region_Service:operation-param1,param2,...
		String region = line.substring(0,line.indexOf("_"));
		String service = line.substring(line.indexOf("_")+1,line.indexOf(":"));
		String operation = line.substring(line.indexOf(":")+1,line.indexOf("-"));
		String temp = line.substring(line.indexOf("-")+1);
		String[] params = temp.split(",");
		Object[] obj = new Object[params.length];
		for(int x = 0; x < params.length; x++)
		{
			obj[x] = params[x];
		}
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke(service, operation, obj);
			return String.valueOf(client.getResult(id));
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * playerStatsService
	 * getAggregatedStats
	 * @return
	 */
	//TODO Check for null
	public static String getRankedStats(String region, int acctID, String gameMode, String season)
	{
		HashMap<Integer, HashMap<String, Double>> champions = new HashMap<Integer, HashMap<String, Double>>();
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke("playerStatsService", "getAggregatedStats", new Object[] {acctID,gameMode,season});
			Object[] array = (client.getResult(id).getTO("data").getTO("body").getArray("lifetimeStatistics"));
			for(Object x : array)
			{
				if(x instanceof TypedObject)
				{
					TypedObject y = (TypedObject) x;
					//Data for champion exists
					if(champions.containsKey(y.getInt("championId")))
					{
						champions.get(y.getInt("championId")).put(y.getString("statType"), y.getDouble("value"));
					}
					else //Data for champion doesn't exist
					{
						HashMap<String, Double> champion = new HashMap<String, Double>();
						champion.put(y.getString("statType"), y.getDouble("value"));
						champions.put(y.getInt("championId"), champion);
					}
				}
			}
			//Generates the JSON from collected data
			String json = "{";
			for(int x : champions.keySet())
			{
				json += ("\"" + x + "\"" + ":{");
				for(String s : champions.get(x).keySet())
				{
					json += ("\"" + s + "\":" + champions.get(x).get(s) + ",");
				}
				json = json.substring(0,json.length()-1);
				json += "},";
			}
			json = json.substring(0,json.length()-1);
			json += "}";
			return json;
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private static String genericAPICall(String region, String service, String operation, Object[] args)
	{
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			String json = "{";
			int id = client.invoke(service, operation, args);
			TypedObject data = client.getResult(id);//.getTO("data").getTO("body");
			for(String x : data.keySet())
			{
				json = addObject(json, data, x);
			}
			json = json.substring(0,json.length()-1);
			json += "}";
			return json;
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String genericRawOutput(String region, String service, String operation, Object[] args)
	{
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke(service, operation, args);
			return String.valueOf(client.getResult(id));
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getRecentMatchHistory(String region, int accountID)
	{
		return genericAPICall(region, "playerStatsService", "getRecentGames", new Object[] {accountID});
	}
	
	public static String getSummonerByName(String region, String name)
	{
		return genericAPICall(region, "summonerService", "getSummonerByName", new Object[] {name});
	}
	
	public static String getInGameProgressInfo(String region, String name)
	{
		return genericAPICall(region, "gameService", "retrieveInProgressSpectatorGameInfo", new Object[] {name});
	}
	
	public static String getLeagueData(String region, int summonerID, String queue)
	{
		return genericAPICall(region, "LeaguesServiceProxy", "getLeagueForPlayer", new Object[] {summonerID,queue});
	}
	
	public static String getSummonersByIDs(String region, Object[] summonerIDs)
	{
		return genericAPICall(region, "summonerService", "getSummonerNames", new Object[]{summonerIDs});
	}
	
	public static String getAllPublicSummonerDataByAccount(String region, int accountID)
	{
		return genericAPICall(region, "summonerService", "getAllPublicSummonerDataByAccount", new Object[]{accountID});
	}
	
	public static String getAllSummonerDataByAccount(String region, int accountID)
	{
		return genericAPICall(region, "summonerService", "getAllSummonerDataByAccount", new Object[]{accountID});
	}
	
	public static String getPlayerStatsByAccount(String region, int accountID)
	{
		return genericAPICall(region, "playerStatsService", "retrievePlayerStatsByAccountId", new Object[]{accountID});
	}
	
	public static String getPlayerRankedTeams(String region, int summonerID)
	{
		return genericAPICall(region, "summonerTeamService", "findPlayer", new Object[]{summonerID});
	}
	
	public static String getAllLeaguesForPlayer(String region, int summonerID)
	{
		return genericAPICall(region, "leaguesServiceProxy", "getAllLeaguesForPlayer", new Object[]{summonerID});
	}
	
	public static String getLoginDataPacketForUser(String region)
	{
		return genericAPICall(region, "clientFacadeService", "getLoginDataPacketForUser", new Object[0]);
	}
	
	private static String addObject(String json, TypedObject data, String x)
	{
		json += "\"" + x + "\":";
		if(data.get(x) == null)
		{
			json += "\"null\","; 
		}
		if(data.get(x) instanceof Integer)
		{
			json += data.getInt(x) + ",";
			return json;
		}
		if(data.get(x) instanceof Double)
		{
			json += data.getDouble(x) + ",";
			return json;
		}
		if(data.get(x) instanceof Boolean)
		{
			json += data.getBool(x) + ",";
			return json;
		}
		if(data.get(x) instanceof String)
		{
			json += "\"" + data.getString(x) + "\",";
			return json;
		}
		if(data.get(x) instanceof Date)
		{
			json += "\"" + data.getDate(x) + "\",";
			return json;
		}
		if(data.get(x) instanceof Byte)
		{
			json += Integer.valueOf(String.valueOf(data.get(x))) + ",";
			return json;
		}
		//Need to check if it is an array manually because data.get(x) returns a TypedObject even when it is an Array
		if(!isArray(data, x) && data.get(x) instanceof TypedObject)
		{
			json += "{";
			for(String s : data.getTO(x).keySet())
			{
				json = addObject(json, data.getTO(x), s);
			}
			if(data.getTO(x).size() > 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "},";
			return json;
		}
		if(data.get(x) instanceof byte[])
		{
			byte[] array = (byte[]) data.get(x);
			json += "[";
			for(Object o : array)
			{
				json = addArrayObject(json, o);
			}
			if(array.length != 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "],";
			return json;
		}
		if(data.get(x) instanceof Object[] || isArray(data, x))
		{
			Object[] array = data.getArray(x);
			json += "[";
			for(Object o : array)
			{
				json = addArrayObject(json, o);
			}
			if(array.length != 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "],";
			return json;
		}
		
		return json;
	}
	
	private static String addArrayObject(String json, Object o)
	{
		if(o == null)
		{
			json += "\"null\","; 
		}
		if(o instanceof Integer)
		{
			json += o + ",";
			return json;
		}
		if(o instanceof Double)
		{
			json += o + ",";
			return json;
		}
		if(o instanceof Boolean)
		{
			json += o + ",";
			return json;
		}
		if(o instanceof String)
		{
			json += "\"" + o + "\",";
			return json;
		}
		if(o instanceof Date)
		{
			json += "\"" + o + "\",";
			return json;
		}
		if(o instanceof Byte)
		{
			json += Integer.valueOf(String.valueOf(o)) + ",";
			return json;
		}
		if(o instanceof TypedObject)
		{
			json += "{";
			for(String s : ((TypedObject)o).keySet())
			{
				json = addObject(json, ((TypedObject)o), s);
			}
			if(((TypedObject)o).size() > 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "},";
			return json;
		}
		if(o instanceof byte[])
		{
			byte[] array = (byte[]) o;
			json += "[";
			for(Object j : array)
			{
				json = addArrayObject(json, j);
			}
			if(array.length != 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "],";
			return json;
		}
		if(o instanceof Object[])
		{
			Object[] array = (Object[])o;
			json += "[";
			for(Object j : array)
			{
				json = addArrayObject(json, j);
			}
			json += "],";
			return json;
		}
		
		return json;
	}
	
	private static boolean isArray(TypedObject data, String x)
	{
		try{
			if(data.getArray(x) == null)
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
}
