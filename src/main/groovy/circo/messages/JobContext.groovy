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

package circo.messages
import circo.data.DataRef
import circo.data.EmptyRef
import circo.data.ObjectRef
import circo.util.SerializeId
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@SerializeId
@EqualsAndHashCode(includes = 'holder')
@ToString(includes = 'names', includePackage = false)
class JobContext implements Serializable {

    /**
     * Read more about multimap
     * https://code.google.com/p/guava-libraries/wiki/NewCollectionTypesExplained#Multimap
     */
    final Multimap<String, DataRef> holder

    /** Names in list need to be cleared before a put operation is applied */
    final List<String> overridableVariables

    JobContext() {
        holder = ArrayListMultimap.create()
    }


    /**
     * Keep it private. Use {@code #copy} to copy the object
     *
     * @param origin The context to be copied
     */
    private JobContext( JobContext origin )  {
        assert origin
        holder = ArrayListMultimap.create( origin.holder )
        overridableVariables = new ArrayList<>(origin.holder.keySet())
    }

    /**
     * Static copy method
     *
     * @param origin The context to be copied
     * @return A new copy of the specified context
     */
    def static JobContext copy( JobContext origin ) {
        new JobContext(origin)
    }

    def List<String> getNames() {
        new ArrayList<>(holder.keySet())
    }

    def List<DataRef> getRef( String name ) {
        new ArrayList<>(holder.get(name))
    }

    def List<DataRef> allRefs() {
        new ArrayList<>(holder.values())
    }

    /**
     * Return always a collection of data
     * @param name The required value
     */
    def Collection getValues( String name ) {

        holder.get(name) *. data
    }

    def Object getData(String name) {
        Collection<DataRef> refs = holder.get(name)

        if ( !refs ) {
            return new EmptyRef()
        }

        /*
         * inf contains only one element, just return it
         */
        if ( refs.size() == 1 ) {
            return refs.iterator().next().data
        }

        /*
         * otherwise return a collection of the data values
         */
        refs *. data
    }


    boolean contains( String name ) {
        holder.containsKey(name)
    }

    int size() {
        return holder.keySet().size()
    }

    def JobContext plus( JobContext that  ) {

        that?.allRefs()?.each { DataRef it -> this.add(it)  }
        return this
    }

    def JobContext add( DataRef ref ) {
        if ( overridableVariables && overridableVariables.contains(ref.name)) {
            overridableVariables.remove(ref.name)
            holder.removeAll(ref.name)
        }

        holder.put(ref.name, ref)
        return this
    }


    def JobContext add( String name, def values ) {
        values.each {
                this.add(new ObjectRef(name,it))
        }
        this
    }

    def JobContext put( DataRef... ref ) {
        assert ref

        ref.collect().unique().each {
            holder.removeAll(it)
        }

        ref.each { DataRef it ->
            holder.put(it.name, it)
        }

        return this
    }

    def JobContext put( String name, Object... values )  {
        assert name
        assert values
        assert !(values[0] instanceof DataRef)

        holder.removeAll(name)
        values.each {
            holder.put(name, new ObjectRef(name,it))
        }

        return this
    }

    def JobContext removeAll( String name ) {
        holder.removeAll(name)
        this
    }


    /*
     * Invoke the closure for each possible combination for the variables in the context
     * specified by its names
     */
    void combinations( List<String> names, Closure callback ) {

        List<List<DataRef>> sets = names.collect { getRef(it) }

        int numOfCombs = 1;
        for(int i = 0; i < sets.size(); numOfCombs *= sets[i++].size());

        for(int i = 0; i < numOfCombs; i++) {
            int j = 1;

            def param = new ArrayList<DataRef>(sets.size())
            for(List<DataRef> list : sets) {
                def p = (i/j) as int
                param << list[p % list.size()]
                j *= list.size();
            }
            // invoke the callback with this combination
            callback.call(param)
        }


    }

}