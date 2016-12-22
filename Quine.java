public class Quine {
static private String pat = "public class Quine {%1$cstatic private String pat = %2$c%4$s%2$c;%1$cpublic static void main(String[] args) {%1$cSystem.out.printf(pat, 0xa, 0x22, 0x27, pat);%1$c}}%1$c";
public static void main(String[] args) {
System.out.printf(pat, 0xa, 0x22, 0x27, pat);
}}
