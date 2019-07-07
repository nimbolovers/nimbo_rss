package in.nimbo.application.cli;

import in.nimbo.application.App;
import in.nimbo.entity.Site;
import in.nimbo.service.schedule.Schedule;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "exit",
        mixinStandardHelpOptions = true,
//        abbreviateSynopsis = true,
        version = RssCLI.version,
        description = "Save data and exit")
public class ExitCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @Override
    public Void call() {
        App app = rssCLI.getApp();
        app.getSchedule().stopService();

        // Save sites before exit
        for (Site site : app.getSchedule().getSites()) {
            if (site.getId() != 0)
                app.getSiteDAO().update(site);
            else
                app.getSiteDAO().save(site);
        }
        return null;
    }
}
