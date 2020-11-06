package net.minemora.entitytrackerfixer.v1_16_R3.tasks;

import net.minecraft.server.v1_16_R3.ChunkProviderServer;
import net.minecraft.server.v1_16_R3.WorldServer;
import net.minemora.entitytrackerfixer.config.ConfigMain;
import net.minemora.entitytrackerfixer.v1_16_R3.NMSEntityTracker;
import net.minemora.entitytrackerfixer.v1_16_R3.entityTick.EntityTickManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class CheckTask extends BukkitRunnable {

	@Override
	public void run() {
		if(UntrackerTask.isRunning()) {
			return;
		}
		if(ConfigMain.isEnableOnAllWorlds()) {
			for(World world : Bukkit.getWorlds()) {
				checkWorld(world.getName());
			}
		}
		else {
			for(String worldName : ConfigMain.getWorlds()) {
				if(Bukkit.getWorld(worldName) == null) {
					continue;
				}
				checkWorld(worldName);
			}
		}
	}
	
	public void checkWorld(String worldName) {
		WorldServer ws = ((CraftWorld)Bukkit.getWorld(worldName)).getHandle();
		ChunkProviderServer cps = ws.getChunkProvider();
		
		Set<net.minecraft.server.v1_16_R3.Entity> trackAgain = new HashSet<>();
		
		int d = ConfigMain.getTrackingRange();
		for(Player player : Bukkit.getWorld(worldName).getPlayers()) {
			for(Entity ent : player.getNearbyEntities(d, d, d)) {
				net.minecraft.server.v1_16_R3.Entity nms = ((CraftEntity)ent).getHandle();
				if(cps.playerChunkMap.trackedEntities.containsKey(nms.getId()) || !nms.valid) {
					continue;
				}
				trackAgain.add(nms);
			}
		}
		NMSEntityTracker.trackEntities(cps, trackAgain);
		if(ConfigMain.isDisableTickUntracked()) {
			EntityTickManager.getInstance().enableTicking(trackAgain);
		}
	}

}