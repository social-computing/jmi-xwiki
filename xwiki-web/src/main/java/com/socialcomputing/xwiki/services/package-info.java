@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(type=DateTime.class,
        value=DateTimeAdapter.class)
})
package com.socialcomputing.xwiki.services;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import org.joda.time.DateTime;

import com.socialcomputing.xwiki.utils.jaxb.DateTimeAdapter;
