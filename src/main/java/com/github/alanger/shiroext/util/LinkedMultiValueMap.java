package com.github.alanger.shiroext.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// org.springframework.util.LinkedMultiValueMap
public class LinkedMultiValueMap<K, V> extends MultiValueMapAdapter<K, V> // new public base class in 5.3
        implements Serializable, Cloneable {

    private static final long serialVersionUID = -1L;

    /**
     * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}.
     */
    public LinkedMultiValueMap() {
        super(new LinkedHashMap<>());
    }

    /**
     * Create a new LinkedMultiValueMap that wraps a {@link LinkedHashMap}
     * with an initial capacity that can accommodate the specified number of
     * elements without any immediate resize/rehash operations to be expected.
     * 
     * @param expectedSize
     *            the expected number of elements (with a corresponding
     *            capacity to be derived so that no resize/rehash operations are
     *            needed)
     * @see CollectionUtils#newLinkedHashMap(int)
     */
    public LinkedMultiValueMap(int expectedSize) {
        super(CollectionUtils.newLinkedHashMap(expectedSize));
    }

    /**
     * Copy constructor: Create a new LinkedMultiValueMap with the same mappings as
     * the specified Map. Note that this will be a shallow copy; its value-holding
     * List entries will get reused and therefore cannot get modified independently.
     * 
     * @param otherMap
     *            the Map whose mappings are to be placed in this Map
     * @see #clone()
     * @see #deepCopy()
     */
    public LinkedMultiValueMap(Map<K, List<V>> otherMap) {
        super(new LinkedHashMap<>(otherMap));
    }

    /**
     * Create a deep copy of this Map.
     * 
     * @return a copy of this Map, including a copy of each value-holding List entry
     *         (consistently using an independent modifiable {@link ArrayList} for
     *         each entry)
     *         along the lines of {@code MultiValueMap.addAll} semantics
     * @since 4.2
     * @see #addAll(MultiValueMap)
     * @see #clone()
     */
    public LinkedMultiValueMap<K, V> deepCopy() {
        LinkedMultiValueMap<K, V> copy = new LinkedMultiValueMap<>(size());
        forEach((key, values) -> copy.put(key, new ArrayList<>(values)));
        return copy;
    }

    /**
     * Create a regular copy of this Map.
     * 
     * @return a shallow copy of this Map, reusing this Map's value-holding List
     *         entries
     *         (even if some entries are shared or unmodifiable) along the lines of
     *         standard
     *         {@code Map.put} semantics
     * @since 4.2
     * @see #put(Object, List)
     * @see #putAll(Map)
     * @see LinkedMultiValueMap#LinkedMultiValueMap(Map)
     * @see #deepCopy()
     */
    @Override
    public LinkedMultiValueMap<K, V> clone() {
        return new LinkedMultiValueMap<>(this);
    }

}
