package me.oscardoras.claim;

import me.oscardoras.spigotutils.io.TranslatableMessage;

public class Message extends TranslatableMessage {
	
	public Message(String path, String... args) {
		super(ClaimPlugin.plugin, path, args);
	}
	
}