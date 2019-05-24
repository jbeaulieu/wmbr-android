package com.jbproductions.wmbr;

public class Archive {

    public Archive() {}

    private String url, date;

    /**
     * Gets the date the archive was recorded, for human-readable reference
     * @return String containing the date of recording for the archive
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the source url for the archive
     * @return String containing an http url to the archive's mp3 file
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the date the archive was recorded
     * @param d String value of a date
     */
    public void setDate(String d) {
        date = d;
    }

    /**
     * Sets the source url for the archive
     * @param u String containing an http url to the archive's mp3 file
     */
    public void setUrl(String u) {
        url = u;
    }
}
