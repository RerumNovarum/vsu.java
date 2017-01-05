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

/* NB: this class probably uses suboptimal algorithm
 * and also it uses lots of unnecessary memory allocations
 * (because of new String(char[]); though it can be easily fixed)
 */

import java.io.PrintWriter;
import java.util.Iterator;
import java.lang.Iterable;

public class Combinations
    implements Iterator<String> {
    private int n, k;
    private int min;
    private int nextToChange;
    private int[] comb;
    private char[] combstr;
    private char[] chars;

    public Combinations(char[] chars, int k) {
        this.n = chars.length;
        this.k = k;
        this.chars = chars;
    }
    public Combinations(String chars, int k) {
        this(chars.toCharArray(), k);
    }

    @Override
    public boolean hasNext() {
        if (n < k) return false;
        /* if we're yet to initialize this.comb,
         * we're allowed to return at least the first (trivial) combination
         */
        if (comb == null) return true;
        /* invariant: comb[i] > comb[i+1].
         * we want to increase the rightmost digit
         * such that there will be no need to change bits to the left
         */
        int i = nextToChange;
        if (i == k + 1) {
            i = k;
        }
        while (i > 0 && comb[i-1] == 1 + comb[i])
            --i;
        nextToChange = i;
        return i > 0;
    }

    @Override
    public String next() {
        if (!hasNext())
            return null; /* you better throw */
        if (comb == null) {
            this.comb = new int[k+1];
            this.combstr = new char[k];
            comb[0] = n;
            nextToChange = 1;
            for (int i = 1; i <= k; ++i)
                comb[i] = k - i;
        } else {
            comb[nextToChange] += 1;
            for (int j = nextToChange + 1; j <= k; ++j)
                comb[j] = k - j;
            nextToChange += 1;
        }
        for (int i = 0; i < k; ++i)
            combstr[i] = chars[comb[i+1]];
        return new String(combstr);
    }

    public static Iterable<String> choose(String from, int k) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Combinations(from, k);
            }
        };
    }

    public static void main(String[] args) {
        int k = Integer.parseInt(args[1]);
        PrintWriter pw = new PrintWriter(System.out);
        for (String comb: Combinations.choose(args[0], k))
            pw.println(comb);
        pw.flush();
    }
}
