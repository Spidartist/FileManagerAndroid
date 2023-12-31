package vn.edu.hust.fileexamples

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import vn.edu.hust.fileexamples.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    val PREFS_FILE = "my_settings"
    val COLOR_PREF = "background_color"
    var backgroundColor = Color.WHITE

    lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SQLiteDatabase.openDatabase(filesDir.path + "/mydb", null,
            SQLiteDatabase.CREATE_IF_NECESSARY)

        // createTable()

        // List all records
        val cs = db.rawQuery("select * from tblAMIGO", null)
        Log.v("TAG", "Num records: ${cs.count}")
        cs.moveToFirst()
        do {
            val recID = cs.getInt(0)
            val name = cs.getString(1)
            val phoneIndex = cs.getColumnIndex("phone")
            val phone = if (phoneIndex == -1) null else cs.getString(phoneIndex)

            Log.v("TAG", "$recID - $name - $phone")
        }
        while (cs.moveToNext())

        binding.buttonInsert.setOnClickListener {
            db.beginTransaction()
            try {
                val name = binding.editName.text.toString()
                val phone = binding.editPhone.text.toString()
                val sql = "insert into tblAMIGO(name, phone) " +
                        "values('$name','$phone')"
                db.execSQL(sql)
                db.setTransactionSuccessful()
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }

        val prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        backgroundColor = prefs.getInt(COLOR_PREF, Color.WHITE)
        binding.root.setBackgroundColor(backgroundColor)

        binding.buttonRed.setOnClickListener {
            backgroundColor = Color.RED
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonGreen.setOnClickListener {
            backgroundColor = Color.GREEN
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonBlue.setOnClickListener {
            backgroundColor = Color.BLUE
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonReadResource.setOnClickListener {
            val inputStream = resources.openRawResource(R.raw.my_resource)
            val reader = inputStream.reader()
            val content = reader.readText()
            reader.close()
            binding.textView.text = content
        }

        binding.buttonWriteInternal.setOnClickListener {
            val outputStream = openFileOutput("my_data.txt", MODE_PRIVATE)
            val writer = outputStream.writer()
            writer.write(binding.editText.text.toString())
            writer.flush()
            writer.close()
        }

        binding.buttonReadInternal.setOnClickListener {
            val inputStream = openFileInput("my_data.txt")
            val reader = inputStream.reader()
            binding.editText.setText(reader.readText())
            reader.close()
        }

        binding.buttonDeleteInternal.setOnClickListener {
            val path = filesDir.path + "/my_data.txt"
            val file = File(path)
            if (file.exists())
                file.delete()
        }

        binding.buttonWriteExternal.setOnClickListener {
            try {
                val filePath = Environment.getExternalStorageDirectory().path + "/my_data.txt"
                val file = File(filePath)
                val outputStream = file.outputStream()
                val writer = outputStream.writer()
                writer.write(binding.editText.text.toString())
                writer.flush()
                writer.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        binding.buttonReadExternal.setOnClickListener {
            try {
                val filePath = Environment.getExternalStorageDirectory().path + "/my_data.txt"
                val file = File(filePath)
                val inputStream = file.inputStream()
                val reader = inputStream.reader()
                binding.editText.setText(reader.readText())
                reader.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        if (Build.VERSION.SDK_INT >= 30) {
            if (Environment.isExternalStorageManager() == false) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission denied => request permission")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            } else
                Log.v("TAG", "Permission granted")
        }

        val listFiles = Environment.getExternalStorageDirectory().listFiles()
        if (listFiles != null)
            for (item in listFiles) {
                Log.v("TAG", item.name)
                if (item.isDirectory)
                    Log.v("TAG", "directory")
                else if (item.isFile)
                    Log.v("TAG", "file")
            }
    }

    fun createTable() {
        db.beginTransaction()
        try {
            db.execSQL("create table tblAMIGO(" +
                    "recID integer primary key autoincrement," +
                    "name text," +
                    "phone text)")

            db.execSQL("insert into tblAMIGO(name, phone) " +
                    "values('AAA','555-1111')")
            db.execSQL("insert into tblAMIGO(name, phone) " +
                    "values('BBB','555-2222')")
            db.execSQL("insert into tblAMIGO(name, phone) " +
                    "values('CCC','555-3333')")

            db.setTransactionSuccessful()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1234) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission granted")
            } else {
                Log.v("TAG", "Permission denied")
            }
        }
    }

    override fun onStop() {
        val prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(COLOR_PREF, backgroundColor)
        editor.apply()

        db.close()

        super.onStop()
    }
}