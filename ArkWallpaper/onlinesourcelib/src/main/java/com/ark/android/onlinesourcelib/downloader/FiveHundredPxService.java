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

package com.ark.android.onlinesourcelib.downloader;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FiveHundredPxService {
    @GET("v1/photos?&image_size=6&rpp=2")
    Call<PhotosResponse> get500xPhotos();

    class PhotosResponse {
        List<Photo> photos;
    }

    class Photo {
        public int id;
        public String image_url;
        String name;
        User user;
        public boolean nsfw;
    }

    class User {
        String fullname;
    }
}
