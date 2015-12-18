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

import com.pnfsoftware.jeb.core.output.ItemClassIdentifiers;
import com.pnfsoftware.jeb.core.output.text.IAnchor;
import com.pnfsoftware.jeb.core.output.text.ILine;
import com.pnfsoftware.jeb.core.output.text.ITextDocumentPart;
import com.pnfsoftware.jeb.core.output.text.impl.Anchor;
import com.pnfsoftware.jeb.core.output.text.impl.Line;
import com.pnfsoftware.jeb.core.output.text.impl.TextItem;

/**
 * Document part which stores the lines and the scific highlights (items)
 * 
 * @author Cedric Lucas
 *
 */
public class JavascriptDocumentPart implements ITextDocumentPart {

    List<Line> lines = new ArrayList<Line>();
    List<TextItem> items = new ArrayList<TextItem>();

    StringBuilder currentLine = new StringBuilder();

    @Override
    public List<? extends IAnchor> getAnchors() {
        return Arrays.asList(new Anchor(0, 0));
    }

    @Override
    public List<? extends ILine> getLines() {
        return lines;
    }

    public void append(String text) {
        String textlines[] = text.split("\\r?\\n");
        currentLine.append(textlines[0]);
        for(int i = 1; i < textlines.length; i++) {
            newLine();
            currentLine.append(textlines[i]);
        }
    }

    public void newLine() {
        lines.add(new Line(currentLine, items));
        currentLine = new StringBuilder();
        items = new ArrayList<TextItem>();
    }

    public void space() {
        currentLine.append(" ");
    }

    public void append(String text, ItemClassIdentifiers keyword) {
        if(text.contains("\\r") || text.contains("\\n")) {
            throw new IllegalArgumentException("Keyword can not contain CR");
        }
        int start = currentLine.length();
        currentLine.append(text);
        items.add(new TextItem(start, text.length(), keyword));

    }
}
