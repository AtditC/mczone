package co.mczone.ghost;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import co.mczone.api.ConfigAPI;
import co.mczone.api.players.Gamer;
import co.mczone.api.players.GamerRunnable;
import co.mczone.api.server.GameType;
import co.mczone.api.server.Hive;
import co.mczone.ghost.api.Kit;
import co.mczone.ghost.api.Lobby;
import co.mczone.ghost.api.Arena;
import co.mczone.ghost.api.Map;
import co.mczone.ghost.cmds.ArenasCmd;
import co.mczone.ghost.cmds.GhostCmd;
import co.mczone.ghost.cmds.HelpCmd;
import co.mczone.ghost.cmds.KitCmd;
import co.mczone.ghost.cmds.LeaveCmd;
import co.mczone.ghost.events.*;
import co.mczone.util.Chat;
import co.mczone.util.ItemUtil;

public class Ghost extends JavaPlugin {
	@Getter static Ghost instance;
	@Getter static ConfigAPI conf;
	@Getter static Lobby lobby;
	
	@Getter static ConfigAPI kitConf;
	
	public void onEnable() {
		Hive.getInstance().setType(GameType.GHOST);
		
		instance = this;
		conf = new ConfigAPI(this);
		kitConf = new ConfigAPI("kits.yml", this);
		
		lobby = new Lobby(conf.getLocation("lobby"));

		new GeneralEvents();
		new ConnectEvents();
		new SignEvents();
		new GameEvents();

    	Hive.getInstance().registerCommand(Ghost.getInstance(), "arenas", new ArenasCmd());
    	Hive.getInstance().registerCommand(Ghost.getInstance(), "leave", new LeaveCmd());
    	Hive.getInstance().registerCommand(Ghost.getInstance(), "help", new HelpCmd());
    	Hive.getInstance().registerCommand(Ghost.getInstance(), "ghost", new GhostCmd());
    	Hive.getInstance().registerCommand(Ghost.getInstance(), "leave", new LeaveCmd());
    	Hive.getInstance().registerCommand(Ghost.getInstance(), "kit", new KitCmd());
		
		for (String name : kitConf.getKeys(false)) {
			ConfigurationSection kit = kitConf.getConfigurationSection(name);
			List<ItemStack> items = new ArrayList<ItemStack>();
			
			for (String raw : kit.getStringList("items"))
				items.add(ItemUtil.deserializeItem(raw));
			
			if (!kit.contains("effects"))
				new Kit(name, items);
			else {
				List<PotionEffect> effects = new ArrayList<PotionEffect>();
				for (String raw : kit.getStringList("effects"))
					effects.add(ItemUtil.deserializePotionEffect(raw));
				
				new Kit(name, items, effects);
			}
		}
		Chat.log("Loaded " + Kit.getList().size() + " kits");
		
		
		for (String name : conf.getConfigurationSection("maps").getKeys(false)) {
			String base = "maps." + name + ".";
			String title = conf.getString(base + "title", "NULL TITLE");
			String worldName = conf.getString(base + "world");
			
			new WorldCreator(worldName).createWorld();
			
			Location spawn = Bukkit.getWorld(worldName).getSpawnLocation();
			if (conf.contains(base + "spawn"))
				spawn = conf.getLocation(base + "spawn", worldName);
			
			Location red = conf.getLocation(base + "red", worldName);
			Location blue = conf.getLocation(base + "blue", worldName);
			
			new Map(name, title, worldName, spawn, red, blue);
		}
		
		for (String arenaName : conf.getConfigurationSection("arenas").getKeys(false)) {
			String base = "arenas." + arenaName + ".";
			int id = conf.getInt(base + "id", 0);

			String worldName = conf.getString(base + "world", null);
			
			new WorldCreator(worldName).createWorld();
			
			Block sign = conf.getBlock(base + "sign");
			
			List<Map> maps = new ArrayList<Map>();
			for (String s : conf.getStringList(base + "maps")) {
				maps.add(Map.get(s));
			}
			
			
			new Arena(arenaName, worldName, id, sign, maps);
		}
		
		Gamer.addFunction("load-kits", new GamerRunnable() {
			@Override
			public void run() {
				List<Kit> kits = new ArrayList<Kit>();
				ResultSet r = Hive.getInstance().getDatabase().query("SELECT * FROM ghost_donations WHERE username='" + gamer.getName()  + "'");
				try {
					while (r.next()) {
						Kit kit = Kit.get(r.getString("kit"));
						if (kit == null)
							continue;
						
						kits.add(kit);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				gamer.setVariable("ghost-kits", kits);
			}
		});
		
		new BukkitRunnable() {

			@Override
			public void run() {
				Chat.server("&4[Ghost] &cServer shutting down in 30 seconds for reboot!");
				
				new BukkitRunnable() {

					@Override
					public void run() {
						Chat.server("&4[Ghost] &cServer rebooting, please rejoin!");
						Bukkit.shutdown();
					}
					
				}.runTaskLater(Ghost.getInstance(), 20 * 30);
			}
			
		}.runTaskLater(this, 20 * 60 * 60 * 6);
	}
	
	public void onDisable() {
		Chat.log("Cancelling arena schedules...");
		for (Arena a : Arena.getList())
			a.getSchedule().cancel();
	}
}
