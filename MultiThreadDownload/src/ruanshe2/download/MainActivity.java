package ruanshe2.download;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ruanshe2.download.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import ruanshe2.download.action.DownloadProgressListener;
import ruanshe2.download.action.FileDownloader;
import ruanshe2.download.action.RecordService;
import ruanshe2.download.action.downloadRecord;


public class MainActivity extends Activity {
	private static final int PROCESSING = 1;
	private static final int FAILURE = -1;
	private RecordService recordService;
	private EditText pathText; // url��ַ
	private String path;
	private EditText input_threadnum;    // �߳���
	private Integer threadnum_limit;
	private EditText savepathText;
	private String savepath;
	private String savDir;
	private String filename;
	private EditText hostText;
	private EditText portText;
	private Button downloadButton;
	//private Button stopButton;
	DownloadProgressListener downloadProgressListener;
	ButtonClickListener listener;
	//private ActionBar actionBar;
	private CheckBox edit_proxy;
	private ArrayList<String> list_filename = new ArrayList<String>();
	private ArrayList<Integer> list_threadnum = new ArrayList<Integer>();
	private ArrayList<TextView> list_filenametext = new ArrayList<TextView>();
	private ArrayList<String> list_path = new ArrayList<String>();
	private ArrayList<String> list_savDir = new ArrayList<String>();
//	private ArrayList<File> list_file = new ArrayList<File>();
	private ArrayList<Thread> list_thread = new ArrayList<Thread>();
	private ArrayList<ProgressBar> list_progressBar = new ArrayList<ProgressBar>();
	private ArrayList<Button> list_buttonsuspend = new ArrayList<Button>();
	private ArrayList<Button> list_buttoncontinue = new ArrayList<Button>();
	private ArrayList<LinearLayout> list_buttonplace = new ArrayList<LinearLayout>();
	private ArrayList<DownloadProgressListener> list_downloadProgressListener = new ArrayList<DownloadProgressListener>();
	private ArrayList<FileDownloader> list_loader = new ArrayList<FileDownloader>();

	private Handler handler = new UIHandler();

	private final class UIHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PROCESSING: // ���½���
				Integer index = msg.getData().getInt("index");
				ProgressBar progressBar = list_progressBar.get(index);
				if (msg.getData().getInt("size") == -1){
					Toast.makeText(getApplicationContext(), list_filename.get(index)+ "�����߳���������ʧ��",Toast.LENGTH_LONG).show();
					
					list_filenametext.get(index).setVisibility(View.GONE);
					
					list_progressBar.get(index).setVisibility(View.GONE);
					
					list_buttonsuspend.get(index).setVisibility(View.GONE);
					
					list_buttoncontinue.get(index).setVisibility(View.GONE);
				}else{
					progressBar.setProgress(msg.getData().getInt("size"));
				}
				if (progressBar.getProgress() == progressBar.getMax()) {
					
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); 
					Date Date_now = new Date(System.currentTimeMillis());//��ȡ��ǰʱ�� 
					String Date_str = formatter.format(Date_now);
					downloadRecord onerecord = new downloadRecord(list_path.get(index), list_savDir.get(index), list_filename.get(index), Date_str);
					recordService.addRecord(onerecord);
					threadnum_limit += list_threadnum.get(index);
					
					Toast.makeText(getApplicationContext(), list_filename.get(index)+ "�������",Toast.LENGTH_LONG).show();
					
					list_filenametext.get(index).setVisibility(View.GONE);
					
					list_progressBar.get(index).setVisibility(View.GONE);
					
					list_buttonsuspend.get(index).setVisibility(View.GONE);
					
