package com.sunmi.ipc.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.datelibrary.DatePickDialog;
import com.datelibrary.bean.DateType;
import com.sunmi.ipc.R;
import com.sunmi.ipc.model.TimeBean;
import com.sunmi.ipc.rpc.IPCCall;
import com.sunmi.ipc.rpc.IpcConstants;
import com.sunmi.ipc.utils.AACDecoder;
import com.sunmi.ipc.utils.AudioMngHelper;
import com.sunmi.ipc.utils.H264Decoder;
import com.sunmi.ipc.utils.IOTCClient;
import com.sunmi.ipc.utils.TimeView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import sunmi.common.base.BaseActivity;
import sunmi.common.rpc.sunmicall.ResponseBean;
import sunmi.common.utils.CommonHelper;
import sunmi.common.utils.ThreadPool;
import sunmi.common.utils.log.LogCat;
import sunmi.common.view.VerticalSeekBar;

/**
 * Description:
 * Created by bruce on 2019/4/11.
 */
@EActivity(resName = "activity_video_play")
public class VideoPlayActivity extends BaseActivity
        implements SurfaceHolder.Callback, IOTCClient.Callback,
        SeekBar.OnSeekBarChangeListener {
    @ViewById(resName = "vv_ipc")
    SurfaceView videoView;
    @ViewById(resName = "sb_zoom")
    SeekBar sbZoom;
    @ViewById(resName = "sBar_voice")
    VerticalSeekBar sBarVoice;//音量控制
    @ViewById(resName = "ll_change_volume")
    LinearLayout llChangeVolume;//音量控制
    @ViewById(resName = "iv_record")
    ImageView ivRecord;//录制
    @ViewById(resName = "iv_volume")
    ImageView ivVolume;//音量
    @ViewById(resName = "tv_quality")
    TextView tvQuality;//画质
    @ViewById(resName = "ll_video_quality")
    LinearLayout llVideoQuality;//是否显示画质
    @ViewById(resName = "tv_hd_quality")
    TextView tvHDQuality;//高清画质
    @ViewById(resName = "tv_sd_quality")
    TextView tvSDQuality;//标清画质
    @ViewById(resName = "cm_timer")
    Chronometer cmTimer;//录制时间
    @ViewById(resName = "rl_record")
    RelativeLayout rlRecord;
    @ViewById(resName = "iv_calender")
    TextView ivCalender;//日历
    @ViewById(resName = "rl_top_setting")
    RelativeLayout rlTopSetting;//top设置
    @ViewById(resName = "ll_bottom")
    RelativeLayout rlBottomSetting;//bottom设置
    @ViewById(resName = "iv_screenshot")
    ImageView ivScreenshot;//截图
    @ViewById(resName = "iv_live")
    ImageView ivLive;//直播
    @ViewById(resName = "iv_play")
    ImageView ivPlay;//开始播放
    @ViewById(resName = "recyclerView")
    RecyclerView recyclerView;
    @ViewById(resName = "my_view")
    TimeView timeView;//时间绘制
    @ViewById(resName = "tv_show")
    TextView tvShow;//test time

    @Extra
    String UID;
    //手机屏幕的宽高
    private int screenW, screenH;
    private float currX = 540, currY = 860;

    int currZoom, currFocus;

    private H264Decoder mPlayer = null;
    private AACDecoder mAudioPlayer = null;

    private AudioMngHelper audioMngHelper = null;
    private boolean isStartRecord;//是否开始录制
    private boolean isShowVolume;//是否显示音量调控
    private boolean isShowQuality;//是否画质
    private boolean isClickScreen;//是否点击屏幕
    private boolean isStartPlay;//是否开始播放
    private boolean isCurrentPlayBack;//是否正在回放

    //
    private LinearLayoutManager linearLayoutManager;
    //日历
    private Calendar calendar;
    //选择视频日期列表
    private List<TimeBean> list = new ArrayList<>();
    //绘制的小时列表
    private List<String> dateList = new ArrayList<>();
    //当前时间 ，三天前秒数 ，未来6小时后的秒数 ，区间总共秒数
    private long currentDateSeconds, threeDaysBeforeSeconds, sixHoursAfterSeconds, minutesTotal;
    //手机屏幕的宽高
    private int mScreenWidth, mScreenHeight;
    //3天秒数
    private long threeDaysSeconds = 3 * 24 * 60 * 60;
    //6小时后的秒数
    private int sixHoursSeconds = 6 * 60 * 60;
    //3天的分钟数 1分钟=1dp
    private long threeDaysMinutes = 3 * 24 * 60;
    //当前分钟走的秒数
    private int currentSecond;
    //是否点击滚动到某个位置
    private boolean isOnclickScroll;
    //刻度尺移动定时器
    private Timer moveTimer;


    //用于播放视频的mediaPlayer对象
    private MediaPlayer firstPlayer,     //负责播放进入视频播放界面后的第一段视频
            nextMediaPlayer, //负责一段视频播放结束后，播放下一段视频
            cachePlayer,     //负责setNextMediaPlayer的player缓存对象
            currentPlayer;   //负责当前播放视频段落的player对象
    private SurfaceHolder surfaceHolder;
    //存放所有视频端的url
    private ArrayList<String> videoListQueue = new ArrayList<>();
    //所有player对象的缓存
    private HashMap<String, MediaPlayer> playersCache = new HashMap<>();
    //当前播放到的视频段落数
    private int currentVideoIndex;


    @AfterViews
    void init() {
        //保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏
        screenW = CommonHelper.getScreenWidth(context);
        screenH = CommonHelper.getScreenHeight(context);
        sbZoom.setOnSeekBarChangeListener(this);

        //当前天
        calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        ivCalender.setText(currentDay + "");

        layoutManger();
        recyclerViewInit();

        //设置播放器的宽高
        ViewGroup.LayoutParams lp = videoView.getLayoutParams();
        //lp.height = lp.width = CommonHelper.getScreenHeight(context);//设置等宽
        lp.width = screenW;//屏幕宽高
        lp.height = screenH;
        videoView.setLayoutParams(lp);

        //回调
        IOTCClient.setCallback(this);
        surfaceHolder = videoView.getHolder();// SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this); // 因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
        //videoView.getHolder().addCallback(this);
        mAudioPlayer = new AACDecoder();
//        IPCCall.getInstance().fsGetStatus(context);//fs

        //直播
//        playClick();
        //初始化音量
        adjustVoice();
        initGetVolume();
    }

    @Override
    protected boolean needLandscape() {
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        LogCat.e(TAG, "666666 seekBar.getProgress() = " + seekBar.getProgress());
        IPCCall.getInstance().fsZoom(seekBar.getProgress(), context);
    }

    //开始直播
    void playClick() {
        ThreadPool.getCachedThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                IOTCClient.start(UID);
            }
        });
    }

    @Click(resName = "tv_add")
    void addClick() {
        if (currFocus > 775) {
            shortTip("已到最大焦距");
            return;
        }
        IPCCall.getInstance().fsFocus(currFocus += 5, context);
    }

    @Click(resName = "tv_minus")
    void minusClick() {
        if (currFocus < 5) {
            shortTip("已到最小焦距");
            return;
        }
        IPCCall.getInstance().fsFocus(currFocus -= 5, context);
    }

    @Click(resName = "tv_auto")
    void autoFocusClick() {
        IPCCall.getInstance().fsAutoFocus(context);
    }

    @Click(resName = "btn_next")
    void nextClick() {
        LogCat.e(TAG, "addClick");
        if (screenH <= 0 || screenW <= 0) {
            shortTip("已到最小焦距");
            return;
        }
        //        IPCCall.getInstance().fsReset(context);
        IPCCall.getInstance().fsIrMode(0, context);
        shortTip("x = " + (int) currX * 100 / screenW);
        shortTip("y = " + (int) currY * 100 / screenH);
        IPCCall.getInstance().fsSetFocusPoint((int) currX * 100 / screenW, (int) currY * 100 / screenH, context);
    }

    @Click(resName = "rl_video_back")
    void backClick() {
        this.finish();
    }

    //视频录制
    @Click(resName = "iv_record")
    void recordClick() {
        if (isStartRecord) {
            ivRecord.setBackgroundResource(R.mipmap.ic_recording_normal);
            rlRecord.setVisibility(View.GONE);
            isStartRecord = false;
            cmTimer.stop();//关闭录制
        } else {
            ivRecord.setBackgroundResource(R.mipmap.ic_recording);
            rlRecord.setVisibility(View.VISIBLE);
            isStartRecord = true;
            startRecord();//开始录制
        }
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    //开始计时录制
    private void startRecord() {
        cmTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = System.currentTimeMillis() - cArg.getBase();
                Date d = new Date(time);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                cmTimer.setText(sdf.format(d));
            }
        });
        cmTimer.setBase(System.currentTimeMillis());
        cmTimer.start();
    }

    //获取当前音量
    private void initGetVolume() {
        int currentVolume100 = audioMngHelper.get100CurrentVolume();
        if (currentVolume100 == 0) {
            ivVolume.setBackgroundResource(R.mipmap.ic_muse);
        } else {
            ivVolume.setBackgroundResource(R.mipmap.ic_volume);
        }
    }

    //音量
    @Click(resName = "iv_volume")
    void volumeClick() {
        if (isShowVolume) {
            llChangeVolume.setVisibility(View.GONE);
            isShowVolume = false;
        } else {
            llChangeVolume.setVisibility(View.VISIBLE);
            //获取当前音量
            int currentVolume100 = audioMngHelper.get100CurrentVolume();
            if (currentVolume100 == 0) {
                ivVolume.setBackgroundResource(R.mipmap.ic_muse);
            } else {
                ivVolume.setBackgroundResource(R.mipmap.ic_volume);
            }
            sBarVoice.setProgress(currentVolume100);
            isShowVolume = true;
        }
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    //画质
    @Click(resName = "tv_quality")
    void qualityClick() {
        if (isShowQuality) {
            llVideoQuality.setVisibility(View.GONE);
            isShowQuality = false;
        } else {
            llVideoQuality.setVisibility(View.VISIBLE);
            isShowQuality = true;
        }
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    //高清画质
    @Click(resName = "tv_hd_quality")
    void hdQualityClick() {
        llVideoQuality.setVisibility(View.GONE);
        isShowQuality = false;
        tvQuality.setText(R.string.str_HD);
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    int valueType = 0;

    //标清画质
    @Click(resName = "tv_sd_quality")
    void sdQualityClick() {
        llVideoQuality.setVisibility(View.GONE);
        isShowQuality = false;
        tvQuality.setText(R.string.str_SD);
        valueType = valueType == 0 ? 1 : 0;
        LogCat.e(TAG, "11111111 va" + valueType);
        IOTCClient.changeValue(valueType);
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    //开始，暂停
    @Click(resName = "iv_play")
    void playLiveClick() {
        playClick();
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    //直播
    @Click(resName = "iv_live")
    void playApBackClick() {
        playClick();
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }


    //显示日历
    @Click(resName = "iv_calender")
    void calenderClick() {
        //第三方
        DatePickDialog dialog = new DatePickDialog(this);
        //设置上下年分限制
        dialog.setYearLimt(100);
        //设置标题
        dialog.setTitle("选择时间");
        //设置类型
        dialog.setType(DateType.TYPE_YMD);
        //设置消息体的显示格式，日期格式
        dialog.setMessageFormat("yyyy-MM-dd HH:mm");
        //设置选择回调
        dialog.setOnChangeLisener(null);
        //设置点击确定按钮回调
        dialog.setOnSureLisener(null);
        dialog.show();
    }

    //点击屏幕
    @Click(resName = "rl_screen")
    void screenClick() {
        if (isClickScreen) {
            rlTopSetting.setVisibility(View.VISIBLE);
            ivRecord.setVisibility(View.VISIBLE);
            ivScreenshot.setVisibility(View.VISIBLE);
            ivLive.setVisibility(View.VISIBLE);
            rlBottomSetting.setVisibility(View.VISIBLE);

            isClickScreen = false;
        } else {
            rlTopSetting.setVisibility(View.GONE);
            ivRecord.setVisibility(View.GONE);
            ivScreenshot.setVisibility(View.GONE);
            ivLive.setVisibility(View.GONE);
            rlBottomSetting.setVisibility(View.GONE);

            isClickScreen = true;
        }
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    //*********************************************************************
    //***********************云端回放***************************************
    //*********************************************************************
    /*
     * 初始化播放首段视频的player
     */
    private void initFirstPlayer() {
        firstPlayer = new MediaPlayer();
        firstPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        firstPlayer.setDisplay(surfaceHolder);

        firstPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        onVideoPlayCompleted(mp);
                    }
                });

        //设置cachePlayer为该player对象
        cachePlayer = firstPlayer;
        initNexttPlayer();

        //player对象初始化完成后，开启播放
        startPlayFirstVideo();
    }

    private void startPlayFirstVideo() {
        try {
            firstPlayer.setDataSource(videoListQueue.get(currentVideoIndex));
            firstPlayer.prepare();
            firstPlayer.start();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    /*
     * 新开线程负责初始化负责播放剩余视频分段的player对象,避免UI线程做过多耗时操作
     */
    private void initNexttPlayer() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                for (int i = 1; i < videoListQueue.size(); i++) {
                    nextMediaPlayer = new MediaPlayer();
                    nextMediaPlayer
                            .setAudioStreamType(AudioManager.STREAM_MUSIC);

                    nextMediaPlayer
                            .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    onVideoPlayCompleted(mp);
                                }
                            });

                    try {
                        nextMediaPlayer.setDataSource(videoListQueue.get(i));
                        nextMediaPlayer.prepare();
                    } catch (IOException e) {
                        // TODO 自动生成的 catch 块
                        e.printStackTrace();
                    }

                    //set next mediaplayer
                    cachePlayer.setNextMediaPlayer(nextMediaPlayer);
                    //set new cachePlayer
                    cachePlayer = nextMediaPlayer;
                    //put nextMediaPlayer in cache
                    playersCache.put(String.valueOf(i), nextMediaPlayer);

                }

            }
        }).start();
    }

    /*
     * 负责处理一段视频播放过后，切换player播放下一段视频
     */
    private void onVideoPlayCompleted(MediaPlayer mp) {
        mp.setDisplay(null);
        //get next player
        currentPlayer = playersCache.get(String.valueOf(++currentVideoIndex));
        if (currentPlayer != null) {
            currentPlayer.setDisplay(surfaceHolder);
        } else {
            shortTip("视频播放完毕");
        }
    }

    private void getVideoUrls() {
//        videoListQueue.add("http://sunmi-test.oss-cn-hangzhou.aliyuncs.com/VIDEO/IPC/SS101D8BS00088/0_25.mp4");
//        videoListQueue.add("http://sunmi-test.oss-cn-hangzhou.aliyuncs.com/VIDEO/IPC/SS101D8BS00088/25_50.mp4");
//        videoListQueue.add("http://sunmi-test.oss-cn-hangzhou.aliyuncs.com/VIDEO/IPC/SS101D8BS00088/0_25.mp4");
//        videoListQueue.add("http://sunmi-test.oss-cn-hangzhou.aliyuncs.com/VIDEO/IPC/SS101D8BS00088/25_50.mp4");

        videoListQueue.add("http://sunmi-test.oss-cn-hangzhou.aliyuncs.com/VIDEO/IPC/SS101D8BS00088/0_20.mp4");
        videoListQueue.add("http://sunmi-test.oss-cn-hangzhou.aliyuncs.com/VIDEO/IPC/SS101D8BS00088/20_40.mp4");
        videoListQueue.add("http://sunmi-test.oss-cn-hangzhou.aliyuncs.com/VIDEO/IPC/SS101D8BS00088/40_60.mp4");
    }

    /*
     * 负责界面销毁时，release各个mediaplayer
     * @see android.app.Activity#onDestroy()
     */
    private void cloudPlayDestroy() {
        if (firstPlayer != null) {
            if (firstPlayer.isPlaying()) {
                firstPlayer.stop();
            }
            firstPlayer.release();
        }
        if (nextMediaPlayer != null) {
            if (nextMediaPlayer.isPlaying()) {
                nextMediaPlayer.stop();
            }
            nextMediaPlayer.release();
        }

        if (currentPlayer != null) {
            if (currentPlayer.isPlaying()) {
                currentPlayer.stop();
            }
            currentPlayer.release();
        }
        currentPlayer = null;
    }


    //***********************云端回放***************************************!
    //*********************************************************************

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer = new H264Decoder(holder.getSurface(), 0);

        //surfaceView创建完毕后，首先获取该直播间所有视频分段的url
