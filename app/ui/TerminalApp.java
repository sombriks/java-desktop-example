package app.ui;

//DEPS dev.tamboui:tamboui-toolkit:LATEST
//DEPS dev.tamboui:tamboui-jline3-backend:LATEST

import app.core.TodoManager;
import dev.tamboui.toolkit.app.ToolkitApp;
import dev.tamboui.toolkit.element.Element;

import static dev.tamboui.toolkit.Toolkit.*;

public class TerminalApp extends ToolkitApp {

    @Override
    protected Element render() {
        return panel(" My Todo App ",
                row(
                        panel(
                                column(
                                        text("[ Type to filter or Create ]").dim(),
                                        spacer(),
                                        row(text("Basic"), spacer(), text("(5)")),
                                        row(text("General"), spacer(), text("(2)")),
                                        row(text("Groceries"), spacer(), text("(3)")),
                                        row(text("Important"), spacer(), text("(1)"))
                                ).margin(1)
                        ),
                        panel(
                                column(
                                        text("[ Type to filter or create ]").dim(),
                                        spacer(),
                                        row(text("[ ] Review monthly report")),
                                        row(text("[X] Workout").cyan()),
                                        row(text("[ ] Grocery shopping")),
                                        spacer()
                                ).margin(1)
                        )

                )

        ).doubleBorder();
    }

    public static void createApp(TodoManager todoManager) throws Exception {
        new TerminalApp().run();
    }
}
