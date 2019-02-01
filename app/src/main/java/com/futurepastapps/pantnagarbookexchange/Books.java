package com.futurepastapps.pantnagarbookexchange;

/**
 * Created by HP on 23-06-2018.
 */

public class Books {

    private String userName , authorName , genre , language , userThumb , description;
    private boolean available;

    public Books() {

    }

    public Books(String userName, String authorName, String genre, String language, String userThumb, String description, boolean available) {
        this.userName = userName;
        this.authorName = authorName;
        this.genre = genre;
        this.language = language;
        this.userThumb = userThumb;
        this.description = description;
        this.available = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserThumb() {
        return userThumb;
    }

    public void setUserThumb(String userThumb) {
        this.userThumb = userThumb;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}