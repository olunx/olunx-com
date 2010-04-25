package com.olunx.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class FetchWord {

//	private String API_URL = "http://dict-co.iciba.com/api/dictionary.php";
//	private String WORD_KEY = "w";
//	private String REQUEST_MOTHOD = "GET";
//	private String CHARSET = "utf-8";
//
//	private String WORD_TAG = "key";// 单词
//	private String PHONETICS_TAG = "ps";// 音标
//	private String POS_TAG = "pos";// 词性
//	private String TRANS_TAG = "acceptation";// 解释
//	private String PRON_TAG = "pron";// 发音
//	private String SENT_TAG = "sent";// 短语
//	private String SENT_ORIG_TAG = "orig";// 短语内容
//	private String SENT_TRANS_TAG = "trans";// 短语解释
	
	private String API_URL = "http://dict.cn/ws.php";
	private String WORD_KEY = "q";
	private String REQUEST_MOTHOD = "GET";
	private String CHARSET = "gb2312";

	private String WORD_TAG = "key";// 单词
	private String PHONETICS_TAG = "pron";// 音标
	private String POS_TAG = "null";// 词性
	private String TRANS_TAG = "def";// 解释
	private String PRON_TAG = "audio";// 发音
	private String SENT_TAG = "sent";// 短语
	private String SENT_ORIG_TAG = "orig";// 短语内容
	private String SENT_TRANS_TAG = "trans";// 短语解释

	private Word word = null;

	public Word getWord(String keyword) {
		
		//结果
		word = new Word();
		
		String urlStr = this.API_URL + "?" + WORD_KEY + "=" + keyword;
		urlStr = urlStr.replace(" ", "%20");//处理空格
		
		URL url = null;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(this.REQUEST_MOTHOD);
			conn.connect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}catch (IOException e) {
			e.printStackTrace();
		}

		InputStream inputStream = null;
		try {
			inputStream = conn.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//返回
		if(inputStream == null) {
			return null;
		}
		
		Reader reader = null;
		try {
			reader = new InputStreamReader(inputStream, this.CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		InputSource inputSource = new InputSource(reader);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		Document doc = null;
		try {
			doc = builder.parse(inputSource);
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// root
		Element root = doc.getDocumentElement();
		if (root == null) {
			return null;
		}
		if (!root.hasChildNodes()) {
			return null;
		}
//		System.out.println(root.getNodeName());

		// 记录数据
		NodeList nodes = null;
		int length = 0;

		// 单词
		nodes = doc.getElementsByTagName(this.WORD_TAG);
		length = nodes.getLength();
		for (int i = 0; i < length; i++) {
//			System.out.println(nodes.item(i).getTextContent());
			word.setWord(nodes.item(i).getChildNodes().item(0).getNodeValue());
		}

		// 音标
		nodes = doc.getElementsByTagName(this.PHONETICS_TAG);
		length = nodes.getLength();
		for (int i = 0; i < length; i++) {
//			System.out.println(nodes.item(i).getTextContent());
			word.setPhonetic(nodes.item(i).getChildNodes().item(0).getNodeValue());
		}

		// 词性，多个词性对应多个解释。
		NodeList posList = doc.getElementsByTagName(this.POS_TAG);
		if (posList != null && posList.getLength() > 0) {
			nodes = doc.getElementsByTagName(this.TRANS_TAG);
			length = nodes.getLength();
			StringBuffer posStr = new StringBuffer();
			if (posList.getLength() == length) {
				for (int i = 0; i < length; i++) {
//					System.out.println(posList.item(i).getTextContent() + nodes.item(i).getTextContent());
					posStr.append(posList.item(i).getChildNodes().item(0).getNodeValue() + nodes.item(i).getChildNodes().item(0).getNodeValue() + "\n");
				}
			}
			word.setTranslation(posStr.toString());
		} else {
			// 解释
			nodes = doc.getElementsByTagName(this.TRANS_TAG);
			length = nodes.getLength();
			for (int i = 0; i < length; i++) {
//				System.out.println(nodes.item(i).getTextContent());
				word.setTranslation(nodes.item(i).getChildNodes().item(0).getNodeValue());
			}
		}

		// 发音
		nodes = doc.getElementsByTagName(this.PRON_TAG);
		length = nodes.getLength();
		for (int i = 0; i < length; i++) {
//			System.out.println(nodes.item(i).getTextContent());
			word.setPronounce(nodes.item(i).getChildNodes().item(0).getNodeValue());
		}
		
		// 短语
		nodes = doc.getElementsByTagName(this.SENT_TAG);
		length = nodes.getLength();
		Element sent = null;
		NodeList orig = null;
		int origLen = 0;
		NodeList trans = null;
		int transLen = 0;
		StringBuffer sentStr = new StringBuffer();
		for (int i = 0; i < length; i++) {
			// System.out.println(nodes.item(i).getTextContent());
			sent = (Element) nodes.item(i);
			orig = sent.getElementsByTagName(this.SENT_ORIG_TAG);
			origLen = orig.getLength();
			trans = sent.getElementsByTagName(this.SENT_TRANS_TAG);
			transLen = trans.getLength();
			if (origLen == transLen) {
				for (int j = 0; j < origLen; j++) {
//					System.out.println(orig.item(j).getTextContent() + "\n" + trans.item(j).getTextContent());
					sentStr.append(orig.item(j).getChildNodes().item(0).getNodeValue() + "\n" + trans.item(j).getChildNodes().item(0).getNodeValue() + "\n\n");
				}
			}
		}
		word.setSentences(sentStr.toString());
		
		return word;
	}

}
