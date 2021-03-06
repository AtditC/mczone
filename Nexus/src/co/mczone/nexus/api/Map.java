package co.mczone.nexus.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import co.mczone.api.ConfigAPI;
import co.mczone.api.modules.Coordinate;
import co.mczone.api.players.Gamer;
import co.mczone.nexus.Nexus;
import co.mczone.nexus.enums.TeamColor;
import co.mczone.util.Chat;

import lombok.Getter;

public class Map {
	
	@Getter static List<Map> list = new ArrayList<Map>();
	
	@Getter String title;
	@Getter String worldName;
	@Getter List<String> creators = new ArrayList<String>();
	@Getter String version;
	
	@Getter Coordinate spawn;
	@Getter List<Team> teams;
	@Getter ConfigAPI config;
	
	@Getter int duration;
	
	public Map(String title, List<String> creators, String version, String worldName, int duration, ConfigAPI config, Coordinate spawn, List<Team> teams) {
		this.title = title;
		this.creators = creators;
		this.version = version;
		this.worldName = worldName;
		this.spawn = spawn;
		this.duration = duration;
		this.config = config;
		this.teams = teams;
		list.add(this);
	}
	
	public Team getTeam(TeamColor color) {
		for (Team team : teams)
			if (team.getColor() == color)
				return team;
		return null;
	}
	
	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}
	
	public void loadMap() {
		Chat.log(Prefix.LOG_WORLDS + "Generating world: " + worldName + "...");
		
		WorldCreator wc = new WorldCreator(worldName);
		wc.createWorld();
		
		getWorld().setAutoSave(false);
		getWorld().setKeepSpawnInMemory(false);
		getWorld().setFullTime(6000);
		
		for (Entity e : getWorld().getEntities())
			if (e instanceof Player == false)
				e.remove();
	}
	
	public void loadMatch() {
		if (getWorld() == null)
			loadMap();
				
		for (Team team : teams) {
			// Register teams
			org.bukkit.scoreboard.Team t = Nexus.getRotary().getScoreboard().registerNewTeam(team.getColor().name().toLowerCase());
			t.setAllowFriendlyFire(false);
			t.setPrefix(team.getColor().getChatColor() + "");
			
			team.setTeam(t);
			team.getMembers().clear();
		}
	}
	
	public void unloadWorld() {
		Chat.log(Prefix.LOG_WORLDS + "Unloading world: " + worldName + "...");
		
		boolean unloaded = Bukkit.unloadWorld(worldName, false);
		
		if (unloaded)
			Chat.log(Prefix.LOG_WORLDS + "Successfully unloaded world!");
		else
			Chat.log(Prefix.LOG_WORLDS + "Failed to unload world");
	}
	
	public Location getSpawnLocation() {
		Location spawn = this.spawn.getLocation(getWorld());
		return spawn;
	}
	
	public Team getTeam(Gamer g) {
		for (Team t : teams)
			if (t.getMembers().contains(g))
				return t;
		return null;
	}

	public static Map get(String name) {
		for (Map map : list)
			if (map.getWorldName().equalsIgnoreCase(name))
				return map;
		return null;
	}
	
}
