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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.PriorityQueue;

import com.globalsight.ling.tm2.lucene.LuceneUtil;

/**
 * Class used to markup highlighted terms found in the best sections
 * of a text, using configurable {@link Fragmenter}, {@link Scorer},
 * {@link Formatter} and tokenizers.
 * @author mark@searcharea.co.uk
 */
public class Highlighter
{
    static public final int DEFAULT_MAX_DOC_BYTES_TO_ANALYZE = 50*1024;

    private int maxDocBytesToAnalyze = DEFAULT_MAX_DOC_BYTES_TO_ANALYZE;
    private Formatter formatter;
    private Fragmenter textFragmenter = new SimpleFragmenter();
    private Scorer fragmentScorer = null;

    public Highlighter(Scorer fragmentScorer)
    {
        this(new SimpleHTMLFormatter(), fragmentScorer);
    }


    public Highlighter(Formatter formatter, Scorer fragmentScorer)
    {
        this.formatter = formatter;
        this.fragmentScorer = fragmentScorer;
    }


    /**
     * Highlights chosen terms in a text, extracting the most relevant
     * section.  The document text is analysed in chunks to record hit
     * statistics across the document. After accumulating stats, the
     * fragment with the highest score is returned.
     *
     * @param tokenStream a stream of tokens identified in the text
     * parameter, including offset information.
     *
     * This is typically produced by an analyzer re-parsing a document's
     * text. Some work may be done on retrieving TokenStreams more efficently
     * by adding support for storing original text position data in the Lucene
     * index but this support is not currently available (as of Lucene 1.4 rc2).
     *
     * @param text text to highlight terms in
     *
     * @return highlighted text fragment or null if no terms found
     */
    public final String getBestFragment(TokenStream tokenStream, String text)
        throws IOException
    {
        String[] results = getBestFragments(tokenStream, text, 1);

        if (results.length > 0)
        {
            return results[0];
        }

        return null;
    }

    /**
     * Highlights chosen terms in a text, extracting the most relevant sections.
     *
     * The document text is analysed in chunks to record hit
     * statistics across the document. After accumulating stats, the
     * fragments with the highest scores are returned as an array of
     * strings in order of score (contiguous fragments are merged into
     * one in their original order to improve readability)
     *
     * @param text text to highlight terms in
     * @param maxNumFragments the maximum number of fragments.
     *
     * @return highlighted text fragments (between 0 and
     * maxNumFragments number of fragments)
     */
    public final String[] getBestFragments(TokenStream tokenStream,
        String text, int maxNumFragments)
        throws IOException
    {
        maxNumFragments = Math.max(1, maxNumFragments); //sanity check

        TextFragment[] frag =
            getBestTextFragments(tokenStream, text, true, maxNumFragments);

        //Get text
        ArrayList fragTexts = new ArrayList();
        int n = 0;
        for (int i = 0; i < frag.length; i++)
        {
            if ((frag[i] != null) && (frag[i].getScore() > 0))
            {
                fragTexts.add(frag[i].toString());
            }
        }

        return (String[]) fragTexts.toArray(new String[0]);
    }


    /**
     * Highlights terms in the text, extracting the most relevant
     * sections and concatenating the chosen fragments with a
     * separator (typically "...").
     *
     * The document text is analysed in chunks to record hit
     * statistics across the document. After accumulating stats, the
     * fragments with the highest scores are returned in order as
     * "separator" delimited strings.
     *
     * @param text text to highlight terms in
     * @param maxNumFragments the maximum number of fragments.
     * @param separator the separator used to intersperse the document
     * fragments (typically "...")
     *
     * @return highlighted text
     */
    public final String getBestFragments(TokenStream tokenStream,
        String text, int maxNumFragments, String separator)
        throws IOException
    {
        String sections[] =
            getBestFragments(tokenStream, text, maxNumFragments);

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < sections.length; i++)
        {
            if (i > 0)
            {
                result.append(separator);
            }
            result.append(sections[i]);
        }

