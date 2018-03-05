package kidzania.picturework;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

public class UploadImage extends ParentListImage {

    File file = new File(Environment.getExternalStorageDirectory().getPath(), IMAGE_DIRECTORY_NAME + "/" + SUB_IMAGE_DIRECTORY_NAME_UPLOAD_IMAGE);

    @Override
    public void onResume(){
        super.onResume();
        clear();
    }

    @Override
    public void initialization(){
        super.initialization();
        btnParent.setText("MOVE TO SERVER");
        btnParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadFileToOriginal uploadFileToOriginal = new UploadFileToOriginal();
                uploadFileToOriginal.execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.button_copy, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.btn_copy:
                startActivity(new Intent(UploadImage.this, CopyImage.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void ShowImage(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                getFromSdcard(file, 10);
            } else {
                requestPermission(); // Code for permission
            }
        }
        else {
            getFromSdcard(file, 10);
        }

    }

    @Override
    public void clear(){
        super.clear();
        ShowImage();
    }





}
