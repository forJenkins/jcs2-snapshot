package org.apache.commons.jcs.auxiliary.lateral;

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
import java.rmi.UnmarshalException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jcs.auxiliary.AbstractAuxiliaryCache;
import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;
import org.apache.commons.jcs.engine.CacheAdaptor;
import org.apache.commons.jcs.engine.CacheEventQueueFactory;
import org.apache.commons.jcs.engine.CacheStatus;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.ICacheEventQueue;
import org.apache.commons.jcs.engine.behavior.ICacheServiceNonLocal;
import org.apache.commons.jcs.engine.stats.StatElement;
import org.apache.commons.jcs.engine.stats.Stats;
import org.apache.commons.jcs.engine.stats.behavior.IStatElement;
import org.apache.commons.jcs.engine.stats.behavior.IStats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to queue up update requests to the underlying cache. These requests will be processed in
 * their order of arrival via the cache event queue processor.
 */
public class LateralCacheNoWait<K extends Serializable, V extends Serializable>
    extends AbstractAuxiliaryCache<K, V>
{
    /** Don't change */
    private static final long serialVersionUID = -7251187566116178475L;

    /** The logger. */
    private final static Log log = LogFactory.getLog( LateralCacheNoWait.class );

    /** The cache */
    private final LateralCache<K, V> cache;

    /** The event queue */
    private ICacheEventQueue<K, V> eventQueue;

    /** times get called */
    private int getCount = 0;

    /** times remove called */
    private int removeCount = 0;

    /** times put called */
    private int putCount = 0;

    /**
     * Constructs with the given lateral cache, and fires up an event queue for aysnchronous
     * processing.
     * <p>
     * @param cache
     */
    public LateralCacheNoWait( LateralCache<K, V> cache )
    {
        this.cache = cache;

        if ( log.isDebugEnabled() )
        {
            log.debug( "Constructing LateralCacheNoWait, LateralCache = [" + cache + "]" );
        }

        CacheEventQueueFactory<K, V> fact = new CacheEventQueueFactory<K, V>();
        this.eventQueue = fact.createCacheEventQueue( new CacheAdaptor<K, V>( cache ), LateralCacheInfo.listenerId, cache
            .getCacheName(), cache.getAuxiliaryCacheAttributes().getEventQueuePoolName(), cache
            .getAuxiliaryCacheAttributes().getEventQueueType() );

        // need each no wait to handle each of its real updates and removes,
        // since there may
        // be more than one per cache? alternative is to have the cache
        // perform updates using a different method that specifies the listener
        // this.q = new CacheEventQueue(new CacheAdaptor(this),
        // LateralCacheInfo.listenerId, cache.getCacheName());
        if ( cache.getStatus() == CacheStatus.ERROR )
        {
            eventQueue.destroy();
        }
    }

    /**
     * @param ce
     * @throws IOException
     */
    public void update( ICacheElement<K, V> ce )
        throws IOException
    {
        putCount++;
        try
        {
            eventQueue.addPutEvent( ce );
        }
        catch ( IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
    }

    /**
     * Synchronously reads from the lateral cache.
     * <p>
     * @param key
     * @return ICacheElement<K, V> if found, else null
     */
    public ICacheElement<K, V> get( K key )
    {
        getCount++;
        if ( this.getStatus() != CacheStatus.ERROR )
        {
            try
            {
                return cache.get( key );
            }
            catch ( UnmarshalException ue )
            {
                log.debug( "Retrying the get owing to UnmarshalException..." );
                try
                {
                    return cache.get( key );
                }
                catch ( IOException ex )
                {
                    log.error( "Failed in retrying the get for the second time." );
                    eventQueue.destroy();
                }
            }
            catch ( IOException ex )
            {
                eventQueue.destroy();
            }
        }
        return null;
    }

    /**
     * Gets multiple items from the cache based on the given set of keys.
     * <p>
     * @param keys
     * @return a map of K key to ICacheElement<K, V> element, or an empty map if there is no
     *         data in cache for any of these keys
     */
    public Map<K, ICacheElement<K, V>> getMultiple(Set<K> keys)
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
     * Synchronously reads from the lateral cache.
     * <p>
     * @param pattern
     * @return ICacheElement<K, V> if found, else empty
     */
    public Map<K, ICacheElement<K, V>> getMatching(String pattern)
    {
        getCount++;
        if ( this.getStatus() != CacheStatus.ERROR )
        {
            try
            {
                return cache.getMatching( pattern );
            }
            catch ( UnmarshalException ue )
            {
                log.debug( "Retrying the get owing to UnmarshalException." );
                try
                {
                    return cache.getMatching( pattern );
                }
                catch ( IOException ex )
                {
                    log.error( "Failed in retrying the get for the second time." );
                    eventQueue.destroy();
                }
            }
            catch ( IOException ex )
            {
                eventQueue.destroy();
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Return the keys in this cache.
     * <p>
     * @see org.apache.commons.jcs.auxiliary.AuxiliaryCache#getKeySet()
     */
    public Set<K> getKeySet() throws IOException
    {
        try
        {
            return cache.getKeySet();
        }
        catch ( IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
        return Collections.emptySet();
    }

    /**
     * Adds a remove request to the lateral cache.
     * <p>
     * @param key
     * @return always false
     */
    public boolean remove( K key )
    {
        removeCount++;
        try
        {
            eventQueue.addRemoveEvent( key );
        }
        catch ( IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
        return false;
    }

    /** Adds a removeAll request to the lateral cache. */
    public void removeAll()
    {
        try
        {
            eventQueue.addRemoveAllEvent();
        }
        catch ( IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
    }

    /** Adds a dispose request to the lateral cache. */
    public void dispose()
    {
        try
        {
            eventQueue.addDisposeEvent();
        }
        catch ( IOException ex )
        {
            log.error( ex );
            eventQueue.destroy();
        }
    }

    /**
     * No lateral invocation.
     * <p>
     * @return The size value
     */
    public int getSize()
    {
        return cache.getSize();
    }

    /**
     * No lateral invocation.
     * <p>
     * @return The cacheType value
     */
    public CacheType getCacheType()
    {
        return cache.getCacheType();
    }

    /**
     * Returns the asyn cache status. An error status indicates either the lateral connection is not
     * available, or the asyn queue has been unexpectedly destroyed. No lateral invocation.
     * <p>
     * @return The status value
     */
    public CacheStatus getStatus()
    {
        return eventQueue.isWorking() ? cache.getStatus() : CacheStatus.ERROR;
    }

    /**
     * Gets the cacheName attribute of the LateralCacheNoWait object
     * <p>
     * @return The cacheName value
     */
    public String getCacheName()
    {
        return cache.getCacheName();
    }

    /**
     * Replaces the lateral cache service handle with the given handle and reset the queue by
     * starting up a new instance.
     * <p>
     * @param lateral
     */
    public void fixCache( ICacheServiceNonLocal<K, V> lateral )
    {
        cache.fixCache( lateral );
        resetEventQ();
    }

    /**
     * Resets the event q by first destroying the existing one and starting up new one.
     */
    public void resetEventQ()
    {
        if ( eventQueue.isWorking() )
        {
            eventQueue.destroy();
        }
        CacheEventQueueFactory<K, V> fact = new CacheEventQueueFactory<K, V>();
        this.eventQueue = fact.createCacheEventQueue( new CacheAdaptor<K, V>( cache ), LateralCacheInfo.listenerId, cache
            .getCacheName(), cache.getAuxiliaryCacheAttributes().getEventQueuePoolName(), cache
            .getAuxiliaryCacheAttributes().getEventQueueType() );
    }

    /**
     * @return Returns the AuxiliaryCacheAttributes.
     */
    public AuxiliaryCacheAttributes getAuxiliaryCacheAttributes()
    {
        return cache.getAuxiliaryCacheAttributes();
    }

    /**
     * getStats
     * @return String
     */
    public String getStats()
    {
        return getStatistics().toString();
    }

    /**
     * this won't be called since we don't do ICache logging here.
     * <p>
     * @return String
     */
    @Override
    public String getEventLoggingExtraInfo()
    {
        return "Lateral Cache No Wait";
    }

    /**
     * @return statistics about this communication
     */
    public IStats getStatistics()
    {
        IStats stats = new Stats();
        stats.setTypeName( "Lateral Cache No Wait" );

        ArrayList<IStatElement> elems = new ArrayList<IStatElement>();

        // IStatElement se = null;
        // no data gathered here

        // get the stats from the event queue too
        // get as array, convert to list, add list to our outer list
        IStats eqStats = this.eventQueue.getStatistics();

        IStatElement[] eqSEs = eqStats.getStatElements();
        List<IStatElement> eqL = Arrays.asList( eqSEs );
        elems.addAll( eqL );

        IStatElement se = null;

        se = new StatElement();
        se.setName( "Get Count" );
        se.setData( "" + this.getCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Remove Count" );
        se.setData( "" + this.removeCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Put Count" );
        se.setData( "" + this.putCount );
        elems.add( se );

        se = new StatElement();
        se.setName( "Attributes" );
        se.setData( "" + cache.getAuxiliaryCacheAttributes() );
        elems.add( se );

        // get an array and put them in the Stats object
        IStatElement[] ses = elems.toArray( new StatElement[elems.size()] );
        stats.setStatElements( ses );

        return stats;
    }

    /**
     * @return debugging info.
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( " LateralCacheNoWait " );
        buf.append( " Status = " + this.getStatus() );
        buf.append( " cache = [" + cache.toString() + "]" );
        return buf.toString();
    }
}
