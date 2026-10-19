package net.sf.l2j.gameserver.serverpackets;

public class NormalCamera extends L2GameServerPacket
{
    private static final String _S__C8_NORMALCAMERA = "[S] C8 NormalCamera";

    public NormalCamera(){}
    
    public void runImpl(){}
    
    public void writeImpl()
    {
        writeC(200);
    }
    
    public String getType()
    {
        return _S__C8_NORMALCAMERA;
    }
}
