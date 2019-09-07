import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A static class that generates Tasks based on the user's input.
 */
public class TextToTaskTranslator {
    /*A Pattern for the format "dd/mm/yyyy" or "d/m/yy"
      (Day and Month have either 1 or 2 digits, Year has either 2 or 4 digits)*/
    private static final Pattern DATE_FORMAT_PATTERN = Pattern.compile("\\b\\d{1,2}\\/\\d{1,2}\\/(\\d{2}|\\d{4})\\b");

    //A Pattern for the format "hhmm". Will not clash with the 4 digits for year in DATE_FORMAT_PATTERN
    private static final Pattern TIME_FORMAT_PATTERN = Pattern.compile("(?<!\\/)\\d{4}\\b");

    private static final int [] DAYS_EACH_MONTH = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    private static final String [] NAMES_EACH_MONTH 
        = { "January", "Februrary", "March", "April", "May", "June", 
            "July", "August",  "September", "October", "November", "December" };

    /**
     * Generates a <code>ToDoTask</code> from the user's input.
     * 
     * @return The generated <code>ToDoTask</code>.
     * @throws DukeException The user's input lacks a <code>Task</code> description.
     */
    public static Task translateToDoTask(String userInputString) throws DukeException {
        String description = userInputString.substring(4).trim();
        checkDescriptionExists(description);
        return new ToDoTask(description);
    }

    /**
     * Generates a <code>DeadlineTask</code> from the user's input.
     * 
     * @return The generated <code>DeadlineTask</code>.
     * @throws DukeException The user's input command is incomplete, lacks a description or includes an invalid date.
     */
    public static Task translateDeadlineTask(String userInputString) throws DukeException {
        String withoutCommand = userInputString.substring(8).trim();

        if(!withoutCommand.equals("") && withoutCommand.contains("/by")) {
            String [] splitString = withoutCommand.split("/by");

            checkDescriptionExists(splitString);
            String description = splitString[0].trim();

            LocalDate deadlineDate = null;
            LocalTime deadlineTime = null;

            if(splitString.length == 2) {
                String timeSpecifications = splitString[1];

                deadlineDate = extractDateFromString(timeSpecifications);
                deadlineTime = extractTimeFromString(timeSpecifications);
            }

            return new DeadlineTask(description, new DukeDateTime(deadlineDate, deadlineTime));
        } else {
            throw new DukeException(DukeTextFormatter.makeFormattedText(
                String.format(DukeUi.ERROR_INCOMPLETE_COMMAND, "deadline")));
        }
    }

    /**
     * Generates an <code>EventTask</code> from the user's input.
     * 
     * @return The generated <code>EventTask</code>.
     * @throws DukeException The user's input command is incomplete, lacks a description or includes an invalid date.
     */
    public static Task translateEventTask(String userInputString) throws DukeException {
        String withoutCommand = userInputString.substring(5).trim();

        if(!withoutCommand.equals("") && withoutCommand.contains("/at")) {
            String [] splitString = withoutCommand.split("/at");

            checkDescriptionExists(splitString);
            String description = splitString[0].trim();

            LocalDate startDate = null;
            LocalTime startTime = null;
            LocalDate endDate = null;
            LocalTime endTime = null;

            if(splitString.length == 2) {
                boolean bothStartAndEndDateExist
                    = splitString[1].contains("to") && !splitString[1].endsWith("to");

                if(bothStartAndEndDateExist) {
                    String [] timeSpecifications = splitString[1].split("to");

                    startDate = extractDateFromString(timeSpecifications[0]);
                    startTime = extractTimeFromString(timeSpecifications[0]);
                    endDate = extractDateFromString(timeSpecifications[1]);
                    endTime = extractTimeFromString(timeSpecifications[1]);
                } else {
                    String timeSpecifications = splitString[1];

                    startDate = extractDateFromString(timeSpecifications);
                    startTime = extractTimeFromString(timeSpecifications);
                }
            }

            DukeDateTime startDateTime = new DukeDateTime(startDate, startTime);
            DukeDateTime endDateTime = new DukeDateTime(endDate, endTime);
            DukeDuration eventDuration = new DukeDuration(startDateTime, endDateTime);
            return new EventTask(description, eventDuration);
        } else {
            throw new DukeException(DukeTextFormatter.makeFormattedText(
                String.format(DukeUi.ERROR_INCOMPLETE_COMMAND, "event")));
        }
    }
    
    /*Translates DD/MM/YYYY into a LocalDate. Days and months can also be input with 1 digit, while the year can
      be input with 2 digits*/
    private static LocalDate extractDateFromString(String timeSpecifications) throws DukeException {
        Matcher dateFormatMatcher = DATE_FORMAT_PATTERN.matcher(timeSpecifications);
        LocalDate deadlineDate = null;
                
        if(dateFormatMatcher.find()) {
            String dateOnly = timeSpecifications.substring(dateFormatMatcher.start(), dateFormatMatcher.end());
            String [] values = dateOnly.split("/");

            int day = Integer.parseInt(values[0]);
            int month = Integer.parseInt(values[1]);
            int year = Integer.parseInt(values[2]);

            checkDateCorrect(day, month);
                    
            if(year < 100) {
                year += 2000;
            }

            deadlineDate = LocalDate.of(year, month, day);
        }

        return deadlineDate;
    }

    //Translates military time (HHMM) into a LocalTime
    private static LocalTime extractTimeFromString(String timeSpecifications) throws DukeException {
        Matcher timeFormatMatcher = TIME_FORMAT_PATTERN.matcher(timeSpecifications);
        LocalTime deadlineTime = null;

        if(timeFormatMatcher.find()) {
            String timeOnly = timeSpecifications.substring(timeFormatMatcher.start(), timeFormatMatcher.end());
            int hour = Integer.parseInt(timeOnly.substring(0, 2));
            int minute = Integer.parseInt(timeOnly.substring(2, 4));

            checkTimeCorrect(hour, minute);
    
            deadlineTime = LocalTime.of(hour, minute);
        }

        return deadlineTime;
    }

    //Throws an exception if the input date cannot exist
    private static void checkDateCorrect (int day, int month) throws DukeException {
        if(day == 0) {
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_DAY_ZERO));
        }

        if(month == 0) {
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_MONTH_ZERO));
        }

        if(month > 12) {
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_MONTH_BIG));
        }

        if(DAYS_EACH_MONTH[month - 1] < day) {
            throw new DukeException(DukeTextFormatter.makeFormattedText(
                String.format(DukeUi.ERROR_DAY_BIG, NAMES_EACH_MONTH[month - 1])));
        }
    }

    //Throws an exception if the input time cannot exist
    private static void checkTimeCorrect (int hour, int minute) throws DukeException {
        //Checks for the hour
        if(hour < 0 || hour > 23) {
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_HOURS_OOB));
        }
        
        //Checks for the minutes
        if(minute < 0 || minute > 59) {
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_MINUTES_OOB));
        }
    }

    //Throws an Exception if the would-be Task doesn't have a description
    private static void checkDescriptionExists(String description) throws DukeException {
        if(description.equals("")) {
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_NO_DESCRIPTION));
        }
    }
    
    //Throws an Exception if the would-be Task doesn't have a description
    private static void checkDescriptionExists (String [] splitString) throws DukeException {
        if(splitString.length == 0){
            throw new DukeException(DukeTextFormatter.makeFormattedText(DukeUi.ERROR_NO_DESCRIPTION));
        }
    }
}
