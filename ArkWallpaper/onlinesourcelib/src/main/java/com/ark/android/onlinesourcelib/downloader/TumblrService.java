package com.ark.android.onlinesourcelib.downloader;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 *
 * Created by ahmed-basyouni on 4/25/17.
 */

public interface TumblrService {

    @GET("v2/blog/")
    Call<TumblrService.PhotosResponse> get500xPhotos();

    class PhotosResponse {
        Response response;
    }

    class Response{
        List<Post> posts;
    }

    class Post{
        public List<Photos> photos;
        String id;
    }

    class Photos {
        public OriginalSize original_size;
    }

    class OriginalSize{
        public String url;
    }
}
