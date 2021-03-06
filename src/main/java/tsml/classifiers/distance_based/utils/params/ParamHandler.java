package tsml.classifiers.distance_based.utils.params;

import weka.core.OptionHandler;

import java.util.*;
import java.util.function.Consumer;

/**
 * Purpose: handle generic options for a class. These may be in the form of a String[] (weka style), List<String> or
 * bespoke ParamSet / ParamSpace themselves.
 *
 * You must override the getParams() and setParams() functions. These will be automatically transformed into list /
 * array format for compatibility with non-bespoke non-ParamSet / ParamSpace code.
 *
 * Contributors: goastler
 */
public interface ParamHandler
    extends OptionHandler {

    /**
     * get the options array
     * @return
     */
    @Override
    default String[] getOptions() {
        return getOptionsList().toArray(new String[0]);
    }

    /**
     * get the options list.
     * @return
     */
    default List<String> getOptionsList() {
        return getParams().getOptionsList();
    }

    /**
     * set options via list.
     * @param options
     * @throws Exception
     */
    default void setOptionsList(List<String> options) throws
                                                      Exception {
        ParamSet params = new ParamSet();
        params.setOptionsList(options);
        setParams(params);
    }

    /**
     * set options via array.
     * @param options the list of options as an array of strings
     * @throws Exception
     */
    @Override
    default void setOptions(String[] options) throws
                                      Exception {
        setOptionsList(new ArrayList<>(Arrays.asList(options))); // todo replace with view
    }

    @Override
    default Enumeration listOptions() {
        return Collections.enumeration(listParams());
    }

    default void setParams(ParamSet param) {
        throw new UnsupportedOperationException("param setting not supported. make sure you've overriden setParams " +
                                                    "and getParams!");
    }

    default ParamSet getParams() {
        return new ParamSet();
    }

    /**
     * Set parameter using name, a setter and a class type of the parameter. This is a utility method so you don't
     * have to keep typing out the parameter propogation code every time you set a parameter from options.
     * @param params
     * @param name
     * @param setter
     * @param clazz
     * @param <A>
     */
    static <A> void setParam(ParamSet params, String name, Consumer<A> setter, Class<? extends A> clazz) {
        List<Object> paramSets = params.get(name);
        if(paramSets == null) {
            return;
        }
        for(Object value : paramSets) {
            try {
                setter.accept((clazz.cast(value)));
            } catch(ClassCastException e) {
                IllegalStateException exception = new IllegalStateException(
                    "Cannot cast {" + value + "} to class {" + clazz.getSimpleName() + "} for"
                        + " parameter {" + name + "}");
                exception.addSuppressed(e);
                throw exception;
            }
        }
    }

    default Set<String> listParams() {
        // todo use getParams to populate this
        throw new UnsupportedOperationException("param list not specified");
    }

    /**
     * set a parameter to a ParamSet. Parameters are propogated through that object to children, if any parameters
     * are specified for the children.
     * @param object
     * @param paramSet
     */
    static void setParams(Object object, ParamSet paramSet) {
        try {
            if(object instanceof ParamHandler) {
                ((ParamHandler) object).setParams(paramSet);
            } else if(object instanceof OptionHandler) {
                ((OptionHandler) object).setOptions(paramSet.getOptions());
            } else {
                throw new IllegalArgumentException("params not settable");
            }
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
