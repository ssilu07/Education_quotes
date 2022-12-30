package com.royals.edunotes._database;

/**
 * Created by Admin on 18-06-2018.
 */

public class ModelDatabase {

    public static final String TABLE_NAME = "bookmarktable";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_BOOKMARK = "bookmark";
    public static final String COLUMN_CATEGORY = "category";

    private int id;
    private String note;
    private String timestamp;
    private String bookmark;
    private String category;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER,"
                    + COLUMN_NOTE + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COLUMN_BOOKMARK + " TEXT,"
                    + COLUMN_CATEGORY + " TEXT"
                    + ")";

    public ModelDatabase() {
    }

    public ModelDatabase(int id, String note, String timestamp, String bookmark,String category) {
        this.id = id;
        this.note = note;
        this.timestamp = timestamp;
        this.bookmark = bookmark;
        this.category = category;
    }
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
