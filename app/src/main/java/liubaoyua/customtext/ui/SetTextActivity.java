package liubaoyua.customtext.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import liubaoyua.customtext.R;
import liubaoyua.customtext.adapters.TextRecyclerAdapter;
import liubaoyua.customtext.app.AppHelper;
import liubaoyua.customtext.app.MyApplication;
import liubaoyua.customtext.databinding.ActivitySetTextBinding;
import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.entity.AppPreference;
import liubaoyua.customtext.entity.CustomText;
import liubaoyua.customtext.entity.DataLoadedEvent;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.Utils;


public class SetTextActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    private static ArrayList<CustomText> clipboard = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private TextRecyclerAdapter textRecyclerAdapter;
    private SwitchCompat switchCompat;
    private Toolbar toolbar;
    private AppBarLayout appbar;

    private String packageName = "";
    private String appName = "";
    private AppPreference prefHelper;

    private boolean isInActionMode;
    private ActionMode actionMode;
    private ActionMode.Callback mCallback = new ActionMode.Callback() {
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_copy: {
                    clipboard = textRecyclerAdapter.getSelectedItem();
                    break;
                }
                case R.id.menu_cut: {
                    clipboard = textRecyclerAdapter.cutSelectedItem();
                    break;
                }
                case R.id.menu_paste: {
                    textRecyclerAdapter.pasteClipBoard(clipboard);
                    break;
                }
                case R.id.menu_select_all: {
                    textRecyclerAdapter.selectAll();
                }
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            isInActionMode = true;
            textRecyclerAdapter.multiSelectMode = true;
            textRecyclerAdapter.notifyDataSetChanged();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            textRecyclerAdapter.deselectAllItem();
            isInActionMode = false;
            textRecyclerAdapter.multiSelectMode = false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySetTextBinding b = DataBindingUtil.setContentView(this, R.layout.activity_set_text);

        Utils.configStatusBarColor(this);
        appbar = b.appBar;
        toolbar = b.toolbar;
        toolbar.inflateMenu(R.menu.menu_set_text);
        toolbar.setTitle(getString(R.string.title_activity_settings));
        toolbar.setNavigationIcon(R.mipmap.ic_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(this);



        final FloatingActionButton fab = b.fabButton;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        mRecyclerView = b.recyclerView;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        textRecyclerAdapter = new TextRecyclerAdapter(SetTextActivity.this, null, mRecyclerView);
        mRecyclerView.setAdapter(textRecyclerAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            float firstRawY, oldRawY, newRawY;
            boolean flag = false;
            float dy;

            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;

            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN: // 按下事件，记录按下时手指在悬浮窗的XY坐标值
                        firstRawY = oldRawY = event.getRawY();
                        flag = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        newRawY = event.getRawY();
                        dy = newRawY - oldRawY;
                        if (oldRawY == 0) {
                            firstRawY = oldRawY = newRawY;
                            flag = false;
                            break;
                        }
                        if (dy < -50) {
                            if (fab.getVisibility() == View.VISIBLE) {
                                ObjectAnimator animator = ObjectAnimator.ofFloat(fab,
                                        "translationY", 0, fab.getHeight() + fabBottomMargin);
                                animator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        fab.setVisibility(View.INVISIBLE);
                                    }
                                });
                                animator.start();
                            }
                        } else if (dy > 50) {           // 下滑
                            if (fab.getVisibility() == View.INVISIBLE) {
                                fab.setVisibility(View.VISIBLE);
                                ObjectAnimator animator = ObjectAnimator.ofFloat(fab,
                                        "translationY", fab.getHeight() + fabBottomMargin, 0);
                                animator.start();
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        flag = false;
                        oldRawY = 0;
                }
                return false;
            }
        });


        reloadData();

    }

    private void reloadData() {
        packageName = getIntent().getStringExtra(Common.PACKAGE_NAME_ARG);
        prefHelper = new AppPreference(packageName);
        if (Common.FAST_DEBUG) {
            packageName = Common.SYSTEM_UI_PACKAGE_NAME;
        }

        if (packageName.equals(Common.GLOBAL_SETTING_PACKAGE_NAME)) {
            appName = getString(R.string.global_replacement);
        } else if (packageName.equals(Common.SHARING_SETTING_PACKAGE_NAME)) {
            appName = getString(R.string.enabled_replacement);
        } else {
            PackageInfo packageInfo = Utils.getPackageInfoByPackageName(this, packageName);
            if (packageInfo == null) {
                Toast.makeText(this.getApplicationContext(), getString(R.string.error_found)
                        + packageName, Toast.LENGTH_SHORT).show();
                this.finish();
            } else {
                appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            }
        }

        toolbar.setTitle(appName);
        onCreateToolbarMenu(toolbar.getMenu());

        textRecyclerAdapter.setData(prefHelper.getClonedData());
    }


    public boolean onCreateToolbarMenu(Menu menu) {

        // systemui 要特殊处理
        if (!packageName.equals(Common.SYSTEM_UI_PACKAGE_NAME)) {
            if (getPackageManager().getLaunchIntentForPackage(packageName) == null) {
                MenuItem relaunchMenuItem = menu.findItem(R.id.action_relaunch_app);
                relaunchMenuItem.setEnabled(false);
            }
        }

        // 全局替换 和 共享替换有特殊处理
        if (packageName.equals(Common.GLOBAL_SETTING_PACKAGE_NAME)
                || packageName.equals(Common.SHARING_SETTING_PACKAGE_NAME)) {
            menu.findItem(R.id.action_market_link).setEnabled(false);
            menu.findItem(R.id.action_app_info).setEnabled(false);
            menu.findItem(R.id.action_relaunch_app).setEnabled(false);
            menu.findItem(R.id.action_imp_exp_pref).setEnabled(false);
        }

        MenuItem switchMenuItem = menu.findItem(R.id.action_switch).setVisible(true);
        switchCompat = (SwitchCompat) MenuItemCompat.getActionView(switchMenuItem);
        switchCompat.setChecked(prefHelper.isHookEnable());
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefHelper.setHookEnable(b);
            }
        });
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

