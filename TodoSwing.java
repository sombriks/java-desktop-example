///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.formdev:flatlaf:3.5.4
//DEPS com.formdev:flatlaf-extras:3.5.4
//SOURCES app/**/*.java
//JAVA 25+

import app.core.TodoManager;
import static app.ui.SwingApp.createApp;

void main(String... args) {
    createApp(new TodoManager());
}