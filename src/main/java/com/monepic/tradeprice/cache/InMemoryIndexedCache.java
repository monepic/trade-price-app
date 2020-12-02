package com.monepic.tradeprice.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This implementation creates indexes dynamically, based on the <b>keyExtractors</b> provided to the constructor.
 * The indexes can be queried using their <b>ordinal value</b>, corresponding to the same order the <b>keyExtractors</b> were provided.
 * The <b>keyExtractors</b> are simply functions for generating an indexing key from the type <b><T></b> that we're caching.
 * <p>
 * When an cache item is added, replaced or evicted, the indexes are updated sequentially (i.e. not atomically) and is eventually consistent.
 *
 * If an item is updated, the new version is indexed before the old version is removed.  This means that during an update, the 'old' and 'new'
 * versions of the item may appear simultaneously under two different index keys, however they will never both appear under the same index key,
 * as the 'old' value will be filtered out on querying.
 * If all the index keys are derived only from properties which comprise the object identity (things in equals/hashCode) then the item will not
 * ever appear in the same index under multiple keys.
 *
 * The cache is thread-safe in operation, with the above proviso.  For strong consistency guarantees, this implementation is not the appropriate choice
 *
 * @param <T> The type of object we're caching
 */
public class InMemoryIndexedCache<T> implements IndexedCache<T> {

    private static final AtomicLong ID_GEN = new AtomicLong();
    private static final int INDEX_INITIAL_CAPACITY = 1 << 8;
    private static final int SET_INITIAL_CAPACITY = 1 << 8;
    private static final int ALL_ITEMS_CAPACITY = 1 << 11;

    private final ConcurrentHashMap<T, ValueHolder> allItems = new ConcurrentHashMap<>(ALL_ITEMS_CAPACITY);
    private final List<Index> indices = new ArrayList<>();

    @SafeVarargs
    public InMemoryIndexedCache(Function<T, Object>... keyExtractors) {

        if (keyExtractors == null || keyExtractors.length == 0) {
            throw new IllegalArgumentException("keyExtractors cannot be null/empty");
        }

        for (Function<T, Object> keyExtractor : keyExtractors) {
            indices.add(new Index(keyExtractor));
        }
    }

    @Override
    public void addItem(T item) {

        List<Set<ValueHolder>> indexValues = new LinkedList<>();

        for (Index idx : indices) {
            // indexValues needs to be a List - if it was a Set, the empty sets returned
            // by getOrCreateByItem would be considered equal
            indexValues.add(idx.getOrCreateByItem(item));
        }

        ValueHolder valueHolder = new ValueHolder(item, indexValues);

        ValueHolder previous = allItems.put(item, valueHolder);

        for (Set<ValueHolder> idxValue : indexValues) {
            idxValue.add(valueHolder);
        }
        // If an update ends up re-inserting the same item
        // into an index, we choose to remove the old one afterwards
        // to ensure the item is always present.  The 'old' duplicate is
        // filtered out if queried during this update
        if (previous != null) {
            previous.evict(); // un-index old value
        }
    }

    @Override
    public Collection<T> getByIndexOrdinal(int indexOrdinal, Object key) {
        if (indexOrdinal < 0 || indexOrdinal >= indices.size()) {
            throw new IllegalArgumentException("bad index ordinal");
        }
        return indices.get(indexOrdinal).get(key);
    }

    @Override
    public Collection<T> getAll() {
        return allItems.values()
                .stream()
                .map(ValueHolder::getItem)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<T> evict(Predicate<T> evictionPredicate) {
        Predicate<ValueHolder> predicateWrapper = vh -> evictionPredicate.test(vh.getItem());

        Collection<T> removedItems = new LinkedList<>();
        allItems.values().stream()
                .filter(predicateWrapper)
                .forEach(v -> {
                    v.evict(); // remove from the indices
                    allItems.remove(v.getItem(), v); // use the long-form remove(k,v) to avoid removing items that have been concurrently updated
                    removedItems.add(v.getItem());
                });
        return removedItems;
    }

    private class ValueHolder implements Comparable<ValueHolder> {

        final long id;
        final T item;
        final Iterable<Set<ValueHolder>> indices;

        ValueHolder(T item, Iterable<Set<ValueHolder>> indices) {
            this.id = ID_GEN.getAndIncrement();
            this.item = item;
            this.indices = indices;
        }

        public T getItem() { return item; }

        public void evict() {
            for (Set<ValueHolder> set : indices) {
                set.remove(this);
            }
        }

        @Override
        public int compareTo(ValueHolder that) {
            return Long.compare(that.id, id);
        }
    }

    private class Index {

        final ConcurrentHashMap<Object, Set<ValueHolder>> idx = new ConcurrentHashMap<>(INDEX_INITIAL_CAPACITY);
        final Function<T, Object> keyExtractor;

        private Index(Function<T, Object> keyExtractor) {
            this.keyExtractor = keyExtractor;
        }

        Set<T> get(Object key) {
            return idx.getOrDefault(key, Collections.emptySet())
                    .stream()
                    .sorted() // necessary to filter duplicates during updates
                    .map(ValueHolder::getItem)
                    .collect(Collectors.toSet());
        }

        Set<ValueHolder> getOrCreateByItem(T item) {
            Object key = keyExtractor.apply(item);
            return idx.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet(SET_INITIAL_CAPACITY));
        }
    }
}