//        <string name="menu_add_item">增加楼层</string>
//        <string name="menu_clear_empty">清除空行</string>
//        <string name="menu_relaunch_app">重开应用</string>
//        <string name="menu_app_info">应用信息</string>
//        <string name="menu_market_link">市场链接</string>
//        <string name="menu_clear_all">清除所有</string>

        if (id == android.R.id.home) {
            onBackPressed();

        } else if (id == R.id.action_add_item) {
            int size = textRecyclerAdapter.getData().size();
            for (int i = 0; i < 10; i++) {
                textRecyclerAdapter.getData().add(new CustomText());
                textRecyclerAdapter.notifyItemRangeInserted(size, size + 9);
                Snackbar.make(mRecyclerView
                        , getString(R.string.menu_add_item) + " " + getString(R.string.succeed)
                        , Snackbar.LENGTH_LONG).show();
            }

        } else if (id == R.id.action_clear_empty) {
            textRecyclerAdapter.removeEmptyData();
            Snackbar.make(mRecyclerView
                    , getString(R.string.menu_clear_empty) + " " + getString(R.string.succeed)
                    , Snackbar.LENGTH_LONG).show();

            prefHelper.removeEmptyItems();
        } else if (id == R.id.action_relaunch_app) {
            Utils.killPackage(packageName);
            if (!packageName.equals(Common.SYSTEM_UI_PACKAGE_NAME)) {
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                startActivity(LaunchIntent);
            }
        } else if (id == R.id.action_app_info) {
            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + packageName)));

        } else if (id == R.id.action_market_link) {
            Intent intent = new Intent("android.intent.action.VIEW",
                    Uri.parse("market://details?id=" + packageName));
            startActivity(intent);

        } else if (id == R.id.action_clear_all) {
            textRecyclerAdapter.notifyItemRangeRemoved(0, textRecyclerAdapter.getData().size());
            textRecyclerAdapter.setData(new ArrayList<CustomText>());
            for (int i = 0; i < Common.DEFAULT_NUM; i++) {
                textRecyclerAdapter.getData().add(new CustomText());
            }
            switchCompat.setChecked(false);
            Snackbar.make(mRecyclerView
                    , getString(R.string.menu_clear_all) + " " + getString(R.string.succeed)
                    , Snackbar.LENGTH_LONG).show();
        } else if (id == R.id.action_select_mode) {
            if (actionMode != null) {
                return false;
            } else {
                actionMode = toolbar.startActionMode(mCallback);
                return true;
            }

        } else if (id == R.id.action_imp_exp_pref) {
           AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCustomTitle(null);
            CharSequence[] sequences = new CharSequence[2];
            sequences[0] = getString(R.string.array_through_file);
            sequences[1] = getString(R.string.array_through_json);

            builder.setItems(sequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            impExpWithFile();
                            break;
                        case 1:
                        default:
                            impExpWithJson();
                    }
                }
            });

            Dialog dialog = builder.create();
            dialog.show();
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.width = displayMetrics.widthPixels * 3 / 4;
            dialog.getWindow().setAttributes(lp);


        } else if (id == R.id.action_extra_setting) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(SetTextActivity.this);
            builder.setTitle(R.string.dialog_override_global_setting);

            CharSequence[] texts = new CharSequence[2];
            texts[0] = getString(R.string.setting_more_type);
            texts[1] = getString(R.string.setting_use_regex);

            boolean[] check = new boolean[2];
            check[0] = prefHelper.isMoreTypeEnabled();
            check[1] = prefHelper.isUseRegex();

            builder.setMultiChoiceItems(texts, check,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (which == 0 ) {
                                prefHelper.setMoreTypeEnable(isChecked);
                            } else {
                                prefHelper.setUseRegex(isChecked);
                            }
                        }
                    });

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            Dialog dialog = builder.create();

            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void impExpWithJson() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SetTextActivity.this);
        builder.setTitle(R.string.menu_imp_exp_pref);
        builder.setMessage(getString(R.string.str_input_with_string));
        builder.setNegativeButton(R.string.dialog_neg_imp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showJsonInputDialog();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.dialog_pos_exp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String json = generateJson().toString();
                    ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                            .setPrimaryClip(ClipData.newPlainText("text", json));
                    Toast.makeText(SetTextActivity.this, R.string.str_copy_to_clipboard, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SetTextActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    private void showJsonInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.str_set_text_input_string));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_json_input, null);
        final EditText edittext = (EditText) view.findViewById(R.id.json_text);
        edittext.setHint(R.string.str_set_text_json_hint);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    parseJson(edittext.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SetTextActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }


    private void impExpWithFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SetTextActivity.this);
        builder.setTitle(R.string.menu_imp_exp_pref);
        builder.setMessage(R.string.dialog_imp_exp_message);
        builder.setNegativeButton(R.string.dialog_neg_imp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.getFileFromContent(SetTextActivity.this, Common.REQUEST_CODE_FOR_FILE);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.dialog_pos_exp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File srcFile = new File(MyApplication.prefsDir, packageName + ".xml");
                Utils.myLog("srcFile is " + srcFile);
                File destFile = new File(MyApplication.backupDir, appName + "_" + packageName + ".xml");
                try {
                    Utils.CopyFile(srcFile, destFile);
                    destFile.setReadable(true, false);
                    destFile.setWritable(true, false);
                    Toast.makeText(SetTextActivity.this,
                            getString(R.string.dialog_pos_exp) + getString(R.string.succeed) + "  "
                                    + destFile.toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(SetTextActivity.this,
                            getString(R.string.dialog_pos_exp) + getString(R.string.fail) + "  "
                                    + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }



    @Override
    public void onBackPressed() {
        if (!prefHelper.isDataEquals(textRecyclerAdapter.getData())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SetTextActivity.this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle(getString(R.string.dialog_data_not_saved));
            builder.setMessage(getString(R.string.dialog_save_now));
            builder.setPositiveButton(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveData();
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNeutralButton(getString(R.string.dialog_not_to_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            AppInfo info = Utils.getAppInfoByPackageName(packageName);
            if (info != null) {
                if (prefHelper.isHookEnable()) {
                    info.state = AppInfo.ENABLED;
                } else {
                    info.state = AppInfo.DISABLED;
                }
                EventBus.getDefault().post(new DataLoadedEvent());
            }
            super.onBackPressed();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.REQUEST_CODE_FOR_FILE && resultCode == RESULT_OK) {
            String file = data.getData().getSchemeSpecificPart();
            File srcFile = new File(file);
            Utils.myLog("scrFile is " + srcFile);
            if (srcFile.exists() && file.endsWith(".xml")) {
                File destFile = new File(MyApplication.prefsDir, packageName + ".xml");
                Utils.myLog("destFile is " + srcFile);
                try {
                    Utils.CopyFile(srcFile, destFile);
                    destFile.setReadable(true, false);
                    destFile.setWritable(true, false);
                    switchCompat.setChecked(true);
                    Toast.makeText(SetTextActivity.this,
                            getString(R.string.toast_imp_succed), Toast.LENGTH_LONG).show();
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try{
                                Thread.sleep(3000);
                            }catch (Exception e){

                            }
                            AppHelper.terminal();
                        }
                    }.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(SetTextActivity.this,
                            getString(R.string.toast_imp_fail) + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(SetTextActivity.this,
                        getString(R.string.toast_imp_fail_invaild_file), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveData() {
        ArrayList<CustomText> newData = textRecyclerAdapter.getData();
        prefHelper.setNewData(newData);
        if (switchCompat.isChecked()) {
            Snackbar.make(mRecyclerView
                    , getString(R.string.dialog_save) + " " + getString(R.string.succeed)
                    , Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mRecyclerView
                    , getString(R.string.dialog_save) + " " + getString(R.string.succeed)
                    + "," + getString(R.string.switch_is_not_activated)
                    , Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.open), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switchCompat.setChecked(true);
                        }
                    }).show();
        }
    }

    public JSONObject generateJson() throws JSONException {
        List<CustomText> texts = new ArrayList<>();
        texts.addAll(textRecyclerAdapter.getData());

        for (int i = 0; i < texts.size(); i++) {
            if (texts.get(i).isEmpty()) {
                texts.remove(i);
                i--;
            }
        }

        JSONArray array = new JSONArray();
        for (CustomText text : texts) {
            array.put(text.toJson());
        }

        JSONObject result = new JSONObject();
        result.put("packageName", packageName);
        result.put("hackMoreType", prefHelper.isMoreTypeEnabled());
        result.put("useRegex", prefHelper.isUseRegex());
        result.put("data", array);
        Utils.myLog(result.toString(4));
        return result;
    }

    public void parseJson(String json) throws JSONException {
        int start = json.indexOf("{");
        int end = json.lastIndexOf("}") + 1;

        if (start < 0 || end < 0) {
            Toast.makeText(SetTextActivity.this, "不是标准的 Json 文本", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject o = new JSONObject(json.substring(start, end));
        JSONArray array = o.optJSONArray("data");
        if (array != null) {
            ArrayList<CustomText> texts = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                texts.add(CustomText.fromJsonObject(array.getJSONObject(i)));
            }
            prefHelper.setMoreTypeEnable(o.optBoolean("hackMoreType"));
            prefHelper.setUseRegex( o.optBoolean("useRegex"));
            textRecyclerAdapter.addData(texts);
//            saveData();
            Toast.makeText(this, "读取成功", Toast.LENGTH_SHORT).show();
        }
    }

}
