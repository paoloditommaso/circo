/*
 * Copyright (c) 2012, the authors.
 *
 *    This file is part of Circo.
 *
 *    Circo is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Circo is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Circo.  If not, see <http://www.gnu.org/licenses/>.
 */

package circo.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import akka.actor.ActorPath
import akka.actor.Address
import akka.actor.Address as AkkaAddress
import circo.Const
import circo.client.CustomIntRange
import circo.client.CustomStringRange
import groovy.util.logging.Slf4j

/**
 * General utility methods
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
class CircoHelper {

    static private Random rndGen = new Random()

    static final char[] ALPHA = ('a'..'z') as char[]

    static final char[] NUMERIC = ('0'..'9') as char[]

    static final char[] ALPHANUM = (('a'..'z')+('0'..'9')) as char[]

    static DATETIME_FORMAT = "dd/MMM/yyyy HH:mm"

    static SHORT_DATETIME_FORMAT = "HH:mm dd/MMM"

    static TIME_FORMAT = "HH:mm:ss"

    static final EMPTY = '-'

    static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols()

    static {
        SYMBOLS.setGroupingSeparator("'" as char)
    }

    static String fmt( Number value, Integer len = null ) {
        String result
        if ( !value ) {
            result = EMPTY
        }

        DecimalFormat fmt = new DecimalFormat('#,##0', SYMBOLS)
        fmt.getDecimalFormatSymbols().setGroupingSeparator(" " as char)
        result = fmt.format(value)

        if ( len != null ) {
            result = result.padLeft( len )
        }

        result
    }

    static String fmt( Date date ) {
        if( !date ) return EMPTY

        date.format(DATETIME_FORMAT)

    }


    static String fmt( AkkaAddress address, Integer pad = null ) {
        String result
        if ( !address ) {
            result = EMPTY
        }
        else {
            result = address.toString()
            int pos = result.indexOf('@')
            result = pos != 1 ? result.substring(pos+1) : result
        }

        if ( pad ) {
            result = result.padRight(pad)
        }

        result
    }


    static String getSmartTimeFormat( long millis ) {
        getSmartTimeFormat(new Date(millis))
    }

    static String getSmartTimeFormat( Date timestamp ) {
        assert timestamp

        def cal = today()

        def delta = timestamp.getTime() - cal.getTimeInMillis()
        if ( delta > 0 ) {
            timestamp.format(TIME_FORMAT)
        }
        else if ( timestamp.format('yyyy') == String.valueOf( cal.get(Calendar.YEAR)) )  {
            timestamp.format(SHORT_DATETIME_FORMAT)
        }
        else {
            timestamp.format(DATETIME_FORMAT)
        }

    }

    static Calendar today() {
        def now = Calendar.getInstance()
        def year = now.get( Calendar.YEAR )
        def month = now.get( Calendar.MONTH )
        def day = now.get( Calendar.DAY_OF_MONTH )

        new GregorianCalendar(year, month, day)
    }

    /**
     * Convert a string to an {@code Address} instance
     *
     * @param str The akka address in following the syntax {@code [protocol://system@]host:port
     * @param port The TCP port to use if it is not specified by the string value. Default {@code Consts#DEFAULT_AKKA_PORT}
     * @param system The akka system name to be used if it not specified by the string value. Default {@code Consts#DEFAULT_AKKA_SYSTEM}
     * @param protocol The akka protocol to be used if it is not specified by the string value. Default {@code Consts#DEFAULT_AKKA_PROTOCOL}
     * @return
     */
    def static Address parseAddress(String str, int port = Const.DEFAULT_AKKA_PORT, String system = Const.DEFAULT_CLUSTER_NAME, String protocol = Const.DEFAULT_AKKA_PROTOCOL) {
        assert str

        int p = str.indexOf('@')

        if ( p != -1 ) {
            def meta = str.substring(0,p)
            str = str.substring(p+1)


            p = meta.indexOf('://')
            if( p != -1 ) {
                protocol = meta.substring(0,p)
                system = meta.substring(p+3)
            }
            else {
                system = meta
            }
        }

        def host
        p = str.indexOf(':')
        if ( p != -1 ) {
            host = str.substring(0,p)
            port = str.substring(p+1).toInteger()
        }
        else {
            host = str
        }

        new Address(protocol,system,host,port)
    }

    /**
     * Converts a string to a range
     *
     * @param value
     * @return
     */
    static Range parseRange( String value ) {
        assert value

        int p = value.indexOf('..')
        String alpha = value.substring(0,p)
        String omega = value.substring(p+2)

        int step
        p = omega.indexOf(':')
        if ( p != -1 ) {
            step = omega.substring(p+1).toInteger()
            omega = omega.substring(0,p)
        }
        else {
            step =1
        }

        if( alpha.isInteger() && omega.isInteger() ) {
            return new CustomIntRange(alpha.toInteger(),omega.toInteger(), step)
        }
        else {
            return new CustomStringRange(alpha,omega,step)
        }

    }

    /**
     * The application 'version' string
     *
     * @param full
     * @return
     */
    static def String version(boolean full=false) {

        if ( !full ) {
            "${Const.APP_VER}.${Const.APP_BUILDNUM}"
        }
        else {
            "${Const.APP_VER} - Build on ${CircoHelper.fmt(new Date(Const.APP_TIMESTAMP))} - build # ${Const.APP_BUILDNUM}"
        }

    }

    /**
     * Remove the common prefix '/user/xxx' from an {@code ActorPath}
     *
     * @param path The path to be clean
     * @return The actor path as a string without the /user/ prefix
     */
    static String removePathPrefix( ActorPath path ) {
        def str = path.toString()
        def p = str.indexOf('user/')
        return p != -1 ? str.substring(p+5) : str
    }

    /**
     * Creates a random string with the number of character specified
     * e.g. {@code s8hm2nxt3}
     *
     * @param len The len of the final random string
     * @param alphabet The set of characters allowed in the random string
     * @return The generated random string
     */
    static String randomString( int len, char[] alphabet ) {
        assert alphabet.size() > 0

        StringBuilder result = new StringBuilder()
        final max = alphabet.size()

        len.times {
            def index = rndGen.nextInt(max)
            result.append( alphabet[index] )
        }

        return result.toString()
    }

    static String randomString( int len ) {
        randomString(len, ALPHA)
    }


    /**
     * The process scratch folder
     * @param seed
     * @return
     */
    static File createScratchDir( final File baseDir = Const.APP_TMP_DIR ) {

        long timestamp = System.currentTimeMillis()
        while( true ) {

            String rnd1 = randomString(2, NUMERIC)
            String rnd2 = randomString(2, ALPHA)
            String rnd3 = randomString(4, ALPHANUM)
            String rnd4 = randomString(4, ALPHANUM)

            File tempDir = new File(baseDir, "$rnd1/$rnd2/$rnd3-$rnd4".toString());

            if (tempDir.mkdirs()) {
                return tempDir;
            }
            else if ( !tempDir.exists() ) {
                // when 'mkdirs' failed because it was unable to create the folder
                // (since it does not exist) throw an exception
                throw new IllegalStateException("Cannot create scratch folder: '${tempDir}' -- verify access permissions" )
            }

            if( System.currentTimeMillis() - timestamp > 1_000 ) {
                throw new IllegalStateException("Unable to create scratch folder: '${tempDir}' after multiple attempts -- verify access permissions" )
            }

            Thread.sleep(50)
        }
    }
}
