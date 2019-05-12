package com.lanshifu.opengldemo.camera.camera2_surface_demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2BaseFilter;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterBuzz;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterCool;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterFour;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterGray;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterLight;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterNone;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterWarm;
import com.lanshifu.opengldemo.camera.camera2_surface_demo.filter.Camera2FilterZoom;
import com.lanshifu.opengldemo.utils.GLUtil;
import com.lanshifu.opengldemo.utils.ShaderManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.glClearColor;
import static android.opengl.GLES20.glViewport;

public class CameraV2Renderer implements GLSurfaceView.Renderer {
    public static final String TAG = "CameraV2Renderer";
    private Context mContext;
    GLSurfaceView mGLSurfaceView;
    CameraV2 mCamera;
    private int mTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private float[] mTransformMatrix = new float[16];

    Camera2BaseFilter mCamera2BaseFilter;

    public void init(GLSurfaceView surfaceView, CameraV2 camera, Context context) {
        mContext = context;
        mGLSurfaceView = surfaceView;
        mCamera = camera;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        ShaderManager.init(mContext);
        initSurfaceTexture();
        mCamera2BaseFilter = new Camera2FilterNone(mContext, mTextureId);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        Log.i(TAG, "onSurfaceChanged: " + width + ", " + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        //mSurfaceTexture.updateTexImage()更新预览上的图像
        mSurfaceTexture.updateTexImage();
        //直接通过 SurfaceTexture 获取变换矩阵
        mSurfaceTexture.getTransformMatrix(mTransformMatrix);

        if (mFilterChange) {
            updateFilterView();
        }

        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        mCamera2BaseFilter.draw(mTransformMatrix);

    }

    //创建 SurfaceTexture，CameraV2.startPreview()方法需要SurfaceTexture
    public void initSurfaceTexture() {
        //1、获取一个纹理id
        mTextureId = GLUtil.getOESTextureId();
        //2、纹理id设置到 SurfaceTexture 中，
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        //图片数据固定的，而摄像头数据是变换的，所以每当摄像头有新的数据来时，我们需要通过surfaceTexture.updateTexImage()更新预览上的图像
        // updateTexImage 不应该在OnFrameAvailableLister的回调方法中直接调用，而应该在onDrawFrame中执行。而调用requestRender，可以触发onDrawFrame
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Log.d(TAG, "onFrameAvailable: ");
                mGLSurfaceView.requestRender();
            }
        });

        //将 SurfaceTexture 设置给CameraV2,然后调用startPreview
        mCamera.setSurfaceTexture(mSurfaceTexture);
        mCamera.startPreview();
    }


    boolean mFilterChange = false;
    int mFilterType;

    public void setType(int filterType) {
        if (this.mFilterType == filterType) {
            Log.d(TAG, "setType: this.mFilterType == mFilterType");
            return;
        }
        this.mFilterType = filterType;
        mFilterChange = true;
    }

    void updateFilterView() {
        Log.d(TAG, "updateFilterView: ");
        mCamera2BaseFilter = null;
        switch (this.mFilterType) {
            case ShaderManager.GRAY_SHADER:
                mCamera2BaseFilter = new Camera2FilterGray(mContext, mTextureId);
                break;
            case ShaderManager.BASE_SHADER:
                mCamera2BaseFilter = new Camera2FilterNone(mContext, mTextureId);
                break;
            case ShaderManager.WARM_SHADER:
                mCamera2BaseFilter = new Camera2FilterWarm(mContext, mTextureId);
                break;
            case ShaderManager.COOL_SHADER:
                mCamera2BaseFilter = new Camera2FilterCool(mContext, mTextureId);
                break;
            case ShaderManager.BUZZY_SHADER:
                mCamera2BaseFilter = new Camera2FilterBuzz(mContext, mTextureId);
                break;
            case ShaderManager.FOUR_SHADER:
                mCamera2BaseFilter = new Camera2FilterFour(mContext, mTextureId);
                break;
            case ShaderManager.ZOOM_SHADER:
                mCamera2BaseFilter = new Camera2FilterZoom(mContext, mTextureId);
                break;
            case ShaderManager.LIGHT_SHADER:
                mCamera2BaseFilter = new Camera2FilterLight(mContext, mTextureId);
                break;
            default:
                mCamera2BaseFilter = new Camera2FilterNone(mContext, mTextureId);
                break;
        }
        mFilterChange = false;
    }
}
