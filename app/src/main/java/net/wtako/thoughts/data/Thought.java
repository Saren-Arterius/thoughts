package net.wtako.thoughts.data;

import net.wtako.thoughts.interfaces.IHasID;

import java.util.Date;
import java.util.List;

public class Thought implements IHasID {

    int id;
    String title;
    String content;
    int rating;
    List<String> hashTags;
    long date;

    public Thought(int id, String title, String content, int rating, List<String> hashTags, Date date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.hashTags = hashTags;
        this.date = date.getTime();
    }

    @Override
    public int getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<String> getHashTags() {
        return hashTags;
    }

    public Date getDate() {
        return new Date(date);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Thought)) {
            return false;
        }
        Thought thought = (Thought) other;
        return thought.id == id;
    }
}
