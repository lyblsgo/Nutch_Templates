package org.apache.nutch.parse.html;

import java.io.BufferedReader;
import java.io.FileReader;

public class MyFileReader {
	public static String pagecode="";
	public static void get(String args[])throws Exception
	{
		FileReader fr = new FileReader("/home/test/nutch/urls/MTime/url");
		BufferedReader br=new BufferedReader(fr);
		String temp="";
		String s =br.readLine();
		while(s!=null)
		{
			temp+=s;
			s =br.readLine();
		}
		pagecode = temp;		
		System.out.println(pagecode );
	}
}
