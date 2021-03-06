package org.apache.commons.jcs.auxiliary.disk.behavior;

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

import org.apache.commons.jcs.auxiliary.AuxiliaryCacheAttributes;

/**
 * Common disk cache attributes.
 */
public interface IDiskCacheAttributes
    extends AuxiliaryCacheAttributes
{
    /**
     * This is the default purgatory size limit. Purgatory is the area where
     * items to be spooled are temporarily stored. It basically provides access
     * to items on the to-be-spooled queue.
     */
    public static final int MAX_PURGATORY_SIZE_DEFUALT = 5000;

    /**
     * Sets the diskPath attribute of the IJISPCacheAttributes object
     * <p>
     * @param path
     *            The new diskPath value
     */
    public void setDiskPath( String path );

    /**
     * Gets the diskPath attribute of the attributes object
     * <p>
     * @return The diskPath value
     */
    public String getDiskPath();

    /**
     * Gets the maxKeySize attribute of the DiskCacheAttributes object
     * <p>
     * @return The maxPurgatorySize value
     */
    public int getMaxPurgatorySize();

    /**
     * Sets the maxPurgatorySize attribute of the DiskCacheAttributes object
     * <p>
     * @param maxPurgatorySize
     *            The new maxPurgatorySize value
     */
    public void setMaxPurgatorySize( int maxPurgatorySize );

    /**
     * Get the amount of time in seconds we will wait for elements to move to
     * disk during shutdown for a particular region.
     * <p>
     * @return the time in seconds.
     */
    public int getShutdownSpoolTimeLimit();

    /**
     * Sets the amount of time in seconds we will wait for elements to move to
     * disk during shutdown for a particular region.
     * <p>
     * This is how long we give the event queue to empty.
     * <p>
     * The default is 60 seconds.
     * <p>
     * @param shutdownSpoolTimeLimit
     *            the time in seconds
     */
    public void setShutdownSpoolTimeLimit( int shutdownSpoolTimeLimit );

    /**
     * If this is true then remove all is not prohibited.
     * <p>
     * @return boolean
     */
    public boolean isAllowRemoveAll();

    /**
     * If this is false, then remove all requests will not be honored.
     * <p>
     * This provides a safety mechanism for the persistent store.
     * <p>
     * @param allowRemoveAll
     */
    public void setAllowRemoveAll( boolean allowRemoveAll );
}
