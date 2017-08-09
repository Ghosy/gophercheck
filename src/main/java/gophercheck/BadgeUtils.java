package gophercheck;

import java.io.IOException;
import javax.smartcardio.CardException;
import org.nfctools.mf.MfAccess;
import org.nfctools.mf.MfException;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.block.BlockResolver;
import org.nfctools.mf.block.MfBlock;
import org.nfctools.mf.card.MfCard;
import org.nfctools.mf.classic.Key;
import org.nfctools.mf.classic.MemoryLayout;

// The current version of this code is only compatible with Mifare Classic 1K

public final class BadgeUtils {

	// Should not exceed 15 for Mifare Classic 1K
	private static final int BADGE_SECTOR = 0;
	// Should not exceed 2 for Mifare Classic 1K
	private static final int BADGE_BLOCK = 1;
	// Default key for Mifare Classic
	private static final String DEFAULT_KEY = "FFFFFFFFFFFF";

	public static String readBadgeBlock(MfReaderWriter reader, MfCard card) throws CardException {
		byte[] keyBytes = HexUtils.hexStringToBytes(DEFAULT_KEY);
		MfAccess access = new MfAccess(card, BADGE_SECTOR, BADGE_BLOCK, Key.A, keyBytes);
		return readBlock(reader, access);
	}

	private static String readBlock(MfReaderWriter reader, MfAccess access) throws CardException {
		String data = null;
		try {
			MfBlock block = reader.readBlock(access)[0];
			data = HexUtils.bytesToHexString(block.getData());
		} catch(IOException ex) {
			if(ex.getCause() instanceof CardException) {
				throw (CardException) ex.getCause();
			}
		}
		return data;
	}

	public static boolean writeBadgeBlock(MfReaderWriter reader, MfCard card, String BadgeNumber) throws CardException {
		boolean written = false;

		if(BadgeNumber.length() != 9) {
			System.out.println(BadgeNumber + " is not a valid badge number");
		}

		// Establish badge location in memory
		byte[] keyBytes = HexUtils.hexStringToBytes(DEFAULT_KEY);
		MfAccess access = new MfAccess(card, BADGE_SECTOR, BADGE_BLOCK, Key.A, keyBytes);

			// Establish block to be put in memory
			byte[] data = HexUtils.hexStringToBytes("00000000000000000000000" + BadgeNumber);
		try {
			MfBlock block = BlockResolver.resolveBlock(MemoryLayout.CLASSIC_1K, BADGE_SECTOR, BADGE_BLOCK, data);
			try {
				reader.writeBlock(access, block);
				written = true;
			} catch(IOException ex) {
				if(ex.getCause() instanceof CardException) {
					throw (CardException) ex.getCause();
				}
			}
		} catch(MfException ex) {
			System.out.println(ex.getMessage());
		}

		return written;
	}

	public static boolean isValidID(String id) {
		return (id.matches("[0-9]+") && id.length() == 9);
	}

	public static String trimID(String id) {
		if(id.length() < 9) {
			return id;
		}
		else {
			return id.substring(id.length() - 9);
		}
	}

	public static String trimIDStaff(String id) {
		if(id.length() < 3) {
			return id;
		}
		else {
			return id.substring(id.length() - 3);
		}
	}
}
