package net.sf.l2j.gameserver.serverpackets;

public class StartAllianceWar extends ServerBasePacket
{
    private static final String _S__BD_STARTALLIANCEWAR = "[S] bd StartAllianceWar";
    private String _allianceName;
    private String _char;

    public StartAllianceWar(String alliance, String charName)
    {
        _allianceName = alliance;
        _char = charName;
    }
    
    final void runImpl(){}
    
    final void writeImpl()
    {
        writeC(0xbd);
        writeS(_char);
        writeS(_allianceName);
    }
    
    public String getType()
    {
        return _S__BD_STARTALLIANCEWAR;
    }
}