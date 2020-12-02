package com.monepic.tradeprice.cache;

import java.util.Collection;
import java.util.function.Predicate;

public interface IndexedCache<T> {

    /**
     * Add an item to the cache, and index it
     *
     * @param item
     */
    void addItem(T item);

    /**
     * @param indexOrdinal
     * @param key
     * @return the items matching <b>key</b> for the index identified by <b>indexOrdinal</b>
     */
    Collection<T> getByIndexOrdinal(int indexOrdinal, Object key);

    /**
     * @return all the cache entries
     */
    Collection<T> getAll();

    /**
     * remove all items from the cache and all indexes, where the item matches the evictionPredicate
     *
     * @param evictionPredicate
     * @return the removed items
     */
    Collection<T> evict(Predicate<T> evictionPredicate);
}
