################################################################################
Current dependencies:
- src/main, src/build, src/samples:
  - Java 5+
- src/test:
  - Java 8+
  - JUnit 3.8.1 (under lib/junit.jar)
################################################################################
Jadecy x.x, xxxx/xx/xx

Changes since version 1.1:

- Added handling of packages and classes names with dollar signs (other than
  added by compiler before nested classes), as follows:
  - Top level classes names are now properly computed even if the package name
    contains a dollar sign.
  - Classes names minus package, starting or ending with a dollar sign (like
    "$" or "A$B$"), or containing two consecutive dollar signs (like "A$$B"),
    are considered to be top level classes names.
    For example, a nested class "B" in a top level class "$A" will be
    considered to be the top level class "$A$B", but will still have (at least)
    non-API dependency to its outer class "$A", even though
    ClassData.outerClassData() will return null for "$A$B".
  - Other ("regular") classes names minus package, will only be considered top
    level classes if they contain no dollar sign.
    For example, a top level class "A$B" will be considered to be the nested
    class "B" of a top level class "A", which will therefore have a ClassData
    created but with no dependency (as for any depended on but non parsed
    class).
  Note that as before, considered classes names are those read from within the
  class files, whatever the names or paths of these class files.

- Simplified some Comparable/Comparator implementations.

- Added SplitPackageSample.

################################################################################
Jadecy 1.1, 2016/01/02

Changes since version 1.0:

