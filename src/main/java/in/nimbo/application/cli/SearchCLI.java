package in.nimbo.application.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;

@Command(name = "search",
        mixinStandardHelpOptions = true,
//        abbreviateSynopsis = true,
        version = "RSS v1.0",
        description = "Hello to world!")
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
    private Date startDate;

    @CommandLine.Option(names = {"--end"}, paramLabel = "DATE", description = "The name alias for link")
    private Date finishDate;

    @Override
    public Void call() {
        System.out.println(channel);
        System.out.println(title);
        System.out.println(content);
        System.out.println(startDate);
        System.out.println(finishDate);
        return null;
    }
}