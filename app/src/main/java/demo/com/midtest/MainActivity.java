package demo.com.midtest;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    EditText et1, et2, et3;
    Button button, button2, button3;
    ListView listView;
    private DB mDbHelper;
    private long rowId;
    private EditText editText1;
    private String editString1;
    private int mNoteNumber = 1;
    protected static final int MENU_INSERT = Menu.FIRST;
    protected static final int MENU_DELETE = Menu.FIRST + 1;
    protected static final int MENU_UPDATE = Menu.FIRST + 2;
    protected static final int MENU_INSERT_WITH_SPECIFIC_ID = Menu.FIRST + 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  //隱藏狀態列
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBInfo.DB_FILE = getDatabasePath("mydb") + ".db";
        copyDBFile();

        findViews();
        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.empty));
        listView.setOnItemClickListener(this);
        setAdapter();
    }

    void findViews() {
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button2.setEnabled(false);
        button3.setEnabled(false);
        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et3 = (EditText) findViewById(R.id.et3);
    }

    private void setAdapter() {
        mDbHelper = new DB(this).open();
        fillData();
    }

    void fillData() {
        Cursor c = mDbHelper.getAll();
        startManagingCursor(c);
        SimpleCursorAdapter scAdapter = new SimpleCursorAdapter(
                this, R.layout.my_listview, c, new String[]{"公司名稱", "電話", "Email"}, new int[]{R.id.tvName, R.id.tvPhone, R.id.tvEmail},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(scAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        rowId = id;
        System.out.println("rowId = " + rowId);
        Cursor c = mDbHelper.get(rowId);
        et1.setText(c.getString(c.getColumnIndex("公司名稱")));
        et2.setText(c.getString(c.getColumnIndex("電話")));
        et3.setText(c.getString(c.getColumnIndex("Email")));
        button2.setEnabled(true);
        button3.setEnabled(true);
    }

    public void onAdd(View v) {

        if (!et1.getText().toString().equals("")) {
            mNoteNumber = listView.getCount() + 1;
            String comName = et1.getText().toString();
            String comPhone = et2.getText().toString();
            String comEmail = et3.getText().toString();
            mDbHelper.create(comName, comPhone, comEmail);
            fillData();
        }
    }

    public void onUpdate(View v) {

        String comName = et1.getText().toString();
        String comPhone = et2.getText().toString();
        String comEmail = et3.getText().toString();
        if (!et1.equals("")) {
            mDbHelper.update(rowId, comName, comPhone, comEmail);
            fillData();
        }
    }

    public void onDelete(View v) {

        mDbHelper.delete(rowId);
        fillData();
    }

    public void onCall(View v) {
        Intent intentDial = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + et2.getText().toString()));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intentDial);
    }

    public void onMsg(View v) {
        if ( !et2.getText().toString().equals("") ) {
            editText1 = new EditText(this);
        final SmsManager smsManager = SmsManager.getDefault();
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(), 0);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.sendMsg)
                    .setMessage(R.string.enterText)
                    .setView(editText1)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editString1 = editText1.getText().toString();
                            if (!editString1.equals("")) {
                                smsManager.sendTextMessage(
                                        et2.getText().toString(),
                                        null,
                                        editText1.getText().toString(),
                                        pendingIntent,
                                        null
                                );
                                Toast.makeText(MainActivity.this, R.string.sendTo + et2.getText().toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, R.string.noMsg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();
        }

    }

    public void copyDBFile()
    {
        try {
            File f = new File(DBInfo.DB_FILE);
            File dbDir = new File(DBInfo.DB_FILE.substring(0,DBInfo.DB_FILE.length()-12));
            Log.d("GameDBHelper", "copyFiles : "+DBInfo.DB_FILE);
            dbDir.mkdirs();
            if (! f.exists())
            {

                InputStream is = getResources().openRawResource(R.raw.mydb);
                OutputStream os = new FileOutputStream(DBInfo.DB_FILE);
                int read;
                Log.d("GameDBHelper", "Start Copy");
                while ((read = is.read()) != -1)
                {
                    os.write(read);
                }
                Log.d("GameDBHelper", "FilesCopied");
                os.close();
                is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {  //返回鍵事件
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("確認視窗");
        builder.setMessage("確定要結束應用程式嗎?");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("確定",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
        builder.show();
    }
}
