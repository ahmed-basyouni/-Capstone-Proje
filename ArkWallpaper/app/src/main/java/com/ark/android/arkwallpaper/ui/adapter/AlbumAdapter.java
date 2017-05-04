package com.ark.android.arkwallpaper.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.ui.activity.AlbumActivity;
import com.ark.android.arkwallpaper.ui.activity.LastImageInfoActivity;
import com.ark.android.arkwallpaper.utils.IOUtils;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/24/17.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder>{

    private static final int TWO_CELLS = 2;
    private static final int ONE_CELL = 1;
    private final List<Uri> images;
    private final AlbumActivity mContext;
    private final int height;
    private int width;

    private Animator mCurrentAnimator;

    private int mShortAnimationDuration;

    public AlbumAdapter(List<Uri> imagesUri, AlbumActivity activity){
        this.images = imagesUri;
        this.mContext = activity;
        DisplayMetrics metrics = new DisplayMetrics();
        mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        mShortAnimationDuration = mContext.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    @Override
    public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.album_single_item, parent, false);
        return new AlbumHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final AlbumHolder holder, int position) {

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.imageHolder.getLayoutParams();

        if(getItemViewType(holder.getAdapterPosition()) == TWO_CELLS) {
            params.width = (width / 2) - 25;
            params.height = (height / 3) - 24;
            if(holder.getAdapterPosition() % 3 == 0) {
                params.setMarginStart(16);
                params.setMarginEnd(8);
            }else if(holder.getAdapterPosition() % 3 == 1){
                params.setMarginStart(8);
                params.setMarginEnd(16);
            }
        }else if(getItemViewType(holder.getAdapterPosition()) == ONE_CELL){
            params.width = width - 32;
            params.setMarginStart(16);
            params.setMarginEnd(16);
            params.height = (height / 3) - 24;
        }
        params.bottomMargin = 16;
        holder.imageHolder.setLayoutParams(params);

        Glide.with(mContext)
                .using(new GlideContentProviderLoader(mContext))
                .load(images.get(holder.getAdapterPosition()))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(params.width, params.height)
                .into(holder.albumSingleImage);

        holder.albumSingleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, LastImageInfoActivity.class);
                intent.putExtra(LastImageInfoActivity.IMAGE_URI,images.get(holder.getAdapterPosition()).toString());
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(mContext, holder.albumSingleImage , mContext.getString(R.string.expandedImageView));
//                mContext.startActivity(intent, options.toBundle());
                ActivityCompat.startActivityForResult(mContext, intent, LastImageInfoActivity.REQUEST_ID, options.toBundle());
            }
        });
    }

    private void showLargeImageDialog(Uri image) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.album_single_item);

        final ImageView albumNameField = (ImageView) dialog.findViewById(R.id.albumSingleImage);

        Glide.with(mContext)
                .using(new GlideContentProviderLoader(mContext))
                .load(image)
                .into(albumNameField);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);

        dialog.show();
    }

    private void zoomImageFromThumb(final View thumbView, final Uri imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final View largeContainer = mContext.findViewById(R.id.bigContainer);
        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) mContext.findViewById(
                R.id.expandedImageView);

        final ProgressBar progressBar = (ProgressBar) mContext.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        final TextView setAsWallpaper = (TextView) mContext.findViewById(R.id.setAsWallpaper);

        final TextView downloadImage = (TextView) mContext.findViewById(R.id.downloadImage);

        final TextView deleteImage = (TextView) mContext.findViewById(R.id.deleteImage);

        setAsWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WallPaperUtils.changeWallpaperBroadCast(imageResId.toString());
            }
        });

        downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    IOUtils.exportFile(new File(imageResId.getPath()));
                    Toast.makeText(mContext, mContext.getString(R.string.imageSaved), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Glide.with(mContext)
                .using(new GlideContentProviderLoader(mContext))
                .load(imageResId)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(width, height)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(expandedImageView);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        mContext.findViewById(R.id.coordinateLayout)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);
        largeContainer.setVisibility(View.VISIBLE);
        setAsWallpaper.setVisibility(View.VISIBLE);
        downloadImage.setVisibility(View.VISIBLE);
        deleteImage.setVisibility(View.VISIBLE);
        mContext.findViewById(R.id.floatingMenu).setVisibility(View.GONE);
        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        largeContainer.setVisibility(View.GONE);
                        setAsWallpaper.setVisibility(View.GONE);
                        downloadImage.setVisibility(View.GONE);
                        deleteImage.setVisibility(View.GONE);
                        if(mContext.getmAlbumtype() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_GALLERY)
                            mContext.findViewById(R.id.floatingMenu).setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        largeContainer.setVisibility(View.GONE);
                        setAsWallpaper.setVisibility(View.GONE);
                        downloadImage.setVisibility(View.GONE);
                        deleteImage.setVisibility(View.GONE);
                        if(mContext.getmAlbumtype() == GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_GALLERY)
                            mContext.findViewById(R.id.floatingMenu).setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        };
        mContext.setBackClickable(onClickListener);
        expandedImageView.setOnClickListener(onClickListener);
        largeContainer.setOnClickListener(onClickListener);
        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(null);
                mContext.deleteImage(imageResId.toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public int getItemViewType(int position) {
        int mod = position % 3;

        if(mod == 0 || mod == 1)
            return TWO_CELLS;
        else
            return ONE_CELL;
    }

    class AlbumHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.imageHolder)
        CardView imageHolder;
        @BindView(R.id.albumSingleImage)
        ImageView albumSingleImage;

        public AlbumHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}