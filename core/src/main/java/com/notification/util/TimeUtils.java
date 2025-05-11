
package com.notification.util;

import com.notification.common.exception.ApplicationException;
import com.notification.constants.TimePeriod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

	private static final Logger logger = LogManager.getLogger(TimeUtils.class);

	/**
	 * Returns a 2-length array of String dates, timezone:IST index[0] :
	 * fromDate index[1] : toDate
	 * 
	 * @param period
	 * @return
	 */
	public static String[] getTimeString(TimePeriod period) {
		ZoneId ist = ZoneId.of(TimePeriod.INDIAN_TIMEZONE);
		ZonedDateTime now = ZonedDateTime.of(LocalDateTime.now(), ist);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String toDate = now.plusDays(1).format(formatter);
		String fromDate = "";
		switch (period) {
		case ALL_TIME:
			fromDate = now.minusYears(100).format(formatter);
			break;
		case TODAY:
			fromDate = now.format(formatter);
			break;
		case THIS_WEEK:
			fromDate = now.minusWeeks(1).format(formatter);
			break;
		case THIS_MONTH:
			fromDate = now.minusMonths(1).format(formatter);
			break;
		case THIS_QUARTER:
			fromDate = now.minusMonths(3).format(formatter);
			break;
		case THIS_YEAR:
			fromDate = now.minusYears(1).format(formatter);
			break;
		default:
			fromDate = now.format(formatter);
			break;
		}
		return new String[] { fromDate, toDate };
	}

	public static Date convertLocalDateTimeToUtilDate(LocalDateTime ldt, ZoneId z) {
		return Date.from(ldt.atZone(z).toInstant());
	}

	public static LocalDateTime convertUtilDateToLocalDateTime(Date date, ZoneId z) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), z);
	}

	public static Date getCurrentDate() {
		// return
		// Date.from(LocalDateTime.now().atZone(ZoneId.of(TimePeriod.INDIAN_TIMEZONE)).toInstant());
		return new Date();
	}

	/**
	 * Return time difference in milliseconds from scheduled date and now
	 * 
	 * @param scheduledDate
	 * @return
	 */
	public static long getTimeDifference(Date scheduledDate) {
		return scheduledDate.getTime() - new Date().getTime();
	}

	/**
	 * Return time difference between from and to dates in milliseconds
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public static long getTimeDifference(Date fromDate, Date toDate) {
		return toDate.getTime() - fromDate.getTime();
	}

	public static String getDayOfWeek() {
		return new SimpleDateFormat("EEEE").format(new Date());
	}
	
	public static String getDayOfWeek(Date date) {
		return new SimpleDateFormat("EEEE").format(date);
	}

	/**
	 * Checks if <b>now</b> (current time is within range <b><i>from, to</i></b>
	 * If any of the from, to is null, open ended interval is assumed
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean withinRangeSafe(Date from, Date to) {
		Date now = new Date();
		if (from == null && to == null)
			return true;
		if (from == null)
			return now.before(to);
		if (to == null)
			return from.before(now);
		return from.before(now) && now.before(to);
	}

	public static Date safeParse(String s) {
		return safeParse(s, "yyyy-MM-dd HH:mm:ss");
	}

	public static Date safeParse(String s, String format) {
		if (StringUtils.isBlank(s)) {
			return null;
		} else {
			try {
				return new SimpleDateFormat(format).parse(s);
			} catch (ParseException e) {
				logger.error("Date Parse exception", e);
				return null;
			}
		}
	}

	public static long getTimeDifferenceMillisFromNow(long tv, TimeUnit tu) {
		long t = System.currentTimeMillis();
		switch (tu) {
		case DAYS:
			return t - TimeUnit.DAYS.toMillis(tv);
		case HOURS:
			return t - TimeUnit.HOURS.toMillis(tv);
		case MINUTES:
			return t - TimeUnit.MINUTES.toMillis(tv);
		case SECONDS:
			return t - TimeUnit.SECONDS.toMillis(tv);
		case MILLISECONDS:
			return t - TimeUnit.MILLISECONDS.toMillis(tv);
		case MICROSECONDS:
			return t - TimeUnit.MICROSECONDS.toMillis(tv);
		case NANOSECONDS:
			return t - TimeUnit.NANOSECONDS.toMillis(tv);
		default:
			throw new RuntimeException("Should not reach here, illegal timeunit: " + tu.toString());
		}
	}

	public static Date addWorkingdays(Date date, int numberOfDays){
		int i=0;
		while(i < numberOfDays)
		{
			date = DateUtils.addDays(date,1);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			System.out.println(dayOfWeek);
			if(dayOfWeek != 7 && dayOfWeek != 1)
			{
				i++;
			}
		}
		return date;
	}

	public static int diffInWorkingDays(Date date1, Date date2){
		int i=0;
		while(date1.compareTo(date2) < 0)
		{
			date1 = DateUtils.addDays(date1,1);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date1);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if(dayOfWeek != 7 && dayOfWeek != 1)
			{
				i++;
			}
		}
		return i;
	}

	public static Date getDateWithTime(Date date, String timeString) throws ApplicationException {
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		try {
			Date time = timeFormat.parse(timeString);
			Calendar timeCalendar = Calendar.getInstance();
			timeCalendar.setTime(time);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));

			return calendar.getTime();
		} catch (ParseException e) {
			throw new ApplicationException("Failed to parse the timeString : " + timeString);
		}
	}

	public Date atEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public static Date atStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		getTimeString(TimePeriod.TODAY);
		getTimeString(TimePeriod.THIS_WEEK);
		getTimeString(TimePeriod.THIS_MONTH);
		getTimeString(TimePeriod.THIS_QUARTER);
		getTimeString(TimePeriod.THIS_YEAR);
		getTimeString(TimePeriod.ALL_TIME);
		System.out.println(addWorkingdays(new Date(), 3));
		System.out.println(getDayOfWeek());
		System.out.println(getCurrentDate().toLocaleString());
		String s = "2016-11-15 13:09:56";
		Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s);
		System.out.println(d);
	}
}
