package nskf.mysafe.com.nanasakanakf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.qiyukf.unicorn.api.OnBotEventListener;
import com.qiyukf.unicorn.api.StatusBarNotificationConfig;
import com.qiyukf.unicorn.api.UICustomization;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.YSFOptions;
import com.qiyukf.unicorn.api.customization.title_bar.OnTitleBarRightBtnClickListener;
import com.qiyukf.unicorn.api.customization.title_bar.TitleBarConfig;

import nskf.mysafe.com.nanasakanakf.activities.MainActivity;
import nskf.mysafe.com.nanasakanakf.interfaces.FrescoImageLoader;
import nskf.mysafe.com.nanasakanakf.utils.NSKFManager;

public class YsApplication extends Application {

    private StatusBarNotificationConfig mStausbarnc;

    private void setServiceEntranceActivity(Activity activity) {
        mStausbarnc.notificationEntrance = activity.getClass();
    }
    /**
     * 创建全局变量
     * 全局变量一般都比较倾向于创建一个单独的数据类文件，并使用static静态变量
     *
     * 这里使用了在Application中添加数据的方法实现全局变量
     * 注意在AndroidManifest.xml中的Application节点添加android:name=".MyApplication"属性
     *
     */
    private WindowManager.LayoutParams wmParams=new WindowManager.LayoutParams();


    public WindowManager.LayoutParams getMywmParams(){
        return wmParams;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //消息推送后打开页面
        mStausbarnc = new StatusBarNotificationConfig();
        mStausbarnc.notificationEntrance = MainActivity.class;

        //初始化七鱼客服
        Unicorn.init(this, ysfAppId(), ysfOptions(), new FrescoImageLoader(this));

        Fresco.initialize(this);
    }

    private String ysfAppId() {
        return NanaContest.NanaFishAppKey;
    }

    private YSFOptions ysfOptions() {
        YSFOptions options = new YSFOptions();
        //定制消息点击事件
        mStausbarnc.notificationSmallIconId = R.mipmap.ic_launcher;
        //定制UI页面
        UICustomization uiCustomization = new UICustomization();
        uiCustomization.hideKeyboardOnEnterConsult = true;
        //定制右上角按钮
        TitleBarConfig titleBarConfig = new TitleBarConfig();
        titleBarConfig.titleBarRightText = "退出";
        titleBarConfig.titleBarRightImg = R.mipmap.ic_logout;
        titleBarConfig.onTitleBarRightBtnClickListener = new OnTitleBarRightBtnClickListener() {
            @Override
            public void onClick(Activity activity) {
                NSKFManager.getInstance().quitUserLogin();
            }
        };

//        options.titleBarConfig = titleBarConfig;
        options.uiCustomization = uiCustomization;
        options.statusBarNotificationConfig = mStausbarnc;
        options.onBotEventListener = new OnBotEventListener() {
            @Override
            public boolean onUrlClick(Context context, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
                return true;
            }
        };
        return options;

    }

}
