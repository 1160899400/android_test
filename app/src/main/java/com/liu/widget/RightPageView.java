package com.liu.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.Toast;

import com.liu.constants.TurnPageMode;

import java.io.File;
import java.io.IOException;


/**
 * @author HZLI02
 * @date 2018/8/10
 */

public class RightPageView extends View {
    private Context context;
    private float viewWidth;
    private float viewHeight;

    private int deepColor = 0x55333333;
    private int lightColor = 0x01333333;

    /**
     * 翻页模式：不翻页，从右上右下右中，左上左下左中翻页
     */
    private int turnPageMode = TurnPageMode.MODE_NO_ACTION;

    public int sendMode = TurnPageMode.MODE_NO_ACTION;
    public Bitmap sendBmp;

    public int pageIndex = 2;

    public float postAWidth;
    public float postAHeight;

    /**
     * 翻页成功结果
     */
    private boolean turnPage = false;


    //-1为左上，1为左下
    private int calPointFactor;

    /**
     * 翻页状态(正在翻页)
     */
    private boolean isTurningPage = false;
    private Scroller mScroller1;
    private Scroller mScroller2;
    /**
     * pathA为当前页路径，pathB为下一页，pathC为背页，pathD为上一页
     */
    private Path pathA;

    private Path pathB;
    private Path pathC;
    private Path pathD;

    private float CURVATURE;
    private int mSubWidthStart, mSubWidthEnd, mSubHeightStart, mSubHeightEnd;
    private float mSubMinWidth, mSubMinHeight;
    private static final int SUB_WIDTH = 19, SUB_HEIGHT = 19;
    private float[] mVerts;

    private float edgeAB;
    private float edgeBC;
    private float edgeAK;
    private float edgeKH;

    //绘制文字画笔
    private Paint textPaint;
    private Bitmap bmpCurrentPage;
    private Bitmap bmpNextPage;
    private Bitmap bmpBackPage;
    private Bitmap bmpLastPage;
    private float sin0;
    private float cos0;
    private Matrix mMatrix;

    float lPathAShadowDis = 10.0f;
    float rPathAShadowDis = 10.0f;
    private GradientDrawable shadow1;
    private GradientDrawable shadow2;

    private boolean hasSendMode;

    private MyPoint a, f, g, e, h, c, j, b, k, d, i, m, n, o, p, q, r;
    private Bitmap mBitmap;
    private long maxPageNum;
    private static final String imageBasePath = Environment.getExternalStorageDirectory().getPath() + "/ImageBook/";

    private Handler mHandler;

    private static final String TAG = "RightPageView";

    public RightPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setViewHandler(Handler handler) {
        mHandler = handler;
    }

    /**
     * 对View初始化
     */
    private void init(Context context, AttributeSet attrs) {
        a = new MyPoint();
        a.setXY(-1, -1);
        b = new MyPoint();
        c = new MyPoint();
        d = new MyPoint();
        e = new MyPoint();
        f = new MyPoint();
        g = new MyPoint();
        h = new MyPoint();
        i = new MyPoint();
        j = new MyPoint();
        k = new MyPoint();
        m = new MyPoint();
        n = new MyPoint();
        o = new MyPoint();
        p = new MyPoint();
        q = new MyPoint();
        r = new MyPoint();
        this.context = context;

        hasSendMode = false;
        pathA = new Path();
        pathC = new Path();

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        //设置自像素。如果该项为true，将有助于文本在LCD屏幕上的显示效果。
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(30);
        mVerts = new float[(SUB_WIDTH + 1) * (SUB_HEIGHT + 1) * 2];
        mScroller1 = new Scroller(context, new LinearInterpolator());
        mScroller2 = new Scroller(context, new AccelerateDecelerateInterpolator());
        mMatrix = new Matrix();
        pageIndex = 2;
        maxPageNum = getTotalPages();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = measureSize(getMeasuredHeight(), heightMeasureSpec);
        int width = measureSize(getMeasuredWidth(), widthMeasureSpec);
        setMeasuredDimension(width, height);
        viewWidth = width;
        viewHeight = height;
        mSubMinWidth = viewWidth / (SUB_WIDTH + 1);
        mSubMinHeight = viewHeight / (SUB_HEIGHT + 1);

        Log.d(TAG, "init view width：" + viewWidth + "hegiht:" + viewHeight);

        //在这里初始化A B C页面要展示的内容,A为当前页，B为下一页，C为当前页的背页
        initPageView();
        a.setXY(-1, -1);
    }

