package nskf.mysafe.com.nanasakanakf.utils;

import android.content.Context;

import com.qiyukf.unicorn.api.ConsultSource;
import com.qiyukf.unicorn.api.StatusBarNotificationConfig;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.YSFOptions;
import com.qiyukf.unicorn.api.lifecycle.SessionLifeCycleListener;
import com.qiyukf.unicorn.api.lifecycle.SessionLifeCycleOptions;

import nskf.mysafe.com.nanasakanakf.NanaContest;
import nskf.mysafe.com.nanasakanakf.R;
import nskf.mysafe.com.nanasakanakf.interfaces.FrescoImageLoader;

public class NSKFManager {


    /**
     * 单例
     */
    private static NSKFManager Instance;

    /**
     * 获取单例
     *
     * @return
     */
    public static NSKFManager getInstance() {
        if (Instance == null)
            Instance = new NSKFManager();
        return Instance;
    }


    /**
     * 初始化七鱼客服
     *
     * @param context
     * @return
     */
    public boolean initNanaSakana(Context context) {
        YSFOptions options = new YSFOptions();
        options.statusBarNotificationConfig = new StatusBarNotificationConfig();
        options.statusBarNotificationConfig.notificationSmallIconId = R.drawable.ysf_video_play_icon;
        return Unicorn.init(context, NanaContest.NanaFishAppKey, options, new FrescoImageLoader(context));
    }

    /**
     * 退出登录
     * 在关闭程序的时候或是退出的时候 结束与七鱼的连系
     */
    public void quitUserLogin() {
        Unicorn.logout();
    }


    private static String Title;
    private static String SourceUrl;
    private static String SourceTitle;

    /**
     * 打开客服页面
     *
     * @param context     上下文
     * @param title       左上角标题
     * @param sourceUrl   来源页面的URL
     * @param sourceTitle 来源页面的标题
     * @param cis         无用
     * @return false:打开时失败
     */
    public boolean openCsPage(Context context, String title, String sourceUrl, String sourceTitle, String cis) {
        if (!isNullOrEmpty(title))
            Title = title;
        if (!isNullOrEmpty(sourceUrl))
            SourceUrl = sourceUrl;
        if (!(isNullOrEmpty(sourceTitle)))
            SourceTitle = sourceTitle;

        if (Unicorn.isServiceAvailable()) {
            Unicorn.openServiceActivity(context, Title, getConsultSource(SourceUrl, SourceTitle, cis));
            return true;
        } else
            return false;
    }

    private ConsultSource getConsultSource(String sourceUrl, String sourceTitle, String cis) {
        ConsultSource consultSource = new ConsultSource(sourceUrl, sourceTitle, cis);

        SessionLifeCycleOptions lifeCycleOptions = new SessionLifeCycleOptions();
        lifeCycleOptions.setCanCloseSession(true)
                .setCanQuitQueue(true)
                .setQuitQueuePrompt("");
        SessionLifeCycleListener lifeCycleListener = new SessionLifeCycleListener() {
            @Override
            public void onLeaveSession() {
                quitUserLogin();
            }
        };
        lifeCycleOptions.setSessionLifeCycleListener(lifeCycleListener);
        consultSource.sessionLifeCycleOptions = lifeCycleOptions;
        consultSource.groupId = NanaContest.NanaFishGroupID_Back;
        return consultSource;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.equals("") || value.length() <= 0;
    }

}

