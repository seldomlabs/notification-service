package com.notification.constants;

import com.notification.util.TimeUtils;

import java.util.Date;

public enum Day {
    SUNDAY(0,DayType.WEEKEND),MONDAY(1,DayType.WEEKDAY),TUESDAY(2,DayType.WEEKDAY),WEDNESDAY(3,DayType.WEEKDAY),THURSDAY(4,DayType.WEEKDAY),FRIDAY(5,DayType.WEEKEND),SATURDAY(6,DayType.WEEKEND);

    public enum DayType{
        WEEKDAY,WEEKEND
    }

    private int dayNumber;
    private DayType dayType;

    private Day(int dayNumber, DayType dayType){
        this.dayNumber = dayNumber;
        this.dayType = dayType;
    }

    public int getDayNumber(){
        return dayNumber;
    }
    public DayType getDayType() {
        return dayType;
    }

    public static Day getDayByNumber(int daysNumber){
        for (Day day : Day.values()) {
            if (day.getDayNumber() == daysNumber)
                return day;
        }
        return null;
    }

    public static Day getPreviousDay(Day day){
        if(day==null) return null;
        int dayNumber = day.getDayNumber();
        int previousDayNumber = dayNumber-1;
        if(previousDayNumber== -1){
            return Day.SATURDAY;
        }
        return getDayByNumber(previousDayNumber);
    }

    public static Day getNextDay(Day day){
        if(day==null) return null;
        int dayNumber = day.getDayNumber();
        int nextDayNumber = dayNumber+1;
        if(nextDayNumber == 7){
            return Day.SUNDAY;
        }
        return getDayByNumber(nextDayNumber);
    }

    public static Day getDayByDate(Date date){
        if(date==null) return null;
        return Day.valueOf(TimeUtils.getDayOfWeek(date).toUpperCase());
    }

    public boolean isWeekDay(){
        Day day = getDayByNumber(this.dayNumber);
        return day != null && day.dayType.equals(DayType.WEEKDAY);
    }

    public boolean isWeekend(){
        Day day = getDayByNumber(this.dayNumber);
        return day != null && day.dayType.equals(DayType.WEEKEND);
    }
}
