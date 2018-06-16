package altice.jrojas.android__sharing_app.classes;

import android.location.Location;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import altice.jrojas.android__sharing_app.interfaces.Hashable;

/**
 * Created by jaime on 6/3/2018.
 */

public class Article implements Hashable{
    private String id;
    private String imageUrl;
    private String title;
    private String author;
    private String description;
    private Date createdAt;
    private long likes;
    private long favourites;
    private ArticleLocation location;
    private User user;

    public Article(){}

    public Article(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public Article(String id, String imageUrl, String title, String author, String description, long likes, long favourites, ArticleLocation location) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.description = description;
        this.createdAt = createdAt;
        this.likes = likes;
        this.favourites = favourites;
        this.location = location;
        this.createdAt = new Date();
    }

    public Article(String imageUrl, String title, String author, String description, long likes, long favourites, ArticleLocation location) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.description = description;
        this.createdAt = createdAt;
        this.likes = likes;
        this.favourites = favourites;
        this.location = location;
        this.createdAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long like() {
        this.likes += 1;
        return this.likes;
    }

    public long dislike() {
        if(this.likes > 0) {
            this.likes -= 1;
        }
        return this.likes;
    }

    public long getFavourites() {
        return favourites;
    }

    public void setFavourites(long favourites) {
        this.favourites = favourites;
    }

    public long favorite() {
        this.favourites += 1;
        return this.favourites;
    }

    public long unfavorite() {
        if(this.favourites > 0) {
            this.favourites -= 1;
        }
        return this.favourites;
    }

    public ArticleLocation getLocation() {
        return location;
    }

    public void setLocation(ArticleLocation location) {
        this.location = location;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Map<String, Object> toHashMap() {
        Map<String, Object> article = new HashMap<>();
        article.put("imageUrl", this.imageUrl);
        article.put("title", this.title);
        article.put("description", this.description);
        article.put("likes", this.likes);
        article.put("favourites", this.favourites);
        article.put("createdAt", this.createdAt);
        article.put("location", this.location);
        article.put("user", this.user);
        return article;
    }
}
