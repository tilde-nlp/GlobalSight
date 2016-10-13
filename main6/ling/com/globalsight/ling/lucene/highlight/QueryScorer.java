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
package com.globalsight.ling.lucene.highlight;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.Query;

/**
 * {@link Scorer} implementation which scores text fragments by the
 * number of unique query terms found.  This class uses the {@link
 * QueryTermExtractor} class to process determine the query terms and
 * their boosts to be used.
 * @author mark@searcharea.co.uk
 */

//TODO: provide option to roll idf into the scoring equation by
//passing a IndexReader.  TODO: provide option to boost score of
//fragments near beginning of document based on fragment.getFragNum()
public class QueryScorer
    implements Scorer
{
    TextFragment currentTextFragment = null;
    HashSet uniqueTermsInFragment;
    float totalScore = 0;
    private HashMap termsToFind;

    /**
     * Constructor.
     *
     * @param query a Lucene query (ideally rewritten using query.rewrite
     * before being passed to this class and the searcher)
     */
    public QueryScorer(Query query)
    {
        this(QueryTermExtractor.getTerms(query));
    }

    public QueryScorer(WeightedTerm []weightedTerms)
    {
        termsToFind = new HashMap();

        for (int i = 0; i < weightedTerms.length; i++)
        {
            termsToFind.put(weightedTerms[i].term, weightedTerms[i]);
        }
    }

    public void startFragment(TextFragment newFragment)
    {
        uniqueTermsInFragment = new HashSet();
        currentTextFragment = newFragment;
        totalScore=0;
    }

    public float getTokenScore(Token token)
    {
        String termText=token.toString();

        WeightedTerm queryTerm = (WeightedTerm)termsToFind.get(termText);
        if (queryTerm == null)
        {
            //not a query term - return
            return 0;
        }
        //found a query term - is it unique in this fragment?
        if (!uniqueTermsInFragment.contains(termText))
        {
            totalScore += queryTerm.getWeight();
            uniqueTermsInFragment.add(termText);
        }

        return queryTerm.getWeight();
    }

    public float getFragmentScore()
    {
        return totalScore;
    }

    public void allFragmentsProcessed()
    {
        //this class has no special operations to perform at end of processing
    }
}
