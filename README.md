# gradle-gzjar-plugin

Task that builds a Jar-file which includes a gzipped version of
every resource. Can be used as a drop-in replacement for normal `Jar`.

```groovy
plugins {
    id "fi.evident.gzjar" version "0.1.3"
}

task frontendJar(type: fi.evident.gradle.gzjar.tasks.GzJar, dependsOn: buildAssets) {
    baseName "frontend-assets"
    from "build/assets"
}

```
