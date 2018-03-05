package kidzania.picturework;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import kidzania.picturework.ListImage.AndroidMultiPartEntity;
import kidzania.picturework.ListImage.ImageModel;
import kidzania.picturework.ListImage.ListAdapter;

import static kidzania.picturework.ConnectionClass.IPDATABASE;
import static kidzania.picturework.ConnectionClass.IPUPLOAD;
import static kidzania.picturework.ConnectionClass.NAMADB;
import static kidzania.picturework.ConnectionClass.PASSWORD;
import static kidzania.picturework.ConnectionClass.PATH;
import static kidzania.picturework.ConnectionClass.USER;
import static kidzania.picturework.ListImage.ListAdapter.ArrFileName;
import static kidzania.picturework.ListImage.ListAdapter.ArrLocationImage;
import static kidzania.picturework.ListImage.ListAdapter.ArrLocationImageCompress;

public class ParentListImage extends AppCompatActivity {

    public ListAdapter listAdapter;
    public File[] listFile;

    public RecyclerView myRecyclerviewGroup;
    public WrapContentLinearLayoutManager linearLayoutManager;
    public List<ImageModel> dataModel = new ArrayList<>();
    public ProgressBar progressUpload;
    public Button btnParent;

    public String LocationImage;
    public String FileNameImage;
    public long totalSize = 0;

    public final String IMAGE_DIRECTORY_NAME = "Picture Service";
    public final String SUB_IMAGE_DIRECTORY_NAME_UPLOAD_IMAGE = "Upload";
    public final String SUB_IMAGE_DIRECTORY_NAME_COMPESS_IMAGE = "Compress";

    public String URL_ORIGINAL, URL_RESIZE;

    private static final int PERMISSION_REQUEST_CODE = 1;

