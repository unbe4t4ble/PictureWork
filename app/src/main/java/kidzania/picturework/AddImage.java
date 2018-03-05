package kidzania.picturework;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import static kidzania.picturework.ConnectionClass.IPDATABASE;
import static kidzania.picturework.ConnectionClass.IPUPLOAD;
import static kidzania.picturework.ConnectionClass.NAMADB;
import static kidzania.picturework.ConnectionClass.PASSWORD;
import static kidzania.picturework.ConnectionClass.PATH;
import static kidzania.picturework.ConnectionClass.USER;
import static kidzania.picturework.ScanActivity.BRAC_NO;
import static kidzania.picturework.ScanActivity.SHIFT;
import static kidzania.picturework.ScanActivity.VISIT_DATE;

public class AddImage extends AppCompatActivity {

    ConnectionClass connectionClass;
    TextView text1;
    EditText fileNameImage;
    ProgressBar pbbar;
    ListView listImage;
    Button btnAddImage;
    Switch SwTransferSale;
    ArrayAdapter adapter = null;
    ArrayList<String> ArraySTR = new ArrayList<>();
    String imgFileName;
    int angkaInc;
    DataSQLlite dbHelper;
    Cursor cursor;

    int GET_STATUS;
    String STATUS = "STATUS";

    String ImageName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);
        dbHelper = new DataSQLlite(this);
        getIP();
        connectionClass = new ConnectionClass();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        fileNameImage = (EditText) findViewById(R.id.fileNameImage);
        listImage = (ListView) findViewById(R.id.listImage);
        listImage.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddImage.this);
                builder.setMessage("Are you sure you want to delete '"+ArraySTR.get(position)+"'?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ImageName = ArraySTR.get(position);
                                if (GET_STATUS == 0) {
                                    DeleteFileImage deleteFileImage = new DeleteFileImage();
                                    deleteFileImage.execute(ImageName);
                                }else{
                                    DeleteDataLocal();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                return false;
            }
        });
        pbbar = (ProgressBar) findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);

        btnAddImage = (Button) findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInc();
            }
        });
        text1 = (TextView) findViewById(R.id.text1);
        SwTransferSale = (Switch) findViewById(R.id.SwTransferSale);
        SwTransferSale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    text1.setVisibility(View.INVISIBLE);
                }else{
                    text1.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = getIntent();
        GET_STATUS = intent.getIntExtra(STATUS, 0);

        if (GET_STATUS == 0) {
            FillList fillList = new FillList();
            fillList.execute("");
        }else{
            RefreshListView();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setInc(){
        if (!TextUtils.isEmpty(fileNameImage.getText())){
            angkaInc = Integer.parseInt(fileNameImage.getText().toString().trim());
            if(SwTransferSale.isChecked()){
                imgFileName = String.valueOf(angkaInc)+".jpg";
            }else {
                imgFileName = "IMG_" + String.valueOf(angkaInc) + ".jpg";
            }

            if (GET_STATUS == 0) {
                AddNameImage addNameImage = new AddNameImage();
                addNameImage.execute("");
            }else{
                InsertDataLocal();
            }
        }
    }

    public class FillList extends AsyncTask<String, String, String> {

        String result;
        boolean Show=true;

        @Override
        protected void onPreExecute() {
            pbbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    result = "Error in connection with SQL server";
                } else {
                    String query = "SELECT IMAGE FROM PW_DATA "+
                                   "WHERE VISIT_DATE='"+VISIT_DATE+"' AND SHIFT="+SHIFT+" AND BRAC_NO='"+BRAC_NO+"'";
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    ArraySTR.clear();
                    while (rs.next()) {
                        ArraySTR.add(rs.getString("IMAGE"));
                    }

                    Show=false;
                    //result = "Connection is success";
                }
            } catch (Exception ex) {
                result = "Error retrieving data from table";

            }
            return result;
        }

        @Override
        protected void onPostExecute(String r) {
            pbbar.setVisibility(View.GONE);
            adapter = new ArrayAdapter(AddImage.this, android.R.layout.simple_list_item_1, ArraySTR);
            listImage.setAdapter(adapter);
            if(Show) {
                Toast.makeText(AddImage.this, r, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class AddNameImage extends AsyncTask<String, String, String> {

        String result = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            pbbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    result = "Error in connection with SQL server";
                } else {
                    String query = "INSERT INTO PW_DATA (VISIT_DATE,SHIFT,BRAC_NO,IMAGE) VALUES ('" + VISIT_DATE + "'," + SHIFT + ",'" + BRAC_NO + "','"+imgFileName+"')";
                    PreparedStatement preparedStatement = con.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    result = "Added Successfully";
                    isSuccess = true;
                }
            } catch (Exception ex) {
                isSuccess = false;
                result = "Added Failed";
            }
            return result;
        }


        @Override
        protected void onPostExecute(String r) {
            pbbar.setVisibility(View.GONE);
            Toast.makeText(AddImage.this, r, Toast.LENGTH_SHORT).show();
            if(isSuccess) {
                angkaInc++;
                fileNameImage.setText(String.valueOf(angkaInc));
                FillList fillList = new FillList();
                fillList.execute("");
            }

        }
    }

    public class DeleteFileImage extends AsyncTask<String, String, String> {

        String result = "";
        Boolean isSuccess = false;

        @Override
        protected void onPreExecute() {
            pbbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r) {
            pbbar.setVisibility(View.GONE);
            Toast.makeText(AddImage.this, r, Toast.LENGTH_SHORT).show();
            if(isSuccess) {
                FillList fillList = new FillList();
                fillList.execute("");
            }

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    result = "Error in connection with SQL server";
                } else {
                    String query = "DELETE FROM PW_DATA "+
                            "WHERE VISIT_DATE='"+VISIT_DATE+"' AND SHIFT="+SHIFT+" AND BRAC_NO='"+BRAC_NO+"' AND IMAGE='"+params[0]+"'";
                    PreparedStatement preparedStatement = con.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    result = "Deleted Successfully";
                    isSuccess = true;
                }
            } catch (Exception ex) {
                isSuccess = false;
                result = "Exceptions";
            }
            return result;
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
        }
    }

    private void InsertDataLocal(){
        String SQLText = "INSERT INTO PW_DATA (VISIT_DATE,SHIFT,BRAC_NO,IMAGE) VALUES ('" + VISIT_DATE + "'," + SHIFT + ",'" + BRAC_NO + "','"+imgFileName+"')";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(SQLText);
        angkaInc++;
        fileNameImage.setText(String.valueOf(angkaInc));
        RefreshListView();
    }

    private void RefreshListView(){
        String SQLText ="SELECT IMAGE FROM PW_DATA WHERE BRAC_NO='"+BRAC_NO+"'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery(SQLText, null);
        ArraySTR.clear();
        if (cursor.moveToFirst()) {
            do {
                ArraySTR.add(cursor.getString(cursor.getColumnIndex("IMAGE")));
            } while (cursor.moveToNext());
        }
        adapter = new ArrayAdapter(AddImage.this, android.R.layout.simple_list_item_1, ArraySTR);
        listImage.setAdapter(adapter);
    }

    private void DeleteDataLocal(){
        String query = "DELETE FROM PW_DATA "+
                "WHERE VISIT_DATE='"+VISIT_DATE+"' AND SHIFT="+SHIFT+" AND BRAC_NO='"+BRAC_NO+"' AND IMAGE='"+ImageName+"'";
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(query);
        RefreshListView();
    }
}
