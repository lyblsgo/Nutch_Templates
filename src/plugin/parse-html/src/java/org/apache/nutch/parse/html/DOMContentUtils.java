/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.parse.html;

import java.net.URL;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.regex.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.NodeWalker;
import org.apache.nutch.util.NutchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;

import org.w3c.dom.*;

/**
 * A collection of methods for extracting content from DOM trees.
 * 
 * This class holds a few utility methods for pulling content out of DOM nodes,
 * such as getOutlinks, getText, etc.
 * 
 */
public class DOMContentUtils {

	public static class LinkParams {
		public String elName;
		public String attrName;
		public int childLen;

		public LinkParams(String elName, String attrName, int childLen) {
			this.elName = elName;
			this.attrName = attrName;
			this.childLen = childLen;
		}

		public String toString() {
			return "LP[el=" + elName + ",attr=" + attrName + ",len=" + childLen
					+ "]";
		}
	}
	public static final Logger LOG = LoggerFactory
			.getLogger("org.apache.nutch.parse.html");
	private HashMap<String, LinkParams> linkParams = new HashMap<String, LinkParams>();

	public DOMContentUtils(Configuration conf) {
		setConf(conf);
	}

	/**
	 * my code about set configuration of the template
	 */
	private String template_Value []= null;
	private String template_Name = null;
	private String temp_sb ;
	
	// 偶数为正则表达式，对应奇数为模板名称
//	private String templateBlock[] = {
//			new String("http://people.mtime.com/[0-9]{6}/awards.html"),
//			new String("template.mtime.awardsofpeople"),
//			new String(
//					"http://movie.mtime.com/[0-9]{6}/reviews/short/new(-[0-9]+)?.html"),
//			new String("template.mtime.shortcomments"),
//			new String("http://movie.mtime.com/[0-9]{6}/comment(-[0-9]+)?.html"),
//			new String("template.mtime.listoflongcomments"),
//			new String("http://movie.mtime.com/[0-9]{6}/reviews/[0-9]{7}.html"),
//			new String("template.mtime.detailsoflongcomments"),
//			new String(
//					"http://(movie|people).mtime.com/[0-9]{6}/news(-[0-9]+)?.html"),
//			new String("template.mtime.listofnews"),
//			new String(
//					"http://news.mtime.com/[0-9]{4}/[0-9]{2}/[0-9]{2}/[0-9]{7}.html"),
//			new String("template.mtime.detailsofnews"),
//			new String(
//					"http://people.mtime.com/[0-9]{6}/filmographies/(#pageIndex=[0-9]+)?"),
//			new String("template.mtime.filmographiesofpeople"),
//			new String("http://people.mtime.com/[0-9]{6}/details.html"),
//			new String("template.mtime.detailsofpeople"), 
//			new String("http://www.imdb.com/title/tt[0-9]{7}/\\?ref_=inth_ov_tt"),
//			new String("template.imdb.scanofnewmovies"),};
	private String regexsBlock[]=null;
	
	private String bigTemplate=null;
	private String regexs=null;
	private String choseTemplate[] = {
			new String("1905"),new String("imdb"),new String("mtime"),new String("douban"),
	};
	private void choseTemplate(String url){
		for(int i=0;i< choseTemplate.length;i++ ){
			if(url.indexOf(choseTemplate[i])!=-1){
				bigTemplate="./TemplatesForWebsites/Templates_"+choseTemplate[i]+".xml";
				break;
			}
		}
	}	
	private void choseRegexs(String url){
		for(int i=0;i< choseTemplate.length;i++ ){
			if(url.indexOf(choseTemplate[i])!=-1){
				regexs="./RegexsForWebsites/Regexs_"+choseTemplate[i]+".xml";
				break;
			}
		}
	}

	public void setConf(Configuration conf) {
		// forceTags is used to override configurable tag ignoring, later on
		Collection<String> forceTags = new ArrayList<String>(1);

		linkParams.clear();
		linkParams.put("a", new LinkParams("a", "href", 1));
		linkParams.put("area", new LinkParams("area", "href", 0));
		if (conf.getBoolean("parser.html.form.use_action", true)) {
			linkParams.put("form", new LinkParams("form", "action", 1));
			if (conf.get("parser.html.form.use_action") != null)
				forceTags.add("form");
		}
		linkParams.put("frame", new LinkParams("frame", "src", 0));
		linkParams.put("iframe", new LinkParams("iframe", "src", 0));
		linkParams.put("script", new LinkParams("script", "src", 0));
		linkParams.put("link", new LinkParams("link", "href", 0));
		linkParams.put("img", new LinkParams("img", "src", 0));

		// remove unwanted link tags from the linkParams map
		String[] ignoreTags = conf
				.getStrings("parser.html.outlinks.ignore_tags");
		for (int i = 0; ignoreTags != null && i < ignoreTags.length; i++) {
			if (!forceTags.contains(ignoreTags[i]))
				linkParams.remove(ignoreTags[i]);
		}

		// Map<String, String> tmp = conf.getValByRegex("write*");
		// for(String key : tmp.keySet()){
		// System.err.println("key = "+ key + "and value = " + tmp.get(key));
		// }
		// templateFor_MTimeShortComments=conf.get("template.mtime.shortcomment");

	}

	/**
	 * This method takes a {@link StringBuilder} and a DOM {@link Node}, and
	 * will append all the content text found beneath the DOM node to the
	 * <code>StringBuilder</code>.
	 * 
	 * <p>
	 * 
	 * If <code>abortOnNestedAnchors</code> is true, DOM traversal will be
	 * aborted and the <code>StringBuffer</code> will not contain any text
	 * encountered after a nested anchor is found.
	 * 
	 * <p>
	 * 
	 * @return true if nested anchors were found
	 */
	public boolean getText(StringBuilder sb, Node node,
			boolean abortOnNestedAnchors) {
		if (getTextHelper(sb, node, abortOnNestedAnchors, 0)) {
			return true;
		}
		return false;
	}

	/**
	 * This is a convinience method, equivalent to
	 * {@link #getText(StringBuffer,Node,boolean) getText(sb, node, false)}.
	 * 
	 */
	public void getText(StringBuilder sb, Node node) {
		getText(sb, node, false);
	}

