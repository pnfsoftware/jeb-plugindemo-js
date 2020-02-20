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
import java.util.Arrays;
import java.util.List;

import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.input.BytesInput;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.output.AbstractTransientUnitRepresentation;
import com.pnfsoftware.jeb.core.output.IGenericDocument;
import com.pnfsoftware.jeb.core.output.IUnitFormatter;
import com.pnfsoftware.jeb.core.output.UnitFormatterAdapter;
import com.pnfsoftware.jeb.core.output.table.ITableDocument;
import com.pnfsoftware.jeb.core.output.table.impl.Cell;
import com.pnfsoftware.jeb.core.output.table.impl.StaticTableDocument;
import com.pnfsoftware.jeb.core.output.table.impl.TableRow;
import com.pnfsoftware.jeb.core.output.text.impl.AsciiDocument;
import com.pnfsoftware.jeb.core.output.tree.ITreeDocument;
import com.pnfsoftware.jeb.core.output.tree.impl.Node;
import com.pnfsoftware.jeb.core.output.tree.impl.StaticTreeDocument;
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

    private List<FunctionNode> functions = new ArrayList<>();

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

        // avoid reprocessing the file
        setProcessed(true);
        return true;
    }

    @Override
    public IUnitFormatter getFormatter() {
        UnitFormatterAdapter adapter = new UnitFormatterAdapter(
                new AbstractTransientUnitRepresentation("javascript raw code", true) {
            @Override
                    public IGenericDocument createDocument() {
                return new AsciiDocument(getInput());
            }
        });
        // Display functions as tab
        //        if(functions != null) {
        //            for(final FunctionNode function: functions) {
        //                adapter.addDocumentPresentation(new AbstractUnitRepresentation(function.getName()) {
        //                    @Override
        //                    public IGenericDocument getDocument() {
        //                        return new AsciiDocument(new BytesInput(function.toSource().getBytes()));
        //                    }
        //                });
        //            }
        //        }

        // Displays the Statistics table
        adapter.addDocumentPresentation(new AbstractTransientUnitRepresentation("Statistics") {
            @Override
            public IGenericDocument createDocument() {
                return getStatisticsTable();
            }
        });

        // Display js as tree
        adapter.addDocumentPresentation(new AbstractTransientUnitRepresentation("Functions") {
            @Override
            public IGenericDocument createDocument() {
                return getFunctionsTree();
            }
        });

        return adapter;
    }

    private ITableDocument getStatisticsTable() {
        List<TableRow> rows = new ArrayList<>();
        rows.add(new TableRow(new Cell("Length"), new Cell(Integer.toString(root.getLength()))));
        rows.add(new TableRow(new Cell("Type"), new Cell(Integer.toString(root.getType()))));
        return new StaticTableDocument(Arrays.asList("Property", "Value"), rows);
    }

    private ITreeDocument getFunctionsTree() {
        List<Node> treeRoot = new ArrayList<>();
        try {
            if(functions != null) {
                for(FunctionNode function: functions) {
                    treeRoot.add(buildFunctionNode(function));
                }
            }
        }
        catch(Exception e) {
            logger.catching(e);
        }
        return new StaticTreeDocument(treeRoot);
    }

    private Node buildFunctionNode(FunctionNode function) {
        Node functionNode = new Node(function.getName());
        if(function.getParamCount() > 0) {
            for(AstNode var: function.getParams()) {
                functionNode.addChild(new Node(((Name)var).getIdentifier()));
            }
        }
        return functionNode;
    }
}
