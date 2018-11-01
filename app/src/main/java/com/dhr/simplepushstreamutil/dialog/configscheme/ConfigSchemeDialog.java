package com.dhr.simplepushstreamutil.dialog.configscheme;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.bean.ConfigLinuxPushBean;
import com.dhr.simplepushstreamutil.bean.ConfigSchemeBean;
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;

public class ConfigSchemeDialog extends Dialog {
    private SharedPreferencesUtil sharedPreferencesUtil;
    private Gson gson = new Gson();
    private Context context;

    private RadioGroup rgConfig;
    private RadioButton rb1;
    private RadioButton rb2;

    private Button btnOk;
    private Button btnCancel;

    public ConfigSchemeDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context = context;
        sharedPreferencesUtil = new SharedPreferencesUtil(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_configscheme);

        initView();
    }

    private void initView() {
        rgConfig = findViewById(R.id.rgConfig);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);

        String configLinuxPush = (String) sharedPreferencesUtil.getSharedPreference("ConfigScheme", "");
        if (configLinuxPush.isEmpty()) {
            ConfigLinuxPushBean configLinuxPushBean = new ConfigLinuxPushBean();
            configLinuxPushBean.setStatus(0);
            sharedPreferencesUtil.put("ConfigLinuxPush", gson.toJson(configLinuxPushBean));
        }
        configLinuxPush = (String) sharedPreferencesUtil.getSharedPreference("ConfigScheme", "");
        ConfigLinuxPushBean configLinuxPushBean = gson.fromJson(configLinuxPush, ConfigLinuxPushBean.class);
        switch (configLinuxPushBean.getStatus()) {
            case 0:
                rb1.setChecked(true);
                break;
            case 1:
                rb2.setChecked(true);
                break;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnCancel:
                    dismiss();
                    break;
                case R.id.btnOk:
                    ConfigSchemeBean configSchemeBean = new ConfigSchemeBean();
                    switch (rgConfig.getCheckedRadioButtonId()) {
                        case R.id.rb1:
                            configSchemeBean.setSchemeType(0);
                            break;
                        case R.id.rb2:
                            configSchemeBean.setSchemeType(1);
                            break;
                    }
                    sharedPreferencesUtil.put("ConfigScheme", gson.toJson(configSchemeBean));
                    dismiss();
                    break;
            }
        }
    };
}
