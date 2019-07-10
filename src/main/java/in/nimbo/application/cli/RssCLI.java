package in.nimbo.application.cli;

import in.nimbo.application.App;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "rss",
        version = RssCLI.version,
        description = "Rss application to fetch data from RSS link",
        synopsisHeading      = "%nUsage:%n%n",
        descriptionHeading   = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading    = "%nOptions:%n%n",
        commandListHeading   = "%nCommands:%n%n",
        subcommands = {SearchCLI.class, AddCLI.class, ExitCLI.class,
                DateReportCLI.class, HourReportCLI.class, ContentCLI.class})
public class RssCLI implements Callable<Void> {
    private App app;
    static final String version = "RSS V1.0";

    public RssCLI(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        return null;
    }
}