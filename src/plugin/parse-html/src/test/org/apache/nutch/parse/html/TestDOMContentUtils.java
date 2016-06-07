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

import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.Parse;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.storage.ParseStatus;
import org.apache.nutch.util.EncodingDetector;
import org.apache.nutch.util.NutchConfiguration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cyberneko.html.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.apache.html.dom.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Unit tests for DOMContentUtils.
 */
public class TestDOMContentUtils {

	public static String get() {
		String pagecode;
		FileReader fr = null;
		try {
			fr = new FileReader("/home/test/nutch/urls/MTime/test_url_codes");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String temp = "";
		String s = "";
		try {
			s = br.readLine();
		} catch (Exception e) {
		}
		while (s != null) {
			temp += s;
			try {
				s = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pagecode = temp;
		return pagecode;
	}

	private static String mytestpages = get();
	private static final String[] testPages = { mytestpages };
	private static String[] testBaseHrefs = { "http://www.nutch.org",
			"http://www.nutch.org/docs/foo.html", "http://www.nutch.org/docs/",
			"http://www.nutch.org/docs/", "http://www.nutch.org/frames/",
			"http://www.nutch.org/maps/", "http://www.nutch.org/whitespace/",
			"http://www.nutch.org//", "http://www.nutch.org/",
			"http://www.nutch.org/", "http://www.nutch.org/",
			"http://www.nutch.org/;something" };

	private static final DocumentFragment testDOMs[] = new DocumentFragment[testPages.length];

	private static URL[] testBaseHrefURLs = new URL[testPages.length];

	private static final String[] answerText = { "title body anchor" };

	private static Configuration conf;
	private static DOMContentUtils utils = null;

	@Before
	public void setup() {

		conf = NutchConfiguration.create();
		conf.setBoolean("parser.html.form.use_action", true);
		utils = new DOMContentUtils(conf);
		DOMFragmentParser parser = new DOMFragmentParser();
		for (int i = 0; i < testPages.length; i++) {
			DocumentFragment node = new HTMLDocumentImpl()
					.createDocumentFragment();
			try {
				parser.parse(new InputSource(new ByteArrayInputStream(
						testPages[i].getBytes())), node);
				testBaseHrefURLs[i] = new URL(testBaseHrefs[i]);
			} catch (Exception e) {
				assertTrue("caught exception: " + e, false);
			}
			testDOMs[i] = node;
		}
		
	}

	private static boolean equalsIgnoreWhitespace(String s1, String s2) {
		StringTokenizer st1 = new StringTokenizer(s1);
		StringTokenizer st2 = new StringTokenizer(s2);

		while (st1.hasMoreTokens()) {
			if (!st2.hasMoreTokens())
				return false;
			if (!st1.nextToken().equals(st2.nextToken()))
				return false;
		}
		if (st2.hasMoreTokens())
			return false;
		return true;
	}

	@Test
	public void testGetText() {
		if (testDOMs[0] == null)
			setup();
		
		String text = "";
		String title = "";
		Outlink[] outlinks = new Outlink[0];
		Metadata metadata = new Metadata();
		ParseStatus status = new ParseStatus();
		
		Parse parse = new Parse(text, title, outlinks, status);
		
		for (int i = 0; i < testPages.length; i++) {
			StringBuilder sb = new StringBuilder();
			String url="http://movie.mtime.com/173060/reviews/7891622.html";			
			utils.getText_DOM(testDOMs[i],url,parse);
			Utf8 sbs = parse.getActor();
			Utf8 movie=parse.getMovieAwards();
			System.out.println(sbs);
			//System.out.println(mytestpages);
			assertTrue(
					"expecting text: " + answerText[i]
							+ System.getProperty("line.separator")
							+ System.getProperty("line.separator")
							+ "got text: " + text,
					equalsIgnoreWhitespace(answerText[i], text));
			
			
			
//			try {  
//	            ByteBuffer bf=ByteBuffer.wrap(mytestpages.getBytes());
//	            InputSource input = new InputSource(new ByteArrayInputStream(
//						bf.array(), bf.arrayOffset()
//								+ bf.position(),
//						bf.remaining()));								
//
//	            DocumentFragment doc = parseNeko(input);
// 
//	            XPathFactory factory = XPathFactory.newInstance();  
//	            XPath xpath = factory.newXPath();  
//	            // 选取所有class元素的name属性  
//	            // XPath语法介绍： http://w3school.com.cn/xpath/  
//	            XPathExpression expr = xpath.compile("//H3");  
//	            NodeList nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);  
//	            for (int j = 0; j < nodes.getLength(); j++) {  
//	                System.out.println("name = " + nodes.item(j).getFirstChild().getNodeValue());  
//	                    }  
//	        } catch (XPathExpressionException e) {  
//	            e.printStackTrace();  
//	        } catch (ParserConfigurationException e) {  
//	            e.printStackTrace();  
//	        } catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}  
		}
	}
	public DocumentFragment parseNeko(InputSource input) throws Exception {
		DOMFragmentParser parser = new DOMFragmentParser();
		try {
			parser.setFeature(
					"http://cyberneko.org/html/features/augmentations", true);
			parser.setProperty(
					"http://cyberneko.org/html/properties/default-encoding",
					"UTF-8");
			parser.setFeature(
					"http://cyberneko.org/html/features/scanner/ignore-specified-charset",
					true);
			parser.setFeature(
					"http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
					false);
			parser.setFeature(
					"http://cyberneko.org/html/features/balance-tags/document-fragment",
					true);
		} catch (SAXException e) {
		}
		// convert Document to DocumentFragment
		HTMLDocumentImpl doc = new HTMLDocumentImpl();
		doc.setErrorChecking(false);
		DocumentFragment res = doc.createDocumentFragment();
		DocumentFragment frag = doc.createDocumentFragment();
		parser.parse(input, frag);
		res.appendChild(frag);

		try {
			while (true) {
				frag = doc.createDocumentFragment();
				parser.parse(input, frag);
				if (!frag.hasChildNodes())
					break;
				res.appendChild(frag);
			}
		} catch (Exception x) {
		};
		return res;
	}
}
