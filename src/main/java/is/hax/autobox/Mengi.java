
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

package is.hax.autobox;


import is.hax.autobox.iterators.IterableString;
import is.hax.autobox.iterators.JoinedIterator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * A handy iterable class.
 *
 * @author Vidar Svansson
 *
 * @param <T> Generic type of the {@link Iterable} set.
 *
 * @see Iterable
 *
 * @since Jun 2008
 */
public class Mengi<T> implements Iterable<T> {

    private final Iterable<T> iterable;
    private Filter<T> filter;
    private int length = -1;


    public Mengi(T ... t) {
        this(null, Arrays.asList(t));
    }

    public Mengi(Collection<T> collection){
        length = collection.size();
        this.iterable = collection;
    }


    public Mengi(Iterable<T> iterable) {
        this(null, iterable);
    }


    private Mengi(Filter<T> filter, Iterable<T> iterable){
        this.iterable = iterable;
        this.filter = filter;
    }


    /**
     * Returns an iterator over the elements in this query.
     * Only elements that pass the filter are returned if the query contains a filter.
     * <br></br><br></br>
     * @see Iterable#iterator
     * @see Filter
     * @since 0.1
     */
    public Iterator<T> iterator() {
        Filter<T> filter = getFilter();

        if(filter == null){
            return iterable.iterator();
        }

        return new QueryIterator(iterable, getFilter(), this);
    }

    /**
     * Chain this Query with a new filter.
     *
     * @param filter to be applied to the query.
     * @return a chained Query object containing the filter.
     * @since 0.1
     */
    final public Mengi<T> filter(Filter<T> filter)  {
        return new Mengi<T>(filter, this);
    }




    protected Filter<T> getFilter(){
        return filter;
    }

    /**
     * Slice the query by a given range.
     *
     * @param x the start index
     * @param y the end index
     * @return a chained query that iterates from x to y
     * @since 0.1
     */
    final public Mengi<T> slice(final int x, final int y) {
        return new Mengi<T>(this) {
            @Override protected Filter<T> getFilter() {
                return new Filter<T>() {
                    int state = 0;
                    public boolean filter(T t) {
                        return ++ state > x && state < y ;
                    }
                };
            }
        };
    }

    /**
     * Slice the query from a given index
     *
     * @param x the start index
     * @return a chained query that iterates from x
     * @since 0.1
     */
    final public Mengi<T> slice(final int x) {
        return new Mengi<T>(this) {
            @Override protected Filter<T> getFilter() {
                return new Filter<T>() {
                    int state = 0;
                    public boolean filter(T t) {
                        return ++ state > x ;
                    }
                };
            }
        };
    }

    /**
     * Return the length of this query.
     * If the internal object is a {@link Collection}, then return its size.
     * If the query has never been iterated, then an iteration will be performed.
     * <br></br><br></br>
     * @return the length of this query.
     * @since 0.1
     */
    public int length() {
        if (length > -1) return length;
        int tmp = 0;
        for (T t : this) ++ tmp;
        return length = tmp;
    }

    /**
     * Returns an element in the query by the given index.
     * Currently returns null if not found.
     * <br></br><br></br>
     * @param index the position of element to retrieve.
     * @return an element in the query by the given index.
     * @since 0.1
     */
    public T get(final int index) {
        int x = 0;
        for (T t : this) {
            if (x ++ == index) {
                return t;
            }
        } return null;
    }

    /**
     * Create a new Mengi of elements represented by this
     * Mengi transformed by the transformer.
     * <br></br><br></br>
     * @param <I> The Generic type of the returned Mengi.
     * @param transformer the transformer to apply on the elements.
     * @return A new Mengi object containing the transformed elements.
     * @since 0.1
     */
    final public <I> Mengi<I> map(Function<I,T> transformer) {
        List<I> map = new LinkedList<I>();
        for (T t : this) {
            map.add(transformer.call(t));
        }
        return new Mengi<I>(map);
    }

    /**
     * A wrapper around the for(T : Iterable<T>) syntax for chaining
     * @param lambda the function to apply to each element
     *
     * @return a reference to this object.
     * @since 0.1
     */
    final public Mengi<T> each(Function<?,T> lambda) {
        for (T t : this) lambda.call(t);
        return this;
    }

    /**
     * A wrapper around method invocation on each element of the query.
     * When invoked, a proxy of type T is returned.
     * The proxy can then be used to invoke a method on each element.
     * This works best with void methods since it is not possible to return all the results.
     * This currently only works on queries that actually contain at least one element.
     *
     *
     * @return a T {@link Proxy} that when invoked, invokes the method on all elements in this query.
     * @since 0.1
     */
    final public T each() {
    	// create proxy of T
    	// when proxy method is invoked, invoke on all elements
    	Class<?>[] interfaces = interfaces(false);
    	@SuppressWarnings("unchecked")
        T t = (T)Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces,
				new IteratorInvocationHandler<T>(this));

