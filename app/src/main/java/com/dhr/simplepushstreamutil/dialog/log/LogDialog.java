package com.dhr.simplepushstreamutil.dialog.log;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.badoo.mobile.util.WeakHandler;
import com.dhr.simplepushstreamutil.R;

public class LogDialog extends Dialog {

    private Button btnOk;
    private TextView tvLog;

    private StringBuilder cacheLog;

    private ScrollView svLog;
    private WeakHandler weakHandler = new WeakHandler();

    public LogDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        cacheLog = new StringBuilder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_log);

        initView();
    }

    private void initView() {
        btnOk = findViewById(R.id.btnOk);
        tvLog = findViewById(R.id.tvLog);
        svLog = findViewById(R.id.svLog);

        btnOk.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnOk:
                    dismiss();
                    break;
            }
        }
    };

    public void addLog(String log) {
        if (cacheLog.length() > 8000) {
            clearAndAddLog(log);
        } else {
            cacheLog.append(log);
            if (isShowing()) {
                setLog();
            }
        }
    }

    public void clearAndAddLog(String log) {
        cacheLog.delete(0, cacheLog.length());
        cacheLog.append(log);
        if (isShowing()) {
            setLog();
        }
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        getWindow().getDecorView().setPadding(0, 0, 0, 0);

        getWindow().setAttributes(layoutParams);
        setLog();
    }

    private void setLog() {
        if (cacheLog.length() > 0) {
            tvLog.setText(cacheLog.toString());
            weakHandler.post(new Runnable() {
                @Override
                public void run() {
                    svLog.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }
}
