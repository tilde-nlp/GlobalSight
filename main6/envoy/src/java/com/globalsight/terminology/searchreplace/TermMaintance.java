/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.terminology.searchreplace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.terminology.util.SqlUtil;

public class TermMaintance extends TbMaintance
{
    static private final Logger CATEGORY = Logger
            .getLogger(TermMaintance.class);

    public TermMaintance(SearchReplaceParams rp, Termbase m_termbase)
    {
        super(rp, m_termbase);
    }

    @Override
    public void replace(long levelId, String oldFieldText, String replaceText)
            throws Exception
    {
        TbTerm tt = HibernateUtil.get(TbTerm.class, levelId);
        String xml = replaceField(tt.getXml(), oldFieldText, replaceText);
        tt.setXml(xml);
        HibernateUtil.save(tt);
    }

    @Override
    public ArrayList search()
    {
        CATEGORY.info("Begin term-level search in termbase "
                + m_termbase.getName() + " for " + rp.getSearchText());

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Search params: " + rp.toString());
        }

        ArrayList array = new ArrayList();
        String language = rp.getLanguage();

        m_termbase.addReader();

        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append("from TbTerm t where t.tbLanguage.concept.termbase.id=");
            hql.append(m_termbase.getId());
            hql.append(" and t.tbLanguage.name='");
            hql.append(SqlUtil.quote(language)).append("'");
            hql.append(" and t.xml!=''");
            List list = HibernateUtil.search(hql.toString());

            Iterator<TbTerm> ite = list.iterator();

            while (ite.hasNext())
            {
                TbTerm term = ite.next();

                if (entryIsLocked(term.getTbLanguage().getConcept().getId()))
                {
                    String message = "entry "
                            + term.getTbLanguage().getConcept().getId()
                            + " is locked, ignoring...";
                    CATEGORY.info(message);
                }
                else
                {
                    searchField(array, term.getXml(), term.getTbLanguage()
                            .getConcept().getId(), term.getId());
                }

            }
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
        finally
        {
            m_termbase.releaseReader();
        }

        CATEGORY.info("End term-level search in termbase "
                + m_termbase.getName());

        return array;
    }
}
