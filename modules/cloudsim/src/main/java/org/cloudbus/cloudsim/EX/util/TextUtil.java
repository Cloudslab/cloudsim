package org.cloudbus.cloudsim.EX.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Primitives;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * This is a utility class for transforming beans and other classes into
 * consistent and well aligned text. Can be used to easily generate readable log
 * or CSV files.
 * 
 * @author nikolay.grozev
 * 
 * @see {@link Textualize}
 * 
 */
public class TextUtil {

    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    private static final SimpleDateFormat TIME_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /** Format for double precision numbers. */
    public static final DecimalFormat DEC_FORMAT = new DecimalFormat("####0.00");
    /** Number of positions used when converting doubles to text. */
    public static final int SIZE_OF_DBL_STRINGS = 10;
    /** Number of positions used when converting integers to text. */
    public static final int SIZE_OF_INT_STRINGS = 7;
    /** Minimal number of positions used when outputting strings. */
    public static final int SIZE_OF_STRINGS = 20;
    /** The new line symbol of this system. */
    public static final String NEW_LINE = System.getProperty("line.separator");

    /** The default delimeter for lines. */
    public static final String DEFAULT_DELIM = ";";

    private static final String STANDARD_GET_REGEX = "get.+";
    private static final String BOOLGET_REGEX = "is.+";
    private static final Map<Class<?>, List<Method>> GET_METHODS = new HashMap<>();

    /**
     * Converts the specified class to a single line of text. Convenient for
     * generating a header line in a log or a CSV file.
     * 
     * @param headers
     *            - the names of the headers.
     * @param headerClasses
     *            - the types of the headers. May be null or empty if the types
     *            are unknown.
     * @param delim
     *            - the delimter.
     * @return a line for the headers.
     */
    public static String getCaptionLine(final List<String> headers, final List<? extends Class<?>> headerClasses,
            final String delim) {
        StringBuilder buffer = new StringBuilder();
        int i = 0;
        for (String h : headers) {
            buffer.append(headerClasses == null || headerClasses.isEmpty() ? h : formatHeader(h, headerClasses.get(i)));
            if (i < headers.size() - 1) {
                buffer.append(delim);
            }
            i++;
        }
        return buffer.toString();
    }

