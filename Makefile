JAVAC = javac
JAR = jar
JAVA = java

JAVACP = .

JAVACFLAGS = -cp $(JAVACP)
JAVAFLAGS = -cp $(JAVACP)

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
