package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.RecipeBookItemList;

public class RequestRecipeBookDestroy extends ClientBasePacket 
{
    private static final String _C__AC_REQUESTRECIPEBOOKDESTROY = "[C] AD RequestRecipeBookDestroy";
    //private static Logger _log = Logger.getLogger(RequestSellItem.class.getName());

    private int _RecipeID;

    /**
    * Unknown Packet:ad
    * 0000: ad 02 00 00 00
    */
    public RequestRecipeBookDestroy(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);

        _RecipeID = readD();
    }
            
    void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;
        activeChar.unregisterRecipeList(_RecipeID);

        RecipeBookItemList response = new RecipeBookItemList();
        response.addRecipes(activeChar.getRecipeBook());

        activeChar.sendPacket(response);
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType() 
    {
        return _C__AC_REQUESTRECIPEBOOKDESTROY;
    }
}