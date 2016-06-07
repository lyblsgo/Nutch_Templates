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
 
package org.apache.nutch.storage;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Protocol;
import org.apache.avro.util.Utf8;
import org.apache.avro.ipc.AvroRemoteException;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.FixedSize;
import org.apache.avro.specific.SpecificExceptionBase;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificFixed;
import org.apache.gora.persistency.StateManager;
import org.apache.gora.persistency.impl.PersistentBase;
import org.apache.gora.persistency.impl.StateManagerImpl;
import org.apache.gora.persistency.StatefulHashMap;
import org.apache.gora.persistency.ListGenericArray;

@SuppressWarnings("all")
public class WebPage extends PersistentBase {
  public static final Schema _SCHEMA = Schema.parse("{\"type\":\"record\",\"name\":\"WebPage\",\"namespace\":\"org.apache.nutch.storage\",\"fields\":["
  		+ "{\"name\":\"baseUrl\",\"type\":\"string\"},"
  		+ "{\"name\":\"status\",\"type\":\"int\"},"
  		+ "{\"name\":\"fetchTime\",\"type\":\"long\"},"
  		+ "{\"name\":\"prevFetchTime\",\"type\":\"long\"},"
  		+ "{\"name\":\"fetchInterval\",\"type\":\"int\"},"
  		+ "{\"name\":\"retriesSinceFetch\",\"type\":\"int\"},"
  		+ "{\"name\":\"modifiedTime\",\"type\":\"long\"},"
  		+ "{\"name\":\"prevModifiedTime\",\"type\":\"long\"},"
  		+ "{\"name\":\"protocolStatus\",\"type\":{\"type\":\"record\",\"name\":\"ProtocolStatus\",\"fields\":[{\"name\":\"code\",\"type\":\"int\"},{\"name\":\"args\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"lastModified\",\"type\":\"long\"}]}},"
  		+ "{\"name\":\"content\",\"type\":\"bytes\"},"
  		+ "{\"name\":\"contentType\",\"type\":\"string\"},"
  		+ "{\"name\":\"prevSignature\",\"type\":\"bytes\"},"
  		+ "{\"name\":\"signature\",\"type\":\"bytes\"},"
  		+ "{\"name\":\"title\",\"type\":\"string\"},"
  		+ "{\"name\":\"text\",\"type\":\"string\"},"
  		+ "{\"name\":\"parseStatus\",\"type\":{\"type\":\"record\",\"name\":\"ParseStatus\",\"fields\":[{\"name\":\"majorCode\",\"type\":\"int\"},{\"name\":\"minorCode\",\"type\":\"int\"},{\"name\":\"args\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}},"
  		+ "{\"name\":\"score\",\"type\":\"float\"},"
  		+ "{\"name\":\"reprUrl\",\"type\":\"string\"},"
  		+ "{\"name\":\"headers\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},"
  		+ "{\"name\":\"outlinks\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},"
  		+ "{\"name\":\"inlinks\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},"
  		+ "{\"name\":\"markers\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},"
  		+ "{\"name\":\"metadata\",\"type\":{\"type\":\"map\",\"values\":\"bytes\"}},"
  		+ "{\"name\":\"batchId\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieIdProfessional\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieIdIMDB\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieIdMtime\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieIdDouban\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieId1905\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieChineseName\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieEnglishName\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieShortName\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieAliasName\",\"type\":\"string\"},"
  		+ "{\"name\":\"director\",\"type\":\"string\"},"
  		+ "{\"name\":\"writer\",\"type\":\"string\"},"
  		+ "{\"name\":\"actor\",\"type\":\"string\"},"
  		+ "{\"name\":\"producer\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinematography\",\"type\":\"string\"},"
  		+ "{\"name\":\"cutter\",\"type\":\"string\"},"
  		+ "{\"name\":\"wardrobe\",\"type\":\"string\"},"
  		+ "{\"name\":\"visualAffectArtist\",\"type\":\"string\"},"
  		+ "{\"name\":\"associateDirector\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieType\",\"type\":\"string\"},"
  		+ "{\"name\":\"publishCompany\",\"type\":\"string\"},"
  		+ "{\"name\":\"produceCompany\",\"type\":\"string\"},"
  		+ "{\"name\":\"distributionInformation\",\"type\":\"string\"},"
  		+ "{\"name\":\"plotIntroduction\",\"type\":\"string\"},"
  		+ "{\"name\":\"roleIntroduction\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieLongComment\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieShortComment\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieWeiboComment\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieNews\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieScoreIMDB\",\"type\":\"float\"},"
  		+ "{\"name\":\"movieScoreMtime\",\"type\":\"float\"},"
  		+ "{\"name\":\"movieScoreDouban\",\"type\":\"float\"},"
  		+ "{\"name\":\"movieScore1905\",\"type\":\"float\"},"
  		+ "{\"name\":\"movieAwards\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaIdProfessional\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaIdMtime\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaIdGewara\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaIdSell\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaChineseName\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaShortName\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaAliasName\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaCity\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaDistrict\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaStreet\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaAmapLocation\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaBaiduLocation\",\"type\":\"string\"},"
  		+ "{\"name\":\"theaterChainName\",\"type\":\"string\"},"
  		+ "{\"name\":\"filmInvestmentCompany\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaAssetsJoining\",\"type\":\"string\"},"
  		+ "{\"name\":\"bus\",\"type\":\"string\"},"
  		+ "{\"name\":\"subway\",\"type\":\"string\"},"
  		+ "{\"name\":\"drive\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaFacility\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinema3D\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaCreditCard\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaSellGoods\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaParking\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaChildrenTicket\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaResting\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaIntroduction\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaDeals\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaCommentMtime\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaCommentGewara\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaCommentDianping\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaCommentWeibo\",\"type\":\"string\"},"
  		+ "{\"name\":\"cinemaScoreMtime\",\"type\":\"float\"},"
  		+ "{\"name\":\"cinemaScoreGewara\",\"type\":\"float\"},"
  		+ "{\"name\":\"cinemaScoreDianping\",\"type\":\"float\"},"
  		+ "{\"name\":\"actorIdMtime\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorIdDouban\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorId1905\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorIdIMDB\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorChineseName\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorEnglishName\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorShortName\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorAliasName\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorBirthName\",\"type\":\"string\"},"
  		+ "{\"name\":\"gender\",\"type\":\"string\"},"
  		+ "{\"name\":\"birthday\",\"type\":\"string\"},"
  		+ "{\"name\":\"birthplace\",\"type\":\"string\"},"
  		+ "{\"name\":\"bloodtype\",\"type\":\"string\"},"
  		+ "{\"name\":\"height\",\"type\":\"string\"},"
  		+ "{\"name\":\"weight\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorIntroduction\",\"type\":\"string\"},"
  		+ "{\"name\":\"birthplaceIntroduction\",\"type\":\"string\"},"
  		+ "{\"name\":\"profession\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorCompany\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorNews\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorProduction\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorCommentMtime\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorCommentGewara\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorCommentDouban\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorCommentWeibo\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorScore\",\"type\":\"float\"},"
  		+ "{\"name\":\"relatives\",\"type\":\"string\"},"
  		+ "{\"name\":\"gossip\",\"type\":\"string\"},"
  		+ "{\"name\":\"actorAwards\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardIdMtime\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardIdIMDB\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardId1905\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardChineseName\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardEnglishName\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardShortName\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardAliasName\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardHoldTime\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardHoldPlace\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardHoldCompany\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardItems\",\"type\":\"string\"},"
  		+ "{\"name\":\"awardMovies\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboV\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboLevel\",\"type\":\"int\"},"
  		+ "{\"name\":\"weiboName\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboId\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboOfficial\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboFans\",\"type\":\"int\"},"
  		+ "{\"name\":\"weiboNum\",\"type\":\"int\"},"
  		+ "{\"name\":\"weiboInterest\",\"type\":\"int\"},"
  		+ "{\"name\":\"weiboTime\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboContent\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboZan\",\"type\":\"int\"},"
  		+ "{\"name\":\"weiboTopic\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboTransmit\",\"type\":\"string\"},"
  		+ "{\"name\":\"weiboComment\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoProfessional\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoSite\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteMtime\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteTaobao\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteGewara\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteMaoyan\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteWangpiao\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteSpider\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteBaidu\",\"type\":\"string\"},"
  		+ "{\"name\":\"siteAmap\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoFilm\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoTheater\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoHall\",\"type\":\"string\"},"
  		+ "{\"name\":\"sellSeat\",\"type\":\"string\"},"
  		+ "{\"name\":\"price\",\"type\":\"string\"},"
  		+ "{\"name\":\"vipPrice\",\"type\":\"string\"},"
  		+ "{\"name\":\"promotionPrice\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoLanguage\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoVersion\",\"type\":\"string\"},"
  		+ "{\"name\":\"NoTime\",\"type\":\"string\"},"
  		+ "{\"name\":\"activitySite\",\"type\":\"string\"},"
  		+ "{\"name\":\"activityFilm\",\"type\":\"string\"},"
  		+ "{\"name\":\"activityTheater\",\"type\":\"string\"},"
  		+ "{\"name\":\"activityRule\",\"type\":\"string\"},"
  		+ "{\"name\":\"activityPrPrice\",\"type\":\"string\"},"
  		+ "{\"name\":\"activityPrice\",\"type\":\"string\"},"
  		+ "{\"name\":\"activityTime\",\"type\":\"string\"},"
  		+ "{\"name\":\"activityEffectTime\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieNewsOutlinks\",\"type\":\"string\"},"
  		+ "{\"name\":\"movieNewsName\",\"type\":\"string\"}"
  		+ "]}");
  public static enum Field {
    BASE_URL(0,"baseUrl"),
    STATUS(1,"status"),
    FETCH_TIME(2,"fetchTime"),
    PREV_FETCH_TIME(3,"prevFetchTime"),
    FETCH_INTERVAL(4,"fetchInterval"),
    RETRIES_SINCE_FETCH(5,"retriesSinceFetch"),
    MODIFIED_TIME(6,"modifiedTime"),
    PREV_MODIFIED_TIME(7,"prevModifiedTime"),
    PROTOCOL_STATUS(8,"protocolStatus"),
    CONTENT(9,"content"),
    CONTENT_TYPE(10,"contentType"),
    PREV_SIGNATURE(11,"prevSignature"),
    SIGNATURE(12,"signature"),
    TITLE(13,"title"),
    TEXT(14,"text"),
    PARSE_STATUS(15,"parseStatus"),
    SCORE(16,"score"),
    REPR_URL(17,"reprUrl"),
    HEADERS(18,"headers"),
    OUTLINKS(19,"outlinks"),
    INLINKS(20,"inlinks"),
    MARKERS(21,"markers"),
    METADATA(22,"metadata"),
    BATCH_ID(23,"batchId"),
    
