package com.olunx.irss.util;

import java.io.File;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import android.util.Log;

import com.olunx.irss.db.ImagesHelper;

public class HtmlParser {

	private String fontColor;
	private String fontSize;
	private boolean isOfflineReadMode = false;
	private Map<String, String> imageMap;
	private final String TAG = "com.olunx.irss.util.HtmlParser";
	
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
				if(isOfflineReadMode) {
					Log.i(TAG, "replace offline image");
					ImageTag img = (ImageTag) node;
					String localUrl = imageMap.get(img.getImageURL());
					if(localUrl != null && !localUrl.equals("")){
//						localUrl = "file://" + localUrl;
						localUrl = localUrl.substring(localUrl.lastIndexOf(File.separator) + 1, localUrl.length());
						Log.i(TAG, localUrl);
						img.setImageURL(localUrl);
						Log.i(TAG, img.toHtml());
					}
					
				}
				
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
	public String parseHtml(String articleId, String html){
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
		isOfflineReadMode = Config.init().isOffLineReadMode();
		if(isOfflineReadMode) {
			ImagesHelper helper = new ImagesHelper();
			imageMap = helper.getImagesMapByArticleId(articleId);
			helper.close();
		}
		return parseNodes(list).toHtml();
	}
	
}
