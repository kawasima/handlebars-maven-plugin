package net.unit8.maven.plugins.handlebars;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Options extends ScriptableObject {

    public KnownHelpers knowHelpers;
    public Boolean knownHelpersOnly;

    public Options(KnownHelpers knownHelpers, Boolean knownHelpersOnly) {
        this.knowHelpers = knownHelpers;
        this.knownHelpersOnly = knownHelpersOnly;
    }

    @Override
    public String getClassName() {
        return "HandlebarsMavenPluginOptions";
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (name.equals("knownHelpers")) {
            return knowHelpers;
        } else if (name.equals("knownHelpersOnly")) {
            return knownHelpersOnly;
        }

        return super.get(name, start);
    }
}
