package in.nimbo.application.cli;

import in.nimbo.application.App;
import in.nimbo.entity.report.HourReport;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "hour-report",
        version = RssCLI.version,
        description = "Report for each hour for each site"
)
public class HourReportCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI parent;

    @CommandLine.Option(names = "--title", paramLabel = "STRING", description = "Value must be appeared in title of entry")
    private String title;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        App app = parent.getApp();
        List<HourReport> reports = app.getRssService().getHourReports(Utility.removeQuotation(title));
        showHourReports(reports);
        return null;
    }

    public static void showHourReports(List<HourReport> reports) {
        System.out.println();
        for (HourReport report:reports) {
            System.out.println("Channel: " + report.getChannel());
            System.out.println("News: " + report.getCount());
            System.out.println("Hour: " + report.getHour());
            System.out.println("----------");
        }
    }
}
