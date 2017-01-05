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

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Arrays;

public class KnightsTour {
    public static final int INFTY = Integer.MAX_VALUE;
    public static class BoardTraversal {
        /* traversal[serial(x,y)] = the No of a step on which (x,y) is visited */
        private int[] traversal;
        /* board dimensions */
        private int m, n;
        /* moves allowed for a knight */
        private static final int[] MOVES_X = { 1,  1, -1, -1, 2,  2, -2, -2};
        private static final int[] MOVES_Y = { 2, -2,  2, -2, 1, -1,  1, -1};
        private static final int MOVES_NO  = 8;

        /* input:
         *  (m, n): dimensions
         *  (x0,y0): traversal origin
         * after construction, board.i(x,y) returns the position of (x,y) in traversal
         */
        public BoardTraversal(int m, int n, int x0, int y0) {
            if (!isProblemSolvable(m, n)) {
                return;
            }
            this.traversal = new int[m*n];
            this.m = m;
            this.n = n;
            for (int x = 0; x < m; ++x)
                for (int y = 0; y < n; ++y)
                    i(x, y, INFTY);
            i(x0, y0, 0);

            int[] choice = new int[m*n];
            Move[] movs = new Move[MOVES_NO];
            for (int i = 0; i < MOVES_NO; ++i)
                movs[i] = new Move();

            int maxDepth = n*m;
            int d = 1;
            int x = x0;
            int y = y0;

            /* d=maxDepth means we've all cells covered */
            while (d != maxDepth) {
                /* feasibleMoves() fills `movs` with points reachable from (x,y).
                 * The Wansdorf's heuristic is used:
                 * we always jump to the cell with the fewest number of "continuations".
                 * I.e. `movs` is ordered by the number of continuations
                 * i.e. the number of unvisited cells reachable from (mov.x, mov.y).
                 * If no cell is reachable from (mov.x,mov.y),
                 * mov.continuations equals INFTY.
                 * In this case we jump back.
                 * The previous cell is found just via linear search,
                 * with the condition i(xprev,yprew) == d-1.
                 * Because of the heuristic used this won't happen too often
                 */
                feasibleMoves(x, y, movs);
                Move mov = movs[choice[d]];
                if (!isFeasible(mov)) {
                    i(x, y, INFTY);
                    choice[d] = 0;
                    d += -1;
                    /* we'll choose different continuation next time.
                     * NB: it is crucial that the sorting algorithm applied to movs is *stable*
                     */
                    choice[d] += 1;
                    i(x, y, INFTY);
                    /* jump back */
                    for (int k = 0; k < MOVES_NO; ++k) {
                        if (i(x + MOVES_X[k], y + MOVES_Y[k]) == d) {
                            x += MOVES_X[k];
                            y += MOVES_Y[k];
                            break;
                        }
                    }
                } else {
                    x = mov.x;
                    y = mov.y;
                    i(x, y, d);
                    d += 1;
                }
            }
        }

        public boolean exists() {
            return this.traversal != null;
        }

        public int i(int x, int y) {
            if (!validCoords(x, y)) return INFTY;
            if (traversal == null) return INFTY;
            return traversal[y*n + x];
        }

        private void i(int x, int y, int val) {
            traversal[y*n + x] = val;
        }

        public int numberContinuations(Move mov) {
            int movs = 0;
            int oldStep = i(mov.x, mov.y);
            i(mov.x, mov.y, 1);
            for (int k = 0; k < MOVES_NO; ++k) {
                if (isFeasible(mov.x + MOVES_X[k], mov.y + MOVES_Y[k])) {
                    ++movs;
                }
            }
            i(mov.x, mov.y, oldStep);
            return movs;
        }

        private static boolean isProblemSolvable(int m, int n) {
            return (m & n & 1) == 0 && /* m and n aren't odd simultaneously */
                !(m == 1 && n != 1) &&
                m != 2 &&
                m != 4 &&
                !(m == 3 && (n == 4 || n == 6 || n ==8));
        }

        private boolean validCoords(int x, int y) {
            return 0 <= x && x < n &&
                0 <= y && y < m;
        }
        private boolean isFeasible(int x, int y) {
            return validCoords(x, y) &&
                i(x, y) == INFTY;
        }
        
        private boolean isFeasible(Move mov) {
            return mov.continuations != INFTY && isFeasible(mov.x, mov.y);
        }

        private void feasibleMoves(int x0, int y0, Move[] moves) {
            for (int k = 0; k < MOVES_NO; ++k) {
                moves[k].x = x0 + MOVES_X[k];
                moves[k].y = y0 + MOVES_Y[k];
                moves[k].continuations = 0;
                if (isFeasible(moves[k])) {
                    moves[k].continuations = numberContinuations(moves[k]);
                } else {
                    moves[k].continuations = INFTY;
                }
            }
            /* it is stable, although the implementation might use additional memory */
            /* TODO: replace it with some heapsort to guarantee stability */
            /*       and avoid unnecessary reallocations */
            Arrays.sort(moves, MOVES_BY_CONTINUATIONS);
        }

        private static class Move {
            public int x, y, continuations;
        }
        private static final Comparator<Move> MOVES_BY_CONTINUATIONS = new MoveComparator();
        public static class MoveComparator
                implements Comparator<Move> {
                @Override
                public int compare(Move a, Move b) {
                    if (a == null) return 1;
                    if (b == null) return -1;
                    if (a.continuations < b.continuations) return -1;
                    else if (a.continuations > b.continuations) return 1;
                    if (a.x < b.x) return -1;
                    else if (a.x > b.x) return 1;
                    if (a.y < b.y) return -1;
                    else if (a.y > b.y) return 1;
                    return 0;
                }
        }
    }

    public static void main(String[] args) {
        int m = Integer.parseInt(args[0]);
        int n = Integer.parseInt(args[1]);
        int x0 = (int) args[2].charAt(0) - (int) 'a';
        int y0 = (int) args[2].charAt(1) - (int) '1';
        y0 = m - y0 - 1;
        BoardTraversal tour = new BoardTraversal(m, n, x0, y0);
        PrintWriter pw = new PrintWriter(System.out);
        if (!tour.exists()) {
            pw.print("unsolvable");
            return;
        }
        for (int y = 0; y < m; ++y) {
            for (int x = 0; x < n - 1; ++x) {
                pw.print(tour.i(x, y));
                pw.print(' ');
            }
            pw.println(tour.i(n-1,y));
        }
        pw.flush();
    }
}