    MOVIE_ID_PROFESSIONAL(24,"movieIdProfessional"),
    MOVIE_ID_IMDB(25,"movieIdIMDB"),
    MOVIE_ID_MTIME(26,"movieIdMtime"),
    MOVIE_ID_DOUBAN(27,"movieIdDouban"),
    MOVIE_ID_1905(28,"movieId1905"),    
    MOVIE_CHINESE_NAME(29,"movieChineseName"),
    MOVIE_ENGLISH_NAME(30,"movieEnglishName"),
    MOVIE_SHORT_NAME(31,"movieShortName"),
    MOVIE_ALIAS_NAME(32,"movieAliasName"),   
    DIRECTOR(33,"director"),
    WRITER(34,"writer"),
    ACTOR(35,"actor"),
    PRODUCER(36,"producer"),
    CINEMATOGRAPHY(37,"cinematography"),
    CUTTER(38,"cutter"),
    WARDROBE(39,"wardrobe"),
    VISUAL_AFFECT_ARTIST(40,"visualAffectArtist"),
    ASSOCIATE_DIRECTOR(41,"associateDirector"),    
    MOVIE_TYPE(42,"movieType"),    
    PUBLISH_COMPANY(43,"publishCompany"),
    PRODUCE_COMPANY(44,"produceCompany"),    
    DISTRIBUTION_INFORMATION(45,"distributionInformation"),    
    PLOT_INTRODUCTION(46,"plotIntroduction"),
    ROLE_INTRODUCTION(47,"roleIntroduction"),    
    MOVIE_LONG_COMMENT(48,"movieLongComment"),
    MOVIE_SHORT_COMMENT(49,"movieShortComment"),
    MOVIE_WEIBO_COMMENT(50,"movieWeiboComment"),   
    MOVIE_NEWS(51,"movieNews"),
    MOVIE_SCORE_IMDB(52,"movieScoreIMDB"),
    MOVIE_SCORE_MTIME(53,"movieScoreMtime"),
    MOVIE_SCORE_DOUBAN(54,"movieScoreDouban"),
    MOVIE_SCORE_1905(55,"movieScore1905"),
    MOVIE_AWARDS(56,"movieAwards"),
    
    CINEMA_ID_PROFESSIONAL(57,"cinemaIdProfessional"),
    CINEMA_ID_MTIME(58,"cinemaIdMtime"),
    CINEMA_ID_GEWARA(59,"cinemaIdGewara"),
    CINEMA_ID_SELL(60,"cinemaIdSell"),
    CINEMA_CHINESE_NAME(61,"cinemaChineseName"),
    CINEMA_SHORT_NAME(62,"cinemaShortName"),
    CINEMA_ALIAS_NAME(63,"cinemaAliasName"), 
    CINEMA_CITY(64,"cinemaCity"),
    CINEMA_DISTRICT(65,"cinemaDistrict"),
    CINEMA_STREET(66,"cinemaStreet"),
    CINEMA_AMAP_LOCATION(67,"cinemaAmapLocation"),
    CINEMA_BAIDU_LOCATION(68,"cinemaBaiduLocation"),
    THEATER_CHAIN_NAME(69,"theaterChainName"),
    FILM_INVESTMENT_COMPANY(70,"filmInvestmentCompany"),
    CINEMA_ASSETS_JOINING(71,"cinemaAssetsJoining"),
    BUS(72,"bus"),
    SUBWAY(73,"subway"),
    DRIVE(74,"drive"),
    CINEMA_FACILITY(75,"cinemaFacility"),
    CINEMA_3D(76,"cinema3D"),
    CINEMA_CREDIT_CARD(77,"cinemaCreditCard"),
    CINEMA_SELL_GOODS(78,"cinemaSellGoods"),
    CINEMA_PARKING(79,"cinemaParking"),
    CINEMA_CHILDREN_TICKET(80,"cinemaChildrenTicket"),
    CINEMA_RESTING(81,"cinemaResting"),
    CINEMA_INTRODUCTION(82,"cinemaIntroduction"),
    CINEMA_DEALS(83,"cinemaDeals"),
    CINEMA_COMMENT_MTIME(84,"cinemaCommentMtime"),
    CINEMA_COMMENT_GEWARA(85,"cinemaCommentGewara"),
    CINEMA_COMMENT_DIANPING(86,"cinemaCommentDianping"),
    CINEMA_COMMENT_WEIBO(87,"cinemaCommentWeibo"),
    CINEMA_SCORE_MTIME(88,"cinemaScoreMtime"),
    CINEMA_SCORE_GEWARA(89,"cinemaScoreGewara"),
    CINEMA_SCORE_DIANPING(90,"cinemaScoreDianping"),
    
    ACTOR_ID_MTIME(91,"actorIdMtime"),
    ACTOR_ID_DOUBAN(92,"actorIdDouban"),
    ACTOR_ID_1905(93,"actorId1905"),
    ACTOR_ID_IMDB(94,"actorIdIMDB"),
    ACTOR_CHINESE_NAME(95,"actorChineseName"),
    ACTOR_ENGLISH_NAME(96,"actorEnglishName"),
    ACTOR_SHORT_NAME(97,"actorShortName"),
    ACTOR_ALIAS_NAME(98,"actorAliasName"),
    ACTOR_BIRTH_NAME(99,"actorBirthName"),
    GENTOR(100,"gender"),
    BIRTHDAY(101,"birthday"),
    BIRTHPLACE(102,"birthplace"),
    BLOODTYPE(103,"bloodtype"),
    HEIGHT(104,"height"),
    WEIGHT(105,"weight"),
    ACTOR_INTRODUCTION(106,"actorIntroduction"),
    BIRTHPLACE_INTRODUCTION(107,"birthplaceIntroduction"),
    PROFESSION(108,"profession"),
    ACTOR_COMPANY(109,"actorCompany"),
    ACTOR_NEWS(110,"actorNews"),
    ACTOR_PRODUCTION(111,"actorProduction"),
    ACTOR_COMMENT_MTIME(112,"actorCommentMtime"),
    ACTOR_COMMENT_GEWARA(113,"actorCommentGewara"),
    ACTOR_COMMENT_DOUBAN(114,"actorCommentDouban"),
    ACTOR_COMMENT_WEIBO(115,"actorCommentWeibo"),
    ACTOR_SCORE(116,"actorScore"),
    RELATIVES(117,"relatives"),
    GOSSIP(118,"gossip"),
    ACTOR_AWARDS(119,"actorAwards"),
    
    AWARD_ID_MTIME(120,"awardIdMtime"),
    AWARD_ID_IMDB(121,"awardIdIMDB"),
    AWARD_ID_1905(122,"awardId1905"),
    AWARD_CHINESE_NAME(123,"awardChineseName"),
    AWARD_ENGLISH_NAME(124,"awardEnglishName"),
    AWARD_SHORT_NAME(125,"awardShortName"),
    AWARD_ALIAS_NAME(126,"awardAliasName"),
    AWARD_HOLD_TIME(127,"awardHoldTime"),
    AWARD_HOLD_PLACE(128,"awardHoldPlace"),
    AWARD_HOLD_COMPANY(129,"awardHoldCompany"),
    AWARD_ITEMS(130,"awardItems"),
    AWARD_MOVIES(131,"awardMovies"),
    
    WEIBO_V(132,"weiboV"),
    WEIBO_LEVEL(133,"weiboLevel"),
    WEIBO_NAME(134,"weiboName"),
    WEIBO_ID(135,"weiboId"),
    WEIBO_OFFICIAL(136,"weiboOfficial"),
    WEIBO_FANS(137,"weiboFans"),
    WEIBO_NUM(138,"weiboNum"),
    WEIBO_INTERSET(139,"weiboInterest"),
    WEIBO_TIME(140,"weiboTime"),
    WEIBO_CONTENT(141,"weiboContent"),
    WEIBO_ZAN(142,"weiboZan"),
    WEIBO_TOPIC(143,"weiboTopic"),
    WEIBO_TRANSMIT(144,"weiboTransmit"),
    WEIBO_COMMENT(145,"weiboComment"),
    
    NO_PROFESSIONAL(146,"NoProfessional"),
    NO_SITE(147,"NoSite"),
    SITE_MTIME(148,"siteMtime"),
    SITE_TAOBAO(149,"siteTaobao"),
    SITE_GEWARA(150,"siteGewara"),
    SITE_MAOYAN(151,"siteMaoyan"),
    SITE_WANGPIAO(152,"siteWangpiao"),
    SITE_SPIDER(153,"siteSpider"),
    SITE_BAIDU(154,"siteBaidu"),
    SITE_AMAP(155,"siteAmap"),
    NO_FILE(156,"NoFilm"),
    NO_THEATER(157,"NoTheater"),
    NO_HALL(158,"NoHall"),
    SELL_SEAT(159,"sellSeat"),
    PRICE(160,"price"),
    VIP_PRICE(161,"vipPrice"),
    PROMOTION_PRICE(162,"promotionPrice"),
    NO_LANGUAGE(163,"NoLanguage"),
    NO_VERSION(164,"NoVersion"),
    NO_TIME(165,"NoTime"),
    
