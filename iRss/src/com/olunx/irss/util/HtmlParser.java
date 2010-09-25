package com.olunx.irss.util;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class HtmlParser {

	private String fontColor;
	private String fontSize;
	
	public HtmlParser(String fontColor, String fontSize){
		this.fontColor = fontColor;
		this.fontSize = fontSize;
	}
	
	/**
	 * 处理各个节点的样式
	 * 
	 * @param list
	 * @return
	 */
	private NodeList parseNodes(NodeList list) {
		if (list == null)
			return null;

		NodeList nodes = new NodeList();
		Node node = null;
		SimpleNodeIterator iterator = list.elements();

		while (iterator.hasMoreNodes()) {
			node = iterator.nextNode();
			if (node == null)
				break;

			// 处理各种包含样式的节点
			if (node instanceof BodyTag) {// span
				BodyTag body = (BodyTag) node;
				body.setAttribute("style", "color:" + fontColor + ";font-size:" + fontSize);
			} else if (node instanceof ImageTag) {// img
//				ImageTag img = (ImageTag) node;
//				img.setImageURL("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
			} else if (node instanceof ParagraphTag) {// p
				ParagraphTag p = (ParagraphTag) node;
				p.setAttribute("style", "color:" + fontColor + ";font-size:" + fontSize);
			} else if (node instanceof Div) {// div
				Div div = (Div) node;
				div.setAttribute("style", "color:" + fontColor + ";font-size:" + fontSize);
			}

			parseNodes(node.getChildren());// 处理子节点
			nodes.add(node);
		}

		return nodes;
	}

	/**
	 * @throws ParserException
	 */
	public String parseHtml(String html){
		System.out.println("process html");
		NodeList list = null;
		try {
			Parser parser = new Parser(html);
//			parser.setURL("html/feed.html");
//			parser.setInputHTML(html);
			list = parser.parse(null);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		
		return parseNodes(list).toHtml();
	}
}
