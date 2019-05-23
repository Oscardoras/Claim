package org.bukkitplugin.claim.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Team;
import org.bukkitplugin.claim.Message;
import org.bukkitplugin.claim.owner.EntityOwner;
import org.bukkitplugin.claim.owner.Owner;
import org.bukkitplugin.claim.owner.TeamOwner;
import org.bukkitutils.command.CommandAPI;
import org.bukkitutils.command.CommandExecutor;
import org.bukkitutils.command.CommandMessage;
import org.bukkitutils.command.arguments.Argument;
import org.bukkitutils.command.arguments.DynamicSuggestedStringArgument;
import org.bukkitutils.command.arguments.DynamicSuggestedStringArgument.DynamicSuggestionsWithCommandSender;
import org.bukkitutils.command.arguments.LiteralArgument;

public final class Ally {
	private Ally() {}
	
	
	private static void register(LinkedHashMap<String, Argument> arguments, ClaimCommandExecutor executor) {
		CommandAPI.register("ally", arguments, new Permission("claim.command.ally"), new CommandExecutor() {
			public int run(CommandSender sender, Object[] args) {
				return executor.run(sender, new EntityOwner((Entity) sender), args);
			}
		});
		
		LinkedHashMap<String, Argument> argu = new LinkedHashMap<>();
		argu.put("ally_literal", new LiteralArgument("ally").withPermission(new Permission("claim.command.team.ally")));
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
	
	
	public static final DynamicSuggestedStringArgument offlinePlayers = new DynamicSuggestedStringArgument(new DynamicSuggestionsWithCommandSender() {
		public String[] getSuggestions(CommandSender sender) {
			List<String> list = new ArrayList<String>();
			for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) list.add(offlinePlayer.getName());
			return list.toArray(new String[0]);
		}
	});
	
