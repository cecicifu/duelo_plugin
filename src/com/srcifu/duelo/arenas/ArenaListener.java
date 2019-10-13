package com.srcifu.duelo.arenas;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.srcifu.duelo.duelos.Duelo;

public class ArenaListener implements Listener {
	
	private Duelo main;
	
	public ArenaListener(Duelo duelo) { this.main = duelo; }

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player)
		{
			Player victima = (Player) event.getEntity();
			Player atacante = (Player) event.getDamager();
			
			Arena victimaArena = main.getArenaManager().getArenaByPlayer(victima);
			
			if(victimaArena == null || victimaArena.getPlayers().contains(atacante))
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onKill(PlayerDeathEvent event)
	{
		if(event.getEntity().getKiller() instanceof Player)
		{
			Player victima = event.getEntity();
			Player asesino = (Player) victima.getKiller();
			
			Arena arena = main.getArenaManager().getArenaByPlayer(asesino);
			
			if(arena != null)
				arena.eliminate(victima);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		Player leaver = event.getPlayer();
		Arena arena = main.getArenaManager().getArenaByPlayer(leaver);
		
		if(arena != null)
			arena.eliminate(leaver);
	}
}
