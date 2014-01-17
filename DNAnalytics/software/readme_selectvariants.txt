
Las Palmas de Gran Canaria
February 2013
Author: Pascual Lorente Arencibia
Title: SelectVariants Expression

Valid expressions that can be put into 'Expression Field'

1. Single expression
---------------------------------------------------
Any single expression follows this sintax:
  
WORD SIMBOL VALUE

# WORD
 VCF Tags:
   AF : Allele Frequency (only with biallic)
   AC : Allele Count
   DP : Read Depth
   (try with any other vcf info tag)
 Math expression:
   AC / AN : Allele Count divided by Allele Number

# SIMBOL
 == : equal
 != : not equal
 >  : greater
 <  : less
 >= : greater or equal
 <= : less or equal

# VALUE
 numbers:
   naturals : 1, 4, -3
   reals    : 0.4, 10.0
 strings: '1/1', 'MQ'
 booleans: TRUE, FALSE

2. Combined expression
------------------------------------------------------
You can concatenate two or more expressions

single_expression LOGIC_OPERATOR single_expression

# LOGIC_OPERATOR
 && : and
 || : or

3. Examples
------------------------------------------------------
 
AF == 1 && culprit = 'QD'
Select variants with Allele Frequency 1 (100%) and culprit value QD

 QD < 2.0 || ReadPosRankSum < -20.0 || FS > 200.0
Select variants with any of the conditions above

 QUAL / DP < 10.0
Select variants which QD divided by DP (Read Depth) is less than 10.0