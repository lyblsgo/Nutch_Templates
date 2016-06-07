/*******************************************************************************
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
 ******************************************************************************/
package org.apache.nutch.crawl;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.ParseStatusUtils;
import org.apache.nutch.protocol.ProtocolStatusUtils;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.Bytes;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.NutchTool;
import org.apache.nutch.util.StringUtil;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.ToolUtil;
import org.apache.gora.mapreduce.GoraMapper;
import org.apache.gora.query.Query;
import org.apache.gora.query.Result;
import org.apache.gora.store.DataStore;

/**
 * Displays information about the entries of the webtable
 **/

public class WebTableReader extends NutchTool implements Tool {

  public static final Logger LOG = LoggerFactory.getLogger(WebTableReader.class);

  public static class WebTableStatMapper extends
      GoraMapper<String, WebPage, Text, LongWritable> {
    LongWritable COUNT_1 = new LongWritable(1);
    private boolean sort = false;

    public WebTableStatMapper() {
    }

    @Override
    public void setup(Context context) {
      sort = context.getConfiguration().getBoolean("db.reader.stats.sort",
          false);
    }

    public void close() {
    }

    @Override
    protected void map(
        String key,
        WebPage value,
        org.apache.hadoop.mapreduce.Mapper<String, WebPage, Text, LongWritable>.Context context)
        throws IOException, InterruptedException {
      context.write(new Text("T"), COUNT_1);
      context.write(new Text("status " + value.getStatus()), COUNT_1);
      context.write(new Text("retry " + value.getRetriesSinceFetch()), COUNT_1);
      context.write(new Text("s"), new LongWritable(
          (long) (value.getScore() * 1000.0)));
      if (sort) {
        URL u = new URL(TableUtil.unreverseUrl(key.toString()));
        String host = u.getHost();
        context.write(new Text("status " + value.getStatus() + " " + host),
            COUNT_1);
      }

    }
  }

  public static class WebTableStatCombiner extends
      Reducer<Text, LongWritable, Text, LongWritable> {
    LongWritable val = new LongWritable();

    @Override
    public void setup(Context context) {
    }

    @Override
    public void cleanup(Context context) {
    }

    @Override
    public void reduce(
        Text key,
        Iterable<LongWritable> values,
        org.apache.hadoop.mapreduce.Reducer<Text, LongWritable, Text, LongWritable>.Context context)
        throws IOException, InterruptedException {
      val.set(0L);
      Iterator<LongWritable> iter = values.iterator();
      String k = key.toString();
      if (!k.equals("s")) {
        while (iter.hasNext()) {
          LongWritable cnt = iter.next();
          val.set(val.get() + cnt.get());
        }
        context.write(key, val);
      } else {
        long total = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        while (iter.hasNext()) {
          LongWritable cnt = iter.next();
          if (cnt.get() < min)
            min = cnt.get();
          if (cnt.get() > max)
            max = cnt.get();
          total += cnt.get();
        }
        context.write(new Text("scn"), new LongWritable(min));
        context.write(new Text("scx"), new LongWritable(max));
        context.write(new Text("sct"), new LongWritable(total));
      }
    }

  }

  public static class WebTableStatReducer extends
      Reducer<Text, LongWritable, Text, LongWritable> {

    @Override
    public void cleanup(Context context) {
    }

    @Override
    protected void reduce(
        Text key,
        Iterable<LongWritable> values,
        org.apache.hadoop.mapreduce.Reducer<Text, LongWritable, Text, LongWritable>.Context context)
        throws IOException, InterruptedException {
      Iterator<LongWritable> iter = values.iterator();
      String k = key.toString();
      if (k.equals("T")) {
        // sum all values for this key
        long sum = 0;
        while (iter.hasNext()) {
          sum += iter.next().get();
        }
        // output sum
        context.write(key, new LongWritable(sum));
      } else if (k.startsWith("status") || k.startsWith("retry")) {
        LongWritable cnt = new LongWritable();
        while (iter.hasNext()) {
          LongWritable val = iter.next();
          cnt.set(cnt.get() + val.get());
        }
        context.write(key, cnt);
      } else if (k.equals("scx")) {
        LongWritable cnt = new LongWritable(Long.MIN_VALUE);
        while (iter.hasNext()) {
          LongWritable val = iter.next();
          if (cnt.get() < val.get())
            cnt.set(val.get());
        }
        context.write(key, cnt);
      } else if (k.equals("scn")) {
        LongWritable cnt = new LongWritable(Long.MAX_VALUE);
        while (iter.hasNext()) {
          LongWritable val = iter.next();
          if (cnt.get() > val.get())
            cnt.set(val.get());
        }
        context.write(key, cnt);
      } else if (k.equals("sct")) {
        LongWritable cnt = new LongWritable();
        while (iter.hasNext()) {
          LongWritable val = iter.next();
          cnt.set(cnt.get() + val.get());
        }
        context.write(key, cnt);
      }
    }

  }

  public void processStatJob(boolean sort) throws Exception {

    if (LOG.isInfoEnabled()) {
      LOG.info("WebTable statistics start");
    }
    run(ToolUtil.toArgMap(Nutch.ARG_SORT, sort));
    for (Entry<String,Object> e : results.entrySet()) {
      LOG.info(e.getKey() + ":\t" + e.getValue());
    }
  }