    ACTIVITY_SITE(166,"activitySite"),
    ACTIVITY_FILM(167,"activityFilm"),
    ACTIVITY_THEATER(168,"activityTheater"),
    ACTIVITY_RULE(169,"activityRule"),
    ACTIVITY_PR_PRICE(170,"activityPrPrice"),
    ACTIVITY_PRICE(171,"activityPrice"),
    ACTIVITY_TIME(172,"activityTime"),
    ACTIVITY_EFFECT_TIME(173,"activityEffectTime"),
    
    MOVIE_NEWS_OUTLINKS(174,"movieNewsOutlinks"),
    MOVIE_NEWS_NAME(175,"movieNewsName")
    ;
    private int index;
    private String name;
    Field(int index, String name) {this.index=index;this.name=name;}
    public int getIndex() {return index;}
    public String getName() {return name;}
    public String toString() {return name;}
  };
  public static final String[] _ALL_FIELDS = 
	  {"baseUrl","status","fetchTime","prevFetchTime","fetchInterval","retriesSinceFetch","modifiedTime","prevModifiedTime","protocolStatus","content","contentType","prevSignature","signature","title","text","parseStatus","score","reprUrl","headers","outlinks","inlinks","markers","metadata","batchId",
	  "movieIdProfessional","movieIdIMDB","movieIdMtime","movieIdDouban","movieId1905",
	  "movieChineseName","movieEnglishName","movieShortName","movieAliasName",
	  "director","writer","actor","producer","cinematography","cutter","wardrobe","visualAffectArtist","associateDirector",
	  "movieType",
	  "publishCompany","produceCompany",
	  "distributionInformation",
	  "plotIntroduction","roleIntroduction",
	  "movieLongComment","movieShortComment","movieWeiboComment",
	  "movieNews",
	  "movieScoreIMDB","movieScoreMtime","movieScoreDouban","movieScore1905",
	  "movieAwards",
	  "cinemaIdProfessional","cinemaIdMtime","cinemaIdGewara","cinemaIdSell",
	  "cinemaChineseName","cinemaShortName","cinemaAliasName",
	  "cinemaCity","cinemaDistrict","cinemaStreet",
	  "cinemaAmapLocation","cinemaBaiduLocation",
	  "theaterChainName","filmInvestmentCompany","cinemaAssetsJoining",
	  "bus","subway","drive",
	  "cinemaFacility","cinema3D","cinemaCreditCard","cinemaSellGoods","cinemaParking","cinemaChildrenTicket","cinemaResting",
	  "cinemaIntroduction","cinemaDeals",
	  "cinemaCommentMtime","cinemaCommentGewara","cinemaCommentDianping","cinemaCommentWeibo","cinemaScoreMtime","cinemaScoreGewara","cinemaScoreDianping",
	  "actorIdMtime","actorIdDouban","actorId1905","actorIdIMDB",
	  "actorChineseName","actorEnglishName","actorShortName","actorAliasName","actorBirthName",
	  "gender","birthday","birthplace","bloodtype","height","weight","actorIntroduction",
	  "birthplaceIntroduction",
	  "profession",
	  "actorCompany",
	  "actorNews",
	  "actorProduction",
	  "actorCommentMtime","actorCommentGewara","actorCommentDouban","actorCommentWeibo",
	  "actorScore",
	  "relatives","gossip",
	  "actorAwards",
	  "awardIdMtime","awardIdIMDB","awardId1905",
	  "awardChineseName","awardEnglishName","awardShortName","awardAliasName",
	  "awardHoldTime",
	  "awardHoldPlace",
	  "awardHoldCompany",
	  "awardItems",
	  "awardMovies",
	  "weiboV","weiboLevel","weiboName","weiboId","weiboOfficial","weiboFans","weiboNum","weiboInterest",
	  "weiboTime","weiboContent","weiboZan","weiboTopic","weiboTransmit","weiboComment",
	  "NoProfessional","NoSite",
	  "siteMtime","siteTaobao","siteGewara","siteMaoyan","siteWangpiao","siteSpider","siteBaidu","siteAmap",
	  "NoFilm",
	  "NoTheater",
	  "NoHall",
	  "sellSeat",
	  "price","vipPrice","promotionPrice",
	  "NoLanguage",
	  "NoVersion",
	  "NoTime",
	  "activitySite","activityFilm","activityTheater","activityRule","activityPrPrice","activityPrice","activityTime","activityEffectTime",
	  "movieNewsOutlinks","movieNewsName"
	  };
  static {
    PersistentBase.registerFields(WebPage.class, _ALL_FIELDS);
  }
  private Utf8 baseUrl;
  private int status;
  private long fetchTime;
  private long prevFetchTime;
  private int fetchInterval;
  private int retriesSinceFetch;
  private long modifiedTime;
  private long prevModifiedTime;
  private ProtocolStatus protocolStatus;
  private ByteBuffer content;
  private Utf8 contentType;
  private ByteBuffer prevSignature;
  private ByteBuffer signature;
  private Utf8 title;
  private Utf8 text;
  private ParseStatus parseStatus;
  private float score;
  private Utf8 reprUrl;
  private Map<Utf8,Utf8> headers;
  private Map<Utf8,Utf8> outlinks;
  private Map<Utf8,Utf8> inlinks;
  private Map<Utf8,Utf8> markers;
  private Map<Utf8,ByteBuffer> metadata;
  private Utf8 batchId;
  
  private Utf8 movieIdProfessional;
  private Utf8 movieIdIMDB;
  private Utf8 movieIdMtime;
  private Utf8 movieIdDouban;
  private Utf8 movieId1905; 
  private Utf8 movieChineseName;
  private Utf8 movieEnglishName;
  private Utf8 movieShortName;
  private Utf8 movieAliasName;  
  private Utf8 director;
  private Utf8 writer;
  private Utf8 actor;
  private Utf8 producer;
  private Utf8 cinematography;
  private Utf8 cutter;
  private Utf8 wardrobe;
  private Utf8 visualAffectArtist;
  private Utf8 associateDirector;  
  private Utf8 movieType;  
  private Utf8 publishCompany;
  private Utf8 produceCompany; 
  private Utf8 distributionInformation;  
  private Utf8 plotIntroduction;
  private Utf8 roleIntroduction; 
  private Utf8 movieLongComment;
  private Utf8 movieShortComment;
  private Utf8 movieWeiboComment;
  private Utf8 movieNews;
  private float movieScoreIMDB;
  private float movieScoreMtime;
  private float movieScoreDouban;
  private float movieScore1905;
  private Utf8 movieAwards;
  
  private Utf8 cinemaIdProfessional;
  private Utf8 cinemaIdMtime;
  private Utf8 cinemaIdGewara;
  private Utf8 cinemaIdSell;
  private Utf8 cinemaChineseName;
  private Utf8 cinemaShortName;
  private Utf8 cinemaAliasName;
  private Utf8 cinemaCity;
  private Utf8 cinemaDistrict;
  private Utf8 cinemaStreet;
  private Utf8 cinemaAmapLocation;
  private Utf8 cinemaBaiduLocation;
  private Utf8 theaterChainName;
  private Utf8 filmInvestmentCompany;
  private Utf8 cinemaAssetsJoining;
  private Utf8 bus;
  private Utf8 subway;
  private Utf8 drive;
  private Utf8 cinemaFacility;
  private Utf8 cinema3D;
  private Utf8 cinemaCreditCard;
  private Utf8 cinemaSellGoods;
  private Utf8 cinemaParking;
  private Utf8 cinemaChildrenTicket;
  private Utf8 cinemaResting;
  private Utf8 cinemaIntroduction;
  private Utf8 cinemaDeals;
  private Utf8 cinemaCommentMtime;
  private Utf8 cinemaCommentGewara;
  private Utf8 cinemaCommentDianping;
  private Utf8 cinemaCommentWeibo;
  private float cinemaScoreMtime;
  private float cinemaScoreGewara;
  private float cinemaScoreDianping;
  
  private Utf8 actorIdMtime;
  private Utf8 actorIdDouban;
  private Utf8 actorId1905;
  private Utf8 actorIdIMDB;
  private Utf8 actorChineseName;
  private Utf8 actorEnglishName;
  private Utf8 actorShortName;
  private Utf8 actorAliasName;
  private Utf8 actorBirthName;
  private Utf8 gender;
  private Utf8 birthday;
  private Utf8 birthplace;
  private Utf8 bloodtype;
  private Utf8 height;
  private Utf8 weight;
  private Utf8 actorIntroduction;
  private Utf8 birthplaceIntroduction;
  private Utf8 profession;
  private Utf8 actorCompany;
  private Utf8 actorNews;
  private Utf8 actorProduction;
  private Utf8 actorCommentMtime;
  private Utf8 actorCommentGewara;
  private Utf8 actorCommentDouban;
  private Utf8 actorCommentWeibo;
  private float actorScore;
  private Utf8 relatives;
  private Utf8 gossip;
  private Utf8 actorAwards;
  
  private Utf8 awardIdMtime;
  private Utf8 awardIdIMDB;
  private Utf8 awardId1905;
  private Utf8 awardChineseName;
  private Utf8 awardEnglishName;
  private Utf8 awardShortName;
  private Utf8 awardAliasName;
  private Utf8 awardHoldTime;
  private Utf8 awardHoldPlace;
  private Utf8 awardHoldCompany;
  private Utf8 awardItems;
  private Utf8 awardMovies;
  
  private Utf8 weiboV;
  private int weiboLevel;
  private Utf8 weiboName;
  private Utf8 weiboId;
  private Utf8 weiboOfficial;
  private int weiboFans;
  private int weiboNum;
  private int weiboInterest;
  private Utf8 weiboTime;
  private Utf8 weiboContent;
  private int weiboZan;
  private Utf8 weiboTopic;
  private Utf8 weiboTransmit;
  private Utf8 weiboComment;
  
