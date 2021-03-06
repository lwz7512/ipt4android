package com.ybcx.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.ybcx.R;
import com.ybcx.PintuApp;

import android.content.Context;
import android.util.Log;


public class DateTimeHelper {
    private static final String TAG = "DateTimeHelper";

    // Wed Dec 15 02:53:36 +0000 2010
    public static final DateFormat TWITTER_DATE_FORMATTER = new SimpleDateFormat(
            "E MMM d HH:mm:ss Z yyyy", Locale.US);

    public static final DateFormat TWITTER_SEARCH_API_DATE_FORMATTER = new SimpleDateFormat(
            "E, d MMM yyyy HH:mm:ss Z", Locale.US); // TODO: Z -> z ?

    public static final Date parseDateTime(String dateString) {
        try {
            Log.v(TAG, String.format("in parseDateTime, dateString=%s", dateString));
            return TWITTER_DATE_FORMATTER.parse(dateString);
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse Twitter date string: " + dateString);
            return null;
        }
    }

    // Handle "yyyy-MM-dd'T'HH:mm:ss.SSS" from sqlite
    public static final Date parseDateTimeFromSqlite(String dateString) {
        try {
            Log.v(TAG, String.format("in parseDateTime, dateString=%s", dateString));
            return null;
            //FIXME,...
//            return TwitterDatabase.DB_DATE_FORMATTER.parse(dateString);
        } catch (Exception e) {
            Log.w(TAG, "Could not parse Twitter date string: " + dateString);
            return null;
        }
    }

    public static final Date parseSearchApiDateTime(String dateString) {
        try {
            return TWITTER_SEARCH_API_DATE_FORMATTER.parse(dateString);
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse Twitter search date string: "
                    + dateString);
            return null;
        }
    }

    public static final DateFormat AGO_FULL_DATE_FORMATTER = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public static String getRelativeDate(Date date) {
        Date now = new Date();

        Context mctx = PintuApp.mContext;
//        String prefix = mctx.getString(R.string.tweet_created_at_beautify_prefix);
        String prefix = "";
        String sec = mctx.getString(R.string.tweet_created_at_beautify_sec);
        String min = mctx.getString(R.string.tweet_created_at_beautify_min);
        String hour = mctx.getString(R.string.tweet_created_at_beautify_hour);
        String day = mctx.getString(R.string.tweet_created_at_beautify_day);
        String suffix = mctx.getString(R.string.tweet_created_at_beautify_suffix);
               
        // Seconds.
        long diff = (now.getTime() - date.getTime()) / 1000;

        if (diff < 0) {
            diff = 0;
        }

        if (diff < 60) {
            return diff + sec + suffix;
        }

        // Minutes.
        diff /= 60;

        if (diff < 60) {
            return  diff + min + suffix;
        }

        // Hours.
        diff /= 60;

        if (diff < 24) {
            return  diff + hour + suffix;
        }
        
        diff /=24;

        return diff+day+suffix;
    }
    
    //新加的整合格式方法，字符串到字符串
    //lwz7512 @ 2011/08/24
    public static  String getRelativeTimeByFormatDate(String publishTime) throws ParseException{
    	Date pubDate = AGO_FULL_DATE_FORMATTER.parse(publishTime);
    	return getRelativeDate(pubDate);
    }

    public static long getNowTime() {
        return Calendar.getInstance().getTime().getTime();
    }
    
    public static String longTimeToDate(long miliseconds){
    	Date dt = new Date(miliseconds);
    	return AGO_FULL_DATE_FORMATTER.format(dt);
    }
    
}
