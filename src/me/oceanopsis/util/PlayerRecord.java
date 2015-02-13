
package me.oceanopsis.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;

public class PlayerRecord implements Comparable<PlayerRecord>
{
	private String						playerName;
	
	private double						kd;
	
	public static List<PlayerRecord>	rankingList;
	
	public PlayerRecord(String playerName, double kd)
	{
		this.playerName = playerName;
		this.kd = kd;
		PlayerRecord.addPlayer(this);
	}
	
	private String getName()
	{
		return playerName;
	}
	
	private double getKd()
	{
		return kd;
	}
	
	private static void addPlayer(PlayerRecord playerRecord)
	{
		if(rankingList == null)
		{
			rankingList = new ArrayList<PlayerRecord>();
			rankingList.add(playerRecord);
		}
		else if(!rankingList.contains(playerRecord))
		{
			rankingList.add(playerRecord);
		}
	}
	
	public static PlayerRecord getRecord(String playerName)
	{
		for(PlayerRecord p : rankingList)
		{
			if(p.getName().equals(playerName))
			{
				return p;
			}
		}
		return null;
	}
	
	public String toString()
	{
		return (ChatColor.YELLOW + "" + (rankingList.indexOf(this) + 1) + ". " + ChatColor.GOLD + playerName + ": " + ChatColor.GREEN + String.format("%.2f", kd));
	}
	
	public String getPlayerStats(String playerName)
	{
		PlayerRecord.sortRankings();
		return PlayerRecord.getRecord(playerName).toString();
	}
	
	public static String[] getTopRankings(int top)
	{
		PlayerRecord.sortRankings();
		String[] rankings = new String[top];
		for(int i = 0; i < top; i++)
		{
			rankings[i] = rankingList.get(i).toString();
		}
		return rankings;
	}
	
	public static String[] getTopRankings()
	{
		PlayerRecord.sortRankings();
		int totalRecords = rankingList.size();
		String[] rankings = new String[totalRecords];
		for(int i = 0; i < totalRecords; i++)
		{
			rankings[i] = rankingList.get(i).toString();
		}
		return rankings;
	}
	
	private static void sortRankings()
	{
		Collections.sort(rankingList);
	}
	
	public int compareTo(PlayerRecord record)
	{
		return (new Double(record.getKd())).compareTo(new Double(this.getKd()));
	}
}