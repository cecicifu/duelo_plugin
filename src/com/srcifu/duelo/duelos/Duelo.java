package com.srcifu.duelo.duelos;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.srcifu.duelo.arenas.Arena;
import com.srcifu.duelo.arenas.ArenaListener;
import com.srcifu.duelo.arenas.ArenaManager;

public class Duelo extends JavaPlugin
{
	
	private Map<Player, Player> players = new HashMap<>();
	private ArenaManager arenaManager = new ArenaManager();
	private File arenaFile;
	private YamlConfiguration arenaConfig;
	
	@Override
	public void onEnable()
	{
		System.out.println("Funcionando!");
		getCommand("duelo").setExecutor(this);
		getServer().getPluginManager().registerEvents(new ArenaListener(this), this);
		
		loadArenaConfig();
			
		// Cargar las arenas del archivo de configuracion
		ConfigurationSection arenaSection = arenaConfig.getConfigurationSection("arenas");
		
		for(String string : arenaSection.getKeys(false))
		{
			String loc1 = arenaSection.getString(string + ".loc1");
			String loc2 = arenaSection.getString(string + ".loc2");
			
			Arena arena = new Arena(parseStringToLoc(loc1), parseStringToLoc(loc2));
			arenaManager.addArena(arena);
		}
	}

	@Override
	public void onDisable()
	{
		System.out.println("Desactivado :(");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		// -> /duelo ayuda
		// -> /duelo <jugador>
		// -> /duelo aceptar/rechazar
		
		if (label.equalsIgnoreCase("duelo") && sender instanceof Player) 
		{
			Player player = (Player) sender;
			
			// -> /duelo
			if(args.length == 0)
			{
				player.sendMessage("§c/duelo <jugador>");
				player.sendMessage("§c/duelo <aceptar/rechazar>");
				return true;
			}
			
			// -> /duelo <jugador>
			if (args.length >= 1)
			{
				
				String targetName = args[0];
				
				if (args[0].equalsIgnoreCase("aceptar"))
				{
					
					if(players.containsKey(player))
					{
						player.sendMessage("Ok, que comience el duelo!");
						
						Player firstPlayerTarget = players.get(player);
						firstPlayerTarget.sendMessage("Ok, que comience el duelo!");
						
						// Teletransporte a la arena
						arenaManager.joinArena(player, firstPlayerTarget);
						
						players.remove(player);
					}
					
				} 
				
				else if(args[0].equalsIgnoreCase("rechazar"))
				{
					if(players.containsKey(player))
					{
						player.sendMessage("Has rechazado el duelo");
						
						Player firstPlayerTarget = players.get(player);
						firstPlayerTarget.sendMessage("El jugador §e" + player.getName() + "§r ha rechazado el duelo");
						
						players.remove(player);
					}
				}
				
				else if(args[0].equalsIgnoreCase("arena"))
				{
					if(args.length < 3)
					{
						player.sendMessage("§cEscribe el comando /duelo arena <x,y,z> <x,y,z> para crear una arena");
						return true;
					}
					
					Location loc1 = parseStringToLoc(args[1]);
					Location loc2 = parseStringToLoc(args[2]);
					
					Arena arena = new Arena(loc1, loc2);
					String arenaName = "arena-" + new Random().nextInt(9999);
					
					// Guardar arena en configuracion
					arenaConfig.set("arenas." + arenaName + ".loc1", args[1]);
					arenaConfig.set("arenas." + arenaName + ".loc2", args[2]);

					saveArenaConfig();
					
					arenaManager.addArena(arena);
					
					player.sendMessage("Acabas de crear la arena " + arenaName + " !");
				}
				
				else if(Bukkit.getPlayer(targetName) != null)
				{
					Player target = Bukkit.getPlayer(targetName);
					
					if(players.containsKey(target))
					{
						player.sendMessage("§cTienes una solicitud de duelo pendiente");
						return true;
					}
					
					players.put(target, player);
					player.sendMessage("Has retado al jugador §e" + targetName + "§r a un duelo!");
					target.sendMessage("§e" + player.getName() + "§r te ha retado a un duelo! Usa §e/duelo <aceptar/rechazar>");
				}
				
				else
				{
					player.sendMessage("§cEl jugador " + targetName + " no existe o está desconectado");
				}
				
				return true;
			}
			
		}
		
		return false;
	}
	
	private void saveArenaConfig() {
		// idk what to do here
	}

	private void loadArenaConfig() {
		// Creacion del fichero arenas.yml
		if(!getDataFolder().exists())
		{
			getDataFolder().mkdir();
		}
		
		arenaFile = new File(getDataFolder() + File.separator + "arenas.yml");
		
		if(!arenaFile.exists())
		{
			try {
				arenaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Cargar el archivo de la configuracion arenas.yml
		arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
	}

	public ArenaManager getArenaManager()
	{
		return arenaManager;
	}
	
	public Location parseStringToLoc(String string)
	{
		String[] parsedLoc = string.split(",");
		double x = Double.valueOf(parsedLoc[0]);
		double y = Double.valueOf(parsedLoc[1]);
		double z = Double.valueOf(parsedLoc[2]);
		
		return new Location(Bukkit.getWorld("world"), x, y, z);
	}
	
	public String unparseLocToString(Location loc)
	{
		return loc.getX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
	}
}
