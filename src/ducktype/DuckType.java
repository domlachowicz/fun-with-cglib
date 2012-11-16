package ducktype;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * In computer programming with object-oriented programming languages, duck
 * typing is a style of dynamic typing in which an object's methods and
 * properties determine the valid semantics, rather than its inheritance from a
 * particular class or implementation of a specific interface. 
 * 
 * <p>The name of the concept refers to the duck test, attributed to James Whitcomb Riley, which 
 * may be phrased as follows:</p>
 *
 * <p style="text-indent:20px; border-style:dashed; border-width:1px">When I see a bird that walks like a duck and swims like a duck and quacks like a duck, I call that bird a duck.</p>
 *
 * <p>In duck typing, one is concerned with just those aspects of an object that
 * are used, rather than with the type of the object itself. For example, in a
 * non-duck-typed language, one can create a function that takes an object of
 * type Duck and calls that object's walk and quack methods. In a duck-typed
 * language, the equivalent function would take an object of any type and call
 * that object's walk and quack methods. If the object does not have the methods
 * that are called then the function signals a run-time error. If the object
 * does have the methods, then they are executed no matter the type of the
 * object, evoking the quotation and hence the name of this form of typing.</p>
 * 
 * <p>Duck typing is aided by habitually not testing for the type of arguments in
 * method and function bodies, relying on documentation, clear code and testing
 * to ensure correct use.</p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Duck_typing">Duck typing</a> on Wikipedia
 */
public class DuckType {

    private static final class DuckTypeMethodInterceptor implements MethodInterceptor {

        private final Object wrapped;

        DuckTypeMethodInterceptor(final Object wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Object intercept(final Object o, final Method method, final Object[] os, final MethodProxy mp) throws Throwable {
            try {
                final Method proxy = wrapped.getClass().getMethod(method.getName(), method.getParameterTypes());
                return proxy.invoke(wrapped, os);
            } catch (NoSuchMethodException nsme) {
                throw new NoSuchMethodError(nsme.getMessage());
            }
        }
    }

    /**
     * Casts/reinterprets this object to the requested type
     *
     * <p>Treats this object as though it was a variable of type T. "T" can be
     * any regular Java class or interface.</p>
     *
     * <p>If you attempt to access a method that isn't implemented by "o", the
     * JVM will throw a NoSuchMethodError exception at runtime.</p>
     * 
     * @param <T> The class you want this object to be treated as
     * @param o The object you want to duck type
     * @param c The class you want this object to be treated as
     * @return "o", interpreted as the requested type
     */
    public static <T> T asA(final Object o, final Class<T> c) {
        return (T) Enhancer.create(c, new DuckTypeMethodInterceptor(o));
    }

    public static void main(final String[] args) {
        Duck duck = new Duck();
        Goose goose = new Goose();

        duck.quack(); // quack!
        goose.quack(); // honk!

        duck.waddle(); // happily waddles
        duck.swim();

        duck = DuckType.asA(goose, Duck.class);

        duck.quack(); // honk!
        duck.swim(); // swim!

        try {
            duck.waddle(); // i'm a goose. i don't know how to waddle!
        } catch (Exception nsme) {
            // should get a NoSuchMethodException
            System.out.println(nsme.getClass().getName() + ": " + nsme.getMessage());
        }
    }
}
