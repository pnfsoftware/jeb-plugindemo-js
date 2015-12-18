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

import com.pnfsoftware.jeb.core.IPluginInformation;
import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.Version;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

/**
 * A Sample plugin that check for a #Javascript header and parse javascript code under this tag
 * 
 * @author Cedric Lucas
 *
 */
public class SamplePlugin extends AbstractUnitIdentifier {

    public SamplePlugin() {
        super("hashJavascript", 0);
    }

    @Override
    public IPluginInformation getPluginInformation() {
        return new PluginInformation("hashJavascript", "#Javascript containing JS code", "PNF Software",
                Version.create(0, 1));
    }

    private final static byte[] JS_HEADER = "#Javascript".getBytes();

    @Override
    public boolean canIdentify(IInput input, IUnitCreator parent) {
        return checkBytes(input, 0, JS_HEADER);
    }

    @Override
    public IUnit prepare(String name, IInput input, IUnitProcessor unitProcessor, IUnitCreator parent) {
        IUnit sampleUnit = new SampleUnit(name, input, unitProcessor, parent, pdm);
        sampleUnit.process(); // forces children calculation
        return sampleUnit;
    }
}
