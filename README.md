Jadecy stands for Java Dependencies and Cycles computer.

Jadecy provides treatments to compute dependencies, strongly connected
components (SCCs), and cycles, in classes or packages dependencies graphs parsed
from class files, either through API or through command line.
Lower level treatments, such as graph related computations, are also publicly
available.

It's an evolution of http://sourceforge.net/projects/jcycles.

# License

Apache License V2.0

# Current external dependencies

- src/main, src/build, src/samples:
  - Java 5+, except net.jadecy.comp.JavacHelper which uses JavaCompiler
    and thus requires Java 6+

- src/test:
  - Java 8+
  - JUnit 3.8.1 (under lib/junit.jar)

# Principal classes

- Jadecy: The entry point.

- JadecyUtils: Utilities for dealing with Jadecy, in particular its results.

- DepUnit: For cycles and dependencies unit tests.

- JadecyMain: Allows for a simple usage of Jadecy from command line,
  or from code with either main(...) or runArgs(...) methods.

# Miscellaneous

- Handles class files of major version <= 52 (Java 8), and does best effort
  if major version is higher.
  Could compute dependencies out of JDK 9 ea build 95 class files.

- Cannot parse .jimage files, which would require dependency to jrt-fs.jar,
  which itself requires Java 8, and is not trivial to use
  (FileSystemNotFoundException is easy to get), but their content can easily
  be extracted into class files using jimage executable.

- Uses Tarjan's algorithm for SCCs computation, and Johnson's algorithm
  for cycles computation, with continuations instead of recursion,
  which allows to handle large graphs (< 2^31 vertices).

- To avoid spam due to dependencies within a same top level class and its
  recursively nested classes, cycles and SCCs only involving such classes
  are always ignored by Jadecy class treatments.

- Lower level APIs such as for class files parsing, classes and packages trees
  and graphs manipulation, or graph related computations, are also publicly
  available (cf. overview below), and can easily be extracted (sources contain
  no cyclic dependencies, except deliberate ones in tests).

- As a general rule, for convenience, these treatments ensure determinism
  and when possible some ordering in their result.
 
- Sample Jadecy class usage can be found in src/samples/java,
  and more use cases in tests.

- Sample command line usages with .bat files (based on that, UX or MAC users
  should easily be able to do their own scripts) can be found in
  src/samples/scripts, and more use cases in tests.

- Javadoc: Code Javadoc is done in plain text, not using HTML, for simplicity
  and easier writing and reading when working with source code.

# Main sources packages overview

- net.jadecy: Contains treatments to compute dependencies, strongly connected
  components, and cycles, in classes or packages dependencies graphs parsed
  from class files.
  Principal classes:
  - Jadecy: The entry point.
  - JadecyUtils: Utilities for dealing with Jadecy, in particular its results.
  - DepUnit: To check dependencies and cycles in unit tests.

- net.jadecy.cmd: Contains classes for Jadecy usage through command line.
  Principal classes:
  - JadecyMain: Allows for a simple usage of Jadecy from command line,
    or from code with either main(...) or runArgs(...) methods.

- net.jadecy.code: Contains classes for modeling classes and packages trees
  and dependencies, and related utilities.
  Principal classes:
  - ClassData: Represents a class.
  - PackageData: Represents a package.

- net.jadecy.comp: Contains classes to make it easy to compile Java code
  programmatically, so as to be able to use Jadecy or DepUnit on the
  resulting class files, or to build this library.

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
  - CyclesComputer: Computes cycles (all of them).
  - ShortestCyclesComputer: Computes shortest cycles (covering all the edges
    involved in cycles, i.e. of each SCC).
  - SomeCyclesComputer: Computes some cycles (if any, finds at least one).

- net.jadecy.names: Contains classes for dealing with packages or classes
  names.
  Principal classes:
  - NameFilters: Filters for defining sets of classes or packages.
  - NameUtils: Utilities for packages or classes names.

- net.jadecy.parsing: Classes for parsing classes and packages dependencies
  from file system or memory.
  Principal classes:
  - ClassDepsParser: Computes direct dependencies of a class.
  - FsDepsParser: Parses dependencies from file system.

# Comparison of JadecyMain with jdeps (Oracle)

Comparison done in 2015, so with an early version of jdeps.

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
- Allows to compute (shortest) cycles covering all edges of each SCC, or
  all cycles (might take a long time), or some cycles (at least one if any).
- Allows to output into a file, whether or not DOT format is used.
- Considers dependencies of nested classes, but allows to merge them into
  top level class, which is the default.
- Doesn't by default filter out classes of same package.
- Is more likely not to be silent, and to output error messages, when
  inputs are broken or things go wrong.

# Donation

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=P7EYEFUCXBS9J)
