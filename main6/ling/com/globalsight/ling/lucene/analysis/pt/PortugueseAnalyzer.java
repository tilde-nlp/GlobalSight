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
package com.globalsight.ling.lucene.analysis.pt;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;

import com.globalsight.ling.lucene.analysis.snowball.SnowballAnalyzer;

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
public class PortugueseAnalyzer
    extends SnowballAnalyzer
{
    static private String[] s_stopwords = {
    "de", "a", "o", "que", "e", "do", "da", "em", "um", "para", "com",
    "n�o", "uma", "os", "no", "se", "na", "por", "mais", "as", "dos",
    "como", "mas", "ao", "ele", "das", "�", "seu", "sua", "ou", "quando",
    "muito", "nos", "j�", "eu", "tamb�m", "s�", "pelo", "pela", "at�",
    "isso", "ela", "entre", "depois", "sem", "mesmo", "aos", "seus",
    "quem", "nas", "me", "esse", "eles", "voc�", "essa", "num", "nem",
    "suas", "meu", "�s", "minha", "numa", "pelos", "elas", "qual", "n�s",
    "lhe", "deles", "essas", "esses", "pelas", "este", "dele",
    //  | �          from SER
    //  | foi        from SER
    //  | tem        from TER
    //  | ser        from SER
    //  | h�         from HAV
    //  | est�       from EST
    //  | era        from SER
    //  | ter        from TER
    //  | est�o      from EST
    //  | tinha      from TER
    //  | foram      from SER
    //  | t�m        from TER
    //  | havia      from HAV
    //  | seja       from SER
    //  | ser�       from SER
    //  | tenho      from TER
    //  | fosse      from SER

    // | other words. There are many contractions such as naquele = em+aquele,
    // | mo = me+o, but they are rare.
    // | Indefinite article plural forms are also rare.
    "tu", "te", "voc�s", "vos", "lhes", "meus", "minhas", "teu", "tua",
    "teus", "tuas", "nosso", "nossa", "nossos", "nossas", "dela", "delas",
    "esta", "estes", "estas", "aquele", "aquela", "aqueles", "aquelas",
    "isto", "aquilo",

    // | forms of estar, to be (not including the infinitive):
    "estou", "est�", "estamos", "est�o", "estive", "esteve", "estivemos",
    "estiveram", "estava", "est�vamos", "estavam", "estivera", "estiv�ramos",
    "esteja", "estejamos", "estejam", "estivesse", "estiv�ssemos",
    "estivessem", "estiver", "estivermos", "estiverem",

    // | forms of haver, to have (not including the infinitive):
    "hei", "h�", "havemos", "h�o", "houve", "houvemos", "houveram",
    "houvera", "houv�ramos", "haja", "hajamos", "hajam", "houvesse",
    "houv�ssemos", "houvessem", "houver", "houvermos", "houverem",
    "houverei", "houver�", "houveremos", "houver�o", "houveria",
    "houver�amos", "houveriam",

    // | forms of ser, to be (not including the infinitive):
    "sou", "somos", "s�o", "era", "�ramos", "eram", "fui", "foi",
    "fomos", "foram", "fora", "f�ramos", "seja", "sejamos", "sejam",
    "fosse", "f�ssemos", "fossem", "for", "formos", "forem", "serei",
    "ser�", "seremos", "ser�o", "seria", "ser�amos", "seriam",

    // | forms of ter, to have (not including the infinitive):
    "tenho", "tem", "temos", "t�m", "tinha", "t�nhamos", "tinham",
    "tive", "teve", "tivemos", "tiveram", "tivera", "tiv�ramos",
    "tenha", "tenhamos", "tenham", "tivesse", "tiv�ssemos",
    "tivessem", "tiver", "tivermos", "tiverem", "terei", "ter�",
    "teremos", "ter�o", "teria", "ter�amos", "teriam",
    };

    public PortugueseAnalyzer()
    {
        super("Portuguese", s_stopwords);
    }
}
