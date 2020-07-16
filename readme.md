Main app is :module21:module02 which has ~1,400 modules in its dependency graph.

Running ./gradlew assembleDebug fails (or is very, very slow and I didn't get it to finish)
because the task graph is too big for the 15GB given to the daemon. Tons of GC pauses when
connecting yourkit.