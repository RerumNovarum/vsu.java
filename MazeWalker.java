import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.lang.IndexOutOfBoundsException;


public class MazeWalker {
    private static final int MAX_LEN = 1024;
    private static final int MAX_DEG = 4;
    private static final int INFTY = MAX_LEN+1;
    private static final int EMPTY = INFTY;

    public static class IntArray2
    {
        private int[] data;
        private int W;
        private int H;

        public IntArray2(int w, int h) {
            this.data = new int[w*h];
            this.W = w;
            this.H = h;
        }
        public IntArray2(int w, int h, int ini) {
            this(w, h);
            for (int i = 0; i < w*h; ++i)
                this.data[i] = ini;
        }

        public int W() {
            return this.W;
        }
        public int H() {
            return this.H;
        }
        public int get(int i, int j) {
            return this.data[i*H + j];
        }
        public void set(int i, int j, int val) {
            this.data[i*H + j] = val;
        }
    }

    public static class Point2
    {
        public int x;
        public int y;

        public Point2() {
            this(0, 0);
        }
        public Point2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int hashCode() {
            return 137*this.x + this.y;
        }

        @Override
        public boolean equals(Object that) {
            if (that instanceof Point2) {
                Point2 p = (Point2) that;
                return this.x == p.x && this.y == p.y;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", this.x, this.y);
        }
    }

    public static class MazePercolationSystem
    {
        /* it'd be much better to write integers-specific implementation */
        /* of hashmap with linear probing */
        private HashMap<Point2, Integer> xytoi;
        private int[] itox;
        private int[] itoy;
        private int xo, yo, xd, yd;
        private int n;

        public MazePercolationSystem(String path) {
            this.xytoi = new HashMap<Point2, Integer>();
            /* length of original path */
            int moves = path.length();
            while (!Character.isLetter(path.charAt(moves-1)))
                --moves;
            /* destination (see below) */
            int xd, yd;
            /* (xo, yo) is the origin */
            int xo = 0, yo = 0;
            int x = 0, y = 0;
            int[] DX = new int[1+(int)'U'];
            int[] DY = new int[1+(int)'U'];
            DX['R'] = 1;  DY['R'] = 0;
            DX['L'] = -1; DY['L'] = 0;
            DX['U'] = 0;  DY['U'] = 1;
            DX['D'] = 0;  DY['D'] = -1;

            for (int i = 0; i < moves; ++i) {
                int mov = (int) path.charAt(i);
                x += DX[mov];
                y += DY[mov];
                if (x < xo) xo = x;
                if (y < yo) yo = y;
            }
            /* (xd, yd) is the destination point */
            xd = x;
            yd = y;

            /* (xo, yo) is the new origin, so we translate all the points */
            xo = -xo; yo = -yo;
            xd += xo; yd += yo;

            /* mapping between (x,y) plane and serial indices */
            itox = new int[moves + 1];
            itoy = new int[moves + 1];

            x = xo;
            y = yo;
            int umax = 0; /* last serial number used */
            int u = 0; /* current vertice serial */
            mapxytoi(x, y, u);
            itox[u] = x;
            itoy[u] = y;
            for (int i = 0; i < moves; ++i) {
                int mov = (int) path.charAt(i);
                int xn = x + DX[mov];
                int yn = y + DY[mov];
                int v = xytoi(xn, yn);
                if (v == EMPTY) {
                    v = ++umax;
                    mapxytoi(xn, yn, v);
                    itox[v] = xn;
                    itoy[v] = yn;
                }
                x = xn;
                y = yn;
                u = v;
            }

            this.n = xytoi.size();
            this.xo = xo;
            this.yo = yo;
            this.xd = xd;
            this.yd = yd;
        }

        private void mapxytoi(int x, int y, int i) {
            xytoi.put(new Point2(x, y), i);
        }
        /* plane to serial  mapping */
        public int xytoi(int x, int y) {
            Point2 p = new Point2(x, y);
            if (this.xytoi.containsKey(p))
                return this.xytoi.get(p);
            return EMPTY;
        }
        /* serial to plane */
        public int itox(int i) {
            return itox[i];
        }
        public int itoy(int i) {
            return itoy[i];
        }
        /* origin vertice */
        public int originX() {
            return this.xo;
        }
        public int originY() {
            return this.yo;
        }
        public int originSerial() {
            return 0;
        }
        /* destination vertice */
        public int destinationX() {
            return this.xd;
        }
        public int destinationY() {
            return this.yd;
        }
        public int destinationSerial() {
            return xytoi(this.xd, this.yd);
        }
        /* number of vertices */
        public int n() {
            return this.n;
        }
    }

    public static class Graph
    {
        public IntArray2 adj;
        public int V;

        public Graph(int V) {
            adj = new IntArray2(V, MAX_DEG, EMPTY);
            this.V = V;
        }
        public Graph(MazePercolationSystem maze) {
            this(maze.n());
            for (int v = 0; v < maze.n(); ++v) {
                int x = maze.itox(v);
                int y = maze.itoy(v);
                tryLinkNeighbour(v, x+1, y, maze);
                tryLinkNeighbour(v, x, y+1, maze);
                tryLinkNeighbour(v, x-1, y, maze);
                tryLinkNeighbour(v, x, y-1, maze);
            }
        }
        public Boolean adj(int i, int j) {
            for (int k = 0; k < MAX_DEG; ++k)
                if (adj.get(i, k) == j) return true;
            return false;
        }
        public void link(int i, int j) {
            /* prerequisite: i and j have degree smaller than 4 */
            int k = 0;
            while (adj.get(i,k) != EMPTY) {
                if (k >= 3)
                    throw new IndexOutOfBoundsException("out of bounds");
                if (adj.get(i, k) == j) return;
                ++k;
            }
            adj.set(i, k, j);

            k = 0;
            while(adj.get(j,k) != EMPTY) {
                if (k >= 3)
                    throw new IndexOutOfBoundsException("out of bounds");
                /* unnecessary: if (adj[j][k] == i) return; */
                ++k;
            }
            adj.set(j, k, i);
        }
        private void tryLinkNeighbour(int i0, int x1, int y1, MazePercolationSystem maze) {
            int i1 = maze.xytoi(x1, y1);
            if (i1 == EMPTY) return;
            link(i0, i1);
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
                    int w = g.adj.get(v, k);
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
        /* BufferedReader r = new BufferedReader(new InputStreamReader(System.in)); */
        /* original path --- a line comprised of characters 'L','R','U','D' */
        String iniPat = args[0]; /*  r.readLine(); */
        /* visible (out of fog-of-war) part of the maze */
        /* represented as a percolation system */
        /* with vertices enumerated and mapped to a plane */
        MazePercolationSystem maze = new MazePercolationSystem(iniPat);

        /* the graph representing our out-of-fog-of-war part of maze */
        Graph g = new Graph(maze);

        int io = 0;
        int id = maze.xytoi(maze.destinationX(), maze.destinationY());;
        Paths p = new Paths(g, io);
        int optLen = p .distTo(id);
        char[] optPat = new char[optLen];
        char[][] dtomov = {
            { '0', 'U', '0' },
            { 'L', '0', 'R' },
            { '0', 'D', '0' }
        };
        int x = maze.originX();
        int y = maze.originY();
        for (int i = id; i > 0; ++i) {
            int pre = p.prev(i);
            int xpre = maze.itox(pre);
            int ypre = maze.itoy(pre);
            char mov = dtomov[1-(y-ypre)][1+x-xpre];
            optPat[optLen - i] = mov;
        }
    }
}
