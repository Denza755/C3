package net.sf.l2j.gameserver.serverpackets;

public class StopAllianceWar extends ServerBasePacket
{
    private static final String _S__BF_STOPALLIANCEWAR = "[S] bf StopAllianceWar";
    private String _allianceName;
    private String _char;

    public StopAllianceWar(String alliance, String charName)
    {
        _allianceName = alliance;
        _char = charName;
    }
    
    final void runImpl(){}
    
    final void writeImpl()
    {
        writeC(0xbf);
        writeS(_allianceName);
        writeS(_char);
    }
    
    public String getType()
    {
        return _S__BF_STOPALLIANCEWAR;
    }
}