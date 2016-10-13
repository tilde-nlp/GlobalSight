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
package com.globalsight.ling.lucene.analysis.th;
import java.io.*;

public class ThaiTokenizerTokenManager implements ThaiTokenizerConstants
{
private final int jjMoveStringLiteralDfa0_0()
{
   return jjMoveNfa_0(1, 0);
}
private final void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private final void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private final void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}
private final void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}
private final void jjCheckNAddStates(int start)
{
   jjCheckNAdd(jjnextStates[start]);
   jjCheckNAdd(jjnextStates[start + 1]);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffL, 0x0L, 0x0L
};
static final long[] jjbitVec1 = {
   0x1ff0000000000000L, 0xffffffffffffc000L, 0xffffffffL, 0x600000000000000L
};
static final long[] jjbitVec3 = {
   0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec4 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffL, 0x0L
};
static final long[] jjbitVec5 = {
   0xffffffffffffffffL, 0xffffffffffffffffL, 0x0L, 0x0L
};
static final long[] jjbitVec6 = {
   0x3fffffffffffL, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec7 = {
   0x1600L, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec8 = {
   0x0L, 0xffc000000000L, 0x0L, 0xffc000000000L
};
static final long[] jjbitVec9 = {
   0x0L, 0x3ff00000000L, 0x0L, 0x3ff000000000000L
};
static final long[] jjbitVec10 = {
   0x0L, 0xffc000000000L, 0x0L, 0xff8000000000L
};
static final long[] jjbitVec11 = {
   0x0L, 0xffc000000000L, 0x0L, 0x0L
};
static final long[] jjbitVec12 = {
   0x0L, 0x3ff0000L, 0x0L, 0x3ff0000L
};
static final long[] jjbitVec13 = {
   0x0L, 0x3ffL, 0x0L, 0x0L
};
static final long[] jjbitVec14 = {
   0xfffffffeL, 0x0L, 0x0L, 0x0L
};
static final long[] jjbitVec15 = {
   0x0L, 0x0L, 0x0L, 0xff7fffffff7fffffL
};
private final int jjMoveNfa_0(int startState, int curPos)
{
   int[] nextStates;
   int startsAt = 0;
   jjnewStateCnt = 74;
   int i = 1;
   jjstateSet[0] = startState;
   int j, kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 3)
                        kind = 3;
                     jjCheckNAddStates(0, 17);
                  }
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(18, 23);
                  break;
               case 2:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(18, 23);
                  break;
               case 3:
               case 40:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 4:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAdd(5);
                  break;
               case 5:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(5);
                  break;
               case 6:
               case 49:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(6, 7);
                  break;
               case 7:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAdd(8);
                  break;
               case 8:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(8, 9);
                  break;
               case 9:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAddTwoStates(10, 11);
                  break;
               case 10:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(10, 11);
                  break;
               case 11:
               case 12:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(7, 12);
                  break;
               case 13:
               case 62:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(13, 14);
                  break;
               case 14:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAdd(15);
                  break;
               case 15:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(15, 16);
                  break;
               case 16:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 17:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 18:
               case 19:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(19, 20);
                  break;
               case 20:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAdd(21);
                  break;
               case 21:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(16, 21);
                  break;
               case 22:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAddStates(0, 17);
                  break;
               case 23:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAdd(23);
                  break;
               case 24:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(24, 26);
                  break;
               case 25:
                  if ((0x600000000000L & l) != 0L)
                     jjCheckNAdd(26);
                  break;
               case 26:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(27, 29);
                  break;
               case 28:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 29:
                  if ((0x600000000000L & l) != 0L)
                     jjCheckNAdd(30);
                  break;
               case 30:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  jjCheckNAddTwoStates(29, 30);
                  break;
               case 31:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(31, 32);
                  break;
               case 32:
                  if (curChar == 46)
                     jjCheckNAdd(33);
                  break;
               case 33:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 8)
                     kind = 8;
                  jjCheckNAddTwoStates(32, 33);
                  break;
               case 34:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(34, 35);
                  break;
               case 35:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAddTwoStates(36, 37);
                  break;
               case 36:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(36, 37);
                  break;
               case 37:
               case 38:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(38);
                  break;
               case 39:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(39, 40);
                  break;
               case 41:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(41, 42);
                  break;
               case 42:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAddTwoStates(43, 44);
                  break;
               case 43:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(43, 44);
                  break;
               case 44:
               case 45:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(45, 46);
                  break;
               case 46:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAdd(47);
                  break;
               case 47:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(42, 47);
                  break;
               case 48:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(48, 49);
                  break;
               case 50:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(50, 51);
                  break;
               case 51:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAddTwoStates(52, 53);
                  break;
               case 52:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(52, 53);
                  break;
               case 53:
               case 54:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(54, 55);
                  break;
               case 55:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAdd(56);
                  break;
               case 56:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(56, 57);
                  break;
               case 57:
                  if ((0xf00000000000L & l) != 0L)
                     jjCheckNAddTwoStates(58, 59);
                  break;
               case 58:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(58, 59);
                  break;
               case 59:
               case 60:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(55, 60);
                  break;
               case 61:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(61, 62);
                  break;
               case 65:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 66;
                  break;
               case 68:
                  if (curChar == 46)
                     jjCheckNAdd(69);
                  break;
               case 70:
                  if (curChar != 46)
                     break;
                  if (kind > 5)
                     kind = 5;
                  jjCheckNAdd(69);
                  break;
               case 72:
                  if (curChar == 38)
                     jjstateSet[jjnewStateCnt++] = 73;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddStates(30, 35);
                  if ((0x7fffffe07fffffeL & l) != 0L)
                  {
                     if (kind > 3)
                        kind = 3;
                     jjCheckNAddStates(0, 17);
                  }
                  break;
               case 3:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjAddStates(36, 37);
                  break;
               case 4:
                  if (curChar == 95)
                     jjCheckNAdd(5);
                  break;
               case 5:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(5);
                  break;
               case 6:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(6, 7);
                  break;
               case 7:
                  if (curChar == 95)
                     jjCheckNAdd(8);
                  break;
               case 8:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(8, 9);
                  break;
               case 9:
                  if (curChar == 95)
                     jjCheckNAddTwoStates(10, 11);
                  break;
               case 10:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(10, 11);
                  break;
               case 12:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(7, 12);
                  break;
               case 13:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjAddStates(38, 39);
                  break;
               case 14:
                  if (curChar == 95)
                     jjCheckNAdd(15);
                  break;
               case 15:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(15, 16);
                  break;
               case 16:
                  if (curChar == 95)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 17:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(17, 18);
                  break;
               case 19:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjAddStates(40, 41);
                  break;
               case 20:
                  if (curChar == 95)
                     jjCheckNAdd(21);
                  break;
               case 21:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(16, 21);
                  break;
               case 22:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAddStates(0, 17);
                  break;
               case 23:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAdd(23);
                  break;
               case 24:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddStates(24, 26);
                  break;
               case 25:
                  if (curChar == 95)
                     jjCheckNAdd(26);
                  break;
               case 26:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddStates(27, 29);
                  break;
               case 27:
                  if (curChar == 64)
                     jjCheckNAdd(28);
                  break;
               case 28:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 30:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 7)
                     kind = 7;
                  jjCheckNAddTwoStates(29, 30);
                  break;
               case 31:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(31, 32);
                  break;
               case 33:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 8)
                     kind = 8;
                  jjCheckNAddTwoStates(32, 33);
                  break;
               case 34:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(34, 35);
                  break;
               case 35:
                  if (curChar == 95)
                     jjCheckNAddTwoStates(36, 37);
                  break;
               case 36:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(36, 37);
                  break;
               case 38:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjstateSet[jjnewStateCnt++] = 38;
                  break;
               case 39:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(39, 40);
                  break;
               case 41:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(41, 42);
                  break;
               case 42:
                  if (curChar == 95)
                     jjCheckNAddTwoStates(43, 44);
                  break;
               case 43:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(43, 44);
                  break;
               case 45:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjAddStates(42, 43);
                  break;
               case 46:
                  if (curChar == 95)
                     jjCheckNAdd(47);
                  break;
               case 47:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(42, 47);
                  break;
               case 48:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(48, 49);
                  break;
               case 50:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(50, 51);
                  break;
               case 51:
                  if (curChar == 95)
                     jjCheckNAddTwoStates(52, 53);
                  break;
               case 52:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(52, 53);
                  break;
               case 54:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(54, 55);
                  break;
               case 55:
                  if (curChar == 95)
                     jjCheckNAdd(56);
                  break;
               case 56:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(56, 57);
                  break;
               case 57:
                  if (curChar == 95)
                     jjCheckNAddTwoStates(58, 59);
                  break;
               case 58:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(58, 59);
                  break;
               case 60:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(55, 60);
                  break;
               case 61:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(61, 62);
                  break;
               case 63:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddStates(30, 35);
                  break;
               case 64:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(64, 65);
                  break;
               case 66:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 4)
                     kind = 4;
                  jjCheckNAddTwoStates(65, 66);
                  break;
               case 67:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(67, 68);
                  break;
               case 69:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjAddStates(44, 45);
                  break;
               case 71:
                  if ((0x7fffffe07fffffeL & l) != 0L)
                     jjCheckNAddTwoStates(71, 72);
                  break;
               case 72:
                  if (curChar == 64)
                     jjCheckNAdd(73);
                  break;
               case 73:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 6)
                     kind = 6;
                  jjCheckNAdd(73);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         MatchLoop: do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if (jjCanMove_0(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 1)
                        kind = 1;
                     jjCheckNAdd(0);
                  }
                  if (jjCanMove_1(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 14)
                        kind = 14;
                  }
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(18, 23);
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                  {
                     if (kind > 3)
                        kind = 3;
                     jjCheckNAddStates(0, 17);
                  }
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(30, 35);
                  break;
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 1)
                     kind = 1;
                  jjCheckNAdd(0);
                  break;
               case 2:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(18, 23);
                  break;
               case 3:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 5:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 6:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(6, 7);
                  break;
               case 8:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(46, 47);
                  break;
               case 10:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(48, 49);
                  break;
               case 11:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(7, 12);
                  break;
               case 12:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(7, 12);
                  break;
               case 13:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(13, 14);
                  break;
               case 15:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(15, 16);
                  break;
               case 17:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(50, 51);
                  break;
               case 18:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(19, 20);
                  break;
               case 19:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(19, 20);
                  break;
               case 21:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(16, 21);
                  break;
               case 22:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAddStates(0, 17);
                  break;
               case 23:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 3)
                     kind = 3;
                  jjCheckNAdd(23);
                  break;
               case 24:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(24, 26);
                  break;
               case 26:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(27, 29);
                  break;
               case 28:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(28, 29);
                  break;
               case 30:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 7)
                     kind = 7;
                  jjCheckNAddTwoStates(29, 30);
                  break;
               case 31:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(31, 32);
                  break;
               case 33:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 8)
                     kind = 8;
                  jjCheckNAddTwoStates(32, 33);
                  break;
               case 34:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(34, 35);
                  break;
               case 36:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(52, 53);
                  break;
               case 37:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(38);
                  break;
               case 38:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAdd(38);
                  break;
               case 39:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(39, 40);
                  break;
               case 40:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(3, 4);
                  break;
               case 41:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(41, 42);
                  break;
               case 43:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(54, 55);
                  break;
               case 44:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(45, 46);
                  break;
               case 45:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(45, 46);
                  break;
               case 47:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(42, 47);
                  break;
               case 48:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(48, 49);
                  break;
               case 49:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(6, 7);
                  break;
               case 50:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(50, 51);
                  break;
               case 52:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(56, 57);
                  break;
               case 53:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(54, 55);
                  break;
               case 54:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(54, 55);
                  break;
               case 56:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(58, 59);
                  break;
               case 58:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(60, 61);
                  break;
               case 59:
                  if (!jjCanMove_2(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(55, 60);
                  break;
               case 60:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 9)
                     kind = 9;
                  jjCheckNAddTwoStates(55, 60);
                  break;
               case 61:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(61, 62);
                  break;
               case 62:
                  if (jjCanMove_2(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(13, 14);
                  break;
               case 63:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddStates(30, 35);
                  break;
               case 64:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(64, 65);
                  break;
               case 66:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 4)
                     kind = 4;
                  jjCheckNAddTwoStates(65, 66);
                  break;
               case 67:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(67, 68);
                  break;
               case 69:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjAddStates(44, 45);
                  break;
               case 71:
                  if (jjCanMove_3(hiByte, i1, i2, l1, l2))
                     jjCheckNAddTwoStates(71, 72);
                  break;
               case 73:
                  if (!jjCanMove_3(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 6)
                     kind = 6;
                  jjstateSet[jjnewStateCnt++] = 73;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 74 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   23, 24, 25, 27, 31, 32, 34, 35, 39, 40, 41, 42, 48, 49, 50, 51, 
   61, 62, 3, 4, 6, 7, 13, 14, 24, 25, 27, 25, 26, 27, 64, 65, 
   67, 68, 71, 72, 3, 4, 13, 14, 19, 20, 45, 46, 69, 70, 8, 9, 
   10, 11, 17, 18, 36, 37, 43, 44, 52, 53, 56, 57, 58, 59, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 14:
         return ((jjbitVec0[i2] & l2) != 0L);
      default : 
         return false;
   }
}
private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 48:
         return ((jjbitVec3[i2] & l2) != 0L);
      case 49:
         return ((jjbitVec4[i2] & l2) != 0L);
      case 51:
         return ((jjbitVec5[i2] & l2) != 0L);
      case 61:
         return ((jjbitVec6[i2] & l2) != 0L);
      default : 
         if ((jjbitVec1[i1] & l1) != 0L)
            return true;
         return false;
   }
}
private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 6:
         return ((jjbitVec9[i2] & l2) != 0L);
      case 11:
         return ((jjbitVec10[i2] & l2) != 0L);
      case 13:
         return ((jjbitVec11[i2] & l2) != 0L);
      case 14:
         return ((jjbitVec12[i2] & l2) != 0L);
      case 16:
         return ((jjbitVec13[i2] & l2) != 0L);
      default : 
         if ((jjbitVec7[i1] & l1) != 0L)
            if ((jjbitVec8[i2] & l2) == 0L)
               return false;
            else
            return true;
         return false;
   }
}
private static final boolean jjCanMove_3(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec15[i2] & l2) != 0L);
      default : 
         if ((jjbitVec14[i1] & l1) != 0L)
            return true;
         return false;
   }
}
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, null, null, null, null, null, 
null, null, null, null, };
public static final String[] lexStateNames = {
   "DEFAULT", 
};
static final long[] jjtoToken = {
   0x43fbL, 
};
static final long[] jjtoSkip = {
   0x10000L, 
};
private CharStream input_stream;
private final int[] jjrounds = new int[74];
private final int[] jjstateSet = new int[148];
protected char curChar;
public ThaiTokenizerTokenManager(CharStream stream)
{
   input_stream = stream;
}
public ThaiTokenizerTokenManager(CharStream stream, int lexState)
{
   this(stream);
   SwitchTo(lexState);
}
public void ReInit(CharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private final void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 74; i-- > 0;)
      jjrounds[i] = 0x80000000;
}
public void ReInit(CharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

private final Token jjFillToken()
{
   Token t = Token.newToken(jjmatchedKind);
   t.kind = jjmatchedKind;
   String im = jjstrLiteralImages[jjmatchedKind];
   t.image = (im == null) ? input_stream.GetImage() : im;
   t.beginLine = input_stream.getBeginLine();
   t.beginColumn = input_stream.getBeginColumn();
   t.endLine = input_stream.getEndLine();
   t.endColumn = input_stream.getEndColumn();
   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

public final Token getNextToken() 
{
  int kind;
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {   
   try   
   {     
      curChar = input_stream.BeginToken();
   }     
   catch(java.io.IOException e)
   {        
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedPos == 0 && jjmatchedKind > 16)
   {
      jjmatchedKind = 16;
   }
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

}