	public static final DynamicSuggestedStringArgument teams = new DynamicSuggestedStringArgument(new DynamicSuggestionsWithCommandSender() {
		public String[] getSuggestions(CommandSender sender) {
			List<String> list = new ArrayList<String>();
			for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) list.add(team.getName());
			return list.toArray(new String[0]);
		}
	});
	
	public static void list() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("list", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.list")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				ArrayList<String> list = new ArrayList<String>();
				for (Owner ally : owner.getAllies()) list.add(ally.getName());
				CommandMessage.sendStringList(sender, list, new Message("ally.list"), new Message("ally.empty"));
				return 1;
			}
		});
	}
	
	public static void proposalsList() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("proposals", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.proposals")));
		arguments.put("list", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.proposals.list")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				ArrayList<String> list = new ArrayList<String>();
				for (Owner ally : owner.getAllianceProposals()) list.add(ally.getName());
				CommandMessage.sendStringList(sender, list, new Message("ally.proposals.list"), new Message("ally.proposals.empty"));
				return 1;
			}
		});
	}
	
	public static void proposalsSend() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("proposals", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.proposals")));
		arguments.put("send", new LiteralArgument("send").withPermission(new Permission("claim.command.ally.proposals.send")));
		arguments.put("player", new LiteralArgument("player"));
		arguments.put("ally", offlinePlayers);
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				@SuppressWarnings("deprecation")
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((String) args[0]);
				if (owner.sendAllianceProposal(new EntityOwner(offlinePlayer))) {
					CommandMessage.send(sender, new Message("ally.proposals.send"), offlinePlayer.getName());
					return 1;
				} else {
					CommandMessage.send(sender, new Message("ally.proposals.alreadysent"), offlinePlayer.getName());
					return 0;
				}
			}
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("proposals", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.proposals")));
		arguments.put("send", new LiteralArgument("send").withPermission(new Permission("claim.command.ally.proposals.send")));
		arguments.put("team", new LiteralArgument("team"));
		arguments.put("ally", teams);
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam((String) args[0]);
				if (team != null) {
					if (owner.sendAllianceProposal(new TeamOwner(team))) {
						CommandMessage.send(sender, new Message("ally.proposals.send"), team.getName());
						return 1;
					} else {
						CommandMessage.send(sender, new Message("ally.proposals.alreadysent"), team.getName());
						return 0;
					}
				} else {
					CommandMessage.send(sender, new Message("team.doesnotexist"), (String) args[0]);
					return 0;
				}
			}
		});
	}
	
	public static void proposalsRevoke() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("proposals", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.proposals")));
		arguments.put("revoke", new LiteralArgument("revoke").withPermission(new Permission("claim.command.ally.proposals.revoke")));
		arguments.put("player", new LiteralArgument("player"));
		arguments.put("ally", offlinePlayers);
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				@SuppressWarnings("deprecation")
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((String) args[0]);
				if (owner.revokeAllianceProposal(new EntityOwner(offlinePlayer))) {
					CommandMessage.send(sender, new Message("ally.proposals.revoke"), offlinePlayer.getName());
					return 1;
				} else {
					CommandMessage.send(sender, new Message("ally.proposals.notsent"), offlinePlayer.getName());
					return 0;
				}
			}
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("proposals", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.proposals")));
		arguments.put("revoke", new LiteralArgument("revoke").withPermission(new Permission("claim.command.ally.proposals.revoke")));
		arguments.put("team", new LiteralArgument("team"));
		arguments.put("ally", teams);
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam((String) args[0]);
				if (team != null) {
					if (owner.revokeAllianceProposal(new TeamOwner(team))) {
						CommandMessage.send(sender, new Message("ally.proposals.revoke"), team.getName());
						return 1;
					} else {
						CommandMessage.send(sender, new Message("ally.proposals.notsent"), team.getName());
						return 0;
					}
				} else {
					CommandMessage.send(sender, new Message("team.doesnotexist"), (String) args[0]);
					return 0;
				}
			}
		});
	}
	
	public static void requestsList() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("requests", new LiteralArgument("requests").withPermission(new Permission("claim.command.ally.requests")));
		arguments.put("list", new LiteralArgument("list").withPermission(new Permission("claim.command.ally.requests.list")));
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				ArrayList<String> list = new ArrayList<String>();
				for (Owner ally : owner.getAllianceRequests()) list.add(ally.getName());
				CommandMessage.sendStringList(sender, list, new Message("ally.requests.list"), new Message("ally.requests.empty"));
				return 1;
			}
		});
	}
	
	public static void remove() {
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("remove", new LiteralArgument("remove").withPermission(new Permission("claim.command.ally.remove")));
		arguments.put("player", new LiteralArgument("player").withPermission(new Permission("claim.target.player")));
		arguments.put("ally", offlinePlayers);
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				@SuppressWarnings("deprecation")
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer((String) args[0]);
				if (owner.removeAlly(new EntityOwner(offlinePlayer))) {
					CommandMessage.send(sender, new Message("ally.remove"), offlinePlayer.getName());
					return 1;
				} else {
					CommandMessage.send(sender, new Message("ally.doesnotexist"), (String) args[0]);
					return 0;
				}
			}
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("remove", new LiteralArgument("remove").withPermission(new Permission("claim.command.ally.remove")));
		arguments.put("team", new LiteralArgument("team").withPermission(new Permission("claim.target.team")));
		arguments.put("ally", teams);
		register(arguments, new ClaimCommandExecutor() {
			public int run(CommandSender sender, Owner owner, Object[] args) {
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam((String) args[0]);
				if (team != null)
					if (owner.removeAlly(new TeamOwner(team))) {
						CommandMessage.send(sender, new Message("ally.remove"), team.getName());
						return 1;
					} else {
						CommandMessage.send(sender, new Message("ally.doesnotexist"), (String) args[0]);
						return 0;
					}
				else {
					CommandMessage.send(sender, new Message("team.doesnotexist"), (String) args[0]);
					return 0;
				}
			}
		});
	}
	
}