    /**
     * Converts the specified list of objects to a single line of text.
     * Convenient to converting to a line in a log or a line in a CSV file. The
     * line is formatted in a way so that if put under a line with the headers
     * it will be aligned. If the headers list is empty or null it is ignored.
     * 
     * <br/>
     * 
     * The flag includeFieldNames is used to specify if the names of the
     * properties should be included in the result. If it is true, the result
     * will consist of entries like: "propA=valueA"
     * 
     * @param objects
     *            - the list of objects to print in the line.
     * @param headers
     *            - the headers. Must be of the same size as objects or null or
     *            empty.
     * @param delimeter
     *            - the delimeter to use.
     * @param includeFieldNames
     *            - a flag whether to include the names of the properties in the
     *            line as well.
     * @return
     */
    public static String getTxtLine(final List<?> objects, final List<String> headers, final String delimeter,
            final boolean includeFieldNames) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < objects.size(); i++) {
            String txt = toString(objects.get(i));
            String propName = headers.get(i);
            if (includeFieldNames) {
                result.append(propName).append("=").append(txt);
            } else {
                if (propName.length() > txt.length()) {
                    txt = String.format("%" + propName.length() + "s", txt);
                }
                result.append(txt);
            }

            result.append(i < objects.size() - 1 ? delimeter : "");
        }

        return result.toString();
    }

    /**
     * Converts the specified object to a single line of text. Convenient to
     * converting an object to a line in a log or a line in a CSV file. For the
     * purpose all get methods of the object are consequently called and the
     * results are appended with appropriate formatting. Users, can control
     * which get methods are being called by using the {@link Textualize}
     * annotation and specifying the properties (the parts of the get methods
     * after "get" or "is") and the order they need.
     * 
     * <br/>
     * 
     * Note that if the class is annotated with {@link Textualize} the order
     * specified in the annotation is used. If not - the order of the methods is
     * defined by the class they appear in (this classe's props first, then its
     * superclass and so on). Properties defined within the same class are
     * sorted alphabetically.
     * 
     * @param obj
     *            - the object to extract text from. Must not be null.
     * @return formated line of text, as described above.
     */
    public static String getTxtLine(final Object obj) {
        return getTxtLine(obj, DEFAULT_DELIM);
    }

    /**
     * Converts the specified object to a single line of text. Convenient to
     * converting an object to a line in a log or a line in a CSV file. For the
     * purpose all get methods of the object are consequently called and the
     * results are appended with appropriate formatting. Users, can control
     * which get methods are being called by using the {@link Textualize}
     * annotation and specifying the properties (the parts of the get methods
     * after "get" or "is") and the order they need.
     * 
     * <br/>
     * 
     * The flag includeFieldNames is used to specify if the names of the
     * properties should be included in the result. If it is true, the result
     * will consist of entries like: "propA=valueA"
     * 
     * <br/>
     * 
     * Note that if the class is annotated with {@link Textualize} the order
     * specified in the annotation is used. If not - the order of the methods is
     * defined by the class they appear in (this classe's props first, then its
     * superclass and so on). Properties defined within the same class are
     * sorted alphabetically.
     * 
     * @param obj
     *            - the object to extract text from. Must not be null.
     * @param delimeter
     *            - the delimeter to put between the entries in the line. Must
     *            not be null.
     * @return formated line of text, as described above.
     */
    public static String getTxtLine(final Object obj, final String delimeter) {
        return getTxtLine(obj, delimeter, null, false);
    }

    /**
     * Converts the specified object to a single line of text. Convenient to
     * converting an object to a line in a log or a line in a CSV file. For the
     * purpose all get methods of the object are consequently called and the
     * results are appended with appropriate formatting. Users, can control
     * which get methods are called by directly specifying the properties of
     * interest (with the "properties" parameter).
     * 
     * <br/>
     * 
     * The flag includeFieldNames is used to specify if the names of the
     * properties should be included in the result. If it is true, the result
     * will consist of entries like: "propA=valueA"
     * 
     * <br/>
     * 
     * @param obj
     *            - the object to extract text from. Must not be null.
     * @param properties
     *            - the properties to include in the line.
     * @return formated line of text, as described above.
     */
    public static String getTxtLine(final Object obj, final String[] properties) {
        return getTxtLine(obj, DEFAULT_DELIM, properties, false);
    }

    /**
     * Converts the specified object to a single line of text. Convenient to
     * converting an object to a line in a log or a line in a CSV file. For the
     * purpose all get methods of the object are consequently called and the
     * results are appended with appropriate formatting. Users, can control
     * which get methods are called either by directly specifying the properties
     * of interest (with the "properties" parameter) or by using the
     * {@link Textualize} annotation and specifying the properties (the parts of
     * the get methods after "get" or "is") and the order they need.
     * 
     * <br/>
     * 
     * The flag includeFieldNames is used to specify if the names of the
     * properties should be included in the result. If it is true, the result
     * will consist of entries like: "propA=valueA"
     * 
     * <br/>
     * 
     * @param obj
     *            - the object to extract text from. Must not be null.
     * @param delimeter
     *            - the delimeter to put between the entries in the line. Must
     *            not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @return formated line of text, as described above.
     */
    public static String getTxtLine(final Object obj, final String delimeter, final String[] properties) {
        return getTxtLine(obj, delimeter, properties, false);
    }

    /**
     * Converts the specified object to a single line of text. Convenient to
     * converting an object to a line in a log or a line in a CSV file. For the
     * purpose all get methods of the object are consequently called and the
     * results are appended with appropriate formatting. Users, can control
     * which get methods are called either by directly specifying the properties
     * of interest (with the "properties" parameter) or by using the
     * {@link Textualize} annotation and specifying the properties (the parts of
     * the get methods after "get" or "is") and the order they need.
     * 
     * <br/>
     * 
     * The flag includeFieldNames is used to specify if the names of the
     * properties should be included in the result. If it is true, the result
     * will consist of entries like: "propA=valueA"
     * 
     * <br/>
     * 
     * Note that if the class is annotated with {@link Textualize} the order
     * specified in the annotation is used. If not - the order of the methods is
     * defined by the class they appear in (this classe's props first, then its
     * superclass and so on). Properties defined within the same class are
     * sorted alphabetically.
     * 
     * @param obj
     *            - the object to extract text from. Must not be null.
     * @param delimeter
     *            - the delimeter to put between the entries in the line. Must
     *            not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @param includeFieldNames
     *            - a flag whether to include the names of the properties in the
     *            line as well.
     * @return formated line of text, as described above.
     */
    public static String getTxtLine(final Object obj, final String delimeter, final String[] properties,
            final boolean includeFieldNames) {
        StringBuilder result = new StringBuilder();
        List<Method> methods = extractGetMethodsForClass(obj.getClass(), properties);
        int i = 0;
        for (Method m : methods) {
            Object methodRes = null;
            try {
                methodRes = m.invoke(obj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                methodRes = "ERR " + e.getMessage();
            }

            String propName = getPropName(m);
            String mTxt = toString(methodRes);
            if (includeFieldNames) {
                result.append(propName).append("=").append(mTxt);
            } else {
                if (propName.length() > mTxt.length()) {
                    mTxt = String.format("%" + propName.length() + "s", mTxt);
                }
                result.append(mTxt);
            }

            result.append(i < methods.size() - 1 ? delimeter : "");
            i++;
        }

        return result.toString();
    }

    /**
     * Converts the specified object to a single line of text by concatenating
     * its properties and "Virtual Properties". Essentially this methods calls
     * {@link TextUtil.getTxtLine(final Object obj, final String delimeter,
     * final String[] properties, final boolean includeFieldNames)} to
     * textualise the properties and then appends the virtual properties.
     * 
     * <br>
     * <br>
     * Each virtual property is specified by a name and a function. The function
     * takes as a parameter the object and returns a string.
     * 
     * @param obj
     *            - the object to extract text from. Must not be null.
     * @param delimeter
     *            - the delimeter to put between the entries in the line. Must
     *            not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @param includeFieldNames
     *            - a flag whether to include the names of the properties in the
     *            line as well.
     * @param virtualProps
     *            - must not be null. The Functions must not throw exceptions or
     *            modify the state of the object
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <F> String getTxtLine(final F obj, final String delimeter, final String[] properties,
            final boolean includeFieldNames, final LinkedHashMap<String, Function<? extends F, String>> virtualProps) {
        StringBuilder result = new StringBuilder(getTxtLine(obj, delimeter, properties, includeFieldNames));
        if (!virtualProps.isEmpty()) {
            result.append(delimeter);

            int i = 0;
            for (Map.Entry prop : virtualProps.entrySet()) {
                String propName = (String) prop.getKey();
                String propRes = ((Function<F, String>) prop.getValue()).apply(obj);

                String txt = toString(propRes);
                if (includeFieldNames) {
                    result.append(propName).append("=").append(txt);
                } else {
                    if (propName.length() > txt.length()) {
                        txt = String.format("%" + propName.length() + "s", txt);
                    }
                    result.append(txt);
                }

                result.append(i < virtualProps.size() - 1 ? delimeter : "");
                i++;
            }
        }

        return result.toString();
    }
    
    /**
     * Converts the specified object to a single line of text by concatenating
     * its properties and "Virtual Properties". Essentially this methods calls
     * {@link TextUtil.getTxtLine(final Object obj, final String delimeter,
     * final String[] properties, final boolean includeFieldNames)} to
     * textualise the properties and then appends the virtual properties.
     * 
     * <br>
     * <br>
     * Each virtual property is specified by a name and a function. The function
     * takes as a parameter the object and returns a string.
     * 
     * @param obj
     *            - the object to extract text from. Must not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @param virtualProps
     *            - must not be null. The Functions must not throw exceptions or
     *            modify the state of the object
     * @return
     */
    public static <F> String getTxtLine(final F obj, final String[] properties,
            final LinkedHashMap<String, Function<? extends F, String>> virtualProps) {
        return getTxtLine(obj, DEFAULT_DELIM, properties, false, virtualProps);
    }

    /**
     * Converts the specified class to a single line of text. Convenient for
     * generating a header line in a log or a CSV file. For the purpose the
     * names of all properties (the parts of the get methods after "get" or
     * "is") are concatenated with appropriate padding and formatting. Users,
     * can control which properties are used by using the {@link Textualize}
     * annotation and specifying the properties and the order they need.
     * 
     * <br/>
     * 
     * Note that if the class is annotated with {@link Textualize} the order
     * specified in the annotation is used. If not - the order of the methods is
     * defined by the class they appear in (this classe's props first, then its
     * superclass and so on). Properties defined within the same class are
     * sorted alphabetically.
     * 
     * 
     * @param clazz
     *            - the class to use to create the line. Must not be null.
     * @return formated line of text, as described above.
     */
    public static String getCaptionLine(final Class<?> clazz) {
        return getCaptionLine(clazz, DEFAULT_DELIM);
    }

    /**
     * Converts the specified class to a single line of text. Convenient for
     * generating a header line in a log or a CSV file. For the purpose the
     * names of all properties (the parts of the get methods after "get"
     * orString.valueOf(obj) "is") are concatenated with appropriate padding.
     * The specified delimeter is placed between the entries in the line. Users,
     * can control which properties are used by using the {@link Textualize}
     * annotation and specifying the properties and the order they need.
     * 
     * <br/>
     * 
     * Note that if the class is annotated with {@link Textualize} the order
     * specified in the annotation is used. If not - the order of the methods is
     * defined by the class they appear in (this classe's props first, then its
     * superclass and so on). Properties defined within the same class are
     * sorted alphabetically.
     * 
     * 
     * @param clazz
     *            - the class to use to create the line. Must not be null.
     * @param delimeter
     *            - the delimeter to put between the entries in the line. Must
     *            not be null.
     * @return formated line of text, as described above.
     */
    public static String getCaptionLine(final Class<?> clazz, final String delimeter) {
        return getCaptionLine(clazz, delimeter, null);
    }

    /**
     * Converts the specified class to a single line of text. Convenient for
     * generating a header line in a log or a CSV file. For the purpose the
     * names of all properties (the parts of the get methods after "get"
     * orString.valueOf(obj) "is") are concatenated with appropriate padding.
     * The specified delimeter is placed between the entries in the line. Users,
     * can control properties are used either by directly specifying the
     * properties of interest (with the "properties" parameter) or by using the
     * {@link Textualize} annotation and specifying the properties (the parts of
     * the get methods after "get" or "is") and the order they need.
     * 
     * <br/>
     * 
     * Note that if the class is annotated with {@link Textualize} the order
     * specified in the annotation is used. If not - the order of the methods is
     * defined by the class they appear in (this classe's props first, then its
     * superclass and so on). Properties defined within the same class are
     * sorted alphabetically.
     * 
     * 
     * @param clazz
     *            - the class to use to create the line. Must not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @return formated line of text, as described above.
     */
    public static String getCaptionLine(final Class<?> clazz, final String[] properties) {
        return getCaptionLine(clazz, DEFAULT_DELIM, properties);
    }

    /**
     * Converts the specified class to a single line of text. Convenient for
     * generating a header line in a log or a CSV file. For the purpose the
     * names of all properties (the parts of the get methods after "get"
     * orString.valueOf(obj) "is") are concatenated with appropriate padding.
     * The specified delimeter is placed between the entries in the line. Users,
     * can control properties are used either by directly specifying the
     * properties of interest (with the "properties" parameter) or by using the
     * {@link Textualize} annotation and specifying the properties (the parts of
     * the get methods after "get" or "is") and the order they need.
     * 
     * <br/>
     * 
     * Note that if the class is annotated with {@link Textualize} the order
     * specified in the annotation is used. If not - the order of the methods is
     * defined by the class they appear in (this classe's props first, then its
     * superclass and so on). Properties defined within the same class are
     * sorted alphabetically.
     * 
     * 
     * @param clazz
     *            - the class to use to create the line. Must not be null.
     * @param delimeter
     *            - the delimeter to put between the entries in the line. Must
     *            not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @return formated line of text, as described above.
     */
    public static String getCaptionLine(final Class<?> clazz, final String delimeter, final String[] properties) {
        StringBuilder result = new StringBuilder();
        List<Method> methods = extractGetMethodsForClass(clazz, properties);
        int i = 0;
        for (Method m : methods) {
            String propEntry = getPropName(m);
            Class<?> returnType = Primitives.wrap(m.getReturnType());

            propEntry = formatHeader(propEntry, returnType);

            result.append(propEntry);
            result.append(i < methods.size() - 1 ? delimeter : "");
            i++;
        }

        return result.toString();
    }

    /**
     * Converts the specified class to a single line of text by appending its
     * properties and a set of so-called "virtual properties".
     * 
     * @param clazz
     *            - the class to use to create the line. Must not be null.
     * @param delimeter
     *            - the delimeter to put between the entries in the line. Must
     *            not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @param virtualProps
     *            - virtual properties, which are not actual properties of the
     *            class.
     * @return
     */
    public static String getCaptionLine(final Class<?> clazz, final String delimeter, final String[] properties,
            final String[] virtualProps) {
        StringBuilder result = new StringBuilder(getCaptionLine(clazz, delimeter, properties));
        if (virtualProps.length > 0) {
            result.append(delimeter);
            int i = 0;
            for (String prop : virtualProps) {
                result.append(formatHeader(prop, String.class));
                result.append(i < virtualProps.length - 1 ? delimeter : "");
                i++;
            }
        }

        return result.toString();
    }

    /**
     * Converts the specified class to a single line of text by appending its
     * properties and a set of so-called "virtual properties".
     * 
     * @param clazz
     *            - the class to use to create the line. Must not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @param virtualProps
     *            - virtual properties, which are not actual properties of the
     *            class.
     * @return
     */
    public static String getCaptionLine(final Class<?> clazz, final String[] properties, final String[] virtualProps) {
        return getCaptionLine(clazz, DEFAULT_DELIM, properties, virtualProps);
    }
    
    /**
     * Converts the specified class to a single line of text by appending its
     * properties and a set of so-called "virtual properties".
     * 
     * @param clazz
     *            - the class to use to create the line. Must not be null.
     * @param properties
     *            - the properties to include in the line. If null all
     *            properties specified in a {@link Textualize} annotation are
     *            used. If null and no {@link Textualize} is defined for the
     *            class - then all properties are used.
     * @param virtualProps
     *            - virtual properties, which are not actual properties of the
     *            class.
     * @return
     */
    public static String getCaptionLine(final Class<?> clazz, final String[] properties, Iterable<String> virtualProps) {
        return getCaptionLine(clazz, DEFAULT_DELIM, properties, Iterables.toArray(virtualProps, String.class));
    }
    
    @SuppressWarnings("unchecked")
    private static String formatHeader(String header, final Class<?> entryType) {
        if (Double.class.equals(entryType) || Float.class.equals(entryType) && header.length() < SIZE_OF_DBL_STRINGS) {
            header = String.format("%" + SIZE_OF_DBL_STRINGS + "s", header);
        } else if (Number.class.isAssignableFrom(entryType) && header.length() < SIZE_OF_INT_STRINGS) {
            header = String.format("%" + SIZE_OF_INT_STRINGS + "s", header);
        } else if (entryType != null && entryType.isEnum()) {
            header = String.format("%" + getEnumTxtSize((Class<? extends Enum<?>>) entryType) + "s", header);
        } else if (String.class.isAssignableFrom(entryType)) {
            header = toString(header);
        }
        return header;
    }

    private static List<Method> extractGetMethodsForClass(final Class<?> clazz1, final String[] properties) {
        List<Method> methods = null;
        Class<?> clazz = clazz1;

        Textualize classAnnotation = clazz1.getAnnotation(Textualize.class);
        String[] allowedProps = properties != null ? properties : classAnnotation != null ? classAnnotation
                .properties() : null;

        if (!GET_METHODS.containsKey(clazz)) {
            methods = new ArrayList<>();
            do {
                // Defined in the class methods (not inherited)
                List<Method> clazzMethods = new LinkedList<>(Arrays.asList(clazz.getDeclaredMethods()));

                // Remove duplicated methods with super classes
                List<Method> copyofMethods = new ArrayList<>(methods);
                for (Method method : copyofMethods)
                    for (Method clazzMethod : clazzMethods) {
                        if (clazzMethod.getName().equals(method.getName()))
                            methods.remove(method);
                    }

                // Sort them by name... since getDeclaredMethods does not
                // guarantee order
                clazzMethods.sort(MethodsAlphaComparator.METHOD_CMP);

                methods.addAll(clazzMethods);
                clazz = clazz.getSuperclass();
            } while (clazz != null);

            // Filter methods that are not getters and are not in the annotation
            // (if annotation is specified)
            for (ListIterator<Method> iter = methods.listIterator(); iter.hasNext();) {
                Method m = iter.next();
                if (allowedProps != null && !isAllowedGetter(m, allowedProps)) {
                    iter.remove();
                } else if (classAnnotation == null && !isGetter(m)) {
                    iter.remove();
                }
            }

            // Sort by the order defined in the annotation
            if (allowedProps != null) {
                methods.sort(new MethodsListIndexComparator(Arrays.asList(allowedProps)));
            }

            methods = Collections.unmodifiableList(methods);
            GET_METHODS.put(clazz, methods);
        }
        return GET_METHODS.get(clazz);
    }

    private static String getPropName(final Method getter) {
        return isBoolGetter(getter) ? getter.getName().substring(2) : isGetter(getter) ? getter.getName().substring(3)
                : getter.getName();
    }

    private static boolean isAllowedGetter(final Method m, final String[] allowedProps) {
        HashSet<String> allowedProperties = new HashSet<>(Arrays.asList(allowedProps));
        return isGetter(m) && allowedProperties.contains(getPropName(m));
    }

    private static boolean isGetter(final Method m) {
        return isBoolGetter(m) || isStandardGetter(m);
    }

    private static boolean isStandardGetter(final Method m) {
        return Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length == 0
                && m.getName().matches(STANDARD_GET_REGEX) && !Primitives.wrap(m.getReturnType()).equals(Boolean.class);
    }

    private static boolean isBoolGetter(final Method m) {
        return Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length == 0
                && m.getName().matches(BOOLGET_REGEX) && Primitives.wrap(m.getReturnType()).equals(Boolean.class);
    }

    /**
     * Returns the textual representation of the specified object.
     * 
     * @param obj
     *            - the object. Must not be null.
     * @return - the textual representation of the specified object.
     */
    public static String toString(final Object obj) {
        String result = "";
        if (obj == null) {
            result = "null";
        } else {
            Class<?> clazz = Primitives.wrap(obj.getClass());
            if (clazz.equals(Double.class) || clazz.equals(Float.class)) {
                result = String.format("%" + SIZE_OF_DBL_STRINGS + "s", TextUtil.DEC_FORMAT.format(obj));
            } else if (clazz.equals(Boolean.class)) {
                result = String.valueOf(obj);
            } else if (Number.class.isAssignableFrom(clazz)) {
                result = String.format("%" + SIZE_OF_INT_STRINGS + "s", obj);
            } else if (obj instanceof Date) {
                result = getDateFormat().format(obj);
            } else if (obj instanceof Collection<?> || obj.getClass().isArray()) {
                result = "[...]";
            } else if (obj instanceof Enum<?>) {
                result = String.format("%" + getEnumTxtSize(((Enum<?>) obj).getDeclaringClass()) + "s",
                        obj);
            } else if (obj instanceof Class) {
                result = ((Class<?>) obj).getSimpleName();
                // If toString is not predefined ...
            } else if (String.valueOf(obj).startsWith(obj.getClass().getCanonicalName() + "@")) {
                result = "ref<" + obj.hashCode() + ">";
            } else {
                result = String.format("%" + SIZE_OF_STRINGS + "s", obj);
            }
        }
        return result;
    }

    private static int getEnumTxtSize(final Class<? extends Enum<?>> enumClass) {
        int result = 0;
        for (Enum<?> e : enumClass.getEnumConstants()) {
            int len = String.valueOf(e).length();
            if (len > result) {
                result = len;
            }
        }
        return result;
    }

    /**
     * Returns the format for dates.
     * 
     * @return the format for dates.
     */
    public static DateFormat getDateFormat() {
        return FULL_DATE_FORMAT;
    }

    /**
     * Returns the format for dates, that prints only the time of the day.
     * 
     * @return the format for dates, that prints only the time of the day.
     */
    public static DateFormat getTimeFormat() {
        return TIME_DATE_FORMAT;
    }

    public static String getReadableTime(double time) {
        int days = ((int) time / (24 * 3600));
        int hours = ((int) time / 3600);
        int minutes = (int) time / 60;
        int rest = (int) time % 60;

        // Now normalize the values
        hours = hours % 24;
        minutes = minutes % 60;
        return String.format("%2d:%2d:%2d:%2d", days, hours, minutes, rest);
    }

    private static class MethodsAlphaComparator implements Comparator<Method> {
        static MethodsAlphaComparator METHOD_CMP = new MethodsAlphaComparator();

        private MethodsAlphaComparator() {
        }

        @Override
        public int compare(final Method o1, final Method o2) {
            String prop1 = getPropName(o1);
            String prop2 = getPropName(o2);
            return prop1.compareTo(prop2);
        }
    }

    private static class MethodsListIndexComparator implements Comparator<Method> {
        private List<String> properties = null;

        public MethodsListIndexComparator(final List<String> properties) {
            super();
            this.properties = properties;
        }

        @Override
        public int compare(final Method o1, final Method o2) {
            String prop1 = getPropName(o1);
            String prop2 = getPropName(o2);
            return Integer.compare(properties.indexOf(prop1), properties.indexOf(prop2));
        }
    }
}
