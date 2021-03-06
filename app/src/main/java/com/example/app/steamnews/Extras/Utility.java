package com.example.app.steamnews.Extras;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

import com.example.app.steamnews.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility {

    public static String getPreferredGame(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_game_key),
                context.getString(R.string.pref_game_default));
    }

    public static String getReadableDateString(String timeStr) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = getDateFromDb(timeStr);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d - yyyy");
        return format.format(date);
    }

    public static String getOldDate(String dateStr)
    {
        Date date = getDateFromDb(dateStr);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(date);
    }

    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Converts Date class to a string representation, used for easy comparison and database lookup.
     *
     * @param date The input date
     * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
     */
    public static String getDbDateString(Date date) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    /**
     * Converts a dateText to a long Unix time representation
     *
     * @param dateText the input date string
     * @return the Date object
     */
    public static Date getDateFromDb(String dateText) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dbDateFormat.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFriendlyDayString(Context context, String dateStr) {
        // The day string for news uses the following logic:
        // For today: "Today, June 8"
        // For the next 5 days: (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        String todayStr = getDbDateString(todayDate);
        Date inputDate = getDateFromDb(dateStr);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (todayStr.equals(dateStr)) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(dateStr)));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, -7);

            String weekPastString = getDbDateString(cal.getTime());
            if (dateStr.compareTo(weekPastString) > 0) {
                // If the input date is less than a week in the past, just return the day name.
                return getDayName(context, dateStr);
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(todayDate);
                calendar.add(Calendar.DATE, -30);
                String monthPastString = getDbDateString(calendar.getTime());
                if (dateStr.compareTo(monthPastString) > 0){
                    //use the form "Mon Jun 3"
                    SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                    return shortenedDateFormat.format(inputDate);
                } else {
                    // Otherwise, use the form "10/12/2014"
                    return getOldDate(dateStr);
                }
            }

        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     *
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
            return monthDayFormat.format(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateStr The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return Day Name
     */
    public static String getDayName(Context context, String dateStr) {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        try {
            Date inputDate = dbDateFormat.parse(dateStr);
            Date todayDate = new Date();
            // If the date is today, return the localized version of "Today" instead of the actual
            // day name.
            if (getDbDateString(todayDate).equals(dateStr)) {
                return context.getString(R.string.today);
            } else {
                // If the date is set for yesterday, the format is "Yesterday".
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, -1);
                Date yesterdayDate = cal.getTime();
                if (getDbDateString(yesterdayDate).equals(
                        dateStr)) {
                    return context.getString(R.string.yesterday);
                } else {
                    // Otherwise, the format is just the day of the week (e.g "Wednesday".
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    return dayFormat.format(inputDate);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // It couldn't process the date correctly.
            return "";
        }
    }

    //To escape the html tags in the news contents
    public static String removeHtml(String html) {
        String htmlWithoutImg = html ;
        int i = 0; //i -> to avoid infinite loop
        while(htmlWithoutImg.contains("<img") && i<5) {
                int start = htmlWithoutImg.indexOf("<img");
                String content = htmlWithoutImg.substring(start);
                int end = content.indexOf(">") + 1;

                String htmlImgTag = content.substring(0, end);
                htmlWithoutImg = htmlWithoutImg.replace(htmlImgTag,"");
            i++;
        }
        return Html.fromHtml(htmlWithoutImg).toString();
    }


    //Finds images url
    public static String findUrl(String content) {
        String imageUrl = "image not found";
        if(content.contains("<img"))
        {
            int start = content.indexOf("<img");
            String content2 = content.substring(start);
            int end = content2.indexOf(">") ;

            String imageTag = content2.substring(0, end);

            if(imageTag.contains(" src=")){

                int start2 = imageTag.indexOf(" src=") + 6;
                String imageSource = imageTag.substring(start2);
                int end2 = imageSource.indexOf('"') ;
                imageUrl = imageSource.substring(0,end2);
                Log.e("url",imageUrl);
            }
            else
            {
                return "image not found";
            }
        }
        return imageUrl ;
    }

    public static String selectText(Context context, String GameID){
       String[] games =  context.getResources().getStringArray(R.array.pref_list_game_ids);
       String[] gameIds =  context.getResources().getStringArray(R.array.pref_list_game_ids_values);

        for(int i = 0; i<gameIds.length ; i++)
        {
            if(gameIds[i].equals(GameID)){
                return games[i];
            }
        }
        return "no_match";
    }

    public static int selectIcon(String GameID){
        switch (GameID) {
            case "570":
                return R.drawable.dota_2_icon_1;
            case "440":
                return R.drawable.team_fortress_icon_3;
            case "400":
                return R.drawable.portal_icon_2;
            case "620":
                return R.drawable.portal_2_icon_2;
            case "550":
                return R.drawable.left_4_dead_icon_1;
            case "72850":
                return R.drawable.elder_scrolls_v_skyrim_icon;
            case "8930":
                return R.drawable.civilization_v_icon_1;
            case "238960":
                return R.drawable.path_of_exile_icon;
            default:
                return 0;
        }
    }
}


