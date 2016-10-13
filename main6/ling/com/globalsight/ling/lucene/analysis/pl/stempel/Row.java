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
package com.globalsight.ling.lucene.analysis.pl.stempel;

import java.util.*;
import java.io.*;

/**
 *  The Row class represents a row in a matrix representation of a trie.
 *
 * @author    Leo Galambos
 */
public class Row {
    TreeMap cells = new TreeMap();
    int uniformCnt = 0;
    int uniformSkip = 0;


    /**
     *  Construct a Row object from input carried in via the given input
     *  stream.
     *
     * @param  is               the input stream
     * @exception  IOException  if an I/O error occurs
     */
    public Row(DataInput is) throws IOException {
        for (int i = is.readInt(); i > 0; i--) {
            Character ch = new Character(is.readChar());
            Cell c = new Cell();
            c.cmd = is.readInt();
            c.cnt = is.readInt();
            c.ref = is.readInt();
            c.skip = is.readInt();
            cells.put(ch, c);
        }
    }


    /**
     *  The default constructor for the Row object.
     */
    public Row() { }


    /**
     *  Construct a Row using the cells of the given Row.
     *
     * @param  old  the Row to copy
     */
    public Row(Row old) {
        cells = old.cells;
    }


    /**
     *  Set the command in the Cell of the given Character to the given
     *  integer.
     *
     * @param  way  the Character defining the Cell
     * @param  cmd  the new command
     */
    public void setCmd(Character way, int cmd) {
        Cell c = at(way);
        if (c == null) {
            c = new Cell();
            c.cmd = cmd;
            cells.put(way, c);
        } else {
            c.cmd = cmd;
        }
        c.cnt = (cmd >= 0) ? 1 : 0;
    }


    /**
     *  Set the reference to the next row in the Cell of the given
     *  Character to the given integer.
     *
     * @param  way  the Character defining the Cell
     * @param  ref  The new ref value
     */
    public void setRef(Character way, int ref) {
        Cell c = at(way);
        if (c == null) {
            c = new Cell();
            c.ref = ref;
            cells.put(way, c);
        } else {
            c.ref = ref;
        }
    }


    /**
     *  Return the number of cells in use.
     *
     * @return    the number of cells in use
     */
    public int getCells() {
        Iterator i = cells.keySet().iterator();
        int size = 0;
        for (; i.hasNext(); ) {
            Character c = (Character) i.next();
            Cell e = at(c);
            if (e.cmd >= 0 || e.ref >= 0) {
                size++;
            }
        }
        return size;
    }


    /**
     *  Return the number of references (how many transitions) to other
     *  rows.
     *
     * @return    the number of references
     */
    public int getCellsPnt() {
        Iterator i = cells.keySet().iterator();
        int size = 0;
        for (; i.hasNext(); ) {
            Character c = (Character) i.next();
            Cell e = at(c);
            if (e.ref >= 0) {
                size++;
            }
        }
        return size;
    }


    /**
     *  Return the number of patch commands saved in this Row.
     *
     * @return    the number of patch commands
     */
    public int getCellsVal() {
        Iterator i = cells.keySet().iterator();
        int size = 0;
        for (; i.hasNext(); ) {
            Character c = (Character) i.next();
            Cell e = at(c);
            if (e.cmd >= 0) {
                size++;
            }
        }
        return size;
    }


    /**
     *  Return the command in the Cell associated with the given Character.
     *
     * @param  way  the Character associated with the Cell holding the
     *      desired command
     * @return      the command
     */
    public int getCmd(Character way) {
        Cell c = at(way);
        return (c == null) ? -1 : c.cmd;
    }


    /**
     *  Return the number of patch commands were in the Cell associated
     *  with the given Character before the Trie containing this Row was
     *  reduced.
     *
     * @param  way  the Character associated with the desired Cell
     * @return      the number of patch commands before reduction
     */
    public int getCnt(Character way) {
        Cell c = at(way);
        return (c == null) ? -1 : c.cnt;
    }


    /**
     *  Return the reference to the next Row in the Cell associated with
     *  the given Character.
     *
     * @param  way  the Character associated with the desired Cell
     * @return      the reference, or -1 if the Cell is <tt>null,/tt>
     */
    public int getRef(Character way) {
        Cell c = at(way);
        return (c == null) ? -1 : c.ref;
    }


    /**
     *  Write the contents of this Row to the given output stream.
     *
     * @param  os               the output stream
     * @exception  IOException  if an I/O error occurs
     */
    public void store(DataOutput os) throws IOException {
        os.writeInt(cells.size());
        Iterator i = cells.keySet().iterator();
        for (; i.hasNext(); ) {
            Character c = (Character) i.next();
            Cell e = at(c);
            if (e.cmd < 0 && e.ref < 0) {
                continue;
            }

            os.writeChar(c.charValue());
            os.writeInt(e.cmd);
            os.writeInt(e.cnt);
            os.writeInt(e.ref);
            os.writeInt(e.skip);
        }
    }


    /**
     *  Return the number of identical Cells (containing patch commands) in
     *  this Row.
     *
     * @param  eqSkip  when set to <tt>false</tt> the removed patch
     *      commands are considered
     * @return         the number of identical Cells, or -1 if there are
     *      (at least) two different cells
     */
    public int uniformCmd(boolean eqSkip) {
        Iterator i = cells.values().iterator();
        int ret = -1;
        uniformCnt = 1;
        uniformSkip = 0;
        for (; i.hasNext(); ) {
            Cell c = (Cell) i.next();
            if (c.ref >= 0) {
                return -1;
            }
            if (c.cmd >= 0) {
                if (ret < 0) {
                    ret = c.cmd;
                    uniformSkip = c.skip;
                } else if (ret == c.cmd) {
                    if (eqSkip) {
                        if (uniformSkip == c.skip) {
                            uniformCnt++;
                        } else {
                            return -1;
                        }
                    } else {
                        uniformCnt++;
                    }
                } else {
                    return -1;
                }
            }
        }
        return ret;
    }


    /**
     *  Write the contents of this Row to stdout.
     */
    public void print() {
        for (Iterator i = cells.keySet().iterator(); i.hasNext(); ) {
            Character ch = (Character) i.next();
            Cell c = at(ch);
            System.out.print("[" + ch + ":" + c + "]");
        }
        System.out.println();
    }


    /**
     *  Description of the Method
     *
     * @param  index  Description of the Parameter
     * @return        Description of the Return Value
     */
    Cell at(Character index) {
        return (Cell) cells.get(index);
    }
}
