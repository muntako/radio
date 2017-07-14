//package com.muntako.radio.decoration;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.os.Bundle;
//import android.util.AttributeSet;
//import android.view.View;
//
////import com.github.zagum.speechrecognitionview.RecognitionBar;
////import com.github.zagum.speechrecognitionview.animators.BarParamsAnimator;
////import com.github.zagum.speechrecognitionview.animators.IdleAnimator;
////import com.github.zagum.speechrecognitionview.animators.RmsAnimator;
////import com.github.zagum.speechrecognitionview.animators.RotatingAnimator;
////import com.github.zagum.speechrecognitionview.animators.TransformAnimator;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import edu.cmu.pocketsphinx.Hypothesis;
//import edu.cmu.pocketsphinx.RecognitionListener;
//import edu.cmu.pocketsphinx.SpeechRecognizer;
//
///**
// * Created by ADMIN on 01-Jun-17.
// */
//
//public class RecognitionProgressView extends View implements RecognitionListener {
//
//    public static final int BARS_COUNT = 5;
//    private static final int CIRCLE_RADIUS_DP = 5;
//    private static final int CIRCLE_SPACING_DP = 11;
//    private static final int ROTATION_RADIUS_DP = 25;
//    private static final int IDLE_FLOATING_AMPLITUDE_DP = 3;
//    private static final int[] DEFAULT_BARS_HEIGHT_DP = new int[]{60, 46, 70, 54, 64};
//    private static final float MDPI_DENSITY = 1.5F;
//    private final List<RecognitionBar> recognitionBars = new ArrayList();
//    private Paint paint;
//    private BarParamsAnimator animator;
//    private int radius;
//    private int spacing;
//    private int rotationRadius;
//    private int amplitude;
//    private float density;
//    private boolean isSpeaking;
//    private boolean animating;
//    private SpeechRecognizer speechRecognizer;
//    private RecognitionListener recognitionListener;
//    private int barColor = -1;
//    private int[] barColors;
//    private int[] barMaxHeights;
//
//    public RecognitionProgressView(Context context) {
//        super(context);
//    }
//    public RecognitionProgressView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.init();
//    }
//
//    public RecognitionProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.init();
//    }
//
//    public void setSpeechRecognizer(SpeechRecognizer recognizer) {
//        this.speechRecognizer = recognizer;
//        this.speechRecognizer.addListener(this);
//    }
//
//    public void setRecognitionListener(RecognitionListener listener) {
//        this.recognitionListener = listener;
//    }
//
//    public void play() {
//        this.startIdleInterpolation();
//        this.animating = true;
//    }
//
//    public void stop() {
//        if(this.animator != null) {
//            this.animator.stop();
//            this.animator = null;
//        }
//
//        this.animating = false;
//        this.resetBars();
//    }
//
//    public void setSingleColor(int color) {
//        this.barColor = color;
//    }
//
//    public void setColors(int[] colors) {
//        if(colors != null) {
//            this.barColors = new int[5];
//            if(colors.length < 5) {
//                System.arraycopy(colors, 0, this.barColors, 0, colors.length);
//
//                for(int i = colors.length; i < 5; ++i) {
//                    this.barColors[i] = colors[0];
//                }
//            } else {
//                System.arraycopy(colors, 0, this.barColors, 0, 5);
//            }
//
//        }
//    }
//
//    public void setBarMaxHeightsInDp(int[] heights) {
//        if(heights != null) {
//            this.barMaxHeights = new int[5];
//            if(heights.length < 5) {
//                System.arraycopy(heights, 0, this.barMaxHeights, 0, heights.length);
//
//                for(int i = heights.length; i < 5; ++i) {
//                    this.barMaxHeights[i] = heights[0];
//                }
//            } else {
//                System.arraycopy(heights, 0, this.barMaxHeights, 0, 5);
//            }
//
//        }
//    }
//
//    private void init() {
//        this.paint = new Paint();
//        this.paint.setFlags(1);
//        this.paint.setColor(-7829368);
//        this.density = this.getResources().getDisplayMetrics().density;
//        this.radius = (int)(5.0F * this.density);
//        this.spacing = (int)(11.0F * this.density);
//        this.rotationRadius = (int)(25.0F * this.density);
//        this.amplitude = (int)(3.0F * this.density);
//        if(this.density <= 1.5F) {
//            this.amplitude *= 2;
//        }
//
//    }
//
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if(this.recognitionBars.isEmpty()) {
//            this.initBars();
//        }
//
//    }
//
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        if(!this.recognitionBars.isEmpty()) {
//            if(this.animating) {
//                this.animator.animate();
//            }
//
//            for(int i = 0; i < this.recognitionBars.size(); ++i) {
//                RecognitionBar bar = (RecognitionBar)this.recognitionBars.get(i);
//                if(this.barColors != null) {
//                    this.paint.setColor(this.barColors[i]);
//                } else if(this.barColor != -1) {
//                    this.paint.setColor(this.barColor);
//                }
//
//                canvas.drawRoundRect(bar.getRect(), (float)this.radius, (float)this.radius, this.paint);
//            }
//
//            if(this.animating) {
//                this.invalidate();
//            }
//
//        }
//    }
//
//    private void initBars() {
//        List heights = this.initBarHeights();
//        int firstCirclePosition = this.getMeasuredWidth() / 2 - 2 * this.spacing - 4 * this.radius;
//
//        for(int i = 0; i < 5; ++i) {
//            int x = firstCirclePosition + (2 * this.radius + this.spacing) * i;
//            RecognitionBar bar = new RecognitionBar(x, this.getMeasuredHeight() / 2, 2 * this.radius, ((Integer)heights.get(i)).intValue(), this.radius);
//            this.recognitionBars.add(bar);
//        }
//
//    }
//
//    private List<Integer> initBarHeights() {
//        ArrayList barHeights = new ArrayList();
//        int i;
//        if(this.barMaxHeights == null) {
//            for(i = 0; i < 5; ++i) {
//                barHeights.add(Integer.valueOf((int)((float)DEFAULT_BARS_HEIGHT_DP[i] * this.density)));
//            }
//        } else {
//            for(i = 0; i < 5; ++i) {
//                barHeights.add(Integer.valueOf((int)((float)this.barMaxHeights[i] * this.density)));
//            }
//        }
//
//        return barHeights;
//    }
//
//    private void resetBars() {
//        Iterator i$ = this.recognitionBars.iterator();
//
//        while(i$.hasNext()) {
//            RecognitionBar bar = (RecognitionBar)i$.next();
//            bar.setX(bar.getStartX());
//            bar.setY(bar.getStartY());
//            bar.setHeight(this.radius * 2);
//            bar.update();
//        }
//
//    }
//
//    private void startIdleInterpolation() {
//        this.animator = new IdleAnimator(this.recognitionBars, this.amplitude);
//        this.animator.start();
//    }
//
//    private void startRmsInterpolation() {
//        this.resetBars();
//        this.animator = new RmsAnimator(this.recognitionBars);
//        this.animator.start();
//    }
//
//    private void startTransformInterpolation() {
//        this.resetBars();
//        this.animator = new TransformAnimator(this.recognitionBars, this.getWidth() / 2, this.getHeight() / 2, this.rotationRadius);
//        this.animator.start();
//        ((TransformAnimator)this.animator).setOnInterpolationFinishedListener(new TransformAnimator.OnInterpolationFinishedListener() {
//            public void onFinished() {
////                com.github.zagum.speechrecognitionview.RecognitionProgressView.this.startRotateInterpolation();
//            }
//        });
//    }
//
//    private void startRotateInterpolation() {
//        this.animator = new RotatingAnimator(this.recognitionBars, this.getWidth() / 2, this.getHeight() / 2);
//        this.animator.start();
//    }
//
////    public void onReadyForSpeech(Bundle params) {
////        if(this.recognitionListener != null) {
////            this.recognitionListener.onReadyForSpeech(params);
////        }
////
////    }
//
//    public void onBeginningOfSpeech() {
//        if(this.recognitionListener != null) {
//            this.recognitionListener.onBeginningOfSpeech();
//        }
//
//        this.isSpeaking = true;
//    }
//
//    public void onEndOfSpeech() {
//        if(this.recognitionListener != null) {
//            this.recognitionListener.onEndOfSpeech();
//        }
//
//        this.isSpeaking = false;
//        this.startTransformInterpolation();
//    }
//
////    public void onError(int error) {
////        if(this.recognitionListener != null) {
////            this.recognitionListener.onError(error);
////        }
////
////    }
//
//    @Override
//    public void onPartialResult(Hypothesis hypothesis) {
//        if(this.recognitionListener != null) {
//            this.recognitionListener.onPartialResult(hypothesis);
//        }
//    }
//
//    @Override
//    public void onResult(Hypothesis hypothesis) {
//        if(this.recognitionListener != null) {
//            this.recognitionListener.onResult(hypothesis);
//        }
//    }
//
//    @Override
//    public void onError(Exception e) {
//
//    }
//
//    @Override
//    public void onTimeout() {
//
//    }
//}
