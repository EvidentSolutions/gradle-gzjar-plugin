package fi.evident.gradle.gzjar.tasks;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.internal.tasks.SimpleWorkResult;
import org.gradle.api.tasks.WorkResult;
import org.gradle.internal.IoActions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class GzJarCopyAction implements CopyAction {
    private final File zipFile;

    public GzJarCopyAction(File zipFile) {
        this.zipFile = zipFile;
    }

    public WorkResult execute(final CopyActionProcessingStream stream) {
        ZipOutputStream zipOutStr;

        try {
            zipOutStr = new ZipOutputStream(new FileOutputStream(zipFile));
        } catch (Exception e) {
            throw new GradleException(String.format("Could not create ZIP '%s'.", zipFile), e);
        }

        IoActions.withResource(zipOutStr, new Action<ZipOutputStream>() {
            public void execute(ZipOutputStream outputStream) {
                stream.process(new StreamAction(outputStream));
            }
        });

        return new SimpleWorkResult(true);
    }

    private class StreamAction implements CopyActionProcessingStreamAction {
        private final ZipOutputStream zipOutStr;

        public StreamAction(ZipOutputStream zipOutStr) {
            this.zipOutStr = zipOutStr;
        }

        public void processFile(FileCopyDetailsInternal details) {
            if (details.isDirectory()) {
                visitDir(details);
            } else {
                writeOriginalFile(details);
                writeGzippedFile(details);
            }
        }

        private void writeOriginalFile(FileCopyDetails fileDetails) {
            String path = fileDetails.getRelativePath().getPathString();
            try {
                ZipEntry archiveEntry = new ZipEntry(path);
                archiveEntry.setTime(fileDetails.getLastModified());
                zipOutStr.putNextEntry(archiveEntry);
                fileDetails.copyTo(zipOutStr);
                zipOutStr.closeEntry();
            } catch (Exception e) {
                throw new GradleException(String.format("Could not add %s to ZIP '%s'.", path, zipFile), e);
            }
        }

        private void writeGzippedFile(FileCopyDetails fileDetails) {
            String path = fileDetails.getRelativePath().getPathString() + ".gz";
            try {
                ZipEntry archiveEntry = new ZipEntry(path);
                archiveEntry.setTime(fileDetails.getLastModified());
                zipOutStr.putNextEntry(archiveEntry);
                GZIPOutputStream out = new GZIPOutputStream(zipOutStr, true);
                fileDetails.copyTo(out);
                out.flush();
                zipOutStr.closeEntry();
            } catch (Exception e) {
                throw new GradleException(String.format("Could not add %s to ZIP '%s'.", path, zipFile), e);
            }
        }

        private void visitDir(FileCopyDetails dirDetails) {
            try {
                // Trailing slash in name indicates that entry is a directory
                ZipEntry archiveEntry = new ZipEntry(dirDetails.getRelativePath().getPathString() + '/');
                archiveEntry.setTime(dirDetails.getLastModified());
                zipOutStr.putNextEntry(archiveEntry);
                zipOutStr.closeEntry();
            } catch (Exception e) {
                throw new GradleException(String.format("Could not add %s to ZIP '%s'.", dirDetails, zipFile), e);
            }
        }
    }
}
