# Runtime Server Protocol - MyType Extension

[![RSP+Community+CI](https://img.shields.io/github/workflow/status/redhat-developer/rsp-server-community/RSP%20Community%20CI)](https://github.com/redhat-developer/rsp-server-community/actions)
[![License](https://img.shields.io/badge/license-EPLv2.0-brightgreen.svg)](https://github.com/redhat-developer/rsp-server-community/blob/master/README.md)
[![Visual Studio Marketplace](https://vsmarketplacebadge.apphb.com/version/redhat.vscode-server-connector.svg)](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-community-server-connector)
[![Gitter](https://badges.gitter.im/redhat-developer/server-connector.svg)](https://gitter.im/redhat-developer/server-connector?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)


## Summary

This repository is a home for an RSP server, and the associated VSCode Extension, which can start, stop, and otherwise control MyType runtimes and servers.  This is an example repository meant to show how one might make use of the generic server framework or implement custom actions. 

This repository includes a single server type, defined primarily through the generic server json format. However, the server does manually instantiate its own delegate class and does override the logic for actions, so as to demonstrate some of the options with custom actions and what the back end can ask the UI to do. Specifically, this demonstrates a multi-step action that makes several prompts, and in the end either runs a background task, asks the UI to open an editor, asks the UI to open a browser to a given url, or asks the UI to open a terminal running a given command. 

Important: When trying to use this server type to create a new server and test out the functionality, you must point it to a tomcat 10.x instance. 

The protocol is based on LSP4J. In short, the base protocol is the same as LSP, but the specification of the messages is different. 

The base protocol of LSP can be found [here](https://microsoft.github.io/language-server-protocol/specification). 
The RSP Extensions to the base protocol can be found [here](https://github.com/redhat-developer/rsp-server/blob/master/api/docs/org.jboss.tools.rsp.schema/src/main/resources/schemaMD/specification.md)


## Commands and features

This extension depends on VSCode RSP UI Extension which is going to be installed automatically along with this Extension. RSP UI in conjuction with this Extension supports a number of commands for interacting with supported server adapters; these are accessible via the command menu (`Cmd+Shift+P` on macOS or `Ctrl+Shift+P` on Windows and Linux) and may be bound to keys in the normal way.




## Building this server and extension

Run the following code:

    # First, build the server
    git clone https://github.com/robstryker/rsp-server-custom-vsc-extension
    cd rsp-server-custom-extension/rsp
    mvn clean install
    cd ../

    # Now build the extension
    cd vscode/

    #Build this extension's code
    npm install
    npm run build
    npm run test
    vsce package


