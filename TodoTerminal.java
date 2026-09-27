///usr/bin/env jbang "$0" "$@" ; exit $?
//SOURCES app/**/*.java
//JAVA 25+

import app.core.TodoManager;

import static app.ui.TerminalApp.createApp;

void main(String... args) throws Exception {
    createApp(new TodoManager());
}
