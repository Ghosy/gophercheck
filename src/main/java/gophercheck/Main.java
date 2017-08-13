package gophercheck;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
// Remove these(For the temp timeFix())
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.time.temporal.ChronoUnit;
// Remove these ^
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.smartcardio.CardException;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;
import org.nfctools.spi.acs.Acr122ReaderWriter;
import org.nfctools.spi.acs.AcsTerminal;
import org.nfctools.utils.CardTerminalUtils;

public class Main {
	// There is a better way to do this. Someday you should stop being lazy.
	private static String idStorage;

	private static HashMap<GCBadge, ArrayList<TimeEntry>> gophers = new HashMap<GCBadge, ArrayList<TimeEntry>>();

	public static void main(String args[]) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String cmd = "";
		// Testing
		// ArrayList<TimeEntry> test = new ArrayList<TimeEntry>();
		// test.add(new TimeEntry(LocalDateTime.now(), "Test", 1.5));
		// TimeEntry test2 = new TimeEntry(LocalDateTime.now().minusDays(1), "Test2", 1.5);
		// test2.signEntry(new GCBadge("000000144"), LocalDateTime.now().minusDays(1).plusMinutes(161));
		// test.add(test2);
		// gophers.put(new GCBadge("000000000"), test);
		// Testing ends
		load();

running:
		while(!cmd.equalsIgnoreCase("exit")) {
			System.out.print(">");
			cmd = in.readLine();

			switch(cmd.toLowerCase()) {
				case "a":
				case "add":
					addEntry(in, promptBadge(in));
					save();
					break;
				case "active":
					active();
					break;
				case "gophers":
					listGophers();
					break;
				case "i":
				case "idle":
					idle(in);
					save();
					break;
				case "idlers":
					idlers();
					break;
				case "load":
					load();
					break;
				case "reg":
				case "register":
					register(in);
					save();
					break;
				case "rm":
				case "remove":
					removeEntry(in);
					save();
					break;
				case "sign":
					sign(in);
					save();
					break;
				case "save":
					save();
					System.out.println("Save Complete");
					break;
				case "success":
					System.out.println("Jessie get off the computer");
					break;
				case "s":
				case "sum":
				case "summary":
					summary(in);
					break;
				case "fix":
					fix();
					break;
				case "q":
				case "quit":
					break running;
				case "a valid command":
					System.out.println("I bet you think you're funny, don't you");
					break;
				case "help":
					printUsage();
					break;
				default:
					System.out.println("Not a valid command");
					break;
			}
		}
	}

	private static void fix() {
		for(ArrayList<TimeEntry> gopherEntries : gophers.values()) {
			for(TimeEntry t : gopherEntries) {
				// if(t.taskTime() >= 120 && !(t.getTask().equals("Idle"))) {
				// 	t.setTimeMod(1.5);
				// }
			}
		}
	}

	// TODO: This doesn't follow the same parameter setup as other methods. Change this one or others?
	private static void addEntry(BufferedReader in, String id) throws IOException {
		// String id = promptBadge(in);

		if(gophers.containsKey(new GCBadge(id))) {
			ArrayList<TimeEntry> gopherEntries = gophers.get(new GCBadge(id));
			for(TimeEntry t : gopherEntries) {
				if(!t.isSigned()) {
					System.out.println("This Badge has at least one unsigned entry, please sign those before adding another.");
					return;
				}
			}
			String s, task;
			double timeMod;
			LocalDateTime ltd;

			// Get task name
			System.out.println("Type the name of the task or press ENTER for idle");
			s = in.readLine();
			if(s.equals("")) {
				task = "Idle";
			}
			else {
				task = s;
			}
			// Get time modifier
			System.out.println("Type \"y\" if the work is 1.5 time or press ENTER for 1 time");
			s = in.readLine();
			if(s.equals("")) {
				timeMod = 1.0;
			}
			else {
				timeMod = 1.5;
			}

			// Get dispatch time
			System.out.println("Type the date in yyyy-MM-dd HH:mm format or press ENTER for the current time");
			s = in.readLine();
			if(s.equals("")) {
				ltd = LocalDateTime.now();
			}
			else {
				DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				try {
					ltd = LocalDateTime.parse(s, format);
				} catch(DateTimeParseException ex) {
					System.out.println("The date you provided is not correctly formatted");
					return;
				}
			}

			// Add entry to database
			gopherEntries.add(new TimeEntry(ltd, task, timeMod));
			gophers.put(new GCBadge(id), gopherEntries);
		}
		else {
			System.out.println("Badge Number: " + id + " is not in the database.");
		}
	}

	private static void idle(BufferedReader in) throws IOException {
		String id = promptBadge(in);
		if(gophers.containsKey(new GCBadge(id))) {
			ArrayList<TimeEntry> gopherEntries = gophers.get(new GCBadge(id));
			for(TimeEntry t : gopherEntries) {
				if(!t.isSigned()) {
					System.out.println("This Badge has at least one unsigned entry, please sign those before adding another.");
					return;
				}
			}
			gopherEntries.add(new TimeEntry(LocalDateTime.now(), "Idle", 1.0));
		}
	}

	private static void idle(ArrayList<TimeEntry> gopherEntries) throws IOException {
		gopherEntries.add(new TimeEntry(LocalDateTime.now(), "Idle", 1.0));
	}

	private static void register(BufferedReader in) throws IOException {
		String id = "";
		while(!BadgeUtils.isValidID(id)) {
			System.out.println("Type a badge number");
			id = in.readLine();

			if(!BadgeUtils.isValidID(id)) {
				System.out.println(id + " is not a valid badge number.");
			}
		}
		System.out.println("Scan the new tag");
		writeTag(id);

		ArrayList<TimeEntry> gopherEntries = new ArrayList<TimeEntry>();
		gophers.put(new GCBadge(id), gopherEntries);
	}

	private static void removeEntry(BufferedReader in) throws IOException {
		String id = promptBadge(in);
		if(gophers.containsKey(new GCBadge(id))) {
			System.out.println("Type the entry number you would like to remove");
			int entryNum;
			try {
				entryNum = Integer.parseInt(in.readLine());
				try {
					ArrayList<TimeEntry> gopherEntries  = gophers.get(new GCBadge(id));
					gopherEntries.remove(entryNum);
				} catch(IndexOutOfBoundsException ex) {
					System.out.println("Entry not found");
				}
			} catch(NumberFormatException ex) {
				System.out.println("Please Enter a valid number");
			}
		}
		else {
			System.out.println("Badge Number: " + id + " is not in the database.");
		}
	}

	private static void sign(BufferedReader in) throws IOException {
		System.out.println("Gopher:");
		String id = promptBadge(in);
		if(gophers.containsKey(new GCBadge(id))) {
			TimeEntry unsigned = null;

			// Get time entries and sort them
			ArrayList<TimeEntry> gopherEntries = gophers.get(new GCBadge(id));
			Collections.sort(gopherEntries);

			for(TimeEntry t : gopherEntries) {
				if(!t.isSigned()) {
					// Staff who is signing badge
					System.out.println("Staff:");
					String staffID = promptBadge(in);

					// Requst sign time
					System.out.println("Type the date in yyyy-MM-dd HH:mm format or press ENTER for the current time");
					String s = in.readLine();
					LocalDateTime ltd;
					if(s.equals("")) {
						ltd = LocalDateTime.now();
					}
					else {
						DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
						try {
							ltd = LocalDateTime.parse(s, format);
						} catch(DateTimeParseException ex) {
							System.out.println("The date you provided is not correctly formatted");
							return;
						}
					}

					t.signEntry(new GCBadge(staffID), ltd);

					// If time is longer than 2 hours and not idle time set to 1.5 times
					// if(t.taskTime() >= 120 && !(t.getTask().equals("Idle"))) {
					// 	t.setTimeMod(1.5);
					// }

					// Prompt if resign into idle
					System.out.println("Type \"y\" if you want to resign the badge into idle or ENTER if not");
					s = in.readLine();
					if(s.equals("y")) {
						idle(gopherEntries);
					}

					System.out.println("Type \"y\" if you want to resign the badge into an event or ENTER if not");
					s = in.readLine();
					if(s.equals("y")) {
						addEntry(in, id);
					}

					return;
				}
			}
			System.out.println("No unsigned entries found");
		}
		else {
			System.out.println("Badge Number: " + id + " is not in the database.");
		}
	}

	private static void summary(BufferedReader in) throws IOException {
		String id = promptBadge(in);
		if(gophers.containsKey(new GCBadge(id))) {
			System.out.println("\nBadge Number: " + id + "\n");

			// Get time entries and sort them
			ArrayList<TimeEntry> gopherEntries = gophers.get(new GCBadge(id));
			Collections.sort(gopherEntries);

			long totalTime = 0;
			int i = 0;
			for(TimeEntry t : gopherEntries) {
				totalTime += timeFix(t);
				System.out.println("Entry: " + i);
				t.printSummary();
				System.out.println("\n");
				i++;
			}

			System.out.printf("Total task time: %02d:%02d\n", (totalTime / 60), (totalTime % 60));
		}
		else {
			System.out.println("Badge Number: " + id + " is not in the database.");
		}
	}

	// TODO: Temp this better fucking go bye bye at the end of the con(2017 I mean, don't think you can roll this over to next year)
	private static long timeFix(TimeEntry t) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bytes);
		PrintStream old = System.out;
		System.setOut(ps);
		t.printSummary();
		System.out.flush();
		System.setOut(old);
		String s = bytes.toString();
		String ret = s.substring(s.indexOf("Return Time: ") + 13);
		ret = ret.substring(0, ret.indexOf("\n"));
		String dis = s.substring(s.indexOf("Dispatch Time: ") + 15);
		dis = dis.substring(0, dis.indexOf("\n"));
		dis = "2017-" + dis;

		LocalDateTime returnTime, dispatchTime;
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		try {
			dispatchTime = LocalDateTime.parse(dis, format);
		} catch(DateTimeParseException ex) {
			System.out.println("The date you provided is not correctly formatted");
			return 0;
		}
		if(!ret.equals("N/A")) {
			ret = "2017-" + ret;
			try {
				returnTime = LocalDateTime.parse(ret, format);
			} catch(DateTimeParseException ex) {
				System.out.println("The date you provided is not correctly formatted");
				return 0;
			}
			long timeBetween = (long)(ChronoUnit.MINUTES.between(dispatchTime, returnTime));
			if(timeBetween >= 120) {
				return (long)(1.5 * (double)timeBetween);
			}
			else {
				return timeBetween;
			}
		}
		else {
			return ChronoUnit.MINUTES.between(dispatchTime, LocalDateTime.now());
		}
	}

	private static void active() {
		// for(ArrayList<TimeEntry> gopherEntries : gophers.values()) {
		for(GCBadge badge : gophers.keySet()) {
			ArrayList<TimeEntry> gopherEntries = gophers.get(badge);
			for(TimeEntry t : gopherEntries) {
				if(!t.isSigned() && !(t.getTask().equals("Idle"))) {
					System.out.println("Badge: " + badge.getBadgeNumber());
					t.printSummary();
					System.out.println("\n");
				}
			}
		}
	}

	private static void listGophers() {
		for(GCBadge badge : gophers.keySet()) {
			ArrayList<TimeEntry> gopherEntries = gophers.get(badge);
			long totalTime = 0;
			for(TimeEntry t : gopherEntries) {
				totalTime += timeFix(t);
			}
			System.out.println("Badge: " + badge.getBadgeNumber());
			System.out.printf("Total task time: %02d:%02d\n\n", (totalTime / 60), (totalTime % 60));
		}
	}

	private static void idlers() {
		for(GCBadge badge : gophers.keySet()) {
			ArrayList<TimeEntry> gopherEntries = gophers.get(badge);
			for(TimeEntry t : gopherEntries) {
				if(!t.isSigned() && (t.getTask().equals("Idle"))) {
					System.out.println("Badge: " + badge.getBadgeNumber());
					t.printSummary();
					System.out.println("\n");
				}
			}
		}
	}

	private static void save() {
		try {
			// TODO: the file path should not be hardcoded
			FileOutputStream fos = new FileOutputStream("/home/ghosy/.gophers.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(gophers);
			oos.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static void load() {
		try {
			// TODO: the file path should not be hardcoded
			FileInputStream fileInputStream = new FileInputStream("/home/ghosy/.gophers.dat");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			gophers = (HashMap)objectInputStream.readObject();
			System.out.println("Load Complete");
		}
		catch(ClassNotFoundException | IOException | ClassCastException e) {
			e.printStackTrace();
		}
	}

	private static String promptBadge(BufferedReader in) throws IOException {
		String id = "";
		while(!BadgeUtils.isValidID(id)) {
			System.out.println("Type a badge number or press ENTER to scan a badge");
			id = in.readLine();

			if(id.equals("")) {
				String newID = readTag();
				if(newID != null) {
					id = BadgeUtils.trimID(newID);
				}
			}
			if(!BadgeUtils.isValidID(id)) {
				System.out.println(id + " is not a valid badge number.");
			}
		}
		return id;
	}

	private static void printUsage() {
		System.out.printf("%-20s %-20s\n", "a, add:", "Add a time entry");
		System.out.printf("%-20s %-20s\n", "active:", "List active jobs");
		System.out.printf("%-20s %-20s\n", "i, idle:", "Sign a user into idle at the current time");
		System.out.printf("%-20s %-20s\n", "load:", "Load the most recent saved state");
		System.out.printf("%-20s %-20s\n", "reg, register:", "Setup a new badge");
		System.out.printf("%-20s %-20s\n", "rm, remove:", "Remove a time entry");
		System.out.printf("%-20s %-20s\n", "sign:", "Sign a completed time entry");
		System.out.printf("%-20s %-20s\n", "save:", "Save the current state");
		System.out.printf("%-20s %-20s\n", "s, sum, summary:", "Get the time entries of a gopher");
		System.out.printf("%-20s %-20s\n", "q, quit:", "Quit Gopher Check");
		System.out.printf("%-20s %-20s\n", "help:", "Print this help message");
	}

	private static void listen(MfCardListener listener) throws IOException {
		AcsTerminal acr122;
		try {
			acr122 = new AcsTerminal();
			acr122.setCardTerminal(CardTerminalUtils.getTerminalByName("ACR122"));
			Acr122ReaderWriter rw = new Acr122ReaderWriter(acr122);
			rw.waitForCard(listener, 10000);
			acr122.open();
		}
		catch(RuntimeException ex) {
			// TODO: fix this before release
			System.out.println("Fuck");
			return;
		}
		acr122.close();
	}

	private static void writeTag(String badgeNumber) throws IOException {
		MfCardListener listener = new MfCardListener() {
			@Override
			public void cardDetected(MfCard mfCard, MfReaderWriter mfReaderWriter) throws IOException {
				try {
					if(BadgeUtils.writeBadgeBlock(mfReaderWriter, mfCard, badgeNumber)) {
						System.out.println("Write success");
					}
					else {
						System.out.println("Write Failure");
					}
				} catch(CardException ex) {
					System.out.println("Card failed to read");
				}
			}
		};
		listen(listener);
	}

	private static String readTag() throws IOException {
		String id = "";
		MfCardListener listener = new MfCardListener() {
			@Override
			public void cardDetected(MfCard mfCard, MfReaderWriter mfReaderWriter) throws IOException {
				try {
					idStorage = BadgeUtils.readBadgeBlock(mfReaderWriter, mfCard);
				} catch(CardException ex) {
					System.out.println("Card failed to read");
				}
			}
		};
		listen(listener);

		// This is the definition of cancer. Figure out a real way to do this
		id = idStorage;
		idStorage = "";
		return id;
	}
}
