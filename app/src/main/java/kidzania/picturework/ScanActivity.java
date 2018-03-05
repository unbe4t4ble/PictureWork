package kidzania.picturework;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import static kidzania.picturework.ConnectionClass.IPDATABASE;
import static kidzania.picturework.ConnectionClass.IPUPLOAD;
import static kidzania.picturework.ConnectionClass.NAMADB;
import static kidzania.picturework.ConnectionClass.PASSWORD;
import static kidzania.picturework.ConnectionClass.PATH;
import static kidzania.picturework.ConnectionClass.USER;

public class ScanActivity extends AppCompatActivity {

    TextView Sesi, VisitDate, txtChangeIP;
    public static TextView IDBrac;
    Button btnAddNameImage;

    String selectedItem;
    public static int SHIFT = 0;
    public static String VISIT_DATE;
    public static String BRAC_NO;

    String[] ArrayShift = new String[]{
            "MORNING",
            "AFTERNOON"
    };

    // list of NFC technologies detected:
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };

    DataSQLlite dbHelper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        dbHelper = new DataSQLlite(ScanActivity.this);
        Sesi = (TextView) findViewById(R.id.Sesi);
        Sesi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog("Shift", ArrayShift);
            }
        });
        VisitDate = (TextView) findViewById(R.id.VisitDate);
        VisitDate.setText(CurrentDate());
        IDBrac = (TextView) findViewById(R.id.IDBrac);
        btnAddNameImage = (Button) findViewById(R.id.btnAddNameImage);
        btnAddNameImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(BRAC_NO)) {
                    startActivity(new Intent(ScanActivity.this, MenuSend.class));
                }else{
                    Toast.makeText(ScanActivity.this, "Please define Braclet Number.", Toast.LENGTH_LONG).show();
                }
            }
        });
        txtChangeIP = (TextView) findViewById(R.id.txtChangeIP);
        txtChangeIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogChangeIP();
                //startActivity(new Intent(ScanActivity.this, UploadImage.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.button_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_upload:
                UploadFileForm();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // creating pending intent:
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);

    }

    @Override
    protected void onPause() {
        super.onPause();

        // disabling foreground dispatch:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            IDBrac.setText(ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)));
            BRAC_NO = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }

    }

    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private void ShowDialog(String Tag_Name, final String[] ArrayList){
        final AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
        builder.setTitle(Tag_Name);

        builder.setSingleChoiceItems(ArrayList, -1, // Index of checked item (-1 = no selection)
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Get the alert dialog selected item's text
                        selectedItem = Arrays.asList(ArrayList).get(i);
                        SHIFT = i;

                        // Display the selected item's text on snack bar
                    }
                });
        //}
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Sesi.setText(selectedItem);
            }
        });
        // Create the alert dialog
        AlertDialog dialog = builder.create();
        // Finally, display the alert dialog
        dialog.show();
    }

    private String CurrentDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat dateNow = new SimpleDateFormat("MM/dd/yyyy");
        VISIT_DATE = dateNow.format(c.getTime());
        return df.format(c.getTime());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }

    private void SaveIP(String IPDatabase, String IPUpload,  String NamaDB, String User, String Password, String Path){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from tbl_DB");
        db.execSQL("insert into tbl_DB values ('"+IPDatabase+"','"+IPUpload+"','"+NamaDB+"','"+User+"', '"+Password+"', '"+Path+"')");
    }

    public void DialogChangeIP(){
        getIP();
        AlertDialog.Builder sayWindows = new AlertDialog.Builder(ScanActivity.this);
        View mView = LayoutInflater.from(ScanActivity.this).inflate(R.layout.pop_ganti_ip, null);
        final EditText edtIPDatabase = mView.findViewById(R.id.edtIPDatabase);
        edtIPDatabase.setText(IPDATABASE);
        final EditText edtIPUpload = mView.findViewById(R.id.edtIPUpload);
        edtIPUpload.setText(IPUPLOAD);
        final EditText edtNamaDB = mView.findViewById(R.id.edtNamaDB);
        edtNamaDB.setText(NAMADB);
        final EditText edtUser = mView.findViewById(R.id.edtUser);
        edtUser.setText(USER);
        final EditText edtPass = mView.findViewById(R.id.edtPass);
        edtPass.setText(PASSWORD);
        final EditText edtLocation = mView.findViewById(R.id.edtLocation);
        edtLocation.setText(PATH);
        final Button btnSave = mView.findViewById(R.id.btnSave);
        sayWindows.setView(mView);
        final AlertDialog mAlertDialog = sayWindows.create();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveIP(edtIPDatabase.getText().toString().trim(),
                        edtIPUpload.getText().toString().trim(),
                        edtNamaDB.getText().toString(),
                        edtUser.getText().toString(),
                        edtPass.getText().toString(),
                        edtLocation.getText().toString()
                        );
                mAlertDialog.dismiss();

            }
        });
        mAlertDialog.show();
    }

    public void getIP() {
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

    private void UploadFileForm(){
        startActivity(new Intent(ScanActivity.this, UploadImage.class));
    }

}
