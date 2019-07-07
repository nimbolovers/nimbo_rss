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
        mixinStandardHelpOptions = true,
//        abbreviateSynopsis = true,
        version = RssCLI.version,
        description = "Search in news")
public class SearchCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Option(names = {"--channel"}, paramLabel = "STRING", description = "The name alias for link")
    private String channel = "";

    @CommandLine.Option(names = {"--title"}, paramLabel = "STRING", description = "The name alias for link")
    private String title = "";

    @CommandLine.Option(names = {"--content"}, paramLabel = "STRING", description = "The name alias for link")
    private String content = "";

    @CommandLine.Option(names = {"--start"}, paramLabel = "DATE", description = "The name alias for link")
    private String start;

    @CommandLine.Option(names = {"--end"}, paramLabel = "DATE", description = "The name alias for link")
    private String end;

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
            System.out.println("Unable to search data. We fix it as soon as possible.");
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