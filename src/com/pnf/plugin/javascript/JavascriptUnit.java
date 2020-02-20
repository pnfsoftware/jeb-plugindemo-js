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

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.StringLiteral;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.actions.ActionContext;
import com.pnfsoftware.jeb.core.actions.IActionData;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.input.IInputLocation;
import com.pnfsoftware.jeb.core.output.AbstractTransientUnitRepresentation;
import com.pnfsoftware.jeb.core.output.IGenericDocument;
import com.pnfsoftware.jeb.core.output.IUnitFormatter;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IInteractiveUnit;
import com.pnfsoftware.jeb.core.units.IMetadataManager;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.core.units.NotificationType;
import com.pnfsoftware.jeb.core.units.UnitNotification;

/**
 * {@link IUnit} used for processing JavaScript files. It beautifies the code, highlight important
 * keywords and raises notifications on user interactive keywords.
 * 
 * @author Cedric Lucas
 *
 */
public class JavascriptUnit extends AbstractBinaryUnit implements IInteractiveUnit {
    //private static final ILogger logger = GlobalLog.getLogger(JavascriptUnit.class);

    private AstRoot root = null;

    private TreeMap<Integer, String> lines;

    public JavascriptUnit(String name, IInput input, IUnitProcessor unitProcessor, IUnitCreator parent,
            IPropertyDefinitionManager pdm, AstRoot root) {
        super(null, input, "js", name, unitProcessor, parent, pdm);
        this.root = root;
    }

    private TreeMap<Integer, FunctionNode> functions;

    private TreeMap<Integer, StringLiteral> strings;

    @Override
    public boolean process() {
        try {
            updateLines(IOUtils.toString(getInput().getStream(), "UTF-8"));
        }
        catch(IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // add presentations to existing formatter
        IUnitFormatter formatter = super.getFormatter();
        formatter.addPresentation(new AbstractTransientUnitRepresentation("javascript", true) {
            @Override
            public IGenericDocument createDocument() {
                return new JavascriptDocument(JavascriptUnit.this);
            }
        }, false);

        setProcessed(true);
        return true;
    }

    private void updateLines(String text) {
        lines = new TreeMap<>();
        String textlines[] = text.split("\\n");
        int startOffset = 0;
        for(int i = 0; i < textlines.length; i++) {
            lines.put(startOffset, textlines[i]);
            startOffset += textlines[i].length() + 1; // CR
        }

        functions = new TreeMap<>();
        strings = new TreeMap<>();

        // Visit all the nodes to find notifications
        root.visit(new NodeVisitor() {
            @Override
            public boolean visit(AstNode node) {
                switch(node.getType()) {
                case Token.FUNCTION:
                    // saves address
                    functions.put(node.getAbsolutePosition(), (FunctionNode)node);
                    break;
                case Token.EXPR_VOID:
                case Token.EXPR_RESULT:
                    break;
                case Token.CALL:
                    visitTarget(((FunctionCall)node).getTarget());
                    break;
                case Token.STRING:
                    visitString((StringLiteral)node);
                    break;
                default:
                    break;
                }
                return true;
            }

            private void visitTarget(AstNode target) {
                if(target.getType() == Token.NAME) {
                    if(((Name)target).getIdentifier().equals("alert")) {
                        // Add notification
                        addNotification(new UnitNotification(NotificationType.POTENTIALLY_HARMFUL,
                                "alert is detected at position", Integer.toString(target.getAbsolutePosition())));
                    }
                }
            }

            private void visitString(StringLiteral node) {
                strings.put(node.getAbsolutePosition(), node);
            }

        });
    }

    public static AstRoot buildRoot(Reader r) throws IOException {
        IRFactory factory = new IRFactory();
        return factory.parse(r, null, 0);
    }

    public AstRoot getRoot() {
        return root;
    }

    public TreeMap<Integer, String> getLines() {
        return lines;
    }

    public TreeMap<Integer, FunctionNode> getFunctions() {
        return functions;
    }

    private <T extends AstNode> T getElementAt(String address, TreeMap<Integer, T> map) {
        if(address == null) {
            return null;
        }
        Integer intAddress = Integer.valueOf(address);
        Entry<Integer, T> entry = map.floorEntry(intAddress);
        if(entry == null) {
            // not in a function
            return null;
        }
        if(intAddress < entry.getValue().getAbsolutePosition() + entry.getValue().getLength()) {
            return entry.getValue();
        }
        return null;
    }

    @Override
    public String getAddressLabel(String address) {
        FunctionNode node = getElementAt(address, functions);
        if(node == null) {
            return null;
        }
        return node.getName();
    }

    @Override
    public Map<String, String> getAddressLabels() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getComment(String address) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean setComment(String address, String comment) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Map<String, String> getComments() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canExecuteAction(ActionContext actionContext) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean prepareExecution(ActionContext actionContext, IActionData actionData) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean executeAction(ActionContext actionContext, IActionData actionData) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean executeAction(ActionContext actionContext, IActionData actionData, boolean notify) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getAddressOfItem(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemAtAddress(String address) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Integer> getAddressActions(String address) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Integer> getGlobalActions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Integer> getItemActions(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getItemObject(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> getRelatedItems(long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IInputLocation addressToLocation(String address) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String locationToAddress(IInputLocation location) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IMetadataManager getMetadataManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isValidAddress(String address) {
        // TODO Auto-generated method stub
        return false;
    }

}
