package com.example.testappsyncmanager.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;


public class ImageUtils {

    public static void displayRoundImageFromUrl(final Context context, final String url,
                                                final ImageView imageView) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate();

        Glide.with(context)
                .asBitmap()
                .apply(myOptions)
                .load(url)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory
                                        .create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void displayImageFromUrl(final Context context, final String url,
                                           final ImageView imageView,
                                           Drawable placeholderDrawable) {
        displayImageFromUrl(context, url, imageView, placeholderDrawable, null);
    }

    public static void displayImageFromUrl(final Context context, final String url,
                                           final ImageView imageView, Drawable placeholderDrawable,
                                           RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderDrawable);

        if (listener != null) {
            Glide.with(context)
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        }
    }

    public static void displayRoundImageFromUrlWithoutCache(final Context context, final String url,
                                                            final ImageView imageView) {
        displayRoundImageFromUrlWithoutCache(context, url, imageView, null);
    }

    public static void displayRoundImageFromUrlWithoutCache(final Context context, final String url,
                                                            final ImageView imageView,
                                                            RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        if (listener != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory
                                            .create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(myOptions)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory
                                            .create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
    }

    public static void displayImageFromUrlWithPlaceHolder(final Context context, final String url,
                                                          final ImageView imageView,
                                                          int placeholderResId) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderResId);

        Glide.with(context)
                .load(url)
                .apply(myOptions)
                .into(imageView);
    }

    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView,
                                              Drawable placeholderDrawable,
                                              RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderDrawable);

        if (listener != null) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .into(imageView);
        }
    }

    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView,
                                              String thumbnailUrl, Drawable placeholderDrawable) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderDrawable);

        if (thumbnailUrl != null) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .thumbnail(Glide.with(context).asGif().load(thumbnailUrl))
                    .into(imageView);
        } else {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .into(imageView);
        }
    }

    public static void displayImageFromResource(final Context context,
                                                @DrawableRes final int imageResId,
                                                final ImageView imageView) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(context)
                .load(ContextCompat.getDrawable(context,imageResId))
                .apply(myOptions)
                .into(imageView);
    }
    public static void displayImageFromUri(final Context context,
                                           final String imageResId,
                                           final ImageView imageView) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(context)
                .load(imageResId)
                .apply(myOptions)
                .into(imageView);
    }
}
