package com.dhr.simplepushstreamutil.dialog.resourceurl;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.bean.ResourceUrlInfoBean;
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ResourceUrlInfoDialog extends Dialog {
    private CallBack callBack;
    private Context context;

    private Button btnOk;
    private Button btnCancel;
    private Button btnRemove;

    private EditText etName;
    private SharedPreferencesUtil sharedPreferencesUtil;
    private Gson gson = new Gson();
    private List<ResourceUrlInfoBean> resourceUrlInfoBeans;

    private ListView listContent;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> list;

    public ResourceUrlInfoDialog(@NonNull Context context, @StyleRes int themeResId, CallBack callBack) {
        super(context, themeResId);
        this.context = context;
        this.callBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_serverinfo_load);

        initView();
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                loadDataFromJson();
            }
        });
    }

    private void initView() {
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        btnRemove = findViewById(R.id.btnRemove);
        etName = findViewById(R.id.etName);

        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
        btnRemove.setOnClickListener(onClickListener);

        listContent = findViewById(R.id.listContent);
    }

    private void loadDataFromJson() {
        if (null == sharedPreferencesUtil) {
            sharedPreferencesUtil = new SharedPreferencesUtil(context);
        }
        String serverInfo = (String) sharedPreferencesUtil.getSharedPreference("ResourceUrlInfo", "");
        if (!TextUtils.isEmpty(serverInfo)) {
            resourceUrlInfoBeans = gson.fromJson(serverInfo, new TypeToken<ArrayList<ResourceUrlInfoBean>>() {
            }.getType());
            if (null == list) {
                list = new ArrayList<>();
            }
            list.clear();
            for (ResourceUrlInfoBean bean : resourceUrlInfoBeans) {
                list.add(bean.getSaveName());
            }
            if (null == arrayAdapter) {
                arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice, list);
                listContent.setAdapter(arrayAdapter);
            } else {
                arrayAdapter.notifyDataSetChanged();
            }
            if (null != resourceUrlInfoBeans && !resourceUrlInfoBeans.isEmpty()) {
                listContent.setItemChecked(0, true);
            }
        } else {
            resourceUrlInfoBeans = new ArrayList<>();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnCancel:
                    dismiss();
                    break;
                case R.id.btnOk:
                    int checkedItemPosition = listContent.getCheckedItemPosition();
                    if (checkedItemPosition >= 0) {
                        ResourceUrlInfoBean resourceUrlInfoBean = resourceUrlInfoBeans.get(checkedItemPosition);
                        callBack.confirm(resourceUrlInfoBean.getUrl());
                        dismiss();
                    } else {
                        Toast.makeText(context.getApplicationContext(), "请选择需要提取的记录", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnRemove:
                    checkedItemPosition = listContent.getCheckedItemPosition();
                    if (checkedItemPosition >= 0) {
                        if (!resourceUrlInfoBeans.isEmpty()) {
                            resourceUrlInfoBeans.remove(checkedItemPosition);
                            sharedPreferencesUtil.put("ResourceUrlInfo", gson.toJson(resourceUrlInfoBeans));
                            loadDataFromJson();
                        }
                    } else {
                        Toast.makeText(context.getApplicationContext(), "请选择需要删除的记录", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public interface CallBack {
        void confirm(String url);
    }
}
