Build ModelCrawler
==================

When you've cloned the source code:

```sh
git clone git@github.com:SemsProject/ModelCrawler.git
```

There are two supported options to build this project:

* [Build with Maven](#build-with-maven)

Build with Maven 
-----------------

[Maven](https://maven.apache.org/) is a build automation tool. We ship a `pom.xml` together with the sources which tells maven about versions and dependencies. Thus, maven is able to resolve everything on its own and, in order to create the library, all you need to call is `mvn package`:

```sh
usr@srv $ cd ModelCrawler
usr@srv $ mvn package

[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building ModelCrawler 0.2.2
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ ModelCrawler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /users/stud00/mp487/mig-git/modelcrawler/ModelCrawler/res
[INFO] Copying 4 resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ ModelCrawler ---
[INFO] Nothing to compile - all classes are up to date
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ ModelCrawler ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /users/stud00/mp487/mig-git/modelcrawler/ModelCrawler/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ ModelCrawler ---
[INFO] No sources to compile
[INFO] 
[INFO] --- maven-surefire-plugin:2.17:test (default-test) @ ModelCrawler ---
[INFO] No tests to run.
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ ModelCrawler ---
[INFO] Building jar: /users/stud00/mp487/mig-git/modelcrawler/ModelCrawler/target/ModelCrawler-0.2.2.jar
[INFO] 
[INFO] --- maven-assembly-plugin:2.5.4:single (make-assembly) @ ModelCrawler ---
[INFO] Building jar: /users/stud00/mp487/mig-git/modelcrawler/ModelCrawler/target/ModelCrawler-0.2.2-jar-with-dependencies.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 6.067 s
[INFO] Finished at: 2017-02-14T17:14:59+01:00
[INFO] Final Memory: 42M/537M
[INFO] ------------------------------------------------------------------------
```

That done, you'll find the binaries in the `target` directory.
