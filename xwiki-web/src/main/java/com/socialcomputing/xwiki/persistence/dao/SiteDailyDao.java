/**
 * 
 */
package com.socialcomputing.xwiki.persistence.dao;

import org.joda.time.DateTime;

import com.socialcomputing.xwiki.persistence.model.SiteDaily;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 *
 */
public interface SiteDailyDao {
	
    void update(SiteDaily siteDaily);
    
    void create(SiteDaily siteDaily);
    
    SiteDaily findByURL(String url);
    
    SiteDaily findByURLAndDay(String url, DateTime day);
}