  /** Prints out the entry to the standard out **/
  private void read(String key, boolean dumpContent, boolean dumpHeaders,
      boolean dumpLinks, boolean dumpText) throws ClassNotFoundException, IOException, Exception {
    DataStore<String, WebPage> datastore = StorageUtils.createWebStore(getConf(),
        String.class, WebPage.class);

    Query<String, WebPage> query = datastore.newQuery();
    String reversedUrl = TableUtil.reverseUrl(key);
    query.setKey(reversedUrl);

    Result<String, WebPage> result = datastore.execute(query);
    boolean found = false;
    // should happen only once
    while (result.next()) {
      try {
        WebPage page = result.get();
        String skey = result.getKey();
        // we should not get to this point but nevermind
        if (page == null || skey == null)
          break;
        found = true;
        String url = TableUtil.unreverseUrl(skey);
        System.out.println(getPageRepresentation(url, page, dumpContent,
            dumpHeaders, dumpLinks, dumpText));
      }catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (!found)
      System.out.println(key + " not found");
    result.close();
    datastore.close();
  }

  /** Filters the entries from the table based on a regex **/
  public static class WebTableRegexMapper extends
      GoraMapper<String, WebPage, Text, Text> {

    static final String regexParamName = "webtable.url.regex";
    static final String contentParamName = "webtable.dump.content";
    static final String linksParamName = "webtable.dump.links";
    static final String textParamName = "webtable.dump.text";
    static final String headersParamName = "webtable.dump.headers";

    public WebTableRegexMapper() {
    }

    private Pattern regex = null;
    private boolean dumpContent, dumpHeaders, dumpLinks, dumpText;

    @Override
    protected void map(
        String key,
        WebPage value,
        org.apache.hadoop.mapreduce.Mapper<String, WebPage, Text, Text>.Context context)
        throws IOException, InterruptedException {
      // checks whether the Key passes the regex
      String url = TableUtil.unreverseUrl(key.toString());
      if (regex.matcher(url).matches()) {
        context.write(new Text(url),
            new Text(getPageRepresentation(key, value, dumpContent, dumpHeaders,
                dumpLinks, dumpText)));
      }
    }

    @Override
    protected void setup(
        org.apache.hadoop.mapreduce.Mapper<String, WebPage, Text, Text>.Context context)
        throws IOException, InterruptedException {
      regex = Pattern.compile(context.getConfiguration().get(regexParamName,
          ".+"));
      dumpContent = context.getConfiguration().getBoolean(contentParamName, false);
      dumpHeaders = context.getConfiguration().getBoolean(headersParamName, false);
      dumpLinks = context.getConfiguration().getBoolean(linksParamName, false);
      dumpText = context.getConfiguration().getBoolean(textParamName, false);
    }

  }

  public void processDumpJob(String output, Configuration config, String regex,
      boolean content, boolean headers, boolean links, boolean text)
      throws IOException, ClassNotFoundException, InterruptedException {

    if (LOG.isInfoEnabled()) {
      LOG.info("WebTable dump: starting");
    }

    Path outFolder = new Path(output);
    Job job = new NutchJob(getConf(), "db_dump");
    Configuration cfg = job.getConfiguration();
    cfg.set(WebTableRegexMapper.regexParamName, regex);
    cfg.setBoolean(WebTableRegexMapper.contentParamName, content);
    cfg.setBoolean(WebTableRegexMapper.headersParamName, headers);
    cfg.setBoolean(WebTableRegexMapper.linksParamName, links);
    cfg.setBoolean(WebTableRegexMapper.textParamName, text);

    DataStore<String, WebPage> store = StorageUtils.createWebStore(job
        .getConfiguration(), String.class, WebPage.class);
    Query<String, WebPage> query = store.newQuery();
    query.setFields(WebPage._ALL_FIELDS);

    GoraMapper.initMapperJob(job, query, store, Text.class, Text.class,
        WebTableRegexMapper.class, null, true);

    FileOutputFormat.setOutputPath(job, outFolder);
    job.setOutputFormatClass(TextOutputFormat.class);

    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);

    boolean success = job.waitForCompletion(true);

    if (LOG.isInfoEnabled()) {
      LOG.info("WebTable dump: done");
    }
  }

