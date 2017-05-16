package com.wanari.infinitemarker.opengl;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class GLRenderer implements Renderer {

    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    // Geometric variables
    public static float vertices[];
    public static short indices[];
    public static float uvs[];
    public List<PointF> vertexList;
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;

    public static final int MAX_VERTICES = 100;

    float ssu = 1.0f;

    // Our screenresolution
    float mScreenWidth = 1280;
    float mScreenHeight = 768;

    // Misc
    Context mContext;
    long mLastTime;

    int run = 1;

    public GLRenderer(Context c) {
        mContext = c;
        mLastTime = System.currentTimeMillis() + 100;
    }

    public void setVertexList(List<PointF> vertexList) {
        this.vertexList = vertexList;
    }

    public void onPause() {
        /* Do stuff to pause the renderer */
    }

    public void onResume() {
        /* Do stuff to resume the renderer */
        mLastTime = System.currentTimeMillis();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Get the current time
        long now = System.currentTimeMillis();

        // We should make sure we are valid and sane
        if (mLastTime > now) return;

        // Get the amount of time the last frame took.
        long elapsed = now - mLastTime;

        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        for (run = 0; run * MAX_VERTICES < vertexList.size(); run++) {
            // Update our example
            SetupTriangle();
            // Render our example
            Render(mtrxProjectionAndView);
        }

        // Save the current time to see how long it took :).
        mLastTime = now;

    }

    private void Render(float[] m) {

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(GraphicTools.sp_Image, "vPosition");
        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(GraphicTools.sp_Image, "a_texCoord");
        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(GraphicTools.sp_Image, "uMVPMatrix");
        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(GraphicTools.sp_Image, "s_texture");

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);


        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);


        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int) mScreenWidth, (int) mScreenHeight);

        // Clear our matrices
        for (int i = 0; i < 16; i++) {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // Create the triangles
        SetupTriangle();
        // Create the image information
        SetupImage();

        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Create the shaders, images
        int vertexShader = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, GraphicTools.vs_Image);
        int fragmentShader = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, GraphicTools.fs_Image);

        GraphicTools.sp_Image = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(GraphicTools.sp_Image, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(GraphicTools.sp_Image, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(GraphicTools.sp_Image);                  // creates OpenGL ES program executables


        // Set our shader programm
        GLES20.glUseProgram(GraphicTools.sp_Image);
    }

    public void SetupImage() {
        // Create our UV coordinates.

        uvs = new float[MAX_VERTICES * 4 * 2];

        for (int i = 0; i < 30; i++) {
            uvs[(i * 8)] = 0.0f;
            uvs[(i * 8) + 1] = 0.0f;
            uvs[(i * 8) + 2] = 0.0f;
            uvs[(i * 8) + 3] = 1.0f;
            uvs[(i * 8) + 4] = 1.0f;
            uvs[(i * 8) + 5] = 1.0f;
            uvs[(i * 8) + 6] = 1.0f;
            uvs[(i * 8) + 7] = 0.0f;
        }

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);

        // Retrieve our image from resources.
        int id = mContext.getResources().getIdentifier("drawable/green_marker", null, mContext.getPackageName());

        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();
    }

    public void SetupTriangle() {
        // We have to create the vertices of our triangle.

        // Our collection of vertices
        vertices = new float[MAX_VERTICES * 4 * 3];

        List<PointF> subList;
        if (run * MAX_VERTICES < vertexList.size()) {
            subList = vertexList.subList(run * MAX_VERTICES, (run + 1) * MAX_VERTICES < vertexList.size() ? (run + 1) * MAX_VERTICES : vertexList.size() - 1);
        } else {
            subList = new ArrayList<>();
        }
        // Create the vertex data
        for (int i = 0; i < subList.size(); i++) {
            PointF pointF = subList.get(i);
            float offset_x = pointF.x;
            float offset_y = pointF.y;

            // Create the 2D parts of our 3D vertices, others are default 0.0f
            int pos = i * 12;
            vertices[pos] = offset_x;
            vertices[pos + 1] = offset_y + (30.0f * ssu);
            vertices[pos + 2] = 0f;
            vertices[pos + 3] = offset_x;
            vertices[pos + 4] = offset_y;
            vertices[pos + 5] = 0f;
            vertices[pos + 6] = offset_x + (30.0f * ssu);
            vertices[pos + 7] = offset_y;
            vertices[pos + 8] = 0f;
            vertices[pos + 9] = offset_x + (30.0f * ssu);
            vertices[pos + 10] = offset_y + (30.0f * ssu);
            vertices[pos + 11] = 0f;
        }

        // The indices for all textured quads
        indices = new short[MAX_VERTICES * 6];
        short last = 0;
        for (int i = 0; i < MAX_VERTICES; i++) {
            // We need to set the new indices for the new quad
            int pos = i * 6;
            indices[pos] = last;
            indices[pos + 1] = (short) (last + 1);
            indices[pos + 2] = (short) (last + 2);
            indices[pos + 3] = last;
            indices[pos + 4] = (short) (last + 2);
            indices[pos + 5] = (short) (last + 3);

            // Our indices are connected to the vertices so we need to keep them
            // in the correct order.
            // normal quad = 0,1,2,0,2,3 so the next one will be 4,5,6,4,6,7
            last = (short) (last + 4);
        }

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }
}
