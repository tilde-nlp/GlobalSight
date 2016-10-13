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
package com.globalsight.ling.lucene.analysis.pt_br;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Lucene" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Lucene", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

import com.globalsight.ling.lucene.analysis.WordlistLoader;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;

/**
 * Analyzer for brazilian language. Supports an external list of
 * stopwords (words that will not be indexed at all) and an external
 * list of exclusions (word that will not be stemmed, but indexed).
 *
 * @author    Jo�o Kramer
 */
public final class BrazilianAnalyzer
    extends Analyzer
{
    /**
     * List of typical brazilian stopwords.
     */
    private String[] BRAZILIAN_STOP_WORDS = {
    "a","ainda","alem","ambas","ambos","antes",
    "ao","aonde","aos","apos","aquele","aqueles",
    "as","assim","com","como","contra","contudo",
    "cuja","cujas","cujo","cujos","da","das","de",
    "dela","dele","deles","demais","depois","desde",
    "desta","deste","dispoe","dispoem","diversa",
    "diversas","diversos","do","dos","durante","e",
    "ela","elas","ele","eles","em","entao","entre",
    "essa","essas","esse","esses","esta","estas",
    "este","estes","ha","isso","isto","logo","mais",
    "mas","mediante","menos","mesma","mesmas","mesmo",
    "mesmos","na","nas","nao","nas","nem","nesse","neste",
    "nos","o","os","ou","outra","outras","outro","outros",
    "pelas","pelas","pelo","pelos","perante","pois","por",
    "porque","portanto","proprio","propios","quais","qual",
    "qualquer","quando","quanto","que","quem","quer","se",
    "seja","sem","sendo","seu","seus","sob","sobre","sua",
    "suas","tal","tambem","teu","teus","toda","todas","todo",
    "todos","tua","tuas","tudo","um","uma","umas","uns"};

    /**
     * Contains the stopwords used with the StopFilter.
     */
    private CharArraySet stoptable = LuceneUtil.newCharArraySet();

    /**
     * Contains words that should be indexed but not stemmed.
     */
    private CharArraySet excltable = LuceneUtil.newCharArraySet();

    /**
     * Builds an analyzer.
     */
    public BrazilianAnalyzer()
    {
        stoptable = StopFilter.makeStopSet(LuceneUtil.VERSION, BRAZILIAN_STOP_WORDS );
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public BrazilianAnalyzer( String[] stopwords )
    {
        stoptable = StopFilter.makeStopSet(LuceneUtil.VERSION, stopwords );
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public BrazilianAnalyzer( CharArraySet stopwords )
    {
        stoptable = stopwords;
    }

    /**
     * Builds an analyzer with the given stop words.
     */
    public BrazilianAnalyzer( File stopwords )
        throws IOException
    {
        stoptable = WordlistLoader.getWordSet( stopwords );
    }

    /**
     * Builds an exclusionlist from an array of Strings.
     */
    public void setStemExclusionTable( String[] exclusionlist )
    {
        excltable = StopFilter.makeStopSet(LuceneUtil.VERSION, exclusionlist );
    }

    /**
     * Builds an exclusionlist from a Hashtable.
     */
    public void setStemExclusionTable( CharArraySet exclusionlist )
    {
        excltable = exclusionlist;
    }

    /**
     * Builds an exclusionlist from the words contained in the given file.
     */
    public void setStemExclusionTable( File exclusionlist )
        throws IOException
    {
        excltable = WordlistLoader.getWordSet( exclusionlist );
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the
     * provided Reader.
     *
     * @return A TokenStream build from a StandardTokenizer filtered
     * with StandardFilter, StopFilter, GermanStemFilter and
     * LowerCaseFilter.
     */
    protected TokenStreamComponents createComponents(String fieldName,
            Reader reader)
    {
        Tokenizer t = new StandardTokenizer(LuceneUtil.VERSION, reader);
        
        StandardFilter f = new StandardFilter(LuceneUtil.VERSION, t);
        StopFilter ts = new StopFilter(LuceneUtil.VERSION, f, stoptable);
        BrazilianStemFilter bf = new BrazilianStemFilter( ts, excltable );
        LowerCaseFilter lf = new LowerCaseFilter(LuceneUtil.VERSION, bf);
        
        return new TokenStreamComponents(t, lf);
    }
}
