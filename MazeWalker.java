import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.Queue;
import java.lang.IndexOutOfBoundsException;


public class MazeWalker {
    private static final int MAX_LEN = 1024;
    private static final int MAX_DEG = 4;
    private static final int INFTY = MAX_LEN+1;
    private static final int EMPTY = INFTY;

    public static class Graph
    {
        public int[][] adj;
        public int V;
        public Graph(int V) {
            adj = new int[V][MAX_DEG];
            for (int v = 0; v < V; ++v)
                for (int k = 0; k < MAX_DEG; ++k)
                    adj[v][k] = EMPTY;
        }
        public Boolean adj(int i, int j) {
            for (int k = 0; k < MAX_DEG; ++k)
                if (k == j) return true;
            return false;
        }
        public void link(int i, int j) {
            /* prerequisite: i and j have degree smaller than 4 */
            int k = 0;
            while (adj[i][k] != EMPTY) {
                if (k >= 3) throw new IndexOutOfBoundsException("out of bounds");
                if (adj[i][k] == j) return;
                ++k;
            }
            adj[i][k] = j;

            k = 0;
            while(adj[j][k] != EMPTY) {
                if (k >= 3) throw new IndexOutOfBoundsException("out of bounds");
                /* unnecessary: if (adj[j][k] == i) return; */
                ++k;
            }
            adj[j][k] = i;
        }
    }

    public static class Paths
    {
        Graph g;
        int[] height;
        int[] prev;
        public Paths(Graph g, int orig) {
            this.g = g;
            height = new int[g.V];
            prev = new int[g.V];
            for (int i = 0; i < g.V; ++i) {
                height[i] = INFTY;
                prev[i] = EMPTY;
            }
            height[orig] = 0;

            Queue<Integer> q = new LinkedList<Integer>();
            while(q.size() != 0)
            {
                int v = q.poll();
                int newh = 1 + height[v];
                for (int k = 0; k < MAX_DEG; ++k) {
                    int w = g.adj[v][k];
                    if (w != EMPTY && height[w] > newh) {
                        if (prev[w] == EMPTY)
                            q.add(w);
                        height[w] = newh;
                        prev[w] = v;
                    }
                }
            }
        }
        int distTo(int w) {
            return height[w];
        }
        int prev(int w) {
            return prev[w];
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        /* original path --- a line comprised of characters 'L','R','U','D' */
        String iniPat = r.readLine();
        /* length of original path */
        int hops = iniPat.length();
        while (!Character.isLetter(iniPat.charAt(hops-1)))
                --hops;
        /* destination (see below) */
        int xe, ye;
        /* (xo, yo) is the origin */
        int xo = 0, yo = 0;
        /* (xmax, ymax) is the upper-right corner of the map */
        int xmax = 0, ymax = 0;
        int x = 0, y = 0;
        int[] DX = new int[1+(int)'U'], DY = new int[1+(int)'U'];
        DX['R'] = 1;  DY['R'] = 0;
        DX['L'] = -1; DY['L'] = 0;
        DX['U'] = 0;  DY['U'] = 1;
        DX['D'] = 0;  DY['D'] = -1;

        for (int i = 0; i < hops; ++i) {
            int mov = (int) iniPat.charAt(i);
            x += DX[mov];
            y += DY[mov];
            if (x < xo) xo = x;
            else if (x > xmax) xmax = x;
            if (y < yo) yo = y;
            else if (y > ymax) ymax = y;
        }
        /* (xe, ye) is the destination point */
        xe = x;
        ye = y;

        /* (xo, yo) is the new origin */
        xo = -xo;
        yo = -yo;
        xmax += xo;
        ymax += yo;
        xe += xo;
        ye += yo;
        int W = xmax + 1, H = ymax + 1;

        /* mapping between (x,y) plane and serial indices */
        int[] itox = new int[hops];
        int[] itoy = new int[hops];
        int[][] xytoi = new int[W][H];

        for (int i = 0; i < W; ++i)
            for (int j = 0; j < H; ++j)
                xytoi[i][j] = EMPTY;

        /* the graph representing our out-of-fog-of-war part of maze */
        Graph g = new Graph(hops);
        x = xo;
        y = yo;
        int umax = 0; /* last serial number used */
        int u = 0; /* current vertice serial */
        System.out.println(xmax);
        System.out.println(ymax);
        xytoi[x][y] = u;
        for (int i = 0; i < hops; ++i) {
            int mov = (int) iniPat.charAt(i);
            int xn = x + DX[mov];
            int yn = y + DY[mov];
            int v = xytoi[xn][yn];
            if (v == EMPTY) {
                v = ++umax;
                xytoi[xn][yn] = v;
            }
            /* building the graph here */
            g.link(u, v);
            x = xn;
            y = yn;
            u = v;
        }

        int io = 0;
        int ie = xytoi[xe][ye];
        Paths p = new Paths(g, io);
        int optLen = p .distTo(ie);
        char[] optPat = new char[optLen];
        char[][] dtomov = {
            { '0', 'U', '0' },
            { 'L', '0', 'R' },
            { '0', 'D', '0' }
        };
        for (int i = ie; i > 0; ++i) {
            int pre = p.prev(i);
            int xpre = itox[pre];
            int ypre = itoy[pre];
            char mov = dtomov[1-(y-ypre)][1+x-xpre];
            optPat[optLen - i] = mov;
        }

        System.out.println(optPat);
    }
}
