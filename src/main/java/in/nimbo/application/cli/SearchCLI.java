package in.nimbo.application.cli;

import in.nimbo.application.Utility;
import in.nimbo.entity.Entry;
import in.nimbo.exception.RssServiceException;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "search",
        version = RssCLI.version,
        description = "Search in entries")
public class SearchCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Option(names = {"--channel"}, paramLabel = "STRING",
            description = "Channel of entry")
    private String channel = "";

    @CommandLine.Option(names = {"--title"}, paramLabel = "STRING",
            description = "The text should be appear in title")
    private String title = "";

    @CommandLine.Option(names = {"--content"}, paramLabel = "STRING",
            description = "The text should be appear in content")
    private String content = "";

    @CommandLine.Option(names = {"--start"}, paramLabel = "DATE",
            description = "The entry publication date must be after start date")
    private String start;

    @CommandLine.Option(names = {"--end"}, paramLabel = "DATE",
            description = "The entry publication date must be before end date")
    private String end;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        try {
            Date startDate = Utility.getDate(start);
            Date finishDate = Utility.getDate(end);
            List<Entry> resultEntry = rssCLI.getApp().getRssService().
                    filterEntry(channel, content, title, startDate, finishDate);
            showEntries(resultEntry);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (RssServiceException e) {
            System.out.println("Unable to search data. We will fix it as soon as possible.");
        }
        return null;
    }

    /**
     * show a list of entries in tabular format
     *
     * @param entries entries to print
     */
    public static void showEntries(List<Entry> entries) {
        for (Entry entry : entries) {
            System.out.println("Channel: " + entry.getChannel());
            System.out.println("Title: " + entry.getTitle());
        }
    }
}