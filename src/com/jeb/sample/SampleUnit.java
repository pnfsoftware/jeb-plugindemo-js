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
package com.jeb.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.input.BytesInput;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.output.AbstractUnitRepresentation;
import com.pnfsoftware.jeb.core.output.IGenericDocument;
import com.pnfsoftware.jeb.core.output.IUnitFormatter;
import com.pnfsoftware.jeb.core.output.UnitFormatterAdapter;
import com.pnfsoftware.jeb.core.output.text.impl.AsciiDocument;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

/**
 * The sample unit that manages #Javascript files.
 * 
 * @author Cedric Lucas
 *
 */
public class SampleUnit extends AbstractBinaryUnit {
    private static final ILogger logger = GlobalLog.getLogger(SampleUnit.class);

    public SampleUnit(String name, IInput input, IUnitProcessor unitProcessor, IUnitCreator parent,
            IPropertyDefinitionManager pdm) {
        super(null, input, "hashJavascript", name, unitProcessor, parent, pdm);
    }

    private AstRoot root = null;

    private List<FunctionNode> functions = new ArrayList<FunctionNode>();

    @Override
    public boolean process() {
        // parse the javascript
        IRFactory factory = new IRFactory();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getInput().getStream()));
            reader.readLine(); // ignore first line with #Javascript
            root = factory.parse(reader, null, 0);
        }
        catch(IOException e) {
            logger.catching(e);
            return false;
        }

        // save functions
        List<AstNode> statements = root.getStatements();
        for(AstNode statement: statements) {
            if(statement.getType() == Token.FUNCTION) {
                FunctionNode function = (FunctionNode)statement;
                functions.add(function);
                IUnit jsUnit = getUnitProcessor().process(function.getName(),
                        new BytesInput(function.toSource().getBytes()), this);
                if(jsUnit != null) {
                    addChildUnit(jsUnit);
                }
            }
        }
        setProcessed(true);
        return true;
    }

    @Override
    public IUnitFormatter getFormatter() {
        UnitFormatterAdapter adapter = new UnitFormatterAdapter(new AbstractUnitRepresentation("javascript raw code",
                true) {
            @Override
            public IGenericDocument getDocument() {
                return new AsciiDocument(getInput());
            }
        });
        if(functions != null) {
            for(final FunctionNode function: functions) {
                adapter.addDocumentPresentation(new AbstractUnitRepresentation(function.getName()) {
                    @Override
                    public IGenericDocument getDocument() {
                        return new AsciiDocument(new BytesInput(function.toSource().getBytes()));
                    }
                });
            }
        }
        return adapter;
    }

}
