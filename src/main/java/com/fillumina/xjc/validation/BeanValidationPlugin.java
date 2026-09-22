package com.fillumina.xjc.validation;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import java.io.IOException;
import org.xml.sax.ErrorHandler;

/**
 * Adds the Jakarta Bean Validation annotations of a schema to the classes XJC generates from it.
 *
 * <p>The plugin is switched on with {@code -XBeanValidationAnnotations} and is registered in
 * {@code META-INF/services/com.sun.tools.xjc.Plugin}.
 *
 * @author Francesco Illuminati
 * @author Vojtech Krasa
 * @author cocorossello
 */
public class BeanValidationPlugin extends Plugin {

    public static final String PLUGIN_NAME = "XBeanValidationAnnotations";
    /** The plugin with the first option of a run, as it is written on the command line. */
    static final String OPTION_PREFIX = "-" + PLUGIN_NAME + ":";

    private final BeanValidationOptions.Builder optionsBuilder = BeanValidationOptions.builder();

    @Override
    public String getOptionName() {
        return PLUGIN_NAME;
    }

    /**
     * Reads one option of this plugin. XJC activates a plugin for the argument equal to {@code "-"}
     * plus {@link #getOptionName()} and hands every other argument to every plugin, so an argument
     * this method does not recognize is left to XJC and the other plugins.
     */
    @Override
    public int parseArgument(Options opt, String[] args, int index)
            throws BadCommandLineException, IOException {
        return optionsBuilder.parseArgument(args[index]);
    }

    @Override
    public String getUsage() {
        return BeanValidationOption.usage();
    }

    @Override
    public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
        optionsBuilder.verbose(opt.verbose);

        BeanValidationOptions options = optionsBuilder.build();

        logger(options).info(BeanValidationOption.valuesInUse(options));

        new Processor(options).process(model);

        return true;
    }

    /** @return the logger of a run, for what is not the annotation of one class or property. */
    private static AnnotationLog logger(BeanValidationOptions options) {
        return options.isVerbose()
                ? new AnnotationLogAll("", "")
                : AnnotationLogWarning.INSTANCE;
    }
}
