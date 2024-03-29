package in.nimbo.application.cli;

import in.nimbo.application.App;
import in.nimbo.application.Utility;
import in.nimbo.entity.report.DateReport;
import picocli.CommandLine;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "date-report",
    version = RssCLI.VERSION,
    description = "Report for each date for each site"
)
public class DateReportCLI implements Callable<Void> {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @CommandLine.ParentCommand
    private RssCLI parent;

    @CommandLine.Option(names = {"--title"}, paramLabel = "STRING", description = "Value must be appeared in title of entry")
    private String title;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;


    @Override
    public Void call() {
        App app = parent.getApp();
        List<DateReport> reports = app.getRssService().getDateReports(Utility.removeQuotation(title));
        showDateReports(reports);
        return null;
    }

    public static void showDateReports(List<DateReport> reports){
        for (DateReport report:reports) {
            String dateFormatted = formatter.format(report.getDate());
            Utility.printlnCLI("Channel: " + report.getChannel());
            Utility.printlnCLI("News: " + report.getCount());
            Utility.printlnCLI("Date: " + dateFormatted);
            Utility.printlnCLI("----------");
        }
    }
}
