package ruanshe2.download.action;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class DownloadThread extends Thread {
	private static final String TAG = "DownloadThread";
	private File saveFile;
	private URL downUrl;
	private int block;
	/* ���ؿ�ʼλ�� */
	private int threadId = -1;
	private int downLength;
	private boolean finish = false;
	private FileDownloader downloader;

	public DownloadThread(FileDownloader downloader, URL downUrl,
			File saveFile, int block, int downLength, int threadId) {
		this.downUrl = downUrl;
		this.saveFile = saveFile;
		this.block = block;
		this.downloader = downloader;
		this.threadId = threadId;
		this.downLength = downLength;
	}

	@Override
	public void run() {
		if (downLength < block) {// δ�������
			try {
				HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
				http.setConnectTimeout(5 * 1000); // �������ӳ�ʱ
				http.setRequestMethod("GET"); // �������󷽷��������ǡ�GET��
				// ������ɽ��ܵ�MIME����
				http.setRequestProperty(
						"Accept",
						"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
				http.setRequestProperty("Accept-Language", "zh-CN"); // �������ϣ�����������࣬���������ܹ��ṩһ�����ϵ����԰汾ʱҪ�õ�
				http.setRequestProperty("Referer", downUrl.toString());// ����һ��URL���û��Ӹ�URL�����ҳ��������ʵ�ǰ�����ҳ�档
				http.setRequestProperty("Charset", "UTF-8"); // �ַ���
				int startPos = block * (threadId - 1) + downLength;// ��ʼλ��
				int endPos = block * threadId - 1;// ����λ��
				http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);// ���û�ȡʵ�����ݵķ�Χ
				// ��������ͣ����Servlet���ص�����������������й����ֵ�ǳ����á�
				http.setRequestProperty(
						"User-Agent",
						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
				http.setRequestProperty("Connection", "Keep-Alive"); // ����Ϊ�־�����
				// �õ�������
				InputStream inStream = http.getInputStream();
				byte[] buffer = new byte[4096];
				int offset = 0;
				print("Thread " + this.threadId
						+ " start download from position " + startPos);
				// ��������ļ�
				RandomAccessFile threadfile = new RandomAccessFile(
						this.saveFile, "rwd");
				// ��λ��posλ��
				threadfile.seek(startPos);
				while (!downloader.getExit()
						&& (offset = inStream.read(buffer, 0, 4096)) != -1) {
					// д���ļ�
					threadfile.write(buffer, 0, offset);
					downLength += offset; // �ۼ����صĴ�С
					downloader.update(this.threadId, downLength); // ����ָ���߳���������λ��
					downloader.append(offset); // �ۼ������ش�С
				}
				threadfile.close();
				inStream.close();
				print("Thread " + this.threadId + " download finish");
				this.finish = true;
			} catch (Exception e) {
				this.downLength = -1;
				print("Thread " + this.threadId + ":" + e);
			}
		}
	}

	private static void print(String msg) {
		Log.i(TAG, msg);
	}

	/**
	 * �����Ƿ����
	 * 
	 * @return
	 */
	public boolean isFinish() {
		return finish;
	}

	/**
	 * �Ѿ����ص����ݴ�С
	 * 
	 * @return �������ֵΪ-1,��������ʧ��
	 */
	public long getDownLength() {
		return downLength;
	}
}
