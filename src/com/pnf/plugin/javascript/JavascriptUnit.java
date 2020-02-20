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

import org.mozilla.javascript.ast.AstRoot;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.output.AbstractTransientUnitRepresentation;
import com.pnfsoftware.jeb.core.output.IGenericDocument;
import com.pnfsoftware.jeb.core.output.IUnitFormatter;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

/**
 * Simple Javascript Display
 * 
 * @author Cedric Lucas
 */
public class JavascriptUnit extends AbstractBinaryUnit {

    private AstRoot root = null;

    public JavascriptUnit(String name, IInput input, IUnitProcessor unitProcessor, IUnitCreator parent,
            IPropertyDefinitionManager pdm, AstRoot root) {
        super(null, input, "js", name, unitProcessor, parent, pdm);
        this.root = root;
    }

    @Override
    public boolean process() {
        // add presentations to existing formatter
        IUnitFormatter formatter = super.getFormatter();
        formatter.addPresentation(new AbstractTransientUnitRepresentation("javascript", true) {
            @Override
            public IGenericDocument createDocument() {
                return new JavascriptDocument(root);
            }
        }, false);
        setProcessed(true);
        return true;
    }
}
