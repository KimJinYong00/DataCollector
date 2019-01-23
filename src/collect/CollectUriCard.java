package collect;


import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import data.UriCardData;
import util.ConnectDB;

public class CollectUriCard {
	List<UriCardData> dataList = new ArrayList<UriCardData>();
	static ConnectDB connectDB;
	public static void main(String[] args) throws Exception {
		connectDB = new ConnectDB();
		TreeMap<String, String> nodeList = new TreeMap<String, String>();
		nodeList.put("NODE0000000014", "ī��߱�/����>��ǰ,����");
		nodeList.put("NODE0000000015", "ī��߱�/����>ī���û,�߱�,���");
		nodeList.put("NODE0000000016", "ȸ������/��������>��ȸ��");
		nodeList.put("NODE0000000017", "ȸ������/��������>ȸ������");
		nodeList.put("NODE0000000018", "ȸ������/��������>��������");
		nodeList.put("NODE0000000019", "ī���̿�>�̿��ѵ�");
		nodeList.put("NODE0000000020", "ī���̿�>�ŷ�����");
		nodeList.put("NODE0000000021", "��������>�ܱ�ī�����(���ݼ���)");
		nodeList.put("NODE0000000022", "��������>���ī�����(ī���)");
		nodeList.put("NODE0000000024", "����Ʈ/���ϸ���>����Ʈ,���ϸ���");
		nodeList.put("NODE0000000025", "Ȩ������");
		nodeList.put("NODE0000000026", "����Ʈ��");
		nodeList.put("NODE0000000027", "�ؿ��̿�");
		nodeList.put("NODE0000000028", "�������/���ǽ�û��");
		nodeList.put("NODE0000000029", "��Ÿ");
		CollectUriCard collectUriCard = new CollectUriCard();
		Iterator<String> iterator = nodeList.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next();
			collectUriCard.init(key, nodeList.get(key));
			System.out.println("���� �Ϸ�");
			collectUriCard.connectDB();
			System.out.println("DB ���ۿϷ�");
		}
		connectDB.rs.close();
		connectDB.stmt.close();
		connectDB.con.close();
		
		System.out.println("��");
	}
	
	public void init(String nodeId, String category) 
	throws Exception {
		String url = "https://sccd.wooribank.com/ccd/Dream?withyou=CDCNT0213&actionBoard=goFaqList&Faq_listcount=1&nodeId=" + nodeId;
		Document doc = Jsoup.connect(url).get();
		
		String numString = doc.select("p.p-card-notice em:eq(1)").text().substring(0, 1);
		
		int num = Integer.parseInt(numString);
		int count;
		if(num % 5 != 0)
			count = num / 5 + 1;
		else
			count = num / 5;
		System.out.println(num);
		
		for(int i = 1; i <= count; i++) {
			url = "https://sccd.wooribank.com/ccd/Dream?withyou=CDCNT0213&actionBoard=goFaqList&Faq_listcount=1&nodeId=" + nodeId + "&pageNo=" + i;
			doc = Jsoup.connect(url).get();
			Elements titles = doc.select("tr.f");
			for(Element t : titles)
			{
				String title = new String(t.select("td.title").text().getBytes("UTF-8"),"UTF-8");
				title = title.replaceAll("'", "");
				String uriId = t.select("td.title a").attr("onclick").substring(30,44);
				UriCardData cardData = new UriCardData();
				cardData.setCategory(category);
				cardData.setUriId(uriId);
				cardData.setTitle(title);
				dataList.add(cardData);
			}
		}
		
		for(int i = 0; i < dataList.size(); i++) {
			url = "https://sccd.wooribank.com/ccd/Dream?withyou=CDCNT0213&__STEP=1&actionBoard=goFaqList&kb_id=" + dataList.get(i).getUriId() + "&nodeId=" + nodeId;
			doc = Jsoup.connect(url).get();
			Elements conElements = doc.select("div.board-view-cont div");
			String contents = new String(conElements.text().getBytes("UTF-8"),"UTF-8");
			contents = contents.replaceAll("'", " ");
			dataList.get(i).setContent(contents);
			dataList.get(i).setUrl(url);
		}
	}
	
	public void connectDB() {
		try{
			StringBuffer sql = new StringBuffer("INSERT INTO FAQ_BANK_NOISPIDER(QUESTION, ANSWER, CATEGORY, URL, BANK_NAME) VALUES(?,?,?,?,'�츮ī��')");
			PreparedStatement pstm = (PreparedStatement) connectDB.con.prepareStatement(sql.toString());
			
			for(int i = 0; i < dataList.size(); i++) {
				UriCardData cardData = dataList.get(i);
				pstm.setString(1, cardData.getTitle());
				pstm.setString(2, cardData.getContent());
				pstm.setString(3, cardData.getCategory());
				pstm.setString(4, cardData.getUrl());
				pstm.addBatch();
				pstm.clearParameters();
			}
			pstm.executeBatch();
			dataList.clear();
			
		} catch(Exception e) {}
	}

}