- Added ShortestCyclesComputer, which computes shortest cycles covering all
  edges of each SCC.
  It uses the cycle detection algorithm described in the paper "Efficient
  Retrieval and Ranking of Undesired Package Cycles in Large Software Systems",
  by Jannik Laval, Jean-Remy Falleri, Philippe Vismara, and Stephane Ducasse
  (cf. http://www.jot.fm/issues/issue_2012_04/article4.pdf),
  modulo some optimizations that can greatly speed things up, for example in
  case of a single but long cycle, and usually reduce the amount of cycles found
  for covering SCCs edges.
  This new algorithm makes SomeCyclesComputer, and maybe also CyclesComputer,
  mostly obsolete in practice for hunting down cycles, since it's both fast and
  covers all edges of SCCs.

- Added Jadecy.computeShortestCycles(...).

- Added DepUnit.checkShortestCycles(...), for user not having to explicitly
  allow all actual acceptable cycles as with checkCycles(...) method.

- For JadecyMain, added -scycles computation (for shortest cycles),
  and -minsize option for SCCs and cycles.
  
- Added JadecyMain.runArgs(...), for flexible programmatic usage of JadecyMain.

- Added JadecyMainSample and sample_scycles.bat.

- Added tracking of src/build with git.

- Various javadoc, internal code and comments cleanups and improvements that
  are not really worth mentioning.

################################################################################
Jadecy 1.0, 2015/12/13

Jadecy stands for Java Dependencies and Cycles computer.

- Why:
  One of my projects grew so big that I could no longer mentally keep track of
  its structure and dependencies, and decided to create unit tests to check
  them. Not finding a suitable library for that, I made this one.

- License: Apache License V2.0.

- Overview:
  Jadecy provides treatments to compute dependencies, strongly connected
  components, and cycles, in classes or packages dependencies graphs parsed from
  class files, either through API or through command line.
  Lower level treatments, such as graph related computations, are also publicly
  available.
  Historical note: It's an evolution of http://sourceforge.net/projects/jcycles.
  Principal classes:
  - Jadecy: The entry point.
  - DepUnit: for cycles and dependencies unit tests.
  - JadecyMain: Allows for a simple usage of Jadecy from command line.

- Main sources require Java 1.5 or later, tests sources require Java 1.8
  or later for testing higher versions features.

- Handles class files of major version <= 52 (Java 8), and does best effort
  if major version is higher.
  Could compute dependencies out of JDK 9 ea build 95 class files.

- Cannot parse .jimage files, which would require dependency to jrt-fs.jar,
  which itself requires Java 8, and is not trivial to use
  (FileSystemNotFoundException is easy to get), but their content can easily be
  extracted into class files using jimage executable.

- Uses Tarjan's algorithm for SCCs computation, and Johnson's algorithm
  for cycles computation, with continuations instead of recursion, which allows
  to handle large graphs (< 2^31 vertices).

- To avoid spam due to dependencies within a same top level class and its
  recursively nested classes, cycles and SCCs only involving such classes are
  always ignored by Jadecy class treatments.

- Lower level APIs such as for class files parsing, classes and packages trees
  and graphs manipulation, or graph related computations, are also publicly
  available (cf. overview below), and can easily be extracted (sources contain
  no cyclic dependencies, except deliberate ones in tests).

- As a general rule, for convenience, these treatments ensure determinism and
  when possible some ordering in their result.
 
- Sample Jadecy class usage can be found in src/samples/java,
  and more use cases in tests.

- Sample command line usages with .bat files (based on that, UX or MAC users
  should easily be able to do their own scripts) can be found in
  src/samples/scripts, and more use cases in tests.

- Javadoc: Code Javadoc is done in plain text, not using HTML, for simplicity
  and easier writing and reading when working with source code.

- Main sources packages overview:

  - net.jadecy: Contains treatments to compute dependencies, strongly connected
    components, and cycles, in classes or packages dependencies graphs parsed
    from class files.
    Principal classes:
    - Jadecy: The entry point.
    - JadecyUtils: Utilities for dealing with Jadecy, in particular its results.
    - DepUnit: To check dependencies and cycles in unit tests.

  - net.jadecy.cmd: Contains classes for Jadecy usage through command line.
    Principal classes:
    - JadecyMain: Allows for a simple usage of Jadecy from command line.

  - net.jadecy.code: Contains classes for modeling classes and packages trees
    and dependencies, and related utilities.
    Principal classes:
    - ClassData: Represents a class.
    - PackageData: Represents a package.
    - NameFilters: Filters for defining sets of classes or packages.

  - net.jadecy.graph: Contains classes for graphs representations and
    computations.
    Principal classes:
    - InterfaceVertex: Interface for representing a vertex and its successors,
      a graph being a collection of vertices.
    - ReachabilityComputer: Computes dependencies.
    - OneShortestPathComputer: Computes one shortest path.
    - PathsGraphComputer: Computes a graph containing all paths from a set of
      vertices to another.
    - SccsComputer: Computes strongly connected components.
    - CyclesComputer: Computes cycles.
    - SomeCyclesComputer: Computes some cycles.

  - net.jadecy.comp: Contains classes to make it easy to compile Java code
    programmatically, so as to be able to use Jadecy or DepUnit on the resulting
    class files, or to build this library without depending on an IDE or other
    separate build tool.

  - net.jadecy.parsing: Classes for parsing classes and packages dependencies
    from file system or memory.
    Principal classes:
    - ClassDepsParser: Computes direct dependencies of a class.
    - FsDepsParser: Parses dependencies from file system.

- Comparison of JadecyMain (the command-line tool) with jdeps (Oracle):
  - Doesn't necessarily compute the same dependencies with -apionly option
    (but generally the same ones without it).
  - Has no feature related to modules, or origin source or jar files.
  - Does not by default discriminate between JDK internals and other classes,
    not to be tied to JDK structure (but that can be done using custom parsing
    filters, and the filter used to define the set of classes which dependencies
    must be computed among parsed classes).
  - Does not indicate in which file or archive classes were found.
  - The set of files which dependencies must be computed is not given as a list,
    but by a regular expression used against parsed elements.
  - Can compute on graphs of packages dependencies (in addition to class
    dependencies), in which case classes causing each dependency can be
    indicated.
  - Allows to compute depending elements (in addition to elements depended on),
    using inverse dependencies graph.
  - Allows to compute differential dependencies, i.e. the additional dependencies
    caused by a set of elements over those caused by another set of elements.
  - Allows to compute step-by-step dependencies, up to a max amount of steps.
  - Allows to compute byte size of dependencies, for example as a rough measure
    of the associated conceptual weight.
  - Allows to compute one shortest path between two sets of elements,
    or a graph containing all paths up to a max length.
  - Allows to compute strongly connected components (of size > 1).
  - Allows to compute cycles, possibly only up to a certain size for computation
    to terminate faster, or with an non exhaustive but always fast algorithm.
  - Allows to output into a file, whether or not DOT format is used.
  - Considers dependencies of nested classes, but allows to merge them into
    top level class, which is the default.
  - Doesn't by default filter out classes of same package.
  - Is more likely not to be silent, and to output error messages, when
    inputs are broken or things go wrong.

################################################################################