  private Utf8 NoProfessional;
  private Utf8 NoSite;
  private Utf8 siteMtime;
  private Utf8 siteTaobao;
  private Utf8 siteGewara;
  private Utf8 siteMaoyan;
  private Utf8 siteWangpiao;
  private Utf8 siteSpider;
  private Utf8 siteBaidu;
  private Utf8 siteAmap;
  private Utf8 NoFilm;
  private Utf8 NoTheater;
  private Utf8 NoHall;
  private Utf8 sellSeat;
  private Utf8 price;
  private Utf8 vipPrice;
  private Utf8 promotionPrice;
  private Utf8 NoLanguage;
  private Utf8 NoVersion;
  private Utf8 NoTime;
  
  private Utf8 activitySite;
  private Utf8 activityFilm;
  private Utf8 activityTheater;
  private Utf8 activityRule;
  private Utf8 activityPrPrice;
  private Utf8 activityPrice;
  private Utf8 activityTime;
  private Utf8 activityEffectTime;
  
  private Utf8 movieNewsOutlinks;
  private Utf8 movieNewsName;
  public WebPage() {
    this(new StateManagerImpl());
  }
  public WebPage(StateManager stateManager) {
    super(stateManager);
    headers = new StatefulHashMap<Utf8,Utf8>();
    outlinks = new StatefulHashMap<Utf8,Utf8>();
    inlinks = new StatefulHashMap<Utf8,Utf8>();
    markers = new StatefulHashMap<Utf8,Utf8>();
    metadata = new StatefulHashMap<Utf8,ByteBuffer>();
  }
  public WebPage newInstance(StateManager stateManager) {
    return new WebPage(stateManager);
  }
  public Schema getSchema() { return _SCHEMA; }
  public Object get(int _field) {
    switch (_field) {
    case 0: return baseUrl;
    case 1: return status;
    case 2: return fetchTime;
    case 3: return prevFetchTime;
    case 4: return fetchInterval;
    case 5: return retriesSinceFetch;
    case 6: return modifiedTime;
    case 7: return prevModifiedTime;
    case 8: return protocolStatus;
    case 9: return content;
    case 10: return contentType;
    case 11: return prevSignature;
    case 12: return signature;
    case 13: return title;
    case 14: return text;
    case 15: return parseStatus;
    case 16: return score;
    case 17: return reprUrl;
    case 18: return headers;
    case 19: return outlinks;
    case 20: return inlinks;
    case 21: return markers;
    case 22: return metadata;
    case 23: return batchId;
    case 24: return movieIdProfessional;
    case 25: return movieIdIMDB;
    case 26: return movieIdMtime;
    case 27: return movieIdDouban;
    case 28: return movieId1905;
    case 29: return movieChineseName;
    case 30: return movieEnglishName;
    case 31: return movieShortName;
    case 32: return movieAliasName;
    case 33: return director;
    case 34: return writer;
    case 35: return actor;
    case 36: return producer;
    case 37: return cinematography;
    case 38: return cutter;
    case 39: return wardrobe;
    case 40: return visualAffectArtist;
    case 41: return associateDirector;
    case 42: return movieType;
    case 43: return publishCompany;
    case 44: return produceCompany;
    case 45: return distributionInformation;
    case 46: return plotIntroduction;
    case 47: return roleIntroduction;
    case 48: return movieLongComment;
    case 49: return movieShortComment;
    case 50: return movieWeiboComment;
    case 51: return movieNews;
    case 52: return movieScoreIMDB;
    case 53: return movieScoreMtime;
    case 54: return movieScoreDouban;
    case 55: return movieScore1905;
    case 56: return movieAwards;
    
    case 57: return cinemaIdProfessional;
    case 58: return cinemaIdMtime;
    case 59: return cinemaIdGewara;
    case 60: return cinemaIdSell;
    case 61: return cinemaChineseName;
    case 62: return cinemaShortName;
    case 63: return cinemaAliasName;
    case 64: return cinemaCity;
    case 65: return cinemaDistrict;
    case 66: return cinemaStreet;
    case 67: return cinemaAmapLocation;
    case 68: return cinemaBaiduLocation;
    case 69: return theaterChainName;
    case 70: return filmInvestmentCompany;
    case 71: return cinemaAssetsJoining;
    case 72: return bus;
    case 73: return subway;
    case 74: return drive;
    case 75: return cinemaFacility;
    case 76: return cinema3D;
    case 77: return cinemaCreditCard;
    case 78: return cinemaSellGoods;
    case 79: return cinemaParking;
    case 80: return cinemaChildrenTicket;
    case 81: return cinemaResting;
    case 82: return cinemaIntroduction;
    case 83: return cinemaDeals;
    case 84: return cinemaCommentMtime;
    case 85: return cinemaCommentGewara;
    case 86: return cinemaCommentDianping;
    case 87: return cinemaCommentWeibo;
    case 88: return cinemaScoreMtime;
    case 89: return cinemaScoreGewara;
    case 90: return cinemaScoreDianping;
    
    case 91: return actorIdMtime;
    case 92: return actorIdDouban;
    case 93: return actorId1905;
    case 94: return actorIdIMDB;
    case 95: return actorChineseName;
    case 96: return actorEnglishName;
    case 97: return actorShortName;
    case 98: return actorAliasName;
    case 99: return actorBirthName;
    case 100: return gender;
    case 101: return birthday;
    case 102: return birthplace;
    case 103: return bloodtype;
    case 104: return height;
    case 105: return weight;
    case 106: return actorIntroduction;
    case 107: return birthplaceIntroduction;
    case 108: return profession;
    case 109: return actorCompany;
    case 110: return actorNews;
    case 111: return actorProduction;
    case 112: return actorCommentMtime;
    case 113: return actorCommentGewara;
    case 114: return actorCommentDouban;
    case 115: return actorCommentWeibo;
    case 116: return actorScore;
    case 117: return relatives;
    case 118: return gossip;    
    case 119: return actorAwards;
    
    case 120: return awardIdMtime;
    case 121: return awardIdIMDB;
    case 122: return awardId1905;
    case 123: return awardChineseName;
    case 124: return awardEnglishName;
    case 125: return awardShortName;
    case 126: return awardAliasName;
    case 127: return awardHoldTime;
    case 128: return awardHoldPlace;
    case 129: return awardHoldCompany;
    case 130: return awardItems;
    case 131: return awardMovies;
    
    case 132: return weiboV;
    case 133: return weiboLevel;
    case 134: return weiboName;
    case 135: return weiboId;
    case 136: return weiboOfficial;
    case 137: return weiboFans;
    case 138: return weiboNum;
    case 139: return weiboInterest;
    case 140: return weiboTime;
    case 141: return weiboContent;
    case 142: return weiboZan;
    case 143: return weiboTopic;
    case 144: return weiboTransmit;
    case 145: return weiboComment;
    
    case 146: return NoProfessional;
    case 147: return NoSite;
    case 148: return siteMtime;
    case 149: return siteTaobao;
    case 150: return siteGewara;
    case 151: return siteMaoyan;
    case 152: return siteWangpiao;
    case 153: return siteSpider;
    case 154: return siteBaidu;
    case 155: return siteAmap;
    case 156: return NoFilm;
    case 157: return NoTheater;
    case 158: return NoHall;
    case 159: return sellSeat;
    case 160: return price;
    case 161: return vipPrice;
    case 162: return promotionPrice;
    case 163: return NoLanguage;
    case 164: return NoVersion;
    case 165: return NoTime;
    
    case 166: return activitySite;
    case 167: return activityFilm;
    case 168: return activityTheater;
    case 169: return activityRule;
    case 170: return activityPrPrice;
    case 171: return activityPrice;
    case 172: return activityTime;
    case 173: return activityEffectTime;
    
    case 174: return movieNewsOutlinks;
    case 175: return movieNewsName;
    default: throw new AvroRuntimeException("Bad index");
    }
  }
  @SuppressWarnings(value="unchecked")
  public void put(int _field, Object _value) {
    if(isFieldEqual(_field, _value)) return;
    getStateManager().setDirty(this, _field);
    switch (_field) {
    case 0:baseUrl = (Utf8)_value; break;
    case 1:status = (Integer)_value; break;
    case 2:fetchTime = (Long)_value; break;
    case 3:prevFetchTime = (Long)_value; break;
    case 4:fetchInterval = (Integer)_value; break;
    case 5:retriesSinceFetch = (Integer)_value; break;
    case 6:modifiedTime = (Long)_value; break;
    case 7:prevModifiedTime = (Long)_value; break;
    case 8:protocolStatus = (ProtocolStatus)_value; break;
    case 9:content = (ByteBuffer)_value; break;
    case 10:contentType = (Utf8)_value; break;
    case 11:prevSignature = (ByteBuffer)_value; break;
    case 12:signature = (ByteBuffer)_value; break;
    case 13:title = (Utf8)_value; break;
    case 14:text = (Utf8)_value; break;
    case 15:parseStatus = (ParseStatus)_value; break;
    case 16:score = (Float)_value; break;
    case 17:reprUrl = (Utf8)_value; break;
    case 18:headers = (Map<Utf8,Utf8>)_value; break;
    case 19:outlinks = (Map<Utf8,Utf8>)_value;break;
    case 20:inlinks = (Map<Utf8,Utf8>)_value; break;
    case 21:markers = (Map<Utf8,Utf8>)_value; break;
    case 22:metadata = (Map<Utf8,ByteBuffer>)_value; break;
    case 23:batchId = (Utf8)_value; break;
    case 24:movieIdProfessional = (Utf8)_value;break;
    case 25:movieIdIMDB = (Utf8)_value;break;
    case 26:movieIdMtime = (Utf8)_value;break;
    case 27:movieIdDouban = (Utf8)_value;break;
    case 28:movieId1905 = (Utf8)_value;break;
    case 29:movieChineseName = (Utf8)_value;break;
    case 30:movieEnglishName = (Utf8)_value;break;
    case 31:movieShortName = (Utf8)_value;break;
    case 32:movieAliasName = (Utf8)_value;break;
    case 33:director = (Utf8)_value;break;
    case 34:writer = (Utf8)_value;break;
    case 35:actor = (Utf8)_value;break;
    case 36:producer = (Utf8)_value;break;
    case 37:cinematography = (Utf8)_value;break;
    case 38:cutter = (Utf8)_value;break;
    case 39:wardrobe = (Utf8)_value;break;
    case 40:visualAffectArtist = (Utf8)_value;break;
    case 41:associateDirector = (Utf8)_value;break;
    case 42:movieType = (Utf8)_value;break;
    case 43:publishCompany = (Utf8)_value;break;
    case 44:produceCompany = (Utf8)_value;break;
    case 45:distributionInformation = (Utf8)_value;break;
    case 46:plotIntroduction = (Utf8)_value;break;
    case 47:roleIntroduction = (Utf8)_value;break;
    case 48:movieLongComment = (Utf8)_value;break;
    case 49:movieShortComment = (Utf8)_value;break;
    case 50:movieWeiboComment = (Utf8)_value;break;
    case 51:movieNews = (Utf8)_value;break;
    case 52:movieScoreIMDB = (Float)_value; break;
    case 53:movieScoreMtime = (Float)_value; break;
    case 54:movieScoreDouban = (Float)_value; break;
    case 55:movieScore1905 = (Float)_value; break;
    case 56:movieAwards = (Utf8)_value;break;
    case 57:cinemaIdProfessional = (Utf8)_value;break;
    case 58:cinemaIdMtime = (Utf8)_value;break;
    case 59:cinemaIdGewara = (Utf8)_value;break;
    case 60:cinemaIdSell = (Utf8)_value;break;
    case 61:cinemaChineseName = (Utf8)_value;break;
    case 62:cinemaShortName = (Utf8)_value;break;
    case 63:cinemaAliasName = (Utf8)_value;break;
    case 64:cinemaCity = (Utf8)_value;break;
    case 65:cinemaDistrict = (Utf8)_value;break;
    case 66:cinemaStreet = (Utf8)_value;break;
    case 67:cinemaAmapLocation = (Utf8)_value;break;
    case 68:cinemaBaiduLocation = (Utf8)_value;break;
    case 69:theaterChainName = (Utf8)_value;break;
    case 70:filmInvestmentCompany = (Utf8)_value;break;
    case 71:cinemaAssetsJoining = (Utf8)_value;break;
    case 72:bus = (Utf8)_value;break;
    case 73:subway = (Utf8)_value;break;
    case 74:drive = (Utf8)_value;break;
    case 75:cinemaFacility = (Utf8)_value;break;
    case 76:cinema3D = (Utf8)_value;break;
    case 77:cinemaCreditCard = (Utf8)_value;break;
    case 78:cinemaSellGoods = (Utf8)_value;break;
    case 79:cinemaParking = (Utf8)_value;break;
    case 80:cinemaChildrenTicket = (Utf8)_value;break;
    case 81:cinemaResting = (Utf8)_value;break;
    case 82:cinemaIntroduction = (Utf8)_value;break;
    case 83:cinemaDeals = (Utf8)_value;break;
    case 84:cinemaCommentMtime = (Utf8)_value;break;
    case 85:cinemaCommentGewara = (Utf8)_value;break;
    case 86:cinemaCommentDianping = (Utf8)_value;break;
    case 87:cinemaCommentWeibo = (Utf8)_value;break;
    case 88:cinemaScoreMtime = (Float)_value; break;
    case 89:cinemaScoreGewara = (Float)_value; break;
    case 90:cinemaScoreDianping = (Float)_value; break;
    case 91:actorIdMtime = (Utf8)_value;break;
    case 92:actorIdDouban = (Utf8)_value;break;
    case 93:actorId1905 = (Utf8)_value;break;
    case 94:actorIdIMDB = (Utf8)_value;break;
    case 95: actorChineseName = (Utf8)_value;break;
    case 96:actorEnglishName = (Utf8)_value;break;
    case 97:actorShortName = (Utf8)_value;break;
    case 98: actorAliasName = (Utf8)_value;break;
    case 99:actorBirthName = (Utf8)_value;break;
    case 100:gender = (Utf8)_value;break;
    case 101:birthday = (Utf8)_value;break;
    case 102:birthplace = (Utf8)_value;break;
    case 103:bloodtype = (Utf8)_value;break;
    case 104:height = (Utf8)_value;break;
    case 105:weight = (Utf8)_value;break;
    case 106:actorIntroduction = (Utf8)_value;break;
    case 107:birthplaceIntroduction = (Utf8)_value;break;
    case 108:profession = (Utf8)_value;break;
    case 109:actorCompany = (Utf8)_value;break;
    case 110:actorNews = (Utf8)_value;break;
    case 111:actorProduction = (Utf8)_value;break;
    case 112:actorCommentMtime = (Utf8)_value;break;
    case 113:actorCommentGewara = (Utf8)_value;break;
    case 114:actorCommentDouban = (Utf8)_value;break;
    case 115:actorCommentWeibo = (Utf8)_value;break;
    case 116:actorScore = (Float)_value;break;
    case 117:relatives = (Utf8)_value;break;
    case 118:gossip = (Utf8)_value;break;
    case 119:actorAwards = (Utf8)_value;break;
    case 120:awardIdMtime = (Utf8)_value;break;
    case 121:awardIdIMDB = (Utf8)_value;break;
    case 122:awardId1905 = (Utf8)_value;break;
    case 123:awardChineseName = (Utf8)_value;break;
    case 124:awardEnglishName = (Utf8)_value;break;
    case 125:awardShortName = (Utf8)_value;break;
    case 126:awardAliasName = (Utf8)_value;break;
    case 127:awardHoldTime = (Utf8)_value;break;
    case 128:awardHoldPlace = (Utf8)_value;break;
    case 129:awardHoldCompany = (Utf8)_value;break;
    case 130:awardItems = (Utf8)_value;break;
    case 131:awardMovies = (Utf8)_value;break;
    case 132:weiboV = (Utf8)_value;break;
    case 133:weiboLevel = (Integer)_value; break;
    case 134:weiboName = (Utf8)_value;break;
    case 135:weiboId = (Utf8)_value;break;
    case 136:weiboOfficial = (Utf8)_value;break;
    case 137:weiboFans = (Integer)_value; break;
    case 138:weiboNum = (Integer)_value; break;
    case 139:weiboInterest = (Integer)_value; break;
    case 140:weiboTime = (Utf8)_value;break;
    case 141:weiboContent = (Utf8)_value;break;
    case 142:weiboZan = (Integer)_value; break;
    case 143:weiboTopic = (Utf8)_value;break;
    case 144:weiboTransmit = (Utf8)_value;break;
    case 145:weiboComment = (Utf8)_value;break;
    case 146:NoProfessional = (Utf8)_value;break;
    case 147:NoSite = (Utf8)_value;break;
    case 148:siteMtime = (Utf8)_value;break;
    case 149:siteTaobao = (Utf8)_value;break;
    case 150:siteGewara = (Utf8)_value;break;
    case 151:siteMaoyan = (Utf8)_value;break;
    case 152:siteWangpiao = (Utf8)_value;break;
    case 153:siteSpider = (Utf8)_value;break;
    case 154:siteBaidu = (Utf8)_value;break;
    case 155:siteAmap = (Utf8)_value;break;
    case 156:NoFilm = (Utf8)_value;break;
    case 157:NoTheater = (Utf8)_value;break;
    case 158:NoHall = (Utf8)_value;break;
    case 159:sellSeat = (Utf8)_value;break;
    case 160:price = (Utf8)_value;break;
    case 161:vipPrice = (Utf8)_value;break;
    case 162:promotionPrice = (Utf8)_value;break;
    case 163:NoLanguage = (Utf8)_value;break;
    case 164:NoVersion = (Utf8)_value;break;
    case 165:NoTime = (Utf8)_value;break;
    case 166:activitySite = (Utf8)_value;break;
    case 167:activityFilm = (Utf8)_value;break;
    case 168:activityTheater = (Utf8)_value;break;
    case 169:activityRule = (Utf8)_value;break;
    case 170:activityPrPrice = (Utf8)_value;break;
    case 171:activityPrice = (Utf8)_value;break;
    case 172:activityTime = (Utf8)_value;break;
    case 173:activityEffectTime = (Utf8)_value;break;
    case 174:movieNewsOutlinks = (Utf8)_value;break;
    case 175:movieNewsName = (Utf8)_value;break;
    default: throw new AvroRuntimeException("Bad index");
    }
  }
  public Utf8 getBaseUrl() {
    return (Utf8) get(0);
  }
  public void setBaseUrl(Utf8 value) {
    put(0, value);
  }
  public int getStatus() {
    return (Integer) get(1);
  }
  public void setStatus(int value) {
    put(1, value);
  }
  public long getFetchTime() {
    return (Long) get(2);
  }
  public void setFetchTime(long value) {
    put(2, value);
  }
  public long getPrevFetchTime() {
    return (Long) get(3);
  }
  public void setPrevFetchTime(long value) {
    put(3, value);
  }
  public int getFetchInterval() {
    return (Integer) get(4);
  }
  public void setFetchInterval(int value) {
    put(4, value);
  }
  public int getRetriesSinceFetch() {
    return (Integer) get(5);
  }
  public void setRetriesSinceFetch(int value) {
    put(5, value);
  }
  public long getModifiedTime() {
    return (Long) get(6);
  }
  public void setModifiedTime(long value) {
    put(6, value);
  }
  public long getPrevModifiedTime() {
    return (Long) get(7);
  }
  public void setPrevModifiedTime(long value) {
    put(7, value);
  }
  public ProtocolStatus getProtocolStatus() {
    return (ProtocolStatus) get(8);
  }
  public void setProtocolStatus(ProtocolStatus value) {
    put(8, value);
  }
  public ByteBuffer getContent() {
    return (ByteBuffer) get(9);
  }
  public void setContent(ByteBuffer value) {
    put(9, value);
  }
  public Utf8 getContentType() {
    return (Utf8) get(10);
  }
  public void setContentType(Utf8 value) {
    put(10, value);
  }
  public ByteBuffer getPrevSignature() {
    return (ByteBuffer) get(11);
  }
  public void setPrevSignature(ByteBuffer value) {
    put(11, value);
  }
  public ByteBuffer getSignature() {
    return (ByteBuffer) get(12);
  }
  public void setSignature(ByteBuffer value) {
    put(12, value);
  }
  public Utf8 getTitle() {
    return (Utf8) get(13);
  }
  public void setTitle(Utf8 value) {
    put(13, value);
  }
  public Utf8 getText() {
    return (Utf8) get(14);
  }
  public void setText(Utf8 value) {
    put(14, value);
  }
  public ParseStatus getParseStatus() {
    return (ParseStatus) get(15);
  }
  public void setParseStatus(ParseStatus value) {
    put(15, value);
  }
  public float getScore() {
    return (Float) get(16);
  }
  public void setScore(float value) {
    put(16, value);
  }
  public Utf8 getReprUrl() {
    return (Utf8) get(17);
  }
  public void setReprUrl(Utf8 value) {
    put(17, value);
  }
  @SuppressWarnings("unchecked")
  public Map<Utf8, Utf8> getHeaders() {
    return (Map<Utf8, Utf8>) get(18);
  }
  public Utf8 getFromHeaders(Utf8 key) {
    if (headers == null) { return null; }
    return headers.get(key);
  }
  public void putToHeaders(Utf8 key, Utf8 value) {
    getStateManager().setDirty(this, 18);
    headers.put(key, value);
  }
  public Utf8 removeFromHeaders(Utf8 key) {
    if (headers == null) { return null; }
    getStateManager().setDirty(this, 18);
    return headers.remove(key);
  }
  @SuppressWarnings("unchecked")
  public Map<Utf8, Utf8> getOutlinks() {
    return (Map<Utf8, Utf8>) get(19);
  }
  public Utf8 getFromOutlinks(Utf8 key) {
    if (outlinks == null) { return null; }
    return outlinks.get(key);
  }
  public void putToOutlinks(Utf8 key, Utf8 value) {
    getStateManager().setDirty(this, 19);
    outlinks.put(key, value);
  }
  public Utf8 removeFromOutlinks(Utf8 key) {
    if (outlinks == null) { return null; }
    getStateManager().setDirty(this, 19);
    return outlinks.remove(key);
  }
  @SuppressWarnings("unchecked")
  public Map<Utf8, Utf8> getInlinks() {
    return (Map<Utf8, Utf8>) get(20);
  }
  public Utf8 getFromInlinks(Utf8 key) {
    if (inlinks == null) { return null; }
    return inlinks.get(key);
  }
  public void putToInlinks(Utf8 key, Utf8 value) {
    getStateManager().setDirty(this, 20);
    inlinks.put(key, value);
  }
  public Utf8 removeFromInlinks(Utf8 key) {
    if (inlinks == null) { return null; }
    getStateManager().setDirty(this, 20);
    return inlinks.remove(key);
  }
  @SuppressWarnings("unchecked")
  public Map<Utf8, Utf8> getMarkers() {
    return (Map<Utf8, Utf8>) get(21);
  }
  public Utf8 getFromMarkers(Utf8 key) {
    if (markers == null) { return null; }
    return markers.get(key);
  }
  public void putToMarkers(Utf8 key, Utf8 value) {
    getStateManager().setDirty(this, 21);
    markers.put(key, value);
  }
  public Utf8 removeFromMarkers(Utf8 key) {
    if (markers == null) { return null; }
    getStateManager().setDirty(this, 21);
    return markers.remove(key);
  }
  @SuppressWarnings("unchecked")
  public Map<Utf8, ByteBuffer> getMetadata() {
    return (Map<Utf8, ByteBuffer>) get(22);
  }
  public ByteBuffer getFromMetadata(Utf8 key) {
    if (metadata == null) { return null; }
    return metadata.get(key);
  }
  public void putToMetadata(Utf8 key, ByteBuffer value) {
    getStateManager().setDirty(this, 22);
    metadata.put(key, value);
  }
  public ByteBuffer removeFromMetadata(Utf8 key) {
    if (metadata == null) { return null; }
    getStateManager().setDirty(this, 22);
    return metadata.remove(key);
  }
  public Utf8 getBatchId() {
    return (Utf8) get(23);
  }
  public void setBatchId(Utf8 value) {
    put(23, value);
  } 
  public Utf8 getMovieIdProfessional() {
	return (Utf8) get(24);
  }
  public void setMovieIdProfessional(Utf8 value) {
	put(24, value);
  }
  public Utf8 getMovieIdIMDB() {
	return (Utf8) get(25);
  }
  public void setMovieIdIMDB(Utf8 value) {
	put(25, value);
  }
  public Utf8 getMovieIdMtime() {
	return (Utf8) get(26);
  }
  public void setMovieIdMtime(Utf8 value) {
	put(26, value);
  }
  public Utf8 getMovieIdDouban() {
	return (Utf8) get(27);
  }
  public void setMovieIdDouban(Utf8 value) {
	put(27, value);
  } 
  public Utf8 getMovieId1905() {
	return (Utf8) get(28);
  }
  public void setMovieId1905(Utf8 value) {
	put(28, value);
  }  
  public Utf8 getMovieChineseName() {
	return (Utf8) get(29);
  }
  public void setMovieChineseName(Utf8 value) {
	put(29, value);
  }   
  public Utf8 getMovieEnglishName() {
	return (Utf8) get(30);
  }
  public void setMovieEnglishName(Utf8 value) {
	put(30, value);
  } 
  public Utf8 getMovieShortName() {
	return (Utf8) get(31);
  }
  public void setMovieShortName(Utf8 value) {
	put(31, value);
  } 
  public Utf8 getMovieAliasName() {
	return (Utf8) get(32);
  }
  public void setMovieAliasName(Utf8 value) {
	put(32, value);
  } 
  public Utf8 getDirector() {
	return (Utf8) get(33);
  }
  public void setDirector(Utf8 value) {
	put(33, value);
  } 
  public Utf8 getWriter() {
	return (Utf8) get(34);
  }
  public void setWriter(Utf8 value) {
	put(34, value);
  } 
  public Utf8 getActor() {
	return (Utf8) get(35);
  }
  public void setActor(Utf8 value) {
	put(35, value);
  } 
  public Utf8 getProducer() {
	return (Utf8) get(36);
  }
  public void setProducer(Utf8 value) {
	put(36, value);
  } 
  public Utf8 getCinematography() {
	return (Utf8) get(37);
  }
  public void setCinematography(Utf8 value) {
	put(37, value);
  } 
  public Utf8 getCutter() {
	return (Utf8) get(38);
  }
  public void setCutter(Utf8 value) {
	put(38, value);
  } 
  public Utf8 getWardrobe() {
	return (Utf8) get(39);
  }
  public void setWardrobe(Utf8 value) {
	put(39, value);
  } 
  public Utf8 getVisualAffectArtist() {
	return (Utf8) get(40);
  }
  public void setVisualAffectArtist(Utf8 value) {
	put(40, value);
  } 
  public Utf8 getAssociateDirector() {
	return (Utf8) get(41);
  }
  public void setAssociateDirector(Utf8 value) {
	put(41, value);
  } 
  public Utf8 getmovieType() {
	return (Utf8) get(42);
  }
  public void setMovieType(Utf8 value) {
	put(42, value);
  } 
  public Utf8 getPublishCompany() {
	return (Utf8) get(43);
  }
  public void setPublishCompany(Utf8 value) {
	put(43, value);
  } 
  public Utf8 getProduceCompany() {
	return (Utf8) get(44);
  }
  public void setProduceCompany(Utf8 value) {
	put(44, value);
  }
  public Utf8 getDistributionInformation() {
	return (Utf8) get(45);
  }
  public void setDistributionInformation(Utf8 value) {
	put(45, value);
  }
  public Utf8 getPlotIntroduction() {
	return (Utf8) get(46);
  }
  public void setPlotIntroduction(Utf8 value) {
	put(46, value);
  }
  public Utf8 getRoleIntroduction() {
	return (Utf8) get(47);
  }
  public void setRoleIntroduction(Utf8 value) {
	put(47, value);
  }
  public Utf8 getMovieLongComment() {
	return (Utf8) get(48);
  }
  public void setMovieLongComment(Utf8 value) {
	put(48, value);
  }
  public Utf8 getMovieShortComment() {
	return (Utf8) get(49);
  }
  public void setMovieShortComment(Utf8 value) {
	put(49, value);
  }
  public Utf8 getMovieWeiboComment() {
	return (Utf8) get(50);
  }
  public void setMovieWeiboComment(Utf8 value) {
	put(50, value);
  }
  public Utf8 getMovieNews() {
	return (Utf8) get(51);
  }
  public void setMovieNews(Utf8 value) {
	put(51, value);
  }
  public float getMovieScoreIMDB() {
	return (Float) get(52);
  }
  public void setMovieScoreIMDB(float value) {
	put(52, value);
  }
  public float getMovieScoreMtime() {
	return (Float) get(53);
  }
  public void setMovieScoreMtime(float value) {
	put(53, value);
  }
  public float getMovieScoreDouban() {
	return (Float) get(54);
  }
  public void setMovieScoreDouban(float value) {
	put(54, value);
  }
  public float getMovieScore1905() {
	return (Float) get(55);
  }
  public void setMovieScore1905(float value) {
	put(55, value);
  }
  public Utf8 getMovieAwards() {
	return (Utf8) get(56);
  }
  public void setMovieAwards(Utf8 value) {
	put(56, value);
  }
  public Utf8 getCinemaIdProfessional() {
	return (Utf8) get(57);
  }
  public void setCinemaIdProfessional(Utf8 value) {
	put(57, value);
  }
  public Utf8 getCinemaIdMtime() {
	return (Utf8) get(58);
  }
  public void setCinemaIdMtime(Utf8 value) {
	put(58, value);
  }
  public Utf8 getCinemaIdGewara() {
	return (Utf8) get(59);
  }
  public void setCinemaIdGewara(Utf8 value) {
	put(59, value);
  }
  public Utf8 getCinemaIdSell() {
	return (Utf8) get(60);
  }
  public void setCinemaIdSell(Utf8 value) {
	put(60, value);
  }
  public Utf8 getCinemaChineseName() {
	return (Utf8) get(61);
  }
  public void setCinemaChineseName(Utf8 value) {
	put(61, value);
  }  
  public Utf8 getCinemaShortName() {
	return (Utf8) get(62);
  }
  public void setCinemaShortName(Utf8 value) {
	put(62, value);
  } 
  public Utf8 getCinemaAliasName() {
	return (Utf8) get(63);
  }
  public void setCinemaAliasName(Utf8 value) {
	put(63, value);
  }  
  public Utf8 getCinemaCity() {
	return (Utf8) get(64);
  }
  public void setCinemaCity(Utf8 value) {
	put(64, value);
  }   
  public Utf8 getCinemaDistrict() {
	return (Utf8) get(65);
  }
  public void setCinemaDistrict(Utf8 value) {
	put(65, value);
  }   
  public Utf8 getCinemaStreet() {
	return (Utf8) get(66);
  }
  public void setCinemaStreet(Utf8 value) {
	put(66, value);
  }     
  public Utf8 getCinemaAmapLocation() {
	return (Utf8) get(67);
  }
  public void setCinemaAmapLocation(Utf8 value) {
	put(67, value);
  }  
  public Utf8 getCinemaBaiduLocation() {
	return (Utf8) get(68);
  }
  public void setCinemaBaiduLocation(Utf8 value) {
	put(68, value);
  }  
  public Utf8 getTheaterChainName() {
	return (Utf8) get(69);
  }
  public void setTheaterChainName(Utf8 value) {
	put(69, value);
  } 
  public Utf8 getFilmInvestmentCompany() {
	return (Utf8) get(70);
  }
  public void setFilmInvestmentCompany(Utf8 value) {
	put(70, value);
  } 
  public Utf8 getCinemaAssetsJoining() {
	return (Utf8) get(71);
  }
  public void setCinemaAssetsJoining(Utf8 value) {
	put(71, value);
  } 
  public Utf8 getBus() {
	return (Utf8) get(72);
  }
  public void setBus(Utf8 value) {
	put(72, value);
  }
  public Utf8 getSubway() {
	return (Utf8) get(73);
  }
  public void setSubway(Utf8 value) {
	put(73, value);
  }
  public Utf8 getDrive() {
	return (Utf8) get(74);
  }
  public void setDrive(Utf8 value) {
	put(74, value);
  }
  public Utf8 getCinemaFacility() {
	return (Utf8) get(75);
  }
  public void setCinemaFacility(Utf8 value) {
	put(75, value);
  }
  public Utf8 getCinema3D() {
	return (Utf8) get(76);
  }
  public void setCinema3D(Utf8 value) {
	put(76, value);
  }
  public Utf8 getCinemaCreditCard() {
	return (Utf8) get(77);
  }
  public void setCinemaCreditCard(Utf8 value) {
	put(77, value);
  }
  public Utf8 getCinemaSellGoods() {
	return (Utf8) get(78);
  }
  public void setCinemaSellGoods(Utf8 value) {
	put(78, value);
  }
  public Utf8 getCinemaParking() {
	return (Utf8) get(79);
  }
  public void setCinemaParking(Utf8 value) {
	put(79, value);
  }
  public Utf8 getCinemaChildrenTicket() {
	return (Utf8) get(80);
  }
  public void setCinemaChildrenTicket(Utf8 value) {
	put(80, value);
  }
  public Utf8 getCinemaResting() {
	return (Utf8) get(81);
  }
  public void setCinemaResting(Utf8 value) {
	put(81, value);
  }
  public Utf8 getCinemaIntroduction() {
	return (Utf8) get(82);
  }
  public void setCinemaIntroduction(Utf8 value) {
	put(82, value);
  }
  public Utf8 getCinemaDeals() {
	return (Utf8) get(83);
  }
  public void setCinemaDeals(Utf8 value) {
	put(83, value);
  }
  public Utf8 getCinemaCommentMtime() {
	return (Utf8) get(84);
  }
  public void setCinemaCommentMtime(Utf8 value) {
	put(84, value);
  }
  public Utf8 getCinemaCommentGewara() {
	return (Utf8) get(85);
  }
  public void setCinemaCommentGewara(Utf8 value) {
	put(85, value);
  }
  public Utf8 getCinemaCommentDianping() {
	return (Utf8) get(86);
  }
  public void setCinemaCommentDianping(Utf8 value) {
	put(86, value);
  }
  public Utf8 getCinemaCommentWeibo() {
	return (Utf8) get(87);
  }
  public void setCinemaCommentWeibo(Utf8 value) {
	put(87, value);
  }
  public float getCinemaScoreMtime() {
	return (Float) get(88);
  }
  public void setCinemaScoreMtime(float value) {
	put(88, value);
  }
  public float getCinemaScoreGewara() {
	return (Float) get(89);
  }
  public void setCinemaScoreGewara(float value) {
	put(89, value);
  }
  public float getCinemaScoreDianping() {
	return (Float) get(90);
  }
  public void setCinemaScoreDianping(float value) {
	put(90, value);
  }
  public Utf8 getActorIdMtime() {
	  return (Utf8) get(91);
  }
  public void setActorIdMtime(Utf8 value) {
	  put(91, value);
  }
  public Utf8 getActorIdDouban() {
	  return (Utf8) get(92);
  }
  public void setActorIdDouban(Utf8 value) {
	  put(92, value);
  }  
  public Utf8 getActorId1905() {
	  return (Utf8) get(93);
  }
  public void setActorId1905(Utf8 value) {
	  put(93, value);
  } 
  public Utf8 getActorIdIMDB() {
	  return (Utf8) get(94);
  }
  public void setActorIdIMDB(Utf8 value) {
	  put(94, value);
  }
  public Utf8 getActorChineseName() {
	  return (Utf8) get(95);
  }
  public void setActorChineseName(Utf8 value) {
	  put(95, value);
  }
  public Utf8 getActorEnglishName() {
	  return (Utf8) get(96);
  }
  public void setActorEnglishName(Utf8 value) {
	  put(96, value);
  }
  public Utf8 getActorShortName() {
	  return (Utf8) get(97);
  }
  public void setActorShortName(Utf8 value) {
	  put(97, value);
  }
  public Utf8 getActorAliasName() {
	  return (Utf8) get(98);
  }
  public void setActorAliasName(Utf8 value) {
	  put(98, value);
  }
  public Utf8 getActorBirthName() {
	  return (Utf8) get(99);
  }
  public void setActorBirthName(Utf8 value) {
	  put(99, value);
  }
  public Utf8 getGender() {
	  return (Utf8) get(100);
  }
  public void setGender(Utf8 value) {
	  put(100, value);
  }
  public Utf8 getBirthday() {
	  return (Utf8) get(101);
  }
  public void setBirthday(Utf8 value) {
	  put(101, value);
  }
  public Utf8 getBirthplace() {
	  return (Utf8) get(102);
  }
  public void setBirthplace(Utf8 value) {
	  put(102, value);
  }
  public Utf8 getBloodtype() {
	  return (Utf8) get(103);
  }
  public void setBloodtype(Utf8 value) {
	  put(103, value);
  }
  public Utf8 getHeight() {
	  return (Utf8) get(104);
  }
  public void setHeight(Utf8 value) {
	  put(104, value);
  }
  public Utf8 getWeight() {
	  return (Utf8) get(105);
  }
  public void setWeight(Utf8 value) {
	  put(105, value);
  }
  public Utf8 getActorIntroduction() {
	  return (Utf8) get(106);
  }
  public void setActorIntroduction(Utf8 value) {
	  put(106, value);
  }
  public Utf8 getBirthplaceIntroduction() {
	  return (Utf8) get(107);
  }
  public void setBirthplaceIntroduction(Utf8 value) {
	  put(107, value);
  }
  public Utf8 getProfession() {
	  return (Utf8) get(108);
  }
  public void setProfession(Utf8 value) {
	  put(108, value);
  }
  public Utf8 getActorCompany() {
	  return (Utf8) get(109);
  }
  public void setActorCompany(Utf8 value) {
	  put(109, value);
  }
  public Utf8 getActorNews() {
	  return (Utf8) get(110);
  }
  public void setActorNews(Utf8 value) {
	  put(110, value);
  }
  public Utf8 getActorProduction() {
	  return (Utf8) get(111);
  }
  public void setActorProduction(Utf8 value) {
	  put(111, value);
  }
  public Utf8 getActorCommentMtime() {
	  return (Utf8) get(112);
  }
  public void setActorCommentMtime(Utf8 value) {
	  put(112, value);
  }
  public Utf8 getActorCommentGewara() {
	  return (Utf8) get(113);
  }
  public void setActorCommentGewara(Utf8 value) {
	  put(113, value);
  }
  public Utf8 getActorCommentDouban() {
	  return (Utf8) get(114);
  }
  public void setActorCommentDouban(Utf8 value) {
	  put(114, value);
  }
  public Utf8 getActorCommentWeibo() {
	  return (Utf8) get(115);
  }
  public void setActorCommentWeibo(Utf8 value) {
	  put(115, value);
  }
  public float getActorScore() {
	  return (Float) get(116);
  }
  public void setActorScore(float value) {
	  put(116, value);
  }
  public Utf8 getRelatives() {
	  return (Utf8) get(117);
  }
  public void setRelatives(Utf8 value) {
	  put(117, value);
  }
  public Utf8 getGossip() {
	  return (Utf8) get(118);
  }
  public void setGossip(Utf8 value) {
	  put(118, value);
  }
  public Utf8 getActorAwards() {
	  return (Utf8) get(119);
  }
  public void setActorAwards(Utf8 value) {
	  put(119, value);
  }
  public Utf8 getAwardIdMtime() {
	  return (Utf8) get(120);
  }
  public void setAwardIdMtime(Utf8 value) {
	  put(120, value);
  }
  public Utf8 getAwardIdIMDB() {
	  return (Utf8) get(121);
  }
  public void setAwardIdIMDB(Utf8 value) {
	  put(121, value);
  }
  public Utf8 getAwardId1905() {
	  return (Utf8) get(122);
  }
  public void setAwardId1905(Utf8 value) {
	  put(122, value);
  }
  public Utf8 getAwardChineseName() {
	  return (Utf8) get(123);
  }
  public void setAwardChineseName(Utf8 value) {
	  put(123, value);
  }
  public Utf8 getAwardEnglishName() {
	  return (Utf8) get(124);
  }
  public void setAwardEnglishName(Utf8 value) {
	  put(124, value);
  }
  public Utf8 getAwardShortName() {
	  return (Utf8) get(125);
  }
  public void setAwardShortName(Utf8 value) {
	  put(125, value);
  }
  public Utf8 getAwardAliasName() {
	  return (Utf8) get(126);
  }
  public void setAwardAliasName(Utf8 value) {
	  put(126, value);
  }
  public Utf8 getAwardHoldTime() {
	  return (Utf8) get(127);
  }
  public void setAwardHoldTime(Utf8 value) {
	  put(127, value);
  }
  public Utf8 getAwardHoldPlace() {
	  return (Utf8) get(128);
  }
  public void setAwardHoldPlace(Utf8 value) {
	  put(128, value);
  }
  public Utf8 getAwardHoldCompany() {
	  return (Utf8) get(129);
  }
  public void setAwardHoldCompany(Utf8 value) {
	  put(129, value);
  }
  public Utf8 getAwardItems() {
	  return (Utf8) get(130);
  }
  public void setAwardItems(Utf8 value) {
	  put(130, value);
  }
  public Utf8 getAwardMovies() {
	  return (Utf8) get(131);
  }
  public void setAwardMovies(Utf8 value) {
	  put(131, value);
  }
  public Utf8 getWeiboV() {
	  return (Utf8) get(132);
  }
  public void setWeiboV(Utf8 value) {
	  put(132, value);
  }
  public int getWeiboLevel() {
	  return (Integer) get(133);
  }
  public void setWeiboLevel(int value) {
	  put(133, value);
  }  
  public Utf8 getWeiboName() {
	  return (Utf8) get(134);
  }
  public void setWeiboName(Utf8 value) {
	  put(134, value);
  }
  public Utf8 getWeiboId() {
	  return (Utf8) get(135);
  }
  public void setWeiboId(Utf8 value) {
	  put(135, value);
  }
  public Utf8 getWeiboOfficial() {
	  return (Utf8) get(136);
  }
  public void setWeiboOfficial(Utf8 value) {
	  put(136, value);
  }
  public int getWeiboFans() {
	  return (Integer) get(137);
  }
  public void setWeiboFans(int value) {
	  put(137, value);
  } 
  public int getWeiboNum() {
	  return (Integer) get(138);
  }
  public void setWeiboNum(int value) {
	  put(138, value);
  } 
  public int getWeiboInterest() {
	  return (Integer) get(139);
  }
  public void setWeiboInterest(int value) {
	  put(139, value);
  } 
  public Utf8 getWeiboTime() {
	  return (Utf8) get(140);
  }
  public void setWeiboTime(Utf8 value) {
	  put(140, value);
  }
  public Utf8 getWeiboContent() {
	  return (Utf8) get(141);
  }
  public void setWeiboContent(Utf8 value) {
	  put(141, value);
  }
  public int getWeiboZan() {
	  return (Integer) get(142);
  }
  public void setWeiboZan(int value) {
	  put(142, value);
  } 
  public Utf8 getWeiboTopic() {
	  return (Utf8) get(143);
  }
  public void setweiboTopic(Utf8 value) {
	  put(143, value);
  }
  public Utf8 getWeiboTransmit() {
	  return (Utf8) get(144);
  }
  public void setWeiboTransmit(Utf8 value) {
	  put(144, value);
  }
  public Utf8 getWeiboComment() {
	  return (Utf8) get(145);
  }
  public void setWeiboComment(Utf8 value) {
	  put(145, value);
  }
  public Utf8 getNoProfessional() {
	  return (Utf8) get(146);
  }
  public void setNoProfessional(Utf8 value) {
	  put(146, value);
  }
  public Utf8 getNoSite() {
	  return (Utf8) get(147);
  }
  public void setNoSite(Utf8 value) {
	  put(147, value);
  }
  public Utf8 getSiteMtime() {
	  return (Utf8) get(148);
  }
  public void setSiteMtime(Utf8 value) {
	  put(148, value);
  }
  public Utf8 getSiteTaobao() {
	  return (Utf8) get(149);
  }
  public void setSiteTaobao(Utf8 value) {
	  put(149, value);
  }
  public Utf8 getSiteGewara() {
	  return (Utf8) get(150);
  }
  public void setSiteGewara(Utf8 value) {
	  put(150, value);
  }
  public Utf8 getSiteMaoyan() {
	  return (Utf8) get(151);
  }
  public void setSiteMaoyan(Utf8 value) {
	  put(151, value);
  }
  public Utf8 getSiteWangpiao() {
	  return (Utf8) get(152);
  }
  public void setSiteWangpiao(Utf8 value) {
	  put(152, value);
  }
  public Utf8 getSiteSpider() {
	  return (Utf8) get(153);
  }
  public void setSiteSpider(Utf8 value) {
	  put(153, value);
  }
  public Utf8 getSiteBaidu() {
	  return (Utf8) get(154);
  }
  public void setSiteBaidu(Utf8 value) {
	  put(154, value);
  }
  public Utf8 getSiteAmap() {
	  return (Utf8) get(155);
  }
  public void setSiteAmap(Utf8 value) {
	  put(155, value);
  }
  public Utf8 getNoFilm() {
	  return (Utf8) get(156);
  }
  public void setNoFilm(Utf8 value) {
	  put(156, value);
  }
  public Utf8 getNoTheater() {
	  return (Utf8) get(157);
  }
  public void setNoTheater(Utf8 value) {
	  put(157, value);
  }
  public Utf8 getNoHall() {
	  return (Utf8) get(158);
  }
  public void setNoHall(Utf8 value) {
	  put(158, value);
  }
  public Utf8 getSellSeat() {
	  return (Utf8) get(159);
  }
  public void setSellSeat(Utf8 value) {
	  put(159, value);
  }
  public Utf8 getPrice() {
	  return (Utf8) get(160);
  }
  public void setPrice(Utf8 value) {
	  put(160, value);
  }
  public Utf8 getVipPrice() {
	  return (Utf8) get(161);
  }
  public void setVipPrice(Utf8 value) {
	  put(161, value);
  }
  public Utf8 getPromotionPrice() {
	  return (Utf8) get(162);
  }
  public void setPromotionPrice(Utf8 value) {
	  put(162, value);
  }
  public Utf8 getNoLanguage() {
	  return (Utf8) get(163);
  }
  public void setNoLanguage(Utf8 value) {
	  put(163, value);
  }
  public Utf8 getNoVersion() {
	  return (Utf8) get(164);
  }
  public void setNoVersion(Utf8 value) {
	  put(164, value);
  }
  public Utf8 getNoTime() {
	  return (Utf8) get(165);
  }
  public void setNoTime(Utf8 value) {
	  put(165, value);
  }
  public Utf8 getActivitySite() {
	  return (Utf8) get(166);
  }
  public void setActivitySite(Utf8 value) {
	  put(166, value);
  }
  public Utf8 getActivityFilm() {
	  return (Utf8) get(167);
  }
  public void setActivityFilm(Utf8 value) {
	  put(167, value);
  }
  public Utf8 getActivityTheater() {
	  return (Utf8) get(168);
  }
  public void setActivityTheater(Utf8 value) {
	  put(168, value);
  }
  public Utf8 getActivityRule() {
	  return (Utf8) get(169);
  }
  public void setActivityRule(Utf8 value) {
	  put(169, value);
  }
  public Utf8 getActivityPrPrice() {
	  return (Utf8) get(170);
  }
  public void setActivityPrPrice(Utf8 value) {
	  put(170, value);
  }
  public Utf8 getActivityPrice() {
	  return (Utf8) get(171);
  }
  public void setActivityPrice(Utf8 value) {
	  put(171, value);
  }
  public Utf8 getActivityTime() {
	  return (Utf8) get(172);
  }
  public void setActivityTime(Utf8 value) {
	  put(172, value);
  }
  public Utf8 getActivityEffectTime() {
	  return (Utf8) get(173);
  }
  public void setActivityEffectTime(Utf8 value) {
	  put(173, value);
  }
  public Utf8 getMovieNewsOutlinks() {
	  return (Utf8) get(174);
  }
  public void setMovieNewsOutlinks(Utf8 value) {
	  put(174, value);
  }
  public Utf8 getmovieNewsName() {
	  return (Utf8) get(175);
  }
  public void setMovieNewsName(Utf8 value) {
	  put(175, value);
  }
}