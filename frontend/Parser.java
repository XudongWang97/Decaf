//### This file created by BYACC 1.8(/Java extension  1.13)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//###           14 Sep 06  -- Keltin Leung-- ReduceListener support, eliminate underflow report in error recovery
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";






//#line 11 "Parser.y"
package decaf.frontend;

import decaf.tree.Tree;
import decaf.tree.Tree.*;
import decaf.error.*;
import java.util.*;
//#line 25 "Parser.java"
interface ReduceListener {
  public boolean onReduce(String rule);
}




public class Parser
             extends BaseParser
             implements ReduceListener
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

ReduceListener reduceListener = null;
void yyclearin ()       {yychar = (-1);}
void yyerrok ()         {yyerrflag=0;}
void addReduceListener(ReduceListener l) {
  reduceListener = l;}


//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//## **user defined:SemValue
String   yytext;//user variable to return contextual strings
SemValue yyval; //used to return semantic vals from action routines
SemValue yylval;//the 'lval' (result) I got from yylex()
SemValue valstk[] = new SemValue[YYSTACKSIZE];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
final void val_init()
{
  yyval=new SemValue();
  yylval=new SemValue();
  valptr=-1;
}
final void val_push(SemValue val)
{
  try {
    valptr++;
    valstk[valptr]=val;
  }
  catch (ArrayIndexOutOfBoundsException e) {
    int oldsize = valstk.length;
    int newsize = oldsize*2;
    SemValue[] newstack = new SemValue[newsize];
    System.arraycopy(valstk,0,newstack,0,oldsize);
    valstk = newstack;
    valstk[valptr]=val;
  }
}
final SemValue val_pop()
{
  return valstk[valptr--];
}
final void val_drop(int cnt)
{
  valptr -= cnt;
}
final SemValue val_peek(int relative)
{
  return valstk[valptr-relative];
}
//#### end semantic value section ####
public final static short VOID=257;
public final static short BOOL=258;
public final static short INT=259;
public final static short STRING=260;
public final static short CLASS=261;
public final static short COMPLEX=262;
public final static short NULL=263;
public final static short EXTENDS=264;
public final static short THIS=265;
public final static short WHILE=266;
public final static short FOR=267;
public final static short SUPER=268;
public final static short IF=269;
public final static short ELSE=270;
public final static short RETURN=271;
public final static short BREAK=272;
public final static short NEW=273;
public final static short PRINT=274;
public final static short READ_INTEGER=275;
public final static short READ_LINE=276;
public final static short DEFAULT=277;
public final static short CASE=278;
public final static short DOOD=279;
public final static short DO=280;
public final static short OD=281;
public final static short PRINTCOMP=282;
public final static short SCOPY=283;
public final static short DCOPY=284;
public final static short LITERAL=285;
public final static short IDENTIFIER=286;
public final static short AND=287;
public final static short OR=288;
public final static short STATIC=289;
public final static short INSTANCEOF=290;
public final static short LESS_EQUAL=291;
public final static short GREATER_EQUAL=292;
public final static short EQUAL=293;
public final static short NOT_EQUAL=294;
public final static short UMINUS=295;
public final static short EMPTY=296;
public final static short $nonassoc=297;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    3,    4,    5,    5,    5,    5,    5,
    5,    5,    2,    6,    6,    7,    7,    7,    9,    9,
   10,   10,    8,    8,   11,   12,   12,   13,   13,   13,
   13,   13,   13,   13,   13,   13,   13,   13,   14,   14,
   14,   26,   26,   23,   23,   25,   24,   24,   24,   24,
   24,   24,   24,   24,   24,   24,   24,   24,   24,   24,
   24,   24,   24,   24,   24,   24,   24,   24,   24,   24,
   24,   24,   24,   24,   24,   24,   24,   24,   24,   29,
   29,   31,   30,   28,   28,   27,   27,   32,   32,   18,
   19,   33,   33,   34,   16,   17,   22,   15,   35,   35,
   20,   20,   21,
};
final static short yylen[] = {                            2,
    1,    2,    1,    2,    2,    1,    1,    1,    1,    2,
    3,    1,    6,    2,    0,    2,    2,    0,    1,    0,
    3,    1,    7,    6,    3,    2,    0,    1,    2,    1,
    1,    1,    2,    2,    2,    2,    2,    1,    3,    1,
    0,    2,    0,    2,    4,    5,    1,    1,    1,    3,
    3,    3,    3,    3,    3,    3,    3,    3,    3,    3,
    3,    3,    3,    2,    2,    2,    2,    2,    3,    3,
    1,    1,    4,    5,    6,    5,    8,    4,    4,    2,
    0,    4,    4,    1,    1,    1,    0,    3,    1,    4,
    3,    3,    1,    3,    5,    9,    1,    6,    2,    0,
    2,    1,    4,
};
final static short yydefred[] = {                         0,
    0,    0,    0,    3,    0,    2,    0,    0,   14,   18,
    0,    7,    8,    6,    9,    0,   12,    0,   13,   16,
    0,    0,   17,   10,    0,    4,    0,    0,    0,    0,
   11,    0,   22,    0,    0,    0,    0,    5,    0,    0,
    0,   27,   24,   21,   23,    0,   85,   71,    0,    0,
   72,    0,    0,   97,    0,    0,    0,    0,    0,    0,
    0,    0,    0,   84,    0,    0,    0,    0,    0,    0,
    0,   25,   28,   38,   26,    0,   30,   31,   32,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   49,    0,
    0,    0,   47,    0,   48,    0,    0,    0,    0,    0,
    0,    0,    0,   93,    0,    0,    0,    0,    0,   66,
   68,   67,    0,    0,    0,   29,   33,   34,   35,   36,
   37,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   42,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   69,   70,    0,    0,    0,
   91,    0,    0,    0,    0,    0,   63,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   73,    0,    0,  103,
    0,   94,   92,   90,   79,   78,    0,    0,   45,    0,
    0,   95,    0,    0,   74,    0,   81,    0,   76,   46,
    0,    0,   98,    0,   75,    0,   99,    0,    0,    0,
   80,    0,    0,    0,   77,   96,    0,    0,   83,   82,
};
final static short yydgoto[] = {                          2,
    3,    4,   73,   21,   34,    8,   11,   23,   35,   36,
   74,   46,   75,   76,   77,   78,   79,   80,   81,   82,
   83,   84,   93,   86,   95,   88,  190,   89,  204,  210,
  211,  145,  103,  104,  203,
};
final static short yysindex[] = {                      -242,
 -261,    0, -242,    0, -229,    0, -213,  -49,    0,    0,
  231,    0,    0,    0,    0, -210,    0, -207,    0,    0,
   19,  -90,    0,    0,  -86,    0,   37,  -11,   51, -207,
    0, -207,    0,  -85,   53,   52,   56,    0,  -25, -207,
  -25,    0,    0,    0,    0,    1,    0,    0,   66,   67,
    0,   68,  322,    0,  297,   70,   73,   74,   75,  322,
   77,   79,   80,    0,   82,  322,  322,  322,  322,  322,
   76,    0,    0,    0,    0,   64,    0,    0,    0,   69,
   71,   78,   84,   85,   86,  807,    0, -161,    0,  322,
  322,  322,    0,  807,    0,   87,   38,  322,   91,   95,
  322,  439, -218,    0,  322,  322,  322,  322,  -35,    0,
    0,    0,  -35, -148,  469,    0,    0,    0,    0,    0,
    0,  322,  322,  322,  322,  322,  322,  322,  322,  322,
  322,  322,  322,  322,  322,    0,  322,  105,  480,   93,
  491,  109,  106,  807,  -24,    0,    0,  502,   35,  322,
    0,   -1,  532,  572,  593,  112,    0,  807,  939,  913,
    2,    2,  946,  946,  -19,  -19,  -35,  -35,  -35,    2,
    2,  755,  322,   35,  322,   35,    0,  785,  322,    0,
   31,    0,    0,    0,    0,    0, -130,  322,    0,  116,
  120,    0,  678, -104,    0,  807,    0,  126,    0,    0,
  322,   35,    0, -247,    0,  127,    0,  111,  113,   47,
    0,   35,  322,  322,    0,    0,  846,  857,    0,    0,
};
final static short yyrindex[] = {                         0,
    0,    0,  173,    0,   59,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,  115,    0,    0,  134,
    0,  134,    0,    0,    0,  138,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  -55,    0,    0,    0,    0,
    0,    0,  -30,    0,    0,    0,    0,    0,    0, -102,
    0,    0,    0,    0,    0, -102, -102, -102, -102, -102,
 -102,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  883,    0,  418,    0,    0, -102,
  -55, -102,    0,  131,    0,    0,    0, -102,    0,    0,
 -102,    0,    0,    0, -102, -102, -102, -102,  118,    0,
    0,    0,  144,    0,    0,    0,    0,    0,    0,    0,
    0, -102, -102, -102, -102, -102, -102, -102, -102, -102,
 -102, -102, -102, -102, -102,    0, -102,   42,    0,    0,
    0,    0, -102,   16,    0,    0,    0,    0,  -55, -102,
    0,    0,    0,    0,    0,    0,    0,  -26,   90,  151,
  777,  797,  587,  623,  966,  974,  356,  382,  409,  838,
  874,    0,  -32,  -55, -102,  -55,    0,    0, -102,    0,
    0,    0,    0,    0,    0,    0,    0, -102,    0,    0,
  152,    0,    0,  -33,    0,   18,    0,    0,    0,    0,
  -31,  -55,    0,    0,    0,    0,    0,    0,    0,    0,
    0,  -55, -102, -102,    0,    0,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
    0,  191,  186,   -8,    3,    0,    0,    0,  166,    0,
   25,    0, -107,  -83,    0,    0,    0,    0,    0,    0,
    0,    0,  450, 1181,  517,    0,    0,    4,    0,    0,
    0,  -92,    0,   55,    0,
};
final static int YYTABLESIZE=1395;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                        100,
   28,  100,  100,   41,   28,   28,  100,  140,   87,   41,
  136,  100,  152,   22,   39,   47,  180,  133,    1,  179,
   25,   33,  131,   33,    5,  100,  136,  132,  102,  208,
  100,   44,   39,   70,    7,   68,   69,   64,  133,  184,
   71,  182,  179,  131,  129,   66,  130,  136,  132,   12,
   13,   14,   15,   16,   17,  137,   89,   97,   88,   89,
  150,   88,  151,   43,   67,   45,  192,   70,  194,   68,
   69,  137,    9,   10,   71,   24,   30,   26,   44,   66,
  191,   31,   44,   44,   44,   44,   44,   44,   44,  100,
   32,  100,  137,   39,  207,   40,   41,   42,   67,   44,
   44,   44,   44,   44,  216,   90,   91,   92,   70,   98,
   68,   69,   99,  100,  101,   71,  105,  206,  106,  107,
   66,  108,  116,   42,  138,   72,  142,  117,  143,  118,
   61,  146,   44,   61,   44,  147,  119,  156,   70,   67,
   68,   69,  120,  121,  173,   71,  122,   61,   61,  177,
   66,  175,  188,  197,   64,  198,  200,   42,   64,   64,
   64,   64,   64,  179,   64,  202,  205,  212,  213,   67,
  214,  215,    1,    5,   20,   64,   64,   64,   19,   64,
   65,   15,   61,   43,   65,   65,   65,   65,   65,  101,
   65,   62,   86,    6,   62,   27,   20,   37,   31,   29,
   38,   65,   65,   65,  183,   65,    0,  209,   62,   62,
   64,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  100,  100,  100,  100,  100,  100,  100,
   43,  100,  100,  100,  100,  100,   65,  100,  100,  100,
  100,  100,  100,   62,  100,  100,  100,  100,  100,  100,
  100,  100,  100,   43,   43,   43,  100,   12,   13,   14,
   15,   16,   17,   47,    0,   48,   49,   50,   51,   52,
    0,   53,   54,   55,   56,   57,   58,    0,   59,    0,
   60,    0,   61,   62,   63,   64,    0,    0,    0,    0,
   65,   12,   13,   14,   15,   16,   17,   47,    0,   48,
   49,   50,   51,   52,    0,   53,   54,   55,   56,   57,
   58,    0,   59,    0,   60,    0,   61,   62,   63,   64,
    0,    0,    0,    0,   65,    0,    0,    0,   44,   44,
    0,    0,   44,   44,   44,   44,  114,    0,   47,    0,
   48,    0,    0,   51,    0,    0,    0,    0,   55,    0,
   57,   58,    0,   59,   70,   19,   68,   69,   62,   63,
   64,   71,    0,    0,    0,   65,   66,    0,   47,    0,
   48,    0,    0,   51,    0,    0,   61,   61,   55,    0,
   57,   58,    0,   59,    0,   67,    0,    0,   62,   63,
   64,    0,   52,    0,    0,   65,   52,   52,   52,   52,
   52,    0,   52,    0,   64,   64,    0,    0,   64,   64,
   64,   64,    0,   52,   52,   52,    0,   52,   53,    0,
    0,    0,   53,   53,   53,   53,   53,    0,   53,    0,
   65,   65,    0,    0,   65,   65,   65,   65,   62,   53,
   53,   53,    0,   53,    0,   54,    0,    0,   52,   54,
   54,   54,   54,   54,   48,   54,    0,    0,   40,   48,
   48,    0,   48,   48,   48,    0,   54,   54,   54,    0,
   54,    0,    0,    0,   53,  133,   40,   48,    0,   48,
  131,  129,    0,  130,  136,  132,    0,   12,   13,   14,
   15,   16,   17,    0,    0,   85,  149,    0,  135,    0,
  134,   54,    0,    0,    0,  133,    0,    0,   48,  157,
  131,  129,    0,  130,  136,  132,  133,    0,    0,   18,
  174,  131,  129,    0,  130,  136,  132,  133,  135,  137,
  134,  176,  131,  129,    0,  130,  136,  132,  133,  135,
   85,  134,  181,  131,  129,    0,  130,  136,  132,    0,
  135,    0,  134,   12,   13,   14,   15,   16,   17,  137,
    0,  135,   87,  134,    0,    0,    0,    0,  133,    0,
  137,    0,  185,  131,  129,    0,  130,  136,  132,    0,
    0,  137,   96,    0,   47,    0,   48,    0,    0,   51,
    0,  135,  137,  134,   55,    0,   57,   58,   85,   59,
    0,    0,    0,    0,   62,   63,   64,   87,  133,    0,
    0,   65,  186,  131,  129,    0,  130,  136,  132,    0,
    0,    0,  137,   85,    0,   85,    0,   55,    0,  133,
   55,  135,    0,  134,  131,  129,  187,  130,  136,  132,
    0,    0,   52,   52,   55,   55,   52,   52,   52,   52,
   85,   85,  135,    0,  134,    0,    0,    0,    0,    0,
    0,   85,  137,   56,    0,   87,   56,    0,   53,   53,
    0,    0,   53,   53,   53,   53,    0,    0,    0,   55,
   56,   56,    0,  137,    0,    0,    0,    0,    0,    0,
   87,    0,   87,    0,    0,   54,   54,    0,    0,   54,
   54,   54,   54,    0,   48,   48,    0,    0,   48,   48,
   48,   48,    0,    0,  133,   56,    0,   87,   87,  131,
  129,    0,  130,  136,  132,  123,  124,    0,   87,  125,
  126,  127,  128,    0,    0,    0,  201,  135,    0,  134,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  123,  124,    0,    0,  125,
  126,  127,  128,    0,    0,    0,  123,  124,  137,    0,
  125,  126,  127,  128,    0,    0,    0,  123,  124,    0,
    0,  125,  126,  127,  128,    0,    0,    0,  123,  124,
    0,  133,  125,  126,  127,  128,  131,  129,    0,  130,
  136,  132,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,  135,    0,  134,   59,  123,  124,
   59,  133,  125,  126,  127,  128,  131,  129,    0,  130,
  136,  132,    0,    0,   59,   59,    0,   60,    0,    0,
   60,    0,    0,  133,  135,  137,  134,  189,  131,  129,
    0,  130,  136,  132,   60,   60,    0,    0,  123,  124,
    0,    0,  125,  126,  127,  128,  135,    0,  134,   59,
    0,    0,    0,   55,   55,  137,    0,  195,   58,  123,
  124,   58,  133,  125,  126,  127,  128,  131,  129,   60,
  130,  136,  132,  133,    0,   58,   58,  137,  131,  129,
    0,  130,  136,  132,  219,  135,    0,  134,    0,   56,
   56,    0,    0,    0,   57,  220,  135,   57,  134,   47,
    0,    0,    0,    0,   47,   47,    0,   47,   47,   47,
   58,   57,   57,    0,    0,    0,  137,    0,    0,    0,
    0,    0,   47,    0,   47,    0,    0,  137,    0,  133,
    0,    0,    0,    0,  131,  129,    0,  130,  136,  132,
    0,    0,    0,    0,  123,  124,   57,    0,  125,  126,
  127,  128,  135,   47,  134,  133,    0,    0,    0,    0,
  131,  129,  133,  130,  136,  132,    0,  131,  129,    0,
  130,  136,  132,    0,    0,    0,    0,    0,  135,    0,
  134,    0,    0,  137,    0,  135,   50,  134,   50,   50,
   50,    0,    0,    0,   51,    0,   51,   51,   51,    0,
    0,    0,    0,   50,   50,   50,    0,   50,    0,  137,
    0,   51,   51,   51,    0,   51,  137,    0,    0,    0,
    0,  123,  124,    0,    0,  125,  126,  127,  128,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   50,    0,
    0,    0,    0,   59,   59,    0,   51,    0,    0,   59,
   59,  123,  124,    0,    0,  125,  126,  127,  128,    0,
    0,    0,    0,   60,   60,    0,    0,    0,    0,   60,
   60,    0,    0,  123,  124,    0,    0,  125,  126,  127,
  128,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   58,   58,    0,    0,    0,    0,
   58,   58,  123,  124,    0,    0,  125,  126,  127,  128,
    0,    0,    0,  123,  124,    0,    0,  125,  126,  127,
  128,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   57,   57,    0,    0,    0,    0,   57,   57,    0,   47,
   47,    0,    0,   47,   47,   47,   47,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  123,
    0,    0,    0,  125,  126,  127,  128,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  125,
  126,  127,  128,   94,    0,    0,  125,  126,    0,    0,
  102,    0,    0,    0,    0,    0,  109,  110,  111,  112,
  113,  115,   50,   50,    0,    0,   50,   50,   50,   50,
   51,   51,    0,    0,   51,   51,   51,   51,    0,    0,
  139,    0,  141,    0,    0,    0,    0,    0,  144,    0,
    0,  148,    0,    0,    0,  144,  153,  154,  155,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,  158,  159,  160,  161,  162,  163,  164,  165,
  166,  167,  168,  169,  170,  171,    0,  172,    0,    0,
    0,    0,    0,  178,    0,    0,    0,    0,    0,    0,
  102,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  144,    0,  193,    0,    0,    0,  196,
    0,    0,    0,    0,    0,    0,    0,    0,  199,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  217,  218,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         33,
   91,   35,   36,   59,   91,   91,   40,   91,   41,   41,
   46,   45,  105,   11,   41,  263,   41,   37,  261,   44,
   18,   30,   42,   32,  286,   59,   46,   47,   59,  277,
   64,   40,   59,   33,  264,   35,   36,  285,   37,   41,
   40,  149,   44,   42,   43,   45,   45,   46,   47,  257,
  258,  259,  260,  261,  262,   91,   41,   55,   41,   44,
  279,   44,  281,   39,   64,   41,  174,   33,  176,   35,
   36,   91,  286,  123,   40,  286,   40,   59,   37,   45,
  173,   93,   41,   42,   43,   44,   45,   46,   47,  123,
   40,  125,   91,   41,  202,   44,   41,  123,   64,   58,
   59,   60,   61,   62,  212,   40,   40,   40,   33,   40,
   35,   36,   40,   40,   40,   40,   40,  201,   40,   40,
   45,   40,   59,  123,  286,  125,   40,   59,   91,   59,
   41,   41,   91,   44,   93,   41,   59,  286,   33,   64,
   35,   36,   59,   59,   40,   40,   61,   58,   59,   41,
   45,   59,   41,  123,   37,  286,   41,  123,   41,   42,
   43,   44,   45,   44,   47,  270,   41,   41,   58,   64,
   58,  125,    0,   59,   41,   58,   59,   60,   41,   62,
   37,  123,   93,  286,   41,   42,   43,   44,   45,   59,
   47,   41,   41,    3,   44,  286,   11,   32,   93,  286,
  286,   58,   59,   60,  150,   62,   -1,  204,   58,   59,
   93,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  257,  258,  259,  260,  261,  262,  263,
  286,  265,  266,  267,  268,  269,   93,  271,  272,  273,
  274,  275,  276,   93,  278,  279,  280,  281,  282,  283,
  284,  285,  286,  286,  286,  286,  290,  257,  258,  259,
  260,  261,  262,  263,   -1,  265,  266,  267,  268,  269,
   -1,  271,  272,  273,  274,  275,  276,   -1,  278,   -1,
  280,   -1,  282,  283,  284,  285,   -1,   -1,   -1,   -1,
  290,  257,  258,  259,  260,  261,  262,  263,   -1,  265,
  266,  267,  268,  269,   -1,  271,  272,  273,  274,  275,
  276,   -1,  278,   -1,  280,   -1,  282,  283,  284,  285,
   -1,   -1,   -1,   -1,  290,   -1,   -1,   -1,  287,  288,
   -1,   -1,  291,  292,  293,  294,  261,   -1,  263,   -1,
  265,   -1,   -1,  268,   -1,   -1,   -1,   -1,  273,   -1,
  275,  276,   -1,  278,   33,  125,   35,   36,  283,  284,
  285,   40,   -1,   -1,   -1,  290,   45,   -1,  263,   -1,
  265,   -1,   -1,  268,   -1,   -1,  287,  288,  273,   -1,
  275,  276,   -1,  278,   -1,   64,   -1,   -1,  283,  284,
  285,   -1,   37,   -1,   -1,  290,   41,   42,   43,   44,
   45,   -1,   47,   -1,  287,  288,   -1,   -1,  291,  292,
  293,  294,   -1,   58,   59,   60,   -1,   62,   37,   -1,
   -1,   -1,   41,   42,   43,   44,   45,   -1,   47,   -1,
  287,  288,   -1,   -1,  291,  292,  293,  294,  288,   58,
   59,   60,   -1,   62,   -1,   37,   -1,   -1,   93,   41,
   42,   43,   44,   45,   37,   47,   -1,   -1,   41,   42,
   43,   -1,   45,   46,   47,   -1,   58,   59,   60,   -1,
   62,   -1,   -1,   -1,   93,   37,   59,   60,   -1,   62,
   42,   43,   -1,   45,   46,   47,   -1,  257,  258,  259,
  260,  261,  262,   -1,   -1,   46,   58,   -1,   60,   -1,
   62,   93,   -1,   -1,   -1,   37,   -1,   -1,   91,   41,
   42,   43,   -1,   45,   46,   47,   37,   -1,   -1,  289,
   41,   42,   43,   -1,   45,   46,   47,   37,   60,   91,
   62,   41,   42,   43,   -1,   45,   46,   47,   37,   60,
   91,   62,   41,   42,   43,   -1,   45,   46,   47,   -1,
   60,   -1,   62,  257,  258,  259,  260,  261,  262,   91,
   -1,   60,   46,   62,   -1,   -1,   -1,   -1,   37,   -1,
   91,   -1,   41,   42,   43,   -1,   45,   46,   47,   -1,
   -1,   91,  286,   -1,  263,   -1,  265,   -1,   -1,  268,
   -1,   60,   91,   62,  273,   -1,  275,  276,  149,  278,
   -1,   -1,   -1,   -1,  283,  284,  285,   91,   37,   -1,
   -1,  290,   41,   42,   43,   -1,   45,   46,   47,   -1,
   -1,   -1,   91,  174,   -1,  176,   -1,   41,   -1,   37,
   44,   60,   -1,   62,   42,   43,   44,   45,   46,   47,
   -1,   -1,  287,  288,   58,   59,  291,  292,  293,  294,
  201,  202,   60,   -1,   62,   -1,   -1,   -1,   -1,   -1,
   -1,  212,   91,   41,   -1,  149,   44,   -1,  287,  288,
   -1,   -1,  291,  292,  293,  294,   -1,   -1,   -1,   93,
   58,   59,   -1,   91,   -1,   -1,   -1,   -1,   -1,   -1,
  174,   -1,  176,   -1,   -1,  287,  288,   -1,   -1,  291,
  292,  293,  294,   -1,  287,  288,   -1,   -1,  291,  292,
  293,  294,   -1,   -1,   37,   93,   -1,  201,  202,   42,
   43,   -1,   45,   46,   47,  287,  288,   -1,  212,  291,
  292,  293,  294,   -1,   -1,   -1,   59,   60,   -1,   62,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  287,  288,   -1,   -1,  291,
  292,  293,  294,   -1,   -1,   -1,  287,  288,   91,   -1,
  291,  292,  293,  294,   -1,   -1,   -1,  287,  288,   -1,
   -1,  291,  292,  293,  294,   -1,   -1,   -1,  287,  288,
   -1,   37,  291,  292,  293,  294,   42,   43,   -1,   45,
   46,   47,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   60,   -1,   62,   41,  287,  288,
   44,   37,  291,  292,  293,  294,   42,   43,   -1,   45,
   46,   47,   -1,   -1,   58,   59,   -1,   41,   -1,   -1,
   44,   -1,   -1,   37,   60,   91,   62,   93,   42,   43,
   -1,   45,   46,   47,   58,   59,   -1,   -1,  287,  288,
   -1,   -1,  291,  292,  293,  294,   60,   -1,   62,   93,
   -1,   -1,   -1,  287,  288,   91,   -1,   93,   41,  287,
  288,   44,   37,  291,  292,  293,  294,   42,   43,   93,
   45,   46,   47,   37,   -1,   58,   59,   91,   42,   43,
   -1,   45,   46,   47,   59,   60,   -1,   62,   -1,  287,
  288,   -1,   -1,   -1,   41,   59,   60,   44,   62,   37,
   -1,   -1,   -1,   -1,   42,   43,   -1,   45,   46,   47,
   93,   58,   59,   -1,   -1,   -1,   91,   -1,   -1,   -1,
   -1,   -1,   60,   -1,   62,   -1,   -1,   91,   -1,   37,
   -1,   -1,   -1,   -1,   42,   43,   -1,   45,   46,   47,
   -1,   -1,   -1,   -1,  287,  288,   93,   -1,  291,  292,
  293,  294,   60,   91,   62,   37,   -1,   -1,   -1,   -1,
   42,   43,   37,   45,   46,   47,   -1,   42,   43,   -1,
   45,   46,   47,   -1,   -1,   -1,   -1,   -1,   60,   -1,
   62,   -1,   -1,   91,   -1,   60,   41,   62,   43,   44,
   45,   -1,   -1,   -1,   41,   -1,   43,   44,   45,   -1,
   -1,   -1,   -1,   58,   59,   60,   -1,   62,   -1,   91,
   -1,   58,   59,   60,   -1,   62,   91,   -1,   -1,   -1,
   -1,  287,  288,   -1,   -1,  291,  292,  293,  294,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   93,   -1,
   -1,   -1,   -1,  287,  288,   -1,   93,   -1,   -1,  293,
  294,  287,  288,   -1,   -1,  291,  292,  293,  294,   -1,
   -1,   -1,   -1,  287,  288,   -1,   -1,   -1,   -1,  293,
  294,   -1,   -1,  287,  288,   -1,   -1,  291,  292,  293,
  294,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,  287,  288,   -1,   -1,   -1,   -1,
  293,  294,  287,  288,   -1,   -1,  291,  292,  293,  294,
   -1,   -1,   -1,  287,  288,   -1,   -1,  291,  292,  293,
  294,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
  287,  288,   -1,   -1,   -1,   -1,  293,  294,   -1,  287,
  288,   -1,   -1,  291,  292,  293,  294,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  287,
   -1,   -1,   -1,  291,  292,  293,  294,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  291,
  292,  293,  294,   53,   -1,   -1,  291,  292,   -1,   -1,
   60,   -1,   -1,   -1,   -1,   -1,   66,   67,   68,   69,
   70,   71,  287,  288,   -1,   -1,  291,  292,  293,  294,
  287,  288,   -1,   -1,  291,  292,  293,  294,   -1,   -1,
   90,   -1,   92,   -1,   -1,   -1,   -1,   -1,   98,   -1,
   -1,  101,   -1,   -1,   -1,  105,  106,  107,  108,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,  122,  123,  124,  125,  126,  127,  128,  129,
  130,  131,  132,  133,  134,  135,   -1,  137,   -1,   -1,
   -1,   -1,   -1,  143,   -1,   -1,   -1,   -1,   -1,   -1,
  150,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  173,   -1,  175,   -1,   -1,   -1,  179,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,  188,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,  213,  214,
};
}
final static short YYFINAL=2;
final static short YYMAXTOKEN=297;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"'!'",null,"'#'","'$'","'%'",null,null,"'('","')'","'*'","'+'",
"','","'-'","'.'","'/'",null,null,null,null,null,null,null,null,null,null,"':'",
"';'","'<'","'='","'>'",null,"'@'",null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,"'['",null,"']'",null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,"'{'",null,"'}'",null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,"VOID","BOOL","INT","STRING",
"CLASS","COMPLEX","NULL","EXTENDS","THIS","WHILE","FOR","SUPER","IF","ELSE",
"RETURN","BREAK","NEW","PRINT","READ_INTEGER","READ_LINE","DEFAULT","CASE",
"DOOD","DO","OD","PRINTCOMP","SCOPY","DCOPY","LITERAL","IDENTIFIER","AND","OR",
"STATIC","INSTANCEOF","LESS_EQUAL","GREATER_EQUAL","EQUAL","NOT_EQUAL","UMINUS",
"EMPTY","$nonassoc",
};
final static String yyrule[] = {
"$accept : Program",
"Program : ClassList",
"ClassList : ClassList ClassDef",
"ClassList : ClassDef",
"VariableDef : Variable ';'",
"Variable : Type IDENTIFIER",
"Type : INT",
"Type : VOID",
"Type : BOOL",
"Type : STRING",
"Type : CLASS IDENTIFIER",
"Type : Type '[' ']'",
"Type : COMPLEX",
"ClassDef : CLASS IDENTIFIER ExtendsClause '{' FieldList '}'",
"ExtendsClause : EXTENDS IDENTIFIER",
"ExtendsClause :",
"FieldList : FieldList VariableDef",
"FieldList : FieldList FunctionDef",
"FieldList :",
"Formals : VariableList",
"Formals :",
"VariableList : VariableList ',' Variable",
"VariableList : Variable",
"FunctionDef : STATIC Type IDENTIFIER '(' Formals ')' StmtBlock",
"FunctionDef : Type IDENTIFIER '(' Formals ')' StmtBlock",
"StmtBlock : '{' StmtList '}'",
"StmtList : StmtList Stmt",
"StmtList :",
"Stmt : VariableDef",
"Stmt : SimpleStmt ';'",
"Stmt : IfStmt",
"Stmt : WhileStmt",
"Stmt : ForStmt",
"Stmt : PrintCompStmt ';'",
"Stmt : DoStmt ';'",
"Stmt : ReturnStmt ';'",
"Stmt : PrintStmt ';'",
"Stmt : BreakStmt ';'",
"Stmt : StmtBlock",
"SimpleStmt : LValue '=' Expr",
"SimpleStmt : Call",
"SimpleStmt :",
"Receiver : Expr '.'",
"Receiver :",
"LValue : Receiver IDENTIFIER",
"LValue : Expr '[' Expr ']'",
"Call : Receiver IDENTIFIER '(' Actuals ')'",
"Expr : LValue",
"Expr : Call",
"Expr : Constant",
"Expr : Expr '+' Expr",
"Expr : Expr '-' Expr",
"Expr : Expr '*' Expr",
"Expr : Expr '/' Expr",
"Expr : Expr '%' Expr",
"Expr : Expr EQUAL Expr",
"Expr : Expr NOT_EQUAL Expr",
"Expr : Expr '<' Expr",
"Expr : Expr '>' Expr",
"Expr : Expr LESS_EQUAL Expr",
"Expr : Expr GREATER_EQUAL Expr",
"Expr : Expr AND Expr",
"Expr : Expr OR Expr",
"Expr : '(' Expr ')'",
"Expr : '-' Expr",
"Expr : '!' Expr",
"Expr : '@' Expr",
"Expr : '$' Expr",
"Expr : '#' Expr",
"Expr : READ_INTEGER '(' ')'",
"Expr : READ_LINE '(' ')'",
"Expr : THIS",
"Expr : SUPER",
"Expr : NEW IDENTIFIER '(' ')'",
"Expr : NEW Type '[' Expr ']'",
"Expr : INSTANCEOF '(' Expr ',' IDENTIFIER ')'",
"Expr : '(' CLASS IDENTIFIER ')' Expr",
"Expr : CASE '(' Expr ')' '{' CaseStmtList DefaultStmt '}'",
"Expr : DCOPY '(' Expr ')'",
"Expr : SCOPY '(' Expr ')'",
"CaseStmtList : CaseStmtList CaseStmt",
"CaseStmtList :",
"CaseStmt : Constant ':' Expr ';'",
"DefaultStmt : DEFAULT ':' Expr ';'",
"Constant : LITERAL",
"Constant : NULL",
"Actuals : ExprList",
"Actuals :",
"ExprList : ExprList ',' Expr",
"ExprList : Expr",
"PrintCompStmt : PRINTCOMP '(' ExprList ')'",
"DoStmt : DO DoBranch OD",
"DoBranch : DoBranch DOOD DoSubStmt",
"DoBranch : DoSubStmt",
"DoSubStmt : Expr ':' Stmt",
"WhileStmt : WHILE '(' Expr ')' Stmt",
"ForStmt : FOR '(' SimpleStmt ';' Expr ';' SimpleStmt ')' Stmt",
"BreakStmt : BREAK",
"IfStmt : IF '(' Expr ')' Stmt ElseClause",
"ElseClause : ELSE Stmt",
"ElseClause :",
"ReturnStmt : RETURN Expr",
"ReturnStmt : RETURN",
"PrintStmt : PRINT '(' ExprList ')'",
};

