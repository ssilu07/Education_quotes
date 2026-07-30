package com.royal.edunotes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class ShareUtils {

    public static void shareViewAsImage(Context context, View view, String text) {
        try {
            // Create bitmap from view
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            // Save to cache directory
            File cachePath = new File(context.getCacheDir(), "shared_images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "quote_" + System.currentTimeMillis() + ".png");

            FileOutputStream stream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            // Share using FileProvider
            Uri contentUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text + "\n\nDownload the app:\nhttps://play.google.com/store/apps/details?id=com.royal.edunotes");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share Quote"));

        } catch (Exception e) {
            Toast.makeText(context, "Failed to share image", Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareTextAndImage(Context context, String text, Bitmap bitmap) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);

            if (bitmap != null) {
                File cachePath = new File(context.getCacheDir(), "shared_images");
                cachePath.mkdirs();
                File imageFile = new File(cachePath, "share_" + System.currentTimeMillis() + ".png");
                FileOutputStream stream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                stream.close();

                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", imageFile);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                shareIntent.setType("text/plain");
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show();
        }
    }
}
