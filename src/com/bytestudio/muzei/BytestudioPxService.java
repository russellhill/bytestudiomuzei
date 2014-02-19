package com.bytestudio.muzei;

import java.util.List;

import retrofit.http.GET;

interface BytestudioPxService {
    @GET("/muzei_json.php")
    PhotosResponse getPopularPhotos();

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
}