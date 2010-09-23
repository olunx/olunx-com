import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.Span;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.NodeVisitor;
import org.htmlparser.visitors.ObjectFindingVisitor;

public class Test {

	/**
	 * @param args
	 * @throws ParserException
	 */
	public static void main(String[] args) throws ParserException {
		// TODO Auto-generated method stub
		// testImageVisitor();
		// new Test().testTagVisitor();
		new Test().parse();
		// parse();
	}

	public static void testImageVisitor() {
		try {
			ImageTag imgLink;
			ObjectFindingVisitor visitor = new ObjectFindingVisitor(ImageTag.class);
			Parser parser = new Parser();
			parser.setURL("http://olunx.com/feed");
			parser.setEncoding(parser.getEncoding());
			parser.visitAllNodesWith(visitor);
			Node[] nodes = visitor.getTags();
			for (int i = 0; i < nodes.length; i++) {
				imgLink = (ImageTag) nodes[i];
				imgLink.setImageURL("custom Url");
				System.out.println("testImageVisitor() ImageURL = " + imgLink.getImageURL());
				System.out.println("testImageVisitor() ImageLocation = " + imgLink.extractImageLocn());
				System.out.println("testImageVisitor() SRC = " + imgLink.getAttribute("SRC"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 测试对指定Tag的NodeVisitor的用法
	 */
	public void testTagVisitor() {
		try {

			Parser parser = new Parser();
			parser.setURL("http://www.baidu.com/");
			NodeVisitor visitor = new MNodeVisitor();
			parser.visitAllNodesWith(visitor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MNodeVisitor extends NodeVisitor {
		public void visitTag(Tag tag) {

			if (tag instanceof ImageTag) {
				ImageTag img = (ImageTag) tag;
				img.setImageURL("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
				// System.out.println(img.toHtml());
			} else if (tag instanceof ParagraphTag) {
				ParagraphTag p = (ParagraphTag) tag;
				p.setAttribute("style", "none");
				// System.out.println("p style .......................... " +
				// p.getAttribute("style"));
				// System.out.println(p.toHtml());
			} else if (tag instanceof Div) {
				Div div = (Div) tag;
				div.setAttribute("style", "none");
				// System.out.println("div style .......................... " +
				// div.getAttribute("style"));
				// System.out.println(div.toHtml());
			}

			parseNodes(tag.getChildren());// 处理子节点

			System.out.println(tag.toHtml());// 打印结果
		}

		public void viisitEndTag(Tag tag) {
			System.out.println(tag.toHtml());// 打印结果
		}

	}

	/**
	 * 处理各个节点的样式
	 * 
	 * @param list
	 * @return
	 */
	public NodeList parseNodes(NodeList list) {
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
			if (node instanceof ImageTag) {// img
				ImageTag img = (ImageTag) node;
				img.setImageURL("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
			} else if (node instanceof ParagraphTag) {// p
				ParagraphTag p = (ParagraphTag) node;
				if (p.getAttribute("style") != null)
					p.setAttribute("style", "none");
			} else if (node instanceof Div) {// div
				Div div = (Div) node;
				if (div.getAttribute("style") != null)
					div.setAttribute("style", "none");
			} else if (node instanceof Span) {// span
				Span span = (Span) node;
				if (span.getAttribute("style") != null)
					span.setAttribute("style", "none");
			} else if (node instanceof TagNode) {// pre
				TagNode tag = (TagNode) node;
				if (tag.getAttribute("style") != null)
					tag.setAttribute("style", "none");
				// System.out.println(node.getClass() +
				// "=======================" + node.getText());
			}

			parseNodes(node.getChildren());// 处理子节点
			nodes.add(node);
		}

		return nodes;
	}

	public void parse() throws ParserException {
		Parser parser = new Parser();
		parser.setURL("html/feed.html");
		NodeList list = parser.parse(null);
		System.out.println(parseNodes(list).toHtml());
	}
}
