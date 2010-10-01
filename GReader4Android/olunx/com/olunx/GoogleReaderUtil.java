package com.olunx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.syndication.DateParser;

import be.lechtitseb.google.reader.api.model.item.Item;
import be.lechtitseb.google.reader.api.model.opml.Outline;

public class GoogleReaderUtil {

	/**
	 * 构建dom文档
	 * 
	 * @param in
	 * @return
	 */
	private static Document buildDoc(InputStream in) {
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbfactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = db.parse(in);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return doc;
	}

	public static ArrayList<Outline> parseOPMLSubscriptions(String xmlContent) {

		return parseOPMLSubscriptions(new ByteArrayInputStream(xmlContent.getBytes()));
	}

	/**
	 * 提取opml的条目
	 * 
	 * @param in
	 * @return
	 */
	public static ArrayList<Outline> parseOPMLSubscriptions(InputStream in) {

		Document doc = buildDoc(in);

		if (doc == null) {
			return null;
		}

		Element root = doc.getDocumentElement();

		if (root == null) {
			return null;
		}

		NodeList outline = root.getElementsByTagName("outline");// 获取body节点

		ArrayList<Outline> outlinesList = new ArrayList<Outline>();

		int outlineLength = outline.getLength();
		for (int i = 0; i < outlineLength; i++) {
			Node node = outline.item(i);// 获取每一个body

			Node parent = node.getParentNode();// 获取父亲节点
			if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
				Element parentEle = (Element) parent;
				if (!"".equals(parentEle.getAttribute("title"))) {// 判断父亲节点是否为空
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element nodeEle = (Element) node;
						Outline singleOutline = new Outline();
						singleOutline.setTitle(nodeEle.getAttribute("title"));
						singleOutline.setText(nodeEle.getAttribute("text"));
						singleOutline.setXmlUrl(nodeEle.getAttribute("xmlUrl"));
						singleOutline.setHtmlUrl(nodeEle.getAttribute("htmlUrl"));
						singleOutline.setCategory(parentEle.getAttribute("title"));
						outlinesList.add(singleOutline);
						continue;
					}
				}
			}

			// 处理没有分类的情况
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element nodeEle = (Element) node;
				if (!"".equals(nodeEle.getAttribute("xmlUrl"))) {
					Outline singleOutline = new Outline();
					singleOutline.setTitle(nodeEle.getAttribute("title"));
					singleOutline.setText(nodeEle.getAttribute("text"));
					singleOutline.setXmlUrl(nodeEle.getAttribute("xmlUrl"));
					singleOutline.setHtmlUrl(nodeEle.getAttribute("htmlUrl"));
					singleOutline.setCategory("未分类");
					outlinesList.add(singleOutline);
				}
			}
		}

		return outlinesList;
	}

	public static ArrayList<Item> parseFeedContent(String xmlContent){
		return parseFeedContent(new InputSource(new ByteArrayInputStream(xmlContent.getBytes())));
	}
	
	/**
	 * 用sax解析文章数据
	 * 
	 * @param in
	 * @return
	 */
	public static ArrayList<Item> parseFeedContent(InputSource in) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = null;
		XMLReader reader = null;
		FeedHandler handler = new FeedHandler();

		try {
			parser = factory.newSAXParser();
			reader = parser.getXMLReader();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		}

		reader.setContentHandler(handler);

		try {
			reader.parse(in);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		return handler.getItems();
	}

}

class FeedHandler extends DefaultHandler {

	private ArrayList<Item> items = new ArrayList<Item>();
	private StringBuilder titleBuilder;
	private StringBuilder linkBuilder;
	private StringBuilder publishedBuilder;
	private StringBuilder updatedBuilder;
	private StringBuilder contentBuilder;
	private StringBuilder summaryBuilder;
	private boolean inEntry = false;
	private boolean inSource = false;
	private boolean title = false;
	private boolean link = false;
	private boolean published = false;
	private boolean updated = false;
	private boolean content = false;
	private boolean summary = false;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String tagName = localName.length() != 0 ? localName : qName;
		tagName = tagName.toLowerCase().trim();
		if (tagName.equals("entry")) {
			inEntry = true;
			titleBuilder = new StringBuilder();
			linkBuilder = new StringBuilder();
			publishedBuilder = new StringBuilder();
			updatedBuilder = new StringBuilder();
			contentBuilder = new StringBuilder();
			summaryBuilder = new StringBuilder();
		}
		if (inEntry) {
			if (tagName.equals("title")) {
				title = true;
			} else if (tagName.equals("link")) {
				link = true;
			} else if (tagName.equals("published")) {
				published = true;
			} else if (tagName.equals("updated")) {
				updated = true;
			} else if (tagName.equals("content")) {
				content = true;
			} else if (tagName.equals("summary")) {
				summary = true;
			}
		}

		// 排除source里面的标签
		if (tagName.equals("source")) {
			inSource = true;
		}
		if (inSource) {
			if (tagName.equals("title")) {
				title = false;
			} else if (tagName.equals("link")) {
				link = false;
			}
		}

		// 链接
		if (link) {
			linkBuilder.append(attributes.getValue("href"));
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String tagName = localName.length() != 0 ? localName : qName;
		tagName = tagName.toLowerCase().trim();
		if (tagName.equals("entry")) {
			inEntry = false;
			Item item = new Item();
			item.setTitle(titleBuilder.toString());
			item.setUrl(linkBuilder.toString());
			item.setPublishedOn(DateParser.parseDate(publishedBuilder.toString()));
			item.setUpdatedOn(DateParser.parseDate(updatedBuilder.toString()));
			item.setContent(contentBuilder.toString());
			item.setContentTextDirection(summaryBuilder.toString());
			items.add(item);
		}
		if (inEntry) {
			if (tagName.equals("title")) {
				title = false;
			} else if (tagName.equals("link")) {
				link = false;
			} else if (tagName.equals("published")) {
				published = false;
			} else if (tagName.equals("updated")) {
				updated = false;
			} else if (tagName.equals("content")) {
				content = false;
			} else if (tagName.equals("summary")) {
				summary = false;
			}
		}

		// 排除source里面的标签
		if (tagName.equals("source")) {
			inSource = false;
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// System.out.print(new String(ch, start, length));
		if (title) {
			titleBuilder.append(new String(ch, start, length));
		} else if (published) {
			publishedBuilder.append(new String(ch, start, length));
		} else if (updated) {
			updatedBuilder.append(new String(ch, start, length));
		} else if (content) {
			contentBuilder.append(new String(ch, start, length));
		} else if (summary) {
			summaryBuilder.append(new String(ch, start, length));
		}
	}

	public ArrayList<Item> getItems() {
		return items;
	}

}
