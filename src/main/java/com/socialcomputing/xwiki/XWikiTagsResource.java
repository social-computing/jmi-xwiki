/*
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package com.socialcomputing.xwiki;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.rest.XWikiResource;

import com.socialcomputing.xwiki.rest.model.jaxb.ObjectFactory;
import com.socialcomputing.xwiki.rest.model.jaxb.Page;
import com.socialcomputing.xwiki.rest.model.jaxb.PageId;
import com.socialcomputing.xwiki.rest.model.jaxb.Tag;
import com.socialcomputing.xwiki.rest.model.jaxb.TagsResponse;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * Xwiki extended tags Rest service
 * <p>
 * This Rest service exposes the set of tags used on all the wiki pages. Each
 * tag returns the list of pages on which the tag is set
 * </p>
 * 
 * @version $Id$
 */
@Component
@Named("com.socialcomputing.xwiki.XWikiTagsResource")
@Path("/wikis/{wikiName}/sc/tags")
@Singleton
public class XWikiTagsResource extends XWikiResource {
    @Inject
    private Execution execution;

    @Inject
    @Named("currentmixed")
    private DocumentReferenceResolver<String> resolver;

    @GET
    public TagsResponse getTags(@QueryParam("space") String space, @PathParam("wikiName") String wiki)
            throws XWikiException, QueryException {

        // Get the xwiki context object as initialized for this request
        XWikiContext xcontext = getXWikiContext();

        // Store the current database before doing an operation
        String database = xcontext.getDatabase();

        try {
            xcontext.setDatabase(wiki);
            
            // HQL Query to get the list of available tags and the pages for each tag
            String query = "select elements(prop.list), doc.fullName, doc.title "
                    + "from XWikiDocument doc, BaseObject as obj, DBStringListProperty as prop "
                    + "where obj.className='XWiki.TagClass' and obj.id=prop.id.id and prop.id.name='tags' "
                    + "and doc.fullName = obj.name";
            if (!StringUtils.isEmpty(space)) {
                // TODO: escape!
                query = query + " and doc.space = " + space;
            }
            
            // Query the wiki database
            List<Object[]> tags = queryManager.createQuery(query, Query.HQL).execute();

        
            // Service response
            // Construct the response with JAXB
            ObjectFactory factory = new ObjectFactory();
            TagsResponse response = factory.createTagsResponse();
            
            Map<String, Tag> uniqueTags = new HashMap<String, Tag>();
            Map<String, Page> uniquePages = new HashMap<String, Page>();
            for (Object[] taggedPage : tags) {
                Tag uniqueTag = uniqueTags.get((String) taggedPage[0]);
                if (uniqueTag == null) {
                    uniqueTag = factory.createTag();
                    uniqueTag.setName((String) taggedPage[0]);
                    uniqueTags.put((String) taggedPage[0], uniqueTag);
                    response.getTags().add(uniqueTag);
                }
                
                Page uniquePage = uniquePages.get((String) taggedPage[1]);
                if (uniquePage == null) {
                    uniquePage = factory.createPage();
                    uniquePage.setId((String) taggedPage[1]);
                    uniquePage.setName((String) taggedPage[2]);
                    uniquePage.setUrl(xcontext.getWiki().getURL(resolver.resolve((String) taggedPage[1]), "view", xcontext));
                    response.getPages().add(uniquePage);
                }
                
                PageId pageId = factory.createPageId();
                pageId.setId(uniquePage.getId());
                uniqueTag.getPages().add(pageId);
            }
            return response;
        }
        finally {
            xcontext.setDatabase(database);
        }
    }

    private XWikiContext getXWikiContext() {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}