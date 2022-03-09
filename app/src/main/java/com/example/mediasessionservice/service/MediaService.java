package com.example.mediasessionservice.service;

import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiayiye5
 * @date 2021/11/30 15:46
 */
public class MediaService extends MediaBrowserService {

    /**
     * 媒体会话，受控端
     */
    private MediaSession mediaSession;
    private static final String TAG = "打印消息：MediaService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        //初始化，第一个参数为context，第二个参数为String类型tag，这里就设置为类名了
        mediaSession = new MediaSession(this, "MediaService");
        //设置token
        setSessionToken(mediaSession.getSessionToken());
        //设置callback，这里的callback就是客户端对服务指令到达处
        mediaSession.setCallback(mCallback);
    }

    //mediaSession设置的callback，也是客户端控制指令所到达处
    private final MediaSession.Callback mCallback = new MediaSession.Callback() {
        //重写的方法都是选择性重写的，不完全列列举，具体可以查询文章末尾表格
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
        @Override
        public void onPlay() {
            super.onPlay();
            Log.d(TAG, "onPlay: ");
            //客户端mMediaController.getTransportControls().play()就会调用到这里，以下类推
            //处理播放逻辑
            //处理完成后通知客户端更新，这里就会回调给客户端的MediaController.Callback
            Bundle bundle = new Bundle();
            bundle.putString("bundle_key","10010");
            PlaybackState playbackState = new PlaybackState.Builder()
                    .setActions(PlaybackState.ACTION_PLAY)
                    .setState(PlaybackState.STATE_PLAYING, 10086L, 1.0F, 119L)
                    .setExtras(bundle)
                    .build();
            mediaSession.setPlaybackState(playbackState);
        }

        @Override
        public void onPause() {
            super.onPause();
            //暂停

        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            //下一首
            //通知媒体信息改变
            MediaMetadata mediaMetadata = new MediaMetadata.Builder().build();
            mediaSession.setMetadata(mediaMetadata);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            //自定义指令发送到的地方
            //对应客户端 mMediaController.getTransportControls().sendCustomAction(...)
            if ("自定义action".equals(action)) {
                String name = extras.getString("name");
                Log.d(TAG, "onCustomAction===" + name);
            }
        }


    };

    //自己写的方法，用于改变播放列表
    private void changePlayList() {
        //通知播放队列改变
//        mediaSession.setQueue(queueItems);
    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        //MediaBrowserService必须重写的方法，第一个参数为客户端的packageName，第二个参数为Uid
        //第三个参数是从客户端传递过来的Bundle。
        //通过以上参数来进行判断，若同意连接，则返回BrowserRoot对象，否则返回null;

        //构造BrowserRoot的第一个参数为rootId(自定义)，第二个参数为Bundle;
        int bundleId = rootHints.getInt("bundleID");
        String bundleName = rootHints.getString("bundleName");
        String bundleTitle = rootHints.getString("bundleTitle");
        Log.d(TAG, "onGetRoot====" + bundleId + "==" + bundleTitle + "==" + bundleName);
        return new BrowserRoot("MyMedia", rootHints);
    }

    @Override
    public void onLoadChildren(String parentId, MediaBrowserService.Result<List<MediaBrowser.MediaItem>> result) {
        //MediaBrowserService必须重写的方法，用于处理订阅信息，文章后面会提及

        //使用result之前,一定需要detach();
        result.detach();
        //新建MediaItem数组
        ArrayList<MediaBrowser.MediaItem> mediaItems = new ArrayList<>();

        //根据parentId,获取不同的媒体列表
        if ("123".equals(parentId)) {
            Toast.makeText(this, "订阅成功", Toast.LENGTH_LONG).show();
            MediaMetadata metadata = new MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "101")
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "一首歌")
                    .putString(MediaMetadata.METADATA_KEY_DATE, "2021.12.2")
                    .build();

            MediaDescription.Builder bob = new MediaDescription.Builder();
            Bundle bundle = new Bundle();
            bundle.putString("access_token","123456");
            bundle.putString("refresh_token","11111");
            bundle.putString("date_token","3333");
            bob.setExtras(bundle);
            bob.setMediaId("101");

            mediaItems.add(new MediaBrowser.MediaItem(bob.build(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
//            mediaItems.add(new MediaBrowser.MediaItem(metadata.getDescription(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }
        //发送数据
        result.sendResult(mediaItems);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("打印状态","onDestroy");
    }
}