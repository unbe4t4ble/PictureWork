package kidzania.picturework;

import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import kidzania.picturework.ListImage.ListAdapter;

import static kidzania.picturework.ConnectionClass.PATH;
import static kidzania.picturework.ListImage.ListAdapter.ArrLocationImage;

/**
 * Created by mubarik on 22/11/2017.
 */

public class CopyImage extends ParentListImage {

    File file = new File(Environment.getExternalStorageDirectory().getPath(), PATH);
    int index;

    @Override
    public void onResume(){
        super.onResume();
        clear();
    }

    @Override
    public void initialization(){
        super.initialization();
        btnParent.setText("MOVE TO PHOTO SERVICE FOLDER");
        btnParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = 0;
                while (index < ArrLocationImage.size()) {
                    File source = new File(ArrLocationImage.get(index));
                    String[] setFileName = ListAdapter.ArrFileName.get(index).split("IMG");
                    String FileName = "IMG"+setFileName[1];
                    File destination = new File(Environment.getExternalStorageDirectory().getPath(), IMAGE_DIRECTORY_NAME +
                            "/" + SUB_IMAGE_DIRECTORY_NAME_UPLOAD_IMAGE +"/"+FileName);
                    try {
                        copyFile(source, destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    index++;
                }
                DeleteFile();
                Toast.makeText(CopyImage.this, "DONE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void clear(){
        super.clear();
        getFromSdcard(file);
    }

    @Override
    public void DeleteFile(){
        int index = 0;
        while (index < ArrLocationImage.size()) {
            File file = new File(ArrLocationImage.get(index));
            file.delete();
            index++;
        }
        clear();
        Toast.makeText(CopyImage.this, "DONE", Toast.LENGTH_SHORT).show();
    }

}
