package api.jcloudify.app.file;

import api.jcloudify.app.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
