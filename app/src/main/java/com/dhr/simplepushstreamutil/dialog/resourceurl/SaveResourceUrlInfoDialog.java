package com.dhr.simplepushstreamutil.dialog.resourceurl;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dhr.simplepushstreamutil.R;

public class SaveResourceUrlInfoDialog extends Dialog {
    private CallBack callBack;
    private Context context;

    private Button btnOk;
    private Button btnCancel;

    private EditText etName;

    public SaveResourceUrlInfoDialog(@NonNull Context context, @StyleRes int themeResId, CallBack callBack) {
        super(context, themeResId);
        this.context = context;
        this.callBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_resourceurlinfo_save);

        initView();
    }

    private void initView() {
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        etName = findViewById(R.id.etName);

        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnCancel:
                    dismiss();
                    break;
                case R.id.btnOk:
                    String name = etName.getText().toString();
                    if (!TextUtils.isEmpty(name)) {
                        callBack.confirm(name);
                        dismiss();
                    } else {
                        Toast.makeText(context.getApplicationContext(), "名称不能为空，请输入后再试", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public interface CallBack {
        void confirm(String name);
    }
}
