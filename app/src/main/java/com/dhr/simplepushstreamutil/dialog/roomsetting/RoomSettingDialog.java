package com.dhr.simplepushstreamutil.dialog.roomsetting;

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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.adapter.MyExpandableListAdapter;
import com.hiczp.bilibili.api.web.live.entity.LiveAreaListEntity;

import java.util.ArrayList;
import java.util.List;

public class RoomSettingDialog extends Dialog {
    private Context context;
    private CallBack callBack;

    private EditText etRoomName;
    private ExpandableListView expListArea;
    private Button btnOk;
    private Button btnCancel;

    //定义父列表项List数据集合
    private List<LiveAreaListEntity.DataBean> group = new ArrayList<>();
    //定义子列表项List数据集合
    private MyExpandableListAdapter myExpandableListAdapter;

    public RoomSettingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_room_setting);

        initView();
    }

    private void initView() {
        etRoomName = findViewById(R.id.etRoomName);
        expListArea = findViewById(R.id.expListArea);
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
    }

    public void updateData(List<LiveAreaListEntity.DataBean> data) {
        group = data;
        if (!group.isEmpty()) {
            List<LiveAreaListEntity.DataBean.ListBean> listBeans = group.get(0).getList();
            if (!listBeans.isEmpty()) {
                listBeans.get(0).setChecked(true);
            }
        }
        myExpandableListAdapter = new MyExpandableListAdapter(context, group);
        expListArea.setAdapter(myExpandableListAdapter);
        expListArea.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                for (LiveAreaListEntity.DataBean.ListBean listBean : group.get(groupPosition).getList()) {
                    listBean.setChecked(false);
                }
                group.get(groupPosition).getList().get(childPosition).setChecked(true);
                myExpandableListAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnOk:
                    String roomName = etRoomName.getText().toString();
                    if (roomName.isEmpty()) {
                        Toast.makeText(context, "房间标题为空，请输入房间标题后重试", Toast.LENGTH_SHORT).show();
                    } else {
                        for (LiveAreaListEntity.DataBean dataBean : group) {
                            for (LiveAreaListEntity.DataBean.ListBean listBean : dataBean.getList()) {
                                if (listBean.isChecked()) {
                                    callBack.callBack(roomName, listBean.getId());
                                    dismiss();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                case R.id.btnCancel:
                    dismiss();
                    break;
            }
        }
    };

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

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void callBack(String roomName, String areaId);
    }
}
