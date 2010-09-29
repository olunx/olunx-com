package com.olunx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.syndication.DateParser;

import be.lechtitseb.google.reader.api.model.item.Item;
import be.lechtitseb.google.reader.api.model.opml.Outline;

public class GoogleReaderUtil {

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
		
		for (int i = 0; i < outline.getLength(); i++) {
			Node node = outline.item(i);// 获取每一个body
			
			Node parent = node.getParentNode();//获取父亲节点
			if(parent!= null && parent.getNodeType() == Node.ELEMENT_NODE) {
				Element parentEle = (Element) parent;
				if(!"".equals(parentEle.getAttribute("title"))){//判断父亲节点是否为空
					if(node.getNodeType() == Node.ELEMENT_NODE) {
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
			
			//处理没有分类的情况
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element nodeEle = (Element) node;
				if(!"".equals(nodeEle.getAttribute("xmlUrl"))) {
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

	public static ArrayList<Item> parseFeedContent(String articleContent) {
		return parseFeedContent(new ByteArrayInputStream(articleContent.getBytes()));
	}

	public static ArrayList<Item> parseFeedContent(InputStream in) {
		Document doc = buildDoc(in);

		if (doc == null) {
			return null;
		}

		Element root = doc.getDocumentElement();

		if (root == null) {
			return null;
		}

		NodeList entries = root.getElementsByTagName("entry");// 获取所有entry节点

		ArrayList<Item> items = new ArrayList<Item>();

		for (int i = 0; i < entries.getLength(); i++) {
			Item item = new Item();
			Element entryNode = (Element) entries.item(i);// 获取每一个entry节点
			NodeList children = entryNode.getChildNodes();// 获取entry下的所有节点

			for (int j = 0; j < children.getLength(); j++) {
				Node node = children.item(j);// 获取独立的节点，如title、link、content、published、updated、author
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element childNode = (Element) node;
					if ("title".equals(childNode.getNodeName())) {
						item.setTitle(childNode.getFirstChild().getNodeValue());
					} else if ("link".equals(childNode.getNodeName())) {
						item.setUrl(childNode.getAttribute("href"));
					} else if ("content".equals(childNode.getNodeName())) {
						item.setContent(childNode.getFirstChild().getNodeValue());
						System.out.println("content:" + item.getContent());
					} else if ("description".equals(childNode.getNodeName())) {
						item.setContentTextDirection(childNode.getChildNodes().item(0).getFirstChild().getNodeValue());
					} else if ("published".equals(childNode.getNodeName())) {
						 item.setPublishedOn(DateParser.parseDate(childNode.getFirstChild().getNodeValue()));
					} else if ("updated".equals(childNode.getNodeName())) {
						 item.setUpdatedOn(DateParser.parseDate(childNode.getFirstChild().getNodeValue()));
					} else if ("author".equals(childNode.getNodeName())) {
						item.setAuthor(childNode.getChildNodes().item(0).getFirstChild().getNodeValue());
					}
				}
			}
			items.add(item);
		}

		return items;
	}
}
