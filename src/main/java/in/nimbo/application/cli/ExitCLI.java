package in.nimbo.application.cli;

import in.nimbo.application.App;
import in.nimbo.dao.SiteDAO;
import in.nimbo.entity.Site;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "exit",
        version = RssCLI.VERSION,
        description = "Save data and exit")
public class ExitCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        App app = rssCLI.getApp();
        app.getSchedule().stopService();

        SiteDAO siteDAO = app.getRssService().getSiteDAO();
        for (Site site : siteDAO.getSites()) {
            siteDAO.update(site);
        }

        System.exit(0);
        return null;
    }
}