  private static String getPageRepresentation(String key, WebPage page,
      boolean dumpContent, boolean dumpHeaders, boolean dumpLinks, boolean dumpText) {
    StringBuffer sb = new StringBuffer();
    sb.append("key:\t" + key).append("\n");
    sb.append("baseUrl:\t" + page.getBaseUrl()).append("\n");
    sb.append("status:\t").append(page.getStatus()).append(" (").append(
        CrawlStatus.getName((byte) page.getStatus())).append(")\n");
    sb.append("fetchTime:\t" + page.getFetchTime()).append("\n");
    sb.append("prevFetchTime:\t" + page.getPrevFetchTime()).append("\n");
    sb.append("fetchInterval:\t" + page.getFetchInterval()).append("\n"); 
    sb.append("retriesSinceFetch:\t" + page.getRetriesSinceFetch()).append("\n");
    sb.append("modifiedTime:\t" + page.getModifiedTime()).append("\n");
    sb.append("prevModifiedTime:\t" + page.getPrevModifiedTime()).append("\n");
    sb.append("protocolStatus:\t" +
        ProtocolStatusUtils.toString(page.getProtocolStatus())).append("\n");
    ByteBuffer prevSig = page.getPrevSignature();
        if (prevSig != null) {
      sb.append("prevSignature:\t" + StringUtil.toHexString(prevSig)).append("\n");
    }
    ByteBuffer sig = page.getSignature();
    if (sig != null) {
      sb.append("signature:\t" + StringUtil.toHexString(sig)).append("\n");
    }
    sb.append("parseStatus:\t" +
        ParseStatusUtils.toString(page.getParseStatus())).append("\n");
    sb.append("title:\t" + page.getTitle()).append("\n");
    sb.append("score:\t" + page.getScore()).append("\n");

    Map<Utf8, Utf8> markers = page.getMarkers();
    sb.append("markers:\t" + markers).append("\n");
    sb.append("reprUrl:\t" + page.getReprUrl()).append("\n");
    Utf8 batchId = page.getBatchId();
    if (batchId != null) {
      sb.append("batchId:\t" + batchId.toString()).append("\n");
    }
    Map<Utf8, ByteBuffer> metadata = page.getMetadata();
    if (metadata != null) {
      Iterator<Entry<Utf8, ByteBuffer>> iterator = metadata.entrySet()
          .iterator();
      while (iterator.hasNext()) {
        Entry<Utf8, ByteBuffer> entry = iterator.next();
        sb.append("metadata " + entry.getKey().toString()).append(" : \t")
            .append(Bytes.toString(entry.getValue())).append("\n");
      }
    }
    if (dumpLinks) {
      Map<Utf8,Utf8> inlinks = page.getInlinks();
      Map<Utf8,Utf8> outlinks = page.getOutlinks();
      if (outlinks != null) {
        for (Entry<Utf8,Utf8> e : outlinks.entrySet()) {
          sb.append("outlink:\t" + e.getKey() + "\t" + e.getValue() + "\n");
        }
      }
      if (inlinks != null) {
        for (Entry<Utf8,Utf8> e : inlinks.entrySet()) {
          sb.append("inlink:\t" + e.getKey() + "\t" + e.getValue() + "\n");
        }
      }
    }
    if (dumpHeaders) {
      Map<Utf8,Utf8> headers = page.getHeaders();
      if (headers != null) {
        for (Entry<Utf8,Utf8> e : headers.entrySet()) {
          sb.append("header:\t" + e.getKey() + "\t" + e.getValue() + "\n");
        }
      }
    }
    ByteBuffer content = page.getContent();
    if (content != null && dumpContent) {
      sb.append("contentType:\t" + page.getContentType()).append("\n");
      sb.append("content:start:\n");
      sb.append(Bytes.toString(content));
      sb.append("\ncontent:end:\n");
    }
    Utf8 text = page.getText();
    Utf8 actor= page.getActor();
    Utf8 movieIDProfessional = page.getMovieIdProfessional();
    Utf8 movieIDIMDB = page.getMovieIdIMDB();
    Utf8 movieIDMtime = page.getMovieIdMtime();
    Utf8 movieIDDouban = page.getMovieIdDouban();
    Utf8 movieID1905 = page.getMovieId1905();
    Utf8 movieChineseName=page.getMovieChineseName();
    Utf8 movieEnglishName=page.getMovieEnglishName();
    Utf8 movieShortname=page.getMovieShortName();
    Utf8 movieAliasName=page.getMovieAliasName();
    Utf8 director=page.getDirector();
    Utf8 writer=page.getWriter();
    Utf8 producer=page.getProducer();
    Utf8 cinematography=page.getCinematography();
    Utf8 cutter=page.getCutter();
    Utf8 wardrobe=page.getWardrobe();
    Utf8 visualAffectArtist=page.getVisualAffectArtist();
    Utf8 associateDirector=page.getAssociateDirector();
    Utf8 movieType=page.getmovieType();
    Utf8 publishCompany=page.getPublishCompany();
    Utf8 producerCompany=page.getProduceCompany();
    Utf8 distributionInformation=page.getDistributionInformation();
    Utf8 plotIntroduction=page.getPlotIntroduction();
    Utf8 roleIntroduction=page.getRoleIntroduction();
    Utf8 movieLongComment=page.getMovieLongComment();
    Utf8 movieShortComment=page.getMovieShortComment();
    Utf8 movieWeiboComment=page.getMovieWeiboComment();
    Utf8 movieNews=page.getMovieNews();
    Float movieScoreIMDB=page.getMovieScoreIMDB();
    Float movieScoreMtime=page.getMovieScoreMtime();
    Float movieScoreDouban=page.getMovieScoreDouban();
    Float movieScore1905=page.getMovieScore1905();
    Utf8 movieAwards=page.getMovieAwards();
    
     Utf8 cinemaIdProfessional=page.getCinemaIdProfessional();
     Utf8 cinemaIdMtime=page.getCinemaIdMtime();
     Utf8 cinemaIdGewara=page.getCinemaIdGewara();
     Utf8 cinemaIdSell=page.getCinemaIdSell();
     Utf8 cinemaChineseName=page.getCinemaChineseName();
     Utf8 cinemaShortName=page.getCinemaShortName();
     Utf8 cinemaAliasName=page.getCinemaAliasName();
     Utf8 cinemaCity=page.getCinemaCity();
     Utf8 cinemaDistrict=page.getCinemaDistrict();
     Utf8 cinemaStreet=page.getCinemaStreet();
     Utf8 cinemaAmapLocation=page.getCinemaAmapLocation();
     Utf8 cinemaBaiduLocation=page.getCinemaBaiduLocation();
     Utf8 theaterChainName=page.getTheaterChainName();
     Utf8 filmInvestmentCompany=page.getFilmInvestmentCompany();
     Utf8 cinemaAssetsJoining=page.getCinemaAssetsJoining();
     Utf8 bus=page.getBus();
     Utf8 subway=page.getSubway();
     Utf8 drive=page.getDrive();
     Utf8 cinemaFacility=page.getCinemaFacility();
     Utf8 cinema3D=page.getCinema3D();
     Utf8 cinemaCreditCard=page.getCinemaCreditCard();
     Utf8 cinemaSellGoods=page.getCinemaSellGoods();
     Utf8 cinemaParking=page.getCinemaParking();
     Utf8 cinemaChildrenTicket=page.getCinemaChildrenTicket();
     Utf8 cinemaResting=page.getCinemaResting();
     Utf8 cinemaIntroduction=page.getCinemaIntroduction();
     Utf8 cinemaDeals=page.getCinemaDeals();
     Utf8 cinemaCommentMtime=page.getCinemaCommentMtime();
     Utf8 cinemaCommentGewara=page.getCinemaCommentGewara();
     Utf8 cinemaCommentDianping=page.getCinemaCommentDianping();
     Utf8 cinemaCommentWeibo=page.getCinemaCommentWeibo();
     Float cinemaScoreMtime=page.getCinemaScoreMtime();
     Float cinemaScoreGewara=page.getCinemaScoreGewara();
     Float cinemaScoreDianping=page.getCinemaScoreDianping();
    
     Utf8 actorIdMtime=page.getActorIdMtime();
     Utf8 actorIdDouban=page.getActorIdDouban();
     Utf8 actorId1905=page.getActorId1905();
     Utf8 actorIdIMDB=page.getActorIdIMDB();
     Utf8 actorChineseName=page.getActorChineseName();
     Utf8 actorEnglishName=page.getActorEnglishName();
     Utf8 actorShortName=page.getActorShortName();
     Utf8 actorAliasName=page.getActorAliasName();
     Utf8 actorBirthName=page.getActorBirthName();
     Utf8 gender=page.getGender();
     Utf8 birthday=page.getBirthday();
     Utf8 birthplace=page.getBirthplace();
     Utf8 bloodtype=page.getBloodtype();
     Utf8 height=page.getHeight();
     Utf8 weight=page.getWeight();
     Utf8 actorIntroduction=page.getActorIntroduction();
     Utf8 birthplaceIntroduction=page.getBirthplaceIntroduction();
     Utf8 profession=page.getProfession();
     Utf8 actorCompany=page.getActorCompany();
     Utf8 actorNews=page.getActorNews();
     Utf8 actorProduction=page.getActorProduction();
     Utf8 actorCommentMtime=page.getActorCommentMtime();
     Utf8 actorCommentGewara=page.getActorCommentGewara();
     Utf8 actorCommentDouban=page.getActorCommentDouban();
     Utf8 actorCommentWeibo=page.getActorCommentWeibo();
     Float actorScore=page.getActorScore();
     Utf8 relatives=page.getRelatives();
     Utf8 gossip=page.getGossip();
     Utf8 actorAwards=page.getActorAwards();
    
     Utf8 awardIdMtime=page.getAwardIdMtime();
     Utf8 awardIdIMDB=page.getAwardIdIMDB();
     Utf8 awardId1905=page.getAwardId1905();
     Utf8 awardChineseName=page.getAwardChineseName();
     Utf8 awardEnglishName=page.getAwardEnglishName();
     Utf8 awardShortName=page.getAwardShortName();
     Utf8 awardAliasName=page.getAwardAliasName();
     Utf8 awardHoldTime=page.getAwardHoldTime();
     Utf8 awardHoldPlace=page.getAwardHoldPlace();
     Utf8 awardHoldCompany=page.getAwardHoldCompany();
     Utf8 awardItems=page.getAwardItems();
     Utf8 awardMovies=page.getAwardMovies();
    
     Utf8 weiboV=page.getWeiboV();
     int weiboLevel=page.getWeiboLevel();
     Utf8 weiboName=page.getWeiboName();
     Utf8 weiboId=page.getWeiboId();
     Utf8 weiboOfficial=page.getWeiboOfficial();
     int weiboFans=page.getWeiboFans();
     int weiboNum=page.getWeiboNum();
     int weiboInterest=page.getWeiboInterest();
     Utf8 weiboTime=page.getWeiboTime();
     Utf8 weiboContent=page.getWeiboContent();
     int weiboZan=page.getWeiboZan();
     Utf8 weiboTopic=page.getWeiboTopic();
     Utf8 weiboTransmit=page.getWeiboTransmit();
     Utf8 weiboComment=page.getWeiboComment();
    
     Utf8 NoProfessional=page.getNoProfessional();
     Utf8 NoSite=page.getNoSite();
     Utf8 siteMtime=page.getSiteMtime();
     Utf8 siteTaobao=page.getSiteTaobao();
     Utf8 siteGewara=page.getSiteGewara();
     Utf8 siteMaoyan=page.getSiteMaoyan();
     Utf8 siteWangpiao=page.getSiteWangpiao();
     Utf8 siteSpider=page.getSiteSpider();
     Utf8 siteBaidu=page.getSiteBaidu();
     Utf8 siteAmap=page.getSiteAmap();
     Utf8 NoFilm=page.getNoFilm();
     Utf8 NoTheater=page.getNoTheater();
     Utf8 NoHall=page.getNoHall();
     Utf8 sellSeat=page.getSellSeat();
     Utf8 price=page.getPrice();
     Utf8 vipPrice=page.getVipPrice();
     Utf8 promotionPrice=page.getPromotionPrice();
     Utf8 NoLanguage=page.getNoLanguage();
     Utf8 NoVersion=page.getNoVersion();
     Utf8 NoTime=page.getNoTime();
    
     Utf8 activitySite=page.getActivitySite();
     Utf8 activityFilm=page.getActivityFilm();
     Utf8 activityTheater=page.getActivityTheater();
     Utf8 activityRule=page.getActivityRule();
     Utf8 activityPrPrice=page.getActivityPrPrice();
     Utf8 activityPrice=page.getActivityPrice();
     Utf8 activityTime=page.getActivityTime();
     Utf8 activityEffectTime=page.getActivityEffectTime();
    
     Utf8 movieNewsOutlinks=page.getMovieNewsOutlinks();
     Utf8 movieNewsName=page.getmovieNewsName();
    
    if (text != null && dumpText) {
			sb.append("text:start:\n");
			sb.append(text.toString());
			sb.append("\ntext:end:\n");
			if (actor != null) {
				sb.append("actor:start:\n");
				sb.append(actor.toString());
				sb.append("\nactor:end:\n");
			}if (movieIDProfessional != null) {
				sb.append("movieIDProfessional:start:\n");
				sb.append(movieIDProfessional.toString());
				sb.append("\nmovieIDProfessional:end:\n");
			}if (movieIDIMDB != null) {
				sb.append("movieIDIMDB:start:\n");
				sb.append(movieIDIMDB.toString());
				sb.append("\nmovieIDIMDB:end:\n");
			}if (movieIDMtime != null) {
				sb.append("movieIDMtime:start:\n");
				sb.append(movieIDMtime.toString());
				sb.append("\nmovieIDMtime:end:\n");
			}if (movieIDDouban != null) {
				sb.append("movieIDDouban:start:\n");
				sb.append(movieIDDouban.toString());
				sb.append("\nmovieIDDouban:end:\n");
			}if (movieID1905 != null) {
				sb.append("movieID1905:start:\n");
				sb.append(movieID1905.toString());
				sb.append("\nmovieID1905:end:\n");
			}if (movieChineseName != null) {
				sb.append("movieChineseName:start:\n");
				sb.append(movieChineseName.toString());
				sb.append("\nmovieChineseName:end:\n");
			}if (movieEnglishName != null) {
				sb.append("movieEnglishName:start:\n");
				sb.append(movieEnglishName.toString());
				sb.append("\nmovieEnglishName:end:\n");
			}if (movieShortname != null) {
				sb.append("movieShortname:start:\n");
				sb.append(movieShortname.toString());
				sb.append("\nmovieShortname:end:\n");
			}if (movieAliasName != null) {
				sb.append("movieAliasName:start:\n");
				sb.append(movieAliasName.toString());
				sb.append("\nmovieAliasName:end:\n");
			}if (director != null) {
				sb.append("director:start:\n");
				sb.append(director.toString());
				sb.append("\ndirector:end:\n");
			}if (writer != null) {
				sb.append("writer:start:\n");
				sb.append(writer.toString());
				sb.append("\nwriter:end:\n");
			}if (producer != null) {
				sb.append("producer:start:\n");
				sb.append(producer.toString());
				sb.append("\nproducer:end:\n");
			}if (cinematography != null) {
				sb.append("cinematography:start:\n");
				sb.append(cinematography.toString());
				sb.append("\ncinematography:end:\n");
			}if (cutter != null) {
				sb.append("cutter:start:\n");
				sb.append(cutter.toString());
				sb.append("\ncutter:end:\n");
			}if (wardrobe != null) {
				sb.append("wardrobe:start:\n");
				sb.append(wardrobe.toString());
				sb.append("\nwardrobe:end:\n");
			}if (visualAffectArtist != null) {
				sb.append("visualAffectArtist:start:\n");
				sb.append(visualAffectArtist.toString());
				sb.append("\nvisualAffectArtist:end:\n");
			}if (associateDirector != null) {
				sb.append("associateDirector:start:\n");
				sb.append(associateDirector.toString());
				sb.append("\nassociateDirector:end:\n");
			}if (movieType != null) {
				sb.append("movieType:start:\n");
				sb.append(movieType.toString());
				sb.append("\nmovieType:end:\n");
			}if (publishCompany != null) {
				sb.append("publishCompany:start:\n");
				sb.append(publishCompany.toString());
				sb.append("\npublishCompany:end:\n");
			}if (producerCompany != null) {
				sb.append("producerCompany:start:\n");
				sb.append(producerCompany.toString());
				sb.append("\nproducerCompany:end:\n");
			}if (distributionInformation != null) {
				sb.append("distributionInformation:start:\n");
				sb.append(distributionInformation.toString());
				sb.append("\ndistributionInformation:end:\n");
			}if (plotIntroduction != null) {
				sb.append("plotIntroduction:start:\n");
				sb.append(plotIntroduction.toString());
				sb.append("\nplotIntroduction:end:\n");
			}if (roleIntroduction != null) {
				sb.append("roleIntroduction:start:\n");
				sb.append(roleIntroduction.toString());
				sb.append("\nroleIntroduction:end:\n");
			}if (movieLongComment != null) {
				sb.append("movieLongComment:start:\n");
				sb.append(movieLongComment.toString());
				sb.append("\nmovieLongComment:end:\n");
			}if (movieShortComment != null) {
				sb.append("movieShortComment:start:\n");
				sb.append(movieShortComment.toString());
				sb.append("\nmovieShortComment:end:\n");
			}if (movieWeiboComment != null) {
				sb.append("movieWeiboComment:start:\n");
				sb.append(movieWeiboComment.toString());
				sb.append("\nmovieWeiboComment:end:\n");
			}if (movieNews != null) {
				sb.append("movieNews:start:\n");
				sb.append(movieNews.toString());
				sb.append("\nmovieNews:end:\n");
			}if (movieScoreIMDB != 0) {
				sb.append("movieScoreIMDB:start:\n");
				sb.append(movieScoreIMDB.toString());
				sb.append("\nmovieScoreIMDB:end:\n");
			}if (movieScoreMtime != 0) {
				sb.append("movieScoreMtime:start:\n");
				sb.append(movieScoreMtime.toString());
				sb.append("\nmovieScoreMtime:end:\n");
			}if (movieScoreDouban != 0) {
				sb.append("movieScoreDouban:start:\n");
				sb.append(movieScoreDouban.toString());
				sb.append("\nmovieScoreDouban:end:\n");
			}if (movieScore1905 != 0) {
				sb.append("movieScore1905:start:\n");
				sb.append(movieScore1905.toString());
				sb.append("\nmovieScore1905:end:\n");
			}if (movieAwards != null) {
				sb.append("movieAwards:start:\n");
				sb.append(movieAwards.toString());
				sb.append("\nmovieAwards:end:\n");
			}
			
			if (cinemaIdProfessional != null) {
				sb.append("cinemaIdProfessional:start:\n");
				sb.append(cinemaIdProfessional.toString());
				sb.append("\ncinemaIdProfessional:end:\n");
			}if (cinemaIdMtime != null) {
				sb.append("cinemaIdMtime:start:\n");
				sb.append(cinemaIdMtime.toString());
				sb.append("\ncinemaIdMtime:end:\n");
			}if (cinemaIdGewara != null) {
				sb.append("cinemaIdGewara:start:\n");
				sb.append(cinemaIdGewara.toString());
				sb.append("\ncinemaIdGewara:end:\n");
			}if (cinemaIdSell != null) {
				sb.append("cinemaIdSell:start:\n");
				sb.append(cinemaIdSell.toString());
				sb.append("\ncinemaIdSell:end:\n");
			}if (cinemaChineseName != null) {
				sb.append("cinemaChineseName:start:\n");
				sb.append(cinemaChineseName.toString());
				sb.append("\ncinemaChineseName:end:\n");
			}if (cinemaShortName != null) {
				sb.append("cinemaShortName:start:\n");
				sb.append(cinemaShortName.toString());
				sb.append("\ncinemaShortName:end:\n");
			}if (cinemaAliasName != null) {
				sb.append("cinemaAliasName:start:\n");
				sb.append(cinemaAliasName.toString());
				sb.append("\ncinemaAliasName:end:\n");
			}if (cinemaCity != null) {
				sb.append("cinemaCity:start:\n");
				sb.append(cinemaCity.toString());
				sb.append("\ncinemaCity:end:\n");
			}if (cinemaDistrict != null) {
				sb.append("cinemaDistrict:start:\n");
				sb.append(cinemaDistrict.toString());
				sb.append("\ncinemaDistrict:end:\n");
			}if (cinemaStreet != null) {
				sb.append("cinemaStreet:start:\n");
				sb.append(cinemaStreet.toString());
				sb.append("\ncinemaStreet:end:\n");
			}if (cinemaAmapLocation != null) {
				sb.append("cinemaAmapLocation:start:\n");
				sb.append(cinemaAmapLocation.toString());
				sb.append("\ncinemaAmapLocation:end:\n");
			}if (cinemaBaiduLocation != null) {
				sb.append("cinemaBaiduLocation:start:\n");
				sb.append(cinemaBaiduLocation.toString());
				sb.append("\ncinemaBaiduLocation:end:\n");
			}if (theaterChainName != null) {
				sb.append("theaterChainName:start:\n");
				sb.append(theaterChainName.toString());
				sb.append("\ntheaterChainName:end:\n");
			}if (filmInvestmentCompany != null) {
				sb.append("filmInvestmentCompany:start:\n");
				sb.append(filmInvestmentCompany.toString());
				sb.append("\nfilmInvestmentCompany:end:\n");
			}if (cinemaAssetsJoining != null) {
				sb.append("cinemaAssetsJoining:start:\n");
				sb.append(cinemaAssetsJoining.toString());
				sb.append("\ncinemaAssetsJoining:end:\n");
			}if (bus != null) {
				sb.append("bus:start:\n");
				sb.append(bus.toString());
				sb.append("\nbus:end:\n");
			}if (movieID1905 != null) {
				sb.append("subway:start:\n");
				sb.append(subway.toString());
				sb.append("\nsubway:end:\n");
			}if (drive != null) {
				sb.append("drive:start:\n");
				sb.append(drive.toString());
				sb.append("\ndrive:end:\n");
			}if (cinemaFacility != null) {
				sb.append("cinemaFacility:start:\n");
				sb.append(cinemaFacility.toString());
				sb.append("\ncinemaFacility:end:\n");
			}if (cinema3D != null) {
				sb.append("cinema3D:start:\n");
				sb.append(cinema3D.toString());
				sb.append("\ncinema3D:end:\n");
			}if (cinemaCreditCard != null) {
				sb.append("cinemaCreditCard:start:\n");
				sb.append(cinemaCreditCard.toString());
				sb.append("\ncinemaCreditCard:end:\n");
			}if (cinemaSellGoods != null) {
				sb.append("cinemaSellGoods:start:\n");
				sb.append(cinemaSellGoods.toString());
				sb.append("\ncinemaSellGoods:end:\n");
			}if (cinemaParking != null) {
				sb.append("cinemaParking:start:\n");
				sb.append(cinemaParking.toString());
				sb.append("\ncinemaParking:end:\n");
			}if (cinemaChildrenTicket != null) {
				sb.append("cinemaChildrenTicket:start:\n");
				sb.append(cinemaChildrenTicket.toString());
				sb.append("\ncinemaChildrenTicket:end:\n");
			}if (cinemaResting != null) {
				sb.append("cinemaResting:start:\n");
				sb.append(cinemaResting.toString());
				sb.append("\ncinemaResting:end:\n");
			}if (cinemaIntroduction != null) {
				sb.append("cinemaIntroduction:start:\n");
				sb.append(cinemaIntroduction.toString());
				sb.append("\ncinemaIntroduction:end:\n");
			}if (cinemaDeals != null) {
				sb.append("cinemaDeals:start:\n");
				sb.append(cinemaDeals.toString());
				sb.append("\ncinemaDeals:end:\n");
			}if (cinemaCommentMtime != null) {
				sb.append("cinemaCommentMtime:start:\n");
				sb.append(cinemaCommentMtime.toString());
				sb.append("\ncinemaCommentMtime:end:\n");
			}if (cinemaCommentGewara != null) {
				sb.append("cinemaCommentGewara:start:\n");
				sb.append(cinemaCommentGewara.toString());
				sb.append("\ncinemaCommentGewara:end:\n");
			}if (cinemaCommentDianping != null) {
				sb.append("cinemaCommentDianping:start:\n");
				sb.append(cinemaCommentDianping.toString());
				sb.append("\ncinemaCommentDianping:end:\n");
			}if (cinemaCommentWeibo != null) {
				sb.append("cinemaCommentWeibo:start:\n");
				sb.append(cinemaCommentWeibo.toString());
				sb.append("\ncinemaCommentWeibo:end:\n");
			}if (cinemaScoreMtime != 0) {
				sb.append("cinemaScoreMtime:start:\n");
				sb.append(cinemaScoreMtime.toString());
				sb.append("\ncinemaScoreMtime:end:\n");
			}if (cinemaScoreGewara != 0) {
				sb.append("cinemaScoreGewara:start:\n");
				sb.append(cinemaScoreGewara.toString());
				sb.append("\ncinemaScoreGewara:end:\n");
			}if (cinemaScoreDianping != 0) {
				sb.append("cinemaScoreDianping:start:\n");
				sb.append(cinemaScoreDianping.toString());
				sb.append("\ncinemaScoreDianping:end:\n");
			}
			
			if (actorIdMtime != null) {
				sb.append("actorIdMtime:start:\n");
				sb.append(actorIdMtime.toString());
				sb.append("\nactorIdMtime:end:\n");
			}if (actorIdDouban != null) {
				sb.append("actorIdDouban:start:\n");
				sb.append(actorIdDouban.toString());
				sb.append("\nactorIdDouban:end:\n");
			}if (actorId1905 != null) {
				sb.append("actorId1905:start:\n");
				sb.append(actorId1905.toString());
				sb.append("\nactorId1905:end:\n");
			}if (actorIdIMDB != null) {
				sb.append("actorIdIMDB:start:\n");
				sb.append(actorIdIMDB.toString());
				sb.append("\nactorIdIMDB:end:\n");
			}if (actorChineseName != null) {
				sb.append("actorChineseName:start:\n");
				sb.append(actorChineseName.toString());
				sb.append("\nactorChineseName:end:\n");
			}if (actorEnglishName != null) {
				sb.append("actorEnglishName:start:\n");
				sb.append(actorEnglishName.toString());
				sb.append("\nactorEnglishName:end:\n");
			}if (actorShortName != null) {
				sb.append("actorShortName:start:\n");
				sb.append(actorShortName.toString());
				sb.append("\nactorShortName:end:\n");
			}if (actorAliasName != null) {
				sb.append("actorAliasName:start:\n");
				sb.append(actorAliasName.toString());
				sb.append("\nactorAliasName:end:\n");
			}if (actorBirthName != null) {
				sb.append("actorBirthName:start:\n");
				sb.append(actorBirthName.toString());
				sb.append("\nactorBirthName:end:\n");
			}if (gender != null) {
				sb.append("gender:start:\n");
				sb.append(gender.toString());
				sb.append("\ngender:end:\n");
			}if (birthday != null) {
				sb.append("birthday:start:\n");
				sb.append(birthday.toString());
				sb.append("\nbirthday:end:\n");
			}if (birthplace != null) {
				sb.append("birthplace:start:\n");
				sb.append(birthplace.toString());
				sb.append("\nbirthplace:end:\n");
			}if (bloodtype != null) {
				sb.append("bloodtype:start:\n");
				sb.append(bloodtype.toString());
				sb.append("\nbloodtype:end:\n");
			}if (height != null) {
				sb.append("height:start:\n");
				sb.append(height.toString());
				sb.append("\nheight:end:\n");
			}if (weight != null) {
				sb.append("weight:start:\n");
				sb.append(weight.toString());
				sb.append("\nweight:end:\n");
			}if (actorIntroduction != null) {
				sb.append("actorIntroduction:start:\n");
				sb.append(actorIntroduction.toString());
				sb.append("\nactorIntroduction:end:\n");
			}if (birthplaceIntroduction != null) {
				sb.append("birthplaceIntroduction:start:\n");
				sb.append(birthplaceIntroduction.toString());
				sb.append("\nbirthplaceIntroduction:end:\n");
			}if (profession != null) {
				sb.append("profession:start:\n");
				sb.append(profession.toString());
				sb.append("\nprofession:end:\n");
			}if (actorCompany != null) {
				sb.append("actorCompany:start:\n");
				sb.append(actorCompany.toString());
				sb.append("\nactorCompany:end:\n");
			}if (actorNews != null) {
				sb.append("actorNews:start:\n");
				sb.append(actorNews.toString());
				sb.append("\nactorNews:end:\n");
			}if (actorProduction != null) {
				sb.append("actorProduction:start:\n");
				sb.append(actorProduction.toString());
				sb.append("\nactorProduction:end:\n");
			}if (actorCommentMtime != null) {
				sb.append("actorCommentMtime:start:\n");
				sb.append(actorCommentMtime.toString());
				sb.append("\nactorCommentMtime:end:\n");
			}if (actorCommentGewara != null) {
				sb.append("actorCommentGewara:start:\n");
				sb.append(actorCommentGewara.toString());
				sb.append("\nactorCommentGewara:end:\n");
			}if (actorCommentDouban != null) {
				sb.append("actorCommentDouban:start:\n");
				sb.append(actorCommentDouban.toString());
				sb.append("\nactorCommentDouban:end:\n");
			}if (actorCommentWeibo != null) {
				sb.append("actorCommentWeibo:start:\n");
				sb.append(actorCommentWeibo.toString());
				sb.append("\nactorCommentWeibo:end:\n");
			}if (actorScore != 0) {
				sb.append("actorScore:start:\n");
				sb.append(actorScore.toString());
				sb.append("\nactorScore:end:\n");
			}if (relatives != null) {
				sb.append("relatives:start:\n");
				sb.append(relatives.toString());
				sb.append("\nrelatives:end:\n");
			}if (gossip != null) {
				sb.append("gossip:start:\n");
				sb.append(gossip.toString());
				sb.append("\ngossip:end:\n");
			}if (actorAwards != null) {
				sb.append("actorAwards:start:\n");
				sb.append(actorAwards.toString());
				sb.append("\nactorAwards:end:\n");
			}
			
			if (awardIdMtime != null) {
				sb.append("awardIdMtime:start:\n");
				sb.append(awardIdMtime.toString());
				sb.append("\nawardIdMtime:end:\n");
			}if (awardIdIMDB != null) {
				sb.append("awardIdIMDB:start:\n");
				sb.append(awardIdIMDB.toString());
				sb.append("\nawardIdIMDB:end:\n");
			}if (awardId1905 != null) {
				sb.append("awardId1905:start:\n");
				sb.append(awardId1905.toString());
				sb.append("\nawardId1905:end:\n");
			}if (awardChineseName != null) {
				sb.append("awardChineseName:start:\n");
				sb.append(awardChineseName.toString());
				sb.append("\nawardChineseName:end:\n");
			}if (awardEnglishName != null) {
				sb.append("awardEnglishName:start:\n");
				sb.append(awardEnglishName.toString());
				sb.append("\nawardEnglishName:end:\n");
			}if (awardShortName != null) {
				sb.append("awardShortName:start:\n");
				sb.append(awardShortName.toString());
				sb.append("\nawardShortName:end:\n");
			}if (awardAliasName != null) {
				sb.append("awardAliasName:start:\n");
				sb.append(awardAliasName.toString());
				sb.append("\nawardAliasName:end:\n");
			}if (awardHoldTime != null) {
				sb.append("awardHoldTime:start:\n");
				sb.append(awardHoldTime.toString());
				sb.append("\nawardHoldTime:end:\n");
			}if (awardHoldPlace != null) {
				sb.append("awardHoldPlace:start:\n");
				sb.append(awardHoldPlace.toString());
				sb.append("\nawardHoldPlace:end:\n");
			}if (awardHoldCompany != null) {
				sb.append("awardHoldCompany:start:\n");
				sb.append(awardHoldCompany.toString());
				sb.append("\nawardHoldCompany:end:\n");
			}if (awardItems != null) {
				sb.append("awardItems:start:\n");
				sb.append(awardItems.toString());
				sb.append("\nawardItems:end:\n");
			}if (awardMovies != null) {
				sb.append("awardMovies:start:\n");
				sb.append(awardMovies.toString());
				sb.append("\nawardMovies:end:\n");
			}
			
			if (weiboV != null) {
				sb.append("weiboV:start:\n");
				sb.append(weiboV.toString());
				sb.append("\nweiboV:end:\n");
			}if (weiboLevel != 0) {
				sb.append("weiboLevel:start:\n");
				sb.append(weiboLevel);
				sb.append("\nweiboLevel:end:\n");
			}if (weiboName != null) {
				sb.append("weiboName:start:\n");
				sb.append(weiboName.toString());
				sb.append("\nweiboName:end:\n");
			}if (weiboId != null) {
				sb.append("weiboId:start:\n");
				sb.append(weiboId.toString());
				sb.append("\nweiboId:end:\n");
			}if (weiboOfficial != null) {
				sb.append("weiboOfficial:start:\n");
				sb.append(weiboOfficial.toString());
				sb.append("\nweiboOfficial:end:\n");
			}if (weiboFans != 0) {
				sb.append("weiboFans:start:\n");
				sb.append(weiboFans);
				sb.append("\nweiboFans:end:\n");
			}if (weiboNum != 0) {
				sb.append("weiboNum:start:\n");
				sb.append(weiboNum);
				sb.append("\nweiboNum:end:\n");
			}if (weiboInterest != 0) {
				sb.append("weiboInterest:start:\n");
				sb.append(weiboInterest);
				sb.append("\nweiboInterest:end:\n");
			}if (weiboTime != null) {
				sb.append("weiboTime:start:\n");
				sb.append(weiboTime.toString());
				sb.append("\nweiboTime:end:\n");
			}if (weiboContent != null) {
				sb.append("weiboContent:start:\n");
				sb.append(weiboContent.toString());
				sb.append("\nweiboContent:end:\n");
			}if (weiboZan != 0) {
				sb.append("weiboZan:start:\n");
				sb.append(weiboZan);
				sb.append("\nweiboZan:end:\n");
			}if (weiboTopic != null) {
				sb.append("weiboTopic:start:\n");
				sb.append(weiboTopic.toString());
				sb.append("\nweiboTopic:end:\n");
			}if (weiboTransmit != null) {
				sb.append("weiboTransmit:start:\n");
				sb.append(weiboTransmit.toString());
				sb.append("\nweiboTransmit:end:\n");
			}if (weiboComment != null) {
				sb.append("weiboComment:start:\n");
				sb.append(weiboComment.toString());
				sb.append("\nweiboComment:end:\n");
			}
			
			if (NoProfessional != null) {
				sb.append("NoProfessional:start:\n");
				sb.append(NoProfessional.toString());
				sb.append("\nNoProfessional:end:\n");
			}if (NoSite != null) {
				sb.append("NoSite:start:\n");
				sb.append(NoSite.toString());
				sb.append("\nNoSite:end:\n");
			}if (siteMtime != null) {
				sb.append("siteMtime:start:\n");
				sb.append(siteMtime.toString());
				sb.append("\nsiteMtime:end:\n");
			}if (siteTaobao != null) {
				sb.append("siteTaobao:start:\n");
				sb.append(siteTaobao.toString());
				sb.append("\nsiteTaobao:end:\n");
			}if (siteGewara != null) {
				sb.append("siteGewara:start:\n");
				sb.append(siteGewara.toString());
				sb.append("\nsiteGewara:end:\n");
			}if (siteMaoyan != null) {
				sb.append("siteMaoyan:start:\n");
				sb.append(siteMaoyan.toString());
				sb.append("\nsiteMaoyan:end:\n");
			}if (siteSpider != null) {
				sb.append("siteSpider:start:\n");
				sb.append(siteSpider.toString());
				sb.append("\nsiteSpider:end:\n");
			}if (siteBaidu != null) {
				sb.append("siteBaidu:start:\n");
				sb.append(siteBaidu.toString());
				sb.append("\nsiteBaidu:end:\n");
			}if (siteAmap != null) {
				sb.append("siteAmap:start:\n");
				sb.append(siteAmap.toString());
				sb.append("\nsiteAmap:end:\n");
			}if (NoFilm != null) {
				sb.append("NoFilm:start:\n");
				sb.append(NoFilm.toString());
				sb.append("\nNoFilm:end:\n");
			}if (NoTheater != null) {
				sb.append("NoTheater:start:\n");
				sb.append(NoTheater.toString());
				sb.append("\nNoTheater:end:\n");
			}if (NoHall != null) {
				sb.append("NoHall:start:\n");
				sb.append(NoHall.toString());
				sb.append("\nNoHall:end:\n");
			}if (sellSeat != null) {
				sb.append("sellSeat:start:\n");
				sb.append(sellSeat.toString());
				sb.append("\nsellSeat:end:\n");
			}if (price != null) {
				sb.append("price:start:\n");
				sb.append(price.toString());
				sb.append("\nprice:end:\n");
			}if (vipPrice != null) {
				sb.append("vipPrice:start:\n");
				sb.append(vipPrice.toString());
				sb.append("\nvipPrice:end:\n");
			}if (promotionPrice != null) {
				sb.append("promotionPrice:start:\n");
				sb.append(promotionPrice.toString());
				sb.append("\npromotionPrice:end:\n");
			}if (NoLanguage != null) {
				sb.append("NoLanguage:start:\n");
				sb.append(NoLanguage.toString());
				sb.append("\nNoLanguage:end:\n");
			}if (NoVersion != null) {
				sb.append("NoVersion:start:\n");
				sb.append(NoVersion.toString());
				sb.append("\nNoVersion:end:\n");
			}if (NoTime != null) {
				sb.append("NoTime:start:\n");
				sb.append(NoTime.toString());
				sb.append("\nNoTime:end:\n");
			}if (siteWangpiao != null) {
				sb.append("siteWangpiao:start:\n");
				sb.append(siteWangpiao.toString());
				sb.append("\nsiteWangpiao:end:\n");
			}
			
			if (activitySite != null) {
				sb.append("activitySite:start:\n");
				sb.append(activitySite.toString());
				sb.append("\nactivitySite:end:\n");
			}if (activityFilm != null) {
				sb.append("activityFilm:start:\n");
				sb.append(activityFilm.toString());
				sb.append("\nactivityFilm:end:\n");
			}if (activityTheater != null) {
				sb.append("activityTheater:start:\n");
				sb.append(activityTheater.toString());
				sb.append("\nactivityTheater:end:\n");
			}if (activityRule != null) {
				sb.append("activityRule:start:\n");
				sb.append(activityRule.toString());
				sb.append("\nactivityRule:end:\n");
			}if (activityPrPrice != null) {
				sb.append("activityPrPrice:start:\n");
				sb.append(activityPrPrice.toString());
				sb.append("\nactivityPrPrice:end:\n");
			}if (activityPrice != null) {
				sb.append("activityPrice:start:\n");
				sb.append(activityPrice.toString());
				sb.append("\nactivityPrice:end:\n");
			}if (activityTime != null) {
				sb.append("activityTime:start:\n");
				sb.append(activityTime.toString());
				sb.append("\nactivityTime:end:\n");
			}if (activityEffectTime != null) {
				sb.append("activityEffectTime:start:\n");
				sb.append(activityEffectTime.toString());
				sb.append("\nactivityEffectTime:end:\n");
			}
			
			if (movieNewsOutlinks != null) {
				sb.append("movieNewsOutlinks:start:\n");
				sb.append(movieNewsOutlinks.toString());
				sb.append("\nmovieNewsOutlinks:end:\n");
			}if (movieNewsName != null) {
				sb.append("movieNewsName:start:\n");
				sb.append(movieNewsName.toString());
				sb.append("\nmovieNewsName:end:\n");
			}
			
    }

    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(NutchConfiguration.create(), new WebTableReader(),
        args);
    System.exit(res);
  }

