package com.royal.edunotes._models;

/**
 * Created by Admin on 20-03-2018.
 */

    public class CategoryModel {
         String title;
        String dbname;
        int totalCount;
        int viewedCount;

        public String getDbname() {
            return dbname;
        }

        public void setDbname(String dbname) {
            this.dbname = dbname;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getViewedCount() {
            return viewedCount;
        }

        public void setViewedCount(int viewedCount) {
            this.viewedCount = viewedCount;
        }

        public CategoryModel(String title, String dbname) {
            this.title = title;
            this.dbname = dbname;
        }

        public CategoryModel(String title, String dbname, int viewedCount, int totalCount) {
            this.title = title;
            this.dbname = dbname;
            this.viewedCount = viewedCount;
            this.totalCount = totalCount;
        }
    }