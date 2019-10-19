package org.bukkitplugin.claim;

import org.bukkitutils.io.TranslatableMessage;

public class Message extends TranslatableMessage {
	
	public Message(String path, String... args) {
		super(ClaimPlugin.plugin, path, args);
	}
	
}