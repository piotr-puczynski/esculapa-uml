![http://esculapa-uml.eclipselabs.org.codespot.com/git/web_resources/Esculapa_256.gif](http://esculapa-uml.eclipselabs.org.codespot.com/git/web_resources/Esculapa_256.gif)
# What is it? #
EsculapaUML is a consistency checking tool based on EMF and UML2. It is designed to check models used for model-driven architecture. It can extend the sequence diagrams representing Use Cases to scenarios based on the existing model. It can also validate that interaction diagrams are consistent with a model.

See presentation [here](http://docs.google.com/viewer?url=http%3A%2F%2Fesculapa-uml.eclipselabs.org.codespot.com%2Fgit%2Fweb_resources%2Fpresentation.pdf).

# For who is it? #
The tool is targeted to academia as educational software for students of Software Engineering courses willing to investigate their models and to get an insight in how different views on the model relate to each other.

# How it works? #
EsculapaUML works by executing UML model and checking it consistency at run-time. The tool bases its execution on tests (use cases scenarios specified as sequence diagrams) that are executed in given model (including set of instances in the same package where the sequence diagram is).

# What elements are checked? #
Our tool focuses on checking that tests are realizable in given model. This includes checking sequence diagrams with respect to state machines (incl. behavioral and protocol state machines). The protocol state machines defined in context of interfaces are also checked. We support OCL evaluation and effects in Simple Action Language (SAL - our invention).

# Supported Graphical Interface #
We currently support **Topcased** as a platform for modeling. EcsulapaUML integrates into Topcased interface so that you can see the results of checking in the diagrams. Independently, EsculapaUML also supports raw UML2 models.

# Update Site #
`https://esculapa-uml.eclipselabs.org.codespot.com/git/update_site/`

## Details: How to download / install? ##

Help with installation (wiki page): http://code.google.com/a/eclipselabs.org/p/esculapa-uml/wiki/Installation