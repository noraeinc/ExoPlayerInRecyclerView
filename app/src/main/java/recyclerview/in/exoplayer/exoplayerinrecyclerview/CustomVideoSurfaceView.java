package recyclerview.in.exoplayer.exoplayerinrecyclerview;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

//import com.google.android.exoplayer.ExoPlaybackException;
//import com.google.android.exoplayer.ExoPlayer;
//import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
//import com.google.android.exoplayer.MediaCodecTrackRenderer;
//import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
//import com.google.android.exoplayer.audio.AudioCapabilities;
//import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
//import com.google.android.exoplayer.audio.AudioTrack;
//import com.google.android.exoplayer.extractor.ExtractorSampleSource;
//import com.google.android.exoplayer.upstream.Allocator;
//import com.google.android.exoplayer.upstream.DataSource;
//import com.google.android.exoplayer.upstream.DefaultAllocator;
//import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
//import com.google.android.exoplayer.upstream.DefaultUriDataSource;
//import com.google.android.exoplayer.util.Util;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioCapabilitiesReceiver;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;


import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * リストでの動画再生用にカスタマイズした特殊な{@link android.view.View View}。
 */
public class CustomVideoSurfaceView extends FrameLayout
        implements
        SurfaceHolder.Callback,
        View.OnClickListener {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static CustomVideoSurfaceView instance;

    //fields about playing video
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private SimpleExoPlayer player;
    private static final CookieManager defaultCookieManager;
    private DefaultTrackSelector mDefaultTrackSelector;
    private DataSource.Factory mDataSourceFactory;
    private Handler mainHandler;
    private Allocator allocator;
    private DataSource dataSource;

    private Uri currentUri;
    private Context appContext;

    private int defaultWidth = 0;
    private int videoWidth, videoHeight, viewWidth, viewHeight;
    private float aspect;
    private FrameLayout.LayoutParams layoutParams;
    private SurfaceView videoSurfaceView;


    private boolean surfaceViewViable = false;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }


    public void startPlayer(Uri uri) {

        currentUri = uri;

        player.seekTo(0);

//        // Build the sample source
//        sampleSource =
//                new ExtractorSampleSource(uri, dataSource, allocator, 10 * BUFFER_SEGMENT_SIZE);
//
//        // Build the track renderers
//        videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource,
//                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, -1, mainHandler, this, -1);
//        audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, mainHandler, this);
//
//        // Build the ExoPlayer and start playback
//        player.prepare(videoRenderer, audioRenderer);
//        player.setPlayWhenReady(true);

        String extension = "";
        if(uri != null) {
            String uriPath = uri.getPath();
            extension = uriPath.substring(uriPath.lastIndexOf("."));
        }
        // Produces DataSource instances through which media data is loaded.
        mDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "spotlight"));


        MediaSource videoSource = null;
        if(extension.equals(".m3u8")) {
            videoSource = new HlsMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);;
        } else {
            videoSource = new ExtractorMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);;
        }

        // Prepare the mExoPlayer with the source.
        player.prepare(videoSource);


        playVideo();
    }

    //method to actually do the play
    private void playVideo() {

        if (surfaceViewViable) {
//            player
//            player.sendMessage(videoRenderer,
//                    MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
//                    videoSurfaceView.getHolder().getSurface());
        }

    }

    //release the player
    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    /**
     * release memory
     */
    public void onRelease() {
        releasePlayer();

        audioCapabilitiesReceiver.unregister();

        if (mainHandler != null) {
            mainHandler = null;
        }

        allocator = null;
        dataSource = null;

        instance = null;

    }

    public static CustomVideoSurfaceView getInstance(Context context) {
        if (instance != null) {
            return instance;
        } else {
            instance = new CustomVideoSurfaceView(context);
            return instance;
        }
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    private CustomVideoSurfaceView(Context context) {
        super(context);
        initialize(context);
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    private CustomVideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    /**
     * コンストラクタ。
     * {@inheritDoc}
     */
    private CustomVideoSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public void onRestartPlayer() {
        makePlayer();
        if (currentUri != null) {
            startPlayer(currentUri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        player.setPlayWhenReady(!player.getPlayWhenReady());
    }


    /**
     * 初期化処理を行う。
     */
    protected void initialize(Context context) {
        appContext = context.getApplicationContext();

        setVisibility(INVISIBLE);

        // 画面の中央位置を取得する。
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        defaultWidth = point.x;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.addView(inflater.inflate(R.layout.exoplayer_video_surface_view, null));


        videoSurfaceView = (SurfaceView) this.findViewById(R.id.video_surface_view);
        initializeVideoPlayer();
    }

    private void initializeVideoPlayer() {
        mainHandler = new Handler();



        videoSurfaceView.getHolder().addCallback(this);

        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }


        makePlayer();

    }

    private void makePlayer() {
        if (player != null) {
            return;
        }

        // Measures bandwidth during playback. Can be null if not required.
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);

        mDefaultTrackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(getContext(), mDefaultTrackSelector);
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch(playbackState) {
                    case Player.STATE_BUFFERING:
                        break;
                    case Player.STATE_ENDED:
                        player.seekTo(0);
                        break;
                    case Player.STATE_IDLE:
                        break;
                    case Player.STATE_READY:
                        videoSurfaceView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });


    }

    public void stopPlayer() {
        if (player != null) {
            player.stop();
        }
    }

    protected void calculateAspectRatio(int width, int height) {
        viewWidth = defaultWidth;
        viewHeight = defaultWidth;

        videoWidth = width;
        videoHeight = height;

        aspect = (float) videoWidth / videoHeight;

        layoutParams = (FrameLayout.LayoutParams) getLayoutParams();

        if (((float) viewWidth / videoWidth) > ((float) viewHeight / videoHeight)) {
            layoutParams.width = (int) (viewHeight * aspect + 0.5F);
            layoutParams.height = viewHeight;
        } else {
            layoutParams.width = viewWidth;
            layoutParams.height = (int) (viewWidth / aspect + 0.5F);
        }

        layoutParams.gravity = Gravity.CENTER;


        Log.d("20672067", "calculateAspectRatio:" + layoutParams.width + "--" + layoutParams.height);

        setLayoutParams(layoutParams);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceViewViable = true;
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceViewViable = false;
    }



}
