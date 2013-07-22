package net.unit8.maven.plugins.handlebars;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import sun.font.TrueTypeFont;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class KnownHelpers extends ScriptableObject {

    private Set<String> knownHelpers = new HashSet<String>();

    public KnownHelpers(Collection<String> knownHelpers) {
        this.knownHelpers.addAll(knownHelpers);
    }

    @Override
    public String getClassName() {
        return "KnownHelpers";
    }

    @Override
    public Object get(String name, Scriptable start) {
        return knownHelpers.contains(name);
    }
}
