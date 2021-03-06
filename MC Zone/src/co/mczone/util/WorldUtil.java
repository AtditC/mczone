package co.mczone.util;

import net.minecraft.server.v1_5_R3.Packet130UpdateSign;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class WorldUtil {
	public static void sendSignChange(Player p, Sign sign, String[] arr) {
		Packet130UpdateSign packet = new Packet130UpdateSign(sign.getX(), sign.getY(), sign.getZ(), arr);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
	
	public static boolean sameBlock(Block a, Block b) {
		if (a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ())
			return true;
		return false;
	}
}