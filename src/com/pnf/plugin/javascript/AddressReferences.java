/**
Copyright PNF Software, Inc.

    https://www.pnfsoftware.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.pnf.plugin.javascript;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Makes the correspondance between Absolute Position (meaning Address) and the Position in Document
 * by (line, offsetInLine)
 * 
 * @author Cedric Lucas
 *
 */
public class AddressReferences {

    public static class Position implements Comparable<Position> {
        private final int line;
        private final int offsetAtLine;

        public Position(int line, int offsetAtLine) {
            this.line = line;
            this.offsetAtLine = offsetAtLine;
        }

        public int getLine() {
            return line;
        }

        public int getOffsetAtLine() {
            return offsetAtLine;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + line;
            result = prime * result + offsetAtLine;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Position other = (Position)obj;
            if(line != other.line)
                return false;
            if(offsetAtLine != other.offsetAtLine)
                return false;
            return true;
        }

        @Override
        public int compareTo(Position o) {
            int c = Integer.compare(getLine(), o.getLine());
            if(c != 0) {
                return c;
            }
            return Integer.compare(getOffsetAtLine(), o.getOffsetAtLine());
        }

    }

    public static Position getPosition(TreeMap<Integer, String> lines, int absolutePosition) {
        NavigableMap<Integer, String> headMap = lines.headMap(absolutePosition, true);
        Integer position = headMap.lastKey();
        if(position == null) {
            return null;
        }
        return new Position(headMap.size() - 1, absolutePosition - position);
    }

    public static String getAbsolutePosition(TreeMap<Integer, String> lines, int lineDelta, int columnOffset) {
        Entry<Integer, String> line = getEntryAtLine(lines, lineDelta);
        if(line == null) {
            return null;
        }
        return Integer.toString(line.getKey() + columnOffset);
    }

    private static Entry<Integer, String> getEntryAtLine(TreeMap<Integer, String> lines, int lineDelta) {
        int i = 0;
        for(Entry<Integer, String> entry: lines.entrySet()) {
            if(i == lineDelta) {
                return entry;
            }
            i++;
        }
        return null;
    }

}