        return result.toString();
    }


    /**
     * Low level api to get the most relevant (formatted) sections of
     * the document.
     *
     * This method has been made public to allow visibility of score
     * information held in TextFragment objects.  Thanks to Jason
     * Calabrese for help in redefining the interface.
     * @param tokenStream
     * @param text
     * @param maxNumFragments
     * @param mergeContiguousFragments
     * @return
     * @throws IOException
     */
    public final TextFragment[] getBestTextFragments(TokenStream tokenStream,
        String text, boolean mergeContiguousFragments, int maxNumFragments)
        throws IOException
    {
        ArrayList docFrags = new ArrayList();
        StringBuffer newText = new StringBuffer();

        TextFragment currentFrag =
            new TextFragment(newText,newText.length(), docFrags.size());

        fragmentScorer.startFragment(currentFrag);
        docFrags.add(currentFrag);

        FragmentQueue fragQueue = new FragmentQueue(maxNumFragments);

        try
        {
            org.apache.lucene.analysis.Token token;
            String tokenText;
            int startOffset;
            int endOffset;
            int lastEndOffset = 0;
            textFragmenter.start(text);

            TokenGroup tokenGroup=new TokenGroup();

            while ((token = LuceneUtil.getNextToken(tokenStream)) != null)
            {
                if (tokenGroup.numTokens > 0 && tokenGroup.isDistinct(token))
                {
                    // the current token is distinct from previous tokens -
                    // markup the cached token group info
                    startOffset = tokenGroup.startOffset;
                    endOffset = tokenGroup.endOffset;
                    tokenText = text.substring(startOffset, endOffset);
                    String markedUpText =
                        formatter.highlightTerm(tokenText, tokenGroup);

                    // store any whitespace etc from between this and last group
                    if (startOffset > lastEndOffset)
                        newText.append(text.substring(lastEndOffset, startOffset));
                    newText.append(markedUpText);
                    lastEndOffset = endOffset;
                    tokenGroup.clear();

                    // check if current token marks the start of a new fragment
                    if (textFragmenter.isNewFragment(token))
                    {
                        currentFrag.setScore(fragmentScorer.getFragmentScore());

                        //record stats for a new fragment
                        currentFrag.textEndPos = newText.length();
                        currentFrag = new TextFragment(
                            newText, newText.length(), docFrags.size());
                        fragmentScorer.startFragment(currentFrag);
                        docFrags.add(currentFrag);
                    }
                }

                tokenGroup.addToken(token,fragmentScorer.getTokenScore(token));

                if (lastEndOffset > maxDocBytesToAnalyze)
                {
                    break;
                }
            }
            currentFrag.setScore(fragmentScorer.getFragmentScore());

            if (tokenGroup.numTokens > 0)
            {
                // flush the accumulated text (same code as in above loop)
                startOffset = tokenGroup.startOffset;
                endOffset = tokenGroup.endOffset;
                tokenText = text.substring(startOffset, endOffset);
                String markedUpText =
                    formatter.highlightTerm(tokenText, tokenGroup);

                // store any whitespace etc from between this and last group
                if (startOffset > lastEndOffset)
                {
                    newText.append(text.substring(lastEndOffset, startOffset));
                }
                newText.append(markedUpText);
                lastEndOffset = endOffset;
            }

            // append text after end of last token
            if (lastEndOffset < text.length())
            {
                newText.append(text.substring(lastEndOffset));
            }

            currentFrag.textEndPos = newText.length();

            // sort the most relevant sections of the text
            for (int i = 0, max = docFrags.size(); i < max; i++)
            {
                currentFrag = (TextFragment)docFrags.get(i);

                fragQueue.insertWithOverflow(currentFrag);
            }

            // return the most relevant fragments
            TextFragment result[] = new TextFragment[fragQueue.size()];
            for (int i = result.length - 1; i >= 0; i--)
            {
                result[i] = (TextFragment)fragQueue.pop();
            }

            // merge any contiguous fragments to improve readability
            if (mergeContiguousFragments)
            {
                mergeContiguousFragments(result);

                ArrayList fragTexts = new ArrayList();
                for (int i = 0; i < result.length; i++)
                {
                    if (result[i] != null && result[i].getScore() > 0)
                    {
                        fragTexts.add(result[i]);
                    }
                }

                result = (TextFragment[])fragTexts.toArray(new TextFragment[0]);
            }

            return result;
        }
        finally
        {
            if (tokenStream != null)
            {
                try
                {
                    tokenStream.close();
                }
                catch (Exception e)
                {
                }
            }
        }
    }


    /** Improves readability of a score-sorted list of TextFragments
     * by merging any fragments that were contiguous in the original
     * text into one larger fragment with the correct order.  This
     * will leave a "null" in the array entry for the lesser scored
     * fragment.
     *
     * @param frag An array of document fragments in descending score
     */
    private void mergeContiguousFragments(TextFragment[] frag)
    {
        boolean mergingStillBeingDone;

        if (frag.length > 1)
            do
            {
                mergingStillBeingDone = false; //initialise loop control flag
                //for each fragment, scan other frags looking for contiguous blocks
                for (int i = 0; i < frag.length; i++)
                {
                    if (frag[i] == null)
                    {
                        continue;
                    }
                    //merge any contiguous blocks
                    for (int x = 0; x < frag.length; x++)
                    {
                        if (frag[x] == null)
                        {
                            continue;
                        }
                        if (frag[i] == null)
                        {
                            break;
                        }
                        TextFragment frag1 = null;
                        TextFragment frag2 = null;
                        int frag1Num = 0;
                        int frag2Num = 0;
                        int bestScoringFragNum;
                        int worstScoringFragNum;
                        //if blocks are contiguous....
                        if (frag[i].follows(frag[x]))
                        {
                            frag1 = frag[x];
                            frag1Num = x;
                            frag2 = frag[i];
                            frag2Num = i;
                        }
                        else if (frag[x].follows(frag[i]))
                        {
                            frag1 = frag[i];
                            frag1Num = i;
                            frag2 = frag[x];
                            frag2Num = x;
                        }
                        //merging required..
                        if (frag1 != null)
                        {
                            if (frag1.getScore() > frag2.getScore())
                            {
                                bestScoringFragNum = frag1Num;
                                worstScoringFragNum = frag2Num;
                            }
                            else
                            {
                                bestScoringFragNum = frag2Num;
                                worstScoringFragNum = frag1Num;
                            }
                            frag1.merge(frag2);
                            frag[worstScoringFragNum] = null;
                            mergingStillBeingDone = true;
                            frag[bestScoringFragNum] = frag1;
                        }
                    }
                }
            }
            while (mergingStillBeingDone);
    }


    /**
     * @return the maximum number of bytes to be tokenized per doc
     */
    public int getMaxDocBytesToAnalyze()
    {
        return maxDocBytesToAnalyze;
    }

    /**
     * @param byteCount the maximum number of bytes to be tokenized per doc
     * (This can improve performance with large documents)
     */
    public void setMaxDocBytesToAnalyze(int byteCount)
    {
        maxDocBytesToAnalyze = byteCount;
    }

    public Fragmenter getTextFragmenter()
    {
        return textFragmenter;
    }

    public void setTextFragmenter(Fragmenter fragmenter)
    {
        textFragmenter = fragmenter;
    }

    public Scorer getFragmentScorer()
    {
        return fragmentScorer;
    }

    public void setFragmentScorer(Scorer scorer)
    {
        fragmentScorer = scorer;
    }
}

class FragmentQueue<T> extends PriorityQueue<T>
{
    public FragmentQueue(int size)
    {
        super(size);
    }

    public final boolean lessThan(Object a, Object b)
    {
        TextFragment fragA = (TextFragment) a;
        TextFragment fragB = (TextFragment) b;
        if (fragA.getScore() == fragB.getScore())
            return fragA.fragNum > fragB.fragNum;
        else
            return fragA.getScore() < fragB.getScore();
    }
}
