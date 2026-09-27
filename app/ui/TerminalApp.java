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
        return panel("My App",
                text("Hello!").bold().cyan(),
                spacer(),
                text("Press 'q' to quit").dim()
        ).rounded();
    }

    public static void createApp(TodoManager todoManager) throws Exception{
        new TerminalApp().run();
    }
}
