package com.bytestudio.muzei;

import java.util.List;

import retrofit.http.GET;
import retrofit.Callback;

interface BytestudioPxService {
    @GET("/muzei_json.php") 
    PhotosResponse getPopularPhotos();

    @GET("/muzei_categories_json.php") 
    void getCategories(Callback<List<Category>> callback);
    
    static class PhotosResponse {
        List<Photo> photos;
    }

    static class Photo {
        int id;
        String name;
        String description;
        String image_url;
        User user;
    }

    static class User {
        String username;
    }
    
    static class CategoriesResponse {
    	List<Category> categories;
    }
    
    static class Category {
    	int id;
    	String name;
    	String description;
    }
}
