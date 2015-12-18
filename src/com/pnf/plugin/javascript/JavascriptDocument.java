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

import java.util.List;
import java.util.TreeMap;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.VariableDeclaration;
import org.mozilla.javascript.ast.VariableInitializer;

import com.pnf.plugin.javascript.AddressReferences.Position;
import com.pnfsoftware.jeb.core.events.J;
import com.pnfsoftware.jeb.core.output.ItemClassIdentifiers;
import com.pnfsoftware.jeb.core.output.text.ICoordinates;
import com.pnfsoftware.jeb.core.output.text.ITextDocumentPart;
import com.pnfsoftware.jeb.core.output.text.impl.AbstractTextDocument;
import com.pnfsoftware.jeb.core.output.text.impl.Coordinates;
import com.pnfsoftware.jeb.util.events.IEvent;
import com.pnfsoftware.jeb.util.events.IEventListener;

/**
 * 
 * Default Javascript document that beautifies JS by rebuilding complete String from {@link AstRoot}
 * . It also highlight JS keywords.
 * 
 * TODO missing implementations are render as raw text
 * 
 * @author Cedric Lucas
 *
 */
public class JavascriptDocument extends AbstractTextDocument implements IEventListener {

    private JavascriptDocumentPart out;

    private JavascriptUnit unit;

    public JavascriptDocument(JavascriptUnit unit) {
        this.unit = unit;
        unit.addListener(this);
        refreshPart();
    }

    private void refreshPart() {
        out = new JavascriptDocumentPart(unit.getLines());
        visitRoot(unit.getRoot());
    }

    /**
     * Add {@link ItemClassIdentifiers} to some keywords
     */
    private void visitRoot(AstRoot root) {
        // Visit all the nodes to find notifications
        root.visitAll(new NodeVisitor() {
            @Override
            public boolean visit(AstNode node) {
                switch(node.getType()) {
                case Token.FUNCTION:
                    displayFunction((FunctionNode)node);
                    break;
                case Token.EXPR_VOID:
                case Token.EXPR_RESULT:
                    break;
                case Token.CALL:
                    displayCallStatement((FunctionCall)node);
                    break;
                case Token.VAR:
                case Token.CONST:
                    if(node instanceof VariableDeclaration) {
                        displayVariableStatement((VariableDeclaration)node);
                    }
                    // TODO else VariableInitializer
                    break;
                case Token.STRING:
                    displayStringStatement((StringLiteral)node);
                    break;
                case Token.NAME:
                    //out.append(((Name)node).getIdentifier());
                    break;
                default:
                    // nothing to do
                    break;
                }
                return true;
            }

        });
    }

    private void displayVariableStatement(VariableDeclaration statement) {
        List<VariableInitializer> variables = statement.getVariables();
        for(int i = 0; i < variables.size(); i++) {
            VariableInitializer init = variables.get(i);
            if(i == 0) {
                out.addItem(statement.getAbsolutePosition(), "var".length(), ItemClassIdentifiers.KEYWORD);
            }
            Name node = (Name)init.getTarget();
            out.addItem(node.getAbsolutePosition(), node.getIdentifier().length(), ItemClassIdentifiers.IDENTIFIER);
        }
    }

    private void displayStringStatement(StringLiteral statement) {
        out.addItem(statement.getAbsolutePosition(), statement.getLength(), ItemClassIdentifiers.STRING);
    }

    private void displayFunction(FunctionNode functionNode) {
        out.addItem(functionNode.getAbsolutePosition(), "function".length(), ItemClassIdentifiers.KEYWORD);
        Name functionName = functionNode.getFunctionName();
        if(functionName != null) {
            out.addItem(functionName.getAbsolutePosition(), functionName.getLength(), ItemClassIdentifiers.METHOD_NAME,
                    true);
        }
    }

    private void displayCallStatement(FunctionCall node) {
        String functionName = node.getTarget().toSource();
        FunctionNode function = getFunctionNode(functionName);
        if(function != null) {
            out.addItem(node.getTarget().getAbsolutePosition(), functionName.length(),
                    ItemClassIdentifiers.METHOD_NAME, function.getAbsolutePosition());
        }

    }

    @Override
    public long getAnchorCount() {
        return 1;
    }

    @Override
    public ITextDocumentPart getDocumentPart(long anchorId, int linesAfter) {
        return getDocumentPart(anchorId, linesAfter, 0);
    }

    @Override
    public ITextDocumentPart getDocumentPart(long anchorId, int linesAfter, int linesBefore) {
        return out;
    }

    @Override
    public void dispose() {
    }

    private FunctionNode getFunctionNode(String name) {
        if(name == null) {
            return null;
        }
        TreeMap<Integer, FunctionNode> functions = unit.getFunctions();
        for(FunctionNode node: functions.values()) {
            if(name.equals(node.getName())) {
                return node;
            }
        }
        return null;
    }

    @Override
    public ICoordinates addressToCoordinates(String address) {
        if(address == null) {
            return null;
        }
        Integer intAddress = null;
        try {
            intAddress = Integer.parseInt(address);
        }
        catch(NumberFormatException e) {
            // User input
            // Here we can manage function jump for example
            FunctionNode node = getFunctionNode(address);
            if(node == null) {
                return null;
            }
            intAddress = node.getAbsolutePosition();
        }

        Position p = AddressReferences.getPosition(unit.getLines(), intAddress);
        if(p == null) {
            return null;
        }
        return new Coordinates(0, p.getLine(), p.getOffsetAtLine());
    }

    @Override
    public String coordinatesToAddress(ICoordinates coordinates) {
        return AddressReferences.getAbsolutePosition(unit.getLines(), coordinates.getLineDelta(),
                coordinates.getColumnOffset());
    }

    @Override
    public void onEvent(IEvent e) {
        if(e.getType() == J.UnitChange) {
            refreshPart();
            this.notifyListeners(e);
        }
    }

}