					list_buttoncontinue.get(index).setVisibility(View.GONE);
					//showDloading();
				}
				break;
			case FAILURE: // ����ʧ��
				Toast.makeText(getApplicationContext(), R.string.error,Toast.LENGTH_LONG).show();
				break;
			}
		}
	}
	
	/*չʾ��������*/
	private void showDloading(){
		LinearLayout lila = (LinearLayout)findViewById(R.id.main);
		lila.removeAllViews();
		
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(175, 100);
		
		if (list_filename.size() != 0){
			for(int index = 0;index < list_filename.size();index ++){
				list_buttonplace.get(index).removeAllViews();
				list_buttonplace.get(index).addView(list_buttonsuspend.get(index),lp2);
				list_buttonplace.get(index).addView(list_buttoncontinue.get(index),lp2);
				lila.addView(list_filenametext.get(index),lp1);
				lila.addView(list_progressBar.get(index));
				
				lila.addView(list_buttonplace.get(index),lp1);
			}
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//this.deleteDatabase("downloaddb.db");    // ������ɾ�����ݿ���
		threadnum_limit = 10;    // ����߳���
		
		recordService = new RecordService(getApplicationContext());
		
		pathText = (EditText) findViewById(R.id.path);
		input_threadnum = (EditText)findViewById(R.id.edit_threadnum);
		savepathText = (EditText) findViewById(R.id.filename);
		hostText = (EditText) findViewById(R.id.edit_host);
		portText = (EditText) findViewById(R.id.edit_port);
		downloadButton = (Button) findViewById(R.id.downloadbutton);
		//stopButton = (Button) findViewById(R.id.stopbutton);
		edit_proxy = (CheckBox) findViewById(R.id.edit_proxy);
		
					
		listener = new ButtonClickListener();
		downloadButton.setOnClickListener(listener);
		//stopButton.setOnClickListener(listener);
		edit_proxy.setOnClickListener(listener);
		
		downloadProgressListener = new DownloadProgressListener() {
			@Override 
			public void onDownloadSize(int size, int index) {
				Message msg = new Message();
				msg.what = PROCESSING;
				msg.getData().putInt("size", size);
				msg.getData().putInt("index", index);
				handler.sendMessage(msg);
			}
		};
		

	}
	/*���ӵ�����*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.funcmenu, menu);
	    return true;
	}
	
	/*������ѡ��ѡ��*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()) {
	    case R.id.record:
	        // �鿴���ؼ�¼
	    	 Intent intent_record = new Intent();
	    	 intent_record.setClass(MainActivity.this, RecordActivity.class);
	    	 startActivity(intent_record);
	    	break;
	    }
	    return true;
	}
	
	/**
	 * ��ȡ�ļ���
	 */
	private String getFileName(HttpURLConnection conn) {
		String filename = this.path.substring(this.path
				.lastIndexOf('/') + 1);
		if (filename == null || "".equals(filename.trim())) {// �����ȡ�����ļ�����
			for (int i = 0;; i++) {
				String mine = conn.getHeaderField(i);
				if (mine == null)
					break;
				if ("content-disposition".equalsIgnoreCase(conn.getHeaderFieldKey(i))) {  
                    // ͨ��������ʽƥ����ļ���  
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(  
                    		mine.toLowerCase(Locale.getDefault()));  
                    // ���ƥ�䵽���ļ���  
                    if (m.find())  
                        return m.group(1);
				}
			}
			filename = UUID.randomUUID() + ".tmp";// Ĭ��ȡһ���ļ���
		}
		return filename;
	}

	private final class ButtonClickListener implements View.OnClickListener {
		private List<DownloadTask> list_task = new ArrayList<DownloadTask>();
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.downloadbutton: // ��ʼ����
				if (pathText.getText().toString().length() == 0 ||
						input_threadnum.getText().toString().length() == 0){
					Toast.makeText(MainActivity.this, R.string.empty_input, Toast.LENGTH_SHORT).show();
				}
				else{
					Integer threadnumber = Integer.parseInt(input_threadnum.getText().toString());
					if(threadnumber < 0 || threadnumber > 5){
						Toast.makeText(MainActivity.this, R.string.invalid_threadnum, Toast.LENGTH_SHORT).show();
					}
					else{
						threadnum_limit -= threadnumber;
						if(threadnum_limit < 0){    // �߳�����������
							Toast.makeText(MainActivity.this, R.string.overmuch_threadnum, Toast.LENGTH_SHORT).show();
						}
						else{
							path = pathText.getText().toString();
							savepath = savepathText.getText().toString();
							try {
								URL url = new URL(path);
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();
								filename = getFileName(conn);// ��ȡ�ļ�����
							} catch (Exception e) {
								throw new RuntimeException("don't connection this url");
								}
							try {
								// URL���루������Ϊ�˽����Ľ���URL���룩
								filename = URLEncoder.encode(filename, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							if (Environment.getExternalStorageState().equals(
									Environment.MEDIA_MOUNTED)) {
								savDir = Environment.getExternalStorageDirectory()
										+ savepath;
								File file = new File(savDir);					
								// ���SD��Ŀ¼�����ڴ���
								if (!file.exists()) {
									file.mkdir();
								}
								list_filename.add(filename);	
								list_savDir.add(savDir);
								list_path.add(path);
//								list_file.add(file);
								list_threadnum.add(threadnumber);
								TextView tv = new TextView(MainActivity.this);
								tv.setText(filename);
								list_filenametext.add(tv);
								download(path, file, filename, threadnumber);
							} else {
								Toast.makeText(getApplicationContext(),
										R.string.sdcarderror, Toast.LENGTH_LONG).show();
							}
							downloadButton.setEnabled(true);
							//stopButton.setEnabled(true);
						}
					}
				}
				break;			

			case R.id.edit_proxy:
				if (edit_proxy.isChecked()){
					String host = hostText.getText().toString();
					String port = portText.getText().toString();
					Toast.makeText(getApplicationContext(),"���ô��������!", Toast.LENGTH_LONG).show();
					System.setProperty("proxySet", "true");
					System.setProperty("proxyHost", host);
					System.setProperty("proxyPort", port);
				}
				else{
					Toast.makeText(getApplicationContext(),"ȡ�����������!", Toast.LENGTH_LONG).show();
					System.setProperty("proxySet", "flase");
					System.getProperties().remove("http.proxyHost");
					System.getProperties().remove("http.proxyPort");
				}
			}
		}

		/*
		 * �����û��������¼�(���button, ������Ļ....)�������̸߳�����ģ�������̴߳��ڹ���״̬��
		 * ��ʱ�û������������¼����û����5���ڵõ�����ϵͳ�ͻᱨ��Ӧ������Ӧ������
		 * ���������߳��ﲻ��ִ��һ���ȽϺ�ʱ�Ĺ���������������߳��������޷������û��������¼���
		 * ���¡�Ӧ������Ӧ������ĳ��֡���ʱ�Ĺ���Ӧ�������߳���ִ�С�
		 */
		private DownloadTask task;
		private ProgressBar progressbar;
		

		private void exit(int index) {
			if (list_task.get(index) != null)
				list_task.get(index).exit();
		}

		private void download(String path, File savFile, String filename, Integer threadNumber) {
			/*��������*/
			task = new DownloadTask(path, savFile, filename, threadNumber, list_filename.indexOf(filename));
			list_task.add(task);
			/*����������Ľ�����*/
			progressbar = new ProgressBar(MainActivity.this,null,android.R.attr.progressBarStyleHorizontal);
			progressbar.setProgress(0);
			list_progressBar.add(progressbar);
			/*�������������ͣ��ť*/
			Button buttonsuspend = new Button(MainActivity.this);
			buttonsuspend.setText("��ͣ");
			buttonsuspend.setTextSize(12);
			buttonsuspend.setId(400 + list_task.indexOf(task));
			buttonsuspend.setTag(list_task.indexOf(task));
			//buttonsuspend.setOnClickListener(listener);
			buttonsuspend.setOnClickListener(new OnClickListener() { 
				@Override 
				public void onClick(View v) { 
					Integer index = (Integer)v.getTag();
					exit(index);
					list_buttonsuspend.get(index).setEnabled(false);
					list_buttoncontinue.get(index).setEnabled(true);
					Toast.makeText(MainActivity.this, list_filename.get(index) + "����ͣ", Toast.LENGTH_SHORT).show();
				} 
			}); 
			list_buttonsuspend.add(buttonsuspend);
			/*����������ļ�����ť*/
			Button buttoncontinue = new Button(MainActivity.this);
			buttoncontinue.setText("����");
			buttoncontinue.setTextSize(12);
			buttoncontinue.setId(500 + list_task.indexOf(task));
			buttoncontinue.setTag(list_task.indexOf(task));
			buttoncontinue.setEnabled(false);
			//buttoncontinue.setOnClickListener(listener);
			buttoncontinue.setOnClickListener(new OnClickListener() { 
				@Override 
				public void onClick(View v) { 
					Integer index = (Integer)v.getTag();
					
					new Thread(list_task.get(index)).start();
					
					
					list_buttonsuspend.get(index).setEnabled(true);
					list_buttoncontinue.get(index).setEnabled(false);
					Toast.makeText(MainActivity.this, list_filename.get(index) + "�ָ�����", Toast.LENGTH_SHORT).show();
				} 
			}); 
			list_buttoncontinue.add(buttoncontinue);
			Thread onethread = new Thread(task);
			list_thread.add(onethread);
			onethread.start();
			/*��ť����*/
			LinearLayout buttonplace = new LinearLayout(MainActivity.this);
			buttonplace.setOrientation(LinearLayout.HORIZONTAL);
			list_buttonplace.add(buttonplace);
			
			showDloading();    // չʾ��������
			
		}

		/**
		 * 
		 * UI�ؼ�������ػ�(����)�������̸߳�����ģ���������߳��и���UI�ؼ���ֵ�����º��ֵ�����ػ浽��Ļ��
		 * һ��Ҫ�����߳������UI�ؼ���ֵ��������������Ļ����ʾ���������������߳��и���UI�ؼ���ֵ
		 * 
		 */
		private final class DownloadTask implements Runnable {
			private String path;
			private File saveFile;
			private String filename;
			private Integer threadNumber;
			private FileDownloader loader;
			private Integer index;
		
			public DownloadTask(String path, File saveFile, String filename, Integer threadNumber, Integer index) {
				this.path = path;
				this.saveFile = saveFile;
				this.filename = filename;
				this.threadNumber = threadNumber;
				this.index = index;
				list_downloadProgressListener.add(downloadProgressListener);
				
			}

			/**
			 * �˳�����
			 */
			private void exit() {
				if (loader != null)
					loader.exit();
			}

			public void run() {
				try {
					// ʵ����һ���ļ�������
					loader = new FileDownloader(getApplicationContext(), path,
							saveFile, filename, threadNumber);
					list_loader.add(loader);
					list_loader.get(index).clearExit();
					// ���ý��������ֵ
					list_progressBar.get(index).setMax(list_loader.get(index).getFileSize());
//					list_downloadProgressListener.add(downloadProgressListener);
					list_loader.get(index).download(downloadProgressListener,index);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(handler.obtainMessage(FAILURE)); // ����һ������Ϣ����
				}
			}
		}
	}
}
