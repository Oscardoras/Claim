package org.bukkitplugin.claim.rule;

import org.bukkitplugin.claim.owner.Owner;

public interface RuleTarget {
	
	public final static RuleTarget ALLIES = new RuleTarget() {
		public String getId() {
			return "@allies";
		}

		@Override
		public boolean equals(Object object) {
			return object != null && object instanceof RuleTarget && ((RuleTarget) object).getId().equals(getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
	};
	
	public final static RuleTarget NEUTRALS = new RuleTarget() {
		public String getId() {
			return "@neutrals";
		}

		@Override
		public boolean equals(Object object) {
			return object != null && object instanceof RuleTarget && ((RuleTarget) object).getId().equals(getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
	};
	
	
	public String getId();
	
	
	public static RuleTarget getRuleTarget(String id) {
		if (id.equals("@allies")) return ALLIES;
		if (id.equals("@neutrals")) return NEUTRALS;
		return Owner.getOwner(id);
	}
	
}