//        getVideoUrls();
////        //然后初始化播放手段视频的player对象
//        initFirstPlayer();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //关闭操作
        if (mPlayer != null) {
            mPlayer.stopRunning();
            mPlayer = null;
        }
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            //获取点击屏幕的位置，作为焦点位置，用于计算对焦区域
//            float x = event.getX();
//            float y = event.getY();
//
//            if (120 < x && x < 1800 && 160 < y && y < 800) {
//                currX = x;
//                currY = y;
//                //对焦并绘制对焦矩形框
//                overCameraView.setTouchFoucusRect(x, y);
//            }
//        }
//        return false;
//    }

    @Override
    public void onVideoReceived(byte[] videoBuffer) {
        if (mPlayer != null)
            mPlayer.setVideoData(videoBuffer);
    }

    @Override
    public void onAudioReceived(byte[] audioBuffer) {
        mAudioPlayer.setAudioData(audioBuffer);
    }

    @Override
    public int[] getStickNotificationId() {
        return new int[]{IpcConstants.fsAutoFocus, IpcConstants.fsFocus, IpcConstants.fsGetStatus,
                IpcConstants.fsIrMode, IpcConstants.fsReset, IpcConstants.fsZoom};
    }

    @UiThread
    void setSeekBarProgress(int progress) {
        sbZoom.setProgress(progress);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (args == null) return;
        ResponseBean res = (ResponseBean) args[0];
        if (res == null) return;
        if (id == IpcConstants.fsGetStatus) {
            //{"data":[{"opcode":"0x3109","result":{"zoom":0,"max_zoom":500,"max_focus":780,"irmode":0,"auto_focus_start":0,"focus":389},"errcode":1}],"msg_id":"11111","errcode":1}
            try {
                JSONObject jsonObject = res.getResult();
                if (jsonObject.has("zoom")) {
                    currZoom = jsonObject.getInt("zoom");
                    setSeekBarProgress(currZoom);
                }
                if (jsonObject.has("focus")) {
                    currFocus = jsonObject.getInt("focus");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            LogCat.e(TAG, "666666,111res = " + res.toString());
        } else if (id == IpcConstants.fsAutoFocus) {
            LogCat.e(TAG, "666666,222res = " + res.toString());
        } else if (id == IpcConstants.fsReset) {
        }
    }

    //调节音量
    private void adjustVoice() {
        audioMngHelper = new AudioMngHelper(this);
        //int systemCurrent = audioMngHelper.getSystemCurrentVolume();
        //int systemMax = audioMngHelper.getSystemMaxVolume();
        int currentVolume100 = audioMngHelper.get100CurrentVolume();
        sBarVoice.setMax(100);
        sBarVoice.setProgress(currentVolume100);
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
        sBarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    ivVolume.setBackgroundResource(R.mipmap.ic_muse);
                } else {
                    ivVolume.setBackgroundResource(R.mipmap.ic_volume);
                }
                audioMngHelper.setVoice100(progress);
                canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
            }
        });
    }

    //按键控制音量，return true时不显示系统音量 return false时显示系统音量
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            llChangeVolume.setVisibility(View.GONE);
            isShowVolume = false;
            //获取当前音量
            int currentVolume100 = audioMngHelper.get100CurrentVolume();
            if (currentVolume100 == 0) {
                ivVolume.setBackgroundResource(R.mipmap.ic_muse);
            } else {
                ivVolume.setBackgroundResource(R.mipmap.ic_volume);
            }
            return false;
        } else return super.onKeyDown(keyCode, event);
    }

    /**
     * *******************生命周期***************************
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TAG", "onResume");
        canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeMove();//关闭时间抽的timer
        cloudPlayDestroy();//关闭云端视频
    }

    /**
     * *******************时间滑动条***************************
     */

    //初始 recyclerView
    private void layoutManger() {
        //布局管理器
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);


        //当前时间秒数
        currentDateSeconds = System.currentTimeMillis() / 1000;
        //三天前秒数
        threeDaysBeforeSeconds = currentDateSeconds - threeDaysSeconds;
        //6小时后的秒数
        sixHoursAfterSeconds = currentDateSeconds + sixHoursSeconds;
        //区间总共秒数 --当前时间前三天+未来6小时的秒数
        minutesTotal = threeDaysSeconds + sixHoursSeconds;
        currentSecond = calendar.get(Calendar.SECOND);//当前分钟走的秒数
    }

    int itemPosition = 5;

    //开始移动
    public void openMove() {
        if (moveTimer != null) {
            moveTimer.cancel();
            moveTimer = null;
        }
        moveTimer = new Timer();
        moveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        int center = (lastVisibleItem - firstVisibleItem) / 2 + firstVisibleItem + 1;
                        TimeBean bs = list.get(center);
                        String str = secondToDate(bs.getDate(), "yyyy-MM-dd HH:mm:ss");
                        LogCat.e("TAG", "firstVisibleItem=" + firstVisibleItem + " lastVisibleItem=" + lastVisibleItem + "firstVisibleItem: date: " + str);
                        linearLayoutManager.scrollToPositionWithOffset(itemPosition++, 0);//
                        tvShow.setText(str);
                        //绘制时间点和偏移量
                        canvasHours(firstVisibleItem);
                    }
                });
            }
        }, 0, 1000);//一分钟轮询一次
    }

    //结束移动
    public void closeMove() {
        if (moveTimer != null) {
            moveTimer.cancel();
        }
    }

    //日期列表
    private List<TimeBean> timeList(List<TimeBean> list) {
        //添加时间列表（当前的日期）
        TimeBean bean;
        for (int i = 0; i < minutesTotal; i += 60) {//10分钟一个item
            bean = new TimeBean();
            bean.setDate(threeDaysBeforeSeconds - currentSecond + i);
            list.add(bean);
        }

        //选择的日期当前的秒数
//        calendar.clear();
//        calendar.set(2019, 4, 12, 0, 0, 0);//设置时候月份减1即是当月
//        long selectedDate = calendar.getTimeInMillis() / 1000;//设置日期的秒数
//        String str = secondToDate(selectedDate, "yyyy-MM-dd HH:mm:ss");
//        LogCat.e("TAG", "selectedDate=" + str);
//        threeDaysBeforeSeconds = selectedDate - threeDaysSeconds;//选择日期三天前的秒数
//        minutesTotal = currentDateSeconds - selectedDate + threeDaysSeconds + sixHoursSeconds;//区间总共秒数
//        //添加时间列表（选择的日期）
//        TimeBean bean;
//        for (int i = 0; i < minutesTotal; i += 60) {//10分钟一个item 60秒的过度
//            bean = new TimeBean();
//            bean.setDate(threeDaysBeforeSeconds + i);
//            list.add(bean);
//        }

        return list;
    }

    //绘制时间
    int offsetPx;

    private void canvasHours(int firstVisibleItem) {
        //int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
        if (firstVisibleItem < 0) return;
        TimeBean bs = list.get(firstVisibleItem);
        String str = secondToDate(bs.getDate(), "HH:mm:ss");
        int hour = Integer.valueOf(str.substring(0, 2));
        int minute = Integer.valueOf(str.substring(3, 5));
        offsetPx = 0;//第一个小时的偏移量
        if (minute == 0) {
            offsetPx = 0;
        } else {
            offsetPx = (60 - minute) * CommonHelper.dp2px(VideoPlayActivity.this, 1);//偏移
            hour++;
        }
        //绘制下方时间
        dateList.clear();
        for (int i = 0; i < 13; i++) {
            if (hour <= 23) {
                dateList.add((hour++) + ":00");
            } else {
                hour = 0;
            }
            timeView.refresh(dateList, offsetPx);
        }
    }

    private void recyclerViewInit() {
        //添加时间列表
        timeList(list);
        //adapter
        DateAdapter adapter = new DateAdapter(list);
        recyclerView.setAdapter(adapter);
        //recyclerView 滑动监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {//停止滑动
                    if (!isCurrentPlayBack) {
                        IOTCClient.startPlayback();//设备回放
                        isCurrentPlayBack = true;
                    }
                    LogCat.e("TAG", "onScrolled00 ____");
                    int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    int center = (lastVisibleItem - firstVisibleItem) / 2 + firstVisibleItem;
                    TimeBean bs = list.get(center);
                    long date = bs.getDate();
                    String str = secondToDate(date, "yyyy-MM-dd HH:mm:ss");
                    String day = str.substring(8, 11);
                    LogCat.e("TAG", "firstVisibleItem=" + firstVisibleItem + " lastVisibleItem=" + lastVisibleItem + "   firstVisibleItem: date: " + str);
                    ivCalender.setText(day);  //滑动停止显示日期
                    toastForShort(VideoPlayActivity.this, str);//toast显示时间
                    canvasHours(linearLayoutManager.findFirstVisibleItemPosition());//绘制时间轴

                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    LogCat.e("TAG", "onScrolled11 ____");
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    LogCat.e("TAG", "onScrolled22 _____");
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LogCat.e("TAG", "onScrolled33 _____");
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                canvasHours(firstVisibleItem);//绘制时间
                //点击滚动某个position
                if (isOnclickScroll) {
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    int center = (lastVisibleItem - firstVisibleItem) / 2 + firstVisibleItem + 1;
                    TimeBean bs = list.get(center);
                    String str = secondToDate(bs.getDate(), "yyyy-MM-dd HH:mm:ss");
                    LogCat.e("TAG", "firstVisibleItem*** =" + firstVisibleItem + " lastVisibleItem=" + lastVisibleItem + "   firstVisibleItem: date: " + str);
                    tvShow.setText(str);

                    isOnclickScroll = false;
                }
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                openMove();
//            }
//        }, 2000);
    }

    //date adapter
    public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
        private List<TimeBean> list;

        public DateAdapter(List<TimeBean> list) {
            this.list = list;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvLine;
            RelativeLayout rlItem;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLine = itemView.findViewById(R.id.tv_line);
                rlItem = itemView.findViewById(R.id.rl_item);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_time, viewGroup, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            TimeBean bean = list.get(i);
            long date = bean.getDate();
            String str = secondToDate(date, "HH:mm:ss");
//            String dateString = secondToDate(date, "yyyy-MM-dd hh:mm:ss");
//            String hourMinute = str.substring(0, 5);
            String minuteSecond = str.substring(3, 8);
            String hour = str.substring(3, 5);
            LogCat.e("TAG", "minute=" + minuteSecond);
            //渲染
//            if (60 < i && i < 200) {
//                viewHolder.rlItem.setBackgroundResource(R.color.colorOrange);
//            }
            //当前时间线的高度
            ViewGroup.LayoutParams lp = viewHolder.tvLine.getLayoutParams();
            if (minuteSecond.contains("10:00") ||
                    minuteSecond.contains("20:00") ||
                    minuteSecond.contains("30:00") ||
                    minuteSecond.contains("40:00") ||
                    minuteSecond.contains("50:00")) {
                viewHolder.tvLine.setVisibility(View.VISIBLE);
                lp.height = 15;
                lp.width = 1;
                viewHolder.tvLine.setLayoutParams(lp);

            } else if (hour.contains("00")) {
                viewHolder.tvLine.setVisibility(View.VISIBLE);
                lp.height = 30;
                lp.width = CommonHelper.dp2px(VideoPlayActivity.this, 1);
                viewHolder.tvLine.setLayoutParams(lp);

            } else {
                viewHolder.tvLine.setVisibility(View.INVISIBLE);
                lp.height = 15;
                lp.width = 1;
                viewHolder.tvLine.setLayoutParams(lp);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    /**
     * 秒转换为指定格式的日期
     *
     * @param second
     * @param patten
     * @return
     */
    private String secondToDate(long second, String patten) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(second * 1000);//转换为毫秒
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(patten);
        String dateString = format.format(date);
        return dateString;
    }

    //自定义toast
    private Toast mToast;

    public void toastForShort(final Context context, final String msg) {

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (context == null || TextUtils.isEmpty(msg)) {
                    return;
                }
                View layout = LayoutInflater.from(context).inflate(R.layout.toast_item, null);
                TextView tvDate = layout.findViewById(R.id.tv_date);
                tvDate.setText(msg);
                if (mToast == null) {
                    mToast = new Toast(context);
                    mToast.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL, 0, 0);
                    mToast.setDuration(Toast.LENGTH_SHORT);
                    mToast.setView(layout);
                    mToast.show();
                    mToast = null;
                }
            }
        });
    }


}