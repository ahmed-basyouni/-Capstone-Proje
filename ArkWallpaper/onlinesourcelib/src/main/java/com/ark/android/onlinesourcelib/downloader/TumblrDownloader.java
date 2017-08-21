package com.ark.android.onlinesourcelib.downloader;

/**
 * Created by ahmed-basyouni on 4/25/17.
 */


/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.ark.android.arkanalytics.GATrackerManager;
import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class TumblrDownloader {

    public static final int DOWNLOAD_LIMIT = 5;

    public static List<TumblrService.Post> getTumblrPhotos(final String albumName, final int offset) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(final Chain chain) throws IOException {
                        Request request = chain.request();
                        HttpUrl url = request.url().newBuilder()
                                .addEncodedPathSegment(albumName + ".tumblr.com")
                                .addEncodedPathSegment("posts")
                                .addPathSegment("photo")
                                .addQueryParameter("api_key", "fuiKNFp9vQFvjLNvx4sUwti4Yb5yGutBN4Xh10LXZhhRKjWlV4")
                                .addQueryParameter("offset", String.valueOf(offset))
                                .addQueryParameter("limit", String.valueOf(DOWNLOAD_LIMIT)).build();
                        request = request.newBuilder().url(url).build();
                        return chain.proceed(request);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.tumblr.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TumblrService service = retrofit.create(TumblrService.class);
        TumblrService.PhotosResponse response = null;
        try {
            response = service.get500xPhotos().execute().body();
        } catch (IOException e) {
            GATrackerManager.getInstance().trackException(e);
            Crashlytics.logException(e);
            e.printStackTrace();
            return null;
        }

        if (response == null || response.response.posts == null) {
            return null;
        }

        if (response.response.posts.size() == 0) {
            return null;

        } else {
            return response.response.posts;
        }
    }
}

