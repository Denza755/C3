package net.sf.l2j.gameserver.taskmanager.tasks;

import net.sf.l2j.gameserver.model.entity.SevenSigns;
import net.sf.l2j.gameserver.model.entity.SevenSignsFestival;
import net.sf.l2j.gameserver.taskmanager.Task;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.taskmanager.TaskManager.ExecutedTask;
import net.sf.l2j.gameserver.taskmanager.TaskTypes;

/**
 * Updates all data for the Seven Signs and Festival of Darkness engines,
 * when time is elapsed.
 * 
 * @author Tempy
 */
public class TaskSevenSignsUpdate extends Task
{
    public static final String NAME = "SevenSignsUpdate";
    
    public String getName()
    {
        return NAME;
    }

    public void onTimeElapsed(ExecutedTask task)
    {
        try {
            SevenSigns.getInstance().saveSevenSignsData(null, true);

            if (!SevenSigns.getInstance().isSealValidationPeriod())
                SevenSignsFestival.getInstance().saveFestivalData(false);
            
            System.out.println("SevenSigns: Data updated successfully.");
        }
        catch (Exception e) {
            System.out.println("SevenSigns: Failed to save Seven Signs configuration: " + e);
        }
    }
    
    public void initializate()
    {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "0", "1800000", "");
    }
}
