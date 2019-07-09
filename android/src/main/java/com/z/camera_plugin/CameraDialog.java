package com.z.camera_plugin;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dd.CircularProgressButton;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ui.ImageGridActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class CameraDialog extends BaseDialog implements SurfaceHolder.Callback, View.OnClickListener {
    String TAG = "program";
    private Camera mCamera;
    private int cameraPosition = 0;//0代表前置摄像头,1代表后置摄像头,默认打开前置摄像头
    SurfaceHolder holder;
    SurfaceView mSurfaceView;
    ImageButton openLight;
    View focusIndex;
    View bootomRly;

    private float pointX, pointY;
    static final int FOCUS = 1;            // 聚焦
    static final int ZOOM = 2;            // 缩放
    private int mode;                      //0是聚焦 1是放大
    //放大缩小
    int curZoomValue = 0;
    private float dist;
    Camera.Parameters parameters;
    private Handler handler = new Handler();
    boolean safeToTakePicture = true;
    private String picPath;
    private CircularProgressButton circularProgressButton;
    private ImageView imageView_onActivity;
    boolean button_takePhoto_has_taked=false;
    private ImageView backButton;
    private ImageView lookPictureIv_tuku;
    private Button button_takePhoto;
    private ImageButton button_cameraSwitch;

    View root;

    public CameraDialog(Activity activity) {
        super(activity, R.layout.activity_camera);
    }

    public abstract void onAfter(String str);

    String initedStr = "";

    public CameraDialog setDefult (String str){
        initedStr = str;

        initImagePicker();

        CameraUtil.init(activity);

        initView();

        initData();
        return this;
    }

    @Override
    public void initView(View view) {
        root = view;
    }


    private void initView() {
        imageView_onActivity = (ImageView) root.findViewById(R.id.imageView_onActivity);
        mSurfaceView = (SurfaceView) root.findViewById(R.id.my_surfaceView);
        openLight = (ImageButton) root.findViewById(R.id.openLight);
        focusIndex = root.findViewById(R.id.focus_index);
        bootomRly = root.findViewById(R.id.bootomRly);
        circularProgressButton = (CircularProgressButton) root.findViewById(R.id.circularProgressButton);

        backButton = (ImageView) root.findViewById(R.id.back11);
        lookPictureIv_tuku = (ImageView) root.findViewById(R.id.lookPictureIv);
        button_takePhoto = (Button) root.findViewById(R.id.takePhoto);
        button_cameraSwitch = (ImageButton) root.findViewById(R.id.cameraSwitch);

        backButton.setOnClickListener(this);
        lookPictureIv_tuku.setOnClickListener(this);
        button_takePhoto.setOnClickListener(this);
        openLight.setOnClickListener(this);
        button_cameraSwitch.setOnClickListener(this);
    }

    private void reStartCamera(){
        // 初始化一个surfaceView
        try{
            mSurfaceView=new SurfaceView(activity);
        }catch (Exception e){
            Log.d(TAG, "mSurfaceView e: "+e.toString());
        }
        if (mSurfaceView==null){
            Log.d(TAG, "mSurfaceView null: ");
        }else {
            Log.d(TAG, "mSurfaceView != null: ");
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView_onActivity.getLayoutParams();
        layoutParams.width=FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height=FrameLayout.LayoutParams.MATCH_PARENT;
        //mSurfaceView.setLayoutParams(layoutParams);
        LayoutInflater inflater = LayoutInflater.from(activity);
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.activity_camera, null);

        //this.setContentView(layout);
        //this.addContentView(mSurfaceView,layoutParams);
        //this.addContentView
        initView();
        initData();
        //仿照切换摄像头的原理方法
        releaseCamera();
        cameraPosition = (cameraPosition + 1) % mCamera.getNumberOfCameras();
        mCamera = getCamera(cameraPosition);
        if (holder != null) {
            startPreview(mCamera, holder);
        }

    }
    protected void initData() {
        holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this); // 回调接口

        bootomRly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 主点按下
                    case MotionEvent.ACTION_DOWN:
                        pointX = event.getX();
                        pointY = event.getY();
                        mode = FOCUS;
                        break;
                    // 副点按下
                    case MotionEvent.ACTION_POINTER_DOWN:
                        dist = getDistanceBetwin2Point(event);
                        // 如果连续两点距离大于10，则判定为多点模式
                        if (getDistanceBetwin2Point(event) > 10f) {
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = FOCUS;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == FOCUS) {
                        } else if (mode == ZOOM) {
                            float newDist = getDistanceBetwin2Point(event);
                            if (newDist > 10f) {
                                float tScale = (newDist - dist) / dist;
                                if (tScale < 0) {
                                    tScale = tScale * 10;
                                }
                                addZoomIn((int) tScale);
                            }
                        }
                        break;
                }
                return false;
            }
        });
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pointFocus((int) pointX, (int) pointY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(focusIndex.getLayoutParams());
                layout.setMargins((int) pointX - 60, (int) pointY - 60, 0, 0);
                focusIndex.setLayoutParams(layout);
                focusIndex.setVisibility(View.VISIBLE);
                ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(800);
                focusIndex.startAnimation(sa);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusIndex.setVisibility(View.INVISIBLE);
                    }
                }, 700);
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open(0); //1为前置摄像头，0为后置摄像头
        cameraPosition = 0;
        startPreview(mCamera, holder);
    }
    /* 图像数据处理完成后的回调函数 */
    private Camera.PictureCallback takePhotoCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //将照片改为竖直方向
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        switch (cameraPosition) {
                            case 0:// 大摄像头
                                // todo 手机横屏显示就注释掉
                                //matrix.preRotate(90);
                                break;
                            case 1: // 小摄像头 自拍
                                // todo 手机横屏显示就注释掉
                                //matrix.preRotate(270);
                                break;
                        }
                        //matrix.setScale(0.5f, 0.5f);// 压缩完 将近400 KB
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        //bitmap = Bitmap.createBitmap(bitmap, 0, 0, 480, 640, matrix, true);   // 640 * 480
                        saveImageToStorage(activity, bitmap);
                        Log.d(TAG, "bitmap: " + bitmap.getByteCount()
                                + " bitmap.getWidth=" + bitmap.getWidth() +
                                ",bitmap.getHeight=" + bitmap.getHeight()); // bitmap 3 MB
                        mCamera.stopPreview();
                        //mCamera.startPreview();
                        // TODO post pic here
                        startPic(picPath);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    safeToTakePicture = true;
                }
            }).start();
        }
    };

    public void startPic(String picPath) {
        updataProgressBar(); //进度条转圈
        File file = new File(picPath);
        RequestBody imageBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part imageBodyPart = MultipartBody.Part.createFormData("file", file.getName(), imageBody);

        //哪个公司 company（比如百度、微软公司）  、 哪个服务 service（添加用户、删除用户等）

        AIApi.initByBaseURL(activity, ApiConstant.baseUrl);
        String company = ApiConstant.getCompany(initedStr); // 0,emotion
        String service = ApiConstant.getService(initedStr);

        AIApi.uploadPic(company, service, imageBodyPart)  //   图像识别specialMode =   /ms/emotion
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response != null && response.body() != null) {
                                String res = response.body().string();
                                Log.d(TAG, "发送图片返回结果。。。。。。。: " + res);
                                after(AIResult.getResult(initedStr,res));
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "onResponse e: " + e.toString());
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.toString());
                        after("net fail");
                    }
                });
    }

    void after(String str){
        onAfter(str);
        dismissDialog();
    }

    int updataProgressBar_i = 0;

    public void updataProgressBar() {  //进度条转圈
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressTimer=new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        updataProgressBar_i = updataProgressBar_i + 3;
                        if (updataProgressBar_i >= 99) {
                            updataProgressBar_i = 2;
                        }
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (circularProgressButton.getVisibility() != View.VISIBLE) {
                                    circularProgressButton.setVisibility(View.VISIBLE);
                                }
                                circularProgressButton.setProgress(updataProgressBar_i);
                            }
                        });
                    }

                    ;
                };
                progressTimer.schedule(task, 0, 200);
            }
        });
    }
    Timer progressTimer;
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        startPreview(mCamera, holder);
        autoFocus();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    //将bitmap保存在本地，然后通知图库更新
    public void saveImageToStorage(Context context, Bitmap bmp) {
        // 首先保存图片
        /*File appDir = new File(Environment.getExternalStorageDirectory(), "牛");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);*/
        picPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/DCIM/Camera/" + System.currentTimeMillis() + ".jpg";
        String fileName = System.currentTimeMillis() + ".jpg";
        Log.d(TAG, "saveImageToStorage picPath: " + picPath);
        File file = new File(picPath);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            bmp.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            //TastyToast.makeText(context, context.getResources().getString(R.string.error_image_save), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //TastyToast.makeText(context, context.getResources().getString(R.string.error_image_save), TastyToast.LENGTH_LONG, TastyToast.ERROR);
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
    }

    /**
     * 两点的距离
     */
    private float getDistanceBetwin2Point(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void addZoomIn(int delta) {
        try {
            Camera.Parameters params = mCamera.getParameters();
            Log.d("Camera", "Is support Zoom " + params.isZoomSupported());
            if (!params.isZoomSupported()) {
                return;
            }
            curZoomValue += delta;
            if (curZoomValue < 0) {
                curZoomValue = 0;
            } else if (curZoomValue > params.getMaxZoom()) {
                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {
                params.setZoom(curZoomValue);
                mCamera.setParameters(params);
                return;
            } else {
                mCamera.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定点对焦的代码
    private void pointFocus(int x, int y) {
        mCamera.cancelAutoFocus();
        parameters = mCamera.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showPoint(x, y);
        }
        mCamera.setParameters(parameters);
        autoFocus();
    }

    private void showPoint(int x, int y) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            //xy变换了
            int rectY = -x * 2000 / CameraUtil.screenWidth + 1000;
            int rectX = y * 2000 / CameraUtil.screenHeight - 1000;

            int left = rectX < -900 ? -1000 : rectX - 100;
            int top = rectY < -900 ? -1000 : rectY - 100;
            int right = rectX > 900 ? 1000 : rectX + 100;
            int bottom = rectY > 900 ? 1000 : rectY + 100;
            Rect area1 = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(area1, 800));
            parameters.setMeteringAreas(areas);
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    //实现自动对焦
    private void autoFocus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCamera == null) {
                    return;
                }
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            setupCamera(camera);//实现相机的参数初始化
                        }
                    }
                });
            }
        };
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // Autofocus mode is supported 自动对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        Camera.Size previewSize = CameraUtil.findBestPreviewResolution(camera);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), 1000);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);

        camera.setParameters(parameters);

        int picW = CameraUtil.screenHeight * previewSize.width/previewSize.height;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(previewSize.width, previewSize.height);
        mSurfaceView.setLayoutParams(params);

    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CameraUtil.getInstance().setCameraDisplayOrientation(activity, cameraPosition, camera);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 闪光灯开关   开->关->自动
     *
     * @param mCamera
     */
    private void turnLight(Camera mCamera) {
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        String flashMode = mCamera.getParameters().getFlashMode();
        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();
        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {//关闭状态
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            mCamera.setParameters(parameters);
            openLight.setImageResource(R.drawable.camera_flash_on);
        } else if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {//开启状态
            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                openLight.setImageResource(R.drawable.camera_flash_auto);
                mCamera.setParameters(parameters);
            } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                openLight.setImageResource(R.drawable.camera_flash_off);
                mCamera.setParameters(parameters);
            }
        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)
                && supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            openLight.setImageResource(R.drawable.camera_flash_off);
        }
    }

    /**
     * 获取Camera实例
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
            cameraPosition = id;
        } catch (Exception e) {

        }
        return camera;
    }

    public void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new CustomImageLoader());   //设置图片加载器
        imagePicker.setMultiMode(false);
        imagePicker.setShowCamera(false);  //显示拍照按钮
        imagePicker.setCrop(false);        //允许裁剪（单选才有效）
        imagePicker.setSelectLimit(1);    //选中数量限制
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
//            if (intent != null && requestCode == MCameraView.CAMERA_TO_IMAGEGRID) {
//                ArrayList<ImageItem> images = (ArrayList<ImageItem>) intent.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
//                final String iconPath = images.get(0).path;  //     /storage/emulated/0/Pictures/1506422107969.jpg
//                // picPath = Environment.getExternalStorageDirectory().getAbsolutePath()
//                Log.d(TAG, "CameraActivity.onActivityResult() iconPath: " + iconPath);
//                try {
//                    if (mCamera != null) mCamera.stopPreview();
//                    mSurfaceView.setVisibility(View.INVISIBLE);
//                    Glide.with(CameraActivity.this).load(iconPath).into(imageView_onActivity);
//                    CameraActivity.this.picPath = iconPath;
//                    startPic(iconPath);
//                } catch (Exception e) {
//                    Log.d(TAG, "CameraActivity.onActivityResult Exception: " + e.toString());
//                }
//
//            }
//        }
//    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back11) {
            after("fail");
        } else if (i == R.id.lookPictureIv) {
            Intent intent = new Intent(activity, ImageGridActivity.class);
            activity.startActivityForResult(intent, 110);

        } else if (i == R.id.takePhoto) {
            if (button_takePhoto_has_taked == true) {
                return;
            }
            boolean isOK = true;//ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (isOK == true) {
                if (safeToTakePicture) {
                    safeToTakePicture = false;
                    mCamera.takePicture(null, null, takePhotoCallback);
                }
            } else {
                //ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            button_takePhoto_has_taked = true;

        } else if (i == R.id.openLight) {
            turnLight(mCamera);

        } else if (i == R.id.cameraSwitch) {
            releaseCamera();
            cameraPosition = (cameraPosition + 1) % mCamera.getNumberOfCameras();
            mCamera = getCamera(cameraPosition);
            if (holder != null) {
                startPreview(mCamera, holder);
            }

        }

    }
}
