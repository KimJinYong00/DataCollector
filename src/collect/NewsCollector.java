package collect;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import data.NewsData;
import util.PropReader;

public class NewsCollector {
	List<NewsData> newsData = new ArrayList<NewsData>();
	public static void main(String[] args) {
		NewsCollector dataCollector = new NewsCollector();
		Map<String, String> dateUrl = dataCollector.getDateUrl();

		Iterator<String> itr = dateUrl.keySet().iterator();
		while(itr.hasNext()) {
			String key = (String)itr.next();
			dataCollector.collectNews(key, dateUrl.get(key));
			System.out.println(key + " 파일 만들기");
			dataCollector.writeTxt(key);
		}
		
		System.out.println("끝");
	}

	public Map<String, String> getDateUrl() {
		Map<String, String> dateUrl = new HashMap<String, String>();
		String url = "http://imnews.imbc.com/replay/2017/nwdesk/link_data.js";
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
			String line = "";
			while((line = br.readLine()) != null) {
				sb.append(line+"\n");
			}
			String urlList = sb.toString();
			System.out.println(urlList);
			String[] arr = urlList.split(";");
			br.close();
			conn.disconnect();
			
			for(int i = 1; i < arr.length; i++) {
				arr[i] = arr[i].trim();
				if(arr[i].length() < 19)
					continue;
				String date = arr[i].substring(11, 20).trim();
				String u = arr[i].substring(23, 83);
				if(Integer.parseInt(date) >= 20170101)
					dateUrl.put(date, u);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return dateUrl;
		
	}

	public void collectNews(String date, String dateUrl) {
		try {
			Document doc = Jsoup.connect(dateUrl).get();
			Elements list = doc.select("div.list_type01 ul");
			for(Element a : list) {
				Elements article = a.select("a");
				for(Element c : article) {
					String title = c.select("span.title").text();
					String tagA = c.attr("href");
					if(title.equals("오늘의 주요뉴스"))
						continue;
					else if(title.equals("뉴스데스크 클로징"))
						continue;
					NewsData nData = new NewsData();
					nData.setTitle(title);
					nData.setUrl(tagA);
					newsData.add(nData);
				}	
			}

			for(int i = 0; i < newsData.size(); i++) {
				doc = Jsoup.connect(newsData.get(i).getUrl()).get();
				Elements txt = doc.select("section.txt");
				String content = Jsoup.clean(txt.html(), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
				newsData.get(i).setContent(content);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTxt(String date) {
		try {
			String outputPath = PropReader.getInstance().getProperty("outputPath");
			PrintWriter pw = new PrintWriter(outputPath + date + ".txt");

			for(int i = 0; i < newsData.size(); i++) {
				BufferedReader br = new BufferedReader(new StringReader(newsData.get(i).getContent()));
				String title= "기사제목:" + newsData.get(i).getTitle();
				pw.println(title);
				while(true) {
					String line = br.readLine();
					if (line==null) break;
					if (line.length() <= 2 || line.contains("◀ ") || line.contains("="))
						continue;
					pw.println(line);
				}
				pw.println();
				br.close();
			}
			newsData.clear();
			pw.close();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
