package co.mczone.ghost.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import co.mczone.api.players.Gamer;
import co.mczone.api.players.Permissible;
import co.mczone.api.players.RankType;
import co.mczone.ghost.Ghost;
import co.mczone.util.Chat;

public class Kit implements Permissible {
	@Getter static List<Kit> list = new ArrayList<Kit>();
	@Getter String name;
	@Getter List<ItemStack> items = new ArrayList<ItemStack>();
	@Getter List<PotionEffect> effects = new ArrayList<PotionEffect>();
	@Getter boolean free;
	
	public Kit(String name, List<ItemStack> items) {
		this.name = name;
		this.items = items;
		list.add(this);
	}
	
	public Kit(String name, List<ItemStack> items, List<PotionEffect> effects) {
		this.name = name;
		this.items = items;
		this.effects = effects;
		list.add(this);
	}
	
	public String getTitle() {
		return Chat.capitalize(name.toLowerCase());
	}
	
	public static void giveKit(final Player p) {
		Gamer g = Gamer.get(p.getName());
		g.clearInventory();
		Kit k = (Kit) g.getVariable("kit");
		
		if (k == null) {
			g.setVariable("kit", Kit.get("bruiser"));
			k = Kit.get("bruiser");
		}
		
		List<ItemStack> items = k.getItems();
		for (ItemStack i : items)
			g.giveItem(i);
		
		g.removePotionEffects();
		
		for (PotionEffect effect : k.getEffects())
			g.addPotionEffect(effect, false);
	}
	
	public static Kit get(String name) {
		for (Kit k : list) {
			if (k.getName().equalsIgnoreCase(name))
				return k;
		}
		return null;
	}

	@Override
	public boolean hasPermission(Gamer g) {
		@SuppressWarnings("unchecked")
		List<Kit> purchases = (List<Kit>) g.getVariable("ghost-kits");
		
		RankType r = g.getRank().getType();
		if (r.getLevel() >= RankType.TITAN.getLevel()) {
			return true;
		}
		else if (r.getLevel() >= RankType.ELITE.getLevel()) {
			if (Ghost.getConf().getStringList("elite-kits").contains(name.toLowerCase()))
				return true;
		}
		else if (r.getLevel() >= RankType.VIP.getLevel()) {
			if (Ghost.getConf().getStringList("vip-kits").contains(name.toLowerCase()))
				return true;
		}
		else if (purchases != null && purchases.contains(this))
			return true;
		else {
			if (name.equals("archer"))
				return true;
			if (name.equals("bruiser"))
				return true;
		}
		
		return false;
	}
}