    private void initPageView() {
        bmpCurrentPage = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.ARGB_8888);
        bmpNextPage = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.ARGB_8888);
        bmpBackPage = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.ARGB_8888);
        bmpLastPage = Bitmap.createBitmap((int) viewWidth, (int) viewHeight, Bitmap.Config.ARGB_8888);

        try {
            openFirstPage();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 初始化打开第一页
     */

    public void openFirstPage() throws IOException {
        String path = imageBasePath + pageIndex + ".png";
        mBitmap = generatePngBitmap(path);
        setBmpCurrentPage(mBitmap);
        bmpBackPage = getBackBitMap();
        setBmpBackPage(bmpBackPage);
        bmpNextPage = getNextBitMap();
        setBmpNextPage(bmpNextPage);
    }

    /**
     * 设置当前页面的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpCurrentPage(Bitmap bitmap) {
        bmpCurrentPage = bitmap;
    }

    /**
     * 设置下一页面的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpNextPage(Bitmap bitmap) {
        bmpNextPage = bitmap;

    }

    /**
     * 设置当前页面的背页的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpBackPage(Bitmap bitmap) {
        bmpBackPage = bitmap;

    }

    /**
     * 设置上一页面的bitmap内容
     *
     * @param bitmap
     */
    public void setBmpLastPage(Bitmap bitmap) {
        bmpLastPage = bitmap;

    }

    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TurnPageMode.MODE_NO_ACTION == turnPageMode) {
            drawCurPage(canvas, getPathDefault());
        } else if (TurnPageMode.MODE_RIGHT_MIDDLE == turnPageMode) {
            getPathAFromBottomRight();
            getPathC();
            drawCurPage(canvas, pathA);
            drawBackPage(canvas);
            drawShadowHorizontal(canvas);
            drawNextPage(canvas);
            drawPathBShadow(canvas);
        } else if (TurnPageMode.MODE_RIGHT_BOTTOM == turnPageMode) {
            getPathAFromBottomRight();
            getPathC();
            drawCurPage(canvas, pathA);
            drawBackPage(canvas);
            drawShadow(canvas);
            drawNextPage(canvas);
            drawPathBShadow(canvas);
        } else if (TurnPageMode.MODE_LEFT_MIDDLE == turnPageMode || TurnPageMode.MODE_LEFT_BOTTOM == turnPageMode) {
            drawCurPage(canvas, getPathAFromLeft());
            drawLastPage(canvas, getPathD());
            drawShadowRightTurn(canvas);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller1.computeScrollOffset()) {
            float x = mScroller1.getCurrX();
            float y = mScroller1.getCurrY();
            a.setXY(x, y);
            initPointTurnLeft();
            postInvalidate();
            //翻页完成时触发
            if (mScroller1.isFinished()) {
                if (turnPage) {
                    addPage();
                } else {
                    setDefaultPath();
                }

            }

            if (!hasSendMode && turnPage && x < 0) {
                Log.d(TAG, "turn page ef:" + x + "turn page:" + turnPage);
                hasSendMode = true;
                postAHeight = y;
                postAWidth = x;
//                sendTurnCmd(turnPageMode, postAHeight);

            }
        }
        if (mScroller2.computeScrollOffset()) {
            float x = mScroller2.getCurrX();
            float y = mScroller2.getCurrY();
            a.setXY(x, y);
            initPointTurnRight();
            postInvalidate();
            //翻页完成时触发
            if (mScroller2.isFinished()) {
                decPage();
            }
        }
    }


