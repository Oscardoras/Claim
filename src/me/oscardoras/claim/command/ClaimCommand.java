package me.oscardoras.claim.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.scoreboard.Team;

import me.oscardoras.claim.Message;
import me.oscardoras.claim.claimable.Claim;
import me.oscardoras.claim.claimable.Claimable;
import me.oscardoras.claim.claimable.ProtectedClaim;
import me.oscardoras.claim.owner.EntityOwner;
import me.oscardoras.claim.owner.Owner;
import me.oscardoras.claim.owner.TeamOwner;
import me.oscardoras.claim.rule.ClaimRule;
import me.oscardoras.claim.rule.RuleTarget;
import me.oscardoras.spigotutils.command.v1_16_1_V1.Argument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister;
import me.oscardoras.spigotutils.command.v1_16_1_V1.LiteralArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.CommandRegister.CommandExecutorType;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.BooleanArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.OfflinePlayerArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.QuotedStringArgument;
import me.oscardoras.spigotutils.command.v1_16_1_V1.arguments.ScoreboardTeamArgument;
import net.md_5.bungee.api.chat.TextComponent;

public final class ClaimCommand {
	private ClaimCommand() {}
	
	
	private static void register(LinkedHashMap<String, Argument<?>> arguments, ClaimCommandRunnable runnable) {
		CommandRegister.register("claim", arguments, new Permission("claim.command.claim"), CommandExecutorType.ENTITY, (cmd) -> {
			return runnable.run(cmd, new EntityOwner((Entity) cmd.getExecutor()));
		});
		
		LinkedHashMap<String, Argument<?>> argu = new LinkedHashMap<>();
		argu.put("claim_literal", new LiteralArgument("claim").withPermission(new Permission("claim.command.team")));
		argu.putAll(arguments);
		CommandRegister.register("t", argu, new Permission("team.command.team"), CommandExecutorType.ENTITY, (cmd) -> {
			Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(cmd.getExecutor().getName());
			if (team != null) return runnable.run(cmd, new TeamOwner(team));
			else {
				cmd.sendFailureMessage(new Message("sender.does_not_have_team"));
				return 0;
			}
		});
	}
	
	
	public static void list() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("list", new LiteralArgument("list").withPermission(new Permission("claim.command.claim.list")));
		register(arguments, (cmd, owner) -> {
			List<String> list = new ArrayList<String>();
			for (ProtectedClaim protectedClaim : owner.getProtectedClaims()) {
				String name = protectedClaim.getName();
				if (!list.contains(name)) list.add(name);
			}
			cmd.sendListMessage(list, new Object[] {new Message("command.claim.list.list")}, new Object[] {new Message("command.claim.list.empty")});
			return list.size();
		});
	}
	
	public static void claim() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("claim", new LiteralArgument("claim").withPermission(new Permission("claim.command.claim.claim")));
		register(arguments, (cmd, owner) -> {
			Claimable claimable = Claimable.get(cmd.getLocation().getChunk());
			if (!(claimable instanceof Claim) || owner.equals(((Claim) claimable).getOwner())) {
				if (claimable.checkClaim(owner)) {
					claimable.claim(owner);
					cmd.sendMessage(new Message("command.claim.claim"));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("claim.is_too_far"));
					return 0;
				}
			} else if (((Claim) claimable).canBeStolen() || cmd.hasPermission("claim.ignore")) {
				if (claimable.checkClaim(owner)) {
					claimable.claim(owner);
					cmd.sendMessage(new Message("command.claim.steal"));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("claim.is_too_far"));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("claim.can_not_be_stolen"));
				return 0;
			}
		});
	}
	
	public static void unclaim() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("unclaim", new LiteralArgument("unclaim").withPermission(new Permission("claim.command.claim.unclaim")));
		register(arguments, (cmd, owner) -> {
			Claimable claimable = Claimable.get(cmd.getLocation().getChunk());
			if (claimable instanceof Claim) {
				Claim claim = (Claim) claimable;
				if (owner.equals(claim.getOwner())) {
					claim.unClaim();
					cmd.sendMessage(new Message("command.claim.unclaim"));
					return 1;
				} else if (claim.canBeStolen() || cmd.hasPermission("claim.ignore")) {
					claim.unClaim();
					cmd.sendMessage(new Message("command.claim.steal"));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("claim.can_not_be_stolen"));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("claim.is_not_owned"));
				return 0;
			}
		});
	}
	
	public static void protect() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("protect", new LiteralArgument("protect").withPermission(new Permission("claim.command.claim.protect")));
		register(arguments, (cmd, owner) -> {
			Claimable claimable = Claimable.get(cmd.getLocation().getChunk());
			if (!(claimable instanceof Claim) || owner.equals(((Claim) claimable).getOwner())) {
				claimable.protect(owner);
				cmd.sendMessage(new Message("command.claim.protect"));
				return 1;
			} else if (((Claim) claimable).canBeStolen() || cmd.hasPermission("claim.ignore")) {
				claimable.protect(owner);
				cmd.sendMessage(new Message("command.claim.steal"));
				return 1;
			} else {
				cmd.sendFailureMessage(new Message("claim.can_not_be_stolen"));
				return 0;
			}
		});
	}
	
	public static void unprotect() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("unprotect", new LiteralArgument("unprotect").withPermission(new Permission("claim.command.claim.unprotect")));
		register(arguments, (cmd, owner) -> {
			Claimable claimable = Claimable.get(cmd.getLocation().getChunk());
			if (claimable instanceof Claim && owner.equals(((Claim) claimable).getOwner())) {
				if (claimable instanceof ProtectedClaim) {
					((ProtectedClaim) claimable).unProtect();
					cmd.sendMessage(new Message("command.claim.unprotect"));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("claim.is_not_protected"));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("claim.is_not_owned"));
				return 0;
			}
		});
	}
	
	public static void name() {
		LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
		arguments.put("name_literal", new LiteralArgument("name").withPermission(new Permission("claim.command.claim.name")));
		register(arguments, (cmd, owner) -> {
			Claimable claimable = Claimable.get(cmd.getLocation().getChunk());
			if (claimable instanceof Claim && owner.equals(((Claim) claimable).getOwner())) {
				if (claimable instanceof ProtectedClaim) {
					cmd.sendMessage(new Message("command.claim.name.get", ((ProtectedClaim) claimable).getName()));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("claim.is_not_protected"));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("claim.is_not_owned"));
				return 0;
			}
		});
		
		arguments = new LinkedHashMap<>();
		arguments.put("name_literal", new LiteralArgument("name").withPermission(new Permission("claim.command.claim.name")));
		arguments.put("name", new QuotedStringArgument());
		register(arguments, (cmd, owner) -> {
			Claimable claimable = Claimable.get(cmd.getLocation().getChunk());
			if (claimable instanceof Claim && owner.equals(((Claim) claimable).getOwner())) {
				if (claimable instanceof ProtectedClaim) {
					ProtectedClaim protectedClaim = (ProtectedClaim) claimable;
					protectedClaim.setName((String) cmd.getArg(0));
					cmd.sendMessage(new Message("command.claim.name.set", protectedClaim.getName()));
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("claim.is_not_protected"));
					return 0;
				}
			} else {
				cmd.sendFailureMessage(new Message("claim.is_not_owned"));
				return 0;
			}
		});
	}
	
	public static void rule() {
		for (ClaimRule rule : ClaimRule.values()) {
			LinkedHashMap<String, Argument<?>> arguments = new LinkedHashMap<>();
			arguments.put("rule_literal", new LiteralArgument("rule").withPermission(new Permission("claim.command.claim.rule")));
			arguments.put("protectedClaim", new QuotedStringArgument());
			arguments.put("rule", new LiteralArgument(rule.name()));
			register(arguments, (cmd, owner) -> {
				List<ProtectedClaim> protectedClaims = ProtectedClaim.getProtectedClaims(owner, (String) cmd.getArg(0));
				if (!protectedClaims.isEmpty()) {
					
					Map<RuleTarget, Boolean> ruleValues = protectedClaims.get(0).getClaimRuleValues(rule);
					ruleValues.putIfAbsent(RuleTarget.NEUTRALS, false);
					for (RuleTarget ruleTarget : ruleValues.keySet()) {
						if (ruleTarget.equals(RuleTarget.NEUTRALS) && !cmd.hasPermission("claim.target.neutrals")) continue;
						if (ruleTarget instanceof TeamOwner && !cmd.hasPermission("claim.target.team")) continue;
						if (ruleTarget instanceof EntityOwner && !cmd.hasPermission("claim.target.player")) continue;
						
						String id;
						if (ruleTarget.equals(RuleTarget.NEUTRALS)) id = new Message("neutrals").getMessage(cmd.getLanguage());
						else if (ruleTarget instanceof Owner) id = ((Owner) ruleTarget).getDisplayName();
						else id = ruleTarget.getId();
						
						boolean ruleValue = ruleValues.get(ruleTarget);
						ChatColor color = ruleValue ? ChatColor.GREEN : ChatColor.RED;
						cmd.sendMessage(new TextComponent(id + ": " + color + ruleValue));
					}
					return 1;
				} else {
					cmd.sendFailureMessage(new Message("claim.does_not_exist", (String) cmd.getArg(0)));
					return 0;
				}
			});
			
			for (String string : new String[] {"neutrals", "team", "player"}) {
				arguments = new LinkedHashMap<>();
				arguments.put("rule_literal", new LiteralArgument("rule").withPermission(new Permission("claim.command.claim.rule")));
				arguments.put("protectedClaim", new QuotedStringArgument());
				arguments.put("rule", new LiteralArgument(rule.name()));
				arguments.put("targetType", new LiteralArgument(string).withPermission(new Permission("claim.target." + string)));
				if (string.equals("player")) arguments.put("target", new OfflinePlayerArgument());
				if (string.equals("team")) arguments.put("target", new ScoreboardTeamArgument());
				arguments.put("value", new BooleanArgument());
				register(arguments, (cmd, owner) -> {
					List<ProtectedClaim> protectedClaims = ProtectedClaim.getProtectedClaims(owner, (String) cmd.getArg(0));
					if (!protectedClaims.isEmpty()) {
						String id;
						if (string.equals("neutrals")) id = "@neutrals";
						else if (string.equals("team")) id = ((Team) cmd.getArg(1)).getName() + "@team";
						else if (string.equals("player")) id = ((OfflinePlayer) cmd.getArg(1)).getUniqueId().toString() + "@entity";
						else id = null;
						RuleTarget target = RuleTarget.getRuleTarget(id);
						boolean value = (boolean) cmd.getArg(string.equals("neutrals") ? 1 : 2);
						for (ProtectedClaim protectedClaim : protectedClaims)
							protectedClaim.setClaimRuleValue(rule, target, value);
						cmd.sendMessage(new Message("command.claim.rule.set", rule.name(), ""+value));
						return 1;
					} else {
						cmd.sendFailureMessage(new Message("claim.does_not_exist", (String) cmd.getArg(0)));
						return 0;
					}
				});
				
				arguments = new LinkedHashMap<>();
				arguments.put("rule_literal", new LiteralArgument("rule").withPermission(new Permission("claim.command.claim.rule")));
				arguments.put("protectedClaim", new QuotedStringArgument());
				arguments.put("rule", new LiteralArgument(rule.name()));
				arguments.put("targetType", new LiteralArgument(string));
				if (string.equals("player")) arguments.put("target", new OfflinePlayerArgument());
				if (string.equals("team")) arguments.put("target", new ScoreboardTeamArgument());
				arguments.put("remove", new LiteralArgument("remove"));
				register(arguments, (cmd, owner) -> {
					List<ProtectedClaim> protectedClaims = ProtectedClaim.getProtectedClaims(owner, (String) cmd.getArg(0));
					if (!protectedClaims.isEmpty()) {
						String id;
						if (string.equals("neutrals")) id = "@neutrals";
						else if (string.equals("team")) id = ((Team) cmd.getArg(1)).getName() + "@team";
						else if (string.equals("player")) id = ((OfflinePlayer) cmd.getArg(1)).getUniqueId().toString() + "@entity";
						else id = null;
						RuleTarget target = RuleTarget.getRuleTarget(id);
						for (ProtectedClaim protectedClaim : protectedClaims) protectedClaim.removeClaimRuleValue(rule, target);
						cmd.sendMessage(new Message("command.claim.rule.remove", rule.name()));
						return 1;
					} else {
						cmd.sendFailureMessage(new Message("claim.does_not_exist", (String) cmd.getArg(0)));
						return 0;
					}
				});
			}
		}
	}
	
}