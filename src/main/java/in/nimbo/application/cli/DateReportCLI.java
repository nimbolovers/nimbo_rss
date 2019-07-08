package in.nimbo.application.cli;

import in.nimbo.application.App;
import in.nimbo.application.Utility;
import in.nimbo.entity.report.DateReport;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "date-report",
    version = RssCLI.version,
    description = "Report for each date for each site"
)
public class DateReportCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI parent;

    /*@CommandLine.Parameters()
    private boolean isDate;*/

    @CommandLine.Option(names = {"--title"}, paramLabel = "STRING", description = "Title of entry")
    private String title;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;


    @Override
    public Void call() throws Exception {
        App app = parent.getApp();
        List<DateReport> reports = app.getRssService().getReports(title);
        Utility.showDateReports(reports);
        return null;
    }
}
