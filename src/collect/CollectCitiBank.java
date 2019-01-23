package collect;


import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import data.CitiBankData;
import util.ConnectDB;


public class CollectCitiBank {
	List<CitiBankData> dataList = new ArrayList<CitiBankData>();
	static ConnectDB connectDB;
	public static void main(String[] args) throws Exception {
		List<String> nodeList = new ArrayList<String>();
		nodeList.add("NODE0000000117");//인터넷뱅킹
		nodeList.add("NODE0000000118");//보안/인증센터
		nodeList.add("NODE0000000120");//신용카드
		nodeList.add("NODE0000000121");//대출
		nodeList.add("NODE0000000122");//예금/신탁
		nodeList.add("NODE0000000123");//펀드
		nodeList.add("NODE0000000124");//보험
		nodeList.add("NODE0000000125");//외한
		nodeList.add("NODE0000000127");//씨티골드
		nodeList.add("NODE0000000128");//씨티폰
		nodeList.add("NODE0000000129");//공과금
		nodeList.add("NODE0000000158");//씨티모바일
		CollectCitiBank collectCitiBank = new CollectCitiBank();
		connectDB = new ConnectDB();
		for(int i = 0; i < nodeList.size(); i++) {
			collectCitiBank.init(nodeList.get(i));
			System.out.println("수집완료");
			
			collectCitiBank.connectDB();
			System.out.println("DB 저장완료");
		}
		connectDB.rs.close();
		connectDB.stmt.close();
		connectDB.con.close();
		System.out.println("끝");
	}
	
	public void init(String nodeId) 
	throws Exception {
		String url = "https://www.citibank.co.kr/CusConsCnts0101.act?NodeId=" + nodeId + "&PARENTNODEID=" + nodeId + "&PageNo=" + "1";
		Document doc = Jsoup.connect(url).get();
		String middleCategory = doc.select("p.vAlign strong span:eq(0)").text();
		String numString = doc.select("p.vAlign strong span:eq(1)").text();

		int num = Integer.parseInt(numString);
		int count;
		if(num %10 != 0)
			count = num / 10 + 1;
		else
			count = num / 10;
		System.out.println(num);
		for(int i = 1; i <= count; i++) {
			url = "https://www.citibank.co.kr/CusConsCnts0101.act?NodeId=" + nodeId + "&PARENTNODEID=" + nodeId + "&PageNo=" + i;
			doc = Jsoup.connect(url).get();
			Elements titles = doc.select("table.tableX tbody tr");
			for(Element t : titles)
			{
				String idx = new String(t.select("td:eq(0)").text().getBytes("UTF-8"),"UTF-8");
				String smallCategory = t.select("td:eq(1)").text();
				String category;
				if(middleCategory.equals(smallCategory))
					category =middleCategory;
				else
					category = new String(middleCategory + ">" +t.select("td:eq(1)").text());
				
				String title = new String(t.select("td:eq(2) a").text().getBytes("UTF-8"),"UTF-8");
				String faqId = new String(t.select("td:eq(2) a").attr("onclick").substring(25,39).getBytes("UTF-8"),"UTF-8");
				CitiBankData bankData = new CitiBankData();
				bankData.setPageNo(i);
				bankData.setNodeId(nodeId);
				bankData.setIdx(idx);
				bankData.setCategory(category);
				title = title.replaceAll("'", " ");
				bankData.setTitle(title);
				bankData.setFaqId(faqId);
				dataList.add(bankData);
			}
		}
		
		for(int i = 0; i < dataList.size(); i++) {
			url = "https://www.citibank.co.kr/CusConsCnts0102.act?FAQ_ID=" + dataList.get(i).getFaqId() + "&IDX=" + dataList.get(i).getIdx() + "&NodeId=" + dataList.get(i).getNodeId(); 
			doc = Jsoup.connect(url).get();
			
			Elements conElements = doc.select("div.viewCont");
			String contents = new String(conElements.text().getBytes("UTF-8"),"UTF-8");
			contents = contents.replaceAll("'", " ");
			dataList.get(i).setUrl(url);
			dataList.get(i).setContent(contents);
		}
	}
	
	public void connectDB() {
		try{
			StringBuffer sql = new StringBuffer("INSERT INTO FAQ_BANK(QUESTION, ANSWER, CATEGORY, URL, BANK_NAME) VALUES(?,?,?,?,?)");
			PreparedStatement pstm = connectDB.con.prepareStatement(sql.toString());
			
			for(int i = 0; i < dataList.size(); i++) {
				CitiBankData bankData = dataList.get(i);
				pstm.setString(1, bankData.getTitle());
				pstm.setString(2, bankData.getContent());
				pstm.setString(3, bankData.getCategory());
				pstm.setString(4, bankData.getUrl());
				pstm.setString(5, "씨티은행");
				pstm.addBatch();
				pstm.clearParameters();
			}
			pstm.executeBatch();
			dataList.clear();
			
		} catch(Exception e) {}
	}
}
