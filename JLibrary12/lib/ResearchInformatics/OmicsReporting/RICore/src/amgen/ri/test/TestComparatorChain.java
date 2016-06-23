package amgen.ri.test;

/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.comparators.ComparatorChain;

import amgen.ri.util.ExtArray;

/**
 * Tests for ComparatorChain.
 *
 * @version $Revision: 1.1 $ $Date: 2011/10/26 04:13:33 $
 *
 * @author Unknown
 */
public class TestComparatorChain {

    public TestComparatorChain() {}

    public int[] go(List<PseudoRow> in) {
        return ExtArray.quicksort(in, makeComparator());
    }

    public Comparator makeComparator() {
        ComparatorChain chain = new ComparatorChain(new ColumnComparator(0));
        chain.addComparator(new ColumnComparator(1), true);
        return chain;

    }

    public static void main(String[] args) throws Exception {
        TestComparatorChain test = new TestComparatorChain();
        List<PseudoRow> in = test.getComparableObjectsOrdered();
        for (PseudoRow row : in) {
            System.out.println(row);
        }
        System.out.println();
        int[] sortIndexes = test.go(in);
        for (int sortIndex : sortIndexes) {
            System.out.println(in.get(sortIndex));
        }
    }

    public List getComparableObjectsOrdered() {
        List list = new LinkedList();
        list.add(new PseudoRow(3, 1, 0));
        list.add(new PseudoRow(4, 4, 4));
        list.add(new PseudoRow(1, 2, 3));
        list.add(new PseudoRow(1, 38, 3));
        list.add(new PseudoRow(1, 9, 3));
        list.add(new PseudoRow(2, 3, 5));
        list.add(new PseudoRow(2, 2, 4));
        list.add(new PseudoRow(2, 4, 4));
        list.add(new PseudoRow(2, 1, 4));
        list.add(new PseudoRow(2, 8, 4));
        list.add(new PseudoRow(4, 4, 7));
        list.add(new PseudoRow(2, 2, 8));
        list.add(new PseudoRow(2, 11, 8));
        list.add(new PseudoRow(4, 2, 7));
        list.add(new PseudoRow(4, 12, 7));
        return list;
    }

    public static class PseudoRow implements Serializable {

        public int cols[] = new int[3];

        public PseudoRow(int col1, int col2, int col3) {
            cols[0] = col1;
            cols[1] = col2;
            cols[2] = col3;
        }

        public int getColumn(int colIndex) {
            return cols[colIndex];
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("[");
            buf.append(cols[0]);
            buf.append(",");
            buf.append(cols[1]);
            buf.append(",");
            buf.append(cols[2]);
            buf.append("]");
            return buf.toString();
        }

        public boolean equals(Object o) {
            if (! (o instanceof PseudoRow)) {
                return false;
            }

            PseudoRow row = (PseudoRow) o;
            if (getColumn(0) != row.getColumn(0)) {
                return false;
            }

            if (getColumn(1) != row.getColumn(1)) {
                return false;
            }

            if (getColumn(2) != row.getColumn(2)) {
                return false;
            }

            return true;
        }

    }

    public static class ColumnComparator implements Comparator, Serializable {

        protected int colIndex = 0;

        public ColumnComparator(int colIndex) {
            this.colIndex = colIndex;
        }

        public int compare(Object o1, Object o2) {

            int col1 = ( (PseudoRow) o1).getColumn(colIndex);
            int col2 = ( (PseudoRow) o2).getColumn(colIndex);

            if (col1 > col2) {
                return 1;
            } else if (col1 < col2) {
                return -1;
            }

            return 0;
        }

        public int hashCode() {
            return colIndex;
        }

        public boolean equals(Object that) {
            if (that instanceof ColumnComparator) {
                return colIndex == ( (ColumnComparator) that).colIndex;
            } else {
                return false;
            }
        }

        private static final long serialVersionUID = -2284880866328872105L;
    }
}
