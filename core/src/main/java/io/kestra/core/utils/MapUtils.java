package io.kestra.core.utils;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MapUtils {
    public static Map<String, Object> merge(Map<String, Object> a, Map<String, Object> b) {
        if (a == null && b == null) {
            return null;
        }

        if (a == null || a.isEmpty()) {
            return copyMap(b);
        }

        if (b == null || b.isEmpty()) {
            return copyMap(a);
        }

        Map copy = copyMap(a);


        Map<String, Object> copyMap = b
            .entrySet()
            .stream()
            .collect(
                HashMap::new,
                (m, v) -> {
                    Object original = copy.get(v.getKey());
                    Object value = v.getValue();
                    Object found;

                    if (value == null && original == null) {
                        found = null;
                    } else if (value == null) {
                        found = original;
                    } else if (original == null) {
                        found = value;
                    } else if (value instanceof Map && original instanceof Map) {
                        found = merge((Map) original, (Map) value);
                    } else if (value instanceof Collection
                        && original instanceof Collection) {
                        try {
                            Collection merge =
                                copyCollection(
                                    (Collection) original,
                                    (List) Lists
                                        .newArrayList(
                                            (Collection) original,
                                            (Collection) value
                                        )
                                        .stream()
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toList())
                                );
                            found = merge;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        found = value;
                    }


                    m.put(v.getKey(), found);
                },
                HashMap::putAll
            );

        copy.putAll(copyMap);

        return copy;
    }

    private static Map copyMap(Map original) {
        return ((Map<?, ?>) original)
            .entrySet()
            .stream()
            .collect(
                HashMap::new,
                (map, entry) -> {
                    Object value = entry.getValue();
                    Object found;

                    if (value instanceof Map) {
                        found = copyMap((Map) value);
                    } else if (value instanceof Collection) {
                        found = copyCollection((Collection) value, (Collection) value);
                    } else {
                        found = value;
                    }

                    map.put(entry.getKey(), found);

                },
                Map::putAll
            );
    }

    private static Collection copyCollection(Collection collection, Collection elements) {
        try {
            Collection newInstance = collection.getClass().getDeclaredConstructor().newInstance();
            newInstance.addAll(elements);
            return newInstance;
        } catch (Exception e) {
            return new ArrayList<>(elements);
        }
    }

    /**
     * Utility method for merging multiple {@link Map}s that can contains nullable values.
     * Note that the maps provided are assumed to be flat, so this method does not perform a recursive merge.
     *
     * @param maps  The Map to be merged.
     * @return     the merged Map.
     */
    public static Map<String, Object> mergeWithNullableValues(final Map<String, Object>...maps) {
        return Arrays.stream(maps)
            .flatMap(map -> map.entrySet().stream())
            // https://bugs.openjdk.org/browse/JDK-8148463
            .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    /**
     * Utility method that returns an empty HasMap if the <code>map</code> parameter is null,
     * the <code>map</code> parameter otherwise.
     */
    public static <K, V> Map<K, V> emptyOnNull(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }
}
