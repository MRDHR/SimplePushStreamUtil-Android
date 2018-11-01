package com.dhr.simplepushstreamutil.dialog.bilibiliacount;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.badoo.mobile.util.WeakHandler;
import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.params.MainParam;
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;
import com.hiczp.bilibili.api.BilibiliAPI;
import com.hiczp.bilibili.api.BilibiliAccount;
import com.hiczp.bilibili.api.passport.entity.LoginResponseEntity;
import com.hiczp.bilibili.api.passport.exception.CaptchaMismatchException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.login.LoginException;

public class BilibiliAcountDialog extends Dialog {
    private Context context;
    private EditText etUserName;
    private EditText etPassWord;
    private Button btnTestLogin;
    private Button btnSave;
    private Button btnRemove;
    private Button btnCancel;

    private String userName;
    private String password;
    private BilibiliAccount bilibiliAccount;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private Gson gson = new Gson();
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private WeakHandler weakHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MainParam.testLoginSuccess:
                    Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
                    break;
                case MainParam.userNameOrPassWordError:
                    Toast.makeText(context, "用户名密码错误，请重新输入后再试", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    public BilibiliAcountDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bilibili_acount);

        initView();
    }

    private void initView() {
        etUserName = findViewById(R.id.etUserName);
        etPassWord = findViewById(R.id.etPassWord);
        btnTestLogin = findViewById(R.id.btnTestLogin);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);
        btnCancel = findViewById(R.id.btnCancel);

        btnTestLogin.setOnClickListener(onClickListener);
        btnSave.setOnClickListener(onClickListener);
        btnRemove.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        getWindow().getDecorView().setPadding(0, 0, 0, 0);

        getWindow().setAttributes(layoutParams);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnTestLogin:
                    userName = etUserName.getText().toString();
                    password = etPassWord.getText().toString();
                    if (userName.isEmpty()) {
                        Toast.makeText(context, "账号不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
                    } else if (password.isEmpty()) {
                        Toast.makeText(context, "密码不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
                    } else {
                        testLogin();
                    }
                    break;
                case R.id.btnSave:
                    if (null == bilibiliAccount) {
                        Toast.makeText(context, "尚未进行登录，请登录后重试", Toast.LENGTH_SHORT).show();
                    } else {
                        if (null == sharedPreferencesUtil) {
                            sharedPreferencesUtil = new SharedPreferencesUtil(context);
                        }
                        if (null == gson) {
                            gson = new Gson();
                        }
                        sharedPreferencesUtil.put("BilibiliAccount", gson.toJson(bilibiliAccount));
                        Toast.makeText(context, "登录信息保存成功", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnRemove:
                    if (null == sharedPreferencesUtil) {
                        sharedPreferencesUtil = new SharedPreferencesUtil(context);
                    }
                    if (null == gson) {
                        gson = new Gson();
                    }
                    BilibiliAccount bilibiliAccount = new BilibiliAccount("", "", 0L, 0L, 0L);
                    sharedPreferencesUtil.put("BilibiliAccount", gson.toJson(bilibiliAccount));
                    Toast.makeText(context, "登录信息删除成功", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btnCancel:
                    dismiss();
                    break;
            }
        }
    };

    private void testLogin() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                BilibiliAPI bilibiliAPI = new BilibiliAPI();
                try {
                    LoginResponseEntity loginResponseEntity = bilibiliAPI.login(userName, password);
                    int code = loginResponseEntity.getCode();
                    if (0 == code) {
                        bilibiliAccount = loginResponseEntity.toBilibiliAccount();
                        weakHandler.sendEmptyMessage(MainParam.testLoginSuccess);
                    }
                } catch (IOException | CaptchaMismatchException e) {
                    e.printStackTrace();
                } catch (LoginException e) {
                    e.printStackTrace();
                    weakHandler.sendEmptyMessage(MainParam.userNameOrPassWordError);
                }
            }
        });
    }

}
