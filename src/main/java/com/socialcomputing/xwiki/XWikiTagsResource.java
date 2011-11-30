/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import com.socialcomputing.xwiki.rest.model.jaxb.Tag;
import com.socialcomputing.xwiki.rest.model.jaxb.Tags;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * @version $Id$
 */
@Component
@Named("com.socialcomputing.xwiki.XWikiTagsResource")
@Path("/wikis/{wikiName}/sc/tags")
@Singleton
public class XWikiTagsResource extends XWikiResource
{
    @Inject
    private Execution execution;
    
    @Inject
    @Named("currentmixed")
    private DocumentReferenceResolver<String> resolver;

    @GET
    public Tags getTags(@QueryParam("space") String space, @PathParam("wikiName") String wiki) throws XWikiException,
        QueryException
    {
        XWikiContext xcontext = getXWikiContext();
        String database = xcontext.getDatabase();

        try {
            xcontext.setDatabase(wiki);

            String query =
                "select elements(prop.list), doc.fullName, doc.title "
                    + "from XWikiDocument doc, BaseObject as obj, DBStringListProperty as prop "
                    + "where obj.className='XWiki.TagClass' and obj.id=prop.id.id and prop.id.name='tags' "
                    + "and doc.fullName = obj.name";
            if (!StringUtils.isEmpty(space)) {
                // TODO: escape!
                query = query + " and doc.space = " + space;
            }
            List<Object[]> tags = queryManager.createQuery(query, Query.HQL).execute();

            ObjectFactory factory = new ObjectFactory();
            Tags resultTags = factory.createTags();

            Map<String, Tag> uniqueTags = new HashMap<String, Tag>();
            for (Object[] taggedPage : tags) {
                Tag uniqueTag = uniqueTags.get((String) taggedPage[0]);
                if (uniqueTag == null) {
                    uniqueTag = factory.createTag();
                    uniqueTags.put((String) taggedPage[0], uniqueTag);
                    uniqueTag.setName((String) taggedPage[0]);
                    resultTags.getTags().add(uniqueTag);
                }

                Page page = factory.createPage();
                page.setName((String) taggedPage[1]);
                page.setTitle((String) taggedPage[2]);
                page.setUrl(xcontext.getWiki().getURL(resolver.resolve((String) taggedPage[1]), "view", xcontext));
                uniqueTag.getPages().add(page);
            }

            return resultTags;
        } finally {
            xcontext.setDatabase(database);
        }
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}
