package com.fbkj.licencephoto.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class FileHandler {

    /**
     * TAG for log messages.
     */
    private static final String TAG = "FileUtils";

    public FileHandler() {
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException ex) {
            Log.i(TAG, String.format(Locale.getDefault(), "getDataColumn: _data - [%s]", ex.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        Log.i(TAG, e.getMessage());
                        return null;
                    }
                }

            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * ??????????????????X Y????????????
     * ????????????????????????????????????????????????
     */
    public static Bitmap crop(View view, int newWidth, int newHeight) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        int x;//????????????X??????
        int y;//????????????Y??????
        int i = 1;//???????????????????????????
        double fW;//??????????????????
        double fH;//??????????????????
        int w = bitmap.getWidth(); // ????????????????????????
        int h = bitmap.getHeight();

        if (h * 100 / w < newHeight * 100 / newWidth) {//??????
            x = w - (h * newWidth) / newHeight;
            y = 2;
        } else {//??????
            x = 2;
            y = h - (w * newHeight) / newWidth;
        }
        //????????????
        if (newWidth == newHeight) {//???????????????????????????
            fH = fW = (Math.round(w * 100 / newWidth)) / 100.0;//?????????????????????????????????
            x = 2;//???????????????????????????????????????????????????????????????,????????????????????????
            y = i << 5;//?????????????????????36????????????????????? - ???????????? i???????????????????????????

            return  Bitmap.createBitmap(bitmap,
                    Math.abs(x) / 2,
                    Math.abs(y),
                    (int) ((newWidth * fW)),
                    (int) ((newHeight * fH)),
                    null, false);
        } else {
            fH = (Math.round(h * 100 / newHeight)) / 100.0;
            fW = (Math.round(w * 100 / newWidth)) / 100.0;
        }

        return Bitmap.createBitmap(bitmap,
                Math.abs(x) / 2,
                Math.abs(y) / 2,
                (int) ((newWidth * fW)) - x,
                (int) ((newHeight * fH)) - y,
                null, false);
    }

    /**
     * ??????????????????????????????????????????????????????,
     * ????????????????????????view?????????????????????????????????????????????view???????????????
     */
    public static Bitmap cropOrigin(View view, Bitmap b, int newWidth, int newHeight) {
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        view.draw(new Canvas(bitmap));
        int x;
        int y;
        double floorW;
        double floorH;
        int w = view.getWidth(); // ????????????????????????
        int h = view.getHeight();

        if (h * 100 / w < newHeight * 100 / newWidth) {//??????
            x = w - (h * newWidth) / newHeight;
            y = 2;
        } else {//??????
            x = 2;
            y = h - (w * newHeight) / newWidth;
        }
        //????????????
        if (newWidth == newHeight) {//???????????????????????????
            x = 2;
            y = 2;
            floorH = floorW = (Math.round(w * 100 / newWidth)) / 100.0;//????????????
        } else {
            floorH = (Math.round(h * 100 / newHeight)) / 100.0;
            floorW = (Math.round(w * 100 / newWidth)) / 100.0;
        }

        return Bitmap.createBitmap(b, Math.abs(x) / 2, Math.abs(y) / 2,
                (int) ((newWidth * floorW)) - x,
                (int) ((newHeight * floorH)) - y,
                null, false);
    }

    /**
     * ???????????????????????????,??????????????????????????????????????????????????????????????????
     */
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        //???????????????????????????
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
        } else {
            // ????????????appDir???????????????????????????
            File appDir = new File(
                    context.getExternalFilesDir(null).getAbsolutePath() + System.currentTimeMillis());
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                // ???file???????????????????????????????????????
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // ??????????????????
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)));
        }
    }
}
