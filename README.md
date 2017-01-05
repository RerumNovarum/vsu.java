VSU Java labs
-------------

```bash
$ make
$ java -cp .: Quine > Quine.out.java
$ diff Quine.java Quine.out.java
$ java -cp .: MazeWalker RRLUUULLLD
RUUULLLD
$ java -cp .: KnightsTour 8 8 a1
49 18 21 4 53 8 23 6
20 3 50 47 22 5 54 9
17 48 19 44 55 52 7 24
2 41 56 51 46 43 10 59
39 16 45 42 61 58 25 34
30 1 40 57 36 33 60 11
15 38 31 28 13 62 35 26
0 29 14 37 32 27 12 63
$ java -cp .: Combinations 12345 3
321
421
431
432
521
531
532
541
542
543
```
