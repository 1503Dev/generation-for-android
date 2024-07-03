package tc.generation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.webkit.WebView;
import android.content.Intent;
import android.net.Uri;
import android.app.ActionBar;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.WebChromeClient;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.webkit.WebSettings;
import android.provider.MediaStore;
import android.provider.DocumentsContract;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Environment;
import android.content.ContentUris;
import android.database.Cursor;

public class WebviewActivity extends Activity {

    public static final String TAG = "WebviewActivity";
    public static TextView title;
    public static TextView subtitle;
    public static WebView wv;
    private long exitTime = 0;
    private Context mc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_webview);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        wv = findViewById(R.id.webview);
        mc = this;

        WebSettings settings = wv.getSettings();
        settings.setUseWideViewPort(true);//设定支持viewport
        settings.setLoadWithOverviewMode(true);   //自适应屏幕
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);//设定支持缩放
        // 启用JavaScript
        settings.setJavaScriptEnabled(true);
        // 允许从文件加载
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setDomStorageEnabled(true);
        wv.setWebViewClient(new WebViewClient() {
                //设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    updateSubtitle(url);
                    view.loadUrl(url);
                    return true;
                }
            });
        wv.setWebChromeClient(new WebChromeClient() {
                //这里设置获取到的网站title
                @Override
                public void onReceivedTitle(WebView view, String t) {
                    super.onReceivedTitle(view, t);
                    updateSubtitle(view.getUrl());
                    if(view.getUrl().equals("about:blank")){
                        t=getString(R.string.activity_WebviewActivity);
                    }
                    if (t.length() > 32) {
                        title.setTextSize(14);
                    } else if (t.length() > 24) {
                        title.setTextSize(16);
                    } else {
                        title.setTextSize(18);
                    }
                    title.setText(t);
                }
            });

        Intent intent = getIntent();
        String url=intent.getStringExtra("url");
        if (url != null) {
            wv.loadUrl(url);
            updateSubtitle(url);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            Uri uri = intent.getData();
            String mimeType = getContentResolver().getType(uri);
            if ("text/plain".equals(mimeType) || "text/xml".equals(mimeType) || "text/html".equals(mimeType)) {
                wv.loadUrl("file://"+getRealPathFromUri(this,uri));
                updateSubtitle("file://"+getRealPathFromUri(this,uri));
            } else {url = uri.toString();
                wv.loadUrl(url);
                updateSubtitle(url);
            }
        } else {
            url = "about:blank";
            wv.loadUrl(url);
            updateSubtitle(url);
        }
    }
    @Override
    public void onBackPressed() {
        if (wv.canGoBack()) {
            wv.goBack();
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), getString(R.string.s_press_again_2_exit),
                               Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }

        }
    }
    public void updateSubtitle(String text) {
        if (text.length() > 72) {
            subtitle.setTextSize(10);
        } else if (text.length() > 56) {
            subtitle.setTextSize(12);
        } else {
            subtitle.setTextSize(14);
        }
        subtitle.setText(text);
    }
    public static String getRealPathFromUri(Context context, Uri uri) {
        String path = null;

        // 判断Uri的scheme
        String scheme = uri.getScheme();
        if (scheme != null) {
            if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                // file:// 类型的Uri直接获取路径
                path = uri.getPath();
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                // content:// 类型的Uri需要通过查询ContentProvider
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
                    // Android 4.4及以上使用DocumentsContract处理
                    if (isExternalStorageDocument(uri)) {
                        // ExternalStorageProvider
                        String docId = DocumentsContract.getDocumentId(uri);
                        String[] split = docId.split(":");
                        if ("primary".equalsIgnoreCase(split[0])) {
                            path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        }
                    } else if (isDownloadsDocument(uri)) {
                        // DownloadsProvider
                        String id = DocumentsContract.getDocumentId(uri);
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        path = getDataColumn(context, contentUri, null, null);
                    } else if (isMediaDocument(uri)) {
                        // MediaProvider
                        String docId = DocumentsContract.getDocumentId(uri);
                        String[] split = docId.split(":");
                        String type = split[0];
                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }
                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{split[1]};
                        path = getDataColumn(context, contentUri, selection, selectionArgs);
                    }
                } else {
                    // 旧版ContentProvider
                    path = getDataColumn(context, uri, null, null);
                }
            }
        }
        return path;
    }

    /**
     * 获取数据库表中的_data列，即文件的具体路径。
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    public void copyToClipboard(Context context, String text) {
        // 获取系统的剪贴板服务
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            // 创建ClipData对象，第一个参数是标签，一般用于表示数据的类型，第二个参数是要复制的数据
            ClipData clip = ClipData.newPlainText("label", text);
            // 将ClipData内容放到系统剪贴板里
            clipboard.setPrimaryClip(clip);

            // 可选：提示用户已复制
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }
    }
    public void menu(View v) {
        CharSequence[] items = {getString(R.string.activity_WebviewActivity_menu_reload),getString(R.string.activity_WebviewActivity_menu_copy)};
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.activity_WebviewActivity_menu))
            .setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dia, int which) {
                    switch (which) {
                        case 0:
                            wv.loadUrl(wv.getUrl());
                            break;
                        case 1:
                            copyToClipboard(mc, wv.getUrl());
                            break;
                    }
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.s_exit, new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface p1, int p2) {
                    finish();
                }


            })
            .create();
        dialog.show();
    }

}
