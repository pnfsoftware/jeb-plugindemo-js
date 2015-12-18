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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.pnfsoftware.jeb.core.output.IActionableItem;
import com.pnfsoftware.jeb.core.output.ItemClassIdentifiers;
import com.pnfsoftware.jeb.core.output.text.IAnchor;
import com.pnfsoftware.jeb.core.output.text.ILine;
import com.pnfsoftware.jeb.core.output.text.ITextDocumentPart;
import com.pnfsoftware.jeb.core.output.text.impl.Anchor;
import com.pnfsoftware.jeb.core.output.text.impl.Line;
import com.pnfsoftware.jeb.core.output.text.impl.TextItem;

/**
 * Part of the Javascript document
 * 
 * @author Cedric Lucas
 *
 */
public class JavascriptDocumentPart implements ITextDocumentPart {

    private TreeMap<Integer, Line> lines;

    /**
     * All lines are saved because each time document is changed, the {@link #getLines()} method is
     * called. If not buffered, it could raise performance issues
     */
    private List<Line> linesAsList;

    public JavascriptDocumentPart(TreeMap<Integer, String> stringLines) {
        this.lines = new TreeMap<>();
        for(Entry<Integer, String> entry: stringLines.entrySet()) {
            lines.put(entry.getKey(), new Line(entry.getValue()));
        }
    }

    @Override
    public List<? extends IAnchor> getAnchors() {
        return Arrays.asList(new Anchor(0, 0));
    }

    @Override
    public List<? extends ILine> getLines() {
        if(linesAsList == null || linesAsList.isEmpty()) {
            linesAsList = new ArrayList<Line>(lines.values());
        }
        return linesAsList;
    }

    /**
     * Add an {@link ItemClassIdentifiers} on a section
     */
    public void addItem(int absolutePosition, int length, ItemClassIdentifiers keyword) {
        addItem(absolutePosition, length, keyword, 0, false);
    }

    public void addItem(int absolutePosition, int length, ItemClassIdentifiers keyword, int itemId) {
        addItem(absolutePosition, length, keyword, itemId, false);
    }

    public void addItem(int absolutePosition, int length, ItemClassIdentifiers keyword, boolean master) {
        addItem(absolutePosition, length, keyword, master ? absolutePosition: 0, master);
    }

    public void addItem(int absolutePosition, int length, ItemClassIdentifiers keyword, int itemId, boolean master) {
        Entry<Integer, Line> line = lines.floorEntry(absolutePosition);
        int positionInLine = absolutePosition - line.getKey();
        line.getValue().addItem(
                new TextItem(positionInLine, length, keyword, itemId, master ? IActionableItem.ROLE_MASTER: 0));

    }

}
