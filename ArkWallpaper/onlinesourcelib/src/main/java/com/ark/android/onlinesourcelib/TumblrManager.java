package com.ark.android.onlinesourcelib;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.net.URLConnection;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 *
 * Created by ahmed-basyouni on 4/25/17.
 */
public class TumblrManager {
    private static TumblrManager ourInstance = new TumblrManager();

    public static TumblrManager getInstance() {
        return ourInstance;
    }

    private TumblrManager() {
    }

    public void checkBlogInfo(final String blogName, final Action1<String> subscriber){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.tumblr.com/")
                .client(getTumblrOkClient(blogName))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        TumblrBlogVerifier tumblrBlogVerifier = retrofit.create(TumblrBlogVerifier.class);
        tumblrBlogVerifier.checkBlogInfo().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<TumblrBlogVerifier.BlogInfo>() {


            @Override
            public void onNext(TumblrBlogVerifier.BlogInfo blogInfo) {
                if(blogInfo.meta.status == 200){
                    subscriber.call(blogName);
                }else{
                    subscriber.call(null);
                }

            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                subscriber.call(null);
            }

        });
    }

    public OkHttpClient getTumblrOkClient(final String blogName){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(final Chain chain) throws IOException {
                        Request request = chain.request();
                        HttpUrl url = request.url().newBuilder()
                                .addEncodedPathSegment(blogName + ".tumblr.com")
                                .addPathSegment("info")
                                .addQueryParameter("api_key", "fuiKNFp9vQFvjLNvx4sUwti4Yb5yGutBN4Xh10LXZhhRKjWlV4")
                                .build();
                        request = request.newBuilder().url(url).build();
                        return chain.proceed(request);
                    }
                })
                .build();
    }

    public int getOffset(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt("tumblrOffset" , 0);
    }

    public void setOffset(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int currentOffset = sharedPreferences.getInt("tumblrOffset", 0);
        sharedPreferences.edit().putInt("tumblrOffset", currentOffset+TumblrDownloader.DOWNLOAD_LIMIT).apply();
    }

    public void restOffset(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("tumblrOffset", 0).apply();
    }


}
