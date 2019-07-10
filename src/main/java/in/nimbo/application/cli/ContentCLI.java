package in.nimbo.application.cli;

import in.nimbo.entity.Content;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "get-content",
        version = RssCLI.version,
        description = "Get content of an news")
public class ContentCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Parameters(paramLabel = "id", index = "0", description = "ID of news")
    private int entryID;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        Optional<Content> entryContentByID = rssCLI.getApp().getRssService().getEntryContentByID(entryID);
        if (entryContentByID.isPresent()) {
            System.out.println("Content of news (" + entryID + "): ");
            System.out.println(entryContentByID.get().getValue());
        } else {
            System.out.println("There is no news for id: " + entryID);
        }
        return null;
    }
}