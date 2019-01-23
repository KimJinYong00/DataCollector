package collect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class SisaPreviewCollector {
	List<String> data = new ArrayList<String>();
	public static void main(String[] args) {
		SisaPreviewCollector sisaCollector = new SisaPreviewCollector();
		TreeMap<String, String> dateUrl = sisaCollector.getDateUrl();

		Iterator<String> itr = dateUrl.keySet().iterator();
		
		while(itr.hasNext()) {
			String key = (String)itr.next();
			System.out.println("date:" + key);
			sisaCollector.collectData(key, dateUrl.get(key));	
		}
		sisaCollector.writeTxt();
		System.out.println("저장완료");
	}
	
	public void writeTxt() {
		try {
			PrintWriter pw = new PrintWriter("C:/Users/kjy79/Desktop/Collect/sisa/sisaPreview.txt");
			for(int i = 0; i < data.size(); i++) {
				pw.println(data.get(i));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	public TreeMap<String, String> getDateUrl() {
		TreeMap<String, String> dateUrl = new TreeMap<String, String>();
		String url = "http://vodmall.imbc.com/util/wwwUtil_sbox_contents.aspx?progCode=1000845100760100000&yyyy=2017";
		try {
			URL conUrl = new URL(url);

			HttpURLConnection conn = (HttpURLConnection) conUrl.openConnection();
			conn.setDoInput(true);          
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setReadTimeout(20000);
			conn.setRequestMethod("GET");

			StringBuffer sb =  new StringBuffer();
			BufferedReader br = new BufferedReader( new InputStreamReader(conn.getInputStream()));

			for(;;){
				String line =  br.readLine();
				if(line == null) break;
				sb.append(line+"\n");
			}
			
			br.close();
			conn.disconnect();

			String result = sb.toString();
			result = result.replaceAll("\\(", "");
			result = result.replaceAll("\\)", "");
			
			JSONParser parser = new JSONParser();
			JSONArray arr = (JSONArray)parser.parse(result);
			
			for(int i = 0; i < arr.size(); i++) {
				JSONObject json = (JSONObject)arr.get(i);
				dateUrl.put(json.get("BroadDate").toString(), json.get("BroadCastID").toString());
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
		return dateUrl;
	}

	public void collectData(String date, String id) {
		String url = "http://vodmall.imbc.com/util/player/playerutil.ashx";
		try {
			Document doc = Jsoup.connect(url).data("broadcastID", id).parser(Parser.xmlParser()).timeout(20000).post();
			Elements datas = doc.select("root");
			
			for(int i = 0; i < datas.size(); i++) {
				Elements elements = datas.get(0).getElementsByTag("contents");
				elements = elements.get(0).getElementsByTag("current_content");
				elements = elements.get(0).getElementsByTag("preview");
				
				String txt = elements.get(0).text().trim();
				txt = txt.replaceAll("<b>|</b>|<br>|<p>|&#8211", "");
				txt = txt.replaceAll("(   )|(  )","\\\r\\\n");
				txt = txt.replaceAll("(\\. )", ".\\\r\\\n");
				txt = txt.replaceAll("1\\.|2\\.|3\\.|4\\.", "");
				data.add(txt + "\r\n");
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
