package com.dhr.simplepushstreamutil.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.badoo.mobile.util.WeakHandler;
import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.bean.ConfigLinuxPushBean;
import com.dhr.simplepushstreamutil.bean.ConfigSchemeBean;
import com.dhr.simplepushstreamutil.bean.ResolutionBean;
import com.dhr.simplepushstreamutil.bean.ResourceUrlInfoBean;
import com.dhr.simplepushstreamutil.bean.ServerInfoBean;
import com.dhr.simplepushstreamutil.bean.TargetUrlInfoBean;
import com.dhr.simplepushstreamutil.dialog.bilibiliacount.BilibiliAcountDialog;
import com.dhr.simplepushstreamutil.dialog.configlinuxpush.ConfigLinuxPushDialog;
import com.dhr.simplepushstreamutil.dialog.configscheme.ConfigSchemeDialog;
import com.dhr.simplepushstreamutil.dialog.log.LogDialog;
import com.dhr.simplepushstreamutil.dialog.resourceurl.ResourceUrlInfoDialog;
import com.dhr.simplepushstreamutil.dialog.resourceurl.SaveResourceUrlInfoDialog;
import com.dhr.simplepushstreamutil.dialog.roomsetting.RoomSettingDialog;
import com.dhr.simplepushstreamutil.dialog.serverinfo.SaveServerInfoDialog;
import com.dhr.simplepushstreamutil.dialog.serverinfo.ServerInfoDialog;
import com.dhr.simplepushstreamutil.dialog.targeturl.SaveTargetUrlInfoDialog;
import com.dhr.simplepushstreamutil.dialog.targeturl.TargetUrlInfoDialog;
import com.dhr.simplepushstreamutil.params.MainParam;
import com.dhr.simplepushstreamutil.runnable.GetFormatListRunnable;
import com.dhr.simplepushstreamutil.runnable.GetM3u8UrlRunnable;
import com.dhr.simplepushstreamutil.runnable.PushStreamRunnable;
import com.dhr.simplepushstreamutil.util.JschUtil;
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hiczp.bilibili.api.BilibiliAPI;
import com.hiczp.bilibili.api.BilibiliAccount;
import com.hiczp.bilibili.api.web.BilibiliWebAPI;
import com.hiczp.bilibili.api.web.live.LiveService;
import com.hiczp.bilibili.api.web.live.entity.LiveAreaListEntity;
import com.hiczp.bilibili.api.web.live.entity.LiveInfoEntity;
import com.hiczp.bilibili.api.web.live.entity.StartLiveEntity;
import com.hiczp.bilibili.api.web.live.entity.StopLiveEntity;
import com.hiczp.bilibili.api.web.live.entity.UpdateRoomTitleEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cookie;

public class MainActivity extends AppCompatActivity {
    private Toolbar mNormalToolbar;

    private EditText etServerIp;
    private EditText etServerPort;
    private EditText etUserName;
    private EditText etPassWord;
    private Button btnSaveServerInfo;
    private Button btnLoadServerInfo;
    private Button btnTest;

    private String serverIp;
    private int serverPort;
    private String userName;
    private String userPassword;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private JschUtil jschUtil = new JschUtil();

    private SharedPreferencesUtil sharedPreferencesUtil;
    private SaveServerInfoDialog saveServerInfoDialog;
    private ServerInfoDialog serverInfoDialog;
    private ConfigSchemeDialog configSchemeDialog;

    private Gson gson;

    private EditText etResourceUrl;
    private Button btnSaveResourceUrl;
    private Button btnLoadResourceUrlInfo;
    private SaveResourceUrlInfoDialog saveResourceUrlInfoDialog;
    private ResourceUrlInfoDialog resourceUrlInfoDialog;

    private EditText etTargetUrl;
    private Button btnSaveTargetUrl;
    private Button btnLoadTargetUrl;
    private SaveTargetUrlInfoDialog saveTargetUrlInfoDialog;
    private TargetUrlInfoDialog targetUrlInfoDialog;

    private String resourceUrl;
    private String m3u8Url;
    private String liveRoomUrl;
    private StartLiveEntity.DataBean.RtmpBean rtmp;

    private Button btnOpenLiveRoom;
    private Button btnCloseLiveRoom;
    private Button btnToMyLiveRoom;
    private Button btnGetFormatList;
    private Button btnStartPushStream;
    private Button btnStopPushStream;

    private boolean isLocalFile = false;
    private boolean needBackGround = false;

    private Spinner spFormatList;
    private ArrayAdapter<String> formatListAdapter;
    private List<String> listFormat;
    private List<ResolutionBean> listResolutions;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private LogDialog logDialog;

    private BilibiliAcountDialog bilibiliAcountDialog;
    private RadioGroup rgInputOrGet;
    private RadioButton rbInput;
    private RadioButton rbGet;
    private LinearLayout llTargetUrl;
    private LinearLayout llTargetUrlCtrl;

