package com.example.korisnik.camera2_primer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.util.Base64;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Base64;
//import java.util.Base64.Encoder;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Size previewsize;
    private Size jpegsizes[] = null;

    private TextureView textureView;
    private ImageView AnaliziranaSlika;
    private Bitmap AnaliziranaSlika_bmp;
    private BoundingBoxView bb_rez;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    Button getpicture;
    String rezultati_str;


    private Socket socket;

    private static final int SERVERPORT = 6588;
    private static final String SERVER_IP = "192.168.1.2";


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // System.loadLibrary("tensorflow_demo");

        textureView = (TextureView) findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        AnaliziranaSlika = (ImageView)findViewById(R.id.analiziranaslika);

        bb_rez = (BoundingBoxView)findViewById(R.id.boundingboxview);

        getpicture = (Button) findViewById(R.id.getpicture);
        getpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();
            }
        });
        new Thread(new ClientThread()).start();
    }



        void getPicture(){

            if(cameraDevice == null){
                return;
            }

            final CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

            try{

                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                if(characteristics != null){
                    jpegsizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                }
                int width=640,height=480;
                if(jpegsizes!= null && jpegsizes.length>0){
                    width=jpegsizes[0].getWidth();
                    height=jpegsizes[0].getHeight();
                }

                final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
                List<Surface> outputSurfaces = new ArrayList<>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

                final CaptureRequest.Builder capturebuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                capturebuilder.addTarget(reader.getSurface());
                capturebuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                capturebuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

                ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener(){

                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = null;
                        try{
                            image = reader.acquireLatestImage();

                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            // save(bytes);
                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);


                            ByteArrayOutputStream bao = new ByteArrayOutputStream();

                            // izrotirati bm za 90 stepeni
                            android.graphics.Matrix matrix = new android.graphics.Matrix();
                            matrix.postRotate(90);
                            Bitmap bm_rot90 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                            bm_rot90.compress(Bitmap.CompressFormat.JPEG, 90, bao);

                            byte[] ba = bao.toByteArray();
                            String encoded_image = Base64.encodeToString(ba, Base64.DEFAULT);

                            int hight = image.getHeight();
                            int width = image.getWidth();

                        //tmp

                            try {

                                OutputStream out = socket.getOutputStream();
                                DataOutputStream dos = new DataOutputStream(out);
                                DataInputStream dis = new DataInputStream(socket.getInputStream());

                                //dos.writeInt(bytes.length);
                                //dos.writeInt(pixels.length);
                                //dos.writeInt(hight);
                                //dos.writeInt(width);
                                //dos.write(bytes, 0, bytes.length);

                                dos.writeUTF(encoded_image);
                                rezultati_str = dis.readUTF();

                               /* Toast.makeText(getApplicationContext(),
                                        rezultati_str, Toast.LENGTH_LONG).show();*/

                                bb_rez.setResults(rezultati_str);
                                AnaliziranaSlika.setImageBitmap(bm_rot90);

                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }



                        }catch (Exception ee){

                        }
                        finally {
                            if(image!=null)
                                image.close();
                        }
                    }

                    void save(byte[] bytes){
                        File file12=getOutputMediaFile();
                        OutputStream outputStream=null;
                        try
                        {
                            outputStream=new FileOutputStream(file12);
                            outputStream.write(bytes);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }finally {
                            try {
                                if (outputStream != null)
                                    outputStream.close();
                            }catch (Exception e){}
                        }
                    }
                };

                HandlerThread handlerThread = new HandlerThread("takepicture");
                handlerThread.start();
                final Handler handler=new Handler(handlerThread.getLooper());

                reader.setOnImageAvailableListener(imageAvailableListener,handler);

                final CameraCaptureSession.CaptureCallback  previewSSession = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        startCamera();
                    }

                    @Override
                    public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                        super.onCaptureStarted(session, request, timestamp, frameNumber);
                    }
                };

                cameraDevice.createCaptureSession(outputSurfaces,
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                try
                                {
                                    session.capture(capturebuilder.build(),previewSSession,handler);
                                }catch (Exception e)
                                {
                                }
                            }
                            @Override
                            public void onConfigureFailed(CameraCaptureSession session) {
                            }
                        }
                        ,handler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }



    public  void openCamera()
    {
        CameraManager manager=(CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try
        {
            String camerId=manager.getCameraIdList()[0];
            CameraCharacteristics characteristics=manager.getCameraCharacteristics(camerId);
            StreamConfigurationMap map=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewsize=map.getOutputSizes(SurfaceTexture.class)[0];
            manager.openCamera(camerId,stateCallback,null);
        }catch (Exception e)
        {
        }
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    private CameraDevice.StateCallback stateCallback=new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice=camera;
            startCamera();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
        }
        @Override
        public void onError(CameraDevice camera, int error) {
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        if(cameraDevice!=null)
        {
            cameraDevice.close();
        }
    }



    void  startCamera()
    {
        if(cameraDevice==null||!textureView.isAvailable()|| previewsize==null)
        {
            return;
        }
        SurfaceTexture texture=textureView.getSurfaceTexture();
        if(texture==null)
        {
            return;
        }
        texture.setDefaultBufferSize(previewsize.getWidth(),previewsize.getHeight());
        Surface surface=new Surface(texture);
        try
        {
            previewBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        }catch (Exception e)
        {
        }
        previewBuilder.addTarget(surface);
        try
        {
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    previewSession=session;
                    getChangedPreview();
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            },null);
        }catch (Exception e)
        {
        }
    }

    void getChangedPreview()
    {
        if(cameraDevice==null)
        {
            return;
        }
        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread=new HandlerThread("changed Preview");
        thread.start();
        Handler handler=new Handler(thread.getLooper());
        try
        {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, handler);
        }catch (Exception e){}
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

   /* @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.korisnik.camera2_primer/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }*/

  /*  @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.korisnik.camera2_primer/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }*/

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory(),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    class ClientThread implements Runnable{
        @Override
        public void run(){
            try{
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVERPORT);
            }catch(UnknownHostException e1){
                e1.printStackTrace();
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }

}
