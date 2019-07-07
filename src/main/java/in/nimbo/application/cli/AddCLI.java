package in.nimbo.application.cli;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "add",
        mixinStandardHelpOptions = true,
//        abbreviateSynopsis = true,
        version = "RSS v1.0",
        description = "Hello to world!")
public class AddCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Parameters(paramLabel = "name", index = "0", description = "The name alias for link")
    private String siteName;

    @CommandLine.Parameters(paramLabel = "link", index = "1", description = "Link of site")
    private String siteLink;

    @Override
    public Void call() {
        System.out.println(siteName + " " + siteLink);
//        rssCLI.getApp().addSite(siteName, siteLink);
        return null;
    }
}
