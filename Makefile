JAVAC = javac
JAR = jar
JAVA = java

JAVACP = .

JAVACFLAGS = -cp $(JAVACP) -g
JAVAFLAGS = -cp $(JAVACP)

.PHONY: all

all: Quine.out.java MazeWalker.class

%.jar:
	$(JAVAC) $(JAVACFLAGS) $(cdr $^)
	$(JAR) cfe $@ $(^:.java=.class)

%.class: %.java
	$(JAVAC) $(JAVACFLAGS) $<

java-%: %.class
	@JAVACP=$(JAVACP):$<
	@$(JAVA) $(JAVAFLAGS) $*

jar-%: %.jar
	@JAVACP=$(JAVACP):$<
	@$(JAVA) $(JAVAFLAGS) $*

Quine.out.java: Quine.class
	$(JAVA) $(JAVAFLAGS) Quine > Quine.out.java
	diff Quine.out.java Quine.java
