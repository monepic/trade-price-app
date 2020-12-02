package com.monepic.tradeprice.cache;


import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

public class InMemoryIndexedCacheTest {

    private static class TestItem {
        private static boolean LOGICAL_EQUALITY = true;
        private final String id, name, food;

        @Override
        public boolean equals(Object obj) {
            if (!LOGICAL_EQUALITY) {
                return super.equals(obj);
            }

            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TestItem)) {
                return false;
            }
            TestItem that = (TestItem) obj;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            if (!LOGICAL_EQUALITY) {
                return super.hashCode();
            }
            return Objects.hash(id);
        }

        public TestItem(String id, String name, String food) {
            this.id = id;
            this.name = name;
            this.food = food;
        }

        public String getId() { return id; }

        public String getName() { return name; }

        public String getFood() { return food; }

        @Override
        public String toString() {
            return "TestItem{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", food='" + food + '\'' +
                    '}';
        }
    }

    static final TestItem[] ITEMS = {
            new TestItem("one", "Barry", "Pizza"),
            new TestItem("two", "Fred", "Pizza"),
            new TestItem("three", "Perry", "Bacon"),
            new TestItem("four", "Barry", "Potato"),
            new TestItem("five", "Fred", "Crisps"),
    };

    public interface KeyExtractor extends Function<TestItem, Object> {
    }

    static final KeyExtractor[] keyExtractors = {
            TestItem::getName,
            TestItem::getId,
            TestItem::getFood
    };

    private void populate(IndexedCache<TestItem> cache) {
        for (TestItem item : ITEMS) {
            cache.addItem(item);
        }
    }

    @Test
    public void testCreatesAndPopulates() {
        IndexedCache<TestItem> cache = new InMemoryIndexedCache<>(keyExtractors);
        populate(cache);
    }

    @Test
    public void testByName() {
        IndexedCache<TestItem> cache = new InMemoryIndexedCache<>(keyExtractors);
        populate(cache);
        Set<TestItem> expected = Set.of(ITEMS[0], ITEMS[3]);
        assertEquals(expected, cache.getByIndexOrdinal(0, "Barry"));
    }

    @Test
    public void testByFood() {
        IndexedCache<TestItem> cache = new InMemoryIndexedCache<>(keyExtractors);
        populate(cache);
        Set<TestItem> expected = Set.of(ITEMS[3]);
        assertEquals(expected, cache.getByIndexOrdinal(2, "Potato"));
    }

    @Test
    public void testGetAll() {
        IndexedCache<TestItem> cache = new InMemoryIndexedCache<>(keyExtractors);
        populate(cache);
        Set<TestItem> expected = Set.of(ITEMS);
        assertEquals(expected, Set.copyOf(cache.getAll()));
    }

    @Test
    public void testEvictPizza() {
        IndexedCache<TestItem> cache = new InMemoryIndexedCache<>(keyExtractors);
        populate(cache);
        Collection<TestItem> evicted = cache.evict(el -> "Pizza".equals(el.getFood()));

        Set<TestItem> expected = Set.of(ITEMS[2], ITEMS[3], ITEMS[4]);
        assertEquals(expected, Set.copyOf(cache.getAll()));

        // not cached  by id anymore
        assertEquals(0, cache.getByIndexOrdinal(1, "two").size());

        // nor by name
        assertEquals(Set.of(ITEMS[3]), cache.getByIndexOrdinal(0, "Barry"));

        // nor by Pizza
        assertEquals(0, cache.getByIndexOrdinal(2, "Pizza").size());

        assertEquals(Set.of(ITEMS[0], ITEMS[1]), Set.copyOf(evicted));
    }

    @Test
    public void testReplaceItem() {
        IndexedCache<TestItem> cache = new InMemoryIndexedCache<>(keyExtractors);
        populate(cache);
        TestItem newItem = new TestItem("three", "Perry", "Pizza");
        cache.addItem(newItem);
        assertEquals(Set.of(ITEMS[0], ITEMS[1], newItem), cache.getByIndexOrdinal(2, "Pizza"));
        assertEquals(0, cache.getByIndexOrdinal(2, "Bacon").size());
    }

    @Test
    public void testUpdateInPlaceNoDupes() {
        IndexedCache<TestItem> cache = new InMemoryIndexedCache<>(keyExtractors);
        populate(cache);
        TestItem newItem = new TestItem("two", "Fred_new", "Pizza");

        TestItem.LOGICAL_EQUALITY = false; // trick the cache into accepting a duplicate item
        cache.addItem(newItem);
        assertEquals(3, cache.getByIndexOrdinal(2, "Pizza").size(), "Expected duplicate in the cache"); // sanity check - ensure duplicate was added

        TestItem.LOGICAL_EQUALITY = true; // we've simulated an in-place update, resulting in old and new values both present in the index

        Collection<TestItem> result = cache.getByIndexOrdinal(2, "Pizza");
        assertEquals(2, result.size()); // duplicate filtered out ok
        Optional<TestItem> item = result.stream()
                .filter(i -> newItem.equals(i))
                .findFirst();
        assertTrue("Didn't find newItem", item.isPresent());
        assertEquals("Fred_new", item.get().getName());
    }


}
