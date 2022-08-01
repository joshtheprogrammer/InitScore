package user.uziza.initscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

public class Commands implements CommandExecutor, Listener {

	List<String> CMDS = new ArrayList<String>(Arrays.asList("int", "init", "initiative"));
	Set<Scoreboard> init_scoreboard = new HashSet<>();
	Map<String, Score> init_score = new LinkedHashMap<>();
	Set<String> init_score_turn = new HashSet<>();
	Set<String> init_score_filter = new HashSet<>();

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
			
			
			if (plugin.getConfig().get("Filter") != null) {
				String[] filter = plugin.getConfig().get("Filter").toString().split(", ");
				for (String f : filter) {
					init_score_filter.add(f);
				}
			}
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			if (plugin.getConfig().get("Title") != null) {
				String title = plugin.getConfig().get("Title").toString();
				objective.setDisplayName("Initiative " + "(" + title + ")");
			}
			else {
				objective.setDisplayName("Initiative");
			}
			
			if (plugin.getConfig().getConfigurationSection("People") != null) {
				String turn;
				if (plugin.getConfig().get("Turn") != null) {
					turn = plugin.getConfig().get("Turn").toString();
				}
				else {
					turn = plugin.getConfig().getConfigurationSection("People").getKeys(false).iterator().next();
					plugin.getConfig().set("Turn", turn);
					plugin.saveConfig();
				}
				for (String key : plugin.getConfig().getConfigurationSection("People").getKeys(false)) {
					String name = "[ ] " + key;
					if (key.contentEquals(turn)) {
						name = "[X] " + key;
						init_score_turn.add(name.replace("[X] ", ""));
					}
					Score score = objective.getScore(name);
					score.setScore(plugin.getConfig().getInt("People." + key)); // score
	
					init_score.put(key, score);
				}
			}
			else {
				Score score = objective.getScore("None");
				score.setScore(-1);
			}