//    private void sendTurnCmd(int loc, float height) {
//        String cmdStr = "[" + "Loc:" + loc + "/Height:" + height + "]";
//        Log.d(TAG, "right screen send turn cmd:" + cmdStr);
//        Message msg = new Message();
//        msg.what = Constands.MSG_SEND_TURN_LEFT_SCREEN_PAGE;
//        msg.obj = cmdStr;
//        mHandler.sendMessage(msg);
//
//    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        if (isTurningPage) {
            return true;
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initStartArea(event.getX(), event.getY());
                case MotionEvent.ACTION_MOVE:
                    touchPoint(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    //判断翻页是否成功
                    if (pageIndex >= maxPageNum) {
                        Toast.makeText(context, "已经是最后一页", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if (turnPageMode != TurnPageMode.MODE_NO_ACTION) {
                        isTurningPage = true;
                        if (turnPage) {
                            Log.d(TAG, "start turn 1111111111111111111");
                            startTurnLeftAnim();
                        } else {
                            cancelTurnLeftAnim();
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }


    /**
     * 判断起始翻页动作在哪一区域
     *
     * @param x
     * @param y
     * @return
     */
    private void initStartArea(float x, float y) {
        //点在右侧
        if (0.85 * viewWidth < x) {
            if (0.8 * viewHeight < y) {
                turnPageMode = TurnPageMode.MODE_RIGHT_BOTTOM;
            } else {
                turnPageMode = TurnPageMode.MODE_RIGHT_MIDDLE;
            }
        } else {
            turnPageMode = TurnPageMode.MODE_NO_ACTION;
        }
    }

    private void touchPoint(float pointX, float pointY) {
        if (pageIndex >= maxPageNum) {
            return;
        }
        switch (turnPageMode) {
            case TurnPageMode.MODE_RIGHT_MIDDLE:
                a.setXY(pointX, viewHeight - 1);
                f.setXY(viewWidth, viewHeight);
                initPointTurnLeft();
                if (a.x < viewWidth * 0.75) {
                    turnPage = true;
                } else {
                    turnPage = false;
                }
                postInvalidate();
                break;
            case TurnPageMode.MODE_RIGHT_BOTTOM:
                a.setXY(pointX, pointY);
                f.setXY(viewWidth, viewHeight);
                initPointTurnLeft();
                if (c.x < 0) {
                    calcPointAByTouchPoint();
                    initPointTurnLeft();
                }
                if (a.x < viewWidth * 0.75) {
                    turnPage = true;
                } else {
                    turnPage = false;
                }
                postInvalidate();
                break;
            case TurnPageMode.MODE_NO_ACTION:
                break;
            default:
                break;
        }


    }

    /**
     * 绘制当前页内容(将bitmap绘入A区域)
     */
    private void drawCurPage(Canvas canvas, Path path) {
        canvas.save();
        //对绘制内容进行裁剪，取和A区域的交集
        canvas.clipPath(path);
        canvas.drawBitmap(bmpCurrentPage, 0, 0, null);
        canvas.restore();
    }

    /**
     * 绘制A区域bitmap的内容
     *
     * @param bitmap
     * @param pathPaint
     */
    private void drawPathAContentBitmap(Bitmap bitmap, Paint pathPaint) {
        Canvas mCanvas = new Canvas(bitmap);
        mCanvas.drawPath(getPathDefault(), pathPaint);

    }

    /**
     * 绘制下一页内容
     */
    private void drawNextPage(Canvas canvas) {
        canvas.save();
        canvas.clipPath(getPathDefault());
        canvas.clipPath(pathA, Region.Op.DIFFERENCE);
        canvas.clipPath(pathC, Region.Op.DIFFERENCE);
        canvas.drawBitmap(bmpNextPage, 0, 0, null);
        canvas.restore();
    }


    private void drawPathBContentBitmap(Bitmap bitmap, Paint pathPaint) {
        Canvas mCanvas = new Canvas(bitmap);
        mCanvas.drawPath(getPathDefault(), pathPaint);
    }

    /**
     * 绘制当前页的背面内容
     */
    private void drawBackPage(Canvas canvas) {
        canvas.save();
        canvas.clipPath(pathC);
        canvas.clipPath(pathA, Region.Op.DIFFERENCE);
        mSubWidthStart = Math.round((edgeAB / mSubMinWidth)) - 2;
        mSubWidthEnd = Math.round(((edgeAB + edgeBC) / mSubMinWidth)) + 2;
        if (h.y < 0) {
            mSubHeightStart = 0;
        } else {
            mSubHeightStart = (int) (h.y / mSubMinHeight) - 2;
        }
        mSubHeightEnd = (int) ((viewHeight - edgeAK) / mSubMinHeight) + 2;
        int index = 0;
        // 长边偏移
        float offsetLong = edgeKH;
        // 长边偏移递减
        float mulOffsetLong = 0.8F;
        // 短边偏移
        float offsetShort = edgeBC;
        // 短边偏移倍增
        for (int y = 0; y <= SUB_HEIGHT; y++) {
            float fy = viewHeight * y / SUB_HEIGHT;
            for (int x = 0; x <= SUB_WIDTH; x++) {
                float fx = viewWidth * x / SUB_WIDTH;
                if (x == 0) {
                    if (y >= mSubHeightStart && y <= mSubHeightEnd) {
                        offsetLong = offsetLong * mulOffsetLong;
                        fx = viewWidth * x / SUB_WIDTH - offsetLong;
                    }
                }
                if (y == SUB_HEIGHT) {
                    if (x >= mSubWidthStart && x <= mSubWidthEnd) {
                        fy = viewHeight * y / SUB_HEIGHT + offsetShort;
                    }
                }
                mVerts[index * 2] = fx;
                mVerts[index * 2 + 1] = fy;
                index += 1;
            }
        }
        float eh = (float) Math.hypot(f.x - e.x, h.y - f.y);
        float cos0 = (h.y - f.y) / eh;
        float angel = Double.valueOf(Math.toDegrees(Math.acos(cos0))).floatValue();
        //设置翻转和旋转矩阵
        mMatrix.reset();
        canvas.translate(a.x - 0, a.y - f.y);
        canvas.rotate(-2 * angel, 0, f.y);
        canvas.drawBitmapMesh(bmpBackPage, SUB_WIDTH, SUB_HEIGHT, mVerts, 0, null, 0, null);
        canvas.restore();
    }


    private void drawLastPage(Canvas canvas, Path path) {
        canvas.save();
        canvas.clipPath(path);
        mMatrix.reset();
        float angel = new Double(Math.toDegrees(Math.asin(sin0))).floatValue();
        mMatrix.setRotate(angel);
        if (calPointFactor == 1) {
            mMatrix.postTranslate(n.x, n.y);
        } else {
            mMatrix.postTranslate(o.x, o.y);
        }
        canvas.drawBitmap(bmpLastPage, mMatrix, null);
        canvas.restore();
    }


    /**
     * 回到默认状态
     */
    private void setDefaultPath() {
        a.setXY(-1, -1);
        turnPageMode = TurnPageMode.MODE_NO_ACTION;
        isTurningPage = false;
        turnPage = false;
        hasSendMode = false;
        postInvalidate();
    }

    private void addPage() {

        a.setXY(-1, -1);
        turnPageMode = TurnPageMode.MODE_NO_ACTION;
        isTurningPage = false;
        turnPage = false;
        hasSendMode = false;


        pageIndex = pageIndex + 2;
        Log.d(TAG, "page index " + pageIndex + "&&&&&&&&&&&&&");
        bmpLastPage = getLastBitMap();
        setBmpLastPage(bmpLastPage);

        if ((pageIndex + 1) < maxPageNum) {
            bmpBackPage = getBackBitMap();
            setBmpBackPage(bmpBackPage);
        }

        bmpCurrentPage = getCurBitMap();
        setBmpCurrentPage(bmpCurrentPage);


        if ((pageIndex + 2) <= maxPageNum) {
            bmpNextPage = getNextBitMap();
            setBmpNextPage(bmpNextPage);
        }

        setDefaultPath();

    }


    private void decPage() {
        a.setXY(-1, -1);
        turnPageMode = TurnPageMode.MODE_NO_ACTION;
        isTurningPage = false;
        turnPage = false;
        pageIndex = pageIndex - 2;

        bmpCurrentPage = getCurBitMap();
        setBmpCurrentPage(mBitmap);

        if (pageIndex - 2 >= 1) {
            bmpLastPage = getLastBitMap();
            setBmpLastPage(bmpLastPage);
        }

        if ((pageIndex + 1) < maxPageNum) {
            bmpBackPage = getBackBitMap();
            setBmpBackPage(bmpBackPage);
        }

        if ((pageIndex + 2) < maxPageNum) {
            bmpNextPage = getNextBitMap();
            setBmpNextPage(bmpNextPage);
        }

        setDefaultPath();

    }

    private Bitmap getNextBitMap() {
        String path = imageBasePath + (pageIndex + 2) + ".png";
        mBitmap = generatePngBitmap(path);
        return mBitmap;
    }

    private Bitmap getCurBitMap() {
        String path = imageBasePath + pageIndex + ".png";
        mBitmap = generatePngBitmap(path);
        return mBitmap;
    }

    private Bitmap getBackBitMap() {

        String path = imageBasePath + (pageIndex + 1) + ".png";
        mBitmap = generatePngBitmap(path);
        return mBitmap;
    }

    private Bitmap getLastBitMap() {
        String path = imageBasePath + (pageIndex - 2) + ".png";
        mBitmap = generatePngBitmap(path);
        return mBitmap;
    }

    /**
     * 默认的整页区域
     *
     * @return
     */
    private Path getPathDefault() {
        Path path = new Path();
        path.reset();
        path.lineTo(0, viewHeight);
        path.lineTo(viewWidth, viewHeight);
        path.lineTo(viewWidth, 0);
        path.close();
        return path;
    }

    /**
     * 当翻起区域在右上角时的path绘制
     *
     * @return
     */
    private void getPathAFromTopRight() {
        pathA.reset();
        pathA.lineTo(c.x, c.y);
        pathA.quadTo(e.x, e.y, b.x, b.y);
        pathA.lineTo(a.x, a.y);
        pathA.lineTo(k.x, k.y);
        pathA.quadTo(h.x, h.y, j.x, j.y);
        pathA.lineTo(viewWidth, viewHeight);
        pathA.lineTo(0, viewHeight);
        pathA.close();
    }

    /**
     * 当翻起区域在右下角时的path绘制
     *
     * @return
     */
    private void getPathAFromBottomRight() {
        pathA.reset();
        pathA.lineTo(0, viewHeight);
        pathA.lineTo(c.x, c.y);
        pathA.quadTo(e.x, e.y, b.x, b.y);
        pathA.lineTo(a.x, a.y);
        pathA.lineTo(k.x, k.y);
        pathA.quadTo(h.x, h.y, j.x, j.y);
        pathA.lineTo(viewWidth, 0);
        pathA.close();//闭合区域

    }


    private void getPathC() {
        pathC.reset();
//        pathC.moveTo(c.x, c.y);
//        pathC.quadTo(e.x, e.y, b.x, b.y);
//        pathC.lineTo(a.x, a.y);
//        pathC.lineTo(k.x, k.y);
//        pathC.quadTo(h.x, h.y, j.x, j.y);
        pathC.moveTo(a.x, a.y);
        pathC.lineTo(b.x, b.y);
        pathC.lineTo(d.x, d.y);
        pathC.lineTo(i.x, i.y);
        pathC.lineTo(k.x, k.y);
        pathC.close();

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private Path getPathAFromLeft() {
        Path path = getPathDefault();
        path.op(getPathD(), Path.Op.DIFFERENCE);
        return path;
    }


    /**
     * 上一页区域
     */
    private Path getPathD() {
        Path path = new Path();
//        path.moveTo(a.x, a.y);
//        path.quadTo(m.x, m.y, q.x, q.y);
//        path.lineTo(n.x, n.y);
//        path.lineTo(p.x, p.y);
//        path.quadTo(o.x, o.y, a.x, a.y);

        path.moveTo(m.x, m.y);
        path.lineTo(a.x, a.y);
//        path.lineTo(p.x, p.y);
//        path.quadTo(r.x, r.y, 0, f.y);
//        path.lineTo(r.x, r.y);
        path.lineTo(o.x, o.y);
        path.lineTo(n.x, n.y);
        path.close();
        return path;
    }


    /**
     * 初始化折起区域的关键点坐标
     */
    private void initPointTurnLeft() {
        //g为a,f中点
        g.setXY((a.x + f.x) / 2, (a.y + f.y) / 2);
        //eh与af垂直，e为与f水平的点，h为与f垂直的点
        e.setXY(g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x), f.y);
        h.setXY(f.x, g.y - (f.x - g.x) * (f.x - g.x) / (f.y - g.y));
        c.setXY(e.x - (f.x - e.x) / 2, f.y);
        j.setXY(f.x, h.y - (f.y - h.y) / 2);
        b = MyPointUtils.getIntersectionPoint(a, e, c, j);
        k = MyPointUtils.getIntersectionPoint(a, h, c, j);
        d.setXY((c.x + 2 * e.x + b.x) / 4, (2 * e.y + c.y + b.y) / 4);
        i.setXY((j.x + 2 * h.x + k.x) / 4, (2 * h.y + j.y + k.y) / 4);
        edgeAB = (float) MyPointUtils.getLength(a, b);
        edgeBC = (float) MyPointUtils.getLength(b, c);
        edgeAK = (float) MyPointUtils.getLength(a, k);
        edgeKH = (float) MyPointUtils.getLength(k, h);
        //计算d点到ae的距离
        float lA = a.y - e.y;
        float lB = e.x - a.x;
        float lC = a.x * e.y - e.x * a.y;
        lPathAShadowDis = Math.abs((lA * d.x + lB * d.y + lC) / (float) Math.hypot(lA, lB));
        //计算i点到ah的距离
        float rA = a.y - h.y;
        float rB = h.x - a.x;
        float rC = a.x * h.y - h.x * a.y;
        rPathAShadowDis = Math.abs((rA * i.x + rB * i.y + rC) / (float) Math.hypot(rA, rB));
    }

    private void initPointTurnRight() {
        //g为a,f中点
        g.setXY((a.x + f.x) / 2, (a.y + f.y) / 2);
        r.setXY(g.x - (f.y - g.y) * (f.y - g.y) / (f.x - g.x), f.y);
//        q.setXY(r.x + a.x / 10, f.y);
//        p.setXY(2 * r.x / 3 + 1 * a.x / 3, 2 * r.y / 3 + 1 * a.y / 3);
        float eh = (float) Math.hypot(a.x - r.x, a.y - r.y);
        sin0 = (a.y - r.y) / eh;
        cos0 = (a.x - r.x) / eh;
        m.setXY(a.x + calPointFactor * viewHeight * sin0, a.y - calPointFactor * viewHeight * cos0);
        o.setXY(a.x - viewWidth * cos0, a.y - viewWidth * sin0);
        n.setXY(m.x - a.x + o.x, m.y - a.y + o.y);

//        Log.i("###", "o.x:  " + o.x + "  o.y:  " + o.y);
//        p.setXY(o.x - calPointFactor * (viewWidth - a.x) / 10 * sin0, o.y + calPointFactor * (viewWidth - a.x) / 10 * cos0);
//        q.setXY(m.x + (viewWidth - a.x) / 6 * cos0, m.y + (viewWidth - a.x) / 6 * sin0);
//        Log.i("###", "p.x:  " + p.x + "  p.y:  " + p.y);
//        Log.i("###", "q.x:  " + q.x + "  q.y:  " + q.y);
    }


    /**
     * 如果c点x坐标小于0,根据触摸点重新测量a点坐标
     */
    private void calcPointAByTouchPoint() {
        float w0 = viewWidth - c.x;
        float w1 = Math.abs(f.x - a.x);
        float w2 = viewWidth * w1 / w0 - viewWidth / 50;
        a.x = Math.abs(f.x - w2);
        float h1 = Math.abs(f.y - a.y);
        float h2 = w2 * h1 / w1;
        a.y = Math.abs(f.y - h2);
    }


    /**
     * 向右翻页触发
     */
    public void turnRight(int MODE, float height) {
        a.setXY(postAWidth, height);
        turnPageMode = MODE;
        startTurnRightAnim();
    }

    private void startTurnRightAnim() {
        isTurningPage = true;
        int dx = 0, dy = 0;
        switch (turnPageMode) {
            case TurnPageMode.MODE_LEFT_MIDDLE:
            case TurnPageMode.MODE_LEFT_BOTTOM:
                dx = (int) viewWidth - (int) a.x;
                dy = (int) viewHeight - (int) a.y;
                f.setXY(-1 * viewWidth, viewHeight);
                calPointFactor = 1;
                break;
            default:
                break;
        }
        initPointTurnRight();
        postInvalidate();
        mScroller2.startScroll((int) a.x, (int) a.y, dx, dy, 1200);
        invalidate();
    }

    private void startTurnLeftAnim() {
        isTurningPage = true;
        int dx, dy;
        switch (turnPageMode) {
            case TurnPageMode.MODE_RIGHT_MIDDLE:
            case TurnPageMode.MODE_RIGHT_BOTTOM:
                dx = (int) (-1 - viewWidth - a.x);
                dy = (int) (viewHeight - 1 - a.y);
                break;
            default:
                dx = 0;
                dy = 0;
                break;
        }
        mScroller1 = new Scroller(context, new AccelerateDecelerateInterpolator());
        sendMode = turnPageMode;
        sendBmp = bmpBackPage;
        mScroller1.startScroll((int) a.x, (int) a.y, dx, dy, 1200);
        invalidate();
    }

    private void cancelTurnLeftAnim() {
        isTurningPage = true;
        int dx, dy;
        //让a滑动到f点所在位置，留出1像素是为了防止当a和f重叠时出现View闪烁的情况
        if (turnPageMode == TurnPageMode.MODE_RIGHT_BOTTOM || turnPageMode == TurnPageMode.MODE_RIGHT_MIDDLE) {
            dx = (int) (viewWidth - 1 - a.x);
            dy = (int) (viewHeight - 1 - a.y);
        } else {
            dx = 0;
            dy = 0;
        }
        mScroller1.startScroll((int) a.x, (int) a.y, dx, dy, 1200);
        invalidate();
    }

    private void drawShadow(Canvas canvas) {
        float viewDiagonalLength = (float) Math.hypot(viewWidth, viewHeight);
        Path mPath = new Path();
        //渐变颜色数组
        int[] gradientColor1 = {lightColor, deepColor};
        int[] gradientColor2 = {deepColor, lightColor, lightColor};
        shadow1 = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, gradientColor1);
        shadow1.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadow1.setBounds((int) (e.x), (int) e.y, (int) (e.x + lPathAShadowDis / 2), (int) (e.y + viewHeight));
        shadow2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColor2);
        shadow2.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadow2.setBounds((int) h.x, (int) h.y, (int) (h.x + viewDiagonalLength * 10), (int) (h.y + rPathAShadowDis / 2));
        canvas.save();
        mPath.moveTo(a.x - Math.max(rPathAShadowDis, lPathAShadowDis) / 2, a.y);
        mPath.lineTo(d.x, d.y);
        mPath.lineTo(e.x, e.y);
        mPath.lineTo(a.x, a.y);
        mPath.close();
        canvas.clipPath(pathA);
        canvas.clipPath(mPath, Region.Op.INTERSECT);
        canvas.rotate((float) Math.toDegrees(Math.atan2(e.x - a.x, a.y - e.y)), e.x, e.y);
        shadow1.draw(canvas);
        canvas.restore();
        canvas.save();
        mPath.reset();
        mPath.moveTo(a.x - Math.max(rPathAShadowDis, lPathAShadowDis) / 2, a.y);
        mPath.lineTo(h.x, h.y);
        mPath.lineTo(a.x, a.y);
        mPath.close();
        canvas.clipPath(pathA);
        canvas.clipPath(mPath, Region.Op.INTERSECT);
        canvas.rotate((float) Math.toDegrees(Math.atan2(a.y - h.y, a.x - h.x)), h.x, h.y);
        shadow2.draw(canvas);
        canvas.restore();
    }

    private void drawShadowHorizontal(Canvas canvas) {
        canvas.save();
        int[] gradientColors = {lightColor, deepColor};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        //阴影矩形最大的宽度
        int maxShadowWidth = 30;
        gradientDrawable.setBounds((int) (a.x - Math.min(maxShadowWidth, (rPathAShadowDis / 2))), 0, (int) (a.x), (int) viewHeight);
        canvas.clipPath(pathA, Region.Op.INTERSECT);
        float mDegrees = (float) Math.toDegrees(Math.atan2(f.x - a.x, f.y - h.y));
        canvas.rotate(mDegrees, a.x, a.y);
        gradientDrawable.draw(canvas);
        canvas.restore();
    }

    private void drawShadowRightTurn(Canvas canvas) {
        lPathAShadowDis = (float) Math.hypot(viewWidth - a.x, a.y) / 10;
        rPathAShadowDis = (float) Math.hypot(viewWidth - a.x, a.y - viewHeight) / 10;
        canvas.save();
        int[] gradientColor = {0x30333333, deepColor, lightColor};
        GradientDrawable shadow1, shadow2;
        Path path1, path2;
        float mDegree1, mDegree2;
        shadow1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColor);
        shadow1.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadow1.setBounds((int) (a.x), (int) (a.y - viewHeight), (int) (a.x + rPathAShadowDis / 2), (int) (a.y + rPathAShadowDis * 20));
        shadow2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColor);
        shadow2.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        shadow2.setBounds((int) a.x, (int) (a.y - lPathAShadowDis * 20), (int) (a.x + lPathAShadowDis / 2), (int) (a.y + viewWidth));
        mDegree1 = -(float) Math.toDegrees(Math.acos(cos0));
        mDegree2 = mDegree1 + 90;
        path1 = new Path();
        path1.moveTo(a.x, a.y);
        path1.lineTo(a.x + Math.max(lPathAShadowDis, rPathAShadowDis) / 2, a.y);
        path1.lineTo(m.x, m.y);
        path1.close();
        path2 = new Path();
        path2.moveTo(a.x, a.y);
        path2.lineTo(a.x + Math.max(lPathAShadowDis, rPathAShadowDis) / 2, a.y);
        path2.lineTo(o.x, o.y);
        path2.close();
        canvas.clipPath(path1, Region.Op.INTERSECT);
        canvas.rotate(mDegree1, a.x, a.y);
        shadow1.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.clipPath(path2, Region.Op.INTERSECT);
        canvas.rotate(mDegree2, a.x, a.y);
        shadow2.draw(canvas);
        canvas.restore();
    }


    private void drawPathBShadow(Canvas canvas) {
        canvas.save();
        canvas.clipPath(pathA);
        canvas.clipPath(pathC, Region.Op.UNION);
        canvas.clipPath(getPathDefault(), Region.Op.REVERSE_DIFFERENCE);
        int[] gradientColors = new int[]{deepColor, lightColor};
        int deepOffset = 0;
        int lightOffset = 0;
        float aTof = (float) Math.hypot((a.x - f.x), (a.y - f.y));
        float viewDiagonalLength = (float) Math.hypot(viewWidth, viewHeight);
        int left;
        int right;
        int top = (int) c.y;
        int bottom = (int) (viewDiagonalLength + c.y);
        GradientDrawable gradientDrawable;
        //从右向左线性渐变
        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, gradientColors);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        left = (int) (c.x - aTof / 4 - lightOffset);
        right = (int) (c.x + deepOffset);

        gradientDrawable.setBounds(left, top, right, bottom);
        float rotateDegrees = (float) Math.toDegrees(Math.atan2(e.x - f.x, h.y - f.y));
        canvas.rotate(rotateDegrees, c.x, c.y);
        gradientDrawable.draw(canvas);
        canvas.restore();
    }

    private Bitmap generatePngBitmap(String filePath) {
        BitmapFactory.Options config = new BitmapFactory.Options();
        config.inScaled = true;
        config.inDensity = 480;
        config.inTargetDensity = 480;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, config).copy(Bitmap.Config.ARGB_8888, true);

        return bitmap;
    }

    private long getTotalPages() {
//        int fileCount = 0;
//        File file = new File(imageBasePath);
//        if (!file.exists()){
//            Log.e(TAG,"cant find ImageBook directory");
//        }else {
//            Log.i(TAG,"find ImageBook directory ");
//        }
//        File[] list = file.listFiles();
//        for (int i = 0; i < list.length; i++) {
//            if (list[i].isFile()) {
//                fileCount++;
//            }
//        }
//
//        return fileCount;
        return 8;
    }

    /**
     * @param locValue
     * @param heightValue process left screen send turn cmd
     */
    public void handleTurnCmd(int locValue, float heightValue) {

    }
}
