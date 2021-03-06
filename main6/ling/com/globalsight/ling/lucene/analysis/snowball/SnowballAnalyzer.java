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
package com.globalsight.ling.lucene.analysis.snowball;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.util.CharArraySet;

import com.globalsight.ling.lucene.analysis.snowball.snowball.*;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import java.io.Reader;
import java.util.Set;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter},
 * {@link LowerCaseFilter}, {@link StopFilter} and {@link SnowballFilter}.
 *
 * Available stemmers are listed in {@link net.sf.snowball.ext}.  The
 * name of a stemmer is the part of the class name before "Stemmer",
 * e.g., the stemmer in {@link EnglishStemmer} is named "English".
 */
public class SnowballAnalyzer
    extends Analyzer
{
    private String name;
    private CharArraySet stopSet;

    /** Builds the named analyzer with no stop words. */
    public SnowballAnalyzer(String name)
    {
        this.name = name;
    }

    /** Builds the named analyzer with the given stop words. */
    public SnowballAnalyzer(String name, String[] stopWords)
    {
        this(name);
        stopSet = StopFilter.makeStopSet(LuceneUtil.VERSION, stopWords);
    }

    /**
     * Constructs a {@link StandardTokenizer} filtered by a {@link
     * StandardFilter}, a {@link LowerCaseFilter} and a {@link
     * StopFilter}.
     */
    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
        Tokenizer t = new StandardTokenizer(LuceneUtil.VERSION, reader);

        StandardFilter f = new StandardFilter(LuceneUtil.VERSION, t);
        LowerCaseFilter lf = new LowerCaseFilter(LuceneUtil.VERSION, f);
        StopFilter sf = null;
        if (stopSet != null)
        {
            sf = new StopFilter(LuceneUtil.VERSION, lf, stopSet);
        }
        if (sf == null)
        {
            SnowballFilter gf = new SnowballFilter(lf, name);
            return new TokenStreamComponents(t, gf);
        }
        else
        {
            SnowballFilter gf = new SnowballFilter(sf, name);
            return new TokenStreamComponents(t, gf);
        }
    }
}
