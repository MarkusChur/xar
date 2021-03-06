package com.sprylab.xar.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import com.sprylab.xar.toc.model.Encoding;
import com.sprylab.xar.utils.StringUtils;

/**
 * A facade for easily creating xar archives.
 */
public class XarPacker {

    private static final Set<String> DEFAULT_PACK_EXTENSIONS = new HashSet<>();

    static {
        DEFAULT_PACK_EXTENSIONS.add("txt");
        DEFAULT_PACK_EXTENSIONS.add("htm");
        DEFAULT_PACK_EXTENSIONS.add("html");
        DEFAULT_PACK_EXTENSIONS.add("css");
        DEFAULT_PACK_EXTENSIONS.add("js");
        DEFAULT_PACK_EXTENSIONS.add("xml");
        DEFAULT_PACK_EXTENSIONS.add("stxml");
    }

    private final File destFile;

    private final XarSink sink;

    /**
     * Creates a new {@link XarPacker}, which will write to {@code archiveFile}.
     *
     * @param archiveFile the file to write the archive to - will be deleted if exists
     */
    public XarPacker(final File archiveFile) {
        destFile = archiveFile;
        if (destFile.exists()) {
            destFile.delete();
        }
        sink = new XarSink();
    }

    /**
     * Adds a folder or it's content to the archive.
     *
     * @param folder           the folder to add
     * @param asSubFolder      {@code true}, if the folder itself should be added to the archive, {@code false} if only it's content
     * @param packedExtensions a set with all extensions, which should be compressed - if {@code null}, a default list of extensions
     *                         to be compressed will be used
     * @throws Exception if an error occurred
     */
    public void addDirectory(final File folder, final boolean asSubFolder, final Set<String> packedExtensions) throws Exception {
        XarDirectory root = null;
        if (asSubFolder) {
            root = new XarSimpleDirectory(folder.getName());
            sink.addDirectory(root, null);
        }
        addDirectoryContent(folder, root, packedExtensions == null ? DEFAULT_PACK_EXTENSIONS : null);
    }

    /**
     * Adds the content of a folder to the archive.
     *
     * @param folder           the folder to from which to add the content
     * @param parent           the parent folder in the xar archive
     * @param packedExtensions a set with all extensions, which should be compressed - if {@code null}, a default list of extensions
     *                         to be compressed will be used
     * @throws Exception if an error occurred
     */
    public void addDirectoryContent(final File folder, final XarDirectory parent, final Set<String> packedExtensions) throws Exception {
        for (final File file : folder.listFiles()) {
            if (file.isDirectory()) {
                final XarDirectory dir = new XarSimpleDirectory(file.getName());
                sink.addDirectory(dir, parent);
                addDirectoryContent(file, dir, packedExtensions);
            } else {
                final boolean compress = packedExtensions.contains(StringUtils.substringAfterLast(file.getName(), "."));
                final XarEntrySource source = new XarFileSource(file, compress ? Encoding.GZIP : Encoding.NONE);
                sink.addSource(source, parent);
            }
        }
    }

    /**
     * Creates the archive file and writes every entry to it.
     *
     * @throws Exception if an error occurred
     */
    public void write() throws Exception {
        try (final FileOutputStream fos = new FileOutputStream(destFile)) {
            sink.write(fos);
        }
    }

}