//#line 513 "Parser.y"
    
	/**
	 * 打印当前归约所用的语法规则<br>
	 * 请勿修改。
	 */
    public boolean onReduce(String rule) {
		if (rule.startsWith("$$"))
			return false;
		else
			rule = rule.replaceAll(" \\$\\$\\d+", "");

   	    if (rule.endsWith(":"))
    	    System.out.println(rule + " <empty>");
   	    else
			System.out.println(rule);
		return false;
    }
    
    public void diagnose() {
		addReduceListener(this);
		yyparse();
	}
//#line 705 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    //if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      //if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        //if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        //if (yychar < 0)    //it it didn't work/error
        //  {
        //  yychar = 0;      //change it to default string (no -1!)
          //if (yydebug)
          //  yylexdebug(yystate,yychar);
        //  }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        //if (yydebug)
          //debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      //if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0 || valptr<0)   //check for under & overflow here
            {
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            //if (yydebug)
              //debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            //if (yydebug)
              //debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0 || valptr<0)   //check for under & overflow here
              {
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        //if (yydebug)
          //{
          //yys = null;
          //if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          //if (yys == null) yys = "illegal-symbol";
          //debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          //}
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    //if (yydebug)
      //debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    if (reduceListener == null || reduceListener.onReduce(yyrule[yyn])) // if intercepted!
      switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 56 "Parser.y"
{
						tree = new Tree.TopLevel(val_peek(0).clist, val_peek(0).loc);
					}
break;
case 2:
//#line 62 "Parser.y"
{
						yyval.clist.add(val_peek(0).cdef);
					}
break;
case 3:
//#line 66 "Parser.y"
{
                		yyval.clist = new ArrayList<Tree.ClassDef>();
                		yyval.clist.add(val_peek(0).cdef);
                	}
break;
case 5:
//#line 76 "Parser.y"
{
						yyval.vdef = new Tree.VarDef(val_peek(0).ident, val_peek(1).type, val_peek(0).loc);
					}
break;
case 6:
//#line 82 "Parser.y"
{
						yyval.type = new Tree.TypeIdent(Tree.INT, val_peek(0).loc);
					}
break;
case 7:
//#line 86 "Parser.y"
{
                		yyval.type = new Tree.TypeIdent(Tree.VOID, val_peek(0).loc);
                	}
break;
case 8:
//#line 90 "Parser.y"
{
                		yyval.type = new Tree.TypeIdent(Tree.BOOL, val_peek(0).loc);
                	}
break;
case 9:
//#line 94 "Parser.y"
{
                		yyval.type = new Tree.TypeIdent(Tree.STRING, val_peek(0).loc);
                	}
break;
case 10:
//#line 98 "Parser.y"
{
                		yyval.type = new Tree.TypeClass(val_peek(0).ident, val_peek(1).loc);
                	}
break;
case 11:
//#line 102 "Parser.y"
{
                		yyval.type = new Tree.TypeArray(val_peek(2).type, val_peek(2).loc);
                	}
break;
case 12:
//#line 106 "Parser.y"
{
                		yyval.type = new Tree.TypeIdent(Tree.COMPLEX, val_peek(0).loc);
                }
break;
case 13:
//#line 112 "Parser.y"
{
						yyval.cdef = new Tree.ClassDef(val_peek(4).ident, val_peek(3).ident, val_peek(1).flist, val_peek(5).loc);
					}
break;
case 14:
//#line 118 "Parser.y"
{
						yyval.ident = val_peek(0).ident;
					}
break;
case 15:
//#line 122 "Parser.y"
{
                		yyval = new SemValue();
                	}
break;
case 16:
//#line 128 "Parser.y"
{
						yyval.flist.add(val_peek(0).vdef);
					}
break;
case 17:
//#line 132 "Parser.y"
{
						yyval.flist.add(val_peek(0).fdef);
					}
break;
case 18:
//#line 136 "Parser.y"
{
                		yyval = new SemValue();
                		yyval.flist = new ArrayList<Tree>();
                	}
break;
case 20:
//#line 144 "Parser.y"
{
                		yyval = new SemValue();
                		yyval.vlist = new ArrayList<Tree.VarDef>(); 
                	}
break;
case 21:
//#line 151 "Parser.y"
{
						yyval.vlist.add(val_peek(0).vdef);
					}
break;
case 22:
//#line 155 "Parser.y"
{
                		yyval.vlist = new ArrayList<Tree.VarDef>();
						yyval.vlist.add(val_peek(0).vdef);
                	}
break;
case 23:
//#line 162 "Parser.y"
{
						yyval.fdef = new MethodDef(true, val_peek(4).ident, val_peek(5).type, val_peek(2).vlist, (Block) val_peek(0).stmt, val_peek(4).loc);
					}
break;
case 24:
//#line 166 "Parser.y"
{
						yyval.fdef = new MethodDef(false, val_peek(4).ident, val_peek(5).type, val_peek(2).vlist, (Block) val_peek(0).stmt, val_peek(4).loc);
					}
break;
case 25:
//#line 172 "Parser.y"
{
						yyval.stmt = new Block(val_peek(1).slist, val_peek(2).loc);
					}
break;
case 26:
//#line 178 "Parser.y"
{
						yyval.slist.add(val_peek(0).stmt);
					}
break;
case 27:
//#line 182 "Parser.y"
{
                		yyval = new SemValue();
                		yyval.slist = new ArrayList<Tree>();
                	}
break;
case 28:
//#line 189 "Parser.y"
{
						yyval.stmt = val_peek(0).vdef;
					}
break;
case 29:
//#line 194 "Parser.y"
{
                		if (yyval.stmt == null) {
                			yyval.stmt = new Tree.Skip(val_peek(0).loc);
                		}
                	}
break;
case 39:
//#line 211 "Parser.y"
{
						yyval.stmt = new Tree.Assign(val_peek(2).lvalue, val_peek(0).expr, val_peek(1).loc);
					}
break;
case 40:
//#line 215 "Parser.y"
{
                		yyval.stmt = new Tree.Exec(val_peek(0).expr, val_peek(0).loc);
                	}
break;
case 41:
//#line 219 "Parser.y"
{
                		yyval = new SemValue();
                	}
break;
case 43:
//#line 226 "Parser.y"
{
                		yyval = new SemValue();
                	}
break;
case 44:
//#line 232 "Parser.y"
{
						yyval.lvalue = new Tree.Ident(val_peek(1).expr, val_peek(0).ident, val_peek(0).loc);
						if (val_peek(1).loc == null) {
							yyval.loc = val_peek(0).loc;
						}
					}
break;
case 45:
//#line 239 "Parser.y"
{
                		yyval.lvalue = new Tree.Indexed(val_peek(3).expr, val_peek(1).expr, val_peek(3).loc);
                	}
break;
case 46:
//#line 245 "Parser.y"
{
						yyval.expr = new Tree.CallExpr(val_peek(4).expr, val_peek(3).ident, val_peek(1).elist, val_peek(3).loc);
						if (val_peek(4).loc == null) {
							yyval.loc = val_peek(3).loc;
						}
					}
break;
case 47:
//#line 254 "Parser.y"
{
						yyval.expr = val_peek(0).lvalue;
					}
break;
case 50:
//#line 260 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.PLUS, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 51:
//#line 264 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.MINUS, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 52:
//#line 268 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.MUL, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 53:
//#line 272 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.DIV, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 54:
//#line 276 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.MOD, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 55:
//#line 280 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.EQ, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 56:
//#line 284 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.NE, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 57:
//#line 288 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.LT, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 58:
//#line 292 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.GT, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 59:
//#line 296 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.LE, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 60:
//#line 300 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.GE, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 61:
//#line 304 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.AND, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 62:
//#line 308 "Parser.y"
{
                		yyval.expr = new Tree.Binary(Tree.OR, val_peek(2).expr, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 63:
//#line 312 "Parser.y"
{
                		yyval = val_peek(1);
                	}
break;
case 64:
//#line 316 "Parser.y"
{
                		yyval.expr = new Tree.Unary(Tree.NEG, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 65:
//#line 320 "Parser.y"
{
                		yyval.expr = new Tree.Unary(Tree.NOT, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 66:
//#line 324 "Parser.y"
{
                		yyval.expr = new Tree.Unary(Tree.RE, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 67:
//#line 328 "Parser.y"
{
                		yyval.expr = new Tree.Unary(Tree.IM, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 68:
//#line 332 "Parser.y"
{
                		yyval.expr = new Tree.Unary(Tree.COMPCAST, val_peek(0).expr, val_peek(1).loc);
                	}
break;
case 69:
//#line 336 "Parser.y"
{
                		yyval.expr = new Tree.ReadIntExpr(val_peek(2).loc);
                	}
break;
case 70:
//#line 340 "Parser.y"
{
                		yyval.expr = new Tree.ReadLineExpr(val_peek(2).loc);
                	}
break;
case 71:
//#line 344 "Parser.y"
{
                		yyval.expr = new Tree.ThisExpr(val_peek(0).loc);
                	}
break;
case 72:
//#line 348 "Parser.y"
{
                		yyval.expr = new Tree.SuperExpr(val_peek(0).loc);
                	}
break;
case 73:
//#line 352 "Parser.y"
{
                		yyval.expr = new Tree.NewClass(val_peek(2).ident, val_peek(3).loc);
                	}
break;
case 74:
//#line 356 "Parser.y"
{
                		yyval.expr = new Tree.NewArray(val_peek(3).type, val_peek(1).expr, val_peek(4).loc);
                	}
break;
case 75:
//#line 360 "Parser.y"
{
                		yyval.expr = new Tree.TypeTest(val_peek(3).expr, val_peek(1).ident, val_peek(5).loc);
                	}
break;
case 76:
//#line 364 "Parser.y"
{
                		yyval.expr = new Tree.TypeCast(val_peek(2).ident, val_peek(0).expr, val_peek(0).loc);
                	}
break;
case 77:
//#line 368 "Parser.y"
{
                		yyval.expr = new Tree.CaseExpr(val_peek(5).expr, val_peek(2).slist, val_peek(1).stmt, val_peek(7).loc);
                	}
break;
case 78:
//#line 372 "Parser.y"
{
                		yyval.expr = new Tree.DcopyExpr(val_peek(1).expr, val_peek(3).loc);
                	}
break;
case 79:
//#line 376 "Parser.y"
{
                		yyval.expr = new Tree.ScopyExpr(val_peek(1).expr, val_peek(3).loc);
                	}
break;
case 80:
//#line 382 "Parser.y"
{
						yyval.slist.add(val_peek(0).stmt);
					}
break;
case 81:
//#line 386 "Parser.y"
{
						yyval = new SemValue();
						yyval.slist = new ArrayList<Tree>();
					}
break;
case 82:
//#line 393 "Parser.y"
{
						yyval.stmt=new Tree.CaseStmt(val_peek(3).expr,val_peek(1).expr,val_peek(3).loc);
					}
break;
case 83:
//#line 399 "Parser.y"
{
						yyval.stmt=new Tree.DefaultStmt(val_peek(1).expr,val_peek(3).loc);
					}
break;
case 84:
//#line 405 "Parser.y"
{
						yyval.expr = new Tree.Literal(val_peek(0).typeTag, val_peek(0).literal, val_peek(0).loc);
					}
break;
case 85:
//#line 409 "Parser.y"
{
						yyval.expr = new Null(val_peek(0).loc);
					}
break;
case 87:
//#line 416 "Parser.y"
{
                		yyval = new SemValue();
                		yyval.elist = new ArrayList<Tree.Expr>();
                	}
break;
case 88:
//#line 423 "Parser.y"
{
						yyval.elist.add(val_peek(0).expr);
					}
break;
case 89:
//#line 427 "Parser.y"
{
                		yyval.elist = new ArrayList<Tree.Expr>();
						yyval.elist.add(val_peek(0).expr);
                	}
break;
case 90:
//#line 434 "Parser.y"
{
					yyval.stmt = new Tree.PrintCompStmt(val_peek(1).elist, val_peek(3).loc);
				}
break;
case 91:
//#line 439 "Parser.y"
{
						yyval.stmt = new Tree.DoStmt(val_peek(1).slist, val_peek(2).loc);
					}
break;
case 92:
//#line 445 "Parser.y"
{
						yyval.slist.add(val_peek(0).stmt);
					}
break;
case 93:
//#line 449 "Parser.y"
{
						yyval = new SemValue();/*???*/
						yyval.slist = new ArrayList<Tree>();
						yyval.slist.add(val_peek(0).stmt);
					}
break;
case 94:
//#line 457 "Parser.y"
{
						yyval.stmt=new Tree.DoSubStmt(val_peek(2).expr,val_peek(0).stmt,val_peek(2).loc);
					}
break;
case 95:
//#line 463 "Parser.y"
{
						yyval.stmt = new Tree.WhileLoop(val_peek(2).expr, val_peek(0).stmt, val_peek(4).loc);
					}
break;
case 96:
//#line 469 "Parser.y"
{
						yyval.stmt = new Tree.ForLoop(val_peek(6).stmt, val_peek(4).expr, val_peek(2).stmt, val_peek(0).stmt, val_peek(8).loc);
					}
break;
case 97:
//#line 475 "Parser.y"
{
						yyval.stmt = new Tree.Break(val_peek(0).loc);
					}
break;
case 98:
//#line 481 "Parser.y"
{
						yyval.stmt = new Tree.If(val_peek(3).expr, val_peek(1).stmt, val_peek(0).stmt, val_peek(5).loc);
					}
break;
case 99:
//#line 487 "Parser.y"
{
						yyval.stmt = val_peek(0).stmt;
					}
break;
case 100:
//#line 491 "Parser.y"
{
						yyval = new SemValue();
					}
break;
case 101:
//#line 497 "Parser.y"
{
						yyval.stmt = new Tree.Return(val_peek(0).expr, val_peek(1).loc);
					}
break;
case 102:
//#line 501 "Parser.y"
{
                		yyval.stmt = new Tree.Return(null, val_peek(0).loc);
                	}
break;
case 103:
//#line 507 "Parser.y"
{
						yyval.stmt = new Print(val_peek(1).elist, val_peek(3).loc);
					}
break;
//#line 1397 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    //if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      //if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        //if (yychar<0) yychar=0;  //clean, if necessary
        //if (yydebug)
          //yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      //if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
//## The -Jnorun option was used ##
//## end of method run() ########################################



//## Constructors ###############################################
//## The -Jnoconstruct option was used ##
//###############################################################



}
//################### END OF CLASS ##############################
