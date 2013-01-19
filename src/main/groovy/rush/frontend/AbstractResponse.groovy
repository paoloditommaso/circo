/*
 * Copyright (c) 2012, the authors.
 *
 *    This file is part of Rush.
 *
 *    Rush is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Rush is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Rush.  If not, see <http://www.gnu.org/licenses/>.
 */

package rush.frontend

import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Multimap
import groovy.transform.ToString
import groovy.util.logging.Slf4j

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@ToString(includePackage = false)
abstract class AbstractResponse implements Serializable {

    enum Level { INFO, WARN, ERROR }

    Multimap<Level, String> messages = LinkedListMultimap.create()

    /** The ticket (i.e. unique id) of the request that originated this result */
    final String ticket

    def AbstractResponse( String ticket ) {
        this.ticket = ticket
    }

    def AbstractResponse error( String msg ) {
        messages.put(Level.ERROR, msg)
        return this
    }

    def AbstractResponse warn( String msg ) {
        messages.put(Level.WARN, msg)
        return this
    }

    def AbstractResponse info ( String msg ) {
        messages.put(Level.INFO, msg)
        return this
    }

    def Collection<String> getInfo() { messages.get(Level.INFO) }

    def Collection<String> getWarn() { messages.get(Level.WARN) }

    def Collection<String> getError() { messages.get(Level.ERROR) }

    def boolean hasMessages() { messages.size()>0 }

    def boolean hasWarn() { getWarn()?.size()>0 }

    def boolean hasError() { getError()?.size()>0 }

    def boolean hasInfo() { getInfo()?.size()>0 }

    Collection<String> getAllMessages() {
        def result = []
        result << getError()
        result << getWarn()
        result << getInfo()
        return result
    }

    def void printMessages( Level level = null ) {

        if ( hasMessages() ) {
            if ( !level || level == Level.INFO ) info.each { log.info it }
            if ( !level || level == Level.WARN )  warn.each { log.warn "${it}" }
            if ( !level || level == Level.ERROR )  error.each { log.error "${it}" }
        }

    }


}
