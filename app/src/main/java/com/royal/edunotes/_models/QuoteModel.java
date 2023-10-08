package com.royals.edunotes._models;

/**
 * Created by Admin on 21-06-2018.
 */

public class QuoteModel {
    public String quote,timestamp, categoryName;
    public int id;

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

   public String bookmark;

    boolean isBookmared;

    public boolean isBookmared() {
        return isBookmared;
    }

    public void setBookmared(boolean bookmared) {
        isBookmared = bookmared;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
