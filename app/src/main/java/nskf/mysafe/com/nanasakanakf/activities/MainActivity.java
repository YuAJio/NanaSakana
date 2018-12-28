package nskf.mysafe.com.nanasakanakf.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qiyukf.nimlib.sdk.NimIntent;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.YSFUserInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import nskf.mysafe.com.nanasakanakf.utils.NSKFManager;

public class MainActivity extends AppCompatActivity {

    //Gson所需对象
    private Gson gson;
    private GsonBuilder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
//初始化Gson
        initGson();

        final Activity thisActivity = this;

//        startActivity(new Intent(this, Main2Activity.class));

        if (intent != null) {
            Bundle data = intent.getBundleExtra("data");
            if (data != null) {
                setUserInfo(getUserInfo(data));
                String title = data.getString("title");
                String sourceTitle = data.getString("sourceTitle");
                String sourceUrl = data.getString("sourceUrl");
                openKFInMainProcess(title, sourceUrl, sourceTitle);
//                parseIntent(title,sourceUrl,sourceTitle);
            } else
                this.finish();
        } else
            this.finish();


//
//        findViewById(R.id.tv_inter).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
////                ConsultSource source = new ConsultSource(null, null, "custom information string");
////                /**
////                 * 请注意： 调用该接口前，应先检查Unicorn.isServiceAvailable()，
////                 * 如果返回为false，该接口不会有任何动作
////                 *
////                 * @param context 上下文
////                 * @param title   聊天窗口的标题
////                 * @param source  咨询的发起来源，包括发起咨询的url，title，描述信息等
////                 */
////                if (Unicorn.isServiceAvailable())
////                    Unicorn.openServiceActivity(thisActivity, "東北きりたん", source);
////                else {
////
////                }
//            }
//        });
    }


    /**
     * 在主线程下打开客服页面
     *
     * @param title       客服左上角标题
     * @param sourceUrl   客服超链接
     * @param sourceTitle 客服连接标题
     */
    private void openKFInMainProcess(String title, String sourceUrl, String sourceTitle) {

        if (inMainProcess(this)) {
            NSKFManager.getInstance().openCsPage(
                    this,
                    title,
                    sourceUrl,
                    sourceTitle, "");
            this.finish();
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //如果不再主线程上 延迟几秒在进入
            openKFInMainProcess(title, sourceUrl, sourceTitle);
        }

    }

    /**
     * 初始化Gson
     */
    private void initGson() {
        builder = new GsonBuilder();
        gson = builder.create();
    }

    public static boolean inMainProcess(Context context) {
        String mainProcessName = context.getApplicationInfo().processName;
        String processName = getProcessName();
        return TextUtils.equals(mainProcessName, processName);
    }

    /**
     * 获取当前进程名
     */
    private static String getProcessName() {
        BufferedReader reader = null;
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            reader = new BufferedReader(new FileReader(file));
            return reader.readLine().trim();
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置用户信息
     *
     * @param info
     */
    private void setUserInfo(Md_UserInfo info) {
        YSFUserInfo userInfo = new YSFUserInfo();
        // App 的用户 ID
        userInfo.userId = info.UserId;
        // 当且仅当开发者在管理后台开启了 authToken 校验功能时，该字段才有效
        userInfo.authToken = "auth-token-from-user-server";
        // CRM 扩展字段
        userInfo.data = userInfoData(info.Name, info.Phone, info.Email, info.Avatar, null, null, null).toJSONString();
//        userInfo.data = "[" +
//                "{\"key\":\"real_name\",\"value\":\"" + info.Name + "\"}," +
//                "{\"key\":\"mobile_phone\",\"value\":\"" + info.Phone + "\",\"hidden\":\"false\"}," +
//                "{\"key\":\"email\",\"value\":\"" + info.Email + "\"}," +
//                "{\"key\":\"avatar\",\"value\":\"" + info.Avatar + "\"}" +
//                "]";
        Unicorn.setUserInfo(userInfo);
    }

    /**
     * 从json数据中获取用户信息
     *
     * @param bundle
     * @return
     */
    private Md_UserInfo getUserInfo(Bundle bundle) {
        Md_UserInfo info;
        String jsonData = bundle.getString("userInfo");
        info = gson.fromJson(jsonData, Md_UserInfo.class);
//        info = jsonData.<Md_UserInfo>();


//        info.Avatar = bundle.getString("ava");
//        info.Email = bundle.getString("ema");
//        info.Name = bundle.getString("nam");
//        info.Phone = bundle.getString("pho");
//        info.UserId = bundle.getString("uid");
        return info;
    }

    private JSONArray userInfoData(String name, String mobile, String email, String avatar, String auth, String card, String order) {
        JSONArray array = new JSONArray();
        array.add(userInfoDataItem("real_name", name, false, -1, null, null)); // name
        array.add(userInfoDataItem("mobile_phone", mobile, false, -1, null, null)); // mobile
        array.add(userInfoDataItem("email", email, false, -1, null, null)); // email
        array.add(userInfoDataItem("avatar", avatar, false, -1, null, null)); // email
        array.add(userInfoDataItem("real_name_auth", auth, false, 0, "实名认证", null));
        array.add(userInfoDataItem("bound_bank_card", card, false, 1, "绑定银行卡", null));
        array.add(userInfoDataItem("recent_order", order, false, 2, "最近订单", null));

        return array;
    }

    private JSONObject userInfoDataItem(String key, Object value, boolean hidden, int index, String label, String href) {
        JSONObject item = new JSONObject();
        item.put("key", key);
        item.put("value", value);
        if (hidden) {
            item.put("hidden", true);
        }
        if (index >= 0) {
            item.put("index", index);
        }
        if (!TextUtils.isEmpty(label)) {
            item.put("label", label);
        }
        if (!TextUtils.isEmpty(href)) {
            item.put("href", href);
        }
        return item;
    }

    private class Md_UserInfo {
        String UserId;
        String Name;
        String Phone;
        String Email;
        String Avatar;

        public String getUserId() {
            return UserId;
        }

        public void setUserId(String userId) {
            UserId = userId;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public String getPhone() {
            return Phone;
        }

        public void setPhone(String phone) {
            Phone = phone;
        }

        public String getEmail() {
            return Email;
        }

        public void setEmail(String email) {
            Email = email;
        }

        public String getAvatar() {
            return Avatar;
        }

        public void setAvatar(String avatar) {
            Avatar = avatar;
        }
    }

    /**
     * FC不保存状态，防止重启后fragment重复添加
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    private void parseIntent(String title, String sourceUrl, String sourceTitle) {
        Intent intent = getIntent();
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            openKFInMainProcess(title, sourceUrl, sourceTitle);
            // 最好将intent清掉，以免从堆栈恢复时又打开客服窗口
            setIntent(new Intent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent(null, null, null);
        super.onNewIntent(intent);
    }

    //    @Override
//    public void finish() {
//        this.setResult(RESULT_OK);
//        super.finish();
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (getIntent() != null)
//            if (inMainProcess(this))
//                Unicorn.logout();
//        super.onDestroy();
//    }
}
