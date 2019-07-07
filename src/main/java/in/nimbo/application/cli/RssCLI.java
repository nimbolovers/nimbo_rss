package in.nimbo.application.cli;

import in.nimbo.application.App;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "rss",
        mixinStandardHelpOptions = true,
//        abbreviateSynopsis = true,
        version = RssCLI.version,
        description = "Hello to world!",
        subcommands = {SearchCLI.class, AddCLI.class})
public class RssCLI implements Callable<Void> {
    private App app;
    static final String version = "RSS v1.0";

    public RssCLI(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    @CommandLine.Parameters
    private String[] parms;

    @Override
    public Void call() {
        System.out.println(Arrays.toString(parms));
        return null;
    }
}