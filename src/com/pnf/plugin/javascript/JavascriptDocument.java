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
import java.util.List;

import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;

import com.pnfsoftware.jeb.core.output.ItemClassIdentifiers;
import com.pnfsoftware.jeb.core.output.text.ITextDocumentPart;
import com.pnfsoftware.jeb.core.output.text.impl.AbstractTextDocument;
import com.pnfsoftware.jeb.util.format.Strings;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

/**
 * Document that highligts js keywords.
 * 
 * @author Cedric Lucas
 *
 */
public class JavascriptDocument extends AbstractTextDocument {
    private static final ILogger logger = GlobalLog.getLogger(JavascriptDocument.class);

    // 1 indent equals to 2 spaces
    private static final int INDENT_SIZE = 2;

    private JavascriptDocumentPart out;

    /**
     * @param root
     */
    public JavascriptDocument(AstRoot root) {
        out = new JavascriptDocumentPart();
        generateWholePart(root);
    }

    private void generateWholePart(AstRoot root) {
        displayStatements(root.getStatements(), 0);
    }

    private void displayStatements(List<AstNode> statements, int indent) {
        for(AstNode statement: statements) {
            displayStatement(statement, indent);
        }
    }

    private void displayStatement(AstNode statement, int indent) {
        switch(statement.getType()) {
        case Token.FUNCTION:
            displayFunction((FunctionNode)statement, indent);
            break;
        case Token.EXPR_VOID:
        case Token.EXPR_RESULT:
            displayExpressionStatement((ExpressionStatement)statement, indent);
            break;
        case Token.VAR:
            // TODO
            //displayVariableStatement((VariableDeclaration)statement, indent);
            //break;
        default:
            // don't know how to manage it: we will display default text
            out.append(statement.toSource(indent * INDENT_SIZE));
            break;
        }
    }

    private void displayExpressionStatement(ExpressionStatement statement, int indent) {
        out.append(statement.toSource(indent * INDENT_SIZE));
    }

    private void displayFunction(FunctionNode functionNode, int indent) {
        out.append(functionNode.makeIndent(indent * INDENT_SIZE));
        out.append("function", ItemClassIdentifiers.KEYWORD);
        out.space();
        out.append(functionNode.getName(), ItemClassIdentifiers.METHOD_NAME);
        out.append("(" + displayFunctionParameters(functionNode) + ")");
        displayBody(functionNode.getBody(), indent);
    }

    private void displayBody(AstNode body, int indent) {
        if(body.getType() == Token.BLOCK) {
            // block surrounded by {}
            out.append(" {");
            out.newLine();
            displayNode(body, indent + 1);
            out.newLine();
            out.append(body.makeIndent(indent * INDENT_SIZE));
            out.append("}");
            out.newLine();
        }
        else {
            out.newLine();
            displayNode(body, indent + 1);
            out.newLine();
        }
    }

    private void displayNode(Node statement, int indent) {
        Node node = statement.getFirstChild();
        if(node == null) {
            return;
        }
        do {
            if(statement instanceof AstNode) {
                displayStatement((AstNode)node, indent);
            }
            else {
                logger.error("unknown node %s", node.getClass().toString());
                displayNode(node, indent);
            }
            node = statement.getNext();
        }
        while(node != null);
    }

    private String displayFunctionParameters(FunctionNode functionNode) {
        List<String> parameters = new ArrayList<>();
        for(AstNode node: functionNode.getParams()) {
            if(node instanceof Name) {
                parameters.add(((Name)node).getIdentifier());
            }
        }
        return Strings.join(", ", parameters);
    }

    @Override
    public long getAnchorCount() {
        return 1;
    }

    @Override
    public ITextDocumentPart getDocumentPart(long anchorId, int linesAfter, int linesBefore) {
        return out;
    }

}
