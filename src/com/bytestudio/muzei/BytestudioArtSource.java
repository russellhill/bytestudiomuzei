package com.bytestudio.muzei;

import java.util.Random;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import static com.bytestudio.muzei.BytestudioPxService.Photo;
import static com.bytestudio.muzei.BytestudioPxService.PhotosResponse;

public class BytestudioArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "Bytestudio";
    private static final String SOURCE_NAME = "BytestudioArtSource";

    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours

	public BytestudioArtSource() {
		super(SOURCE_NAME);
	}

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://www.bytestudiophotography.com")
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError retrofitError) {
                        int statusCode = retrofitError.getResponse().getStatus();
                        if (retrofitError.isNetworkError() || (500 <= statusCode && statusCode < 600)) {
                            return new RetryException();
                        }
                        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                        return retrofitError;
                    }
                })
                .build();

        BytestudioPxService service = restAdapter.create(BytestudioPxService.class);
        PhotosResponse response = service.getPopularPhotos();

        if (response == null || response.photos == null) {
            throw new RetryException();
        }

        if (response.photos.size() == 0) {
            Log.w(TAG, "No photos returned from API.");
            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
            return;
        }

        Random random = new Random();
        Photo photo;
        String token;
        while (true) {
            photo = response.photos.get(random.nextInt(response.photos.size()));
            token = Integer.toString(photo.id);
            if (response.photos.size() <= 1 || !TextUtils.equals(token, currentToken)) {
                break;
            }
        }

        publishArtwork(new Artwork.Builder()
                .title(photo.name)
                .byline(photo.user.username)
                .imageUri(Uri.parse(photo.image_url))
                .token(token)
                .viewIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(photo.image_url)))
                .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}
