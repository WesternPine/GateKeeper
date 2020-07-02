package dev.westernpine.common.dates;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

public class Dates {
    
    /*
     * Date formatter
     */
    @Getter
    private static final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    /*
     * Returns the current date in GregorianCalender format.
     */
    public static Date getCurrentDate() {
        long time = System.currentTimeMillis();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        return calendar.getTime();
    }
    
    /*
     * Returns the date in string format.
     */
    public static String getDateString(Date date){
        return df.format(date);
    }
    
    /*
     * Returns the date object from string format.
     */
    public static Date getDateFromString(String date){
        Date specifiedDate = new Date();
        try{
            specifiedDate = df.parse(date);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("DATE UNABLE TO BE FORMATTED!");
        }
        return specifiedDate;
    }
    
    /*
     * Returns the difference in milliseconds between dates.
     * The difference = from-to.
     */
    public static long calculateDifference(Date from, Date to){
        Long f = from.getTime();
        Long t = to.getTime();
        return f-t;
        
        
    }
    
    /*
     * Returns a date, in a specified amount of the specified time units, before the specified date.
     */
    public static Date getDateTimeUnitsBefore(Date date, TimeUnit unit, long amount) {
        date.setTime(date.getTime() - unit.toMillis(amount));
        return date;
    }
    
    /*
     * Returns a date, in a specified amount of the specified time units, after the specified date.
     */
    public static Date getDateTimeUnitsAfter(Date date, TimeUnit unit, long amount) {
        date.setTime(date.getTime() + unit.toMillis(amount));
        return date;
    }
    
}