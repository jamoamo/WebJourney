package io.github.jamoamo.webjourney.api.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResolvedBrowserArguments 
{
    private final List<String> arguments = new ArrayList<>();
    private final List<ProvenancedArgument> provenance = new ArrayList<>();

    public ResolvedBrowserArguments(List<String> arguments, List<ProvenancedArgument> provenance)
    {
        if(arguments != null)
        {
            this.arguments.addAll(arguments);
        }
        if(provenance != null)
        {
            this.provenance.addAll(provenance);
        }
    }

    public List<String> getArguments()
    {
        return Collections.unmodifiableList(this.arguments);
    }

    public List<ProvenancedArgument> getProvenance()
    {
        return Collections.unmodifiableList(this.provenance);
    }
}
