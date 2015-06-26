package fi.evident.gradle.gzjar.tasks;

import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

public class GzJar extends AbstractArchiveTask {

    public GzJar() {
        setExtension("jar");
    }

    @Override
    protected CopyAction createCopyAction() {
        return new GzJarCopyAction(getArchivePath());
    }
}
