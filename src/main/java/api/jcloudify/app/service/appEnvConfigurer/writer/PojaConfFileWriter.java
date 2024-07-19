package api.jcloudify.app.service.appEnvConfigurer.writer;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import java.io.File;
import java.util.function.Function;

public interface PojaConfFileWriter extends Function<OneOfPojaConf, File> {}
