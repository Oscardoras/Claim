package org.bukkitplugin.claim.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Team;
import org.bukkitplugin.claim.ClaimPlugin;
import org.bukkitplugin.claim.Message;
import org.bukkitplugin.claim.claimable.Claim;
import org.bukkitplugin.claim.claimable.Claimable;
import org.bukkitplugin.claim.claimable.ProtectedClaim;
import org.bukkitplugin.claim.owner.EntityOwner;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitplugin.claim.owner.TeamOwner;
import org.bukkitplugin.claim.rule.ClaimRule;
import org.bukkitplugin.claim.rule.RuleTarget;
import org.bukkitutils.command.CommandAPI;
import org.bukkitutils.command.CommandExecutor;
import org.bukkitutils.command.CommandMessage;
import org.bukkitutils.command.arguments.Argument;
import org.bukkitutils.command.arguments.BooleanArgument;
import org.bukkitutils.command.arguments.DynamicSuggestedStringArgument;
import org.bukkitutils.command.arguments.DynamicSuggestedStringArgument.DynamicSuggestionsWithCommandSender;
import org.bukkitutils.command.arguments.LiteralArgument;
import org.bukkitutils.command.arguments.TextArgument;
import org.bukkitutils.io.Translate;

public final class ClaimCommand {
	private ClaimCommand() {}
	
	
	private static void register(LinkedHashMap<String, Argument> arguments, ClaimCommandExecutor executor) {
		CommandAPI.register("claim", arguments, new Permission("claim.command.claim"), new CommandExecutor() {
			public int run(CommandSender sender, Object[] args) {
				return executor.run(sender, new EntityOwner((Entity) sender), args);
			}
		});
		
		LinkedHashMap<String, Argument> argu = new LinkedHashMap<>();
		argu.put("claim_literal", new LiteralArgument("claim").withPermission(new Permission("claim.command.team.claim")));
		CommandAPI.register("t", arguments, new Permission("claim.command.team"), new CommandExecutor() {
			public int run(CommandSender sender, Object[] args) {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(sender.getName());
				if (team != null) return executor.run(sender, new TeamOwner(team), args);
				else {
					CommandMessage.send(sender, new Message("sender.doesnothaveteam"));
					return 0;
				}
			}
		});
	}
	
	
	public final static DynamicSuggestedStringArgument offlinePlayers = new DynamicSuggestedStringArgument(new DynamicSuggestionsWithCommandSender() {
		public String[] getSuggestions(CommandSender sender) {
			List<String> list = new ArrayList<String>();
			if (sender instanceof Entity)
				for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) list.add(offlinePlayer.getName());
			return list.toArray(new String[0]);
		}
	});
	
	public final static DynamicSuggestedStringArgument teams = new DynamicSuggestedStringArgument(new DynamicSuggestionsWithCommandSender() {
		public String[] getSuggestions(CommandSender sender) {
			List<String> list = new ArrayList<String>();
			if (sender instanceof Entity)
				for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) list.add(team.getName());
			return list.toArray(new String[0]);
		}
	});
	
	public static void list() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("list", new LiteralArgument("list").withPermission(new Permission("claim.command.claim.list")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				ArrayList<String> list = new ArrayList<String>();
				for (ProtectedClaim protectedClaim : owner.getProtectedClaims()) {
					String name = protectedClaim.getName();
					if (!list.contains(name)) list.add(name);
				}
				CommandMessage.sendStringList(sender, list, new Message("claimcmd.list"), new Message("claimcmd.empty"));
				return 1;
			}
		});
	}
	
	public static void claim() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("claim", new LiteralArgument("claim").withPermission(new Permission("claim.command.claim.claim")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Claimable claimable = Claimable.get(((Entity) sender).getLocation().getChunk());
				if (!(claimable instanceof Claim) || owner.equals(((Claim) claimable).getOwner())) {
					if (claimable.checkClaim(owner)) {
						claimable.claim(owner);
						CommandMessage.send(sender, new Message("claimcmd.claim"));
						return 1;
					} else {
						CommandMessage.send(sender, new Message("claim.istoofar"));
						return 0;
					}
				} else if (((Claim) claimable).canBeStolen()) {
					if (claimable.checkClaim(owner)) {
						claimable.claim(owner);
						CommandMessage.send(sender, new Message("claimcmd.steal"));
						return 1;
					} else {
						CommandMessage.send(sender, new Message("claim.istoofar"));
						return 0;
					}
				} else {
					CommandMessage.send(sender, new Message("claim.cannotbestolen"));
					return 0;
				}
			}
		});
	}
	
	public static void unclaim() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("unclaim", new LiteralArgument("unclaim").withPermission(new Permission("claim.command.claim.unclaim")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Claimable claimable = Claimable.get(((Entity) sender).getLocation().getChunk());
				if (claimable instanceof Claim) {
					Claim claim = (Claim) claimable;
					if (owner.equals(claim.getOwner())) {
						claim.unClaim();
						CommandMessage.send(sender, new Message("claimcmd.unclaim"));
						return 1;
					} else if (claim.getCoef() <= ClaimPlugin.plugin.coefs.steal) {
						claim.unClaim();
						CommandMessage.send(sender, new Message("claimcmd.steal"));
						return 1;
					} else {
						CommandMessage.send(sender, new Message("claim.cannotbestolen"));
						return 0;
					}
				} else {
					CommandMessage.send(sender, new Message("claim.isnotowned"));
					return 0;
				}
			}
		});
	}
	
	public static void protect() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("protect", new LiteralArgument("protect").withPermission(new Permission("claim.command.claim.protect")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Claimable claimable = Claimable.get(((Entity) sender).getLocation().getChunk());
				if (!(claimable instanceof Claim) || owner.equals(((Claim) claimable).getOwner())) {
					claimable.protect(owner);
					CommandMessage.send(sender, new Message("claimcmd.protect"));
					return 1;
				} else if (((Claim) claimable).canBeStolen()) {
					claimable.protect(owner);
					CommandMessage.send(sender, new Message("claimcmd.steal"));
					return 1;
				} else {
					CommandMessage.send(sender, new Message("claim.cannotbestolen"));
					return 0;
				}
			}
		});
	}
	
	public static void unprotect() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("unprotect", new LiteralArgument("unprotect").withPermission(new Permission("claim.command.claim.unprotect")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Claimable claimable = Claimable.get(((Entity) sender).getLocation().getChunk());
				if (claimable instanceof Claim && owner.equals(((Claim) claimable).getOwner())) {
					if (claimable instanceof ProtectedClaim) {
						((ProtectedClaim) claimable).unProtect();
						CommandMessage.send(sender, new Message("claimcmd.unprotect"));
						return 1;
					} else {
						CommandMessage.send(sender, new Message("claim.isnotprotected"));
						return 0;
					}
				} else {
					CommandMessage.send(sender, new Message("claim.isnotowned"));
					return 0;
				}
			}
		});
	}
	
	public static void name() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("name_literal", new LiteralArgument("name").withPermission(new Permission("claim.command.claim.name")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Claimable claimable = Claimable.get(((Entity) sender).getLocation().getChunk());
				if (claimable instanceof Claim && owner.equals(((Claim) claimable).getOwner())) {
					if (claimable instanceof ProtectedClaim) {
						CommandMessage.send(sender, new Message("claim.name.get"), ((ProtectedClaim) claimable).getName());
						return 1;
					} else {
						CommandMessage.send(sender, new Message("claim.isnotprotected"));
						return 0;
					}
				} else {
					CommandMessage.send(sender, new Message("claim.isnotowned"));
					return 0;
				}
			}
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("name_literal", new LiteralArgument("name").withPermission(new Permission("claim.command.claim.name")));
		arguments.put("name", new TextArgument());
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Claimable claimable = Claimable.get(((Entity) sender).getLocation().getChunk());
				if (claimable instanceof Claim && owner.equals(((Claim) claimable).getOwner())) {
					if (claimable instanceof ProtectedClaim) {
						ProtectedClaim protectedClaim = (ProtectedClaim) claimable;
						protectedClaim.setName((String) args[0]);
						CommandMessage.send(sender, new Message("claim.name.set"), protectedClaim.getName());
						return 1;
					} else {
						CommandMessage.send(sender, new Message("claim.isnotprotected"));
						return 0;
					}
				} else {
					CommandMessage.send(sender, new Message("claim.isnotowned"));
					return 0;
				}
			}
		});
	}
	
	public static void rule() {
		for (ClaimRule rule : ClaimRule.values()) {
			LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
			arguments.put("rule", new LiteralArgument("rule").withPermission(new Permission("claim.command.claim.rule")));
			arguments.put("protectedClaim", new TextArgument());
			arguments.put(rule.name(), new LiteralArgument(rule.name()));
			register(arguments, new ClaimCommandExecutor() {
				public int run(CommandSender sender, Owner owner, Object[] args) {
					List<ProtectedClaim> protectedClaims = ProtectedClaim.getProtectedClaims(owner, (String) args[0]);
					if (!protectedClaims.isEmpty()) {
						
						Map<RuleTarget, Boolean> ruleValues = protectedClaims.get(0).getClaimRuleValues(rule);
						ruleValues.putIfAbsent(RuleTarget.NEUTRALS, false);
						ruleValues.putIfAbsent(RuleTarget.ALLIES, false);
						for (RuleTarget ruleTarget : ruleValues.keySet()) {
							if (ruleTarget.equals(RuleTarget.NEUTRALS) && !sender.hasPermission("claim.target.neutrals")) continue;
							if (ruleTarget.equals(RuleTarget.ALLIES) && !sender.hasPermission("claim.target.allies")) continue;
							if (ruleTarget instanceof TeamOwner && !sender.hasPermission("claim.target.team")) continue;
							if (ruleTarget instanceof EntityOwner && !sender.hasPermission("claim.target.player")) continue;
							
							String id;
							if (ruleTarget.equals(RuleTarget.NEUTRALS)) id = Translate.getPluginMessage(sender, new Message("neutrals"));
							else if (ruleTarget.equals(RuleTarget.ALLIES)) id = Translate.getPluginMessage(sender, new Message("allies"));
							else if (ruleTarget instanceof Owner) id = ((Owner) ruleTarget).getName();
							else id = ruleTarget.getId();
							
							boolean ruleValue = ruleValues.get(ruleTarget);
							ChatColor color = ruleValue ? ChatColor.GREEN : ChatColor.RED;
							sender.sendMessage(id + ": " + color + ruleValue);
						}
						return 1;
					} else {
						CommandMessage.send(sender, new Message("claim.doesnotexist"), (String) args[0]);
						return 0;
					}
				}
			});
			
			for (String string : new String[] {"neutrals", "allies", "team", "player"}) {
				arguments = new LinkedHashMap<>();
				arguments.put("rule", new LiteralArgument("rule").withPermission(new Permission("claim.command.claim.rule")));
				arguments.put("protectedClaim", new TextArgument());
				arguments.put(rule.name(), new LiteralArgument(rule.name()));
				arguments.put("targetType", new LiteralArgument(string).withPermission(new Permission("claim.target." + string)));
				if (string.equals("player")) arguments.put("target", offlinePlayers);
				if (string.equals("team")) arguments.put("target", teams);
				arguments.put("value", new BooleanArgument());
				register(arguments, new ClaimCommandExecutor() {
					public int run(CommandSender sender, Owner owner, Object[] args) {
						List<ProtectedClaim> protectedClaims = ProtectedClaim.getProtectedClaims(owner, (String) args[0]);
						if (!protectedClaims.isEmpty()) {
							RuleTarget target = RuleTarget.getRuleTarget((args.length == 3 ? (String) args[1] : "") + "@" + string.replace("player", "entity"));
							if (target != null) {
								for (ProtectedClaim protectedClaim : protectedClaims)
									protectedClaim.setClaimRuleValue(rule, target, (boolean) args[args.length - 1]);
								CommandMessage.send(sender, new Message("claimcmd.rule.set"), rule.name(), ""+(boolean) args[args.length - 1]);
								return 1;
							} else {
								if (string.equals("team")) CommandMessage.send(sender, new Message("team.doesnotexist"), (String) args[1]);
								if (string.equals("player")) CommandMessage.send(sender, new Message("player.doesnotexist"), (String) args[1]);
								return 0;
							}
						} else {
							CommandMessage.send(sender, new Message("claim.doesnotexist"), (String) args[0]);
							return 0;
						}
					}
				});
				
				arguments = new LinkedHashMap<>();
				arguments.put("rule", new LiteralArgument("rule").withPermission(new Permission("claim.command.claim.rule")));
				arguments.put("protectedClaim", new TextArgument());
				arguments.put(rule.name(), new LiteralArgument(rule.name()));
				arguments.put("targetType", new LiteralArgument(string));
				if (string.equals("player")) arguments.put("target", offlinePlayers);
				if (string.equals("team")) arguments.put("target", teams);
				arguments.put("remove", new LiteralArgument("remove"));
				register(arguments, new ClaimCommandExecutor() {
					public int run(CommandSender sender, Owner owner, Object[] args) {
						List<ProtectedClaim> protectedClaims = ProtectedClaim.getProtectedClaims(owner, (String) args[0]);
						if (!protectedClaims.isEmpty()) {
							RuleTarget target = RuleTarget.getRuleTarget((args.length == 3 ? (String) args[1] : "") + "@" + string.replace("player", "entity"));
							if (target != null) {
								for (ProtectedClaim protectedClaim : protectedClaims) protectedClaim.removeClaimRuleValue(rule, target);
								CommandMessage.send(sender, new Message("claimcmd.rule.remove"), rule.name());
								return 1;
							} else {
								if (string.equals("team")) CommandMessage.send(sender, new Message("team.doesnotexist"), (String) args[1]);
								if (string.equals("player")) CommandMessage.send(sender, new Message("player.doesnotexist"), (String) args[1]);
								return 0;
							}
						} else {
							CommandMessage.send(sender, new Message("claim.doesnotexist"), (String) args[0]);
							return 0;
						}
					}
				});
			}
		}
	}
	
}