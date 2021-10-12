package user.uziza.initscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import sun.security.krb5.Config;

public class Commands implements CommandExecutor, Listener {

	List<String> CMDS = new ArrayList<String>(Arrays.asList("int", "init", "initiative"));
	Set<Scoreboard> init_scoreboard = new HashSet<>();
	Map<String, Score> init_score = new HashMap<>();
	
	Plugin plugin;
	ScoreboardManager scoreboardmanager;
	Scoreboard scoreboard;
	Objective objective;
	
	public Commands(ScoreboardManager s, Plugin p) {
		plugin = p;
		scoreboardmanager = s;
		scoreboard = scoreboardmanager.getNewScoreboard();
		objective = scoreboard.registerNewObjective("RTD", "");
		
		if (plugin.getConfig().getKeys(false).size() > 0) {
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName("Initiative");
			for (String key : plugin.getConfig().getConfigurationSection("People").getKeys(false)) {
				Score score = objective.getScore(key);
				score.setScore(plugin.getConfig().getInt("People."+key)); // score
				init_score.put(key, score);
			}
			
			init_scoreboard.add(scoreboard);
		}
		for (Player players : Bukkit.getServer().getOnlinePlayers()) players.setScoreboard(scoreboard);
		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (init_scoreboard.contains(scoreboard)) {
			event.getPlayer().setScoreboard(scoreboard);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase(CMDS.get(0)) || command.getName().equalsIgnoreCase(CMDS.get(1))
				|| command.getName().equalsIgnoreCase(CMDS.get(2))) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.getGameMode() == GameMode.CREATIVE) {
					if (args.length == 1) {
						if (args[0].matches("create")) {
							if (!init_scoreboard.contains(scoreboard)) {
								
								
								objective.setDisplaySlot(DisplaySlot.SIDEBAR);
								objective.setDisplayName("Initiative");

								Score none = objective.getScore("None");
								none.setScore(-1);

								init_scoreboard.add(scoreboard);
								
							} else {
								player.sendMessage("There's a scoreboard already");
							}
						} 
						else if (args[0].matches("add")) {
							if (init_scoreboard.contains(scoreboard)) {
								if (scoreboard.getScores("None") != null) {
									scoreboard.resetScores("None");
								}
								
								int default_roll = (int) Math.floor(Math.random() * (20 - 1 + 1) + 1);

								Score score = objective.getScore(player.getName());
								score.setScore(default_roll); // score
								init_score.put(player.getName(), score);
								
								for (Entry<String, Score> entry : init_score.entrySet()) {
									String key = entry.getKey();
								    Integer value = entry.getValue().getScore();
								    
								    plugin.getConfig().set("People." + key, value);
								}
								
							}
							else {
								player.sendMessage("No scoreboard");
							}
						}
						else if (args[0].matches("clear")) {
							if (init_scoreboard.contains(scoreboard)) {
								for (String players : init_score.keySet()) {
									scoreboard.resetScores(players);
								}
								scoreboard.clearSlot(DisplaySlot.SIDEBAR);
								init_score.clear();
								init_scoreboard.remove(scoreboard);
								plugin.getConfig().set("People", null);
							}
							else {
								player.sendMessage("No scoreboard");
							}
						} 
						else if (args[0].matches("delete")) {
							if (init_scoreboard.contains(scoreboard)) {
								if (init_score.containsKey(player.getName())) {
									init_score.remove(player.getName());
									scoreboard.resetScores(player.getName());
									if (init_score.isEmpty()) {
										plugin.getConfig().set("People", null);
										Score none = objective.getScore("None");
										none.setScore(-1);
									}
									else {
										for (Entry<String, Score> entry : init_score.entrySet()) {
											String key = entry.getKey();
										    Integer value = entry.getValue().getScore();
										    
										    System.out.println("People."+key);
										    
										    plugin.getConfig().set("People." + key, value);
										}
										
									}
									
								}
								else {
									player.sendMessage("Can't delete");
								}
							}
							else {
								player.sendMessage("No scoreboard");
							}
						}
					}
					else if (args.length == 2) {
							if (args[0].matches("delete")) {
								if (init_scoreboard.contains(scoreboard)) {
									if (init_score.containsKey(args[1])) {
										init_score.remove(args[1]);
										scoreboard.resetScores(args[1]);
										if (init_score.isEmpty()) {
											plugin.getConfig().set("People", null);
											Score none = objective.getScore("None");
											none.setScore(-1);
										}
										else {
											for (Entry<String, Score> entry : init_score.entrySet()) {
												String key = entry.getKey();
											    Integer value = entry.getValue().getScore();
											    
											    plugin.getConfig().set("People." + key, value);
											}
											
										}
									
									}
									else {
										player.sendMessage("Invalid entry");
									}
									
									
								}
								else {
									player.sendMessage("No scoreboard");
								}
							}
							if (args[0].matches("add")) {
								if (init_scoreboard.contains(scoreboard)) {
									try {
										String RTD = args[1];
										ArrayList<String> listOfMaths = new ArrayList<String>();
	
										String sorted = args[1].replaceAll("\\-", "+-");
	
										String sort1[] = sorted.split("[+]");
	
										int aggregate1 = 0;
										int aggregate2 = 0;
	
										for (int i = 0; i < sort1.length; i++) {
											if (sort1[i].matches("[0-9]+d[0-9]+|\\-[0-9]+d[0-9]+")) {
												ArrayList<String> listOfRolls = new ArrayList<String>();
												int num = Integer.parseInt(sort1[i].replace("-", "").split("d")[0]);
												int size = Integer.parseInt(sort1[i].split("d")[1]);
	
												if (num > 100 && size > 100) {
													throw new ArithmeticException("Too much and too big");
												} else {
													if (num > 100) {
														throw new ArithmeticException("Too much");
													}
													if (size > 100) {
														throw new ArithmeticException("Too big");
													}
												}
	
												int roll = 0;
	
												if (sort1[i].matches("\\-[0-9]+d[0-9]+")) {
													for (int ii = 0; ii < num; ii++) {
														roll = (int) Math.floor(Math.random() * (size - 1 + 1) + 1);
														listOfRolls.add(String.valueOf(roll));
														aggregate2 -= roll;
													}
													if (num == 1) {
														listOfMaths.add("-" + listOfRolls.toString().replace(" ", "")
																.replace(",", "+").replace("[", "").replace("]", ""));
													} else {
														listOfMaths.add("-" + listOfRolls.toString().replace(" ", "")
																.replace(",", "+").replace("[", "(").replace("]", ")"));
													}
												} else {
													for (int ii = 0; ii < num; ii++) {
														roll = (int) Math.floor(Math.random() * (size - 1 + 1) + 1);
														listOfRolls.add(String.valueOf(roll));
														aggregate2 += roll;
													}
													if (num == 1) {
														listOfMaths.add("+" + listOfRolls.toString().replace(" ", "")
																.replace(",", "+").replace("[", "").replace("]", ""));
													} else {
														listOfMaths.add("+" + listOfRolls.toString().replace(" ", "")
																.replace(",", "+").replace("[", "(").replace("]", ")"));
													}
												}
											} else {
												listOfMaths.add("+" + sort1[i]);
												aggregate1 += Integer.parseInt(sort1[i]);
											}
										}
										if (scoreboard.getScores("None") != null) {
											scoreboard.resetScores("None");
										}
										int default_roll = (int) Math.floor(Math.random() * (20 - 1 + 1) + 1);
	
										Score score = objective.getScore(player.getName());
										score.setScore(default_roll + aggregate1 + aggregate2); // score
										init_score.put(player.getName(), score);

										for (Entry<String, Score> entry : init_score.entrySet()) {
											String key = entry.getKey();
										    Integer value = entry.getValue().getScore();
										    
										    plugin.getConfig().set("People." + key, value);
										}
										
									} catch (ArithmeticException a) {
										sender.sendMessage(String.valueOf(a.getMessage()));
									} catch (Exception e) {
										return false;
								}
							} 
							else {
								player.sendMessage("No scoreboard");
							}
						}
						
					} 
					else if (args.length == 3) {
						if (args[0].matches("add")) {
							if (init_scoreboard.contains(scoreboard)) {
								try {
									String RTD = args[1];
									ArrayList<String> listOfMaths = new ArrayList<String>();
	
									String sorted = args[1].replaceAll("\\-", "+-");
	
									String sort1[] = sorted.split("[+]");
	
									int aggregate1 = 0;
									int aggregate2 = 0;
	
									for (int i = 0; i < sort1.length; i++) {
										if (sort1[i].matches("[0-9]+d[0-9]+|\\-[0-9]+d[0-9]+")) {
											ArrayList<String> listOfRolls = new ArrayList<String>();
											int num = Integer.parseInt(sort1[i].replace("-", "").split("d")[0]);
											int size = Integer.parseInt(sort1[i].split("d")[1]);
	
											if (num > 100 && size > 100) {
												throw new ArithmeticException("Too much and too big");
											} else {
												if (num > 100) {
													throw new ArithmeticException("Too much");
												}
												if (size > 100) {
													throw new ArithmeticException("Too big");
												}
											}
	
											int roll = 0;
	
											if (sort1[i].matches("\\-[0-9]+d[0-9]+")) {
												for (int ii = 0; ii < num; ii++) {
													roll = (int) Math.floor(Math.random() * (size - 1 + 1) + 1);
													listOfRolls.add(String.valueOf(roll));
													aggregate2 -= roll;
												}
												if (num == 1) {
													listOfMaths.add("-" + listOfRolls.toString().replace(" ", "")
															.replace(",", "+").replace("[", "").replace("]", ""));
												} else {
													listOfMaths.add("-" + listOfRolls.toString().replace(" ", "")
															.replace(",", "+").replace("[", "(").replace("]", ")"));
												}
											} else {
												for (int ii = 0; ii < num; ii++) {
													roll = (int) Math.floor(Math.random() * (size - 1 + 1) + 1);
													listOfRolls.add(String.valueOf(roll));
													aggregate2 += roll;
												}
												if (num == 1) {
													listOfMaths.add("+" + listOfRolls.toString().replace(" ", "")
															.replace(",", "+").replace("[", "").replace("]", ""));
												} else {
													listOfMaths.add("+" + listOfRolls.toString().replace(" ", "")
															.replace(",", "+").replace("[", "(").replace("]", ")"));
												}
											}
										} else {
											listOfMaths.add("+" + sort1[i]);
											aggregate1 += Integer.parseInt(sort1[i]);
										}
									}
									if (scoreboard.getScores("None") != null) {
										scoreboard.resetScores("None");
									}
									int default_roll = (int) Math.floor(Math.random() * (20 - 1 + 1) + 1);
	
									Score score = objective.getScore(args[2]);
									score.setScore(default_roll + aggregate1 + aggregate2); // score
									init_score.put(args[2], score);
									
									for (Entry<String, Score> entry : init_score.entrySet()) {
										String key = entry.getKey();
									    Integer value = entry.getValue().getScore();
									    
									    plugin.getConfig().set("People." + key, value);
									}
									
								} catch (ArithmeticException a) {
									sender.sendMessage(String.valueOf(a.getMessage()));
								} catch (Exception e) {
									return false;
								}
							} 
							else {
								player.sendMessage("No scoreboard");
							}
						}
					}
					plugin.saveConfig();
					for (Player players : Bukkit.getServer().getOnlinePlayers()) players.setScoreboard(scoreboard);
				}
				else {
					player.sendMessage("Not in creative mode");
				}
				return true;	
			}
			
		}
		return false;
	}

}
