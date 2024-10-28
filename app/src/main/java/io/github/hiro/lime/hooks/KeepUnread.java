package io.github.hiro.lime.hooks;
import android.content.Context;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.hiro.lime.LimeOptions;

public class KeepUnread implements IHook {

    @Override
    public void hook(LimeOptions limeOptions, XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (limeOptions.removeKeepUnread.checked) return;


        XposedHelpers.findAndHookMethod(
                "com.linecorp.line.chatlist.view.fragment.ChatListFragment",
                loadPackageParam.classLoader,
                "onCreateView",
                LayoutInflater.class, ViewGroup.class, android.os.Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View rootView = (View) param.getResult();
                        Context context = rootView.getContext();

                        // レイアウトを作成
                        RelativeLayout layout = new RelativeLayout(context);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layout.setLayoutParams(layoutParams);

                        // スイッチの状態をファイルから読み取る
                        boolean isChecked = readStateFromFile(context); // ファイルから状態を読み込む

                        // スイッチを作成
                        Switch switchView = new Switch(context);
                        switchView.setText(""); // テキストが必要ない場合は空に
                        switchView.setTextColor(Color.WHITE); // テキストの色を白に設定

                        // スイッチのレイアウトパラメータを設定
                        RelativeLayout.LayoutParams switchParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        switchParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); // 中央に配置

                        // スイッチの状態を設定
                        switchView.setChecked(isChecked);

                        // スイッチのリスナーを設定
                        switchView.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                            saveStateToFile(context, isChecked1); // ファイルに状態を保存
                        });

                        // レイアウトにスイッチを追加
                        layout.addView(switchView, switchParams);

                        // 既存のビューにスイッチのレイアウトを追加
                        if (rootView instanceof ViewGroup) {
                            ViewGroup rootViewGroup = (ViewGroup) rootView;
                            if (rootViewGroup.getChildCount() > 0 && rootViewGroup.getChildAt(0) instanceof ListView) {
                                ListView listView = (ListView) rootViewGroup.getChildAt(0);
                                listView.addFooterView(layout); // ListViewにフッターとして追加
                            } else {
                                rootViewGroup.addView(layout); // 他のビューに追加
                            }
                        }
                    }

                    // スイッチの状態をファイルに保存するメソッド
                    private void saveStateToFile(Context context, boolean state) {
                        String filename = "keep_unread_state.txt";
                        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                            fos.write((state ? "1" : "0").getBytes()); // 状態をファイルに書き込む
                        } catch (IOException e) {
                            e.printStackTrace(); // エラーハンドリング
                        }
                    }

                    // スイッチの状態をファイルから読み取るメソッド
                    private boolean readStateFromFile(Context context) {
                        String filename = "keep_unread_state.txt";
                        try (FileInputStream fis = context.openFileInput(filename)) {
                            int c;
                            StringBuilder sb = new StringBuilder();
                            while ((c = fis.read()) != -1) {
                                sb.append((char) c); // 読み込んだデータを文字列に変換
                            }
                            return "1".equals(sb.toString()); // 状態を判定
                        } catch (IOException e) {
                            e.printStackTrace(); // エラーハンドリング
                            return false; // 例外が発生した場合はデフォルト値を返す
                        }
                    }
                }
        );


        XposedHelpers.findAndHookMethod(
                loadPackageParam.classLoader.loadClass(Constants.MARK_AS_READ_HOOK.className),
                Constants.MARK_AS_READ_HOOK.methodName,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                }

        );
    }
}