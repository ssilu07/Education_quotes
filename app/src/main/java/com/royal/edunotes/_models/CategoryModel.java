package com.royals.edunotes._models;

/**
 * Created by Admin on 20-03-2018.
 */

    public class CategoryModel {
         String title;
        String dbname;
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


        public CategoryModel(String title, String dbname) {
            this.title = title;
            this.dbname = dbname;

        }



    }