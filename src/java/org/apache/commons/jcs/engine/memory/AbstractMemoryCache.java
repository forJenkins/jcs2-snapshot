package org.apache.commons.jcs.engine.memory;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.commons.jcs.engine.behavior.IRequireScheduler;
import org.apache.commons.jcs.engine.control.CompositeCache;
import org.apache.commons.jcs.engine.memory.behavior.IMemoryCache;
import org.apache.commons.jcs.engine.memory.shrinking.ShrinkerThread;
import org.apache.commons.jcs.engine.memory.util.MemoryElementDescriptor;
import org.apache.commons.jcs.engine.stats.Stats;
import org.apache.commons.jcs.engine.stats.behavior.IStats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This base includes some common code for memory caches.
 * <p>
 * This keeps a static reference to a memory shrinker clock daemon. If this region is configured to
 * use the shrinker, the clock daemon will be setup to run the shrinker on this region.
 */
public abstract class AbstractMemoryCache<K extends Serializable, V extends Serializable>
    implements IMemoryCache<K, V>, Serializable, IRequireScheduler
{
    /** Don't change. */
    private static final long serialVersionUID = -4494626991630099575L;

    /** Log instance */
    private final static Log log = LogFactory.getLog( AbstractMemoryCache.class );

    /** The region name. This defines a namespace of sorts. */
    protected String cacheName;

    /** Map where items are stored by key.  This is created by the concrete child class. */
    public Map<K, MemoryElementDescriptor<K, V>> map;

    /** Region Elemental Attributes, used as a default and copied for each item. */
    public IElementAttributes elementAttributes;

    /** Cache Attributes.  Regions settings. */
    public ICompositeCacheAttributes cacheAttributes;

    /** The cache region this store is associated with */
    public CompositeCache<K, V> cache;

    /** status */
    protected CacheStatus status;

    /** How many to spool at a time. */
    protected int chunkSize;

    /**
     * For post reflection creation initialization
     * <p>
     * @param hub
     */
    public synchronized void initialize( CompositeCache<K, V> hub )
    {
        this.cacheName = hub.getCacheName();
        this.cacheAttributes = hub.getCacheAttributes();
        this.cache = hub;
        map = createMap();

        chunkSize = cacheAttributes.getSpoolChunkSize();
        status = CacheStatus.ALIVE;
    }

    /**
     * @see org.apache.commons.jcs.engine.behavior.IRequireScheduler#setScheduledExecutorService(java.util.concurrent.ScheduledExecutorService)
     */
    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutor)
    {
        if ( cacheAttributes.isUseMemoryShrinker() )
        {
            scheduledExecutor.scheduleAtFixedRate(
                    new ShrinkerThread<K, V>(this), 0, cacheAttributes.getShrinkerIntervalSeconds(),
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Children must implement this method. A FIFO implementation may use a tree map. An LRU might
     * use a hashtable. The map returned should be threadsafe.
     * <p>
     * @return a threadsafe Map
     */
    public abstract Map<K, MemoryElementDescriptor<K, V>> createMap();

    /**
     * Removes an item from the cache
     * <p>
     * @param key Identifies item to be removed
     * @return Description of the Return Value
     * @exception IOException Description of the Exception
     */
    public abstract boolean remove( K key )
        throws IOException;

    /**
     * Get an item from the cache
     * <p>
     * @param key Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException Description of the Exception
     */
    public abstract ICacheElement<K, V> get( K key )
        throws IOException;

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of K key to ICacheElement<K, V> element, or an empty map if there is no
     *         data in cache for any of these keys
     * @throws IOException
     */
    public Map<K, ICacheElement<K, V>> getMultiple( Set<K> keys )
        throws IOException
    {
        Map<K, ICacheElement<K, V>> elements = new HashMap<K, ICacheElement<K, V>>();

        if ( keys != null && !keys.isEmpty() )
        {
            for (K key : keys)
            {
                ICacheElement<K, V> element = get( key );

                if ( element != null )
                {
                    elements.put( key, element );
                }
            }
        }

        return elements;
    }

    /**
     * Get an item from the cache without affecting its last access time or position. Not all memory
     * cache implementations can get quietly.
     * <p>
     * @param key Identifies item to find
     * @return Element matching key if found, or null
     * @exception IOException
     */
    public ICacheElement<K, V> getQuiet( K key )
        throws IOException
    {
        ICacheElement<K, V> ce = null;

        MemoryElementDescriptor<K, V> me = map.get( key );
        if ( me != null )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( cacheName + ": MemoryCache quiet hit for " + key );
            }

            ce = me.ce;
        }
        else if ( log.isDebugEnabled() )
        {
            log.debug( cacheName + ": MemoryCache quiet miss for " + key );
        }

        return ce;
    }

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce Description of the Parameter
     * @exception IOException Description of the Exception
     */
    public abstract void update( ICacheElement<K, V> ce )
        throws IOException;

    /**
     * Get a set of the keys for all elements in the memory cache
     * <p>
     * @return A set of the key type
     */
    public abstract Set<K> getKeySet();

    /**
     * Removes all cached items from the cache.
     * <p>
     * @exception IOException
     */
    public void removeAll()
        throws IOException
    {
        map.clear();
    }

    /**
     * Prepares for shutdown.
     * <p>
     * @exception IOException
     */
    public void dispose()
        throws IOException
    {
        log.info( "Memory Cache dispose called." );
    }

    /**
     * @return statistics about the cache
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Abstract Memory Cache" );
        return stats;
    }

    /**
     * Returns the current cache size.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        return this.map.size();
    }

    /**
     * Returns the cache status.
     * <p>
     * @return The status value
     */
    public CacheStatus getStatus()
    {
        return this.status;
    }

    /**
     * Returns the cache (aka "region") name.
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        String attributeCacheName = this.cacheAttributes.getCacheName();
        if(attributeCacheName != null)
        	return attributeCacheName;
        return cacheName;
    }

    /**
     * Puts an item to the cache.
     * <p>
     * @param ce
     * @exception IOException
     */
    public void waterfal( ICacheElement<K, V> ce )
        throws IOException
    {
        this.cache.spoolToDisk( ce );
    }

    /**
     * Gets the iterator attribute of the LRUMemoryCache object
     * <p>
     * @return The iterator value
     */
    public Iterator<Map.Entry<K, MemoryElementDescriptor<K, V>>> getIterator()
    {
        return map.entrySet().iterator();
    }

    // ---------------------------------------------------------- debug method
    /**
     * Dump the cache map for debugging.
     */
    public void dumpMap()
    {
        log.debug( "dumpingMap" );
        for (Map.Entry<K, MemoryElementDescriptor<K, V>> e : map.entrySet())
        {
            MemoryElementDescriptor<K, V> me = e.getValue();
            log.debug( "dumpMap> key=" + e.getKey() + ", val=" + me.ce.getVal() );
        }
    }

    /**
     * Returns the CacheAttributes.
     * <p>
     * @return The CacheAttributes value
     */
    public ICompositeCacheAttributes getCacheAttributes()
    {
        return this.cacheAttributes;
    }

    /**
     * Sets the CacheAttributes.
     * <p>
     * @param cattr The new CacheAttributes value
     */
    public void setCacheAttributes( ICompositeCacheAttributes cattr )
    {
        this.cacheAttributes = cattr;
    }

    /**
     * Gets the cache hub / region that the MemoryCache is used by
     * <p>
     * @return The cache value
     */
    public CompositeCache<K, V> getCompositeCache()
    {
        return this.cache;
    }
}
