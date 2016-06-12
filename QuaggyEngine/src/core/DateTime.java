package core;



/** A useful class for storing timestamp information.
 *  Designed to be converted from the timestamp format
 *  used by GW2 APIs, or 2015-12-25 11:11:11.
 *  
 *  When using the interval functionality, this class supports
 *  negative time intervals by placing ~ before a value.
 *  This is not supported for absolute times. */
public class DateTime implements Comparable<DateTime>{
	
	//a tiny amount of time
	public static final DateTime DELTA_TIME = new DateTime("0000-00-00 00:00:01");
	
	public int day, month, year;
	public int hour, minute, second;
	
	//Quick references for the time at which Java's system clock begins
	public static final DateTime ORIGIN_UTC = new DateTime("1970-01-01 00:00:00");
	public static final DateTime ORIGIN_EST = ORIGIN_UTC.toEastern();
	
	/** Create a DateTime object from the relevant information */
	public DateTime(int year, int month, int day, int hour, int minute, int second) {
		this.year = year; this.month = month; this.day = day;
		this.hour = hour; this.minute = minute; this.second = second;
	}
	
	/** Create a DateTime object from a GW2Spidy timestamp String.
	 *  Takes string of the form 2015-12-25 11:11:11 */
	public DateTime (String timestamp) {
		try{
			String[] pieces = timestamp.split(" ");
			String[] datePieces = pieces[0].split("-");
			String[] timePieces = pieces[1].split(":");
			this.year = parseInt(datePieces[0]);
			this.month = parseInt(datePieces[1]);
			this.day = parseInt(datePieces[2]);
			this.hour = parseInt(timePieces[0]);
			this.minute = parseInt(timePieces[1]);
			this.second = parseInt(timePieces[2]);
		}
		catch (Exception e) {
			System.out.println("Error initializing DateTime from timestamp : " + timestamp);
			e.printStackTrace();
		}
	}
	
	/** Copy constructor. */
	public DateTime(DateTime other) {
		this(other.generateTimestamp());
	}
	
	/** Parses an integer, assuming that if the first character is ~
	 *  we will make the integer negative.
	 *  
	 *  Can throw NumberFormatException in the case of failure.
	 */
	public int parseInt(String input) {
		if (input.charAt(0) == '~') {
			return -1 * Integer.parseInt(input.substring(1, input.length()));
		}
		else return Integer.parseInt(input);
	}
	
	/** Formats an integer to include a ~ before negatives. */
	private String format(int val) {
		if (val < 0) return "~" + Math.abs(val);
		else return "" + val;
	}
	
	/** Generate a string timestamp from this DateTime object. */
	public String generateTimestamp() {
		return format(year) + "-" + format(month) + "-" + format(day) + " " + 
				format(hour) + ":" + format(minute) + ":" + format(second);
	}
	
	@Override
	public String toString() {
		return generateTimestamp();
	}
	
	/** Returns true iff this date is within the range provided, inclusive. */
	public boolean inRange(DateTime low, DateTime high) {
		return this.compareTo(low) >= 0 && this.compareTo(high) <= 0;
	}
	
	/** Compare, such that older date/times are less than more recent ones. */
	@Override
	public int compareTo(DateTime that) {
		int cmpDate = compareDate(that);
		if (cmpDate != 0) return cmpDate;
		return compareTime(that);
	}
	
	
	/** Compares two objects solely on the basis of time of day (ignoring date) */
	public int compareTime(DateTime that) {
		if (this.hour < that.hour) return -1;
		if (this.hour > that.hour) return 1;
		if (this.minute < that.minute) return -1;
		if (this.minute > that.minute) return 1;
		if (this.second < that.second) return -1;
		if (this.second > that.second) return 1;
		return 0;
	}
	
	/** Compares two objects solely on the basis of date (ignoring time of day) */
	public int compareDate(DateTime that) {
		if (this.year < that.year) return -1;
		if (this.year > that.year) return  1;
		if (this.month < that.month) return -1;
		if (this.month > that.month) return 1;
		if (this.day < that.day) return -1;
		if (this.day > that.day) return 1;
		return 0;
	}
	
	/** Compare whether two objects are equal. */
	public boolean equals(DateTime that) {
		return this.toString().equals(that.toString());
	}
	
