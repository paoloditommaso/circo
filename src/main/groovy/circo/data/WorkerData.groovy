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

package circo.data

import akka.actor.ActorRef
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import circo.messages.JobId
import circo.utils.SerializeId

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@SerializeId
@EqualsAndHashCode
@TupleConstructor
@ToString(includePackage = false)
class WorkerData implements Serializable {

    final WorkerRef worker

    def JobId currentJobId

    def long processed

    def long failed


    static WorkerData of( WorkerRef worker) {
        new WorkerData(worker)
    }

    static WorkerData of( WorkerRef worker, Closure closure ) {
        def result = new WorkerData(worker)
        closure.call(result)
        return result
    }

    static WorkerData of( ActorRef actor ) {
        new WorkerData(new WorkerRef(actor))
    }

}
