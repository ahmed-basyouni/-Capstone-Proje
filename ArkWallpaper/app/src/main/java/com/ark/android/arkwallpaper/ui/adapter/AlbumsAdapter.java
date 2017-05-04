package com.ark.android.arkwallpaper.ui.adapter;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.data.model.AlbumObject;
import com.ark.android.arkwallpaper.presenter.contract.AlbumFragmentContract;
import com.ark.android.arkwallpaper.ui.activity.AlbumActivity;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.ark.android.onlinesourcelib.FiveHundredSyncAdapter;
import com.ark.android.onlinesourcelib.FivePxSyncUtils;
import com.ark.android.onlinesourcelib.TumblrSyncUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

/**
 *
 * Created by ahmed-basyouni on 4/23/17.
 */

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumsViewHolder>{

    private final Activity context;
    private final List<AlbumObject> albums;
    private final int height;
    private final int width;
    private final AlbumFragmentContract.IAlbumsPresenter iAlbumPresenter;

    public AlbumsAdapter(List<AlbumObject> albums, Activity activity, AlbumFragmentContract.IAlbumsPresenter iAlbumsPresenter){
        this.context = activity;
        this.albums = albums;
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        width = metrics.widthPixels;
        this.iAlbumPresenter = iAlbumsPresenter;
    }

    @Override
    public AlbumsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);
        return new AlbumsViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final AlbumsViewHolder holder, int position) {

        final AlbumObject albumObject = albums.get(position);

        if((albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_PX || albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR)
                && albumObject.getAlbumImage().toString().equalsIgnoreCase("")){
            holder.syncText.setVisibility(View.VISIBLE);
            holder.albumImage.setVisibility(View.INVISIBLE);
        }else{
            holder.syncText.setVisibility(View.GONE);
            holder.albumImage.setVisibility(View.VISIBLE);
        }

        holder.playAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallPaperUtils.changeAlbumBroadCast(albumObject.getAlbumName());
                holder.selectedView.setVisibility(View.GONE);
            }
        });

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.viewsHolder.getLayoutParams();
        params.height = this.height / 3;
        params.width = RecyclerView.LayoutParams.MATCH_PARENT;
        holder.viewsHolder.setLayoutParams(params);

        holder.albumName.setText(albumObject.getAlbumName());
        holder.albumEnabled.setChecked(albumObject.isEnabled());
        holder.albumEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                iAlbumPresenter.enableAlbum(isChecked, albumObject.getAlbumName());
            }
        });

        holder.editAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_GALLERY)
                    showEditAlbumDialog(albumObject.getAlbumName());
                else if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR){
                    editTumblrAlbum(albumObject);
                }else{
                    editFivePxAlbum(albumObject);
                }
            }
        });

        holder.deleteAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(albumObject);
            }
        });

        holder.hideSelectedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.selectedView.setVisibility(View.GONE);
            }
        });

        holder.albumCount.setText(String.format(context.getString(R.string.albumCount), albumObject.getCount()));

        Glide.with(context)
                .using(new GlideContentProviderLoader(context))
                .load(albumObject.getAlbumImage().toString().equalsIgnoreCase("") ?
                        Uri.parse("android.resource://"+ WallpaperApp.getWallpaperApp().getPackageName() +"/drawable/bg3")
                        : albumObject.getAlbumImage())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.albumImage);

        if(!albumObject.isEnabled()){
            holder.viewsHolder.setAlpha(0.6f);
        }else{
            holder.viewsHolder.setAlpha(1f);
        }

        final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent event) {
                // triggers first for both single tap and long press
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if(holder.selectedView.getVisibility() == View.GONE){
                    Intent i = new Intent(context,  AlbumActivity.class);
                    i.putExtra("albumName" , albumObject.getAlbumName());
                    i.putExtra("tumblrBlog", albumObject.getTumblrBlogName());
                    i.putExtra("fivePxCat", albumObject.getFivePxCategoryName());
                    i.putExtra("type", albumObject.getType());
                    Bundle b = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        //b = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(),
                        //                                         view.getHeight()).toBundle();
                        Bitmap bitmap = Bitmap.createBitmap(holder.viewsHolder.getWidth(), holder.viewsHolder.getHeight(), Bitmap.Config.ARGB_8888);
                        bitmap.eraseColor(Color.parseColor("#308cf8"));

                        b = ActivityOptions.makeThumbnailScaleUpAnimation(holder.viewsHolder, bitmap, 0, 0).toBundle();
                    }
                    context.startActivity(i, b);
//                    context.startActivity(new Intent(context, AlbumActivity.class).putExtra("albumName" , albumObject.getAlbumName()));
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if(holder.selectedView.getVisibility() == View.GONE && holder.syncText.getVisibility() == View.GONE) {
                    super.onLongPress(e);
                    int x = (int) e.getX();
                    int y = (int) e.getY();

                    int startRadius = 0;
                    int endRadius = (int) Math.hypot(width, height / 3);

                    Animator anim = ViewAnimationUtils.createCircularReveal(holder.selectedView, x, y, startRadius, endRadius);

                    holder.selectedView.setVisibility(View.VISIBLE);
                    if(WallPaperUtils.getCurrentAlbum() != null && WallPaperUtils.getCurrentAlbum().equals(albumObject.getAlbumName()))
                        holder.playAlbum.setVisibility(View.GONE);
                    else
                        holder.playAlbum.setVisibility(View.VISIBLE);
                    anim.start();
                }
            }
        });

        holder.viewsHolder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void editFivePxAlbum(final AlbumObject albumObject) {
        Action1<String> action1 = new Action1<String>() {
            @Override
            public void call(String s) {
                context.getContentResolver().delete(GallaryDataBaseContract.GalleryTable.CONTENT_URI
                        , GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?" , new String[]{albumObject.getAlbumName()});
                ContentValues contentValues = new ContentValues();
                contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_Five_PX_CATEGORY, s);
                contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI, "");
                contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT, 0);
                WallpaperApp.getWallpaperApp().getContentResolver().update(GallaryDataBaseContract.AlbumsTable.CONTENT_URI, contentValues
                        , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + " = ?" , new String[]{albumObject.getAlbumName()});
                Bundle bundle = new Bundle();
                bundle.putString(FiveHundredSyncAdapter.CAT_KEY, s);
                bundle.putBoolean("isPer", true);
                FivePxSyncUtils.updatePeriodicSync(albumObject.getFivePxCategoryName(), s);
                FivePxSyncUtils.TriggerRefresh(bundle);
            }
        };
        iAlbumPresenter.showAdd500PxDialog(albumObject.getFivePxCategoryName(), action1);
    }

    private void editTumblrAlbum(final AlbumObject albumObject) {
        Action1<String> action = new Action1<String>() {
            @Override
            public void call(String s) {
                if(s != null){
                    context.getContentResolver().delete(GallaryDataBaseContract.GalleryTable.CONTENT_URI
                            , GallaryDataBaseContract.GalleryTable.COLUMN_ALBUM_NAME + " = ?" , new String[]{albumObject.getAlbumName()});
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TUMBLR_BLOG_NAME, s);
                    contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI, "");
                    contentValues.put(GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT, 0);
                    WallpaperApp.getWallpaperApp().getContentResolver().update(GallaryDataBaseContract.AlbumsTable.CONTENT_URI, contentValues
                            , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME + " = ?" , new String[]{albumObject.getAlbumName()});
                    Bundle bundle = new Bundle();
                    bundle.putString("albumName", s);
                    bundle.putBoolean("isPer", true);
                    TumblrSyncUtils.updatePeriodicSync(albumObject.getTumblrBlogName(), s);
                    TumblrSyncUtils.TriggerRefresh(bundle);
                }else{
                    Toast.makeText(context, context.getString(R.string.blogNotFound), Toast.LENGTH_SHORT).show();
                }
            }
        };
        iAlbumPresenter.showEditTumblrDialog(albumObject.getTumblrBlogName(), action);
    }

    private void showDeleteDialog(final AlbumObject albumObject) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete))
                .setMessage(context.getString(R.string.album_delete_confirmation))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR)
                            TumblrSyncUtils.removePeriodicSync(albumObject.getTumblrBlogName());
                        else if(albumObject.getType() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_PX)
                            FivePxSyncUtils.removePeriodicSync(albumObject.getFivePxCategoryName());
                        iAlbumPresenter.deleteAlbum(albumObject.getAlbumName());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void showEditAlbumDialog(final String oldName) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_album_dialog);

        ((TextView)dialog.findViewById(R.id.dialogTitle)).setText(context.getString(R.string.rename));
        final EditText albumNameField = (EditText) dialog.findViewById(R.id.albumNameField);
        albumNameField.setText(oldName);
        Button okButton = (Button) dialog.findViewById(R.id.addAlbum);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!albumNameField.getText().toString().isEmpty()){
                    iAlbumPresenter.editAlbumName(oldName, albumNameField.getText().toString());
                    dialog.dismiss();
                }else{
                    Toast.makeText(context, context.getString(R.string.no_name_provided), Toast.LENGTH_SHORT).show();
                }
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumsViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.albumName) TextView albumName;
        @BindView(R.id.albumEnabled) CheckBox albumEnabled;
        @BindView(R.id.albumImage) ImageView albumImage;
        @BindView(R.id.viewsHolder) CardView viewsHolder;
        @BindView(R.id.selectedView) LinearLayout selectedView;
        @BindView(R.id.editAlbum) TextView editAlbum;
        @BindView(R.id.deleteAlbum) TextView deleteAlbum;
        @BindView(R.id.hideSelectedView) TextView hideSelectedView;
        @BindView(R.id.albumCount) TextView albumCount;
        @BindView(R.id.syncText) TextView syncText;
        @BindView(R.id.playAlbum) TextView playAlbum;

        AlbumsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