	/** Computes the standard modulus. */
	private static int mod(int a, int b) {
		int res = (a % b);
		if (res < 0) res = b + res;
		return res;
	}
	/** Computes a div useful for datetimes. */
	private static int div(int a, int b) {
		int res = (a / b);
		if (a < 0) res -= 1;
		return res;
	}
	
	/** Inverts this DateTime, returning a new DateTime
	 * that is identical but with all values their negatives. */
	public DateTime invert() {
		DateTime newTime = new DateTime(this);
		newTime.year *= -1;
		newTime.hour *= -1;
		newTime.day *= -1;
		newTime.hour *= -1;
		newTime.minute *= -1;
		newTime.second *= -1;
		return newTime;
	}
	
	/** Adds time delta to our current time. Will add time units starting from left.
	 *  This means add seconds first, then minutes, etc, etc. 
	 */
	public DateTime add(DateTime delta) {
		int newSecond = mod(this.second + delta.second, 60);
		int minuteCarry = div(this.second + delta.second, 60);
		int newMinute = mod(this.minute + delta.minute + minuteCarry, 60);
		int hourCarry = div(this.minute + delta.minute + minuteCarry, 60);
		int newHour = mod(this.hour + delta.hour + hourCarry, 24);
		int dayCarry = div(this.hour + delta.hour + hourCarry, 24);
		int newDay = this.day + delta.day + dayCarry;
		//Subtract the days of the month until we get under the desired amount of days for our month
		int newMonth = this.month;
		int yearCarry = 0;
		while (newDay > numDays(newMonth, this.year + yearCarry)) {
			newDay -= numDays(newMonth, this.year + yearCarry);
			yearCarry += (newMonth / 12);
			newMonth = newMonth % 12 + 1;
		}
		//Add the days of the month until we get to at least the first of the month
		while (newDay < 1) {
			newMonth = mod(newMonth - 2, 12) + 1;
			newDay += numDays(newMonth, this.year + yearCarry);
			yearCarry -= (newMonth / 12);
		}
		newMonth += delta.month;
		yearCarry += div(newMonth - 1, 12);
		newMonth = mod(newMonth - 1, 12) + 1;
		int newYear = this.year + delta.year + yearCarry;
		return new DateTime(newYear + "-" + newMonth + "-" + newDay + " " + 
				newHour + ":" + newMinute + ":" + newSecond);
	}
	
	/** Convert a time from UTC to Eastern. (-5 hours) */
	public DateTime toEastern() {
		return this.add(new DateTime("00-00-00 ~5:00:00"));
	}
	
	/** Convert a time from Eastern to UTC (+5 hours) */
	public DateTime toUTC() {
		return this.add(new DateTime("00-00-00 05:00:00"));
	}
	
	/** Reference returning the number of days in a month. 
	 	Attempts to take into account leap years.*/
	public static int numDays(int month, int year) {
		//Leap year considerations
		if (month == 2) {
			if (year % 4 == 0 && (year % 100 != 0 || year % 1000 == 0)) return 29;
			else return 28;
		}
		//Generic months
		if (month == 9 || month == 4 || month == 6 || month == 11) return 30;
		return 31;
	}
	
	/** Given a year, returns the number of days in that year.
	 * Accounts for leap years.
	 */
	public static int yearDays(int year) {
		if (year % 4 == 0 && (year % 100 != 0 || year % 1000 == 0)) return 366;
		return 365;
	}
	
	/** Gets the current timestamp, in EST. */
	public static DateTime current() {
		long millis = System.currentTimeMillis();
		long seconds = millis / 1000;
		int hours = (int)(seconds / 3600);
		int leftOverSeconds = (int)(seconds % 3600);
		int leftOverMinutes = leftOverSeconds / 60;
		leftOverSeconds = leftOverSeconds % 60;
		String toAdd = "00-00-00 " + hours + ":" + leftOverMinutes + ":" + leftOverSeconds;
		return ORIGIN_EST.add(new DateTime(toAdd));
	}
	
	/** Computes the number of days in between two dates. Assumes b is later than a. */
	public static int daysBetween(DateTime a, DateTime b) {
		a = new DateTime(a);
		if (a.compareTo(b) > 0) {
			throw new IllegalArgumentException("daysBetween : b must be later than a");
		}
		int days = 0;
		DateTime delta = new DateTime("00-00-01 00:00:00");
		while (a.compareDate(b) < 0) {
			days++;
			a = a.add(delta);
		}
		return days;
	}
}