    DataSQLlite dbHelper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_list_image);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initialization();
        dbHelper = new DataSQLlite(ParentListImage.this);
        getIP();
    }

    @Override
    public void onResume(){
        super.onResume();
        ArrLocationImage.clear();
        ArrFileName.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initialization() {
        myRecyclerviewGroup = findViewById(R.id.myRecyclerviewGroup);
        listAdapter = new ListAdapter(ParentListImage.this, dataModel);
        myRecyclerviewGroup.setAdapter(listAdapter);
        linearLayoutManager = new WrapContentLinearLayoutManager(ParentListImage.this);
        myRecyclerviewGroup.setLayoutManager(linearLayoutManager);
        progressUpload = findViewById(R.id.progressUpload);
        btnParent = findViewById(R.id.btnParent);
    }


    public void getFromSdcard(File file, int count) {
        if (!file.exists()) {
            file.mkdirs();
        }

        if (file.isDirectory()) {
            listFile = file.listFiles();
            count = 0;
            for (File aListFile : listFile) {
                if (count < 10) {
                    if (aListFile.isFile()) {
                        String files = aListFile.getName();
                        if (files.endsWith(".jpg") || files.endsWith(".jpeg") ||
                                files.endsWith(".JPG") || files.endsWith(".JPEG")) {

                            ImageModel model = new ImageModel();
                            model.setlocationImage(aListFile.getAbsolutePath());
                            model.setfileName(aListFile.getName());
                            dataModel.add(model);
                        }
                    }
                }
                count++;
            }
            listAdapter.notifyItemInserted(dataModel.size());
        }
    }

    public void getFromSdcard(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }

        if (file.isDirectory()) {
            listFile = file.listFiles();
            for (File aListFile : listFile) {
                if (aListFile.isFile()) {
                    String files = aListFile.getName();
                    if (files.endsWith(".jpg") || files.endsWith(".jpeg") ||
                        files.endsWith(".JPG") || files.endsWith(".JPEG")) {

                        ImageModel model = new ImageModel();
                        model.setlocationImage(aListFile.getAbsolutePath());
                        model.setfileName(aListFile.getName());
                        dataModel.add(model);
                    }
                }
            }
            listAdapter.notifyItemInserted(dataModel.size());
        }
    }

    public class UploadFileToOriginal extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressUpload.setVisibility(View.VISIBLE);
            progressUpload.setProgress(0);
            progressUpload.incrementProgressBy(1);

            //super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // updating progress bar value
            progressUpload.setProgress(progress[0]);
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URL_ORIGINAL);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                // Adding file data to http body
                int i = 0;
                while (i < ArrLocationImage.size()){

                    LocationImage = ArrLocationImage.get(i);
                    FileNameImage = ArrFileName.get(i);

                    //File sourceFile = new File(compressImage(LocationImage));
                    File sourceFile = new File(LocationImage);

                    entity.addPart("image"+String.valueOf(i), new FileBody(sourceFile));

                    i++;
                }
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressUpload.setVisibility(View.GONE);
            UploadFileToResize uploadFileToResize = new UploadFileToResize();
            uploadFileToResize.execute();
        }
    }

    public void DeleteFile() {
        int index = 0;
        while (index < ArrLocationImage.size()) {
            File file = new File(ArrLocationImage.get(index));
            file.delete();
            File fileCompress = new File(ArrLocationImageCompress.get(index));
            fileCompress.delete();
            index++;
        }
        clear();
        Toast.makeText(ParentListImage.this, "DONE", Toast.LENGTH_SHORT).show();
    }

    public void clear() {
        dataModel.clear();
        listAdapter.notifyItemRangeRemoved(0, dataModel.size());
        listAdapter.notifyDataSetChanged();
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;


        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;

        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), IMAGE_DIRECTORY_NAME + "/" + SUB_IMAGE_DIRECTORY_NAME_COMPESS_IMAGE);
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + FileNameImage);
        return uriSting;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new RandomAccessFile(sourceFile,"rw").getChannel();
            destination = new RandomAccessFile(destFile,"rw").getChannel();

            long position = 0;
            long count    = source.size();

            source.transferTo(position, count, destination);
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(ParentListImage.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ParentListImage.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(ParentListImage.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(ParentListImage.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private void getIP() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("select * from tbl_DB", null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cursor.moveToPosition(0);
            IPDATABASE = cursor.getString(cursor.getColumnIndex("IPDATABASE"));
            IPUPLOAD = cursor.getString(cursor.getColumnIndex("IPUPLOAD"));
            NAMADB = cursor.getString(cursor.getColumnIndex("NAMADB"));
            USER = cursor.getString(cursor.getColumnIndex("USER"));
            PASSWORD = cursor.getString(cursor.getColumnIndex("PASSWORD"));
            PATH = cursor.getString(cursor.getColumnIndex("PATH"));

            URL_ORIGINAL = "http://"+IPUPLOAD+"/picture_work/uploadImageOriginal.php";
            URL_RESIZE = "http://"+IPUPLOAD+"/picture_work/uploadImageResize.php";
        }
    }

    public class UploadFileToResize extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressUpload.setVisibility(View.VISIBLE);
            progressUpload.setProgress(0);
            progressUpload.incrementProgressBy(1);

            //super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // updating progress bar value
            progressUpload.setProgress(progress[0]);
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(URL_RESIZE);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                // Adding file data to http body
                int i = 0;
                ArrLocationImageCompress.clear();
                while (i < ArrLocationImage.size()){

                    LocationImage = ArrLocationImage.get(i);
                    FileNameImage = ArrFileName.get(i);

                    String LocationCompress = compressImage(LocationImage);
                    File sourceFile = new File(LocationCompress);

                    ArrLocationImageCompress.add(LocationCompress);
                    entity.addPart("image"+String.valueOf(i), new FileBody(sourceFile));

                    i++;
                }
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("Server", "Response from server: " + result);
            progressUpload.setVisibility(View.GONE);
            DeleteFile();
            super.onPostExecute(result);
        }
    }
}