	// returns true if abortOnNestedAnchors is true and we find nested
	// anchors
	private boolean getTextHelper(StringBuilder sb, Node node,
			boolean abortOnNestedAnchors, int anchorDepth) {
		boolean abort = false;
		NodeWalker walker = new NodeWalker(node);

		while (walker.hasNext()) {

			Node currentNode = walker.nextNode();
			String nodeName = currentNode.getNodeName();
			short nodeType = currentNode.getNodeType();

			if ("script".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if ("style".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if (abortOnNestedAnchors && "a".equalsIgnoreCase(nodeName)) {
				anchorDepth++;
				if (anchorDepth > 1) {
					abort = true;
					break;
				}
			}
			if (nodeType == Node.COMMENT_NODE) {
				walker.skipChildren();
			}
			if (nodeType == Node.TEXT_NODE) {
				// cleanup and trim the value
				String text = currentNode.getNodeValue();
				text = text.replaceAll("\\s+", " ");
				text = text.trim();
				if (text.length() > 0) {
					if (sb.length() > 0)
						sb.append(' ');
					sb.append(text);
				}
			}
		}

		return abort;
	}
	
	/**
	 * my code about bytebuffer to String
	 */
	
	public static String byteBufferToString(ByteBuffer buffer) {
		CharBuffer charBuffer = null;
		try {
			Charset charset = Charset.forName("UTF-8");
			CharsetDecoder decoder = charset.newDecoder();
			charBuffer = decoder.decode(buffer);
			buffer.flip();
			return charBuffer.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * my code about extracting informations through template of the pages
	 */
	private void setMyconf(Configuration myconf) {
		if (template_Name != null)
			template_Value = myconf.getStrings(template_Name);
	}

	private void mySetupAboutTemplates(String url) {
		Configuration myconf = NutchConfiguration.create();
		choseTemplate(url);
		if (bigTemplate != null) {
			myconf.addResource(bigTemplate);
		}
		setMyconf(myconf);
	}
	private void mySetupAboutRegexs(String url) {
		Configuration myconf_R = NutchConfiguration.create();
		choseRegexs(url);
		if (regexs!= null) {
			myconf_R.addResource(regexs);
			regexsBlock =myconf_R.getStrings("regular.expression");
		}
	}

	public boolean getText_DOM(DocumentFragment doc,
			boolean abortOnNestedAnchors, String url,Parse parse,WebPage page) {
		if (getTextHelper_DOM(doc, abortOnNestedAnchors, 0, url,parse,page)) {
			return true;
		}
		return false;
	}

	/**
	 * This is a convinience method, equivalent to
	 * {@link #getText(StringBuffer,Node,boolean) getText_DOM(sb, node, false)}.
	 * 
	 */
	public void getText_DOM(DocumentFragment doc, String url,Parse parse,WebPage page) {
		getText_DOM(doc, false, url,parse,page);
	}

	// returns true if abortOnNestedAnchors is true and we find nested
	// anchors
	private boolean getTextHelper_DOM(DocumentFragment doc,
			boolean abortOnNestedAnchors, int anchorDepth, String url,Parse parse,WebPage page) {
		boolean abort = false;
		NodeWalker walker = new NodeWalker(doc);

		while (walker.hasNext()) {

			Node currentNode = walker.nextNode();
			String nodeName = currentNode.getNodeName();

			if ("script".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if ("style".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if (abortOnNestedAnchors && "a".equalsIgnoreCase(nodeName)) {
				anchorDepth++;
				if (anchorDepth > 1) {
					abort = true;
					break;
				}
			}
		}		
		
	    template_Value = null;
	    template_Name = null;
		temp_sb =null;
		
		
		regexsBlock=null;		
		bigTemplate=null;
		regexs=null;
		
		mySetupAboutRegexs(url);
		if (regexsBlock != null) {
			for(int i=0;i<regexsBlock.length;i++){
				regexsBlock[i]=regexsBlock[i].replaceAll("\\s+", "");
				regexsBlock[i]=regexsBlock[i].replaceAll("\n+", "");
			}

			for (int t = 1; t < regexsBlock.length; t += 2) {
				Pattern p = Pattern.compile(regexsBlock[t-1]);
				Matcher m = p.matcher(url);
				if (m.find()) {
					template_Name = regexsBlock[t];
					break;
				}
			}
		}
		mySetupAboutTemplates(url);

		/**
		 * 微影评     ：http://movie.mtime.com/217723/reviews/short/new.html    ---//H3|//P[@class='px14']/A|//P[@class='mt6 px12 clearfix']|//P/SPAN[@class='db_point ml6']|//DIV[@class='mt10']/A
		 * 长影评列表  ：http://movie.mtime.com/217723/comment.html              ---//P[@class='db_enname']|//H1[@property='v:itemreviewed']/A|//P[@class='db_year']|//DD[@class='comboxcont']/DIV/H3/A|//DD//P[@class='mt20']|//DIV[@class='pic_58']/P/A|//SPAN[@class='db_point ml12']
		 * 长影评     ：http://movie.mtime.com/217723/reviews/7897160.html      ---//P[@class='db_position']|//DIV[@class='db_mediacont db_commentcont']/P
		 * 新闻资讯列表：http://movie/people.mtime.com/217723/news.html          ---//DIV[@class='db_newscont']/H3/A|//DIV[@class='db_newscont']//P[@class='c_94 mt9 px12']|//DIV[@class='db_newscont']/P[@class='db_newsinfo']
		 * 新闻内容   ：http://news.mtime.com/2015/05/15/1542632.html           ---//DIV[@class='newsheadtit']|//P[@class='mt15 ml25 newstime ']|//DIV[@class='newstext']/DIV
		 * Actor个人档案：http://people.mtime.com/924883/details.html           ---//DIV[@class='per_s_tit']|//DL[@class='per_info_cont']/DT|//DL[@class='per_info_cont']/DD|//DIV[@id='lblAllGraphy']/P|//DL[@class='per_relalist']/DT|//DIV[@id='lblAllGraphy']/P|//DL[@class='per_relalist']/DD/DIV|//DL[@class='per_relalist']/DD/P|//H3[@class='fl normal']
		 * Actor作品年表：http://people.mtime.com/924883/filmographies/         ---//DIV[@class='pic96']//H3|//DIV[@class='pic96']/P|//DIV[@class='pic96']/DIV/SPAN
		 * Actor荣誉成就：http://people.mtime.com/912602/awards.html            ---//DIV[@class='per_awardsbox']//DD|//DIV[@class='per_awardsbox']//H3|//DIV[@id='awardInfo_data']/DD/H3|//DIV[@id='awardInfo_data']/DD/DL/DT|//DIV[@id='awardInfo_data']/DD/DL/DD		
		 */
		
		//String template_Value[] = {new String("MovieAwards"),new String("//DIV[@id='awardInfo_data']")};
		if (template_Value != null) {
			try {
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				
				for (int tmpt = 1; tmpt < template_Value.length; tmpt = tmpt + 2) {
					// XPath语法介绍： http://w3school.com.cn/xpath/
					if (template_Value[tmpt] != null&&template_Value[tmpt]!="") {
						StringBuilder sbs =new StringBuilder();
						XPathExpression expr = xpath.compile(template_Value[tmpt]);

						NodeList nodes = (NodeList) expr.evaluate(doc,
								XPathConstants.NODESET);

						for (int i = 0; i < nodes.getLength(); i++) {
							Node currentNode = nodes.item(i);
							if (currentNode.hasChildNodes()) {
								NodeList nodeListText = currentNode
										.getChildNodes();
								for (int k = 0; k < nodeListText.getLength(); k++) {
									Node curNode_child = nodeListText.item(k);
									if (curNode_child.getNodeType() == Node.TEXT_NODE) {
										String text = curNode_child
												.getNodeValue();
										if (text != null) {
											text = text.replaceAll("\\s+", " ");
											text = text.trim();
											if (text.length() > 0) {
												sbs.append(text);
											}
										}
									} 
									temp_sb= "";
									getDeepTextOfNodes(curNode_child);
									sbs.append(temp_sb);
//									if(curNode_child.hasChildNodes()) {
//										if (curNode_child.getFirstChild()
//												.getNodeType() == Node.TEXT_NODE) {
//											String text = curNode_child
//													.getFirstChild()
//													.getNodeValue();
//											if (text != null) {
//												text = text.replaceAll("\\s+",
//														" ");
//												text = text.trim();
//												if (text.length() > 0) {
//													sbs.append(text);
//												}
//											}
//										}
//									}
								}
							} 
//							Node t=currentNode.getFirstChild();
//							String tt=t.getNodeValue().trim();
//							boolean b1=!currentNode.hasChildNodes();
//							boolean b2=currentNode.hasChildNodes();
//							boolean b3=currentNode.getFirstChild().getNodeType()==Node.TEXT_NODE;
//							boolean b4=currentNode.getFirstChild().getNodeValue().trim()=="";
							if(!currentNode.hasChildNodes()||currentNode.hasChildNodes()&&currentNode.getFirstChild().getNodeType()==Node.TEXT_NODE&&currentNode.getFirstChild().getNodeValue().trim().equalsIgnoreCase("")){
								NamedNodeMap attrs_cur = currentNode
										.getAttributes();
								for (int j = 0; j < attrs_cur.getLength(); j++) {
									Node attr = attrs_cur.item(j);
									String attrName = attr.getNodeName()
											.toLowerCase();
									if (attrName.equals("entertime")) {
										String text = attr.getNodeValue();
										if (text != null) {
											text = text.replaceAll("\\s+", " ");
											text = text.trim();
											if (text.length() > 0) {
												sbs.append(text);
											}
										}
									}
									if (attrName.equals("movieid")) {
										String text = attr.getNodeValue();
										if (text != null) {
											text = text.replaceAll("\\s+", " ");
											text = text.trim();
											if (text.length() > 0) {
												sbs.append(text);
											}
										}
									}
//									if (attrName.equals("href")) {
//										String text = attr.getNodeValue();
//										if (text != null) {
//											text = text.replaceAll("\\s+", " ");
//											text = text.trim();
//											if (text.length() > 0) {
//												if (sbs != null)
//													sbs.append(";");
//												sbs.append(text);
//											}
//										}
//									}
								}
							}

						}
						if(template_Value[tmpt-1]!=null){
								LOG.info("I am here");
							switch(template_Value[tmpt-1]){
							
							    case "MovieIdProfessional":parse.setMovieIdProfessional(new Utf8(sbs.toString())); break;
							    case "MovieIdIMDB":parse.setMovieIdIMDB(new Utf8(sbs.toString())); break;
							    case "MovieIdMtime":parse.setMovieIdMtime(new Utf8(sbs.toString())); break;
							    case "MovieIdDouban":parse.setMovieIdDouban(new Utf8(sbs.toString())); break;
							    case "MovieId1905":parse.setMovieId1905(new Utf8(sbs.toString())); break;
							    case "MovieChineseName":parse.setMovieChineseName(new Utf8(sbs.toString())); break;
							    case "MovieEnglishName":parse.setMovieEnglishName(new Utf8(sbs.toString())); break;
							    case "MovieShortName":parse.setMovieShortName(new Utf8(sbs.toString())); break;
							    case "MovieAliasName":parse.setMovieAliasName(new Utf8(sbs.toString())); break;
							    case "Director":parse.setDirector(new Utf8(sbs.toString())); break;
							    case "Writer":parse.setWriter(new Utf8(sbs.toString())); break;
							    case "Actor":parse.setActor(new Utf8(sbs.toString())); break;
							    case "Producer":parse.setProducer(new Utf8(sbs.toString())); break; 
							    case "Cinematography":parse.setCinematography(new Utf8(sbs.toString())); break;
							    case "Cutter":parse.setCutter(new Utf8(sbs.toString())); break;
							    case "Wardrobe":parse.setWardrobe(new Utf8(sbs.toString())); break;
							    case "VisualAffectArtist":parse.setVisualAffectArtist(new Utf8(sbs.toString())); break;
							    case "AssociateDirector":parse.setAssociateDirector(new Utf8(sbs.toString())); break;
							    case "MovieType":parse.setMovieType(new Utf8(sbs.toString())); break;
							    case "PublishCompany":parse.setPublishCompany(new Utf8(sbs.toString())); break;
							    case "ProduceCompany":parse.setProduceCompany(new Utf8(sbs.toString())); break;
							    case "DistributionInformation":parse.setDistributionInformation(new Utf8(sbs.toString())); break;
							    case "PlotIntroduction":parse.setPlotIntroduction(new Utf8(sbs.toString())); break;
							    case "RoleIntroduction":parse.setRoleIntroduction(new Utf8(sbs.toString())); break;
							    case "MovieLongComment":parse.setMovieLongComment(new Utf8(sbs.toString())); break;
							    case "MovieShortComment":parse.setMovieShortComment(new Utf8(sbs.toString())); break;
							    case "MovieWeiboComment":parse.setMovieWeiboComment(new Utf8(sbs.toString())); break;
							    case "MovieNews":parse.setMovieNews(new Utf8(sbs.toString())); break;
							    case "MovieScoreIMDB":parse.setMovieScoreIMDB(Float.parseFloat(sbs.toString())); break;
							    case "MovieScoreMtime":parse.setMovieScoreMtime(Float.parseFloat(sbs.toString())); break;
							    case "MovieScoreDouban":parse.setMovieScoreDouban(Float.parseFloat(sbs.toString())); break;
							    case "MovieScore1905":parse.setMovieScore1905(Float.parseFloat(sbs.toString())); break;
							    case "MovieAwards":parse.setMovieAwards(new Utf8(sbs.toString())); break;
							    case "CinemaChineseName":parse.setCinemaChineseName(new Utf8(sbs.toString()));break;
							    case "CinemaShortName":parse.setCinemaShortName(new Utf8(sbs.toString()));break;
							    case "CinemaAliasName":parse.setCinemaAliasName(new Utf8(sbs.toString()));break;
							    case "CinemaCity":parse.setCinemaCity(new Utf8(sbs.toString()));break;
							    case "CinemaDistrict":parse.setCinemaDistrict(new Utf8(sbs.toString()));break;
							    case "CinemaStreet":parse.setCinemaStreet(new Utf8(sbs.toString()));break;
							    case "CinemaFacility":parse.setCinemaFacility(new Utf8(sbs.toString()));break;
							    case "CinemaIntroduction":parse.setCinemaIntroduction(new Utf8(sbs.toString()));break;
							    case "CinemaDeals":parse.setCinemaDeals(new Utf8(sbs.toString()));break;
							    case "CinemaCommentMtime":parse.setCinemaCommentMtime(new Utf8(sbs.toString()));break;
							    case "ActorChineseName":parse.setActorChineseName(new Utf8(sbs.toString()));break;
							    case "ActorEnglishName":parse.setActorEnglishName(new Utf8(sbs.toString()));break;
							    case "Gender":parse.setGender(new Utf8(sbs.toString()));break;
							    case "ActorIntroduction":parse.setActorIntroduction(new Utf8(sbs.toString()));break;
							    case "ActorNews":parse.setActorNews(new Utf8(sbs.toString()));break;
							    case "ActorProduction":parse.setActorProduction(new Utf8(sbs.toString()));break;
							    case "ActorCommentMtime":parse.setActorCommentMtime(new Utf8(sbs.toString()));break;
							    case "ActorAwards":parse.setActorAwards(new Utf8(sbs.toString()));break;
							    case "AwardChineseName":parse.setAwardChineseName(new Utf8(sbs.toString()));break;
							    case "AwardEnglishName":parse.setAwardEnglishName(new Utf8(sbs.toString()));break;
							    case "AwardShortName":parse.setAwardShortName(new Utf8(sbs.toString()));break;
							    case "AwardAliasName":parse.setAwardAliasName(new Utf8(sbs.toString()));break;
							    case "AwardHoldTime":parse.setAwardHoldTime(new Utf8(sbs.toString()));break;
							    case "AwardItems":parse.setAwardItems(new Utf8(sbs.toString()));break;
							    case "MovieNewsOutlinks":parse.setMovieNewsOutlinks(new Utf8(sbs.toString()));break;
							    case "MovieNewsName":parse.setMovieNewsName(new Utf8(sbs.toString()));break;
							}
						}	
					}
				}
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			NodeWalker walkers = new NodeWalker(doc);
			StringBuilder sbs=new StringBuilder();
			while (walkers.hasNext()) {

				Node currentNode = walkers.nextNode();
				String nodeName = currentNode.getNodeName();
				short nodeType = currentNode.getNodeType();

				if ("script".equalsIgnoreCase(nodeName)) {
					walkers.skipChildren();
				}
				if ("style".equalsIgnoreCase(nodeName)) {
					walkers.skipChildren();
				}
				if (nodeType == Node.COMMENT_NODE) {
					walkers.skipChildren();
				}
				if (nodeType == Node.TEXT_NODE) {
					// cleanup and trim the value
					String text = currentNode.getNodeValue();
					text = text.replaceAll("\\s+", " ");
					text = text.trim();
					if (text.length() > 0) {
						if (sbs.length() > 0)
							sbs.append(' ');
						sbs.append(text);
					}
				}
			}
			parse.setText(sbs.toString());
			
		}
		return abort;
	}

	private void getDeepTextOfNodes(Node curNode_child) {
		if (curNode_child.hasChildNodes()) {
			Node curNode_gchild=curNode_child.getFirstChild();
			while(curNode_gchild!=null){
				if (curNode_gchild.getNodeType() == Node.TEXT_NODE) {
					String text = curNode_gchild.getNodeValue();
					if (text != null) {
						text = text.replaceAll("\\s+", " ");
						text = text.trim();
						if (text.length() > 0) {
							temp_sb+=text;
						}
					}
				} else {
						getDeepTextOfNodes(curNode_gchild);
				}
				curNode_gchild=curNode_gchild.getNextSibling();
			}
		}
	}
	/**
	 * my code about extract text,why do this,in order to avoid unnecessary
	 * faults !This method takes a {@link StringBuilder} and a DOM {@link Node},
	 * and will append all the content text found beneath the DOM node to the
	 * <code>StringBuilder</code>.
	 * 
	 * <p>
	 * 
	 * If <code>abortOnNestedAnchors</code> is true, DOM traversal will be
	 * aborted and the <code>StringBuffer</code> will not contain any text
	 * encountered after a nested anchor is found.
	 * 
	 * <p>
	 * 
	 * @return true if nested anchors were found
	 */
	// #region
	public void getText_OnlyText(StringBuilder sb, Node node) {
		getText_OnlyText(sb, node, false);
	}

	public boolean getText_OnlyText(StringBuilder sb, Node node,
			boolean abortOnNestedAnchors) {
		if (getTextHelper_OnlyText(sb, node, abortOnNestedAnchors, 0))
			return true;
		return false;
	}

	private boolean getTextHelper_OnlyText(StringBuilder sb, Node node,
			boolean abortOnNestedAnchors, int anchorDepth) {
		boolean abort = false;
		NodeWalker walker = new NodeWalker(node);

		// my code
		double choose_parse_pattern = -1;
		if (sb.indexOf("演职员表") != -1) {
			choose_parse_pattern = 1;
			sb.setLength(0);
		} else if (sb.indexOf("个人档案") != -1) {
			choose_parse_pattern = 2;
			sb.setLength(0);
		} else if (sb.indexOf("作品列表") != -1) {
			choose_parse_pattern = 3;
			sb.setLength(0);
		} else if (sb.indexOf("荣誉成就") != -1) {
			choose_parse_pattern = 4;
			sb.setLength(0);
		} else if (sb.indexOf("新闻资讯") != -1) {
			choose_parse_pattern = 6.1;// details of news == 6.2
			sb.setLength(0);
		} else if (sb.indexOf("留言") != -1) {
			choose_parse_pattern = 7;
			sb.setLength(0);
		} else if (sb.indexOf("影评") != -1) {
			choose_parse_pattern = 8.1;// lists of the long comments==8.1
										// details
										// of the long comments==8.2 details of
										// the short comments==8.3
			sb.setLength(0);
		} else {
			choose_parse_pattern = 0;
			sb.setLength(0);
		}

		while (walker.hasNext()) {

			Node currentNode = walker.nextNode();
			String nodeName = currentNode.getNodeName();
			short nodeType = currentNode.getNodeType();

			if ("script".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if ("style".equalsIgnoreCase(nodeName)) {
				walker.skipChildren();
			}
			if (abortOnNestedAnchors && "a".equalsIgnoreCase(nodeName)) {
				anchorDepth++;
				if (anchorDepth > 1) {
					abort = true;
					break;
				}
			}
			if (nodeType == Node.COMMENT_NODE) {
				walker.skipChildren();
			}
			if (nodeType == Node.TEXT_NODE) {
				// my code
				if (choose_parse_pattern == 0) {
					// cleanup and trim the value
					String text = currentNode.getNodeValue();
					text = text.replaceAll("\\s+", " ");
					text = text.trim();
					if (text.length() > 0) {
						if (sb.length() > 0)
							sb.append(' ');
						sb.append(text);
					}
				} else {
					NamedNodeMap attrs_parent = currentNode.getParentNode()
							.getAttributes(); // get all properties
					NamedNodeMap attrs_parent_parent = currentNode
							.getParentNode().getParentNode().getAttributes();
					NamedNodeMap attrs_parent_parent_parent = currentNode
							.getParentNode().getParentNode().getParentNode()
							.getAttributes();
					NamedNodeMap attrs_parent_parent_parent_parent = null;
					if (currentNode.getParentNode().getParentNode()
							.getParentNode().getParentNode() != null) {
						attrs_parent_parent_parent_parent = currentNode
								.getParentNode().getParentNode()
								.getParentNode().getParentNode()
								.getAttributes();
					}

					Node classNode_p = null;
					Node panNode_p = null;
					Node idNode_p = null;

					Node classNode_p_p = null;
					Node panNode_p_p = null;
					Node propertyNode_p_p = null;
					Node idNode_p_p = null;
					Node festivalidNode_p_p = null;

					Node classNode_p_p_p = null;
					Node panNode_p_p_p = null;
					Node propertyNode_p_p_p = null;
					Node idNode_p_p_p = null;
					Node festivalidNode_p_p_p = null;

					Node classNode_p_p_p_p = null;
					Node festivalidNode_p_p_p_p = null;
					// _P
					if (attrs_parent != null) {
						for (int i = 0; i < attrs_parent.getLength(); i++) {
							Node attr = attrs_parent.item(i);
							String attrName = attr.getNodeName().toLowerCase();
							if (attrName.equals("class")) {
								classNode_p = attr;
							} else if (attrName.equals("pan")) {
								panNode_p = attr;
							} else if (attrName.equals("id")) {
								idNode_p = attr;
							}
						}
					}
					// _P_P
					if (attrs_parent_parent != null) {
						for (int i = 0; i < attrs_parent_parent.getLength(); i++) {
							Node attr = attrs_parent_parent.item(i);
							String attrName = attr.getNodeName().toLowerCase();
							if (attrName.equals("class")) {
								classNode_p_p = attr;
							} else if (attrName.equals("pan")) {
								panNode_p_p = attr;
							} else if (attrName.equals("property")) {
								propertyNode_p_p = attr;
							} else if (attrName.equals("id")) {
								idNode_p_p = attr;
							} else if (attrName.equals("festivalid")) {
								festivalidNode_p_p = attr;
							}
						}
					}
					// _P_P_P
					if (attrs_parent_parent_parent != null) {
						for (int i = 0; i < attrs_parent_parent_parent
								.getLength(); i++) {
							Node attr = attrs_parent_parent_parent.item(i);
							String attrName = attr.getNodeName().toLowerCase();
							if (attrName.equals("class")) {
								classNode_p_p_p = attr;
							} else if (attrName.equals("pan")) {
								panNode_p_p_p = attr;
							} else if (attrName.equals("property")) {
								propertyNode_p_p_p = attr;
							} else if (attrName.equals("id")) {
								idNode_p_p_p = attr;
							} else if (attrName.equals("festivalid")) {
								festivalidNode_p_p_p = attr;
							}
						}
					}

					// _P_P_P_P
					if (attrs_parent_parent_parent_parent != null) {
						for (int i = 0; i < attrs_parent_parent_parent_parent
								.getLength(); i++) {
							Node attr = attrs_parent_parent_parent_parent
									.item(i);
							String attrName = attr.getNodeName().toLowerCase();
							if (attrName.equals("class")) {
								classNode_p_p_p_p = attr;
							} else if (attrName.equals("festivalid")) {
								festivalidNode_p_p_p_p = attr;
							}
						}
					}

					// choose_parse_pattern == 1
					// extract URL: http://movie.mtime.com/******/
					// #region
					if (choose_parse_pattern == 1) {
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if ("__r_c_".equals(classNode_name)
									|| "db_year".equals(classNode_name)
									|| "otherbox __r_c_".equals(classNode_name)) {
								// cleanup and trim the value
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
							if ("info_r".equals(classNode_name)) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								sb.append("\r\n");
								if (text.length() > 0) {
									sb.append(text);
									sb.append("\r\n");
								}
							}
						}

						if (classNode_p != null && panNode_p != null) {
							String panNode_name = panNode_p.getNodeValue()
									.toLowerCase();
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (panNode_name
									.indexOf("m14_movie_overview_actor") != -1
									&& "__r_c_".equals(classNode_name)) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
					}
					// #endregion

					// choose_parse_pattern == 2
					// extract personal informations of The actors
					// URL:http://people.mtime.com/******/details.html
					// #region
					else if (choose_parse_pattern == 2) {
						if (panNode_p_p != null) {
							String panNode_name = panNode_p_p.getNodeValue()
									.toLowerCase();
							if (panNode_name
									.equalsIgnoreCase("m14_person_index_personname")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("per_s_tit")
									|| classNode_name
											.equalsIgnoreCase("per_info_cont")
									|| classNode_name
											.equalsIgnoreCase("per_relalist")
									|| classNode_name
											.equalsIgnoreCase("per_s_tit fl")
									|| classNode_name
											.equalsIgnoreCase("fl normal")
									|| classNode_name
											.equalsIgnoreCase("img_right")
									|| classNode_name
											.equalsIgnoreCase("img_left")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p_p != null) {
							String classNode_name = classNode_p_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name
									.equalsIgnoreCase("per_info_cont")
									|| classNode_name
											.equalsIgnoreCase("per_relalist")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (idNode_p_p != null) {
							String idNode_name = idNode_p_p.getNodeValue()
									.toLowerCase();
							if (idNode_name.equalsIgnoreCase("lblallgraphy")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (idNode_p_p_p != null) {
							String idNode_name = idNode_p_p_p.getNodeValue()
									.toLowerCase();
							if (idNode_name.equalsIgnoreCase("lblallgraphy")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (classNode_name.equalsIgnoreCase("year_tag")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p_p_p != null) {
							String classNode_name = classNode_p_p_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("per_relalist")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}

					}
					// #endregion

					// choose_parse_pattern == 3
					// extract personal informations of The actors
					// URL:http://people.mtime.com/******/filmographies/
					// #region
					else if (choose_parse_pattern == 3) {
						if (panNode_p_p != null) {
							String panNode_name = panNode_p_p.getNodeValue()
									.toLowerCase();
							if (panNode_name
									.equalsIgnoreCase("m14_person_index_personname")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (classNode_name.equalsIgnoreCase("fl")
									|| classNode_name
											.equalsIgnoreCase("year_tag")
									|| classNode_name
											.equalsIgnoreCase("db_point")
									|| classNode_name.equalsIgnoreCase("type")
									|| classNode_name
											.equalsIgnoreCase("c_666 mt9")
									|| classNode_name
											.equalsIgnoreCase("db_count")
									|| classNode_name
											.equalsIgnoreCase("px12 normal m16")
									|| classNode_name
											.equalsIgnoreCase("mr15 none")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (idNode_p != null) {
							String idNode_name = idNode_p.getNodeValue()
									.toLowerCase();
							if (idNode_name.equalsIgnoreCase("leftcounter")
									|| idNode_name
											.equalsIgnoreCase("rightcounter")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name
									.equalsIgnoreCase("per_actortype none")
									|| classNode_name
											.equalsIgnoreCase("per_s_tit")
									|| classNode_name
											.equalsIgnoreCase("per_s_tit none")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}

						if (classNode_p_p_p_p != null) {
							String classNode_name = classNode_p_p_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("pic96")
									|| classNode_name.equalsIgnoreCase("pic75")) {
								String text = currentNode.getNodeValue(); // 获取文本内容
								text = text.replaceAll("\\s+", " "); // 消除所有空格和转行等字符
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}

					}
					// #endregion

					// choose_parse_pattern == 4
					// extract personal informations of The actors
					// URL:http://people.mtime.com/******/awards.html
					// #region
					else if (choose_parse_pattern == 4) {
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (classNode_name
									.equalsIgnoreCase("per_awardstit")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name
									.equalsIgnoreCase("per_awardstit")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (festivalidNode_p_p != null) {
							String festivalidNode_name = festivalidNode_p_p
									.getNodeValue().toLowerCase();
							if (festivalidNode_name.indexOf("awardinfo_") != -1) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (festivalidNode_p_p_p != null) {
							String festivalidNode_name = festivalidNode_p_p_p
									.getNodeValue().toLowerCase();
							if (festivalidNode_name.indexOf("awardinfo_") != -1) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (festivalidNode_p_p_p_p != null) {
							String festivalidNode_name = festivalidNode_p_p_p_p
									.getNodeValue().toLowerCase();
							if (festivalidNode_name.indexOf("awardinfo_") != -1) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
					}
					// #endregion

					// choose_parse_pattern == 6.1
					// extract news lists
					// URL:http://people.mtime.com/******/news.html
					// #region
					else if (choose_parse_pattern == 6.1) {
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (classNode_name.equalsIgnoreCase("db_newsinfo")
									|| classNode_name
											.equalsIgnoreCase("c_94 mt9 px12")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p_p != null) {
							String classNode_name = classNode_p_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("db_newscont")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (panNode_p_p != null) {
							String panNode_name = panNode_p_p.getNodeValue()
									.toLowerCase();
							if (panNode_name
									.equalsIgnoreCase("M14_Person_Index_PersonName")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
					}
					// #endregion

					// choose_parse_pattern == 6.2
					// extract news details
					// URL:http://news.mtime.com/****/**/**/******.html
					// #region
					else if (choose_parse_pattern == 6.2) {
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase().trim();
							if (classNode_name
									.equalsIgnoreCase("mt15 ml25 newstime")
									|| classNode_name
											.equalsIgnoreCase("newsnote")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("newsheadtit")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p_p != null) {
							String classNode_name = classNode_p_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("newstext")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p_p_p != null) {
							String classNode_name = classNode_p_p_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("newstext")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
					}
					// #endregion

					// choose_parse_pattern == 7
					// extract personal informations of The actors
					// URL:http://people.mtime.com/******/comment.html
					else if (choose_parse_pattern == 7) {
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name
									.equalsIgnoreCase("mod_short none")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(" ");
									sb.append(text);
								}
							}
						}
					}

					// choose_parse_pattern == 8.1
					// extract lists of The movies' long comments
					// URL:http://movie.mtime.com/******/comment.html
					// #region
					else if (choose_parse_pattern == 8.1) {
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (classNode_name.equalsIgnoreCase("db_year")
									|| classNode_name.equalsIgnoreCase("mt20")
									|| classNode_name
											.equalsIgnoreCase("db_point ml12")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("db_year")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (propertyNode_p_p != null) {
							String propertyNode_name = propertyNode_p_p
									.getNodeValue().toLowerCase();
							if (propertyNode_name
									.equalsIgnoreCase("v:itemreviewed")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p_p_p != null) {
							String classNode_name = classNode_p_p_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("comboxcont")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
					}
					// #endregion

					// choose_parse_pattern == 8.2
					// extract the details of The movies' long comments
					// URL:http://movie.mtime.com/******/comment.html
					// #region
					else if (choose_parse_pattern == 8.2) {
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (classNode_name
									.equalsIgnoreCase("px38 mt30 c_000")
									|| classNode_name.equalsIgnoreCase("mt15")
									|| classNode_name.equalsIgnoreCase("mt3")
									|| classNode_name.equalsIgnoreCase("mt9")
									|| classNode_name
											.equalsIgnoreCase("db_point px14 ml10")
									|| classNode_name
											.equalsIgnoreCase("first_letter")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						} else if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("mt15")
									|| classNode_name.equalsIgnoreCase("pt3")
									|| classNode_name
											.equalsIgnoreCase("db_mediacont db_commentcont")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
					}
					// #endregion

					// choose_parse_pattern == 8.3
					// extract The movies' short comments
					// URL:http://movie.mtime.com/196613/reviews/short/new.html
					// #region
					else if (choose_parse_pattern == 8.3) {
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if (classNode_name.equalsIgnoreCase("db_point ml6")
									|| classNode_name
											.equalsIgnoreCase("mt6 px12 clearfix")
									|| classNode_name.equalsIgnoreCase("")
									|| classNode_name.equalsIgnoreCase("")
									|| classNode_name.equalsIgnoreCase("")
									|| classNode_name.equalsIgnoreCase("")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						} else if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if (classNode_name.equalsIgnoreCase("mod_short")
									|| classNode_name.equalsIgnoreCase("px14")
									|| classNode_name.equalsIgnoreCase("")) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
					}
					// #endregion

					// choose_parse_pattern == 11
					// extract URL:
					// http://movie.mtime.com/******/fullcredits.html
					// #region
					else if (choose_parse_pattern == 11) {
						if (propertyNode_p_p != null) {
							String propertyNode_name = propertyNode_p_p
									.getNodeValue().toLowerCase();
							if ("v:itemreviewed"
									.equalsIgnoreCase(propertyNode_name)) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p != null) {
							String classNode_name = classNode_p.getNodeValue()
									.toLowerCase();
							if ("db_year".equalsIgnoreCase(classNode_name)) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p != null) {
							String classNode_name = classNode_p_p
									.getNodeValue().toLowerCase();
							if ("db_enname".equalsIgnoreCase(classNode_name)) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						// extract actors
						if (classNode_p_p_p != null) {
							String classNode_name = currentNode.getNodeValue()
									.toLowerCase();
							if ("pic_58".equalsIgnoreCase(classNode_name)) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
								}
							}
						}
						if (classNode_p_p != null) {
							String classNode_name = currentNode.getNodeValue()
									.toLowerCase();
							if ("pic_58".equalsIgnoreCase(classNode_name)) {
								String text = currentNode.getNodeValue();
								text = text.replaceAll("\\s+", " ");
								text = text.trim();
								if (text.length() > 0) {
									if (sb.length() > 0)
										sb.append(' ');
									sb.append(text);
									sb.append("\r\n");
								}
							}
						}
					}
					// #endregion
				}
			}
		}

		return abort;
	}

	// #endregion

	/**
	 * This method takes a {@link StringBuffer} and a DOM {@link Node}, and will
	 * append the content text found beneath the first <code>title</code> node
	 * to the <code>StringBuffer</code>.
	 * 
	 * @return true if a title node was found, false otherwise
	 */
	public boolean getTitle(StringBuilder sb, Node node) {

		NodeWalker walker = new NodeWalker(node);

		while (walker.hasNext()) {

			Node currentNode = walker.nextNode();
			String nodeName = currentNode.getNodeName();
			short nodeType = currentNode.getNodeType();

			if ("body".equalsIgnoreCase(nodeName)) { // stop after HEAD
				return false;
			}

			if (nodeType == Node.ELEMENT_NODE) {
				if ("title".equalsIgnoreCase(nodeName)) {
					getText(sb, currentNode);
					return true;
				}
			}
		}

		return false;
	}

	/** If Node contains a BASE tag then it's HREF is returned. */
	public URL getBase(Node node) {

		NodeWalker walker = new NodeWalker(node);

		while (walker.hasNext()) {

			Node currentNode = walker.nextNode();
			String nodeName = currentNode.getNodeName();
			short nodeType = currentNode.getNodeType();

			// is this node a BASE tag?
			if (nodeType == Node.ELEMENT_NODE) {

				if ("body".equalsIgnoreCase(nodeName)) { // stop after HEAD
					return null;
				}

				if ("base".equalsIgnoreCase(nodeName)) {
					NamedNodeMap attrs = currentNode.getAttributes();
					for (int i = 0; i < attrs.getLength(); i++) {
						Node attr = attrs.item(i);
						if ("href".equalsIgnoreCase(attr.getNodeName())) {
							try {
								return new URL(attr.getNodeValue());
							} catch (MalformedURLException e) {
							}
						}
					}
				}
			}
		}

		// no.
		return null;
	}

	private boolean hasOnlyWhiteSpace(Node node) {
		String val = node.getNodeValue();
		for (int i = 0; i < val.length(); i++) {
			if (!Character.isWhitespace(val.charAt(i)))
				return false;
		}
		return true;
	}

	// this only covers a few cases of empty links that are symptomatic
	// of nekohtml's DOM-fixup process...
	private boolean shouldThrowAwayLink(Node node, NodeList children,
			int childLen, LinkParams params) {
		if (childLen == 0) {
			// this has no inner structure
			if (params.childLen == 0)
				return false;
			else
				return true;
		} else if ((childLen == 1)
				&& (children.item(0).getNodeType() == Node.ELEMENT_NODE)
				&& (params.elName.equalsIgnoreCase(children.item(0)
						.getNodeName()))) {
			// single nested link
			return true;

		} else if (childLen == 2) {

			Node c0 = children.item(0);
			Node c1 = children.item(1);

			if ((c0.getNodeType() == Node.ELEMENT_NODE)
					&& (params.elName.equalsIgnoreCase(c0.getNodeName()))
					&& (c1.getNodeType() == Node.TEXT_NODE)
					&& hasOnlyWhiteSpace(c1)) {
				// single link followed by whitespace node
				return true;
			}

			if ((c1.getNodeType() == Node.ELEMENT_NODE)
					&& (params.elName.equalsIgnoreCase(c1.getNodeName()))
					&& (c0.getNodeType() == Node.TEXT_NODE)
					&& hasOnlyWhiteSpace(c0)) {
				// whitespace node followed by single link
				return true;
			}

		} else if (childLen == 3) {
			Node c0 = children.item(0);
			Node c1 = children.item(1);
			Node c2 = children.item(2);

			if ((c1.getNodeType() == Node.ELEMENT_NODE)
					&& (params.elName.equalsIgnoreCase(c1.getNodeName()))
					&& (c0.getNodeType() == Node.TEXT_NODE)
					&& (c2.getNodeType() == Node.TEXT_NODE)
					&& hasOnlyWhiteSpace(c0) && hasOnlyWhiteSpace(c2)) {
				// single link surrounded by whitespace nodes
				return true;
			}
		}

		return false;
	}

	/**
	 * Handles cases where the url param information is encoded into the base
	 * url as opposed to the target.
	 * <p>
	 * If the taget contains params (i.e. ';xxxx') information then the target
	 * params information is assumed to be correct and any base params
	 * information is ignored. If the base contains params information but the
	 * tareget does not, then the params information is moved to the target
	 * allowing it to be correctly determined by the java.net.URL class.
	 * 
	 * @param base
	 *            The base URL.
	 * @param target
	 *            The target path from the base URL.
	 * 
	 * @return URL A URL with the params information correctly encoded.
	 * 
	 * @throws MalformedURLException
	 *             If the url is not a well formed URL.
	 */
	private URL fixEmbeddedParams(URL base, String target)
			throws MalformedURLException {

		// the target contains params information or the base doesn't then no
		// conversion necessary, return regular URL
		if (target.indexOf(';') >= 0 || base.toString().indexOf(';') == -1) {
			return new URL(base, target);
		}

		// get the base url and it params information
		String baseURL = base.toString();
		int startParams = baseURL.indexOf(';');
		String params = baseURL.substring(startParams);

		// if the target has a query string then put the params information
		// after
		// any path but before the query string, otherwise just append to the
		// path
		int startQS = target.indexOf('?');
		if (startQS >= 0) {
			target = target.substring(0, startQS) + params
					+ target.substring(startQS);
		} else {
			target += params;
		}

		return new URL(base, target);
	}

	/**
	 * This method finds all anchors below the supplied DOM <code>node</code>,
	 * and creates appropriate {@link Outlink} records for each (relative to the
	 * supplied <code>base</code> URL), and adds them to the
	 * <code>outlinks</code> {@link ArrayList}.
	 * 
	 * <p>
	 * 
	 * Links without inner structure (tags, text, etc) are discarded, as are
	 * links which contain only single nested links and empty text nodes (this
	 * is a common DOM-fixup artifact, at least with nekohtml).
	 */
	public void getOutlinks(URL base, ArrayList<Outlink> outlinks, Node node) {

		NodeWalker walker = new NodeWalker(node);
		while (walker.hasNext()) {

			Node currentNode = walker.nextNode();
			String nodeName = currentNode.getNodeName();
			short nodeType = currentNode.getNodeType();
			NodeList children = currentNode.getChildNodes();
			int childLen = (children != null) ? children.getLength() : 0;

			if (nodeType == Node.ELEMENT_NODE) {

				nodeName = nodeName.toLowerCase();
				LinkParams params = linkParams.get(nodeName);
				if (params != null) {
					if (!shouldThrowAwayLink(currentNode, children, childLen,
							params)) {

						StringBuilder linkText = new StringBuilder();
						getText(linkText, currentNode, true);

						NamedNodeMap attrs = currentNode.getAttributes();
						String target = null;
						boolean noFollow = false;
						boolean post = false;
						for (int i = 0; i < attrs.getLength(); i++) {
							Node attr = attrs.item(i);
							String attrName = attr.getNodeName();
							if (params.attrName.equalsIgnoreCase(attrName)) {
								target = attr.getNodeValue();
							} else if ("rel".equalsIgnoreCase(attrName)
									&& "nofollow".equalsIgnoreCase(attr
											.getNodeValue())) {
								noFollow = true;
							} else if ("method".equalsIgnoreCase(attrName)
									&& "post".equalsIgnoreCase(attr
											.getNodeValue())) {
								post = true;
							}
						}
						if (target != null && !noFollow && !post)
							try {

								URL url = (base.toString().indexOf(';') > 0) ? fixEmbeddedParams(
										base, target) : new URL(base, target);
								outlinks.add(new Outlink(url.toString(),
										linkText.toString().trim()));
							} catch (MalformedURLException e) {
								// don't care
							}
					}
					// this should not have any children, skip them
					if (params.childLen == 0)
						continue;
				}
			}
		}
	}

}
