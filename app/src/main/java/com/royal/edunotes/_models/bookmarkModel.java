package com.royal.edunotes._models;

/**
 * Created by Admin on 23-03-2018.
 */

public class bookmarkModel {
    String title,hack,id;

    public bookmarkModel() {
    }

    public bookmarkModel(String id , String title, String hack) {
        this.title = title;
        this.hack = hack;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHack() {
        return hack;
    }

    public void setHack(String hack) {
        this.hack = hack;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
