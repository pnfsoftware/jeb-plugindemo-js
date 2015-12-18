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

import java.io.InputStreamReader;

import org.mozilla.javascript.ast.AstRoot;

import com.pnfsoftware.jeb.core.IPlugin;
import com.pnfsoftware.jeb.core.IPluginInformation;
import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.Version;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

/**
 * {@link IPlugin} that process JavaScript files
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

    private AstRoot root = null;

    @Override
    public boolean canIdentify(IInput input, IUnitCreator parent) {
        // parse the javascript
        try {
            root = JavascriptUnit.buildRoot(new InputStreamReader(input.getStream()));
            return root.hasChildren();
        }
        catch(Exception e) {
            return false;
        }
    }

    @Override
    public IUnit prepare(String name, IInput input, IUnitProcessor unitProcessor, IUnitCreator parent) {
        IUnit jsUnit = new JavascriptUnit(name, input, unitProcessor, parent, pdm, root);
        jsUnit.process();
        return jsUnit;
    }

}
