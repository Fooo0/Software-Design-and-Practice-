package ruanshe2.download.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ruanshe2.download.db.DBassistant;

public class RecordService {
	private DBassistant openHelper;

	public RecordService(Context context) {
		openHelper = new DBassistant(context);
	}
	// 添加记录
	public void addRecord(downloadRecord record){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("downpath", record.geturl());
		values.put("savepath", record.getsaveDir());
		values.put("filename", record.getfilename());
		values.put("date", record.getdate());
		db.insert("filedownrecord", null, values);
		db.close();
	}
	// 查询
	public List<Map<String, String>> getRecord(){
		SQLiteDatabase db = openHelper.getReadableDatabase();
		List<Map<String, String>> allrecords = new ArrayList<Map<String, String>>();
		Cursor cursor = db.rawQuery("select * from filedownrecord order by date desc", null); 
		while (cursor.moveToNext()) {
			Map<String, String> onerecord = new HashMap<String, String>();
			onerecord.put("id", cursor.getString(0));    // 获取第一列。id
			onerecord.put("filename", cursor.getString(3));    // 获取第四列，文件名
			onerecord.put("date", cursor.getString(4));    // 获取第五列，时间
			onerecord.put("savepath", cursor.getString(2));    // 获取第三列，存储位置
			onerecord.put("downpath", cursor.getString(1));    // 获取第二列，下载地址
			allrecords.add(onerecord);
		} 
		cursor.close(); 
		db.close(); 
		return allrecords;
	}
	// 删除
	public void deleterecord(String recordId){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		String[] args = {recordId};
		db.delete("filedownrecord", "id=?", args);
		db.close();
	}
}
