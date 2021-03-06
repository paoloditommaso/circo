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

package circo.client

import circo.model.Context
import scala.concurrent.duration.Duration
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class CmdSubTest extends Specification {


    def 'test parse cmd sub ' () {

        when:
        def times
        def parser = CommandParser.parse( 'sub -t 1-10 --max-duration 7min --max-inactive 8sec --sync echo hello world' )
        CmdSub cmd = parser.getCommand()


        then:
        !parser.hasFailure()
        !parser.isHelp()
        cmd.maxDuration == Duration.create('7 minutes')
        cmd.maxInactive == Duration.create('8 seconds')
        cmd.times == new IntRange(1,10)
        cmd.command.join(' ') == 'echo hello world'

    }

    def 'test parse cmd sub times' () {

        when:
        def times
        def parser = CommandParser.parse( 'sub --times 1-10:2' )
        CmdSub cmd = parser.getCommand()

        then:
        !parser.hasFailure()
        !parser.isHelp()
        cmd.times == new IntRange(1,10)

    }

    def 'test parse cmd sub each' () {

        when:
        def times
        def parser = CommandParser.parse( 'sub --each a,b,c' )
        CmdSub cmd = parser.getCommand()


        then:
        !parser.hasFailure()
        !parser.isHelp()
        cmd.eachItems == ['a','b','c']

    }

    def 'test parse cmd sub each with assignment' () {

        setup:
        def ctx = new Context().put('Z','999')

        when:
        def times
        def parser = CommandParser.parse( 'sub --each a --each b=1 --each c=[x,y,z]' )
        CmdSub cmd = parser.getCommand()
        cmd.init(ctx)

        then:
        ctx != cmd.context
        cmd.eachItems == ['a','b','c']
        cmd.context.getData('Z') == '999'
        cmd.context.getData('b') == '1'
        cmd.context.getData('c') == ['x','y','z']

    }


    /*
     * different syntax, but identical semantic of the previous
     */
    def 'test parse cmd sub each with assignment (2)' () {

        setup:
        def ctx = new Context().put('Z','999')

        when:
        def times
        def parser = CommandParser.parse( 'sub --each a,b=1,c=[x,y,z]' )
        CmdSub cmd = parser.getCommand()
        cmd.init(ctx)


        then:
        ctx != cmd.context
        cmd.eachItems == ['a','b','c']
        cmd.context.getData('Z') == '999'
        cmd.context.getData('b') == '1'
        cmd.context.getData('c') == ['x','y','z']

    }



}
