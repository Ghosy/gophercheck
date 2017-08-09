package gophercheck;

import java.io.Serializable;

public class GCBadge implements Serializable {
	private String badgeNumber;

	public GCBadge(String badgeNumber) {
		this.badgeNumber = badgeNumber;
	}

	public String getBadgeNumber() {
		return badgeNumber;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof GCBadge && ((GCBadge) o).getBadgeNumber().equals(badgeNumber)) ||
			(o instanceof String && o.equals(badgeNumber));
	}

	@Override
	public int hashCode() {
		return badgeNumber.hashCode();
	}
}
