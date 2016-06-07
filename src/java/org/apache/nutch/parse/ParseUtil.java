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
package org.apache.nutch.parse;

// Commons Logging imports
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.avro.util.Utf8;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.StringUtils;
import org.apache.nutch.crawl.CrawlStatus;
import org.apache.nutch.crawl.Signature;
import org.apache.nutch.crawl.SignatureFactory;
import org.apache.nutch.crawl.URLWebPage;
import org.apache.nutch.fetcher.FetcherJob;
import org.apache.nutch.net.URLFilterException;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.storage.Mark;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.TableUtil;
import org.apache.nutch.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * A Utility class containing methods to simply perform parsing utilities such
 * as iterating through a preferred list of {@link Parser}s to obtain
 * {@link Parse} objects.
 *
 * @author mattmann
 * @author J&eacute;r&ocirc;me Charron
 * @author S&eacute;bastien Le Callonnec
 */
public class ParseUtil extends Configured {

  /* our log stream */
  public static final Logger LOG = LoggerFactory.getLogger(ParseUtil.class);

  private static final int DEFAULT_MAX_PARSE_TIME = 30;
  
  private Configuration conf;
  private Signature sig;
  private URLFilters filters;
  private URLNormalizers normalizers;
  private int maxOutlinks;
  private boolean ignoreExternalLinks;
  private ParserFactory parserFactory;
  /** Parser timeout set to 30 sec by default. Set -1 to deactivate **/
  private int maxParseTime;
  private ExecutorService executorService;
  
