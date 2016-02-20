package ruanshe2.download.action;

public class downloadRecord {

		private String url;
		private String saveDir;
		private String filename;
		private String date;
		
		public downloadRecord(String url, String saveDir, String filename, String date){
			this.url = url;
			this.saveDir = saveDir;
			this.filename = filename;
			this.date = date;
		}
		
		public String geturl(){
			return this.url;
		}
		
		public String getsaveDir(){
			return this.saveDir;
		}
		
		public String getfilename(){
			return this.filename;
		}
		
		public String getdate(){
			return this.date;
		}

}