        return t;
    }


    public Object compose() {
    	Class<?>[] interfaces = interfaces(true);
    	return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces,
    			new CompositeInvocationHandler<T>(this));
    }

    private final class CompositeInvocationHandler<C> implements InvocationHandler {

    	public final Mengi<C> query;

    	public CompositeInvocationHandler(Mengi<C> query) {
    		this.query = query;
    	}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			for(C c : query){
	        	if(method.getDeclaringClass().isInstance(c))
	        		return method.invoke(c, args);
			}
			throw new RuntimeException("found nothing");
		}

    }

    private Class<?>[] interfaces(boolean all){
    	if(!all){
    		Iterator<T> iterator = iterator();
    		if(iterator.hasNext()){
    			T first = iterator.next();
    			return first.getClass().getInterfaces();
    		}
    	}

		Collection<Class<?>> interfaces = new HashSet<Class<?>>();
		for(T t : this) {
			Class<?>[] ifaces = t.getClass().getInterfaces();

            interfaces.addAll(Arrays.asList(ifaces));
		}

		return interfaces.toArray(new Class<?>[interfaces.size()]);
    }



    final public <E extends Exception> T xor(Class<E> ... catchables) throws Exception {
    	Class<?>[] interfaces = interfaces(false);
        @SuppressWarnings("unchecked")
        T xor= (T)Proxy.newProxyInstance(this.getClass().getClassLoader(),  interfaces,
                new XorInvocationHandler<T,E>(this, new Mengi<Class<E>>(catchables)));

        return xor;
    }

    private static final class XorInvocationHandler<C, E extends Exception> implements InvocationHandler {

        final Mengi<C> query;
        final Mengi<Class<E>> catchables;

        public XorInvocationHandler(Mengi<C> query, Mengi<Class<E>> catchables) {
            this.query = query;
            this.catchables = catchables;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Exception lastException = null;

            for(C c : query) {
                try {
                    return method.invoke(c, args);

                } catch (Exception e1) {
                    lastException = e1;
                    for(Class<E> catchable : catchables) {
                        if(catchable.isInstance(e1)){
                            lastException = null;
                        }
                    }
                }
            }

            if(lastException != null) throw lastException;
            throw new AssertionError("unreachable state");
        }

    }



    private static final class IteratorInvocationHandler<C> implements InvocationHandler {

    	public Mengi<C> query;

    	public IteratorInvocationHandler(Mengi<C> query) {
    		this.query = query;
    	}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			for(C c : query) method.invoke(c, args);
			return null;
		}

    }

    private final class QueryIterator implements Iterator<T> {
        private int length = 0;
        private Iterator<? extends T> iterator;

        private Filter<T> filter;
        private boolean hasNext = false;
        private T next = null;
        private Mengi<T> query;

        public QueryIterator(Iterable<? extends T> iterable, Filter<T> filter, Mengi<T> query) {
            this.iterator = iterable.iterator();
            this.filter = filter;
            this.query = query;
            forward();
        }

        private void forward() {
            hasNext = false;
            T current;
            while (!hasNext && iterator.hasNext()) {
                current = iterator.next();
                if (filter == null || filter.filter(current)) {
                    ++ length;

                    next = current;
                    hasNext = true;
                }
            }
            if (!iterator.hasNext()) query.length = length;
        }

        public T next() {
            T ret = next;
            forward();
            return ret;
        }

        public boolean hasNext() {
            return hasNext;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    /**
     * Prepend a set to this query.
     * @param iterable the set to prepend
     * @return a chained Query consisting of elements the joined sets.
     * @since 0.1
     */
    public Mengi<T> prepend(final Iterable<T> iterable){
        return new Mengi<T>(this) {
            @Override public Iterator<T> iterator(){

                Iterator<T> left = iterable.iterator();
                Iterator<T> right = super.iterator();


                @SuppressWarnings("unchecked")
                Iterator<T> iterator = new JoinedIterator<T>(left, right);

                return iterator;
            }
        };
    }

    /**
     * Append a set to this query.
     * @param iterable the set to append
     * @return a chained Query consisting of elements the joined sets.
     * @since 0.1
     */
    public Mengi<T> append(final Iterable<T> iterable) {
        return new Mengi<T>(this) {
            @Override public Iterator<T> iterator(){
                @SuppressWarnings("unchecked")
                Iterator<T> it =  new JoinedIterator<T>(super.iterator(), iterable.iterator());
                return it;
            }
        };
    }


    public static <T> Mengi<T> $(T ... t) {
    	return new Mengi<T>(t);
    }

    public static <T> Mengi<T> $(Class<? extends T> klass, T ... t) {
        // nothing to do with the klass.
    	return new Mengi<T>(t);
    }


    public static <T> Mengi<T> $(Iterable<T> iterable) {
    	return new Mengi<T>(null, iterable);
    }

    public static <T> Mengi<T> $(Collection<T> iterable) {
    	return new Mengi<T>(iterable);
    }

    public static Mengi<String> string(String string) {

        Iterable<String> iterable = new IterableString(string);

        return new Mengi<String>(iterable);

    }

}