  /**
   *
   * @param conf
   */
  public ParseUtil(Configuration conf) {
    super(conf);
    setConf(conf);
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
    parserFactory = new ParserFactory(conf);
    maxParseTime=conf.getInt("parser.timeout", DEFAULT_MAX_PARSE_TIME);
    sig = SignatureFactory.getSignature(conf);
    filters = new URLFilters(conf);
    normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_OUTLINK);
    int maxOutlinksPerPage = conf.getInt("db.max.outlinks.per.page", 100);
    maxOutlinks = (maxOutlinksPerPage < 0) ? Integer.MAX_VALUE : maxOutlinksPerPage;
    ignoreExternalLinks = conf.getBoolean("db.ignore.external.links", false);
    executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
      .setNameFormat("parse-%d").setDaemon(true).build());
  }

  /**
   * Performs a parse by iterating through a List of preferred {@link Parser}s
   * until a successful parse is performed and a {@link Parse} object is
   * returned. If the parse is unsuccessful, a message is logged to the
   * <code>WARNING</code> level, and an empty parse is returned.
   *
   * @throws ParserNotFound If there is no suitable parser found. 
   * @throws ParseException If there is an error parsing.
   */
  public Parse parse(String url, WebPage page) throws ParserNotFound, 
      ParseException {
    Parser[] parsers = null;

    String contentType = TableUtil.toString(page.getContentType());

    parsers = this.parserFactory.getParsers(contentType, url);

    for (int i=0; i<parsers.length; i++) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Parsing [" + url + "] with [" + parsers[i] + "]");
      }
      Parse parse = null;
      
      if (maxParseTime!=-1)
    	  parse = runParser(parsers[i], url, page);
      else 
    	  parse = parsers[i].getParse(url, page);
      
      if (parse!=null && ParseStatusUtils.isSuccess(parse.getParseStatus())) {
        return parse;
      }
    }

    LOG.warn("Unable to successfully parse content " + url +
        " of type " + contentType);
    return ParseStatusUtils.getEmptyParse(new ParseException("Unable to successfully parse content"), null);
  }
  
  private Parse runParser(Parser p, String url, WebPage page) {    
    ParseCallable pc = new ParseCallable(p, page, url);
    Future<Parse> task = executorService.submit(pc);
    Parse res = null;
    try {
      res = task.get(maxParseTime, TimeUnit.SECONDS);
    } catch (Exception e) {
      LOG.warn("Error parsing " + url, e);
      task.cancel(true);
    } finally {
      pc = null;
    }
    return res;
  }

  /**
   * Parses given web page and stores parsed content within page. Puts
   * a meta-redirect to outlinks.
   * @param key
   * @param page
   */
  public void process(String key, WebPage page) {
    String url = TableUtil.unreverseUrl(key);
    byte status = (byte) page.getStatus();
    if (status != CrawlStatus.STATUS_FETCHED) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Skipping " + url + " as status is: " + CrawlStatus.getName(status));
      }
      return;
    }

    Parse parse;
    try {
      parse = parse(url, page);
    } catch (ParserNotFound e) {
      // do not print stacktrace for the fact that some types are not mapped.
      LOG.warn("No suitable parser found: " + e.getMessage());
      return;
    } catch (final Exception e) {
      LOG.warn("Error parsing: " + url + ": "
          + StringUtils.stringifyException(e));
      return;
    }

    if (parse == null) {
      return;
    }

    final byte[] signature = sig.calculate(page);

    org.apache.nutch.storage.ParseStatus pstatus = parse.getParseStatus();
    page.setParseStatus(pstatus);
    if (ParseStatusUtils.isSuccess(pstatus)) {
      if (pstatus.getMinorCode() == ParseStatusCodes.SUCCESS_REDIRECT) {
        String newUrl = ParseStatusUtils.getMessage(pstatus);
        int refreshTime = Integer.parseInt(ParseStatusUtils.getArg(pstatus, 1));
        try {
          newUrl = normalizers.normalize(newUrl, URLNormalizers.SCOPE_FETCHER);
          if (newUrl == null) {
            LOG.warn("redirect normalized to null " + url);
            return;
          }
          try {
            newUrl = filters.filter(newUrl);
          } catch (URLFilterException e) {
            return;
          }
          if (newUrl == null) {
            LOG.warn("redirect filtered to null " + url);
            return;
          }
        } catch (MalformedURLException e) {
          LOG.warn("malformed url exception parsing redirect " + url);
          return;
        }
        page.putToOutlinks(new Utf8(newUrl), new Utf8());
        page.putToMetadata(FetcherJob.REDIRECT_DISCOVERED, TableUtil.YES_VAL);
        if (newUrl == null || newUrl.equals(url)) {
          String reprUrl = URLUtil.chooseRepr(url, newUrl,
              refreshTime < FetcherJob.PERM_REFRESH_TIME);
          if (reprUrl == null) {
            LOG.warn("reprUrl==null for " + url);
            return;
          } else {
            page.setReprUrl(new Utf8(reprUrl));
          }
        }
      } else {
    	  page.setMovieIdProfessional(parse.getMovieIdProfessional());
    	  page.setMovieIdIMDB(parse.getMovieIdIMDB());
    	  page.setMovieIdMtime(parse.getMovieIdMtime());
    	  page.setMovieIdDouban(parse.getMovieIdDouban());
    	  page.setMovieId1905(parse.getMovieId1905());
    	  page.setMovieChineseName(parse.getMovieChineseName());
    	  page.setMovieEnglishName(parse.getMovieEnglishName());
    	  page.setMovieAliasName(parse.getMovieAliasName());
    	  page.setDirector(parse.getDirector());
    	  page.setWriter(parse.getWriter());
    	  page.setActor(parse.getActor());
    	  page.setProducer(parse.getProducer());
    	  page.setCinematography(parse.getCinematography());
    	  page.setCutter(parse.getCutter());
    	  page.setWardrobe(parse.getWardrobe());
    	  page.setVisualAffectArtist(parse.getVisualAffectArtist());
    	  page.setAssociateDirector(parse.getAssociateDirector());
    	  page.setMovieType(parse.getMovieType());
    	  page.setPublishCompany(parse.getPublishCompany());
    	  page.setProduceCompany(parse.getProduceCompany());
    	  page.setDistributionInformation(parse.getDistributionInformation());
    	  page.setPlotIntroduction(parse.getPlotIntroduction());
    	  page.setRoleIntroduction(parse.getRoleIntroduction());
    	  page.setMovieLongComment(parse.getMovieLongComment());
    	  page.setMovieShortComment(parse.getMovieShortComment());
    	  page.setMovieWeiboComment(parse.getMovieWeiboComment());
    	  page.setMovieNews(parse.getMovieNews());
    	  page.setMovieScoreIMDB(parse.getMovieScoreIMDB());
    	  page.setMovieScoreMtime(parse.getMovieScoreMtime());
    	  page.setMovieScoreDouban(parse.getMovieScoreDouban());
    	  page.setMovieScore1905(parse.getMovieScore1905());
    	  page.setMovieAwards(parse.getMovieAwards());
    	  
    	  page.setCinemaIdProfessional(parse.getCinemaIdProfessional());
    	  page.setCinemaIdMtime(parse.getCinemaIdMtime());
    	  page.setCinemaIdGewara(parse.getCinemaIdGewara());
    	  page.setCinemaIdSell(parse.getCinemaIdSell());
    	  page.setCinemaChineseName(parse.getCinemaChineseName());
    	  page.setCinemaShortName(parse.getCinemaShortName());
    	  page.setCinemaAliasName(parse.getCinemaAliasName());
    	  page.setCinemaCity(parse.getCinemaCity());
    	  page.setCinemaDistrict(parse.getCinemaDistrict());
    	  page.setCinemaStreet(parse.getCinemaStreet());
    	  page.setCinemaAmapLocation(parse.getCinemaAmapLocation());
    	  page.setCinemaBaiduLocation(parse.getCinemaBaiduLocation());
    	  page.setTheaterChainName(parse.getTheaterChainName());
    	  page.setFilmInvestmentCompany(parse.getFilmInvestmentCompany());
    	  page.setCinemaAssetsJoining(parse.getCinemaAssetsJoining());
    	  page.setBus(parse.getBus());
    	  page.setSubway(parse.getSubway());
    	  page.setDrive(parse.getDrive());
    	  page.setCinemaFacility(parse.getCinemaFacility());
    	  page.setCinema3D(parse.getCinema3D());
    	  page.setCinemaCreditCard(parse.getCinemaCreditCard());
    	  page.setCinemaSellGoods(parse.getCinemaSellGoods());
    	  page.setCinemaParking(parse.getCinemaParking());
    	  page.setCinemaChildrenTicket(parse.getCinemaChildrenTicket());
    	  page.setCinemaResting(parse.getCinemaResting());
    	  page.setCinemaIntroduction(parse.getCinemaIntroduction());
    	  page.setCinemaDeals(parse.getCinemaDeals());
    	  page.setCinemaCommentMtime(parse.getCinemaCommentMtime());
    	  page.setCinemaCommentGewara(parse.getCinemaCommentGewara());
    	  page.setCinemaCommentDianping(parse.getCinemaCommentDianping());
    	  page.setCinemaCommentWeibo(parse.getCinemaCommentWeibo());
    	  page.setCinemaScoreMtime(parse.getCinemaScoreMtime());
    	  page.setCinemaScoreGewara(parse.getCinemaScoreGewara());
    	  page.setCinemaScoreDianping(parse.getCinemaScoreDianping());
    	  
    	  page.setActorIdMtime(parse.getActorIdMtime());
    	  page.setActorIdDouban(parse.getActorIdDouban());
    	  page.setActorId1905(parse.getActorId1905());
    	  page.setActorIdIMDB(parse.getActorIdIMDB());
    	  page.setActorChineseName(parse.getActorChineseName());
    	  page.setActorEnglishName(parse.getActorEnglishName());
    	  page.setActorShortName(parse.getActorShortName());
    	  page.setActorAliasName(parse.getActorAliasName());
    	  page.setActorBirthName(parse.getActorBirthName());
    	  page.setGender(parse.getGender());
    	  page.setBirthday(parse.getBirthday());
    	  page.setBirthplace(parse.getBirthplace());
    	  page.setBloodtype(parse.getBloodtype());
    	  page.setHeight(parse.getHeight());
    	  page.setWeight(parse.getWeight());
    	  page.setActorIntroduction(parse.getActorIntroduction());
    	  page.setBirthplaceIntroduction(parse.getBirthplaceIntroduction());
    	  page.setProfession(parse.getProfession());
    	  page.setActorCompany(parse.getActorCompany());
    	  page.setActorNews(parse.getActorNews());
    	  page.setActorProduction(parse.getActorProduction());
    	  page.setActorCommentMtime(parse.getActorCommentMtime());
    	  page.setActorCommentGewara(parse.getActorCommentGewara());
    	  page.setActorCommentDouban(parse.getActorCommentDouban());
    	  page.setActorCommentWeibo(parse.getActorCommentWeibo());
    	  page.setActorScore(parse.getActorScore());
    	  page.setRelatives(parse.getRelatives());
    	  page.setGossip(parse.getGossip());
    	  page.setActorAwards(parse.getActorAwards());
    	  
    	  page.setAwardIdMtime(parse.getAwardIdMtime());
    	  page.setAwardIdIMDB(parse.getAwardIdIMDB());
    	  page.setAwardId1905(parse.getAwardId1905());
    	  page.setAwardChineseName(parse.getAwardChineseName());
    	  page.setAwardEnglishName(parse.getAwardEnglishName());
    	  page.setAwardShortName(parse.getAwardShortName());
    	  page.setAwardAliasName(parse.getAwardAliasName());
    	  page.setAwardHoldTime(parse.getAwardHoldTime());
    	  page.setAwardHoldPlace(parse.getAwardHoldPlace());
    	  page.setAwardHoldCompany(parse.getAwardHoldCompany());
    	  page.setAwardItems(parse.getAwardItems());
    	  page.setAwardMovies(parse.getAwardMovies());
    	  
    	  page.setWeiboV(parse.getWeiboV());
    	  page.setWeiboLevel(parse.getWeiboLevel());
    	  page.setWeiboName(parse.getWeiboName());
    	  page.setWeiboId(parse.getWeiboId());
    	  page.setWeiboOfficial(parse.getWeiboOfficial());
    	  page.setWeiboFans(parse.getWeiboFans());
    	  page.setWeiboNum(parse.getWeiboNum());
    	  page.setWeiboInterest(parse.getWeiboInterest());
    	  page.setWeiboTime(parse.getWeiboTime());
    	  page.setWeiboContent(parse.getWeiboContent());
    	  page.setWeiboZan(parse.getWeiboZan());
    	  page.setweiboTopic(parse.getWeiboTopic());
    	  page.setWeiboTransmit(parse.getWeiboTransmit());
    	  page.setWeiboComment(parse.getWeiboComment());
    	  
    	  page.setNoProfessional(parse.getNoProfessional());
    	  page.setNoSite(parse.getNoSite());
    	  page.setSiteMtime(parse.getSiteMtime());
    	  page.setSiteTaobao(parse.getSiteTaobao());
    	  page.setSiteGewara(parse.getSiteGewara());
    	  page.setSiteMaoyan(parse.getSiteMaoyan());
    	  page.setSiteWangpiao(parse.getSiteWangpiao());
    	  page.setSiteSpider(parse.getSiteSpider());
    	  page.setSiteBaidu(parse.getSiteBaidu());
    	  page.setSiteAmap(parse.getSiteAmap());
    	  page.setNoFilm(parse.getNoFilm());
    	  page.setNoTheater(parse.getNoTheater());
    	  page.setNoHall(parse.getNoHall());
    	  page.setSellSeat(parse.getSellSeat());
    	  page.setPrice(parse.getPrice());
    	  page.setVipPrice(parse.getVipPrice());
    	  page.setPromotionPrice(parse.getPromotionPrice());
    	  page.setNoLanguage(parse.getNoLanguage());
    	  page.setNoVersion(parse.getNoVersion());
    	  page.setNoTime(parse.getNoTime());
    	  
    	  page.setActivitySite(parse.getActivitySite());
    	  page.setActivityFilm(parse.getActivityFilm());
    	  page.setActivityTheater(parse.getActivityTheater());
    	  page.setActivityRule(parse.getActivityRule());
    	  page.setActivityPrice(parse.getActivityPrice());
    	  page.setActivityPrPrice(parse.getActivityPrPrice());
    	  page.setActivityTime(parse.getActivityTime());
    	  page.setActivityEffectTime(parse.getActivityEffectTime());
    	  
    	  page.setMovieNewsOutlinks(parse.getMovieNewsOutlinks());
    	  page.setMovieNewsName(parse.getMovieNewsName());
    	  
    	  
    	  
        page.setText(new Utf8(parse.getText()));
        page.setTitle(new Utf8(parse.getTitle()));
        ByteBuffer prevSig = page.getSignature();
        if (prevSig != null) {
          page.setPrevSignature(prevSig);
        }
        page.setSignature(ByteBuffer.wrap(signature));
        if (page.getOutlinks() != null) {
          page.getOutlinks().clear();
        }
        final Outlink[] outlinks = parse.getOutlinks();
        final int count = 0;
        String fromHost;
        if (ignoreExternalLinks) {
          try {
            fromHost = new URL(url).getHost().toLowerCase();
          } catch (final MalformedURLException e) {
            fromHost = null;
          }
        } else {
          fromHost = null;
        }
        for (int i = 0; count < maxOutlinks && i < outlinks.length; i++) {
          String toUrl = outlinks[i].getToUrl();
          try {
            toUrl = normalizers.normalize(toUrl, URLNormalizers.SCOPE_OUTLINK);
            toUrl = filters.filter(toUrl);
          } catch (MalformedURLException e2) {
            continue;
          } catch (URLFilterException e) {
            continue;
          }
          if (toUrl == null) {
            continue;
          }
          Utf8 utf8ToUrl = new Utf8(toUrl);
          if (page.getFromOutlinks(utf8ToUrl) != null) {
            // skip duplicate outlinks
            continue;
          }
          String toHost;
          if (ignoreExternalLinks) {
            try {
              toHost = new URL(toUrl).getHost().toLowerCase();
            } catch (final MalformedURLException e) {
              toHost = null;
            }
            if (toHost == null || !toHost.equals(fromHost)) { // external links
              continue; // skip it
            }
          }

          page.putToOutlinks(utf8ToUrl, new Utf8(outlinks[i].getAnchor()));
        }
        Utf8 fetchMark = Mark.FETCH_MARK.checkMark(page);
        if (fetchMark != null) {
          Mark.PARSE_MARK.putMark(page, fetchMark);
        }
      }
    }
  }
}
