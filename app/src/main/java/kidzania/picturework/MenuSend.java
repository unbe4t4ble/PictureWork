package kidzania.picturework;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static kidzania.picturework.ScanActivity.BRAC_NO;
import static kidzania.picturework.ScanActivity.IDBrac;

public class MenuSend extends AppCompatActivity {

    Button btnLive, btnDelay, btnUploadData;

    int STATUS_LIVE = 0;
    int STATUS_DELAY = 1;
    String STATUS = "STATUS";

    DataSQLlite dbHelper;
    Cursor cursor;

    ConnectionClass connectionClass;

    private ProgressDialog pg;

    String vDate, shift, bracNo, imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        dbHelper = new DataSQLlite(MenuSend.this);
        connectionClass = new ConnectionClass();

        btnLive = (Button) findViewById(R.id.btnLive);
        btnDelay = (Button) findViewById(R.id.btnDelay);
        btnUploadData = (Button) findViewById(R.id.btnUploadData);

        btnLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuSend.this, AddImage.class);
                intent.putExtra(STATUS, STATUS_LIVE);
                startActivity(intent);
            }
        });

        btnDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuSend.this, AddImage.class);
                intent.putExtra(STATUS, STATUS_DELAY);
                startActivity(intent);
            }
        });

        btnUploadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNameImage addNameImage = new AddNameImage();
                addNameImage.execute("");
            }
        });
    }

    public class AddNameImage extends AsyncTask<String, String, String> {

        String result = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(MenuSend.this);
            pg.setTitle("Loading");
            pg.setMessage("Please wait ...");
            pg.setCancelable(false);
            pg.setCanceledOnTouchOutside(false);
            pg.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    result = "Error in connection with SQL server";
                } else {
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    cursor = db.rawQuery("SELECT VISIT_DATE,SHIFT,BRAC_NO,IMAGE FROM PW_DATA", null);
                    if (cursor.moveToFirst()) {
                        do {
                            vDate = cursor.getString(cursor.getColumnIndex("VISIT_DATE"));
                            shift = cursor.getString(cursor.getColumnIndex("SHIFT"));
                            bracNo = cursor.getString(cursor.getColumnIndex("BRAC_NO"));
                            imageName = cursor.getString(cursor.getColumnIndex("IMAGE"));

                            String query = "INSERT INTO PW_DATA (VISIT_DATE,SHIFT,BRAC_NO,IMAGE) VALUES ('" + vDate + "'," + shift + ",'" + bracNo + "','"+imageName+"')";
                            PreparedStatement preparedStatement = con.prepareStatement(query);
                            preparedStatement.executeUpdate();

                        } while (cursor.moveToNext());
                    }

                    result = "Added Successfully";
                    DeleteDataLocal();
                }
            } catch (Exception ex) {
                result = "Added Failed";
            }
            return result;
        }


        @Override
        protected void onPostExecute(String r) {
            pg.dismiss();
            Toast.makeText(MenuSend.this, r, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        BRAC_NO = null;
        IDBrac.setText(null);
        super.onBackPressed();
    }

    private void DeleteDataLocal(){
        String query = "DELETE FROM PW_DATA";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(query);
    }
}
