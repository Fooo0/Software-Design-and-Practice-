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
	private EditText pathText; // url地址
	private String path;
	private EditText input_threadnum;    // 线程数
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
			case PROCESSING: // 更新进度
				Integer index = msg.getData().getInt("index");
				ProgressBar progressBar = list_progressBar.get(index);
				if (msg.getData().getInt("size") == -1){
					Toast.makeText(getApplicationContext(), list_filename.get(index)+ "由于线程受阻下载失败",Toast.LENGTH_LONG).show();
					
					list_filenametext.get(index).setVisibility(View.GONE);
					
					list_progressBar.get(index).setVisibility(View.GONE);
					
					list_buttonsuspend.get(index).setVisibility(View.GONE);
					
					list_buttoncontinue.get(index).setVisibility(View.GONE);
				}else{
					progressBar.setProgress(msg.getData().getInt("size"));
				}
				if (progressBar.getProgress() == progressBar.getMax()) {
					
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); 
					Date Date_now = new Date(System.currentTimeMillis());//获取当前时间 
					String Date_str = formatter.format(Date_now);
					downloadRecord onerecord = new downloadRecord(list_path.get(index), list_savDir.get(index), list_filename.get(index), Date_str);
					recordService.addRecord(onerecord);
					threadnum_limit += list_threadnum.get(index);
					
					Toast.makeText(getApplicationContext(), list_filename.get(index)+ "下载完成",Toast.LENGTH_LONG).show();
					
					list_filenametext.get(index).setVisibility(View.GONE);
					
					list_progressBar.get(index).setVisibility(View.GONE);
					
					list_buttonsuspend.get(index).setVisibility(View.GONE);
					
					list_buttoncontinue.get(index).setVisibility(View.GONE);
					//showDloading();
				}
				break;
			case FAILURE: // 下载失败
				Toast.makeText(getApplicationContext(), R.string.error,Toast.LENGTH_LONG).show();
				break;
			}
		}
	}
	
	/*展示正在下载*/
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
		
		//this.deleteDatabase("downloaddb.db");    // 新增表删除数据库用
		threadnum_limit = 10;    // 最大线程数
		
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
	/*增加导航栏*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.funcmenu, menu);
	    return true;
	}
	
	/*导航栏选项选择*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()) {
	    case R.id.record:
	        // 查看下载记录
	    	 Intent intent_record = new Intent();
	    	 intent_record.setClass(MainActivity.this, RecordActivity.class);
	    	 startActivity(intent_record);
	    	break;
	    }
	    return true;
	}
	
	/**
	 * 获取文件名
	 */
	private String getFileName(HttpURLConnection conn) {
		String filename = this.path.substring(this.path
				.lastIndexOf('/') + 1);
		if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
			for (int i = 0;; i++) {
				String mine = conn.getHeaderField(i);
				if (mine == null)
					break;
				if ("content-disposition".equalsIgnoreCase(conn.getHeaderFieldKey(i))) {  
                    // 通过正则表达式匹配出文件名  
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(  
                    		mine.toLowerCase(Locale.getDefault()));  
                    // 如果匹配到了文件名  
                    if (m.find())  
                        return m.group(1);
				}
			}
			filename = UUID.randomUUID() + ".tmp";// 默认取一个文件名
		}
		return filename;
	}

	private final class ButtonClickListener implements View.OnClickListener {
		private List<DownloadTask> list_task = new ArrayList<DownloadTask>();
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.downloadbutton: // 开始下载
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
						if(threadnum_limit < 0){    // 线程数超出限制
							Toast.makeText(MainActivity.this, R.string.overmuch_threadnum, Toast.LENGTH_SHORT).show();
						}
						else{
							path = pathText.getText().toString();
							savepath = savepathText.getText().toString();
							try {
								URL url = new URL(path);
								HttpURLConnection conn = (HttpURLConnection) url.openConnection();
								filename = getFileName(conn);// 获取文件名称
							} catch (Exception e) {
								throw new RuntimeException("don't connection this url");
								}
							try {
								// URL编码（这里是为了将中文进行URL编码）
								filename = URLEncoder.encode(filename, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							if (Environment.getExternalStorageState().equals(
									Environment.MEDIA_MOUNTED)) {
								savDir = Environment.getExternalStorageDirectory()
										+ savepath;
								File file = new File(savDir);					
								// 如果SD卡目录不存在创建
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
					Toast.makeText(getApplicationContext(),"设置代理服务器!", Toast.LENGTH_LONG).show();
					System.setProperty("proxySet", "true");
					System.setProperty("proxyHost", host);
					System.setProperty("proxyPort", port);
				}
				else{
					Toast.makeText(getApplicationContext(),"取消代理服务器!", Toast.LENGTH_LONG).show();
					System.setProperty("proxySet", "flase");
					System.getProperties().remove("http.proxyHost");
					System.getProperties().remove("http.proxyPort");
				}
			}
		}

		/*
		 * 由于用户的输入事件(点击button, 触摸屏幕....)是由主线程负责处理的，如果主线程处于工作状态，
		 * 此时用户产生的输入事件如果没能在5秒内得到处理，系统就会报“应用无响应”错误。
		 * 所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件，
		 * 导致“应用无响应”错误的出现。耗时的工作应该在子线程里执行。
		 */
		private DownloadTask task;
		private ProgressBar progressbar;
		

		private void exit(int index) {
			if (list_task.get(index) != null)
				list_task.get(index).exit();
		}

		private void download(String path, File savFile, String filename, Integer threadNumber) {
			/*下载任务*/
			task = new DownloadTask(path, savFile, filename, threadNumber, list_filename.indexOf(filename));
			list_task.add(task);
			/*该下载任务的进度条*/
			progressbar = new ProgressBar(MainActivity.this,null,android.R.attr.progressBarStyleHorizontal);
			progressbar.setProgress(0);
			list_progressBar.add(progressbar);
			/*该下载任务的暂停按钮*/
			Button buttonsuspend = new Button(MainActivity.this);
			buttonsuspend.setText("暂停");
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
					Toast.makeText(MainActivity.this, list_filename.get(index) + "已暂停", Toast.LENGTH_SHORT).show();
				} 
			}); 
			list_buttonsuspend.add(buttonsuspend);
			/*该下载任务的继续按钮*/
			Button buttoncontinue = new Button(MainActivity.this);
			buttoncontinue.setText("继续");
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
					Toast.makeText(MainActivity.this, list_filename.get(index) + "恢复下载", Toast.LENGTH_SHORT).show();
				} 
			}); 
			list_buttoncontinue.add(buttoncontinue);
			Thread onethread = new Thread(task);
			list_thread.add(onethread);
			onethread.start();
			/*按钮布局*/
			LinearLayout buttonplace = new LinearLayout(MainActivity.this);
			buttonplace.setOrientation(LinearLayout.HORIZONTAL);
			list_buttonplace.add(buttonplace);
			
			showDloading();    // 展示正在下载
			
		}

		/**
		 * 
		 * UI控件画面的重绘(更新)是由主线程负责处理的，如果在子线程中更新UI控件的值，更新后的值不会重绘到屏幕上
		 * 一定要在主线程里更新UI控件的值，这样才能在屏幕上显示出来，不能在子线程中更新UI控件的值
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
			 * 退出下载
			 */
			private void exit() {
				if (loader != null)
					loader.exit();
			}

			public void run() {
				try {
					// 实例化一个文件下载器
					loader = new FileDownloader(getApplicationContext(), path,
							saveFile, filename, threadNumber);
					list_loader.add(loader);
					list_loader.get(index).clearExit();
					// 设置进度条最大值
					list_progressBar.get(index).setMax(list_loader.get(index).getFileSize());
//					list_downloadProgressListener.add(downloadProgressListener);
					list_loader.get(index).download(downloadProgressListener,index);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(handler.obtainMessage(FAILURE)); // 发送一条空消息对象
				}
			}
		}
	}
}
