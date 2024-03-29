package in.nimbo.application.cli;

import in.nimbo.application.App;
import in.nimbo.application.Utility;
import in.nimbo.entity.report.HourReport;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "hour-report",
        version = RssCLI.VERSION,
        description = "Report for each hour for each site"
)
public class HourReportCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI parent;

    @CommandLine.Option(names = "--title", paramLabel = "STRING", description = "Value must be appeared in title of entry")
    private String title;

    @CommandLine.Option(names = "--channel", paramLabel = "STRING", description = "Value must be appeared in channel of entry")
    private String channel;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        App app = parent.getApp();
        List<HourReport> reports = app.getRssService().getHourReports(Utility.removeQuotation(title), Utility.removeQuotation(channel));
        showHourReports(reports);
        return null;
    }

    public static void showHourReports(List<HourReport> reports) {
        for (HourReport report:reports) {
            Utility.printlnCLI("Channel: " + report.getChannel());
            Utility.printlnCLI("News: " + report.getCount());
            Utility.printlnCLI("Hour: " + report.getHour());
            Utility.printlnCLI("----------");
        }
    }
}
