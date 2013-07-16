package com.james;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.RegexFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.util.NodeList;

import com.james.model.result;
import com.james.util.HibernateUtil;

public class GetTotoResults {

	public static String sgPoolUrl = "http://www.singaporepools.com.sg/Lottery?page=wc10_toto_past&drawNo=";
	// 2544 - 2856
	// June 2013

	public static String myFreePostUrl = "http://sg.myfreepost.com/sgTOTO_get.php";
	// ?dMonth=06&dYear=2013 for dates
	// ?drawdate=13-Jun-2013(Thu) for the results

	public static int startNum = 2544;
	public static int endNum = 2856;

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
	public static SimpleDateFormat myFreePostDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

	private static Logger log = Logger.getLogger(GetTotoResults.class);

	// From Singapore Pools
	public static void getFromSgPools(int drawNo) {
		Session session = null;
		try {
			Parser parser = new Parser(sgPoolUrl + drawNo);

			List<NodeFilter> filters = new ArrayList<NodeFilter>();
			filters.add(new TagNameFilter("td"));
			filters.add(new HasAttributeFilter("class", "winning_numbers_toto_b"));
			AndFilter resultFilter = new AndFilter(filters.toArray(new NodeFilter[0]));

			filters = new ArrayList<NodeFilter>();
			filters.add(new TagNameFilter("span"));
			filters.add(new HasAttributeFilter("class", "normal10"));
			AndFilter dateFilter = new AndFilter(filters.toArray(new NodeFilter[0]));

			Date drawDate = null;
			String dateText = "";
			try {
				NodeList spanlist = parser.parse(dateFilter);
				Node dateNode = spanlist.elementAt(1);
				dateText = dateNode.getFirstChild().getText().trim();
				drawDate = dateFormat.parse(dateText);
			} catch (ParseException e) {
				log.error(e);
				log.error("Not able to parse date : " + drawNo + " : " + dateText);
			}

			parser.reset();
			NodeList list = parser.parse(resultFilter);

			String intText = "";
			try {
				session = HibernateUtil.getSessionFactory().getCurrentSession();
				session.beginTransaction();

				for (Node node : list.toNodeArray()) {
					result aResult = new result();
					aResult.setDate(drawDate);
					aResult.setDrawno(drawNo);
					aResult.setNum(Integer.parseInt(node.getFirstChild().getText().trim()));
					session.save(aResult);
				}
				session.getTransaction().commit();
			} catch (NumberFormatException e) {
				log.error(e);
				log.error("Not able to parse integer ");
				if (session != null && session.isOpen())
					session.getTransaction().rollback();
			}
		} catch (Exception e) {
			if (session != null && session.isOpen())
				session.getTransaction().rollback();
			e.printStackTrace();
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

	}

	public static void getFromMyFreePost(String link) {

		try {

			Matcher matcher = Pattern.compile("(\\d{2}\\-\\D{3}\\-\\d{4})").matcher(link);
			String matched = null;
			if (matcher.find()) {
				matched = matcher.group();
			}

			Date matchedDate = myFreePostDateFormat.parse(matched);

			Parser parser = new Parser(link);

			NodeList indexNodes = parser.extractAllNodesThatMatch(new RegexFilter("^\\d{3,4}$"));

			int drawno = 0;
			// Getting the draw no
			for (Node indexNode : indexNodes.toNodeArray()) {
				if (indexNode.getParent().getClass().equals(TableColumn.class)) {
					TableColumn parentColumn = ((TableColumn) indexNode.getParent());
					if (parentColumn.getAttribute("class") != null
							&& parentColumn.getAttribute("class").equals("topprizeresult")) {
						log.info(parentColumn.toHtml());
						drawno = Integer.parseInt(indexNode.getText());
						break;
					}
				}

			}

			if (drawno != 0) {
				parser.reset();

				OrFilter resultFilter = new OrFilter(new HasAttributeFilter("class", "auspfont"),
						new HasAttributeFilter("class", "inauspfont"));
				NodeList tdNodeList = parser.parse(resultFilter);
				Session session = HibernateUtil.getSessionFactory().getCurrentSession();
				try {
					session.beginTransaction();

					for (Node resultNode : tdNodeList.toNodeArray()) {
						resultNode.getFirstChild().getText();
						result aResult = new result();
						aResult.setDate(matchedDate);
						aResult.setDrawno(drawno);
						aResult.setNum(new Integer(resultNode.getFirstChild().getText()));
						session.save(aResult);
					}

					session.getTransaction().commit();
				} catch (Exception e) {
					log.error(e);
					if (session != null && session.isOpen()) {
						session.getTransaction().rollback();
					}
				} finally {
					if (session != null && session.isOpen()) {
						session.close();
					}
				}

			}

		} catch (ParseException ex) {
			log.error("Cannot Parse the date");

		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}

	}
	public static List<String> getDatesFromMyFreePost(int month, int year) {
		List<String> links = new ArrayList<String>();

		try {
			log.info(myFreePostUrl + String.format("?dMonth=%1$02d&dYear=%2$d", month, year));
			Parser parser = new Parser(myFreePostUrl
					+ String.format("?dMonth=%1$02d&dYear=%2$d", month, year));

			NodeList anchorList = parser.parse(new LinkRegexFilter(
					"sgtoto_get.php\\?drawdate(.*)$", false));

			for (Node anchorNode : anchorList.toNodeArray()) {
				if (anchorNode.getClass().equals(LinkTag.class)) {
					links.add(((LinkTag) anchorNode).getLink());
				}
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
		return links;
	}

	public static void main(String[] args) {

		// Get From Singapore Pools
		// for (int index = startNum; index <= 2856; index++) {
		// getFromSgPools(index);
		// }

		for (int month = 1; month <= 12; month++) {
			for (int year = 2001 ; year <= 2005; year++) {
				List<String> resultLinks = getDatesFromMyFreePost(month, year);
				for (String link : resultLinks) {
					getFromMyFreePost(link);
				}
			}
		}

	}
}
