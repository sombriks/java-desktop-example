# The state of java desktop

Back in the _Old Days_, it was just a mess. Now **jpackage** exists.

## Local-First is a thing again

Not everything needs to be in "the cloud".

Use local resources wisely and get the best possible from internet resources.

### Local but evergreen

Being locally installed does not mean to be terminally outdated. See how 
browsers do, how [steam client][steam] this thing so well that it's easy to 
forget that it was installed, not a page in a browser.

[steam]: https://store.steampowered.com

## But what makes desktop java a good idea?

- More than twenty years of solid libraries, documentation and compatibility
- Compatibility with major desktops
- Modern tools like [jbang][jbang]
- There is [jpackage][jpackage] now

[jbang]: https://jbang.dev
[jpackage]: https://dev.java/learn/jvm/other-tools/jpackage/

### JPackage, what's the deal?

Now you create a real, first-class installer for your application. And the
distributed installer doesn't need anything else in the target machine. All
required items goes bundled with it.

All you need is a few command lines.

## Fine, installers are easy bow, so what?

So, let's make a simple desktop app!

Something like this:

```
╔═══════════════════════════════════════════════════════╗
║ [=] My Todo App                                       ║
╠══════════════════════╦════════════════════════════════╣
║                      ║                                ║
║ [ Filter or Create ] ║ [ Type to filter or create   ] ║
║                      ║                                ║
║ Basic            (5) ║ [ ] Review monthly report      ║
║ General          (2) ║ [X] Workout                    ║
║ Groceries        (3) ║ [ ] Grocery shopping           ║
║ Important        (1) ║                                ║
║                      ║                                ║
╚══════════════════════╩════════════════════════════════╝
```
