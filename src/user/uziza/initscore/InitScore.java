package user.uziza.initscore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

public class InitScore extends JavaPlugin {
	 
	public void onEnable() {
		ScoreboardManager scoreboardmanager = Bukkit.getScoreboardManager();
		Commands CMDS = new Commands(scoreboardmanager, this);
		this.getCommand("initiative").setExecutor(CMDS);
		getServer().getPluginManager().registerEvents(CMDS, this);
	}
	
	public void onDisable() {
		
	}
}
