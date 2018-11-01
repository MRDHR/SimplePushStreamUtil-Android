package com.dhr.simplepushstreamutil.dialog.configlinuxpush;

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
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;

public class ConfigLinuxPushDialog extends Dialog {
    private SharedPreferencesUtil sharedPreferencesUtil;
    private Gson gson = new Gson();
    private Context context;

    private RadioGroup rgConfig;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;

    private Button btnOk;
    private Button btnCancel;

    public ConfigLinuxPushDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context = context;
        sharedPreferencesUtil = new SharedPreferencesUtil(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_config_linux_push);

        initView();
    }

    private void initView() {
        rgConfig = findViewById(R.id.rgConfig);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);

        String configLinuxPush = (String) sharedPreferencesUtil.getSharedPreference("ConfigLinuxPush", "");
        if (configLinuxPush.isEmpty()) {
            ConfigLinuxPushBean configLinuxPushBean = new ConfigLinuxPushBean();
            configLinuxPushBean.setStatus(0);
            sharedPreferencesUtil.put("ConfigLinuxPush", gson.toJson(configLinuxPushBean));
        }
        configLinuxPush = (String) sharedPreferencesUtil.getSharedPreference("ConfigLinuxPush", "");
        ConfigLinuxPushBean configLinuxPushBean = gson.fromJson(configLinuxPush, ConfigLinuxPushBean.class);
        switch (configLinuxPushBean.getStatus()) {
            case 0:
                rb1.setChecked(true);
                break;
            case 1:
                rb2.setChecked(true);
                break;
            case 2:
                rb3.setChecked(true);
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
                    ConfigLinuxPushBean configLinuxPushBean = new ConfigLinuxPushBean();
                    switch (rgConfig.getCheckedRadioButtonId()) {
                        case R.id.rb1:
                            configLinuxPushBean.setStatus(0);
                            break;
                        case R.id.rb2:
                            configLinuxPushBean.setStatus(1);
                            break;
                        case R.id.rb3:
                            configLinuxPushBean.setStatus(2);
                            break;
                    }
                    sharedPreferencesUtil.put("ConfigLinuxPush", gson.toJson(configLinuxPushBean));
                    dismiss();
                    break;
            }
        }
    };
}
