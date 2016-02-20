package ruanshe2.download;

import java.util.List;
import java.util.Map;
import com.ruanshe2.download.R;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import ruanshe2.download.action.RecordService;

public class RecordActivity extends Activity {
	private static final int ITEM_DELETE = 0;
	
	private ActionBar actionBar;
	private RecordService recordService;
	private ListView lv;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		
		actionBar = getActionBar();    // ��ȡ������
		actionBar.setDisplayHomeAsUpEnabled(true);    // ����������Ч��
		
		recordService = new RecordService(getApplicationContext());
		
		lv = (ListView) findViewById(R.id.recordList);  
		
		getRecord();
        // ���ó����¼�  
        registerForContextMenu(lv);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.funcmenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()) {
	    case android.R.id.home:    // ��ҳ����
	        this.finish();  
	        break;
	    case R.id.record:     // �鿴���ؼ�¼
			 Intent intent_record = new Intent();
			 intent_record.setClass(this, RecordActivity.class);
			 startActivity(intent_record);
			 break;
	    }
	    return true;
	}
	
	 // ����adapter.  
    public SimpleAdapter buildListAdapter(Context context,  
            List<Map<String, String>> data) {  
        SimpleAdapter adapter = new SimpleAdapter(context, data, R.layout.onerecord,  
                new String[] { "id", "filename", "date", "savepath", "downpath" }, new int[] { R.id.recordid,  
                        R.id.filename, R.id.date, R.id.savepath, R.id.url });  
        return adapter;  
    }
    
    // ����ʱ��ʾ�Ĳ˵�  
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v,  
            ContextMenuInfo menuInfo) {
        menu.add(0, ITEM_DELETE, 0, "ɾ��");  
    }
    
    public boolean onContextItemSelected(MenuItem item) {  
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item  
                .getMenuInfo();  
        String reocrdId = ((TextView) info.targetView.findViewById(R.id.recordid))  
                .getText().toString();  
        if (!TextUtils.isEmpty(reocrdId)) {
            switch (item.getItemId()) {  
            case ITEM_DELETE:  
                // ɾ������   
            	recordService.deleterecord(reocrdId);
            	Intent intent_record = new Intent();
   	    	 	intent_record.setClass(RecordActivity.this, RecordActivity.class);
   	    	 	startActivity(intent_record);
            	//getRecord();
                break;  
            default:  
                break;  
            }  
        } 
        return false;  
    } 
	
	private void getRecord(){
		List<Map<String, String>> recordList = recordService.getRecord();
		LinearLayout ll = (LinearLayout)findViewById(R.id.llayout);
		if(recordList.size() == 0){
			TextView tempty = new TextView(this);
			tempty.setText("û���κ����ؼ�¼Ŷ");
			ll.addView (tempty);
		}
		else{
			SimpleAdapter adapter = buildListAdapter(this, recordList);  
			lv.setAdapter(adapter);
	    }
	}
}
