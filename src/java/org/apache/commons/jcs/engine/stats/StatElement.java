package org.apache.commons.jcs.engine.stats;

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

import org.apache.commons.jcs.engine.stats.behavior.IStatElement;

/**
 * This is a stat data holder.
 */
public class StatElement
    implements IStatElement
{
    /** name of the stat */
    private String name = null;

    /** the data */
    private String data = null;

    /**
     * Get the name of the stat element, ex. HitCount
     * <p>
     * @return the stat element name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Get the data, ex. for hit count you would get a String value for some number.
     * <p>
     * @return String data
     */
    public String getData()
    {
        return data;
    }

    /**
     * Set the data for this element.
     * <p>
     * @param data
     */
    public void setData( String data )
    {
        this.data = data;
    }

    /**
     * @return a readable string.
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( name + " = " + data );
        return buf.toString();
    }
}
