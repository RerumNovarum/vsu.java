JAVAC = javac
JAR = jar
JAVA = java

JAVACP = .

JAVACFLAGS = -cp $(JAVACP) -g
JAVAFLAGS = -cp $(JAVACP)

.PHONY: all

TASKS_SRC = Quine.java MazeWalker.java KnightsTour.java Combinations.java
TASKS_CLASSES = $(TASKS_SRC:.java=.class)

all: Quine.out.java $(TASKS_CLASSES)

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
