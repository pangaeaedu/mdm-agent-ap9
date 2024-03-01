package com.nd.adhoc.push.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 数据检查 工具类
 * <p>
 * Created by HuangYK on 2018/6/5.
 */
public class AdhocDataCheckUtils {

    /**
     * 判断一个集合是否为空，null 或 empty
     *
     * @param pCollection 集合对象
     * @param <T>         数据类型
     * @return true：为空，false：不为空
     */
    public static <T> boolean isCollectionEmpty(Collection<T> pCollection) {
        return pCollection == null || pCollection.isEmpty();

    }

    /**
     * 判断一个 Map 是否为空
     *
     * @param pMap Map 对象
     * @param <K>  Map Key
     * @param <V>  Map Value
     * @return true：为空，false：不为空
     */
    public static <K, V> boolean isMapEmpty(Map<K, V> pMap) {
        return pMap == null || pMap.isEmpty();

    }

    /**
     * 判断一个数组是否为空
     *
     * @param pArray 数组对象
     * @param <T>    数据类型
     * @return true：为空，false：不为空
     */
    public static <T> boolean isArrayEmpty(T[] pArray) {
        return pArray == null || pArray.length <= 0;
    }

    /**
     * 判断一个迭代器是否为空
     *
     * @param pIterator 迭代器
     * @param <T>       数据类型
     * @return true：为空，false：不为空
     */
    public static <T> boolean isIteratorEmpty(Iterator<T> pIterator) {
        return pIterator == null || !pIterator.hasNext();
    }

}
