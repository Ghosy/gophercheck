package gophercheck;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeEntry implements Comparable, Serializable {

	private boolean isComplete = false;
	private LocalDateTime dispatchTime;
	private LocalDateTime returnTime;
	private String task;
	private double timeMod;
	// Staff who signs off on entry
	private GCBadge staffSig;

	public TimeEntry(LocalDateTime dispatchTime, String task, double timeMod) {
		this.task = task;
		this.dispatchTime = dispatchTime;
		this.timeMod = timeMod;
	}

	public void signEntry(GCBadge staffSig, LocalDateTime returnTime) {
		this.staffSig = staffSig;
		this.returnTime = returnTime;
		isComplete = true;
	}

	public LocalDateTime getDispatchTime() {
		return dispatchTime;
	}

	public long taskTime() {
		if(isComplete) {
			return (long)(timeMod * (double)ChronoUnit.MINUTES.between(dispatchTime, returnTime));
		}
		else {
			return (long)(timeMod * (double)ChronoUnit.MINUTES.between(dispatchTime, LocalDateTime.now()));
		}
	}

	// public void setTimeMod(double timeMod) {
	// 	this.timeMod = timeMod;
	// }

	public void printSummary() {
		System.out.println("Task: " + task);
		// System.out.printf("Dispatch Time: %d-%d %02d:%02d\n", dispatchTime.getMonthValue(), dispatchTime.getDayOfMonth(), dispatchTime.getHour(), dispatchTime.getMinute());
		System.out.printf("Dispatch Time: %02d-%02d %02d:%02d\n", dispatchTime.getMonthValue(), dispatchTime.getDayOfMonth(), dispatchTime.getHour(), dispatchTime.getMinute());
		if(isComplete) {
			// System.out.printf("Return Time: %d-%d %02d:%02d\n", returnTime.getMonthValue(), returnTime.getDayOfMonth(), returnTime.getHour(), returnTime.getMinute());
			System.out.printf("Return Time: %02d-%02d %02d:%02d\n", returnTime.getMonthValue(), returnTime.getDayOfMonth(), returnTime.getHour(), returnTime.getMinute());
			System.out.println("Staff Sig: " + BadgeUtils.trimIDStaff(staffSig.getBadgeNumber()));
		}
		else {
			System.out.println("Return Time: N/A");
			System.out.println("Staff Sig: N/A");
		}
		System.out.printf("Time Modifier: %.1f", timeMod);
	}

	public String getTask() {
		return task;
	}

	public boolean isSigned() {
		return isComplete;
	}

	@Override
	public int compareTo(Object anotherTimeEntry) throws ClassCastException {
		if(!(anotherTimeEntry instanceof TimeEntry)) {
			throw new ClassCastException("A TimeEntry object expected.");
		}
		return dispatchTime.compareTo(((TimeEntry) anotherTimeEntry).getDispatchTime());
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof TimeEntry && ((TimeEntry) o).getDispatchTime().equals(dispatchTime) && ((TimeEntry)o).getTask().equals(task));
	}

	@Override
	public int hashCode() {
		return dispatchTime.hashCode();
	}
}
