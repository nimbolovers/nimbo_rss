package in.nimbo.application.cli;

import in.nimbo.application.Utility;
import in.nimbo.entity.report.Report;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "all-report",
        description = "Report count of news for each site",
        version = RssCLI.VERSION
)
public class AllReportCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Option(names = {"--title"}, paramLabel = "STRING", description = "Value must be appeared in title of entry")
    private String title;

    @CommandLine.Option(names = {"--date"}, paramLabel = "DATE", description = "The entry publication date must be equals to this date (format: dd/MM/yyyy)")
    private String date;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() throws Exception {
        LocalDateTime dateTime = null;
        if (date != null){
            try {
                dateTime = Utility.getDate(Utility.removeQuotation(date) + " 00:00:00");
            }catch (Exception e){
                Utility.printCLI(e.getMessage());
                return null;
            }
        }
        List<Report> reports = rssCLI.getApp().getRssService().getAllReports(Utility.removeQuotation(title), dateTime);
        showAllReports(reports);
        return null;
    }

    public static void showAllReports(List<Report> reports){
        Utility.printlnCLI();
        for (Report report:reports) {
            Utility.printCLI("Channel: " + report.getChannel());
            Utility.printCLI("News: " + report.getCount());
            Utility.printCLI("----------");
        }
    }
}
