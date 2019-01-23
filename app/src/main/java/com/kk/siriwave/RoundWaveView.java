package com.kk.siriwave;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

public class RoundWaveView extends View {

    private static final String TAG = "RoundWaveView";

    private static final float LINE_SMOOTHNESS = 0.16f;
    private Path path;
    private Paint paint;

    int pointCount;
    float innerSizeRatio = 0.75f;
    float[] firstRadius;
    float[] secondRadius;   //
    float[] animationOffset;    //
    float[] xs; //
    float[] ys;

    float[] fractions;

    ValueAnimator[] animators;

    float angleOffset;

    float currentCx;
    float currentCy;
    float translationRadius;
    float translationRadiusStep;

    boolean useAnimation;

    public RoundWaveView(Context context) {
        this(context, null);
    }

    public RoundWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundWaveView);
        pointCount = typedArray.getInt(R.styleable.RoundWaveView_pointCount, 6);
        useAnimation = typedArray.getBoolean(R.styleable.RoundWaveView_useAnimation, true);
        typedArray.recycle();
        init(pointCount);

        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColor(Color.MAGENTA);
        paint.setStyle(Paint.Style.FILL);

        float delta = 1.0f / pointCount;
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = delta * i;
        }

        for (int i = 0; i < pointCount; i++) {
            int pos = (int) (Math.random() * pointCount);
            float[] dest = new float[pointCount * 2 + 1];
            int inc = 1;
            for (int j = 0; j < dest.length; j++) {
                dest[j] = fractions[pos];
                pos += inc;
                if (pos < 0 || pos >= fractions.length) {
                    inc = -inc;
                    pos += inc * 2;
                }
            }

            if (i == 0) {
                List<Float> list = new ArrayList<>();
                for (float f : dest) {
                    list.add(f);
                }
                Log.d(TAG, "DEST " + list);
            }

            if (useAnimation) {
                ValueAnimator animator = ValueAnimator.ofFloat(dest).setDuration((long) (4000 + Math.random() * 2000));
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.start();
                if (i == 0) {
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            randomTranslate();
                            invalidate();
                        }
                    });
                }
                animators[i] = animator;
                animator.start();
            }
        }
    }

    private float lastTranslationAngle;
    private void randomTranslate() {
        float r = translationRadiusStep;
        float R = translationRadius;

        float cx = getWidth() / 2;
        float cy = getHeight() / 2;
        float vx = currentCx - cx;
        float vy = currentCy - cy;
        float ratio = 1 - r / R;
        float wx = vx * ratio;
        float wy = vy * ratio;
        lastTranslationAngle = (float) ((Math.random() - 0.5) * Math.PI / 4 + lastTranslationAngle);
        float distRatio = (float) Math.random();

        currentCx = (float) (cx + wx + r * distRatio * Math.cos(lastTranslationAngle));
        currentCy = (float) (cy + wy + r * distRatio * Math.sin(lastTranslationAngle));

    }

    private void init(int pointCount) {
        firstRadius = new float[pointCount];
        secondRadius = new float[pointCount];
        animationOffset = new float[pointCount];
        xs = new float[pointCount];
        ys = new float[pointCount];

        fractions = new float[pointCount + 1];

        animators = new ValueAnimator[pointCount];

        angleOffset = (float) (Math.PI * 2 / pointCount * Math.random());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float radius = Math.min(w, h) / 2;
        float innerRadius = radius * innerSizeRatio;
        float ringWidth = radius - innerRadius;

        for (int i = 0; i < pointCount; i++) {
            firstRadius[i] = (float) (innerRadius + ringWidth * Math.random());
            secondRadius[i] = (float) (innerRadius + ringWidth * Math.random());
            animationOffset[i] = (float) Math.random();
        }

        paint.setShader(new LinearGradient(0, 0, 0, h, Color.RED, Color.BLUE, Shader.TileMode.MIRROR));
        paint.setAlpha((int) (0.2f * 255));

        currentCx = w / 2;
        currentCy = h / 2;
        translationRadius = radius / 6;
        translationRadiusStep = radius / 4000;
    }

    float[] temp = new float[2];
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        paint.setColor(Color.MAGENTA);
//        float cx = getWidth() / 2;
//        float cy = getHeight() / 2;
//        float fraction = (float) valueAnimator.getAnimatedValue();
        for (int i = 0; i < pointCount; i++) {
//            float currentFraction = animationOffset[i] + fraction;
//            if (currentFraction >= 1) {
//                currentFraction = currentFraction - 1;
//            }
            float currentFraction = useAnimation ? (float) animators[i].getAnimatedValue() : 0;
            float radius = firstRadius[i] * (1 - currentFraction) + secondRadius[i] * currentFraction;
            float angle = (float) (Math.PI * 2 / pointCount * i) + angleOffset;
            xs[i] = (float) (currentCx + radius * Math.cos(angle));
            ys[i] = (float) (currentCy + radius * Math.sin(angle));
        }

        path.reset();
        path.moveTo(xs[0], ys[0]);
        for (int i = 0; i < pointCount; i++) {
            float currX = getFromArray(xs, i);
            float currY = getFromArray(ys, i);
            float nextX = getFromArray(xs, i + 1);
            float nextY = getFromArray(ys, i + 1);

            getVector(xs, ys, i, temp);

            float vx = temp[0];
            float vy = temp[1];

            getVector(xs, ys, i + 1, temp);
            float vxNext = temp[0];
            float vyNext = temp[1];

            path.cubicTo(currX + vx, currY + vy, nextX - vxNext, nextY - vyNext, nextX, nextY);
        }

        canvas.drawPath(path, paint);
//        float firstFraction = (float) animators[0].getAnimatedValue();
//        paint.setColor(Color.BLACK);
//        float r = 30;
//        canvas.drawCircle((getWidth() - r * 2) * firstFraction + r, getHeight() / 2, r, paint);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (useAnimation) {
            for (ValueAnimator animator : animators) {
                animator.end();
            }

        }
    }

    static float getFromArray(float[] arr, int pos) {
        return arr[(pos + arr.length) % arr.length];
    }

    static void getVector(float[] xs, float[] ys, int i, float[] out) {
        float nextX = getFromArray(xs, i + 1);
        float nextY = getFromArray(ys, i + 1);
        float prevX = getFromArray(xs, i - 1);
        float prevY = getFromArray(ys, i - 1);

        float vx = (nextX - prevX) * LINE_SMOOTHNESS;
        float vy = (nextY - prevY) * LINE_SMOOTHNESS;

        out[0] = vx;
        out[1] = vy;

    }
}
