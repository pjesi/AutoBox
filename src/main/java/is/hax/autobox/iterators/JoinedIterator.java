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
import java.util.NoSuchElementException;
import java.util.Arrays;

/**
 * Implementation of the Iterator interfaces that joins
 * together multiple interators.
 *
 * @author Vidar Svansson
 *
 * @param <T>
 * @since 2008
 */
public class JoinedIterator<T> implements Iterator<T>{

    private Iterator<? extends T>[] iterators;
    private int status = 0;
    private Iterator<? extends T> current;
    
    public JoinedIterator(Iterator<? extends T> ... iterators){
        this.iterators = iterators;
        this.current = iterators[status++];
    }

    public boolean hasNext() {
        if(current.hasNext()){
            return true;
        }
        if(status < iterators.length) {
            current = iterators[status++];
            return hasNext();
        } return false;
    }

    public T next() {
        if(current.hasNext()){
            return current.next();
        }
        if(status < iterators.length) {
            current = iterators[status++];
            return next();
        }
        throw new NoSuchElementException();
    }

    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }

}