			init_scoreboard.add(scoreboard);
		}
		for (Player players : Bukkit.getServer().getOnlinePlayers())
			players.setScoreboard(scoreboard);

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
				if (args.length == 1) {
					if (args[0].matches("create")) {
						if (player.getGameMode() == GameMode.CREATIVE || player.isOp()) {
							if (!init_scoreboard.contains(scoreboard)) {

								objective.setDisplaySlot(DisplaySlot.SIDEBAR);
								objective.setDisplayName("Initiative");

								Score none = objective.getScore("None");
								none.setScore(-1);

								init_scoreboard.add(scoreboard);

							} else {
								player.sendMessage("There's a scoreboard already");
							}
						} else {
							player.sendMessage("Not in creative mode");
						}
					} else if (args[0].matches("add")) {
						String name = "[ ] " + player.getName();
						if (init_scoreboard.contains(scoreboard)) {
							if (scoreboard.getObjective("RTD").getScore("None").getScore() == -1) {
								scoreboard.resetScores("None");
								init_score_filter.clear();
								if (init_score_turn.isEmpty()) {
									init_score_turn.add(player.getName());
									name = "[X] " + player.getName();
								}
							}

							int default_roll = (int) Math.floor(Math.random() * (20 - 1 + 1) + 1);

							if (init_score_turn.contains(player.getName())) {
								name = "[X] " + player.getName();
							}
							Score score = objective.getScore(name);
							score.setScore(default_roll); // score
							init_score.put(player.getName(), score);

							for (Entry<String, Score> entry : init_score.entrySet()) {
								String key = entry.getKey();
								Integer value = entry.getValue().getScore();

								plugin.getConfig().set("People." + key, value);
							}

						} else {
							player.sendMessage("No scoreboard");
						}
					} else if (args[0].matches("clear")) {
						if (player.getGameMode() == GameMode.CREATIVE || player.isOp()) {
							init_score_turn.clear();
							if (init_scoreboard.contains(scoreboard)) {
								for (String players : init_score.keySet()) {
									scoreboard.resetScores("[X] " + players);
									scoreboard.resetScores("[ ] " + players);
								}
								scoreboard.clearSlot(DisplaySlot.SIDEBAR);
								init_score.clear();
								init_scoreboard.remove(scoreboard);
								plugin.getConfig().set("Turn", null);
								plugin.getConfig().set("People", null);
								plugin.getConfig().set("Title", null);
							} else {
								player.sendMessage("No scoreboard");
							}
						} else {
							player.sendMessage("Not in creative mode");
						}
					} else if (args[0].matches("next")) {
						if (player.getGameMode() == GameMode.CREATIVE || player.isOp()) {
							if (init_score.size() > 1) {
								int pre_score = -1;
								String old_person = null;
								boolean added = false;
								int s = 0;
								for (String key : init_score_turn) {
									pre_score = init_score.get(key).getScore();
									old_person = key;
								}
								init_score_filter.add(old_person);
								int new_score = -1;
								String new_person = null;
								for (Entry<String, Score> entry : init_score.entrySet()) {
									if (!init_score_filter.contains(entry.getKey())) {
										if (entry.getValue().getScore() <= pre_score) {
											if (new_score <= entry.getValue().getScore()) {
												added = true;
												new_score = entry.getValue().getScore();
												new_person = entry.getKey();
											}
										}
									}
								}

								if (added == false) {
									init_score_filter.clear();
									for (Entry<String, Score> entry : init_score.entrySet()) {
										if (!init_score_filter.contains(entry.getKey())) {
											if (entry.getValue().getScore() > pre_score) {

												if (entry.getValue().getScore() > new_score) {
													new_score = entry.getValue().getScore();
													new_person = entry.getKey();

												}
											}
										}
									}
								}
								scoreboard.resetScores("[X] " + old_person);
								Score score = objective.getScore("[ ] " + old_person);
								score.setScore(pre_score);
								scoreboard.resetScores("[ ] " + new_person);
								Score score2 = objective.getScore("[X] " + new_person);
								score2.setScore(new_score);
								init_score.put(old_person, score);
								init_score.put(new_person, score2);
								init_score_turn.remove(old_person);
								init_score_turn.add(new_person);

								plugin.getConfig().set("Filter",
										init_score_filter.toString().replace("[", "").replace("]", ""));
								for (Entry<String, Score> entry : init_score.entrySet()) {
									String key = entry.getKey();
									Integer value = entry.getValue().getScore();

									plugin.getConfig().set("People." + key, value);
								}
							}
						} else {
							player.sendMessage("Not in creative mode");
						}
					} else if (args[0].matches("delete")) {
						if (player.getGameMode() == GameMode.CREATIVE || player.isOp()) {
							if (init_scoreboard.contains(scoreboard)) {
								if (init_score.containsKey(player.getName())) {
									init_score.remove(player.getName());
									scoreboard.resetScores("[X] " + player.getName());
									scoreboard.resetScores("[ ] " + player.getName());
									plugin.getConfig().set("People", null);
									plugin.getConfig().set("Turn", null);
									if (init_score.isEmpty()) {
										init_score_turn.clear();

										Score none = objective.getScore("None");
										none.setScore(-1);
									} else {
										if (init_score_turn.contains(player.getName())) {
											init_score_turn.clear();
											Map.Entry<String, Score> entry = init_score.entrySet().iterator().next();
											init_score_turn.add(entry.getKey());
											int old_score = entry.getValue().getScore();
											
											scoreboard.resetScores("[ ] " + entry.getKey());
											Score score = objective.getScore("[X] " + entry.getKey());
											score.setScore(old_score); // score
											init_score.put(entry.getKey(), score);
										}
										for (Entry<String, Score> entry : init_score.entrySet()) {
											String key = entry.getKey();
											Integer value = entry.getValue().getScore();

											plugin.getConfig().set("People." + key, value);
										}

									}

								} else {
									player.sendMessage("Can't delete");
								}
							} else {
								player.sendMessage("No scoreboard");
							}
						} else {
							player.sendMessage("Not in creative mode");
						}
					}
				} else if (args.length == 2) {
					if (args[0].matches("set")) {
						if (init_scoreboard.contains(scoreboard)) {
							try {
								String name = "[ ] " + player.getName();
								if (scoreboard.getObjective("RTD").getScore("None").getScore() == -1) {
									scoreboard.resetScores("None");
									init_score_filter.clear();
									if (init_score_turn.isEmpty()) {
										init_score_turn.add(player.getName());
										name = "[X] " + player.getName();
									}
								}
								if (init_score_turn.contains(player.getName())) {
									name = "[X] " + player.getName();
								}
								Score score = objective.getScore(name);
								score.setScore(Integer.valueOf(args[1])); // score
								init_score.put(player.getName(), score);

								for (Entry<String, Score> entry : init_score.entrySet()) {
									String key = entry.getKey();
									Integer value = entry.getValue().getScore();

									plugin.getConfig().set("People." + key, value);
								}
							}
							catch (Exception e) {
								
							}
						}
					}
					else if (args[0].matches("setto")) {
						if (Integer.valueOf(args[1]) != null && Integer.valueOf(args[1]) > 0) {
							if (init_scoreboard.contains(scoreboard)) {
								objective.setDisplayName("Initiative " + "(" +args[1] + ")");
								plugin.getConfig().set("Title", Integer.valueOf(args[1]));
							}
							else {
								player.sendMessage("No scoreboard");
							}
						}
						else {
							player.sendMessage("Not a valid Number");
						}
					}
					else if (args[0].matches("delete")) {
						if (player.getGameMode() == GameMode.CREATIVE || player.isOp()) {
							if (init_scoreboard.contains(scoreboard)) {
								if (init_score.containsKey(args[1])) {
									init_score.remove(args[1]);
									scoreboard.resetScores("[X] " + args[1]);
									scoreboard.resetScores("[ ] " + args[1]);
									plugin.getConfig().set("People", null);
									plugin.getConfig().set("Turn", null);
									if (init_score.isEmpty()) {
										init_score_turn.clear();

										Score none = objective.getScore("None");
										none.setScore(-1);
									} else {
										if (init_score_turn.contains(args[1])) {
											init_score_turn.clear();
											Map.Entry<String, Score> entry = init_score.entrySet().iterator().next();
											init_score_turn.add(entry.getKey());
											int old_score = entry.getValue().getScore();
											
											scoreboard.resetScores("[ ] " + entry.getKey());
											Score score = objective.getScore("[X] " + entry.getKey());
											score.setScore(old_score); // score
											init_score.put(entry.getKey(), score);
										}
										for (Entry<String, Score> entry : init_score.entrySet()) {
											String key = entry.getKey();
											Integer value = entry.getValue().getScore();

											plugin.getConfig().set("People." + key, value);
										}

									}

								} else {
									player.sendMessage("Invalid entry");
								}

							} else {
								player.sendMessage("No scoreboard");
							}
						} else {
							player.sendMessage("Not in creative mode");
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
								String name = "[ ] " + player.getName();
								if (scoreboard.getObjective("RTD").getScore("None").getScore() == -1) {
									scoreboard.resetScores("None");
									init_score_filter.clear();
									if (init_score_turn.isEmpty()) {
										init_score_turn.add(player.getName());
										name = "[X] " + player.getName();
									}
								}
								int default_roll = (int) Math.floor(Math.random() * (20 - 1 + 1) + 1);

								if (init_score_turn.contains(player.getName())) {
									name = "[X] " + player.getName();
								}
								Score score = objective.getScore(name);
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
						} else {
							player.sendMessage("No scoreboard");
						}
					}

				} else if (args.length == 3) {
					if (args[0].matches("set")) {
						if (args[2].toLowerCase().matches(player.getName().toLowerCase())||player.getGameMode() == GameMode.CREATIVE||player.isOp()) {
							if (init_scoreboard.contains(scoreboard)) {
								try {
									String name = "[ ] " + args[2];
									if (scoreboard.getObjective("RTD").getScore("None").getScore() == -1) {
										scoreboard.resetScores("None");
										init_score_filter.clear();
										if (init_score_turn.isEmpty()) {
											init_score_turn.add(args[2]);
											name = "[X] " + args[2];
										}
									}
									if (init_score_turn.contains(args[2])) {
										name = "[X] " + args[2];
									}
									Score score = objective.getScore(name);
									score.setScore(Integer.valueOf(args[1])); // score
									init_score.put(args[2], score);

									for (Entry<String, Score> entry : init_score.entrySet()) {
										String key = entry.getKey();
										Integer value = entry.getValue().getScore();

										plugin.getConfig().set("People." + key, value);
									}
								}
								catch (Exception e) {
									
								}
							}
						}
					}
					else if (args[0].matches("add")) {
						if (args[2].toLowerCase().matches(player.getName().toLowerCase())||player.getGameMode() == GameMode.CREATIVE||player.isOp()) {
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
									String name = "[ ] " + args[2];
									if (scoreboard.getObjective("RTD").getScore("None").getScore() == -1) {
										scoreboard.resetScores("None");
										init_score_filter.clear();
										if (init_score_turn.isEmpty()) {
											init_score_turn.add(args[2]);
											name = "[X] " + args[2];
										}
									}
									int default_roll = (int) Math.floor(Math.random() * (20 - 1 + 1) + 1);

									if (init_score_turn.contains(args[2])) {
										name = "[X] " + args[2];
									}
									Score score = objective.getScore(name);
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
							} else {
								player.sendMessage("No scoreboard");
							}
						} else {
							player.sendMessage("Not valid/in creative");
						}
					}
				}
				if (!init_score_turn.isEmpty()) {
					plugin.getConfig().set("Turn", init_score_turn.iterator().next());
				}
				plugin.saveConfig();
				for (Player players : Bukkit.getServer().getOnlinePlayers())
					players.setScoreboard(scoreboard);
				return true;
			}

		}
		return false;
	}

}
