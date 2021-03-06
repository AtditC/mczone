package co.mczone.events;

import java.util.Date;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import co.mczone.MCZone;
import co.mczone.api.infractions.Ban;
import co.mczone.api.infractions.Infraction;
import co.mczone.api.infractions.Tempban;
import co.mczone.api.players.Gamer;
import co.mczone.api.players.Rank;
import co.mczone.api.players.RankType;
import co.mczone.api.server.Hive;
import co.mczone.util.Chat;

public class ConnectEvents implements Listener {
	public ConnectEvents() {
		MCZone.getInstance().getServer().getPluginManager().registerEvents(this, MCZone.getInstance());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		Gamer g = Gamer.get(p.getName());
		
		if (g == null)
			g = new Gamer(p.getName());

		// Import infractions
		g.getInfractions().clear();
		if (Infraction.getList().values().contains(p.getName())) {
			for (Entry<Infraction, String> i : Infraction.getList().entrySet()) {
				if (i.getValue().equals(p.getName()))
					g.getInfractions().add(i.getKey());
			}
		}

		// Check bans/tempbans
		for (Infraction i : g.getInfractions()) {
			String msg = "";
			if (i instanceof Ban) {
				msg = i.getKickMessage();
			} 
			else if (i instanceof Tempban) {
				Date now = Hive.getInstance().getServerTime();
				if (now != null && now.before(((Tempban) i).getExpires())) {
					msg = i.getKickMessage();
				}
			}
			if (msg != "") {
				event.disallow(Result.KICK_OTHER, msg);
				return;
			}
		}

		// Import current ranking
		if (Rank.getRanks().containsKey(p.getName()))
			g.setRank(Rank.getRanks().get(p.getName()));
		else
			g.setRank(new Rank(RankType.USER));
		
		RankType r = g.getRank().getType();

		// VIP and above join full servers
		if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
			if (r.getLevel() >= RankType.VIP.getLevel()) {
				event.allow();
			}
			else {
				event.disallow(Result.KICK_FULL, Chat.colors("&4[Full Server] &cGet VIP or above to join a full server.\n&7www.mczone.co/shop"));
				return;
			}
		}

		g.updateCredits();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Gamer g = Gamer.get(event.getPlayer());
		if (g.getRank().isCancelled()) {
			Date cancel = g.getRank().getExpireDate();
			Chat.player(g, "&7Your &8" + g.getRank().getType().getTitle()	+ " &7will expire on &8" + Infraction.human.format(cancel));
		}
	}
}
