
/**
 *  Copyright 2007-2009 Vidar Svansson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package is.hax.autobox.iterators; 

import java.util.Iterator;


/**
 * Adapter for String to Iterable.
 * <br></br><br></br>
 * This is the internal data structure for Query&lt;String>.
 *
 * @author Vidar Svansson
 *
 * @since 2008
 *
 *
 */
public class IterableString implements Iterable<String> {

    private String string;

    public IterableString(String string){
        this.string = string;
    }

    public Iterator<String> iterator() {
        return new StringIterator(string);
    }

    public String toString() { return string; }

    /**
     * Iterator class for strings and character arrays.
     * The next method returns a char.
     *
     * @author Vidar Svansson
     * @since 0.1
     */
    public static class CharIterator implements Iterator<Character> {
        private char[] seq;
        private int state = 0;

        public CharIterator(String string){
            this(string.toCharArray());
        }

        public CharIterator(char[] chars){
            this.seq = chars;
        }

        public boolean hasNext() {
            return state < seq.length;
        }

        public Character next() {
            return seq[state++];
        }

        public void remove() {
            throw new UnsupportedOperationException("Strings are immutable");
        }
    }

    /**
     * Iterator class for strings and character arrays.
     * The next method returns a String of length 1.
     *
     * @author Vidar Svansson
     * @since 0.1
     */
    public static class StringIterator implements Iterator<String>{
        private CharIterator iterator;

        public StringIterator(String string){
            this.iterator = new CharIterator(string);
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public String next() {
            return "" + iterator.next();
        }

        public void remove() {
            throw new UnsupportedOperationException("Strings are immutable");
        }
    }
}
