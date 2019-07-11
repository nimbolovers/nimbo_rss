package in.nimbo.application.cli;

import in.nimbo.application.Utility;
import in.nimbo.entity.Entry;
import in.nimbo.exception.RssServiceException;
import in.nimbo.service.RSSService;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "search",
        version = RssCLI.VERSION,
        description = "Search in entries")
public class SearchCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Option(names = {"--channel"}, paramLabel = "STRING",
            description = "Channel of entry (default: \"${DEFAULT-VALUE}\")")
    private String channel = "";

    @CommandLine.Option(names = {"--title"}, paramLabel = "STRING",
            description = "The text should be appear in title (default: \"${DEFAULT-VALUE}\")")
    private String title = "";

    @CommandLine.Option(names = {"--content"}, paramLabel = "STRING",
            description = "The text should be appear in content (default: \"${DEFAULT-VALUE}\")")
    private String content = "";

    @CommandLine.Option(names = {"--start"}, paramLabel = "DATE",
            description = "The entry publication date must be after start date (format: dd/MM/yyyy HH:mm:ss)")
    private String start;

    @CommandLine.Option(names = {"--end"}, paramLabel = "DATE",
            description = "The entry publication date must be before end date (format: dd/MM/yyyy HH:mm:ss)")
    private String end;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        try {
            List<Entry> resultEntry = filterEntry(rssCLI.getApp().getRssService(), start, end, channel, content, title);
            showEntries(resultEntry);
        } catch (IllegalArgumentException e) {
            Utility.printlnCLI(e.getMessage());
        } catch (RssServiceException e) {
            Utility.printlnCLI("Unable to search data. We will fix it as soon as possible.");
        }
        return null;
    }

    public List<Entry> filterEntry(RSSService rssService, String startTime, String endTime,
                                   String channel, String content, String title) throws RssServiceException {
        LocalDateTime startDate = null;
        LocalDateTime finishDate = null;
        if (startTime != null)
            startDate = Utility.getDate(Utility.removeQuotation(startTime));
        if (endTime != null)
            finishDate = Utility.getDate(Utility.removeQuotation(endTime));
        return rssService.filterEntry(
                Utility.removeQuotation(channel),
                Utility.removeQuotation(content),
                Utility.removeQuotation(title),
                startDate, finishDate);
    }

    /**
     * show a list of entries in tabular format
     *
     * @param entries entries to print
     */
    public void showEntries(List<Entry> entries) {
        for (Entry entry : entries) {
            Utility.printlnCLI("ID: " + entry.getId());
            Utility.printlnCLI("Channel: " + entry.getChannel());
            Utility.printlnCLI("Title: " + entry.getTitle());
            Utility.printlnCLI("----------");
        }
    }
}