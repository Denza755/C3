package net.sf.l2j.extensions;

import ru.balancer.jbforth.Dict;
import net.sf.l2j.extensions.jbforth.*;

public class L2jDict
{
    public L2jDict(Dict dic)
    {
//    	System.out.println("Add ext. words to "+dic);

        new ai(dic);
        new characters(dic);
        new effects(dic);
        new files(dic);
        new hire(dic);
        new items(dic);
        new l2j(dic);
        new player(dic);
        new ride(dic);
        new server(dic);
        new spawn(dic);
        new sql(dic);
        new timer(dic);
    }
}
