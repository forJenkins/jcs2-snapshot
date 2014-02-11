package org.apache.commons.jcs.auxiliary.remote.value;

import java.io.Serializable;

/**
 * This is the response wrapper. The servlet wraps all different type of responses in one of these
 * objects.
 */
public class RemoteCacheResponse<T>
    implements Serializable
{
    /** Don't change. */
    private static final long serialVersionUID = -8858447417390442568L;

    /** Was the event processed without error */
    private boolean success = true;

    /** Simple error messaging */
    private String errorMessage;

    /**
     * The payload. Typically a key / ICacheElement<K, V> map. A normal get will return a map with one
     * record.
     */
    private T payload;

    /**
     * @param success the success to set
     */
    public void setSuccess( boolean success )
    {
        this.success = success;
    }

    /**
     * @return the success
     */
    public boolean isSuccess()
    {
        return success;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage( String errorMessage )
    {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * @param payload the payload to set
     */
    public void setPayload( T payload )
    {
        this.payload = payload;
    }

    /**
     * @return the payload
     */
    public T getPayload()
    {
        return payload;
    }

    /** @return string */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "\nRemoteHttpCacheResponse" );
        buf.append( "\n success [" + isSuccess() + "]" );
        buf.append( "\n payload [" + getPayload() + "]" );
        buf.append( "\n errorMessage [" + getErrorMessage() + "]" );
        return buf.toString();
    }
}
