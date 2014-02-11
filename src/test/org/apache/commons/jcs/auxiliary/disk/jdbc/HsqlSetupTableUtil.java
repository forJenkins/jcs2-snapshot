package org.apache.commons.jcs.auxiliary.disk.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Can use this to setup a table. */
public class HsqlSetupTableUtil
{
    /**
     * SETUP a TABLE FOR CACHE testing
     * <p>
     * @param cConn
     * @param tableName
     *
     * @throws SQLException if database problems occur
     */
    public static void setupTABLE( Connection cConn, String tableName ) throws SQLException
    {
        boolean newT = true;

        StringBuffer createSql = new StringBuffer();
        createSql.append( "CREATE CACHED TABLE " + tableName + " " );
        createSql.append( "( " );
        createSql.append( "CACHE_KEY             VARCHAR(250)          NOT NULL, " );
        createSql.append( "REGION                VARCHAR(250)          NOT NULL, " );
        createSql.append( "ELEMENT               BINARY, " );
        createSql.append( "CREATE_TIME           TIMESTAMP, " );
        createSql.append( "UPDATE_TIME_SECONDS   BIGINT, " );
        createSql.append( "MAX_LIFE_SECONDS      BIGINT, " );
        createSql.append( "SYSTEM_EXPIRE_TIME_SECONDS      BIGINT, " );
        createSql.append( "IS_ETERNAL            CHAR(1), " );
        createSql.append( "PRIMARY KEY (CACHE_KEY, REGION) " );
        createSql.append( ");" );

        Statement sStatement = cConn.createStatement();

        try
        {
            sStatement.executeQuery( createSql.toString() );
        }
        catch ( SQLException e )
        {
            if ( e.toString().indexOf( "already exists" ) != -1 )
            {
                newT = false;
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            sStatement.close();
        }

        String setupData[] = { "create index iKEY on JCS_STORE2 (CACHE_KEY, REGION)" };

        if ( newT )
        {
            for ( int i = 1; i < setupData.length; i++ )
            {
                try
                {
                    sStatement.executeQuery( setupData[i] );
                }
                catch ( SQLException e )
                {
                    System.out.println( "Exception: " + e );
                }
            }
        }
    }
}
