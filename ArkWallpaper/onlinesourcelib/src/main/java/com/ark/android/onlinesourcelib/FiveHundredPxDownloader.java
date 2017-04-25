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

package com.ark.android.onlinesourcelib;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class FiveHundredPxDownloader {

    public static List<FiveHundredPxService.Photo> get500PXPhotos(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(final Chain chain) throws IOException {
                        Request request = chain.request();
                        HttpUrl url = request.url().newBuilder()
                                .addQueryParameter("consumer_key", "mlyanJDfnMCRIOyFjhSzpmkCpL7jhUV3gV62asZh").build();
                        request = request.newBuilder().url(url).build();
                        return chain.proceed(request);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.500px.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FiveHundredPxService service = retrofit.create(FiveHundredPxService.class);
        FiveHundredPxService.PhotosResponse response = null;
        try {
            response = service.get500xPhotos().execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (response == null || response.photos == null) {
            return null;
        }

        if (response.photos.size() == 0) {
            return null;

        }else{
            return response.photos;
        }
    }
}

