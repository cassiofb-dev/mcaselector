package net.querz.mcaselector.text;

import net.querz.mcaselector.debug.Debug;
import net.querz.mcaselector.exception.ParseException;
import net.querz.mcaselector.filter.PaletteFilter;
import net.querz.mcaselector.io.StringPointer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextHelper {

	private TextHelper() {}

	private static final Map<Pattern, Long> DURATION_REGEXP = new HashMap<>();

	static {
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:years?|y)"), 31536000L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:months?)"), 2628000L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:days?|d)"), 90000L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:hours?|h)"), 3600L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:minutes?|mins?)"), 60L);
		DURATION_REGEXP.put(Pattern.compile("(?<data>\\d+)\\W*(?:seconds?|secs?|s)"), 1L);
	}

	private static final Set<String> validBLockNames = new HashSet<>();

	static {
		try (BufferedReader bis = new BufferedReader(
				new InputStreamReader(Objects.requireNonNull(PaletteFilter.class.getClassLoader().getResourceAsStream("mapping/all_block_names.txt"))))) {
			String line;
			while ((line = bis.readLine()) != null) {
				validBLockNames.add(line);
			}
		} catch (IOException ex) {
			Debug.dumpException("error reading mapping/all_block_names.txt", ex);
		}
	}

	public static String[] parseBlockNames(String raw) {
		StringPointer sp = new StringPointer(raw);
		List<String> blocks = new ArrayList<>();
		try {
			while (sp.hasNext()) {
				sp.skipWhitespace();
				String rawName;
				if (sp.currentChar() == '\'') {
					rawName = "'" + sp.parseQuotedString('\'') + "'";
				} else {
					rawName = sp.parseSimpleString(TextHelper::isValidBlockChar);
				}

				String parsedName = parseBlockName(rawName);
				if (parsedName == null) {
					return null;
				}
				blocks.add(parsedName);
				sp.skipWhitespace();
				if (sp.hasNext()) {
					sp.expectChar(',');
					sp.skipWhitespace();
					if (!sp.hasNext()) {
						return null;
					}
				}
			}
		} catch (Exception ex) {
			return null;
		}
		return blocks.toArray(new String[0]);
	}

	private static boolean isValidBlockChar(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'
				|| c >= '0' && c <= '9'
				|| c == ':' || c == '_' || c == ' ';
	}

	public static String parseBlockName(String raw) {
		raw = raw.replace(" ", "");
		if (raw.startsWith("minecraft:")) {
			if (validBLockNames.contains(raw.substring(10))) {
				return raw;
			}
		} else if (validBLockNames.contains(raw)) {
			return "minecraft:" + raw;
		} else if (raw.startsWith("'") && raw.endsWith("'")) {
			return raw.substring(1, raw.length() - 1);
		}
		return null;
	}

	// parses a duration string and returns the duration in seconds
	public static long parseDuration(String d) {
		boolean result = false;
		int duration = 0;
		for (Map.Entry<Pattern, Long> entry : DURATION_REGEXP.entrySet()) {
			Matcher m = entry.getKey().matcher(d);
			if (m.find()) {
				duration += Long.parseLong(m.group("data")) * entry.getValue();
				result = true;
			}
		}
		if (!result) {
			throw new IllegalArgumentException("could not parse anything from duration string");
		}
		return duration;
	}

	private static final DateTimeFormatter TIMESTAMP_FORMAT =
			new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd[ [HH][:mm][:ss]]")
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
					.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
					.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
					.toFormatter();

	private static final ZoneId ZONE_ID = ZoneId.systemDefault();

	public static int parseTimestamp(String t) {
		String trim = t.trim();
		try {
			LocalDateTime date = LocalDateTime.parse(trim, TIMESTAMP_FORMAT);
			ZonedDateTime zdt = ZonedDateTime.of(date, ZONE_ID);
			return (int) zdt.toInstant().getEpochSecond();
		} catch (DateTimeParseException e) {
			Debug.dump(e.getMessage());
		}
		throw new IllegalArgumentException("could not parse date time");
	}

	public static Integer parseInt(String s, int radix) {
		try {
			return Integer.parseInt(s, radix);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public static String byteToBinaryString(byte b) {
		StringBuilder s = new StringBuilder(Integer.toBinaryString(b & 0xFF));
		for (int i = s.length(); i < 8; i++) {
			s.insert(0, "0");
		}
		return s.toString();
	}

	public static String intToBinaryString(int n) {
		StringBuilder s = new StringBuilder(Integer.toBinaryString(n));
		for (int i = s.length(); i < 32; i++) {
			s.insert(0, "0");
		}
		return s.toString();
	}

	public static String longToBinaryString(long l) {
		StringBuilder s = new StringBuilder(Long.toBinaryString(l));
		for (int i = s.length(); i < 64; i++) {
			s.insert(0, "0");
		}
		return s.toString();
	}

	public static String longToBinaryString(long l, int div) {
		StringBuilder s = new StringBuilder(Long.toBinaryString(l));
		for (int i = s.length(); i < 64; i++) {
			s.insert(0, "0");
		}

		for (int i = 64 - div; i > 0; i -= div) {
			s.insert(i, "_");
		}

		return s.toString();
	}

	public static String getStacktraceAsString(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}
}
