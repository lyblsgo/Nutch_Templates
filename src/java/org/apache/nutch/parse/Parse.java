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
package org.apache.nutch.parse;

import java.nio.ByteBuffer;

import org.apache.avro.util.Utf8;

public class Parse {

	private String text;
	private String title;
	private Outlink[] outlinks;
	private org.apache.nutch.storage.ParseStatus parseStatus;

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

	public Parse() {
	}

	public Parse(String text, String title, Outlink[] outlinks,
			org.apache.nutch.storage.ParseStatus parseStatus) {
		this.text = text;
		this.title = title;
		this.outlinks = outlinks;
		this.parseStatus = parseStatus;
	}

	public String getText() {
		return text;
	}

	public String getTitle() {
		return title;
	}

	public Outlink[] getOutlinks() {
		return outlinks;
	}

	public org.apache.nutch.storage.ParseStatus getParseStatus() {
		return parseStatus;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setOutlinks(Outlink[] outlinks) {
		this.outlinks = outlinks;
	}

	public void setParseStatus(org.apache.nutch.storage.ParseStatus parseStatus) {
		this.parseStatus = parseStatus;
	}

	public Utf8 getMovieIdProfessional() {
		return movieIdProfessional;
	}

	public void setMovieIdProfessional(Utf8 movieIdProfessional) {
		this.movieIdProfessional = movieIdProfessional;
	}

	public Utf8 getMovieIdIMDB() {
		return movieIdIMDB;
	}

	public void setMovieIdIMDB(Utf8 movieIdIMDB) {
		this.movieIdIMDB = movieIdIMDB;
	}

	public Utf8 getMovieIdMtime() {
		return movieIdMtime;
	}

	public void setMovieIdMtime(Utf8 movieIdMtime) {
		this.movieIdMtime = movieIdMtime;
	}

	public Utf8 getMovieIdDouban() {
		return movieIdDouban;
	}

	public void setMovieIdDouban(Utf8 movieIdDouban) {
		this.movieIdDouban = movieIdDouban;
	}

	public Utf8 getMovieId1905() {
		return movieId1905;
	}

	public void setMovieId1905(Utf8 movieId1905) {
		this.movieId1905 = movieId1905;
	}

	public Utf8 getMovieChineseName() {
		return movieChineseName;
	}

	public void setMovieChineseName(Utf8 movieChineseName) {
		this.movieChineseName = movieChineseName;
	}

	public Utf8 getMovieEnglishName() {
		return movieEnglishName;
	}

	public void setMovieEnglishName(Utf8 movieEnglishName) {
		this.movieEnglishName = movieEnglishName;
	}

	public Utf8 getMovieShortName() {
		return movieShortName;
	}

	public void setMovieShortName(Utf8 movieShortName) {
		this.movieShortName = movieShortName;
	}

	public Utf8 getMovieAliasName() {
		return movieAliasName;
	}

	public void setMovieAliasName(Utf8 movieAliasName) {
		this.movieAliasName = movieAliasName;
	}

	public Utf8 getDirector() {
		return director;
	}

	public void setDirector(Utf8 director) {
		this.director = director;
	}

	public Utf8 getWriter() {
		return writer;
	}

	public void setWriter(Utf8 writer) {
		this.writer = writer;
	}

	public Utf8 getActor() {
		return actor;
	}

	public void setActor(Utf8 actor) {
		this.actor = actor;
	}

	public Utf8 getProducer() {
		return producer;
	}

	public void setProducer(Utf8 producer) {
		this.producer = producer;
	}

	public Utf8 getCinematography() {
		return cinematography;
	}

	public void setCinematography(Utf8 cinematography) {
		this.cinematography = cinematography;
	}

	public Utf8 getCutter() {
		return cutter;
	}

	public void setCutter(Utf8 cutter) {
		this.cutter = cutter;
	}

	public Utf8 getWardrobe() {
		return wardrobe;
	}

	public void setWardrobe(Utf8 wardrobe) {
		this.wardrobe = wardrobe;
	}

	public Utf8 getVisualAffectArtist() {
		return visualAffectArtist;
	}

	public void setVisualAffectArtist(Utf8 visualAffectArtist) {
		this.visualAffectArtist = visualAffectArtist;
	}

	public Utf8 getAssociateDirector() {
		return associateDirector;
	}

	public void setAssociateDirector(Utf8 associateDirector) {
		this.associateDirector = associateDirector;
	}

	public Utf8 getMovieType() {
		return movieType;
	}

	public void setMovieType(Utf8 movieType) {
		this.movieType = movieType;
	}

	public Utf8 getPublishCompany() {
		return publishCompany;
	}

	public void setPublishCompany(Utf8 publishCompany) {
		this.publishCompany = publishCompany;
	}

	public Utf8 getProduceCompany() {
		return produceCompany;
	}

	public void setProduceCompany(Utf8 produceCompany) {
		this.produceCompany = produceCompany;
	}

	public Utf8 getDistributionInformation() {
		return distributionInformation;
	}

	public void setDistributionInformation(Utf8 distributionInformation) {
		this.distributionInformation = distributionInformation;
	}

	public Utf8 getPlotIntroduction() {
		return plotIntroduction;
	}

	public void setPlotIntroduction(Utf8 plotIntroduction) {
		this.plotIntroduction = plotIntroduction;
	}

	public Utf8 getRoleIntroduction() {
		return roleIntroduction;
	}

	public void setRoleIntroduction(Utf8 roleIntroduction) {
		this.roleIntroduction = roleIntroduction;
	}

	public Utf8 getMovieLongComment() {
		return movieLongComment;
	}

	public void setMovieLongComment(Utf8 movieLongComment) {
		this.movieLongComment = movieLongComment;
	}

	public Utf8 getMovieShortComment() {
		return movieShortComment;
	}

	public void setMovieShortComment(Utf8 movieShortComment) {
		this.movieShortComment = movieShortComment;
	}

	public Utf8 getMovieWeiboComment() {
		return movieWeiboComment;
	}

	public void setMovieWeiboComment(Utf8 movieWeiboComment) {
		this.movieWeiboComment = movieWeiboComment;
	}

	public Utf8 getMovieNews() {
		return movieNews;
	}

	public void setMovieNews(Utf8 movieNews) {
		this.movieNews = movieNews;
	}

	public float getMovieScoreIMDB() {
		return movieScoreIMDB;
	}

	public void setMovieScoreIMDB(float movieScoreIMDB) {
		this.movieScoreIMDB = movieScoreIMDB;
	}

	public float getMovieScoreMtime() {
		return movieScoreMtime;
	}

	public void setMovieScoreMtime(float movieScoreMtime) {
		this.movieScoreMtime = movieScoreMtime;
	}

	public float getMovieScoreDouban() {
		return movieScoreDouban;
	}

	public void setMovieScoreDouban(float movieScoreDouban) {
		this.movieScoreDouban = movieScoreDouban;
	}

	public float getMovieScore1905() {
		return movieScore1905;
	}

	public void setMovieScore1905(float movieScore1905) {
		this.movieScore1905 = movieScore1905;
	}

	public Utf8 getMovieAwards() {
		return movieAwards;
	}

	public void setMovieAwards(Utf8 movieAwards) {
		this.movieAwards = movieAwards;
	}

	public Utf8 getCinemaIdProfessional() {
		return cinemaIdProfessional;
	}

	public void setCinemaIdProfessional(Utf8 cinemaIdProfessional) {
		this.cinemaIdProfessional = cinemaIdProfessional;
	}

	public Utf8 getCinemaIdMtime() {
		return cinemaIdMtime;
	}

	public void setCinemaIdMtime(Utf8 cinemaIdMtime) {
		this.cinemaIdMtime = cinemaIdMtime;
	}

	public Utf8 getCinemaIdGewara() {
		return cinemaIdGewara;
	}

	public void setCinemaIdGewara(Utf8 cinemaIdGewara) {
		this.cinemaIdGewara = cinemaIdGewara;
	}

	public Utf8 getCinemaIdSell() {
		return cinemaIdSell;
	}

	public void setCinemaIdSell(Utf8 cinemaIdSell) {
		this.cinemaIdSell = cinemaIdSell;
	}

	public Utf8 getCinemaChineseName() {
		return cinemaChineseName;
	}

	public void setCinemaChineseName(Utf8 cinemaChineseName) {
		this.cinemaChineseName = cinemaChineseName;
	}

	public Utf8 getCinemaShortName() {
		return cinemaShortName;
	}

	public void setCinemaShortName(Utf8 cinemaShortName) {
		this.cinemaShortName = cinemaShortName;
	}

	public Utf8 getCinemaAliasName() {
		return cinemaAliasName;
	}

	public void setCinemaAliasName(Utf8 cinemaAliasName) {
		this.cinemaAliasName = cinemaAliasName;
	}

	public Utf8 getCinemaCity() {
		return cinemaCity;
	}

	public void setCinemaCity(Utf8 cinemaCity) {
		this.cinemaCity = cinemaCity;
	}

	public Utf8 getCinemaDistrict() {
		return cinemaDistrict;
	}

	public void setCinemaDistrict(Utf8 cinemaDistrict) {
		this.cinemaDistrict = cinemaDistrict;
	}

	public Utf8 getCinemaStreet() {
		return cinemaStreet;
	}

	public void setCinemaStreet(Utf8 cinemaStreet) {
		this.cinemaStreet = cinemaStreet;
	}

	public Utf8 getCinemaAmapLocation() {
		return cinemaAmapLocation;
	}

	public void setCinemaAmapLocation(Utf8 cinemaAmapLocation) {
		this.cinemaAmapLocation = cinemaAmapLocation;
	}

	public Utf8 getCinemaBaiduLocation() {
		return cinemaBaiduLocation;
	}

	public void setCinemaBaiduLocation(Utf8 cinemaBaiduLocation) {
		this.cinemaBaiduLocation = cinemaBaiduLocation;
	}

	public Utf8 getTheaterChainName() {
		return theaterChainName;
	}

	public void setTheaterChainName(Utf8 theaterChainName) {
		this.theaterChainName = theaterChainName;
	}

	public Utf8 getFilmInvestmentCompany() {
		return filmInvestmentCompany;
	}

	public void setFilmInvestmentCompany(Utf8 filmInvestmentCompany) {
		this.filmInvestmentCompany = filmInvestmentCompany;
	}

	public Utf8 getCinemaAssetsJoining() {
		return cinemaAssetsJoining;
	}

	public void setCinemaAssetsJoining(Utf8 cinemaAssetsJoining) {
		this.cinemaAssetsJoining = cinemaAssetsJoining;
	}

	public Utf8 getBus() {
		return bus;
	}

	public void setBus(Utf8 bus) {
		this.bus = bus;
	}

	public Utf8 getSubway() {
		return subway;
	}

	public void setSubway(Utf8 subway) {
		this.subway = subway;
	}

	public Utf8 getDrive() {
		return drive;
	}

	public void setDrive(Utf8 drive) {
		this.drive = drive;
	}

	public Utf8 getCinemaFacility() {
		return cinemaFacility;
	}

	public void setCinemaFacility(Utf8 cinemaFacility) {
		this.cinemaFacility = cinemaFacility;
	}

	public Utf8 getCinema3D() {
		return cinema3D;
	}

	public void setCinema3D(Utf8 cinema3d) {
		cinema3D = cinema3d;
	}

	public Utf8 getCinemaCreditCard() {
		return cinemaCreditCard;
	}

	public void setCinemaCreditCard(Utf8 cinemaCreditCard) {
		this.cinemaCreditCard = cinemaCreditCard;
	}

	public Utf8 getCinemaSellGoods() {
		return cinemaSellGoods;
	}

	public void setCinemaSellGoods(Utf8 cinemaSellGoods) {
		this.cinemaSellGoods = cinemaSellGoods;
	}

	public Utf8 getCinemaParking() {
		return cinemaParking;
	}

	public void setCinemaParking(Utf8 cinemaParking) {
		this.cinemaParking = cinemaParking;
	}

	public Utf8 getCinemaChildrenTicket() {
		return cinemaChildrenTicket;
	}

	public void setCinemaChildrenTicket(Utf8 cinemaChildrenTicket) {
		this.cinemaChildrenTicket = cinemaChildrenTicket;
	}

	public Utf8 getCinemaResting() {
		return cinemaResting;
	}

	public void setCinemaResting(Utf8 cinemaResting) {
		this.cinemaResting = cinemaResting;
	}

	public Utf8 getCinemaIntroduction() {
		return cinemaIntroduction;
	}

	public void setCinemaIntroduction(Utf8 cinemaIntroduction) {
		this.cinemaIntroduction = cinemaIntroduction;
	}

	public Utf8 getCinemaDeals() {
		return cinemaDeals;
	}

	public void setCinemaDeals(Utf8 cinemaDeals) {
		this.cinemaDeals = cinemaDeals;
	}

	public Utf8 getCinemaCommentMtime() {
		return cinemaCommentMtime;
	}

	public void setCinemaCommentMtime(Utf8 cinemaCommentMtime) {
		this.cinemaCommentMtime = cinemaCommentMtime;
	}

	public Utf8 getCinemaCommentGewara() {
		return cinemaCommentGewara;
	}

	public void setCinemaCommentGewara(Utf8 cinemaCommentGewara) {
		this.cinemaCommentGewara = cinemaCommentGewara;
	}

	public Utf8 getCinemaCommentDianping() {
		return cinemaCommentDianping;
	}

	public void setCinemaCommentDianping(Utf8 cinemaCommentDianping) {
		this.cinemaCommentDianping = cinemaCommentDianping;
	}

	public Utf8 getCinemaCommentWeibo() {
		return cinemaCommentWeibo;
	}

	public void setCinemaCommentWeibo(Utf8 cinemaCommentWeibo) {
		this.cinemaCommentWeibo = cinemaCommentWeibo;
	}

	public float getCinemaScoreMtime() {
		return cinemaScoreMtime;
	}

	public void setCinemaScoreMtime(float cinemaScoreMtime) {
		this.cinemaScoreMtime = cinemaScoreMtime;
	}

	public float getCinemaScoreGewara() {
		return cinemaScoreGewara;
	}

	public void setCinemaScoreGewara(float cinemaScoreGewara) {
		this.cinemaScoreGewara = cinemaScoreGewara;
	}

	public float getCinemaScoreDianping() {
		return cinemaScoreDianping;
	}

	public void setCinemaScoreDianping(float cinemaScoreDianping) {
		this.cinemaScoreDianping = cinemaScoreDianping;
	}

	public Utf8 getActorIdMtime() {
		return actorIdMtime;
	}

	public void setActorIdMtime(Utf8 actorIdMtime) {
		this.actorIdMtime = actorIdMtime;
	}

	public Utf8 getActorIdDouban() {
		return actorIdDouban;
	}

	public void setActorIdDouban(Utf8 actorIdDouban) {
		this.actorIdDouban = actorIdDouban;
	}

	public Utf8 getActorId1905() {
		return actorId1905;
	}

	public void setActorId1905(Utf8 actorId1905) {
		this.actorId1905 = actorId1905;
	}

	public Utf8 getActorIdIMDB() {
		return actorIdIMDB;
	}

	public void setActorIdIMDB(Utf8 actorIdIMDB) {
		this.actorIdIMDB = actorIdIMDB;
	}

	public Utf8 getActorChineseName() {
		return actorChineseName;
	}

	public void setActorChineseName(Utf8 actorChineseName) {
		this.actorChineseName = actorChineseName;
	}

	public Utf8 getActorEnglishName() {
		return actorEnglishName;
	}

	public void setActorEnglishName(Utf8 actorEnglishName) {
		this.actorEnglishName = actorEnglishName;
	}

	public Utf8 getActorShortName() {
		return actorShortName;
	}

	public void setActorShortName(Utf8 actorShortName) {
		this.actorShortName = actorShortName;
	}

	public Utf8 getActorAliasName() {
		return actorAliasName;
	}

	public void setActorAliasName(Utf8 actorAliasName) {
		this.actorAliasName = actorAliasName;
	}

	public Utf8 getActorBirthName() {
		return actorBirthName;
	}

	public void setActorBirthName(Utf8 actorBirthName) {
		this.actorBirthName = actorBirthName;
	}

	public Utf8 getGender() {
		return gender;
	}

	public void setGender(Utf8 gender) {
		this.gender = gender;
	}

	public Utf8 getBirthday() {
		return birthday;
	}

	public void setBirthday(Utf8 birthday) {
		this.birthday = birthday;
	}

	public Utf8 getBirthplace() {
		return birthplace;
	}

	public void setBirthplace(Utf8 birthplace) {
		this.birthplace = birthplace;
	}

	public Utf8 getBloodtype() {
		return bloodtype;
	}

	public void setBloodtype(Utf8 bloodtype) {
		this.bloodtype = bloodtype;
	}

	public Utf8 getHeight() {
		return height;
	}

	public void setHeight(Utf8 height) {
		this.height = height;
	}

	public Utf8 getWeight() {
		return weight;
	}

	public void setWeight(Utf8 weight) {
		this.weight = weight;
	}

	public Utf8 getActorIntroduction() {
		return actorIntroduction;
	}

	public void setActorIntroduction(Utf8 actorIntroduction) {
		this.actorIntroduction = actorIntroduction;
	}

	public Utf8 getBirthplaceIntroduction() {
		return birthplaceIntroduction;
	}

	public void setBirthplaceIntroduction(Utf8 birthplaceIntroduction) {
		this.birthplaceIntroduction = birthplaceIntroduction;
	}

	public Utf8 getProfession() {
		return profession;
	}

	public void setProfession(Utf8 profession) {
		this.profession = profession;
	}

	public Utf8 getActorCompany() {
		return actorCompany;
	}

	public void setActorCompany(Utf8 actorCompany) {
		this.actorCompany = actorCompany;
	}

	public Utf8 getActorNews() {
		return actorNews;
	}

	public void setActorNews(Utf8 actorNews) {
		this.actorNews = actorNews;
	}

	public Utf8 getActorProduction() {
		return actorProduction;
	}

	public void setActorProduction(Utf8 actorProduction) {
		this.actorProduction = actorProduction;
	}

	public Utf8 getActorCommentMtime() {
		return actorCommentMtime;
	}

	public void setActorCommentMtime(Utf8 actorCommentMtime) {
		this.actorCommentMtime = actorCommentMtime;
	}

	public Utf8 getActorCommentGewara() {
		return actorCommentGewara;
	}

	public void setActorCommentGewara(Utf8 actorCommentGewara) {
		this.actorCommentGewara = actorCommentGewara;
	}

	public Utf8 getActorCommentDouban() {
		return actorCommentDouban;
	}

	public void setActorCommentDouban(Utf8 actorCommentDouban) {
		this.actorCommentDouban = actorCommentDouban;
	}

	public Utf8 getActorCommentWeibo() {
		return actorCommentWeibo;
	}

	public void setActorCommentWeibo(Utf8 actorCommentWeibo) {
		this.actorCommentWeibo = actorCommentWeibo;
	}

	public float getActorScore() {
		return actorScore;
	}

	public void setActorScore(float actorScore) {
		this.actorScore = actorScore;
	}

	public Utf8 getRelatives() {
		return relatives;
	}

	public void setRelatives(Utf8 relatives) {
		this.relatives = relatives;
	}

	public Utf8 getGossip() {
		return gossip;
	}

	public void setGossip(Utf8 gossip) {
		this.gossip = gossip;
	}

	public Utf8 getActorAwards() {
		return actorAwards;
	}

	public void setActorAwards(Utf8 actorAwards) {
		this.actorAwards = actorAwards;
	}

	public Utf8 getAwardIdMtime() {
		return awardIdMtime;
	}

	public void setAwardIdMtime(Utf8 awardIdMtime) {
		this.awardIdMtime = awardIdMtime;
	}

	public Utf8 getAwardIdIMDB() {
		return awardIdIMDB;
	}

	public void setAwardIdIMDB(Utf8 awardIdIMDB) {
		this.awardIdIMDB = awardIdIMDB;
	}

	public Utf8 getAwardId1905() {
		return awardId1905;
	}

	public void setAwardId1905(Utf8 awardId1905) {
		this.awardId1905 = awardId1905;
	}

	public Utf8 getAwardChineseName() {
		return awardChineseName;
	}

	public void setAwardChineseName(Utf8 awardChineseName) {
		this.awardChineseName = awardChineseName;
	}

	public Utf8 getAwardEnglishName() {
		return awardEnglishName;
	}

	public void setAwardEnglishName(Utf8 awardEnglishName) {
		this.awardEnglishName = awardEnglishName;
	}

	public Utf8 getAwardShortName() {
		return awardShortName;
	}

	public void setAwardShortName(Utf8 awardShortName) {
		this.awardShortName = awardShortName;
	}

	public Utf8 getAwardAliasName() {
		return awardAliasName;
	}

	public void setAwardAliasName(Utf8 awardAliasName) {
		this.awardAliasName = awardAliasName;
	}

	public Utf8 getAwardHoldTime() {
		return awardHoldTime;
	}

	public void setAwardHoldTime(Utf8 awardHoldTime) {
		this.awardHoldTime = awardHoldTime;
	}

	public Utf8 getAwardHoldPlace() {
		return awardHoldPlace;
	}

	public void setAwardHoldPlace(Utf8 awardHoldPlace) {
		this.awardHoldPlace = awardHoldPlace;
	}

	public Utf8 getAwardHoldCompany() {
		return awardHoldCompany;
	}

	public void setAwardHoldCompany(Utf8 awardHoldCompany) {
		this.awardHoldCompany = awardHoldCompany;
	}

	public Utf8 getAwardItems() {
		return awardItems;
	}

	public void setAwardItems(Utf8 awardItems) {
		this.awardItems = awardItems;
	}

	public Utf8 getAwardMovies() {
		return awardMovies;
	}

	public void setAwardMovies(Utf8 awardMovies) {
		this.awardMovies = awardMovies;
	}

	public Utf8 getWeiboV() {
		return weiboV;
	}

	public void setWeiboV(Utf8 weiboV) {
		this.weiboV = weiboV;
	}

	public int getWeiboLevel() {
		return weiboLevel;
	}

	public void setWeiboLevel(int weiboLevel) {
		this.weiboLevel = weiboLevel;
	}

	public Utf8 getWeiboName() {
		return weiboName;
	}

	public void setWeiboName(Utf8 weiboName) {
		this.weiboName = weiboName;
	}

	public Utf8 getWeiboId() {
		return weiboId;
	}

	public void setWeiboId(Utf8 weiboId) {
		this.weiboId = weiboId;
	}

	public Utf8 getWeiboOfficial() {
		return weiboOfficial;
	}

	public void setWeiboOfficial(Utf8 weiboOfficial) {
		this.weiboOfficial = weiboOfficial;
	}

	public int getWeiboFans() {
		return weiboFans;
	}

	public void setWeiboFans(int weiboFans) {
		this.weiboFans = weiboFans;
	}

	public int getWeiboNum() {
		return weiboNum;
	}

	public void setWeiboNum(int weiboNum) {
		this.weiboNum = weiboNum;
	}

	public int getWeiboInterest() {
		return weiboInterest;
	}

	public void setWeiboInterest(int weiboInterest) {
		this.weiboInterest = weiboInterest;
	}

	public Utf8 getWeiboTime() {
		return weiboTime;
	}

	public void setWeiboTime(Utf8 weiboTime) {
		this.weiboTime = weiboTime;
	}

	public Utf8 getWeiboContent() {
		return weiboContent;
	}

	public void setWeiboContent(Utf8 weiboContent) {
		this.weiboContent = weiboContent;
	}

	public int getWeiboZan() {
		return weiboZan;
	}

	public void setWeiboZan(int weiboZan) {
		this.weiboZan = weiboZan;
	}

	public Utf8 getWeiboTopic() {
		return weiboTopic;
	}

	public void setWeiboTopic(Utf8 weiboTopic) {
		this.weiboTopic = weiboTopic;
	}

	public Utf8 getWeiboTransmit() {
		return weiboTransmit;
	}

	public void setWeiboTransmit(Utf8 weiboTransmit) {
		this.weiboTransmit = weiboTransmit;
	}

	public Utf8 getWeiboComment() {
		return weiboComment;
	}

	public void setWeiboComment(Utf8 weiboComment) {
		this.weiboComment = weiboComment;
	}

	public Utf8 getNoProfessional() {
		return NoProfessional;
	}

	public void setNoProfessional(Utf8 noProfessional) {
		NoProfessional = noProfessional;
	}

	public Utf8 getNoSite() {
		return NoSite;
	}

	public void setNoSite(Utf8 noSite) {
		NoSite = noSite;
	}

	public Utf8 getSiteMtime() {
		return siteMtime;
	}

	public void setSiteMtime(Utf8 siteMtime) {
		this.siteMtime = siteMtime;
	}

	public Utf8 getSiteTaobao() {
		return siteTaobao;
	}

	public void setSiteTaobao(Utf8 siteTaobao) {
		this.siteTaobao = siteTaobao;
	}

	public Utf8 getSiteGewara() {
		return siteGewara;
	}

	public void setSiteGewara(Utf8 siteGewara) {
		this.siteGewara = siteGewara;
	}

	public Utf8 getSiteMaoyan() {
		return siteMaoyan;
	}

	public void setSiteMaoyan(Utf8 siteMaoyan) {
		this.siteMaoyan = siteMaoyan;
	}

	public Utf8 getSiteWangpiao() {
		return siteWangpiao;
	}

	public void setSiteWangpiao(Utf8 siteWangpiao) {
		this.siteWangpiao = siteWangpiao;
	}

	public Utf8 getSiteSpider() {
		return siteSpider;
	}

	public void setSiteSpider(Utf8 siteSpider) {
		this.siteSpider = siteSpider;
	}

	public Utf8 getSiteBaidu() {
		return siteBaidu;
	}

	public void setSiteBaidu(Utf8 siteBaidu) {
		this.siteBaidu = siteBaidu;
	}

	public Utf8 getSiteAmap() {
		return siteAmap;
	}

	public void setSiteAmap(Utf8 siteAmap) {
		this.siteAmap = siteAmap;
	}

	public Utf8 getNoFilm() {
		return NoFilm;
	}

	public void setNoFilm(Utf8 noFilm) {
		NoFilm = noFilm;
	}

	public Utf8 getNoTheater() {
		return NoTheater;
	}

	public void setNoTheater(Utf8 noTheater) {
		NoTheater = noTheater;
	}

	public Utf8 getNoHall() {
		return NoHall;
	}

	public void setNoHall(Utf8 noHall) {
		NoHall = noHall;
	}

	public Utf8 getSellSeat() {
		return sellSeat;
	}

	public void setSellSeat(Utf8 sellSeat) {
		this.sellSeat = sellSeat;
	}

	public Utf8 getPrice() {
		return price;
	}

	public void setPrice(Utf8 price) {
		this.price = price;
	}

	public Utf8 getVipPrice() {
		return vipPrice;
	}

	public void setVipPrice(Utf8 vipPrice) {
		this.vipPrice = vipPrice;
	}

	public Utf8 getPromotionPrice() {
		return promotionPrice;
	}

	public void setPromotionPrice(Utf8 promotionPrice) {
		this.promotionPrice = promotionPrice;
	}

	public Utf8 getNoLanguage() {
		return NoLanguage;
	}

	public void setNoLanguage(Utf8 noLanguage) {
		NoLanguage = noLanguage;
	}

	public Utf8 getNoVersion() {
		return NoVersion;
	}

	public void setNoVersion(Utf8 noVersion) {
		NoVersion = noVersion;
	}

	public Utf8 getNoTime() {
		return NoTime;
	}

	public void setNoTime(Utf8 noTime) {
		NoTime = noTime;
	}

	public Utf8 getActivitySite() {
		return activitySite;
	}

	public void setActivitySite(Utf8 activitySite) {
		this.activitySite = activitySite;
	}

	public Utf8 getActivityFilm() {
		return activityFilm;
	}

	public void setActivityFilm(Utf8 activityFilm) {
		this.activityFilm = activityFilm;
	}

	public Utf8 getActivityTheater() {
		return activityTheater;
	}

	public void setActivityTheater(Utf8 activityTheater) {
		this.activityTheater = activityTheater;
	}

	public Utf8 getActivityRule() {
		return activityRule;
	}

	public void setActivityRule(Utf8 activityRule) {
		this.activityRule = activityRule;
	}

	public Utf8 getActivityPrPrice() {
		return activityPrPrice;
	}

	public void setActivityPrPrice(Utf8 activityPrPrice) {
		this.activityPrPrice = activityPrPrice;
	}

	public Utf8 getActivityPrice() {
		return activityPrice;
	}

	public void setActivityPrice(Utf8 activityPrice) {
		this.activityPrice = activityPrice;
	}

	public Utf8 getActivityTime() {
		return activityTime;
	}

	public void setActivityTime(Utf8 activityTime) {
		this.activityTime = activityTime;
	}

	public Utf8 getActivityEffectTime() {
		return activityEffectTime;
	}

	public void setActivityEffectTime(Utf8 activityEffectTime) {
		this.activityEffectTime = activityEffectTime;
	}

	public Utf8 getMovieNewsOutlinks() {
		return movieNewsOutlinks;
	}

	public void setMovieNewsOutlinks(Utf8 movieNewsOutlinks) {
		this.movieNewsOutlinks = movieNewsOutlinks;
	}

	public Utf8 getMovieNewsName() {
		return movieNewsName;
	}

	public void setMovieNewsName(Utf8 movieNewsName) {
		this.movieNewsName = movieNewsName;
	}

}