    private BilibiliAPI bilibiliAPI;
    private BilibiliWebAPI bilibiliWebAPI;
    private Map<String, List<Cookie>> cookiesMap;
    private RoomSettingDialog roomSettingDialog;
    private LiveService liveService;
    private String roomId;
    private String csrfToken = "";
    private BilibiliAccount bilibiliAccount;
    private LiveAreaListEntity liveAreaListEntity;

    private ConfigLinuxPushDialog configLinuxPushDialog;

    private boolean hasProxy;
    private RadioButton rbBoth;
    private RadioButton rbOnlyAudio;
    private RadioButton rbOnlyImage;

    private WeakHandler weakHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (!isFinishing()) {
                switch (msg.what) {
                    case MainParam.connectServiceSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "连接服务器成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("\n连接服务器成功");
                        break;
                    case MainParam.connectServiceFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("\n连接服务器失败，请检查输入的信息");
                        break;
                    case MainParam.startGetFormatList:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始获取分辨率列表", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("开始获取分辨率列表，请稍候...");
                        break;
                    case MainParam.getFormatListSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取分辨率列表成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n" + "获取分辨率列表成功，请选择推送分辨率，检查直播间地址是否有误，检查无误后点击开始推流按钮");
                        formatListAdapter.notifyDataSetChanged();
                        break;
                    case MainParam.getFormatListFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取分辨率列表失败", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog(msg.obj.toString());
                        formatListAdapter.notifyDataSetChanged();
                        break;
                    case MainParam.startGetM3u8Url:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始获取直播源，请稍候...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开始获取直播源，请稍候...");
                        break;
                    case MainParam.getM3u8UrlSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取直播源成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n获取直播源成功");
                        //直推m3u8或本地视频文件
                        showBackGroundDialog();
                        break;
                    case MainParam.getM3u8UrlFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取直播源失败", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog(msg.obj.toString());
                        break;
                    case MainParam.startAssemblePushStreamCommand:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始组装推流参数即将开始推流，请稍候...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开始组装推流参数即将开始推流，请稍候...");
                        break;
                    case MainParam.pushStreamLog:
                        String result = msg.obj.toString();
                        logDialog.addLog("\n\n" + result);
                        break;
                    case MainParam.stopPushStreamSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "结束推流成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n结束推流成功");
                        break;
                    case MainParam.startGetAreaList:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始获取分区列表信息，请稍候...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开始获取分区列表信息，请稍候...");
                        break;
                    case MainParam.getAreaListSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取分区列表信息成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n获取分区列表信息成功");
                        configRoom();
                        break;
                    case MainParam.startGetOpenLiveParam:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始获取开启直播相关参数，请稍候...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开始获取开启直播相关参数，请稍候...");
                        break;
                    case MainParam.startChangeLiveRoomTitle:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取开启直播参数成功，开始修改房间标题...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n获取开启直播参数成功，开始修改房间标题...");
                        break;
                    case MainParam.startOpenLiveRoom:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "修改房间标题成功，正在开启直播...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n修改房间标题成功，正在开启直播...");
                        break;
                    case MainParam.openLiveRoomSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开启直播成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开启直播成功");
                        btnCloseLiveRoom.setEnabled(true);
                        btnToMyLiveRoom.setEnabled(true);
                        if (hasProxy) {
                            rbOnlyAudio.setEnabled(true);
                            rbOnlyImage.setEnabled(true);
                        } else {
                            rbBoth.setChecked(true);
                            rbOnlyAudio.setEnabled(false);
                            rbOnlyImage.setEnabled(false);
                        }
                        break;
                    case MainParam.openLiveRoomFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开启直播失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开启直播失败，请稍后再试");
                        break;
                    case MainParam.changeLiveRoomTitleFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "更新房间标题失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n更新房间标题失败，请稍后再试");
                        break;
                    case MainParam.getAreaListFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取分区列表失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n获取分区列表失败，请稍后再试");
                        break;
                    case MainParam.loginInfoEmpty:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "登录信息为空，请在菜单栏中登录B站账号信息后重试", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n登录信息为空，请在菜单栏中登录B站账号信息后重试");
                        break;
                    case MainParam.closeLiveRoomSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "关闭直播间成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n关闭直播间成功");
                        btnCloseLiveRoom.setEnabled(false);
                        btnToMyLiveRoom.setEnabled(false);
                        break;
                    case MainParam.closeLiveRoomFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "关闭直播间失败，请自行去B站停止直播", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n关闭直播间失败，请自行去B站停止直播");
                        break;
                }
            }
            return false;
        }
    });

    /**
     * 配置直播间信息
     */
    private void configRoom() {
        if (null == roomSettingDialog) {
            roomSettingDialog = new RoomSettingDialog(MainActivity.this, R.style.dialog_custom);
            roomSettingDialog.setCallBack(new RoomSettingDialog.CallBack() {
                @Override
                public void callBack(String roomName, String areaId) {
                    openLiveRoom(roomName, areaId);
                }
            });
        }
        roomSettingDialog.show();
        roomSettingDialog.updateData(liveAreaListEntity.getData());
    }

    /**
     * 开启直播间
     *
     * @param roomName
     * @param areaId
     */
    private void openLiveRoom(String roomName, String areaId) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    weakHandler.sendEmptyMessage(MainParam.startGetOpenLiveParam);
                    LiveInfoEntity liveInfoEntity = liveService.getLiveInfo().execute().body();
                    if (null != liveInfoEntity && 0 == liveInfoEntity.getCode()) {
                        roomId = liveInfoEntity.getData().getRoomid();
                        List<Cookie> cookies = cookiesMap.get("bilibili.com");
                        for (Cookie cookie : cookies) {
                            if ("bili_jct".equals(cookie.name())) {
                                csrfToken = cookie.value();
                                break;
                            }
                        }
                        weakHandler.sendEmptyMessage(MainParam.startChangeLiveRoomTitle);
                        UpdateRoomTitleEntity updateRoomTitleEntity = liveService.updateRoomTitle(roomId, roomName, csrfToken).execute().body();
                        if (null != updateRoomTitleEntity && 0 == updateRoomTitleEntity.getCode()) {
                            weakHandler.sendEmptyMessage(MainParam.startOpenLiveRoom);
                            StartLiveEntity startLiveEntity = liveService.startLive(roomId, "pc", areaId, csrfToken).execute().body();
                            if (null != startLiveEntity && 0 == startLiveEntity.getCode()) {
                                StartLiveEntity.DataBean.RtmpBean rtmp = startLiveEntity.getData().getRtmp();
                                MainActivity.this.rtmp = rtmp;
                                weakHandler.sendEmptyMessage(MainParam.openLiveRoomSuccess);
                            } else {
                                weakHandler.sendEmptyMessage(MainParam.openLiveRoomFail);
                            }
                        } else {
                            weakHandler.sendEmptyMessage(MainParam.changeLiveRoomTitleFail);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        mNormalToolbar = findViewById(R.id.mNormalToolbar);
        //设置menu
        mNormalToolbar.inflateMenu(R.menu.menu);
        //设置menu的点击事件
        mNormalToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                if (menuItemId == R.id.action_log) {
                    logDialog.show();
                } else if (menuItemId == R.id.action_config) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (null == bilibiliAcountDialog) {
                            bilibiliAcountDialog = new BilibiliAcountDialog(MainActivity.this, R.style.dialog_custom);
                        }
                        bilibiliAcountDialog.show();
                    } else {
                        //在版本低于此的时候，做一些处理
                        Toast.makeText(MainActivity.this, "该功能仅支持Android7.0以上手机", Toast.LENGTH_SHORT).show();
                    }
                } else if (menuItemId == R.id.action_config_linux_push) {
                    if (null == configLinuxPushDialog) {
                        configLinuxPushDialog = new ConfigLinuxPushDialog(MainActivity.this, R.style.dialog_custom);
                    }
                    configLinuxPushDialog.show();
                } else if (menuItemId == R.id.action_config_scheme) {
                    if (null == configSchemeDialog) {
                        configSchemeDialog = new ConfigSchemeDialog(MainActivity.this, R.style.dialog_custom);
                    }
                    configSchemeDialog.show();
                }
                return true;
            }
        });

        etServerIp = findViewById(R.id.etServerIp);
        etServerPort = findViewById(R.id.etServerPort);
        etUserName = findViewById(R.id.etUserName);
        etPassWord = findViewById(R.id.etPassWord);
        btnSaveServerInfo = findViewById(R.id.btnSaveServerInfo);
        btnLoadServerInfo = findViewById(R.id.btnLoadServerInfo);
        btnTest = findViewById(R.id.btnTest);

        btnSaveServerInfo.setOnClickListener(onClickListener);
        btnLoadServerInfo.setOnClickListener(onClickListener);
        btnTest.setOnClickListener(onClickListener);

        etResourceUrl = findViewById(R.id.etResourceUrl);
        btnSaveResourceUrl = findViewById(R.id.btnSaveResourceUrl);
        btnLoadResourceUrlInfo = findViewById(R.id.btnLoadResourceUrlInfo);
        btnSaveResourceUrl.setOnClickListener(onClickListener);
        btnLoadResourceUrlInfo.setOnClickListener(onClickListener);

        etTargetUrl = findViewById(R.id.etTargetUrl);
        btnSaveTargetUrl = findViewById(R.id.btnSaveTargetUrl);
        btnLoadTargetUrl = findViewById(R.id.btnLoadTargetUrl);
        btnSaveTargetUrl.setOnClickListener(onClickListener);
        btnLoadTargetUrl.setOnClickListener(onClickListener);

        btnOpenLiveRoom = findViewById(R.id.btnOpenLiveRoom);
        btnCloseLiveRoom = findViewById(R.id.btnCloseLiveRoom);
        btnToMyLiveRoom = findViewById(R.id.btnToMyLiveRoom);
        btnOpenLiveRoom.setOnClickListener(onClickListener);
        btnCloseLiveRoom.setOnClickListener(onClickListener);
        btnToMyLiveRoom.setOnClickListener(onClickListener);

        btnGetFormatList = findViewById(R.id.btnGetFormatList);
        btnStartPushStream = findViewById(R.id.btnStartPushStream);
        btnStopPushStream = findViewById(R.id.btnStopPushStream);
        btnGetFormatList.setOnClickListener(onClickListener);
        btnStartPushStream.setOnClickListener(onClickListener);
        btnStopPushStream.setOnClickListener(onClickListener);

        spFormatList = findViewById(R.id.spFormatList);
        listFormat = new ArrayList<>();
        listResolutions = new ArrayList<>();
        formatListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listFormat);
        formatListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormatList.setAdapter(formatListAdapter);

        logDialog = new LogDialog(this, R.style.dialog_custom);

        rgInputOrGet = findViewById(R.id.rgInputOrGet);
        rbInput = findViewById(R.id.rbInput);
        rbGet = findViewById(R.id.rbGet);
        llTargetUrl = findViewById(R.id.llTargetUrl);
        llTargetUrlCtrl = findViewById(R.id.llTargetUrlCtrl);
        rgInputOrGet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbInput:
                        llTargetUrl.setVisibility(View.VISIBLE);
                        llTargetUrlCtrl.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rbGet:
                        llTargetUrl.setVisibility(View.GONE);
                        llTargetUrlCtrl.setVisibility(View.GONE);
                        break;
                }
            }
        });
        rbInput.setChecked(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rgInputOrGet.setVisibility(View.VISIBLE);
        } else {
            rgInputOrGet.setVisibility(View.GONE);
        }

        rbBoth = findViewById(R.id.rbBoth);
        rbOnlyAudio = findViewById(R.id.rbOnlyAudio);
        rbOnlyImage = findViewById(R.id.rbOnlyImage);
        rbBoth.setChecked(true);
        rbOnlyAudio.setEnabled(false);
        rbOnlyImage.setEnabled(false);

        btnCloseLiveRoom.setEnabled(false);
        btnToMyLiveRoom.setEnabled(false);
    }

    private void initData() {
        sharedPreferencesUtil = new SharedPreferencesUtil(this);
        gson = new Gson();
        String configScheme = sharedPreferencesUtil.getSharedPreference("ConfigScheme", "").toString();
        if (TextUtils.isEmpty(configScheme)) {
            ConfigSchemeBean configSchemeBean = new ConfigSchemeBean();
            configSchemeBean.setSchemeType(0);
            sharedPreferencesUtil.put("ConfigScheme", gson.toJson(configSchemeBean));
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnSaveServerInfo:
                    boolean b = loadServerInfo();
                    if (!b) {
                        if (null == saveServerInfoDialog) {
                            saveServerInfoDialog = new SaveServerInfoDialog(MainActivity.this, R.style.dialog_custom, new SaveServerInfoDialog.CallBack() {
                                @Override
                                public void confirm(String name) {
                                    String serverInfo = (String) sharedPreferencesUtil.getSharedPreference("ServerInfo", "");
                                    List<ServerInfoBean> serverInfoBeans;
                                    if (!TextUtils.isEmpty(serverInfo)) {
                                        serverInfoBeans = gson.fromJson(serverInfo, new TypeToken<ArrayList<ServerInfoBean>>() {
                                        }.getType());
                                    } else {
                                        serverInfoBeans = new ArrayList<>();
                                    }
                                    ServerInfoBean serverInfoBean = new ServerInfoBean();
                                    serverInfoBean.setSaveName(name);
                                    serverInfoBean.setIp(serverIp);
                                    serverInfoBean.setPort(serverPort);
                                    serverInfoBean.setUserName(userName);
                                    serverInfoBean.setUserPassword(userPassword);
                                    serverInfoBeans.add(serverInfoBean);
                                    sharedPreferencesUtil.put("ServerInfo", gson.toJson(serverInfoBeans));
                                    Toast.makeText(getApplicationContext(), "服务器信息保存记录成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        saveServerInfoDialog.show();
                    }
                    break;
                case R.id.btnLoadServerInfo:
                    if (null == serverInfoDialog) {
                        serverInfoDialog = new ServerInfoDialog(MainActivity.this, R.style.dialog_custom, new ServerInfoDialog.CallBack() {
                            @Override
                            public void confirm(String ip, int port, String userName, String userPassword) {
                                etServerIp.setText(ip);
                                etServerPort.setText(String.valueOf(port));
                                etUserName.setText(userName);
                                etPassWord.setText(userPassword);
                            }
                        });
                    }
                    serverInfoDialog.show();
                    break;
                case R.id.btnTest:
                    b = loadServerInfo();
                    if (!b) {
                        Toast.makeText(getApplicationContext(), "开始测试连接服务器", Toast.LENGTH_SHORT).show();
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    jschUtil.versouSshUtil(serverIp, userName, userPassword, serverPort);
                                    jschUtil.runCmd("ls", "UTF-8");
                                    weakHandler.sendEmptyMessage(MainParam.connectServiceSuccess);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    weakHandler.sendEmptyMessage(MainParam.connectServiceFail);
                                }
                            }
                        });
                    }
                    break;
                case R.id.btnSaveResourceUrl:
                    resourceUrl = etResourceUrl.getText().toString();
                    if (TextUtils.isEmpty(resourceUrl)) {
                        Toast.makeText(getApplicationContext(), "直播源地址不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (null == saveResourceUrlInfoDialog) {
                            saveResourceUrlInfoDialog = new SaveResourceUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new SaveResourceUrlInfoDialog.CallBack() {
                                @Override
                                public void confirm(String name) {
                                    String resourceUrlInfo = (String) sharedPreferencesUtil.getSharedPreference("ResourceUrlInfo", "");
                                    List<ResourceUrlInfoBean> resourceUrlInfoBeans;
                                    if (!TextUtils.isEmpty(resourceUrlInfo)) {
                                        resourceUrlInfoBeans = gson.fromJson(resourceUrlInfo, new TypeToken<ArrayList<ResourceUrlInfoBean>>() {
                                        }.getType());
                                    } else {
                                        resourceUrlInfoBeans = new ArrayList<>();
                                    }
                                    ResourceUrlInfoBean resourceUrlInfoBean = new ResourceUrlInfoBean();
                                    resourceUrlInfoBean.setSaveName(name);
                                    resourceUrlInfoBean.setUrl(resourceUrl);
                                    resourceUrlInfoBeans.add(resourceUrlInfoBean);
                                    sharedPreferencesUtil.put("ResourceUrlInfo", gson.toJson(resourceUrlInfoBeans));
                                    Toast.makeText(getApplicationContext(), "直播源信息保存记录成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        saveResourceUrlInfoDialog.show();
                    }
                    break;
                case R.id.btnLoadResourceUrlInfo:
                    if (null == resourceUrlInfoDialog) {
                        resourceUrlInfoDialog = new ResourceUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new ResourceUrlInfoDialog.CallBack() {
                            @Override
                            public void confirm(String url) {
                                etResourceUrl.setText(url);
                            }
                        });
                    }
                    resourceUrlInfoDialog.show();
                    break;
                case R.id.btnSaveTargetUrl:
                    liveRoomUrl = etTargetUrl.getText().toString();
                    if (TextUtils.isEmpty(liveRoomUrl)) {
                        Toast.makeText(getApplicationContext(), "直播间地址不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (null == saveTargetUrlInfoDialog) {
                            saveTargetUrlInfoDialog = new SaveTargetUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new SaveTargetUrlInfoDialog.CallBack() {
                                @Override
                                public void confirm(String name) {
                                    String targetUrlInfo = (String) sharedPreferencesUtil.getSharedPreference("TargetUrlInfo", "");
                                    List<TargetUrlInfoBean> targetUrlInfoBeans;
                                    if (!TextUtils.isEmpty(targetUrlInfo)) {
                                        targetUrlInfoBeans = gson.fromJson(targetUrlInfo, new TypeToken<ArrayList<TargetUrlInfoBean>>() {
                                        }.getType());
                                    } else {
                                        targetUrlInfoBeans = new ArrayList<>();
                                    }
                                    TargetUrlInfoBean targetUrlInfoBean = new TargetUrlInfoBean();
                                    targetUrlInfoBean.setSaveName(name);
                                    targetUrlInfoBean.setUrl(liveRoomUrl);
                                    targetUrlInfoBeans.add(targetUrlInfoBean);
                                    sharedPreferencesUtil.put("TargetUrlInfo", gson.toJson(targetUrlInfoBeans));
                                    Toast.makeText(getApplicationContext(), "直播间信息保存记录成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        saveTargetUrlInfoDialog.show();
                    }
                    break;
                case R.id.btnLoadTargetUrl:
                    if (null == targetUrlInfoDialog) {
                        targetUrlInfoDialog = new TargetUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new TargetUrlInfoDialog.CallBack() {
                            @Override
                            public void confirm(String url) {
                                etTargetUrl.setText(url);
                            }
                        });
                    }
                    targetUrlInfoDialog.show();
                    break;
                case R.id.btnGetFormatList:
                    b = loadServerInfo();
                    if (!b) {
                        resourceUrl = etResourceUrl.getText().toString();
                        if (TextUtils.isEmpty(resourceUrl)) {
                            Toast.makeText(MainActivity.this, "请输入直播源地址后重试", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = "";
                            String configScheme = sharedPreferencesUtil.getSharedPreference("ConfigScheme", "").toString();
                            ConfigSchemeBean configSchemeBean = gson.fromJson(configScheme, ConfigSchemeBean.class);
                            switch (configSchemeBean.getSchemeType()) {
                                case 0:
                                    message = "该地址是否需要使用youtube-dl进行解析？\n（如填入的为m3u8地址或本地视频文件地址，请选否）";
                                    break;
                                case 1:
                                    message = "该地址是否需要使用streamlink进行解析？\n（如填入的为m3u8地址或本地视频文件地址，请选否）";
                                    break;
                            }
                            showConfirmDialog(message, 3);
                        }
                    }
                    break;
                case R.id.btnStartPushStream:
                    if (rbGet.isChecked()) {
                        liveRoomUrl = rtmp.getAddr() + rtmp.getCode();
                        inputOrGetM3u8Url("请先打开直播间");
                    } else {
                        //手动填写直播间地址
                        liveRoomUrl = etTargetUrl.getText().toString();
                        inputOrGetM3u8Url("直播间地址为空，请输入后重试");
                    }
                    break;
                case R.id.btnStopPushStream:
                    stopPushStreamInLinux();
                    break;
                case R.id.btnOpenLiveRoom:
                    //通过B站账号信息获取推流地址
                    String bilibiliAccountStr = (String) sharedPreferencesUtil.getSharedPreference("BilibiliAccount", "");
                    bilibiliAccount = gson.fromJson(bilibiliAccountStr, BilibiliAccount.class);
                    if (null == bilibiliAccount || null == bilibiliAccount.getAccessToken() || bilibiliAccount.getAccessToken().isEmpty()) {
                        Toast.makeText(MainActivity.this, "登录信息为空，请在菜单栏中登录B站账号信息后重试", Toast.LENGTH_SHORT).show();
                    } else {
                        showConfirmDialog("您的手机目前是否开启了梯子？", 1);
                    }
                    break;
                case R.id.btnCloseLiveRoom:
                    if (rbGet.isChecked()) {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                String bilibiliAccountStr = (String) sharedPreferencesUtil.getSharedPreference("BilibiliAccount", "");
                                BilibiliAccount bilibiliAccount = gson.fromJson(bilibiliAccountStr, BilibiliAccount.class);
                                if (null == bilibiliAccount || null == bilibiliAccount.getAccessToken() || bilibiliAccount.getAccessToken().isEmpty()) {
                                    weakHandler.sendEmptyMessage(MainParam.loginInfoEmpty);
                                } else {
                                    try {
                                        StopLiveEntity stopLiveEntity = liveService.stopLive(roomId, "pc", csrfToken).execute().body();
                                        if (null != stopLiveEntity && 0 == stopLiveEntity.getCode()) {
                                            weakHandler.sendEmptyMessage(MainParam.closeLiveRoomSuccess);
                                        } else {
                                            weakHandler.sendEmptyMessage(MainParam.closeLiveRoomFail);
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                    break;
                case R.id.btnToMyLiveRoom:
                    if (null != roomId && !roomId.isEmpty()) {
                        String site = "https://live.bilibili.com/" + roomId;
                        Intent intent = new Intent();
                        intent.setData(Uri.parse(site));//Url 就是你要打开的网址
                        intent.setAction(Intent.ACTION_VIEW);
                        startActivity(intent); //启动浏览器
                    }
                    break;
            }
        }
    };

    /**
     * 显示确认对话框
     *
     * @param content
     * @param type
     */
    private void showConfirmDialog(String content, int type) {
        AlertDialog.Builder
                normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("温馨提示：");
        normalDialog.setCancelable(false);
        normalDialog.setMessage(content);
        normalDialog.setPositiveButton("是",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (type) {
                            case 1:
                                hasProxy = true;
                                getAreaList();
                                break;
                            case 2:
                                needBackGround = true;
                                pushStreamToLiveRoomInLinux();
                                break;
                            case 3:
                                listFormat.clear();
                                formatListAdapter.notifyDataSetChanged();
                                isLocalFile = false;
                                getFormatListInLinux();
                                break;
                        }
                    }
                });
        normalDialog.setNegativeButton("否",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (type) {
                            case 1:
                                hasProxy = false;
                                getAreaList();
                                break;
                            case 2:
                                needBackGround = false;
                                pushStreamToLiveRoomInLinux();
                                break;
                            case 3:
                                listFormat.clear();
                                formatListAdapter.notifyDataSetChanged();
                                isLocalFile = true;
                                m3u8Url = resourceUrl;
                                break;
                        }
                    }
                });
        normalDialog.show();
    }

    private void inputOrGetM3u8Url(String tips) {
        if (TextUtils.isEmpty(liveRoomUrl)) {
            Toast.makeText(MainActivity.this, tips, Toast.LENGTH_SHORT).show();
        } else {
            if (isLocalFile) {
                //直推m3u8或本地视频文件
                showBackGroundDialog();
            } else {
                //通过youtube-dl获取m3u8地址
                String configScheme = sharedPreferencesUtil.getSharedPreference("ConfigScheme", "").toString();
                ConfigSchemeBean configSchemeBean = gson.fromJson(configScheme, ConfigSchemeBean.class);
                if (0 == configSchemeBean.getSchemeType()) {
                    getM3u8UrlInLinux();
                } else if (1 == configSchemeBean.getSchemeType()) {
                    showBackGroundDialog();
                }
            }
        }
    }

    /**
     * 获取分类列表
     */
    private void getAreaList() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                weakHandler.sendEmptyMessage(MainParam.startGetAreaList);
                bilibiliAPI = new BilibiliAPI(bilibiliAccount);
                try {
                    cookiesMap = bilibiliAPI.toCookies();
                    bilibiliWebAPI = new BilibiliWebAPI(cookiesMap);
                    liveService = bilibiliWebAPI.getLiveService();
                    liveAreaListEntity = liveService.getLiveAreaList().execute().body();
                    if (null != liveAreaListEntity && 0 == liveAreaListEntity.getCode()) {
                        weakHandler.sendEmptyMessage(MainParam.getAreaListSuccess);
                    } else {
                        weakHandler.sendEmptyMessage(MainParam.getAreaListFail);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * linux服务器环境获取分辨率列表
     */
    private void getFormatListInLinux() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                weakHandler.sendEmptyMessage(MainParam.startGetFormatList);
                try {
                    jschUtil.versouSshUtil(serverIp, userName, userPassword, serverPort);
                    String cmd = "";
                    String configScheme = sharedPreferencesUtil.getSharedPreference("ConfigScheme", "").toString();
                    ConfigSchemeBean configSchemeBean = gson.fromJson(configScheme, ConfigSchemeBean.class);
                    switch (configSchemeBean.getSchemeType()) {
                        case 0:
                            cmd = "youtube-dl --list-formats " + resourceUrl;
                            break;
                        case 1:
                            cmd = "streamlink " + resourceUrl;
                            break;
                    }
                    jschUtil.runCmd(cmd, "UTF-8", getFormatListCallBack, configSchemeBean.getSchemeType());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private GetFormatListRunnable.GetFormatListCallBack getFormatListCallBack = new GetFormatListRunnable.GetFormatListCallBack() {
        @Override
        public void getFormatListSuccess(List<ResolutionBean> listResolutions) {
            MainActivity.this.listResolutions.clear();
            MainActivity.this.listResolutions.addAll(listResolutions);
            listFormat.clear();
            for (ResolutionBean resolution : listResolutions) {
                String result = "";
                if (null != resolution.getResolutionPx() && !resolution.getResolutionPx().isEmpty()) {
                    result += resolution.getResolutionPx();
                } else {
                    result += "无分辨率参数";
                }
                if (null != resolution.getFps() && !resolution.getFps().isEmpty()) {
                    result += " " + resolution.getFps();
                }
                listFormat.add(result);
            }
            weakHandler.sendEmptyMessage(MainParam.getFormatListSuccess);
            jschUtil.close();
        }

        @Override
        public void getFormatListFail(List<String> errLog) {
            listFormat.clear();
            listResolutions.clear();
            Message msg = new Message();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n\n" + "获取分辨率列表失败");
            for (String str : errLog) {
                stringBuilder.append("\n").append(str);
            }
            msg.obj = stringBuilder;
            msg.what = MainParam.getFormatListFail;
            weakHandler.sendMessage(msg);
            jschUtil.close();
        }
    };

    private void getM3u8UrlInLinux() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                weakHandler.sendEmptyMessage(MainParam.startGetM3u8Url);
                try {
                    jschUtil.versouSshUtil(serverIp, userName, userPassword, serverPort);
                    String resolutionNo = listResolutions.get(spFormatList.getSelectedItemPosition()).getResolutionNo();
                    jschUtil.runCmd("youtube-dl -f " + resolutionNo + " -g " + resourceUrl, "UTF-8", getM3u8UrlCallBack);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private GetM3u8UrlRunnable.GetM3u8UrlCallBack getM3u8UrlCallBack = new GetM3u8UrlRunnable.GetM3u8UrlCallBack() {
        @Override
        public void GetM3u8UrlSuccess(String m3u8Url) {
            MainActivity.this.m3u8Url = m3u8Url;
            weakHandler.sendEmptyMessage(MainParam.getM3u8UrlSuccess);
        }

        @Override
        public void GetM3u8UrlFail(List<String> errLog) {
            Message msg = new Message();
            msg.what = MainParam.getM3u8UrlFail;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n\n获取直播源失败");
            for (String str : errLog) {
                stringBuilder.append("\n").append(str);
            }
            msg.obj = stringBuilder;
            weakHandler.sendMessage(msg);
            jschUtil.close();
        }
    };

    private void showBackGroundDialog() {
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
                //直推m3u8或本地视频文件
                showConfirmDialog("是否需要开启后台推送？\n（开启后台推送后无法获取当前推流日志）", 2);
                break;
            case 1:
                //是
                needBackGround = true;
                pushStreamToLiveRoomInLinux();
                break;
            case 2:
                //否
                needBackGround = false;
                pushStreamToLiveRoomInLinux();
                break;
        }
    }

    private void pushStreamToLiveRoomInLinux() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    weakHandler.sendEmptyMessage(MainParam.startAssemblePushStreamCommand);
                    jschUtil.versouSshUtil(serverIp, userName, userPassword, serverPort);
                    String videoParams = null;
                    if (rbBoth.isChecked()) {
                        videoParams = " -c:v copy -c:a aac -strict -2 -f flv ";
                    } else if (rbOnlyAudio.isChecked()) {
                        videoParams = " -vn -c:a aac -strict -2  -f flv ";
                    } else if (rbOnlyImage.isChecked()) {
                        videoParams = " -c:v copy -an -strict -2  -f flv ";
                    }
                    String configScheme = sharedPreferencesUtil.getSharedPreference("ConfigScheme", "").toString();
                    ConfigSchemeBean configSchemeBean = gson.fromJson(configScheme, ConfigSchemeBean.class);
                    if (0 == configSchemeBean.getSchemeType()) {
                        String cache;
                        if (needBackGround) {
                            cache = "screen -dmS SimplePushStreamUtil ffmpeg -thread_queue_size 1024 -i " + m3u8Url + videoParams + "\"" + liveRoomUrl + "\"";
                        } else {
                            cache = "ffmpeg -thread_queue_size 1024 -i " + m3u8Url + videoParams + "\"" + liveRoomUrl + "\"";
                        }
                        System.out.println(cache);
                        jschUtil.runCmd(cache, "UTF-8", pushStreamCallBack);
                    } else if (1 == configSchemeBean.getSchemeType()) {
                        String cache;
                        String resolutionPx = listResolutions.get(spFormatList.getSelectedItemPosition()).getResolutionPx();
                        if (resolutionPx.contains("(")) {
                            resolutionPx = resolutionPx.substring(0, resolutionPx.lastIndexOf("("));
                        }
                        if (needBackGround) {
                            cache = "nohup streamlink -O " + resourceUrl + " " + resolutionPx + " | ffmpeg -thread_queue_size 1024 -i pipe:0 " + videoParams + "\"" + liveRoomUrl + "\"" + ";" + "jobs -l";
                        } else {
                            cache = "streamlink -O " + resourceUrl + " " + resolutionPx + " | ffmpeg -thread_queue_size 1024 -i pipe:0 " + videoParams + "\"" + liveRoomUrl + "\"";
                        }
                        System.out.println(cache);
                        jschUtil.runCmd(cache, "UTF-8", pushStreamCallBack);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private PushStreamRunnable.PushStreamCallBack pushStreamCallBack = new PushStreamRunnable.PushStreamCallBack() {
        @Override
        public void pushing(String size, String time, String bitrate) {
            BigDecimal bigDecimal = new BigDecimal(size);
            int i = bigDecimal.intValue();
            String result1;
            if (i >= 1000) {
                result1 = bigDecimal.divide(new BigDecimal(1024)).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "MB";
            } else {
                result1 = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "KB";
            }
            String result2 = new BigDecimal(bitrate).divide(new BigDecimal(8)).setScale(0, BigDecimal.ROUND_HALF_UP).toString();
            Message msg = new Message();
            msg.what = MainParam.pushStreamLog;
            msg.obj = simpleDateFormat.format(new Date()) + "\n" + "已推送文件大小：" + result1 + "\u3000已推流时长：" + time.substring(0, time.lastIndexOf(".")) + "\u3000上传速度：" + result2 + "KB/S";
            weakHandler.sendMessage(msg);
        }

        @Override
        public void pushFail(String reason) {
            Message msg = new Message();
            msg.what = MainParam.pushStreamLog;
            msg.obj = reason;
            weakHandler.sendMessage(msg);
        }
    };

    private void stopPushStreamInLinux() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    jschUtil.versouSshUtil(serverIp, userName, userPassword, serverPort);
                    List<String> ls = jschUtil.runCmd("ps -aux|grep " + "\"" + liveRoomUrl + "\"" + "| grep -v grep | awk '{print $2}'", "UTF-8");
                    for (String str : ls) {
                        jschUtil.runCmd("kill -9 " + str, "UTF-8");
                    }
                    jschUtil.close();
                    weakHandler.sendEmptyMessage(MainParam.stopPushStreamSuccess);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean loadServerInfo() {
        serverIp = etServerIp.getText().toString();
        userName = etUserName.getText().toString();
        userPassword = etPassWord.getText().toString();
        String port = etServerPort.getText().toString();
        if (null == serverIp || serverIp.isEmpty()) {
            Toast.makeText(getApplicationContext(), "服务器ip不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        } else if (TextUtils.isEmpty(port)) {
            Toast.makeText(getApplicationContext(), "端口号不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        } else if (null == userName || userName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "用户名不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        } else if (null == userPassword || userPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "密码不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        }
        serverPort = Integer.parseInt(port);
        return false;
    }

}
