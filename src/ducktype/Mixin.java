package ducktype;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * In object-oriented programming languages, a mixin is a class that provides
 * a certain functionality to be inherited or just reused by a subclass, while
 * not meant for instantiation (the generation of objects of that class).
 * 
 * <p>Mixins are synonymous with abstract base classes. Inheriting from a mixin is not a
 * form of specialization but is rather a means of collecting functionality. A
 * class or object may "inherit" most or all of its functionality from one or
 * more mixins, therefore mixins can be thought of as a mechanism of multiple
 * inheritance.</p>
 * 
 * <p>Unlike CGLIB's Mixin abilities, this class allows you to aggregate class
 * methods, and not just those methods exposed by a class's implemented interfaces.</p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Mixin">Mixin</a> on Wikipedia
 */
public final class Mixin {

    private final static class MixinMethodInterceptor implements MethodInterceptor {

        private final Mixin mixin;

        MixinMethodInterceptor(final Mixin mixin) {
            this.mixin = mixin;
        }

        @Override
        public Object intercept(final Object o, final Method method, final Object[] os, final MethodProxy mp) throws Throwable {
            // todo: we could use the @targetClass to select the "most
            // appropriate" delegate, assuming that multiple delegates implement
            // the same method signature. for now, we pick the first delegate
            // that contains a method matching the requested signature.

            // todo: we might want to explictly disallow the equals(), 
            // toString(), and hashcode() methods, since they will almost
            // certainly do the wrong thing.

            for (Object delegate : mixin.delegates) {
                try {
                    final Method proxy = delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
                    return proxy.invoke(delegate, os);
                } catch (NoSuchMethodException nsme) {
                    // swallow
                }
            }

            throw new NoSuchMethodError(method.toGenericString());
        }
    }
    private final List<Object> delegates = new ArrayList<>();

    /**
     * Creates a new Mixin with no inherited functionality
     */
    public Mixin() {
    }

    /**
     * Inherits functionality from the delegated objects
     *
     * <p>This method aggregates functionality from the passed list of objects.
     * If this list is null or empty, the method has no effect. If the delegate
     * list contains any {@code null} objects, those will be ignored.</p>
     *
     * <p>Since method lookup is currently order-dependant vis-a-vis the list of
     * inherited delegates, this Mixin will proxy the method call to the first
     * inherited delegate object with a matching method signature. This means
     * that (eg.) if 2 different delegate objects both implement a
     * "{@code void close()}" method, the first matching delegate inherited will
     * respond.</p>
     *
     * @param delegates the objects whose functionality you want to inherit
     */
    public void inherit(final Object... delegates) {
        if (delegates != null && delegates.length != 0) {
            for (Object delegate : delegates) {
                if (delegate != null) {
                    this.delegates.add(delegate);
                }
            }
        }
    }

    /**
     * Casts this Mixin to the requested type
     *
     * <p>Treats this Mixin as though it was a variable of type T. There is no
     * strict requirement that this type was one of the types you inherited.</p>
     * 
     * <p>If you attempt to access a method that isn't implemented by any of the
     * delegates, the JVM will throw a {@code NoSuchMethodError} exception at runtime.</p>
     *
     * @param <T> The class you want this mixin to be treated as
     * @param c The class you want this mixin to be treated as
     * @return this Mixin, cast to the requested type
     */
    public final <T> T asA(final Class<T> c) {
        return (T) Enhancer.create(c, new MixinMethodInterceptor(this));
    }

    /**
     * Convenience method for creating a Mixin that inherits the delegates' functionality
     *
     * @see Mixin#inherit(java.lang.Object[])
     * @param delegates the objects whose functionalities you want to inherit
     * @return a new Mixin
     */
    public static Mixin mixin(final Object... delegates) {
        Mixin mi = new Mixin();
        mi.inherit(delegates);
        return mi;
    }

    private static void test(Mixin mixin) {
        Duck duck = mixin.asA(Duck.class);
        duck.quack();
        duck.swim();
        duck.waddle();

        Goose goose = mixin.asA(Goose.class);
        goose.quack();
        duck.swim();
        goose.eatBread();
    }

    public static void main(final String[] args) {
        // this is a little Madden-esque. We create a "Turducken" of sorts, and
        // then see if it behaves like both a duck AND a goose
        System.out.println("Test 1");
        test(mixin(new Duck(), new Goose()));

        // change the order of inheritance
        System.out.println("Test 2");
        test(mixin(new Goose(), new Duck()));
    }
}
