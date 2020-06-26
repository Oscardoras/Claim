package me.oscardoras.claim.rule;

import me.oscardoras.claim.owner.Owner;

public interface RuleTarget {
	
	public final static RuleTarget NEUTRALS = new RuleTarget() {
		public String getId() {
			return "@neutrals";
		}

		@Override
		public boolean equals(Object object) {
			return object == this;
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
	};
	
	
	public String getId();
	
	
	public static RuleTarget getRuleTarget(String id) {
		if (id.equals("@neutrals")) return NEUTRALS;
		else return Owner.getOwner(id);
	}
	
}