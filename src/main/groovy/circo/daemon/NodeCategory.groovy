/*
 * Copyright (c) 2012, the authors.
 *
 *    This file is part of 'Circo'.
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



package circo.daemon

import circo.util.CircoHelper
import org.slf4j.MDC

/**
 * This class provides some helper methods to configure the logging {@code MDC} context
 * <p> It is supped to applied to an actor class using using the @Mixin definition
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

class NodeCategory {


    @Lazy
    def String mdcActorName = { CircoHelper.removePathPrefix(self?.path()) } ()

    @Lazy
    def String mdcNodeId = { nodeId?.toString() } ()


    def setMDCVariables() {
        MDC.put('node', mdcNodeId)
        MDC.put('actor', mdcActorName )
    }


}
