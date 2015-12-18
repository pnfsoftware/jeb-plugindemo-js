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
import java.io.InputStreamReader;

import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

import com.pnfsoftware.jeb.core.IPluginInformation;
import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.Version;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

/**
 * Solution of the Tutorial 3 Exercice. The {@link #canIdentify(IInput, IUnitCreator)} method thy to
 * parse the file to check that it is a js.
 * 
 * @author Cedric Lucas
 *
 */
public class JavascriptPlugin extends AbstractUnitIdentifier {

    public JavascriptPlugin() {
        super("js", 0);
    }

    @Override
    public IPluginInformation getPluginInformation() {
        return new PluginInformation("JavaScript", "Javascript files", "PNF Software", Version.create(0, 1));
    }

    @Override
    public boolean canIdentify(IInput input, IUnitCreator parent) {
        // parse the javascript
        IRFactory factory = new IRFactory();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input.getStream()));
            AstRoot root = factory.parse(reader, null, 0);
            return root.hasChildren();
        }
        catch(Exception e) {
            return false;
        }
    }

    @Override
    public IUnit prepare(String name, IInput input, IUnitProcessor unitProcessor, IUnitCreator parent) {
        return new JavascriptUnit(name, input, unitProcessor, parent, pdm);
    }

}
