package ruanshe2.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBassistant extends SQLiteOpenHelper {
	private static final String DBNAME = "downloaddb.db";
	private static final int VERSION = 1;

	public DBassistant(Context context) {
		super(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// ´´½¨filedownlog±í
		db.execSQL("CREATE TABLE IF NOT EXISTS filedownlog (id integer primary key autoincrement, downpath varchar(100), threadid INTEGER, downlength INTEGER)");
		db.execSQL("CREATE TABLE IF NOT EXISTS filedownrecord (id integer primary key autoincrement, downpath varchar(100), savepath varchar(100), filename varchar(100), date varchar(100))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS filedownlog");
		db.execSQL("DROP TABLE IF EXISTS filedownrecord");
		onCreate(db);
	}
}
