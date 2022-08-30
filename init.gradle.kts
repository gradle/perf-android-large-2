/**
 * Analyses the project dependency graph to identify the the shallowest and deepest projects in the graph.
 * Uses the runtime classpath of each project.
 */
projectsEvaluated {
    rootProject {
        val target = project(":module21:module02")
        val reachable = reachable(target)
        println("found ${reachable.size} projects")
        for (proj in reachable.reversed()) {
            val maxDepth = proj.upstream.map { it.depth }.maxOrNull() ?: 0
            proj.depth = maxDepth + 1
            for (u in proj.upstream) {
                proj.reachableFrom.add(u)
                proj.reachableFrom.addAll(u.reachableFrom)
            }
        }
        println("max depth: ${reachable.maxByOrNull { it.depth }}")
        println("max reachable: ${reachable.maxByOrNull { it.reachableFrom.size }}")
        println("depth of 2: ${reachable.find { it.depth == 2 }}")
        println("reachable from 1: ${reachable.find { it.reachableFrom.size == 1 }}")
    }
}

fun reachable(target: Project): List<ProjDetails> {
    val allDetails = mutableMapOf<ProjNode, ProjDetails>()
    val reachable = mutableListOf<ProjDetails>()
    val seen = mutableSetOf<ProjNode>()
    val visited = mutableSetOf<ProjNode>()
    val queue = mutableListOf(ProjNode(target))
    while (queue.isNotEmpty()) {
        val proj = queue.first()
        if (seen.add(proj)) {
            val details = allDetails.getOrPut(proj) { ProjDetails(proj) }
            for (dep in dependenciesOf(proj)) {
                val depDetails = allDetails.getOrPut(dep) { ProjDetails(dep) }
                depDetails.upstream.add(details)
                queue.add(0, dep)
            }
        } else {
            if (visited.add(proj)) {
                reachable.add(allDetails.getValue(proj))
            }
            queue.removeFirst()
        }
    }
    return reachable
}

fun dependenciesOf(target: ProjNode): List<ProjNode> {
    val deps = mutableListOf<ProjNode>()
    var config = target.project.configurations.findByName("debugRuntimeClasspath")
    if (config == null) {
        config = target.project.configurations.findByName("runtimeClasspath")
        if (config == null) {
            throw RuntimeException("could not find config in ${target.project.path}")
        }
    }
    for (dep in config.allDependencies) {
        if (dep is ProjectDependency) {
            deps.add(ProjNode(dep.dependencyProject))
        }
    }
    return deps
}

data class ProjNode(val project: Project)

data class ProjDetails(val project: ProjNode) {
    val upstream = mutableSetOf<ProjDetails>()
    var depth = 1
    val reachableFrom = mutableSetOf<ProjDetails>()

    override fun toString(): String {
        return "$project.project.path -> depth: $depth, reachableFrom: ${reachableFrom.size}"
    }
}
