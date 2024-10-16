package io.github.chipppppppppp.lime.hooks;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.chipppppppppp.lime.LimeOptions;
import io.github.chipppppppppp.lime.Main;
import io.github.chipppppppppp.lime.R;
import io.github.chipppppppppp.lime.Utils;

public class EmbedOptions implements IHook {
    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (Main.xModulePrefs.getBoolean("unembed_options", false)) return;
        XposedBridge.hookAllMethods(
                loadPackageParam.classLoader.loadClass("com.linecorp.line.settings.main.LineUserMainSettingsFragment"),
                "onViewCreated",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        ViewGroup viewGroup = ((ViewGroup) param.args[0]);
                        Context context = viewGroup.getContext();
                        Utils.addModuleAssetPath(context);

                        SharedPreferences prefs = context.getSharedPreferences(Constants.MODULE_NAME + "-options", Context.MODE_PRIVATE);

                        LinearLayout layout = new LinearLayout(context);
                        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context));

                        Switch switchRedirectWebView = null;
                        for (LimeOptions.Option option : limeOptions.options) {
                            final String name = option.name;

                            Switch switchView = new Switch(context);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = Utils.dpToPx(20, context);
                            switchView.setLayoutParams(params);
                            switchView.setText(option.id);
                            switchView.setChecked(option.checked);

                            if (name.equals("redirect_webview")) switchRedirectWebView = switchView;
                            else if (name.equals("open_in_browser")) {
                                switchRedirectWebView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) switchView.setEnabled(true);
                                    else {
                                        switchView.setChecked(false);
                                        switchView.setEnabled(false);
                                    }
                                });
                                switchView.setEnabled(limeOptions.redirectWebView.checked);
                            }

                            layout.addView(switchView);


                        }

                        {
                            final String script = new String(Base64.decode(prefs.getString("encoded_js_modify_request", ""), Base64.NO_WRAP));

                            LinearLayout layoutModifyRequest = new LinearLayout(context);
                            layoutModifyRequest.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            layoutModifyRequest.setOrientation(LinearLayout.VERTICAL);
                            layoutModifyRequest.setPadding(Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context));

                            EditText editText = new EditText(context);
                            editText.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            editText.setTypeface(Typeface.MONOSPACE);
                            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                                    InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                            editText.setMovementMethod(new ScrollingMovementMethod());
                            editText.setTextIsSelectable(true);
                            editText.setHorizontallyScrolling(true);
                            editText.setVerticalScrollBarEnabled(true);
                            editText.setHorizontalScrollBarEnabled(true);
                            editText.setText(script);

                            layoutModifyRequest.addView(editText);

                            LinearLayout buttonLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            buttonParams.topMargin = Utils.dpToPx(10, context);
                            buttonLayout.setLayoutParams(buttonParams);
                            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                            Button copyButton = new Button(context);
                            copyButton.setText(R.string.button_copy);
                            copyButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("", editText.getText().toString());
                                    clipboard.setPrimaryClip(clip);
                                }
                            });

                            buttonLayout.addView(copyButton);

                            Button pasteButton = new Button(context);
                            pasteButton.setText(R.string.button_paste);
                            pasteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (clipboard != null && clipboard.hasPrimaryClip()) {
                                        ClipData clip = clipboard.getPrimaryClip();
                                        if (clip != null && clip.getItemCount() > 0) {
                                            CharSequence pasteData = clip.getItemAt(0).getText();
                                            editText.setText(pasteData);
                                        }
                                    }
                                }
                            });

                            buttonLayout.addView(pasteButton);

                            layoutModifyRequest.addView(buttonLayout);

                            ScrollView scrollView = new ScrollView(context);

                            scrollView.addView(layoutModifyRequest);

                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                    .setTitle(R.string.modify_request);

                            builder.setView(scrollView);
                            // ここにバックアップボタンを追加
                            Button backupButton = new Button(context);

                            buttonParams.topMargin = Utils.dpToPx(20, context);
                            backupButton.setLayoutParams(buttonParams);
                            backupButton.setText("バックアップ");
                            backupButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    backupChatHistory(context); // バックアップメソッドを呼び出す
                     
                                }
                            });
                            layout.addView(backupButton);
                            
                            Button restoreButton = new Button(context);
                            restoreButton.setLayoutParams(buttonParams);
                            restoreButton.setText("リストア");
                            restoreButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    restoreChatHistory(context); // リストアメソッドを呼び出す
                                }
                            });
                            layout.addView(restoreButton);


                            builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String code = editText.getText().toString();
                                    if (!code.equals(script)) {
                                        prefs.edit().putString("encoded_js_modify_request", Base64.encodeToString(code.getBytes(), Base64.NO_WRAP)).commit();
                                        Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                        Process.killProcess(Process.myPid());
                                        context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                    }
                                }
                            });

                            builder.setNegativeButton(R.string.negative_button, null);

                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    editText.setText(script);
                                }
                            });

                            AlertDialog dialog = builder.create();

                            Button button = new Button(context);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = Utils.dpToPx(20, context);
                            button.setLayoutParams(params);
                            button.setText(R.string.modify_request);

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.show();
                                }
                            });

                            layout.addView(button);
                        }

                        {
                            final String script = new String(Base64.decode(prefs.getString("encoded_js_modify_response", ""), Base64.NO_WRAP));

                            LinearLayout layoutModifyResponse = new LinearLayout(context);
                            layoutModifyResponse.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            layoutModifyResponse.setOrientation(LinearLayout.VERTICAL);
                            layoutModifyResponse.setPadding(Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context), Utils.dpToPx(20, context));

                            EditText editText = new EditText(context);
                            editText.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            editText.setTypeface(Typeface.MONOSPACE);
                            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
                                    InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                            editText.setMovementMethod(new ScrollingMovementMethod());
                            editText.setTextIsSelectable(true);
                            editText.setHorizontallyScrolling(true);
                            editText.setVerticalScrollBarEnabled(true);
                            editText.setHorizontalScrollBarEnabled(true);
                            editText.setText(script);

                            layoutModifyResponse.addView(editText);

                            LinearLayout buttonLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            buttonParams.topMargin = Utils.dpToPx(10, context);
                            buttonLayout.setLayoutParams(buttonParams);
                            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

                            Button copyButton = new Button(context);
                            copyButton.setText(R.string.button_copy);
                            copyButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("", editText.getText().toString());
                                    clipboard.setPrimaryClip(clip);
                                }
                            });

                            buttonLayout.addView(copyButton);

                            Button pasteButton = new Button(context);
                            pasteButton.setText(R.string.button_paste);
                            pasteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (clipboard != null && clipboard.hasPrimaryClip()) {
                                        ClipData clip = clipboard.getPrimaryClip();
                                        if (clip != null && clip.getItemCount() > 0) {
                                            CharSequence pasteData = clip.getItemAt(0).getText();
                                            editText.setText(pasteData);
                                        }
                                    }
                                }
                            });


                            buttonLayout.addView(pasteButton);

                            layoutModifyResponse.addView(buttonLayout);

                            ScrollView scrollView = new ScrollView(context);

                            scrollView.addView(layoutModifyResponse);

                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                    .setTitle(R.string.modify_response);

                            builder.setView(scrollView);

                            builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String code = editText.getText().toString();
                                    if (!code.equals(script)) {
                                        prefs.edit().putString("encoded_js_modify_response", Base64.encodeToString(code.getBytes(), Base64.NO_WRAP)).commit();
                                        Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                        Process.killProcess(Process.myPid());
                                        context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                    }
                                }
                            });

                            builder.setNegativeButton(R.string.negative_button, null);

                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    editText.setText(script);
                                }
                            });

                            AlertDialog dialog = builder.create();

                            Button button = new Button(context);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.topMargin = Utils.dpToPx(20, context);
                            button.setLayoutParams(params);
                            button.setText(R.string.modify_response);

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.show();
                                }
                            });

                            layout.addView(button);
                        }

                        ScrollView scrollView = new ScrollView(context);

                        scrollView.addView(layout);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle(R.string.options_title);

                        builder.setView(scrollView);

                        builder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean optionChanged = false;
                                for (int i = 0; i < limeOptions.options.length; ++i) {
                                    Switch switchView = (Switch) layout.getChildAt(i);
                                    if (limeOptions.options[i].checked != switchView.isChecked()) {
                                        optionChanged = true;
                                    }
                                    prefs.edit().putBoolean(limeOptions.options[i].name, switchView.isChecked()).commit();
                                }

                                if (optionChanged) {
                                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.restarting), Toast.LENGTH_SHORT).show();
                                    Process.killProcess(Process.myPid());
                                    context.startActivity(new Intent().setClassName(Constants.PACKAGE_NAME, "jp.naver.line.android.activity.SplashActivity"));
                                }
                            }
                        });

                        builder.setNegativeButton(R.string.negative_button, null);

                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                for (int i = 0; i < limeOptions.options.length; ++i) {
                                    Switch switchView = (Switch) layout.getChildAt(i);
                                    switchView.setChecked(limeOptions.options[i].checked);
                                }
                            }
                        });

                        AlertDialog dialog = builder.create();

                        Button button = new Button(context);
                        button.setText(R.string.app_name);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.gravity = Gravity.TOP | Gravity.END;
                        layoutParams.rightMargin = Utils.dpToPx(10, context);
                        layoutParams.topMargin = Utils.dpToPx(5, context);
                        button.setLayoutParams(layoutParams);

                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.show();
                            }
                        });

                        FrameLayout frameLayout = new FrameLayout(context);
                        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));

                        frameLayout.addView(button);

                        viewGroup.addView(frameLayout);
                    }
                }
        );
    }


    private LinearLayout createLayout(Context context, Context moduleContext) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addButton(layout, "バックアップ", v -> backupChatHistory(context));
        addButton(layout, "リストア", v -> restoreChatHistory(context));

        return layout;
    }

    private void addButton(LinearLayout layout, String buttonText, View.OnClickListener listener) {
        Button button = new Button(layout.getContext());
        button.setText(buttonText);
        button.setOnClickListener(listener);
        layout.addView(button);
    }

    private void backupChatHistory( Context appContext) {
        // Get the original database file using appContext
        File originalDbFile = appContext.getDatabasePath("naver_line");

        // Create the backup directory path
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");

        // Ensure the backup directory exists
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                Log.e(TAG, "Failed to create backup directory: " + backupDir.getAbsolutePath());
                return;
            }
        }

        // Now we can create a file in the backup directory
        File backupFile = new File(backupDir, "naver_line_backup.db");
        // Backup succeeded, now share the backup file
        shareBackupFileInHookedApp(appContext, backupFile);
        // Perform the backup
        try (FileChannel source = new FileInputStream(originalDbFile).getChannel();
             FileChannel destination = new FileOutputStream(backupFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());

            Log.i(TAG, "Backup successfully created: " + backupFile.getAbsolutePath());


        } catch (IOException e) {
            Log.e(TAG, "Error while creating backup", e);
        }
    }


    private void shareBackupFileInHookedApp(Context hookedAppContext, File backupFile) {
        if (!backupFile.exists()) {
            Toast.makeText(hookedAppContext, "バックアップファイルが存在しません", Toast.LENGTH_SHORT).show();
            return;
        }

        // ファイルのURIを取得
        Uri fileUri = FileProvider.getUriForFile(
                hookedAppContext,
                hookedAppContext.getPackageName() + ".fileprovider", // フックしているアプリのパッケージ名を使用
                backupFile
        );

        // 共有用のIntentを作成
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/octet-stream"); // バイナリファイルのMIMEタイプ
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // URIの読み取り権限を付与

        // フックしているアプリのコンテキストを使用してIntentを発行
        hookedAppContext.startActivity(Intent.createChooser(shareIntent, "バックアップファイルを共有"));
    }



    private void createBackup(File originalDbFile, File temporaryBackupFile, Context context) {
        try (FileChannel source = new FileInputStream(originalDbFile).getChannel();
             FileChannel destination = new FileOutputStream(temporaryBackupFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
            showToast(context, "バックアップが成功しました");
            Log.i(TAG, "Backup created successfully: " + temporaryBackupFile.getAbsolutePath());
        } catch (IOException e) {
            showToast(context, "バックアップ中にエラーが発生しました: " + e.getMessage());
            Log.e(TAG, "Error while creating backup", e);
        }
    }


    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    private void restoreChatHistory(Context context) {
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
        File backupDbFile = new File(backupDir, "naver_line_backup.db");

        if (!backupDbFile.exists()) {
            Toast.makeText(context, "バックアップファイルが見つかりません", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "バックアップファイルが存在しません。処理を終了します。");
            return;
        }

        SQLiteDatabase backupDb = null;
        SQLiteDatabase originalDb = null;
        try {
            backupDb = SQLiteDatabase.openDatabase(backupDbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            originalDb = context.openOrCreateDatabase("naver_line", Context.MODE_PRIVATE, null);

            // chat_historyテーブルの復元
            Cursor cursor = backupDb.rawQuery("SELECT * FROM chat_history", null);
            if (cursor.moveToFirst()) {
                do {
                    String serverId = cursor.getString(cursor.getColumnIndex("server_id"));
                    Integer type = cursor.isNull(cursor.getColumnIndex("type")) ? null : cursor.getInt(cursor.getColumnIndex("type"));
                    String chatId = cursor.getString(cursor.getColumnIndex("chat_id"));
                    String fromMid = cursor.getString(cursor.getColumnIndex("from_mid"));
                    String content = cursor.getString(cursor.getColumnIndex("content"));
                    String createdTime = cursor.getString(cursor.getColumnIndex("created_time"));
                    String deliveredTime = cursor.getString(cursor.getColumnIndex("delivered_time"));
                    Integer status = cursor.isNull(cursor.getColumnIndex("status")) ? null : cursor.getInt(cursor.getColumnIndex("status"));
                    Integer sentCount = cursor.isNull(cursor.getColumnIndex("sent_count")) ? null : cursor.getInt(cursor.getColumnIndex("sent_count"));
                    Integer readCount = cursor.isNull(cursor.getColumnIndex("read_count")) ? null : cursor.getInt(cursor.getColumnIndex("read_count"));
                    String locationName = cursor.getString(cursor.getColumnIndex("location_name"));
                    String locationAddress = cursor.getString(cursor.getColumnIndex("location_address"));
                    String locationPhone = cursor.getString(cursor.getColumnIndex("location_phone"));
                    Integer locationLatitude = cursor.isNull(cursor.getColumnIndex("location_latitude")) ? null : cursor.getInt(cursor.getColumnIndex("location_latitude"));
                    Integer locationLongitude = cursor.isNull(cursor.getColumnIndex("location_longitude")) ? null : cursor.getInt(cursor.getColumnIndex("location_longitude"));
                    Integer attachmentImage = cursor.isNull(cursor.getColumnIndex("attachement_image")) ? null : cursor.getInt(cursor.getColumnIndex("attachement_image"));
                    Integer attachmentImageHeight = cursor.isNull(cursor.getColumnIndex("attachement_image_height")) ? null : cursor.getInt(cursor.getColumnIndex("attachement_image_height"));
                    Integer attachmentImageWidth = cursor.isNull(cursor.getColumnIndex("attachement_image_width")) ? null : cursor.getInt(cursor.getColumnIndex("attachement_image_width"));
                    Integer attachmentImageSize = cursor.isNull(cursor.getColumnIndex("attachement_image_size")) ? null : cursor.getInt(cursor.getColumnIndex("attachement_image_size"));
                    Integer attachmentType = cursor.isNull(cursor.getColumnIndex("attachement_type")) ? null : cursor.getInt(cursor.getColumnIndex("attachement_type"));
                    String attachmentLocalUri = cursor.getString(cursor.getColumnIndex("attachement_local_uri"));
                    String parameter = cursor.getString(cursor.getColumnIndex("parameter"));
                    byte[] chunks = cursor.getBlob(cursor.getColumnIndex("chunks"));

                    // server_idがnullの場合はスキップ
                    if (serverId == null) {
                        continue;
                    }

                    // ContentValuesを使用してデータを挿入
                    ContentValues values = new ContentValues();
                    values.put("server_id", serverId);
                    values.put("type", type);
                    values.put("chat_id", chatId);
                    values.put("from_mid", fromMid);
                    values.put("content", content);
                    values.put("created_time", createdTime);
                    values.put("delivered_time", deliveredTime);
                    values.put("status", status);
                    values.put("sent_count", sentCount);
                    values.put("read_count", readCount);
                    values.put("location_name", locationName);
                    values.put("location_address", locationAddress);
                    values.put("location_phone", locationPhone);
                    values.put("location_latitude", locationLatitude);
                    values.put("location_longitude", locationLongitude);
                    values.put("attachement_image", attachmentImage);
                    values.put("attachement_image_height", attachmentImageHeight);
                    values.put("attachement_image_width", attachmentImageWidth);
                    values.put("attachement_image_size", attachmentImageSize);
                    values.put("attachement_type", attachmentType);
                    values.put("attachement_local_uri", attachmentLocalUri);
                    values.put("parameter", parameter);
                    values.put("chunks", chunks);

                    // データベースに挿入
                    originalDb.insertWithOnConflict("chat_history", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                } while (cursor.moveToNext());
            }
            cursor.close();

            Toast.makeText(context, "リストアが成功しました", Toast.LENGTH_SHORT).show();
            restoreChat(context);
        } catch (Exception e) {
            Toast.makeText(context, "リストア中にエラーが発生しました", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "チャット履歴のリストア中にエラーが発生しました", e);
        }

    }



    private void restoreChat(Context context) {
        File backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LimeBackup");
        File backupDbFile = new File(backupDir, "naver_line_backup.db");

        if (!backupDbFile.exists()) {
            Toast.makeText(context, "バックアップファイルが見つかりません", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "バックアップファイルが存在しません。処理を終了します。");
            return;
        }

        SQLiteDatabase backupDb = null;
        SQLiteDatabase originalDb = null;
        try {
            backupDb = SQLiteDatabase.openDatabase(backupDbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            originalDb = context.openOrCreateDatabase("naver_line", Context.MODE_PRIVATE, null);

            // chatテーブルの復元
            Cursor cursor = backupDb.rawQuery("SELECT * FROM chat", null);
            if (cursor.moveToFirst()) {
                do {
                    String chatId = cursor.getString(cursor.getColumnIndex("chat_id"));
                    String chatName = cursor.getString(cursor.getColumnIndex("chat_name"));
                    String ownerMid = cursor.getString(cursor.getColumnIndex("owner_mid"));
                    String lastFromMid = cursor.getString(cursor.getColumnIndex("last_from_mid"));
                    String lastMessage = cursor.getString(cursor.getColumnIndex("last_message"));
                    String lastCreatedTime = cursor.getString(cursor.getColumnIndex("last_created_time"));
                    Integer messageCount = cursor.isNull(cursor.getColumnIndex("message_count")) ? null : cursor.getInt(cursor.getColumnIndex("message_count"));
                    Integer readMessageCount = cursor.isNull(cursor.getColumnIndex("read_message_count")) ? null : cursor.getInt(cursor.getColumnIndex("read_message_count"));
                    Integer latestMentionedPosition = cursor.isNull(cursor.getColumnIndex("latest_mentioned_position")) ? null : cursor.getInt(cursor.getColumnIndex("latest_mentioned_position"));
                    Integer type = cursor.isNull(cursor.getColumnIndex("type")) ? null : cursor.getInt(cursor.getColumnIndex("type"));
                    Integer isNotification = cursor.isNull(cursor.getColumnIndex("is_notification")) ? null : cursor.getInt(cursor.getColumnIndex("is_notification"));
                    String skinKey = cursor.getString(cursor.getColumnIndex("skin_key"));
                    String inputText = cursor.getString(cursor.getColumnIndex("input_text"));
                    String inputTextMetadata = cursor.getString(cursor.getColumnIndex("input_text_metadata"));
                    Integer hideMember = cursor.isNull(cursor.getColumnIndex("hide_member")) ? null : cursor.getInt(cursor.getColumnIndex("hide_member"));
                    Integer pTimer = cursor.isNull(cursor.getColumnIndex("p_timer")) ? null : cursor.getInt(cursor.getColumnIndex("p_timer"));
                    String lastMessageDisplayTime = cursor.getString(cursor.getColumnIndex("last_message_display_time"));
                    String midP = cursor.getString(cursor.getColumnIndex("mid_p"));
                    Integer isArchived = cursor.isNull(cursor.getColumnIndex("is_archived")) ? null : cursor.getInt(cursor.getColumnIndex("is_archived"));
                    String readUp = cursor.getString(cursor.getColumnIndex("read_up"));
                    Integer isGroupCalling = cursor.isNull(cursor.getColumnIndex("is_groupcalling")) ? null : cursor.getInt(cursor.getColumnIndex("is_groupcalling"));
                    Integer latestAnnouncementSeq = cursor.isNull(cursor.getColumnIndex("latest_announcement_seq")) ? null : cursor.getInt(cursor.getColumnIndex("latest_announcement_seq"));
                    Integer announcementViewStatus = cursor.isNull(cursor.getColumnIndex("announcement_view_status")) ? null : cursor.getInt(cursor.getColumnIndex("announcement_view_status"));
                    String lastMessageMetaData = cursor.getString(cursor.getColumnIndex("last_message_meta_data"));
                    String chatRoomBgmData = cursor.getString(cursor.getColumnIndex("chat_room_bgm_data"));
                    Integer chatRoomBgmChecked = cursor.isNull(cursor.getColumnIndex("chat_room_bgm_checked")) ? null : cursor.getInt(cursor.getColumnIndex("chat_room_bgm_checked"));
                    Integer chatRoomShouldShowBgmBadge = cursor.isNull(cursor.getColumnIndex("chat_room_should_show_bgm_badge")) ? null : cursor.getInt(cursor.getColumnIndex("chat_room_should_show_bgm_badge"));
                    String unreadTypeAndCount = cursor.getString(cursor.getColumnIndex("unread_type_and_count"));

                    // chat_idがnullの場合はスキップ
                    if (chatId == null) {
                        continue;
                    }

                    // ContentValuesを使用してデータを挿入
                    ContentValues values = new ContentValues();
                    values.put("chat_id", chatId);
                    values.put("chat_name", chatName);
                    values.put("owner_mid", ownerMid);
                    values.put("last_from_mid", lastFromMid);
                    values.put("last_message", lastMessage);
                    values.put("last_created_time", lastCreatedTime);
                    values.put("message_count", messageCount);
                    values.put("read_message_count", readMessageCount);
                    values.put("latest_mentioned_position", latestMentionedPosition);
                    values.put("type", type);
                    values.put("is_notification", isNotification);
                    values.put("skin_key", skinKey);
                    values.put("input_text", inputText);
                    values.put("input_text_metadata", inputTextMetadata);
                    values.put("hide_member", hideMember);
                    values.put("p_timer", pTimer);
                    values.put("last_message_display_time", lastMessageDisplayTime);
                    values.put("mid_p", midP);
                    values.put("is_archived", isArchived);
                    values.put("read_up", readUp);
                    values.put("is_groupcalling", isGroupCalling);
                    values.put("latest_announcement_seq", latestAnnouncementSeq);
                    values.put("announcement_view_status", announcementViewStatus);
                    values.put("last_message_meta_data", lastMessageMetaData);
                    values.put("chat_room_bgm_data", chatRoomBgmData);
                    values.put("chat_room_bgm_checked", chatRoomBgmChecked);
                    values.put("chat_room_should_show_bgm_badge", chatRoomShouldShowBgmBadge);
                    values.put("unread_type_and_count", unreadTypeAndCount);

                    // データベースに挿入
                    originalDb.insertWithOnConflict("chat", null, values, SQLiteDatabase.CONFLICT_IGNORE);
                } while (cursor.moveToNext());
            }
            cursor.close();

            Toast.makeText(context, "chatテーブルのリストアが成功しました", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "リストア中にエラーが発生しました", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "chatテーブルのリストア中にエラーが発生しました", e);
        } finally {
            if (backupDb != null) {
                backupDb.close();
            }
            if (originalDb != null) {
                originalDb.close();
            }
        }
    }

}
