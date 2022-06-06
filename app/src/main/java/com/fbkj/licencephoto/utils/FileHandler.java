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
     * 切的实际上是X Y两个参数
     * 控制缩放的宽高是把图片控制不失真
     */
    public static Bitmap crop(View view, int newWidth, int newHeight) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        int x;//实际切的X起点
        int y;//实际切的Y起点
        int i = 1;//可调参，按实际用到
        double fW;//宽度缩放倍数
        double fH;//高度缩放倍数
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        if (h * 100 / w < newHeight * 100 / newWidth) {//切宽
            x = w - (h * newWidth) / newHeight;
            y = 2;
        } else {//切高
            x = 2;
            y = h - (w * newHeight) / newWidth;
        }
        //缩放倍数
        if (newWidth == newHeight) {//如果是等倍直接切吧
            fH = fW = (Math.round(w * 100 / newWidth)) / 100.0;//以宽为准取小数点后两位
            x = 2;//这边需要优化下后期截取的高度，目前没做处理,可以骚味以高来切
            y = i << 5;//怎么也不可能没36个给我切吧，高 - 宽剩余量 i后面可调，没做处理

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
     * 这个方法本来是留给原屎抠出来的图片的,
     * 但是扣出的原图和view里边截取的图宽高不一样，所以取view里边的为准
     */
    public static Bitmap cropOrigin(View view, Bitmap b, int newWidth, int newHeight) {
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        view.draw(new Canvas(bitmap));
        int x;
        int y;
        double floorW;
        double floorH;
        int w = view.getWidth(); // 得到图片的宽，高
        int h = view.getHeight();

        if (h * 100 / w < newHeight * 100 / newWidth) {//切宽
            x = w - (h * newWidth) / newHeight;
            y = 2;
        } else {//切高
            x = 2;
            y = h - (w * newHeight) / newWidth;
        }
        //缩放倍数
        if (newWidth == newHeight) {//如果是等倍直接切吧
            x = 2;
            y = 2;
            floorH = floorW = (Math.round(w * 100 / newWidth)) / 100.0;//以宽为准
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
     * 保存图片进相册里边,旧方法了，有些机型保存不了图片，相册刷新不了
     */
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        //检查有没有存储权限
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, "请至权限中心打开应用权限", Toast.LENGTH_SHORT).show();
        } else {
            // 新建目录appDir，并把图片存到其下
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
                // 把file里面的图片插入到系统相册中
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 通知相册更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)));
        }
    }
}
