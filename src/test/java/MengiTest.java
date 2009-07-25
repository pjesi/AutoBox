import is.hax.autobox.Mengi;
import is.hax.autobox.Function;
import is.hax.autobox.Filter;
import static is.hax.autobox.Mengi.$;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class MengiTest {


    protected Mengi<String> query;


    public static <T> void assertQueryEquals(Mengi<T> mengi, List<T> args){
        int i = 0;
        for(T s : mengi){
            T x = args.get(i++);
            if(!s.equals(x)){
                fail(s + " != " + x);
            }
        }
    }


    public static <T> void assertQueryEquals(Mengi<T> mengi, T ... args){
        assertQueryEquals(mengi, Arrays.asList(args));
    }

    @Test
    public void vararg_constructor_should_create_mengi_of_arguments(){
        query = new Mengi<String>("a", "b", "c");
        assertQueryEquals(query, "a", "b", "c");
    }

    @Test
    public void $_should_create_mengi_of_strings_with_length_of_one(){
        query = Mengi.string("ac");
        assertQueryEquals(query, "a", "c");
    }

    @Test
    public void $_should_create_mengi_of_arguments(){
        query = $("foo", "bar");
        assertQueryEquals(query, "foo", "bar");
    }

    @Test
    public void mengi_should_work_as_an_iteratble(){
        query = $(Arrays.asList("foo", "bar"));
        assertQueryEquals(query, Arrays.asList("foo", "bar"));
        assertTrue(query.iterator().next().equals("foo"));
    }

    @Test
    public void testShouldSliceAndCalculateLength(){
        query = $("a", "b", "c");

        assertEquals(2, query.slice(1).length());
        assertEquals(1, query.slice(1).slice(1).length());
        assertEquals(0, query.slice(1).slice(1).slice(1).length());
    }

    @Test
    public void testShouldSliceAndGetElementsAndCalculateLength(){
        query =$("a", "b", "c");

        assertEquals("a", query.get(0));
        assertEquals(2,   query.slice(1).length());
        assertEquals("b", query.slice(1).get(0));
        assertEquals(1,   query.slice(1).slice(1).length());
        assertEquals("c", query.slice(1).slice(1).get(0));
        assertEquals(0,   query.slice(1).slice(1).slice(1).length());
    }



    @Test
    public void testSlice(){
        query = $("a", "b", "c");

    	assertQueryEquals(query.slice(0),   "a", "b", "c");
    	assertQueryEquals(query.slice(0,1), "a", "b");
    	assertQueryEquals(query.slice(0,2), "a", "b", "c");
    	assertQueryEquals(query.slice(1),   "b", "c");
    	assertQueryEquals(query.slice(1,1), "b");
    	assertQueryEquals(query.slice(1,2), "b", "c");
    	assertQueryEquals(query.slice(2),   "c");
    	assertQueryEquals(query.slice(2,2), "c");
    }
    
    @Test
    public void should_filter_out_into_a_new_mengi_everything_that_passes_the_filter(){
        query = $("a", "b", "c");

        query = query.filter(new Filter<String>(){
            public boolean filter(String a) {
                return a.equals("b");
            }
        });

        assertQueryEquals(query, "b");
    }

    @Test
    public void testShouldWorkInIteratorLoop(){
        query = $("a", "b", "c");

        Iterator<String> it = query.iterator();
        int count = 0;
        while(it.hasNext()){
            it.next();
            count++;
        }
        assertEquals(3, count);
    }

    @Test
    public void testShouldWorkUsingIteratorNext(){
        query = $("a", "b", "c");

        Iterator<String> it = query.iterator();
        it.next();it.next();
        String s = it.next();
        assertEquals("c", s);
    }


    @Test
    public void testChaining(){

        query = $("a", "au", "e", "ey")
            .map(new Function<String,String>(){
                public String call(String t) {
                    return t.toUpperCase(); // capitalize each item
                }
            })
            .filter(new Filter<String>(){
                public boolean filter(String a) {
                    return a.length() > 1; // remove single letter items
                }
            })
            .slice(1); // skip the first item

        assertQueryEquals(query, "EY");
    }

    @Test
    public void testLength(){
        query = $("a", "b", "c");

        assertEquals(3, query.length());
    }

    @Test
    public void testShouldChainSlices(){
        query = $("a", "b", "c");

    	assertQueryEquals(query.slice(1).slice(1), "c");
    }

    @Test
    public void testShouldAlsoWorkWithStrings() {

        Mengi<String> query = Mengi.string("fo0");
        String foo = "";
        for (String c : query) {
            foo += c;
        }

        assertEquals("fo0", foo);
        assertQueryEquals(query.slice(1), "o", "0");

        assertEquals(3, query.length());
        assertEquals(2, query.slice(1).length());

        assertQueryEquals(query.slice(2),"0");

        assertEquals("o", query.get(1));
    }

    @Test
    public void testShouldMapStringToString() {
        Function<String,String> upperCase = new Function<String,String>(){
            public String call(String a) {
                return a.toUpperCase();
            }
        };

        assertQueryEquals(Mengi.string("fo0").map(upperCase), "F", "O", "0");
    }

    @Test
    public void testMapQueryTyped() {
        Function<Integer,String> integers  = new Function<Integer,String>(){
            public Integer call(String a) {
                return Integer.parseInt(a);
            }
        };

        assertQueryEquals($("1", "2").map(integers), 1, 2);

    }

    @Test
    public void testFilterQuery() {
        Filter<String> numbers = new Filter<String>(){
            public boolean filter(String a) {
                try {
                    Integer.parseInt(a);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };

        assertQueryEquals(Mengi.string("fo0").filter(numbers), "0");
    }



	public static class Counter implements Incrementer {
		int count = 0;
		public Counter inc() { count++; return this; }
	}

    @Test
    public void testShouldInvokeMethodOnAllElements(){

    	Counter c0 = new Counter();
    	Counter c1 = new Counter().inc();

    	Mengi<Incrementer> counters = $(Incrementer.class, c0, c1);
    	counters.each().inc();
    	assertEquals(1, c0.count);
    	assertEquals(2, c1.count);

    }

    @Test
    public void should_append_mengi_to_another(){
        query = $("a", "b", "c");

        Mengi<String> q = query.append($("1", "2"));
        assertQueryEquals(q, "a", "b", "c", "1", "2");
    }

    @Test
    public void should_prepend_mengi_to_another(){

        assertQueryEquals(
                $("a", "b", "c").prepend($("1", "2")),
                 "1", "2", "a", "b", "c");

    }


}