  private static enum Op {READ, STAT, DUMP};

  public int run(String[] args) throws Exception {
    if (args.length < 1) {
      System.err
          .println("Usage: WebTableReader (-stats | -url [url] | -dump <out_dir> [-regex regex]) \n \t \t      [-crawlId <id>] [-content] [-headers] [-links] [-text]");
      System.err.println("    -crawlId <id>  - the id to prefix the schemas to operate on, \n \t \t     (default: storage.crawl.id)");
      System.err.println("    -stats [-sort] - print overall statistics to System.out");
      System.err.println("    [-sort]        - list status sorted by host");
      System.err.println("    -url <url>     - print information on <url> to System.out");
      System.err.println("    -dump <out_dir> [-regex regex] - dump the webtable to a text file in \n \t \t     <out_dir>");
      System.err.println("    -content       - dump also raw content");
      System.err.println("    -headers       - dump protocol headers");
      System.err.println("    -links         - dump links");
      System.err.println("    -text          - dump extracted text");
      System.err.println("    [-regex]       - filter on the URL of the webtable entry");
      return -1;
    }
    String param = null;
    boolean content = false;
    boolean links = false;
    boolean text = false;
    boolean headers = false;
    boolean toSort = false;
    String regex = ".+";
    Op op = null;
    try {
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-url")) {
          param = args[++i];
          op = Op.READ;
          //read(param);
          //return 0;
        } else if (args[i].equals("-stats")) {
          op = Op.STAT;
        } else if (args[i].equals("-sort")) {
          toSort = true;
        } else if (args[i].equals("-dump")) {
          op = Op.DUMP;
          param = args[++i];
        } else if (args[i].equals("-content")) {
          content = true;
        } else if (args[i].equals("-headers")) {
          headers = true;
        } else if (args[i].equals("-links")) {
          links = true;
        } else if (args[i].equals("-text")) {
          text = true;
        }else if (args[i].equals("-regex")) {
          regex = args[++i];
        } else if (args[i].equals("-crawlId")) {
          getConf().set(Nutch.CRAWL_ID_KEY, args[++i]);
        }
      }
      if (op == null) {
        throw new Exception("Select one of -url | -stat | -dump");
      }
      switch (op) {
      case READ:
        read(param, content, headers, links, text);
        break;
      case STAT:
        processStatJob(toSort);
        break;
      case DUMP:
        processDumpJob(param, getConf(), regex, content, headers, links, text);
        break;
      }
      return 0;
    } catch (Exception e) {
      LOG.error("WebTableReader: " + StringUtils.stringifyException(e));
      return -1;
    }
  }

  // for now handles only -stat
  @Override
  public Map<String,Object> run(Map<String,Object> args) throws Exception {
    Path tmpFolder = new Path(getConf().get("mapred.temp.dir", ".")
        + "stat_tmp" + System.currentTimeMillis());

    numJobs = 1;
    currentJob = new NutchJob(getConf(), "db_stats");

    currentJob.getConfiguration().setBoolean("mapreduce.fileoutputcommitter.marksuccessfuljobs", false);
    
    Boolean sort = (Boolean)args.get(Nutch.ARG_SORT);
    if (sort == null) sort = Boolean.FALSE;
    currentJob.getConfiguration().setBoolean("db.reader.stats.sort", sort);

    DataStore<String, WebPage> store = StorageUtils.createWebStore(currentJob
        .getConfiguration(), String.class, WebPage.class);
    Query<String, WebPage> query = store.newQuery();
    query.setFields(WebPage._ALL_FIELDS);

    GoraMapper.initMapperJob(currentJob, query, store, Text.class, LongWritable.class,
        WebTableStatMapper.class, null, true);

    currentJob.setCombinerClass(WebTableStatCombiner.class);
    currentJob.setReducerClass(WebTableStatReducer.class);

    FileOutputFormat.setOutputPath(currentJob, tmpFolder);

    currentJob.setOutputFormatClass(SequenceFileOutputFormat.class);

    currentJob.setOutputKeyClass(Text.class);
    currentJob.setOutputValueClass(LongWritable.class);
    FileSystem fileSystem = FileSystem.get(getConf());

    try {
      currentJob.waitForCompletion(true);
    } finally {
      ToolUtil.recordJobStatus(null, currentJob, results);
      if (!currentJob.isSuccessful()) {
        fileSystem.delete(tmpFolder, true);
        return results;
      }
    }

    Text key = new Text();
    LongWritable value = new LongWritable();

    SequenceFile.Reader[] readers = org.apache.hadoop.mapred.SequenceFileOutputFormat
        .getReaders(getConf(), tmpFolder);

    TreeMap<String, LongWritable> stats = new TreeMap<String, LongWritable>();
    for (int i = 0; i < readers.length; i++) {
      SequenceFile.Reader reader = readers[i];
      while (reader.next(key, value)) {
        String k = key.toString();
        LongWritable val = stats.get(k);
        if (val == null) {
          val = new LongWritable();
          if (k.equals("scx"))
            val.set(Long.MIN_VALUE);
          if (k.equals("scn"))
            val.set(Long.MAX_VALUE);
          stats.put(k, val);
        }
        if (k.equals("scx")) {
          if (val.get() < value.get())
            val.set(value.get());
        } else if (k.equals("scn")) {
          if (val.get() > value.get())
            val.set(value.get());
        } else {
          val.set(val.get() + value.get());
        }
      }
      reader.close();
    }

    LongWritable totalCnt = stats.get("T");
    if (totalCnt==null)totalCnt=new LongWritable(0);
    stats.remove("T");
    results.put("TOTAL urls", totalCnt.get());
    for (Map.Entry<String, LongWritable> entry : stats.entrySet()) {
      String k = entry.getKey();
      LongWritable val = entry.getValue();
      if (k.equals("scn")) {
        results.put("min score", (val.get() / 1000.0f));
      } else if (k.equals("scx")) {
        results.put("max score", (val.get() / 1000.0f));
      } else if (k.equals("sct")) {
        results.put("avg score",
            (float) ((((double) val.get()) / totalCnt.get()) / 1000.0));
      } else if (k.startsWith("status")) {
        String[] st = k.split(" ");
        int code = Integer.parseInt(st[1]);
        if (st.length > 2)
          results.put(st[2], val.get());
        else
          results.put(st[0] + " " + code + " ("
              + CrawlStatus.getName((byte) code) + ")", val.get());
      } else
        results.put(k, val.get());
    }
    // removing the tmp folder
    fileSystem.delete(tmpFolder, true);
    if (LOG.isInfoEnabled()) {
      LOG.info("Statistics for WebTable: ");
      for (Entry<String,Object> e : results.entrySet()) {
        LOG.info(e.getKey() + ":\t" + e.getValue());
      }
      LOG.info("WebTable statistics: done");
    }
    return results;
  }
}
