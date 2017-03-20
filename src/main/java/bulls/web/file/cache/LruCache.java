package bulls.web.file.cache;

/**
 * 基于内存大小，（注意）但并非严格的小于设定的内存
 *
 */

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LruCache<K, V> extends LinkedHashMap<K, V>{
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private int menSize;
    private int maxMenSize;
    private ReentrantReadWriteLock lock;

    public LruCache(int initialCapacity){
        super(initialCapacity, DEFAULT_LOAD_FACTOR, true);
        this.menSize = 0;
        this.maxMenSize = 0;
        lock = new ReentrantReadWriteLock(false);  //这里公平还是不公平值得考虑
    }

    public LruCache(int initialCapacity, int maxMenSize){
        this(initialCapacity);
       this.maxMenSize = maxMenSize;
    }

    /**
     * 添加元素，并传入元素大小
     * @param key
     * @param value
     * @param size
     * @return
     */
    public V put(K key, V value, int size){
        lock.writeLock().lock();
        if (super.get(key) == null) {
            this.menSize += size;
            super.put(key, value);
        }
        lock.writeLock().unlock();
        return value;
    }

    @Override
    public V get(Object key){
        lock.readLock().lock();
        V value = super.get(key);
        lock.readLock().unlock();
        return value;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        if (maxMenSize == 0){
            return false;
        }
        return menSize > maxMenSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : entrySet()) {
            sb.append(String.format("%s:%s ", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }

}
