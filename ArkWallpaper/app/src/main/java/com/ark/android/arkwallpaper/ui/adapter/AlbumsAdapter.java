package com.ark.android.arkwallpaper.ui.adapter;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import com.ark.android.arkwallpaper.data.model.AlbumObject;
import com.ark.android.arkwallpaper.presenter.contract.AlbumFragmentContract;
import com.ark.android.arkwallpaper.ui.activity.AlbumActivity;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
                showEditAlbumDialog(albumObject.getAlbumName());
            }
        });

        holder.deleteAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(albumObject.getAlbumName());
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
                .load(albumObject.getAlbumImage())
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
                if(holder.selectedView.getVisibility() == View.GONE) {
                    super.onLongPress(e);
                    int x = (int) e.getX();
                    int y = (int) e.getY();

                    int startRadius = 0;
                    int endRadius = (int) Math.hypot(width, height / 3);

                    Animator anim = ViewAnimationUtils.createCircularReveal(holder.selectedView, x, y, startRadius, endRadius);

                    holder.selectedView.setVisibility(View.VISIBLE);
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

    private void showDeleteDialog(final String albumName) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete))
                .setMessage(context.getString(R.string.album_delete_confirmation))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        iAlbumPresenter.deleteAlbum(albumName);
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

        AlbumsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
