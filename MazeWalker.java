/* Copyright 2016, Sergey Kozlukov <rerumnovarum@openmailbox.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;
import java.lang.Iterable;
import java.lang.IndexOutOfBoundsException;


public class MazeWalker {
    private static final int MAX_LEN = 1024;
    private static final int MAX_DEG = 4;
    private static final int INFTY = MAX_LEN+1;
    private static final int EMPTY = INFTY;

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

    public static class Maze
    {
        /* it'd be much better to write integers-specific implementation */
        /* of hashmap with linear probing */
        private HashMap<Point2, Integer> xytoi;
        private int[] itox;
        private int[] itoy;
        private int xo, yo, xd, yd;
        private int n;

        public Maze(String path) {
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

        /*+-----------------------------------------+
         *| Manifold structure (coordinate systems) |
         *+-----------------------------------------+
         */

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

        /* graph structure */

        public boolean adjacent(int v, int w) {
            int x1 = itox(v);
            int y1 = itoy(v);
            if (x1 == EMPTY) return false;
            int x2 = itox(w);
            int y2 = itoy(w);
            if (x2 == EMPTY) return false;
            x1 -= x2;
            y1 -= x2;
            return -1 <= x1 && x1 <= 1 &&
                -1 <= y1 && y1 <= 1;
        }

        public Iterable<Integer> adj(int v) {
            return new Iterable<Integer>() {
                public Iterator<Integer> iterator() {
                    return new MazeAdjacentVerticesIterator(Maze.this, v);
                }
            };
        }

        private static class MazeAdjacentVerticesIterator
                implements Iterator<Integer> {
                Maze maze;
                int k, x0, y0;
                private static final int[] dx = {
                    -1, 1, 0, 0
                };
                private static final int[] dy = {
                    0, 0, -1, 1
                };
                public MazeAdjacentVerticesIterator(Maze maze, int v) {
                    this.maze = maze;
                    this.k = 0;
                    this.x0 = maze.itox(v);
                    this.y0 = maze.itoy(v);
                }
                public boolean hasNext() {
                    int k0 = this.k;
                    if (next() == null)
                        return false;
                    this.k = k0;
                    return true;
                }
                public Integer next() {
                    int x, y, w;
                    while (k < 4) {
                        x = x0 + dx[k];
                        y = y0 + dy[k];
                        w = maze.xytoi(x, y);
                        k += 1;
                        if (w != EMPTY)
                            return w;
                    }
                    return null;
                }
        }
    }

    public static class Paths
    {
        int[] height;
        int[] prev;
        public Paths(Maze g, int orig) {
            height = new int[g.n()];
            prev = new int[g.n()];
            for (int i = 0; i < g.n(); ++i) {
                height[i] = INFTY;
                prev[i] = EMPTY;
            }
            height[orig] = 0;

            Queue<Integer> q = new LinkedList<Integer>();
            q.add(orig);
            while(q.size() != 0)
            {
                int v = q.poll();
                int newh = 1 + height[v];
                for (int w: g.adj(v)) {
                    if (height[w] > newh) {
                        if (height[w] == INFTY)
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
        /* with vertices enumerated and mapped to a plane */
        Maze maze = new Maze(iniPat);

        int io = 0;
        int x = maze.destinationX();
        int y = maze.destinationY();
        int id = maze.xytoi(x, y);
        Paths p = new Paths(maze, io);
        int optLen = p.distTo(id);
        char[] optPat = new char[optLen];
        char[][] dtomov = {
            { '0', 'U', '0' },
            { 'L', '0', 'R' },
            { '0', 'D', '0' }
        };
        int v = id;
        for (int i = optLen - 1; i >= 0; --i) {
            int pre = p.prev(v);
            int xpre = maze.itox(pre);
            int ypre = maze.itoy(pre);
            char mov = dtomov[1-(y-ypre)][1+x-xpre];
            optPat[i] = mov;
            v = pre;
            x = xpre;
            y = ypre;

        }
        System.out.println(new String(optPat));
    }
}
