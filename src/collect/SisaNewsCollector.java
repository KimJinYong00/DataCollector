package collect;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import data.SisaNewsData;
import util.PropReader;

public class SisaNewsCollector {
	List<SisaNewsData> newsData = new ArrayList<SisaNewsData>();
	public static void main(String[] args) {
		SisaNewsCollector sisaNewsCollector = new SisaNewsCollector();

		sisaNewsCollector.getUrl("http://imnews.imbc.com/weeklyfull/weekly01/2017.html");
		for(int i = 2; i <= 4; i++)
			sisaNewsCollector.getUrl("http://imnews.imbc.com/weeklyfull/weekly01/2017,1,list1," + i + ".html");
		sisaNewsCollector.collectData();
		System.out.println("수집완료");
		sisaNewsCollector.writeTxt();
		System.out.println("저장완료");
	}

	public void getUrl(String url) {
		if(url.contains("2017.html")) {
			try {
				Document doc = Jsoup.connect(url).get();
				Elements li = doc.select("li.mh_first");
				for(Element elm : li) {
					String u = elm.select("a").attr("href").toString();
					String title = elm.select("div.add_date").text();
					String date = elm.select("span.date").text();
					
					SisaNewsData sisaData = new SisaNewsData();
					sisaData.setTitle(title);
					sisaData.setUrl(u);
					sisaData.setDate(date);
					newsData.add(sisaData);
				}
			} catch(Exception e){}
		}
		try {
			Document doc = Jsoup.connect(url).get();
			Elements div = doc.select("div.alt_1_detail");
			for(Element d : div) {
				String title = d.select("a").text();
				String u = d.select("a").attr("href").toString();
				String date = d.select("div.alt_time").text();

				SisaNewsData sisaData = new SisaNewsData();
				sisaData.setTitle(title);
				sisaData.setUrl(u);
				sisaData.setDate(date);
				newsData.add(sisaData);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void collectData() {
		try {
			for(int i = 0; i < newsData.size(); i++) {
				Document doc = Jsoup.connect(newsData.get(i).getUrl()).get();
				Elements txt = doc.select("section.txt");
				String content = Jsoup.clean(txt.html(), "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
				newsData.get(i).setContent(content);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeTxt() {
		try {
			String outputPath = PropReader.getInstance().getProperty("outputPath");
			PrintWriter pw = new PrintWriter(outputPath + "sisaNews.txt");
			for(int i = 0; i < newsData.size(); i++) {
				BufferedReader br = new BufferedReader(new StringReader(newsData.get(i).getContent()));
				String title = "기사제목:" + newsData.get(i).getTitle() + "\r\n날짜:" + newsData.get(i).getDate();
				pw.println(title);
				while(true) {
					String line = br.readLine();
					if (line==null) break;
					line.trim();
					if (line.equals(""))
						continue;
					if (line.length() <= 2 || line.contains("◀ ") || line.contains("=") || line.contains("-") || line.contains("    "))
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
