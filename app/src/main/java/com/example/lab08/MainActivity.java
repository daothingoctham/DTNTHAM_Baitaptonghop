package com.example.lab08;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.model.Contact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    String dbName = "Contact.db";
    String dbPath = "/databases/";
    SQLiteDatabase db = null;
    ArrayAdapter<Contact> adapter;
    ListView lvContact;
    Button btnThem;
    Contact ct;
    int posUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        xulyCopy();
        addView();
        hienThiContact();
        addEvent();
    }

    private void hienThiContact() {
        db = openOrCreateDatabase(dbName, MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("Select * from Contact", null);
        while (cursor.moveToNext()) {
            int ma = cursor.getInt(0);
            String ten = cursor.getString(1);
            String dienthoai = cursor.getString(2);
            adapter.add(new Contact(ma, ten, dienthoai));
        }
    }

    private void addView() {
        lvContact = findViewById(R.id.lvContact);
        adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_list_item_1);
        lvContact.setAdapter(adapter);
        btnThem = findViewById(R.id.btnThem);
        registerForContextMenu(lvContact);
    }


    private void xulyCopy() {
        try {
            File dbFile = getDatabasePath(dbName);
            if (!dbFile.exists()) {
                copyDataFromAsset();
                Toast.makeText(MainActivity.this, "Copy thanh cong", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(MainActivity.this
                        , "file đã tồn tại trên app",
                        Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("Loi", e.toString());
        }

    }

    private void copyDataFromAsset() {
        try {
            InputStream myInput = getAssets().open(dbName);
            String outFileName = getApplicationInfo().dataDir + dbPath + dbName;
            File f = new File(getApplicationInfo().dataDir + dbPath);
            if (!f.exists())
                f.mkdir();
            OutputStream myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception ex) {
            Log.e("LOI", ex.toString());
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context, menu);
    }

    private void addEvent() {
        btnThem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ThemSuaActivity.class);
                intent.putExtra("TRANGTHAI", "THEM");
                startActivityForResult(intent, 113);
            }
        });
        lvContact.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ct = adapter.getItem(i);
                posUpdate = i;
                return false;
            }
        });

    }

    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mnuXoa) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Xác nhận xóa");
            builder.setMessage("Bạn thật sự muốn xóa");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db.delete("Contact", "Ma=?", new String[]{ct.getMa() + ""});
                    adapter.remove(ct);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            //hiển thị cửa sổ này lên:
            dialog.show();
        }
            return super.onContextItemSelected(item);


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Contact ctNew = (Contact) data.getSerializableExtra("CONTACT");
        //addnew
        if (resultCode == 114 && requestCode == 113) {
            adapter.add(ctNew);
            try {
                ContentValues values = new ContentValues();
                values.put("Ma", ctNew.getMa());
                values.put("Ten", ctNew.getTen());
                values.put("Dienthoai", ctNew.getDienthoai());
                if (db.insert("Contact", null, values) > 0) {
                    Toast.makeText(MainActivity.this, "Thêm mới thành công", Toast.LENGTH_SHORT);

                } else {
                    Toast.makeText(MainActivity.this, "Thêm mới thất bại", Toast.LENGTH_SHORT);
                }
            } catch (Exception e) {
                Log.e("Loi:", e.toString());
            }
        }
        //end addnew
        //update
        if (requestCode == 113 && resultCode == 115) {
            try {
                ContentValues values = new ContentValues();
                values.put("Ten", ctNew.getTen());
                values.put("Dienthoai", ctNew.getDienthoai());
                db.update("Contact", values, "Ma=?", new String[]{ctNew.getMa() + ""});
                adapter.getItem(posUpdate).setTen("aaa");
                adapter.getItem(posUpdate).setDienthoai("kkk");
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("Loi:", e.toString());
            }
        }
        //end update

    }


}