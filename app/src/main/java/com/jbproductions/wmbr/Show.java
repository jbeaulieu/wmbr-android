package com.jbproductions.wmbr;

public class Show {

    public Show() {

    }

    private int id, day, time, length, alternates;
    private String name, hosts, producers, url, email, description;
    private String[] daysOfWeekArray = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Weekdays"};

    /**
     * Sets a show's unique ID number
     * Warning: This function does not check for any other Shows with conflicting ID numbers
     * @param showID ID number as int
     */
    public void setID(int showID) {
        id = showID;
    }

    /**
     * Set day(s) of the week a show airs
     * Within the wmbr website, the day a show airs is represented as an integer as follows:
     *      [0-6] -> Sunday through Saturday, respectively. Used for most shows
     *      7 -> Weekdays (Monday-Friday). Used for shows such as LRC and the Nightly News
     * @param dayOfWeek int representing the day(s) of the week a show airs
     */
    public void setDay(int dayOfWeek) {
        day = dayOfWeek;
    }

    /**
     * Sets the start time for the given show
     * @param showTime String value of the show's start time, scraped from wmbr.org
     */
    public void setTime(String showTime) {
        time = convertTo24HourTime(showTime);
    }

    /**
     * Sets the length of a show in minutes
     * @param showLength length of a show in minutes, expressed as int
     */
    public void setLength(int showLength) {
        length = showLength;
    }

    /**
     * Sets the 'alternates' value for a show, as follows
     *      0 -> Show does not alternate
     *      1 -> Show alternates weekly, airs on odd weeks
     *      2 -> Show alternates weekly, airs on even weeks
     * @param alternateID alternate value as int
     */
    public void setAlternates(int alternateID) {
        alternates = alternateID;
    }

    /**
     * Sets the name of a show
     * @param showName String name of show
     */
    public void setName(String showName) {
        name = showName;
    }

    /**
     * Sets the host(s) for a show
     * Note that even if a show has multiple hosts, they are all listed within a single String.
     * This is a reflection of the way host data is stored within the WMBR database.
     * @param showHosts single String containing one or more hosts of a show
     */
    public void setHosts(String showHosts) {
        hosts = showHosts;
    }

    /**
     * Sets the producer(s) for a show
     * Note that even if a show has multiple producers, they are all listed within a single String.
     * This is a reflection of the way producer data is stored within the WMBR database.
     * @param showProducers single String containing one or more producers of a show
     */
    public void setProducers(String showProducers) {
        producers = showProducers;
    }

    /**
     * Sets a URL to be associated with a show
     * @param showUrl String representation of show's URL
     */
    public void setUrl(String showUrl) {
        url = showUrl;
    }

    /**
     * Sets email address associated with a show
     * In WMBR's Xml API, email addresses from the"wmbr.org" domain do not contain a domain suffix.
     * Before setting the email property, this function checks the input string for a '@' character
     * and, if one is not found, appends "@wmbr.org" to the string
     * @param showEmail String representation of show's email address
     */
    public void setEmail(String showEmail) {
        if(showEmail.contains("@")) {
            email = showEmail;
        }
        else {
            email = showEmail + "@wmbr.org";
        }

    }

    /**
     * Sets the host-provided description of a show
     * @param showDescription String containing the description
     */
    public void setDescription(String showDescription) {
        description = showDescription;
    }

    /**
     * Retrieves a show's unique ID number
     * @return int value of the show's ID
     */
    public int getID() {
        return id;
    }

    /**
     * Retrieves a show's name
     * @return String containing the name of the Show
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the days of the week a show airs, expressed as an integer
     * This int value is a reflection of how data is stored within the wmbr database.
     * To retrieve a human-readable day-of-week value, see getDayOfWeekAsPlainText()
     * @return int value of when show airs
     */
    public int getDay() {
        return day;
    }

    /**
     * Gets a readable value of when a show airs based on its 'day' property
     * Examples:    0 -> "Sunday"
     *              3 -> "Wednesday"
     *              7 -> "Weekdays"
     * @return Human-readable value of when show airs
     */
    public String getDayOfWeekAsPlainText() {
        return daysOfWeekArray[day];
    }

    /**
     * Gets the show's start time, expressed in 24-hour time
     * @return int representing the 24-hour time when a show begins
     */
    public int getTime() {
        return time;
    }

    /**
     * Gets the length of a show, expressed in minutes
     * @return show length in minutes, as int
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the 'alternates' value of a show
     * A show which does not alternate will return a value of 0
     * A show which airs on odd weeks will return a value of 1
     * A show which airs on even weeks will return a value of 2
     * @return int expressing how the show alternates
     */
    public int getAlternates() {
        return alternates;
    }

    /**
     * Retrieves the show's hosts
     * All hosts are returned as a single String. This is a direct reflection of how host data
     * is stored within the wmbr database.
     * @return String containing all listed hosts
     */
    public String getHosts() {
        return hosts;
    }

    /**
     * Retrieves the show's producers, if any
     * If no producers are listed, will return an empty String
     * This function is *not* the same as getHosts() and returns a different value
     * @return String containing all listed producers, or empty String if none are listed
     */
    public String getProducers() {
        return producers;
    }

    /**
     * Retrieves the show's URL, if any
     * If no URL has been set, will return an empty String
     * @return String containing show's URL, or empty String if one does not exist
     */
    public String getUrl() {
        return url;
    }

    /**
     * Retrieves the show's email address, if any
     * If no email has been set, will return an empty String
     * @return String containing show's email address, or empty String if one does not exist
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retrieves a show's description
     * @return String containing the host's description of the show
     */
    public String getDescription() {
        return description;
    }

    /**
     * This function is necessary because the WMBR website keeps track of showtimes as strings
     * with a colon and a suffix of "a", "p", "m", or "n" for AM, PM, midnight, and noon.
     * Examples: "7:00a" -> 0700 hours
     *          "10:30p" -> 2230 hours
     *          "12:00m" -> 0000 hours (midnight)
     *          "12:00n" -> 1200 hours (noon)
     * The below function converts the aforementioned time string to an integer representation of
     * a 24-hour time. On a personal note, this method of timekeeping on the WMBR seems website
     * seems exceptionally silly and I look forward to it being changed.
     * @param showTime String value of the show's start time, scraped from wmbr.org
     * @return int in the range [0,2400] representing the show's start time
     */
    private int convertTo24HourTime(String showTime) {

        // Deal with hardcoded "12:00m" or "12:00n" values for noon and midnight shows
        if(showTime == "12:00m") {
            return 0;
        }
        else if(showTime == "12:00n") {
            return 1200;
        }
        else {
            String removeColon = showTime.replace(":", "");
            int time = Integer.parseInt(removeColon.substring(0, removeColon.length()-1));

            if(removeColon.substring(removeColon.length()-1).equals("p")) {
                time += 1200;
            }

            return time;
        }
    }
}
